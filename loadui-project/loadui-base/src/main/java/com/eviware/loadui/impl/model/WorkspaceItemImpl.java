/*
 * Copyright 2011 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.impl.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.xmlbeans.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.LoadUI;
import com.eviware.loadui.api.discovery.AgentDiscovery.AgentReference;
import com.eviware.loadui.api.events.BaseEvent;
import com.eviware.loadui.api.events.CollectionEvent;
import com.eviware.loadui.api.events.EventHandler;
import com.eviware.loadui.api.events.PropertyEvent;
import com.eviware.loadui.api.model.AgentItem;
import com.eviware.loadui.api.model.CanvasItem;
import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.ProjectRef;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.property.Property;
import com.eviware.loadui.config.AgentItemConfig;
import com.eviware.loadui.config.LoaduiProjectDocumentConfig;
import com.eviware.loadui.config.LoaduiWorkspaceDocumentConfig;
import com.eviware.loadui.config.ProjectReferenceConfig;
import com.eviware.loadui.config.WorkspaceItemConfig;
import com.eviware.loadui.impl.XmlBeansUtils;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.InitializableUtils;
import com.eviware.loadui.util.ReleasableUtils;
import com.eviware.loadui.util.collections.CollectionEventSupport;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

public class WorkspaceItemImpl extends ModelItemImpl<WorkspaceItemConfig> implements WorkspaceItem
{
	public static final Logger log = LoggerFactory.getLogger( WorkspaceItemImpl.class );

	private final File workspaceFile;
	private final ScheduledExecutorService executor;
	private final LoaduiWorkspaceDocumentConfig doc;
	private final CollectionEventSupport<ProjectRefImpl, Void> projectList;
	private final CollectionEventSupport<AgentItemImpl, Void> agentList;
	private final ProjectListener projectListener = new ProjectListener();
	private final AgentListener agentListener = new AgentListener();
	private final Property<Boolean> localMode;
	private final Property<Long> garbageCollectionInterval;

	private ScheduledFuture<?> gcTask = null;

	public static WorkspaceItemImpl loadWorkspace( File workspaceFile ) throws XmlException, IOException
	{
		return InitializableUtils.initialize( new WorkspaceItemImpl( workspaceFile,
				workspaceFile.exists() ? LoaduiWorkspaceDocumentConfig.Factory.parse( workspaceFile )
						: LoaduiWorkspaceDocumentConfig.Factory.newInstance() ) );
	}

	private WorkspaceItemImpl( File workspaceFile, LoaduiWorkspaceDocumentConfig doc )
	{
		super( doc.getLoaduiWorkspace() == null ? doc.addNewLoaduiWorkspace() : doc.getLoaduiWorkspace() );

		executor = BeanInjector.getBean( ScheduledExecutorService.class );

		projectList = CollectionEventSupport.of( this, PROJECT_REFS );
		agentList = CollectionEventSupport.of( this, AGENTS );

		this.doc = doc;
		this.workspaceFile = workspaceFile;

		localMode = createProperty( LOCAL_MODE_PROPERTY, Boolean.class, true );
		createProperty( MAX_THREADS_PROPERTY, Long.class, 1000 );
		createProperty( MAX_THREAD_QUEUE_PROPERTY, Long.class, 10000 );
		createProperty( IMPORT_MISSING_AGENTS_PROPERTY, Boolean.class, false );
		createProperty( SOAPUI_PATH_PROPERTY, File.class );
		createProperty( SOAPUI_SYNC_PROPERTY, Boolean.class );
		createProperty( SOAPUI_CAJO_PORT_PROPERTY, Integer.class, 1198 );
		createProperty( LOADUI_CAJO_PORT_PROPERTY, Integer.class, 1199 );
		createProperty( STATISTIC_RESULTS_PATH, File.class,
				new File( System.getProperty( LoadUI.LOADUI_HOME ), "results" ) );
		createProperty( IGNORED_VERSION_UPDATE, String.class, "" );
		garbageCollectionInterval = createProperty( AUTO_GARBAGE_COLLECTION_INTERVAL, Long.class, 60 ); // using
		// seconds
	}

	@Override
	public void init()
	{
		super.init();

		for( AgentItemConfig agentConfig : getConfig().getAgentArray() )
		{
			AgentItemImpl agent = InitializableUtils.initialize( new AgentItemImpl( this, agentConfig ) );
			agent.addEventListener( BaseEvent.class, agentListener );
			agentList.addItem( agent );
		}

		for( ProjectReferenceConfig projectRefConfig : getConfig().getProjectArray() )
		{
			try
			{
				projectList.addItem( new ProjectRefImpl( this, projectRefConfig ) );
			}
			catch( IOException e )
			{
				log.error( "Unable to load Project: " + projectRefConfig.getLabel(), e );
			}
		}

		log.info( "Workspace '{}' loaded successfully", this );

		runGCTimer();

		addEventListener( PropertyEvent.class, new EventHandler<PropertyEvent>()
		{
			@Override
			public void handleEvent( PropertyEvent event )
			{
				if( AUTO_GARBAGE_COLLECTION_INTERVAL.equals( event.getKey() ) )
				{
					if( gcTask != null )
					{
						gcTask.cancel( true );
						gcTask = null;
					}
					runGCTimer();
				}
			}
		} );
	}

	// run internal GCT
	private void runGCTimer()
	{
		Long interval = garbageCollectionInterval.getValue();

		if( interval != null && interval > 0 )
		{
			log.debug( "Scheduling a garbage collection, for " + interval + " seconds." );
			gcTask = executor.scheduleWithFixedDelay( new Runnable()
			{
				@Override
				@SuppressWarnings( value = "DM_GC", justification = "Explicit GC calls are needed due to a memory issue with Jetty." )
				public void run()
				{
					long old = Runtime.getRuntime().freeMemory();
					System.gc();
					long free = Runtime.getRuntime().freeMemory();
					log.debug( "Ran Garbage Collection, Free Memory changed from " + old + " to " + free
							+ ", total memory = " + Runtime.getRuntime().totalMemory() );
				}
			}, interval, interval, TimeUnit.SECONDS );
		}
	}

	@Override
	public File getWorkspaceFile()
	{
		return workspaceFile;
	}

	@Override
	public void delete()
	{
		release();
		if( !workspaceFile.delete() )
			throw new RuntimeException( "Unable to delete Workspace file: " + workspaceFile.getAbsolutePath() );

		super.delete();
	}

	@Override
	public void release()
	{
		ReleasableUtils.releaseAll( projectList, agentList );

		super.release();
	}

	@Override
	public String getLoaduiVersion()
	{
		return getConfig().getLoaduiVersion();
	}

	@Override
	public ProjectItem createProject( File projectFile, String label, boolean enabled )
	{
		try
		{
			if( projectFile.createNewFile() )
			{
				LoaduiProjectDocumentConfig projectConfig = LoaduiProjectDocumentConfig.Factory.newInstance();
				projectConfig.addNewLoaduiProject().setLabel( label );
				projectConfig.save( projectFile );
				return importProject( projectFile, enabled ).getProject();
			}
			else
				throw new IllegalArgumentException( "File already exists: " + projectFile );
		}
		catch( IOException e )
		{
			throw new RuntimeException( "Could not create project file:", e );
		}
	}

	@Override
	public ProjectRef importProject( File projectFile, boolean enabled ) throws IOException
	{
		if( !projectFile.exists() )
			throw new IllegalArgumentException( "File does not exist: " + projectFile );

		// if project is already in workspace do not import it again.
		for( ProjectRefImpl projectRef : projectList.getItems() )
		{
			if( projectRef.getProjectFile().getAbsolutePath().equals( projectFile.getAbsolutePath() ) )
			{
				return projectRef;
			}
		}

		ProjectReferenceConfig projectRefConfig = getConfig().addNewProject();
		projectRefConfig.setProjectFile( projectFile.getAbsolutePath() );
		projectRefConfig.setEnabled( true );
		ProjectRefImpl ref;
		try
		{
			ref = new ProjectRefImpl( this, projectRefConfig );
			ref.setEnabled( enabled );
			projectList.addItem( ref );
			log.debug( "public ProjectRef importProject" );
			//			fireCollectionEvent( PROJECT_REFS, CollectionEvent.Event.ADDED, ref );
			save();
			return ref;
		}
		catch( IOException e )
		{
			getConfig().removeProject( getConfig().sizeOfProjectArray() - 1 );
			throw e;
		}
	}

	@Override
	public AgentItem createAgent( String url, String label )
	{
		if( !url.startsWith( "http" ) )
			url = "https://" + url;
		if( !url.substring( 6 ).contains( ":" ) )
			url += ":8443";
		if( !url.endsWith( "/" ) )
			url += "/";
		AgentItemConfig agentConfig = getConfig().addNewAgent();
		agentConfig.setUrl( url );
		agentConfig.setLabel( label );
		AgentItemImpl agent = InitializableUtils.initialize( new AgentItemImpl( this, agentConfig ) );
		agent.addEventListener( BaseEvent.class, agentListener );
		agentList.addItem( agent );
		save();
		return agent;
	}

	@Override
	public AgentItem createAgent( AgentReference ref, String label )
	{
		AgentItemConfig agentConfig = getConfig().addNewAgent();
		agentConfig.setUrl( ref.getUrl() );
		agentConfig.setId( ref.getId() );
		agentConfig.setLabel( label );
		AgentItemImpl agent = InitializableUtils.initialize( new AgentItemImpl( this, agentConfig ) );
		agent.addEventListener( BaseEvent.class, agentListener );
		agentList.addItem( agent );
		save();
		return agent;
	}

	@Override
	public Collection<ProjectItem> getProjects()
	{
		Collection<ProjectItem> list = new ArrayList<ProjectItem>();
		for( ProjectRef ref : projectList.getItems() )
			if( ref.isEnabled() )
				list.add( ref.getProject() );
		return list;
	}

	@Override
	public Collection<ProjectRef> getProjectRefs()
	{
		return ImmutableSet.<ProjectRef> copyOf( projectList.getItems() );
	}

	@Override
	public Collection<AgentItemImpl> getAgents()
	{
		return agentList.getItems();
	}

	@Override
	public void removeProject( final ProjectRef projectRef )
	{
		if( !( projectRef instanceof ProjectRefImpl && projectList.removeItem( ( ProjectRefImpl )projectRef,
				new Runnable()
				{
					@Override
					public void run()
					{
						ReleasableUtils.release( projectRef );
						for( int i = 0; i < getConfig().sizeOfProjectArray(); i++ )
						{
							if( getConfig().getProjectArray( i ) == ( ( ProjectRefImpl )projectRef ).getConfig() )
							{
								getConfig().removeProject( i );
								save();
								return;
							}
						}
					}
				} ) ) )
		{
			throw new IllegalArgumentException( "Project does not belong to this Workspace" );
		}
	}

	@Override
	public void removeProject( ProjectItem project )
	{
		Preconditions.checkNotNull( project, "Project is null" );

		for( ProjectRefImpl ref : projectList.getItems() )
		{
			if( project == ref.getProject() )
			{
				removeProject( ref );
				return;
			}
		}

		throw new IllegalArgumentException( "Project does not belong to this Workspace" );
	}

	@Override
	public void removeAgent( final AgentItem agent )
	{
		Preconditions.checkNotNull( agent, "Agent is null" );

		if( agent instanceof AgentItemImpl && !agentList.removeItem( ( AgentItemImpl )agent, new Runnable()
		{
			@Override
			public void run()
			{
				for( int i = 0; i < getConfig().sizeOfAgentArray(); i++ )
				{
					if( getConfig().getAgentArray( i ) == ( ( AgentItemImpl )agent ).getConfig() )
					{
						getConfig().removeAgent( i );
						save();
						return;
					}
				}
			}
		} ) )
			throw new IllegalArgumentException( "Agent does not belong to this Workspace" );
	}

	public void projectLoaded( ProjectItem project )
	{
		log.debug( "public void projectLoaded" );
		fireCollectionEvent( PROJECTS, CollectionEvent.Event.ADDED, project );
		project.addEventListener( BaseEvent.class, projectListener );
	}

	@Override
	public void save()
	{
		try
		{
			if( !workspaceFile.exists() )
				if( !workspaceFile.createNewFile() )
					throw new IOException( "Unable to create file: " + workspaceFile.getAbsolutePath() );

			log.info( "Saving Workspace to file: '{}'", workspaceFile );
			XmlBeansUtils.saveToFile( doc, workspaceFile );
		}
		catch( IOException e )
		{
			log.error( "Error saving Workspace!", e );
		}
	}

	@Override
	public boolean isLocalMode()
	{
		return localMode.getValue();
	}

	@Override
	public void setLocalMode( boolean localMode )
	{
		if( localMode != isLocalMode() )
		{
			triggerAction( CanvasItem.COMPLETE_ACTION );
			// triggerAction( CounterHolder.COUNTER_RESET_ACTION ); // shouldn't be
			// needed?
			this.localMode.setValue( localMode );
		}
	}

	private class ProjectListener implements EventHandler<BaseEvent>
	{
		@Override
		public void handleEvent( BaseEvent event )
		{
			if( event.getKey().equals( RELEASED ) )
				fireCollectionEvent( PROJECTS, CollectionEvent.Event.REMOVED, event.getSource() );
			else if( event.getKey().equals( DELETED ) )
				removeProject( ( ProjectItem )event.getSource() );
		}
	}

	private class AgentListener implements EventHandler<BaseEvent>
	{
		@Override
		public void handleEvent( BaseEvent event )
		{
			if( event.getKey().equals( DELETED ) )
			{
				removeAgent( ( AgentItem )event.getSource() );
			}
			else if( event instanceof PropertyEvent && AgentItem.MAX_THREADS_PROPERTY.equals( event.getKey() ) )
			{
				( ( AgentItem )event.getSource() ).sendMessage( AgentItem.AGENT_CHANNEL, Collections.singletonMap(
						AgentItem.SET_MAX_THREADS, ( ( PropertyEvent )event ).getProperty().getStringValue() ) );
			}
		}
	}
}

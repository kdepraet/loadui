/*
 * Copyright 2013 SmartBear Software
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.test.ui.fx.states;

import com.eviware.loadui.api.events.EventFirer;
import com.eviware.loadui.api.model.ProjectRef;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.model.WorkspaceProvider;
import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.test.ui.fx.FxIntegrationBase;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.test.TestUtils;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.loadui.testfx.Assertions.verifyThat;

/**
 * Integration tests for testing the loadUI controller through its API.
 *
 * @author dain.nilsson
 */
@Category(IntegrationTest.class)
public class ProjectCreatedWithoutAgentsStateTest extends FxIntegrationBase
{
	private String nameOfCreatedProject;

	@Before
	public void setup() throws Exception
	{
		ProjectCreatedWithoutAgentsState.STATE.enter();
	}

	@After
	public void cleanup() throws Exception
	{
		if( nameOfCreatedProject != null )
		{
			try
			{
				ProjectRef ref = assertProjectExistsWithName( nameOfCreatedProject );
				ref.delete( true );
			}
			catch( Exception e )
			{
				System.out.println( getClass().getName() + ": Exception during cleanup -> " + e );
			}
		}
		ProjectCreatedWithoutAgentsState.STATE.getParent().enter();
	}

	@Test
	public void shouldHaveProject()
	{
		WorkspaceItem workspace = getWorkspace();
		assertThat( workspace.getProjectRefs().size(), is( 1 ) );
	}

	@Test
	public void shouldRenameProjectThoughMenuButton()
	{
		String newName = "Renamed Project";
		renameProjectThroughMenuButton( newName );
		assertProjectExistsWithName( newName );
	}

	@Test
	public void shouldRenameProjectThoughContextMenu()
	{
		String newName = "Another Project";
		renameProjectThroughContextMenu( newName );
		assertProjectExistsWithName( newName );
	}

	@Test
	public void shouldCloneProjectThroughMenuButton()
	{
		nameOfCreatedProject = "Cloned";
		WorkspaceItem workspace = getWorkspace();
		int projectCount = workspace.getProjectRefs().size();

		cloneProjectThroughMenuButton( nameOfCreatedProject );

		assertProjectCountIs( projectCount + 1 );
		assertProjectExistsWithName( nameOfCreatedProject );
	}

	@Test
	public void shouldCloneProjectThroughContextMenu()
	{
		nameOfCreatedProject = "Cloned2";
		WorkspaceItem workspace = getWorkspace();
		int projectCount = workspace.getProjectRefs().size();

		cloneProjectThroughContextMenu( nameOfCreatedProject );

		assertProjectCountIs( projectCount + 1 );
		assertProjectExistsWithName( nameOfCreatedProject );
	}

	@Test
	public void shouldCreateProjectThroughContextMenu()
	{
		nameOfCreatedProject = "Awesome Project";
		WorkspaceItem workspace = getWorkspace();
		int projectCount = workspace.getProjectRefs().size();

		createProjectThroughContextMenu( nameOfCreatedProject );

		sleep( 1500 ); // extra precaution due to assertion failed in jenkins

		assertProjectCountIs( projectCount + 1 );
		assertProjectExistsWithName( nameOfCreatedProject );
	}

	private void renameProjectThroughMenuButton( String newProjectName )
	{
		click( "#projectRefCarousel .project-ref-view #menuButton" ).click( "#rename-item" )
				.type( newProjectName ).type( KeyCode.ENTER );
		waitOnProjectCarouselEvents();
	}

	private void renameProjectThroughContextMenu( String newProjectName )
	{
		move( "#projectRefCarousel .project-ref-view" ).click( MouseButton.SECONDARY )
				.click( "#rename-item" ).type( newProjectName ).type( KeyCode.ENTER );
		waitOnProjectCarouselEvents();
	}

	private ProjectRef assertProjectExistsWithName( final String expectedName )
	{
		WorkspaceItem workspace = getWorkspace();
		return Iterables.find( workspace.getProjectRefs(), new Predicate<ProjectRef>()
		{
			@Override
			public boolean apply( ProjectRef input )
			{
				return input.getLabel().equals( expectedName );
			}
		} );
	}

	private void cloneProjectThroughMenuButton( String name )
	{
		click( "#projectRefCarousel .project-ref-view .menu-button" ).click( "#clone-item" )
				.type( name ).click( ".check-box" ).click( "#default" );
		waitOnProjectCarouselEvents();
	}

	private void cloneProjectThroughContextMenu( String name )
	{
		click( "#projectRefCarousel .project-ref-view .menu-button" ).click( "#clone-item" )
				.type( name ).click( ".check-box" ).click( "#default" );
		waitOnProjectCarouselEvents();
	}

	private void createProjectThroughContextMenu( String name )
	{
		move( "#projectRefCarousel .prev" ).click( MouseButton.SECONDARY ).sleep( 500 )
				.click( "#create-item" ).type( name ).click( ".check-box" ).click( "#default" );
		waitOnProjectCarouselEvents();
	}

	private void assertProjectCountIs( int expectedCount )
	{
		WorkspaceItem workspace = getWorkspace();
		verifyThat( workspace.getProjectRefs().size(), is( expectedCount ) );
	}

	private WorkspaceItem getWorkspace()
	{
		return BeanInjector.getBean( WorkspaceProvider.class ).getWorkspace();
	}

	private void waitOnProjectCarouselEvents()
	{
		try
		{
			TestUtils.awaitEvents( ( EventFirer )find( "#projectRefCarousel" ) );
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

}

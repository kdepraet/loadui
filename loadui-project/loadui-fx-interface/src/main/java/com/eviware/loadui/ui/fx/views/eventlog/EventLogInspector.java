package com.eviware.loadui.ui.fx.views.eventlog;

import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPaneBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.statistics.store.ExecutionManager;
import com.eviware.loadui.api.testevents.TestEvent;
import com.eviware.loadui.api.testevents.TestEventManager;
import com.eviware.loadui.ui.fx.api.Inspector;
import com.eviware.loadui.ui.fx.api.intent.IntentEvent;
import com.eviware.loadui.ui.fx.api.perspective.PerspectiveEvent;
import com.eviware.loadui.util.statistics.ExecutionListenerAdapter;
import com.google.common.collect.Lists;

public class EventLogInspector implements Inspector
{
	private static final String FILTER = PerspectiveEvent.getPath( PerspectiveEvent.PERSPECTIVE_PROJECT ) + ".*";

	protected static final Logger log = LoggerFactory.getLogger( EventLogInspector.class );

	private final StackPane panel;
	private final ExecutionManager executionManager;
	private final TestEventManager testEventManager;

	private final Property<Execution> execution = new SimpleObjectProperty<>( this, "execution" );
	private final EventLogView eventLog = new EventLogView( execution );

	public EventLogInspector( ExecutionManager executionManager, TestEventManager testEventManager )
	{
		this.executionManager = executionManager;
		this.testEventManager = testEventManager;

		panel = StackPaneBuilder.create().padding( new Insets( 10 ) ).styleClass( "inspector" ).children( eventLog )
				.build();

		executionManager.addExecutionListener( new CurrentExecutionListener() );
		execution.addListener( new ChangeListener<Execution>()
		{
			@Override
			public void changed( ObservableValue<? extends Execution> arg0, Execution oldExecution,
					final Execution newExecution )
			{
				if( newExecution == null )
				{
					eventLog.getItems().clear();
				}
				else
				{
					Platform.runLater( new Runnable()
					{

						@Override
						public void run()
						{
							eventLog.fireEvent( IntentEvent.create( IntentEvent.INTENT_RUN_BLOCKING, new Task<Void>()
							{

								@Override
								protected Void call() throws Exception
								{
									log.debug( "loading execution to Event Log" );

									updateMessage( "Fetching TestEvents" );
									eventLog.getItems().setAll(
											Lists.newArrayList( newExecution.getTestEventRange( 0, Long.MAX_VALUE ) ) );
									return null;
								}
							} ) );

						}
					} );

				}
			}
		} );
	}

	@Override
	public void initialize( ReadOnlyProperty<Scene> sceneProperty )
	{
		sceneProperty.addListener( new ChangeListener<Scene>()
		{
			@Override
			public void changed( ObservableValue<? extends Scene> arg0, Scene oldScene, Scene newScene )
			{
				initSceneListener( newScene );
			}
		} );
		initSceneListener( sceneProperty.getValue() );
	}

	private void initSceneListener( Scene scene )
	{
		if( scene != null )
		{
			scene.addEventHandler( IntentEvent.INTENT_OPEN, new EventHandler<IntentEvent<?>>()
			{
				@Override
				public void handle( IntentEvent<?> event )
				{
					if( event.getArg() instanceof Execution && !execution.isBound() )
					{
						execution.setValue( ( Execution )event.getArg() );
					}
				}
			} );
		}
	}

	@Override
	public String getName()
	{
		return "Event Log";
	}

	@Override
	public Node getPanel()
	{
		return panel;
	}

	@Override
	public void onShow()
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void onHide()
	{
		// TODO Auto-generated method stub
	}

	@Override
	public String getHelpUrl()
	{
		return null;
	}

	@Override
	public String getPerspectiveRegex()
	{
		return FILTER;
	}

	private final class CurrentExecutionListener extends ExecutionListenerAdapter
	{
		private final TestEventManager.TestEventObserver entryObserver = new EntryObserver();

		@Override
		public void executionStarted( ExecutionManager.State oldState )
		{
			execution.bind( new ReadOnlyObjectWrapper<>( executionManager.getCurrentExecution() ) );
			testEventManager.registerObserver( entryObserver );
		}

		@Override
		public void executionStopped( ExecutionManager.State oldState )
		{
			execution.unbind();
			testEventManager.unregisterObserver( entryObserver );
		}

		private final class EntryObserver implements TestEventManager.TestEventObserver
		{
			@Override
			public void onTestEvent( final TestEvent.Entry eventEntry )
			{
				Platform.runLater( new Runnable()
				{
					@Override
					public void run()
					{
						eventLog.getItems().add( eventEntry );
					}
				} );
			}
		}
	}
}

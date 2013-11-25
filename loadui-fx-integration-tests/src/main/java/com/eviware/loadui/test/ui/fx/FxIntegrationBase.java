package com.eviware.loadui.test.ui.fx;

import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.api.model.WorkspaceProvider;
import com.eviware.loadui.ui.fx.util.test.ComponentHandle;
import com.eviware.loadui.ui.fx.util.test.LoadUiRobot;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.test.TestUtils;
import com.google.code.tempusfugit.temporal.Condition;
import com.google.code.tempusfugit.temporal.Timeout;
import com.google.common.collect.Lists;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Region;
import org.loadui.testfx.GuiTest;

import java.awt.*;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.google.code.tempusfugit.temporal.Duration.seconds;
import static com.google.code.tempusfugit.temporal.Timeout.timeout;
import static com.google.code.tempusfugit.temporal.WaitFor.waitOrTimeout;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.loadui.testfx.Matchers.nodeVisible;
import static org.loadui.testfx.Matchers.visible;

/**
 * @author Henrik
 */
public class FxIntegrationBase extends GuiTest
{
	protected final LoadUiRobot robot;

	public enum RunBlocking
	{
		BLOCKING, NON_BLOCKING
	}

	public FxIntegrationBase()
	{
		robot = LoadUiRobot.usingController( this );
	}

	public void create( LoadUiRobot.Component component )
	{
		robot.createComponent( component );
	}

	public void createAt( LoadUiRobot.Component component, Point targetPoint )
	{
		robot.createComponentAt( component, targetPoint );
	}

	public void runTestFor( int number, TimeUnit unit )
	{
		runTestFor( number, unit, RunBlocking.BLOCKING );
	}

	public void runTestFor( final int number, final TimeUnit unit, RunBlocking blocking )
	{
		if( blocking == RunBlocking.NON_BLOCKING )
		{
			new Thread( new Runnable()
			{
				@Override
				public void run()
				{
					robot.runTestFor( number, unit );
				}
			} ).start();
		}
		else
		{
			robot.runTestFor( number, unit );
		}
	}

	public void clickPlayStopButton()
	{
		click( ".project-playback-panel .play-button" );
	}

	public void waitForBlockingTaskToComplete()
	{
		waitUntil( ".task-progress-indicator", is( not( visible() ) ) );
	}

	protected void ensureProjectIsNotRunning()
	{
		final ProjectItem project = getProjectItem();
		if( project.isRunning() )
		{
			try
			{
				waitOrTimeout( new IsCanvasRunning( project, false ), timeout( seconds( 5 ) ) );
				System.out.println( "Project stopped running" );
			}
			catch( InterruptedException | TimeoutException e )
			{
				e.printStackTrace();
				robot.clickPlayStopButton();
				ensureProjectIsNotRunning();
			}
		}
		waitForNodeToDisappear( "#abort-requests" );
	}

	public ComponentHandle connect( LoadUiRobot.Component component )
	{
		return robot.createComponent( component );
	}

	public void waitForNode( final String domQuery )
	{
		TestUtils.awaitCondition( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return !findAll( domQuery ).isEmpty();
			}
		} );
	}

	public void waitForNodeToDisappear( final String domQuery, Timeout timeout )
	{
		try
		{
			waitOrTimeout( new Condition()
			{
				@Override
				public boolean isSatisfied()
				{
					return findAll( domQuery ).isEmpty();
				}
			}, timeout );
		}
		catch( Exception e )
		{
			throw new RuntimeException( e );
		}

	}

	public void waitForNodeToDisappear( final String domQuery )
	{
		waitForNodeToDisappear( domQuery, timeout( seconds( 5 ) ) );
	}

	public void setTestTimeLimitTo( int seconds )
	{
		click( "#set-limits" ).click( "#time-limit" ).doubleClick()
				.type( Integer.toString( seconds ) ).click( "#default" );
	}

	public KnobHandle turnKnobIn( LoadUiRobot.Component component )
	{
		return turnKnobIn( component, 1 );
	}

	public KnobHandle turnKnobIn( LoadUiRobot.Component component, int number )
	{
		final Node componentNode = robot.getComponentNode( component );
		System.out.println( "Component node: " + componentNode );
		Set<Node> knobs = findAll( "#knob", componentNode );

		Node knob = Lists.newArrayList( knobs ).get( number - 1 );

		return new KnobHandle( knob );
	}

	public void ensureResultViewWindowIsClosed()
	{
		if( isResultViewWindowIsOpen() )
		{
			closeCurrentWindow();
		}
	}

	public boolean isResultViewWindowIsOpen()
	{
		return !GuiTest.findAll( ".result-view" ).isEmpty();
	}

	public boolean isInspectorViewOpen()
	{
		final Set<Node> inspectorViews = findAll( ".inspector-view" );
		if( inspectorViews.isEmpty() ) return false;

		final Region view = ( Region )inspectorViews.iterator().next();
		try
		{
			waitOrTimeout( new Condition()
			{
				@Override
				public boolean isSatisfied()
				{
					return view.getHeight() > 150;
				}
			}, timeout( seconds( 2 ) ) );
		}
		catch( Exception e )
		{
			return false;
		}
		return true;
	}

	public void ensureInspectorViewIsClosed()
	{
		final Set<Node> inspectorViews = findAll( ".inspector-view" );
		if( inspectorViews.isEmpty() ) return;

		final Region view = ( Region )inspectorViews.iterator().next();
		double height = view.getHeight();
		if( height > 50 )
		{
			drag( ".inspector-bar" ).by( 0, height ).drop();
		}
	}

	public void ensureNotificationPanelIsNotVisible()
	{
		Set<Node> panels = findAll( ".notification-panel" );
		if( panels.isEmpty() ) return;

		Node panel = panels.iterator().next();
		if( panel.isVisible() && panel.getOpacity() > 0.99 )
		{
			click( "#hide-notification-panel" );
			waitUntil( panel, is( not( nodeVisible() ) ) );
		}
	}

	protected void clickOnAbortButton()
	{
		click( "#abort-requests" ).sleep( 1_000 );
	}

	public static ProjectItem getProjectItem()
	{
		return getWorkspaceItem().getProjects().iterator().next();
	}

	public static WorkspaceItem getWorkspaceItem()
	{
		return BeanInjector.getBean( WorkspaceProvider.class ).getWorkspace();
	}

	public class KnobHandle
	{
		final Node knob;

		KnobHandle( Node knob )
		{
			this.knob = knob;
		}

		public KnobHandle to( long value )
		{
			doubleClick( knob ).type( Long.toString( value ) ).type( KeyCode.ENTER );
			return this;
		}

		public KnobHandle to( String value )
		{
			doubleClick( knob ).type( value ).type( KeyCode.ENTER );
			return this;
		}
	}

}

package com.eviware.loadui.test.ui.fx.tablelog;

import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.test.ui.fx.FxIntegrationBase;
import com.eviware.loadui.util.test.TestUtils;
import com.google.common.util.concurrent.SettableFuture;
import javafx.application.Platform;
import javafx.scene.Node;

import java.util.Collection;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.loadui.testfx.GuiTest.findAll;

public class TableLogTestSupport
{
	public static Set<Node> tableRows()
	{
		return findAll( ".component-view .table-row-cell" );
	}


	static void assertCanRunEventInJavaFxThreadWithin( int limit, TimeUnit timeUnit )
	{
		final AtomicBoolean atomicBoolean = new AtomicBoolean( false );
		Platform.runLater( new Runnable()
		{
			@Override
			public void run()
			{
				atomicBoolean.set( true );
			}
		} );

		final String EMPTY_ERROR = "_______NO_ERROR_____";
		final SettableFuture<String> future = SettableFuture.create();
		Timer timer = new Timer( "assertCanRunEventInJavaFxThread" );
		timer.schedule( new TimerTask()
		{
			@Override
			public void run()
			{
				if( !atomicBoolean.get() )
					future.set( "Event did not occur within time limit" );
				else
					future.set( EMPTY_ERROR );
			}
		}, timeUnit.toMillis( limit ) );

		String error;
		try
		{
			error = future.get( limit * 2, timeUnit );
		}
		catch( InterruptedException | ExecutionException e )
		{
			e.printStackTrace();
			error = "Exception while waiting for future to return: " + e;
		}
		catch( TimeoutException e )
		{
			error = "Timeout waiting for future to return";
		}

		assertThat( error, is( EMPTY_ERROR ) );
	}

	public static void waitForProjectToHaveRunningAs( final boolean running )
	{
		Collection<? extends ProjectItem> projects = FxIntegrationBase.getWorkspaceItem().getProjects();
		final ProjectItem project = projects.iterator().next();
		TestUtils.awaitCondition( new Callable<Boolean>()
		{
			@Override
			public Boolean call() throws Exception
			{
				return project.isRunning() == running;
			}
		} );
	}


}

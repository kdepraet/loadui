package com.eviware.loadui.ui.fx.views.inspector;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.TimelineBuilder;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.util.Duration;

import com.eviware.loadui.ui.fx.util.FXMLUtils;

public class InspectorView extends TabPane
{
	private final BooleanProperty minimizedProperty = new SimpleBooleanProperty( this, "minimized", true );

	private boolean dragging = false;
	private double startY = 0;
	private double lastHeight = 250;

	private Region tabHeaderArea;

	public InspectorView()
	{
		FXMLUtils.load( this );

		//This needs to be deferred so that tabHeaderArea has been created before init() is invoked.
		Platform.runLater( new Runnable()
		{
			@Override
			public void run()
			{
				init();
			}
		} );
	}

	private double boundHeight( double desiredHeight )
	{
		Tab selectedTab = getSelectionModel().getSelectedItem();
		if( selectedTab != null )
		{
			Node selectedNode = selectedTab.getContent();
			if( selectedNode != null )
			{
				desiredHeight = Math.min( desiredHeight, selectedNode.maxHeight( -1 ) );
			}
		}
		return Math.max( tabHeaderArea.prefHeight( -1 ), desiredHeight );
	}

	private void init()
	{
		tabHeaderArea = ( Region )lookup( ".tab-header-area" );
		setMaxHeight( boundHeight( 0 ) );

		tabHeaderArea.setCursor( Cursor.V_RESIZE );

		getSelectionModel().selectedItemProperty().addListener( new InvalidationListener()
		{
			@Override
			public void invalidated( Observable arg0 )
			{
				double oldHeight = getMaxHeight();
				double newHeight = boundHeight( oldHeight );
				if( newHeight < oldHeight - 5.0 )
				{
					TimelineBuilder
							.create()
							.keyFrames(
									new KeyFrame( Duration.seconds( 0.1 ), new KeyValue( maxHeightProperty(), newHeight,
											Interpolator.EASE_BOTH ) ) ).build().playFromStart();
				}
				else
				{
					setMaxHeight( newHeight );
				}
			}
		} );

		tabHeaderArea.addEventHandler( MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>()
		{
			@Override
			public void handle( MouseEvent event )
			{
				startY = event.getScreenY() + getHeight();
			}
		} );
		tabHeaderArea.addEventHandler( MouseEvent.DRAG_DETECTED, new EventHandler<MouseEvent>()
		{
			@Override
			public void handle( MouseEvent event )
			{
				dragging = true;
				minimizedProperty.set( false );
			}
		} );
		tabHeaderArea.addEventHandler( MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>()
		{
			@Override
			public void handle( MouseEvent event )
			{
				if( dragging )
				{
					setMaxHeight( boundHeight( startY - event.getScreenY() ) );
				}
			}
		} );
		tabHeaderArea.addEventHandler( MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>()
		{
			@Override
			public void handle( MouseEvent event )
			{
				dragging = false;
				if( getHeight() > getMaxHeight() )
				{
					minimizedProperty.set( true );
				}
			}
		} );

		tabHeaderArea.addEventHandler( MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>()
		{
			@Override
			public void handle( MouseEvent event )
			{
				if( event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 )
				{
					double target = boundHeight( 0 );
					if( minimizedProperty.get() )
					{
						target = boundHeight( lastHeight );
					}
					else
					{
						lastHeight = getHeight();
					}

					TimelineBuilder
							.create()
							.keyFrames(
									new KeyFrame( Duration.seconds( 0.2 ), new KeyValue( maxHeightProperty(), target,
											Interpolator.EASE_BOTH ) ) ).onFinished( new EventHandler<ActionEvent>()
							{
								@Override
								public void handle( ActionEvent arg0 )
								{
									minimizedProperty.set( !minimizedProperty.get() );
								}
							} ).build().playFromStart();
				}
			}
		} );
	}
}

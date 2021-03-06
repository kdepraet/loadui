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
package com.eviware.loadui.ui.fx.views.canvas;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressBarBuilder;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderPaneBuilder;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.Priority;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.util.StringUtils;

public class ToolbarCounterDisplay extends CounterDisplay
{
	protected static final Logger log = LoggerFactory.getLogger( ToolbarCounterDisplay.class );

	private long limit;
	private final ProgressBar progress;
	private Label limitDisplay;
	private Label separationSlash;

	public ToolbarCounterDisplay( @Nonnull String name, @Nonnull Formatting formatting )
	{
		this.formatting = formatting;

		numberDisplay = numberDisplay();
		numberDisplay.setAlignment( Pos.CENTER_RIGHT );
		separationSlash = LabelBuilder.create().style( "-fx-text-fill: #f2f2f2; -fx-font-size: 10px;" )
				.alignment( Pos.CENTER ).text( "/" ).build();
		limitDisplay = limitDisplay();

		BorderPane numberAndLimitDisplay = BorderPaneBuilder
				.create()
				.prefWidth( 100 )
				.maxWidth( 160 )
				.center( HBoxBuilder.create().children( numberDisplay, separationSlash ).build() )
				.right( limitDisplay )
				.style(
						"-fx-background-color: linear-gradient(to bottom, #545454 0%, #000000 50%, #000000 100%); -fx-padding: 0 6 0 6; -fx-background-radius: 5; -fx-border-width: 1; -fx-border-color: #333333; -fx-border-radius: 4; " )
				.build();

		progress = progressBar();

		Label label = label ( name );

		HBox labelAndProgress = HBoxBuilder.create().children( label, progress ).spacing( 3 ).alignment( Pos.BOTTOM_LEFT )
				.build();

		HBox.setHgrow( label, Priority.NEVER );
		HBox.setHgrow( progress, Priority.ALWAYS );

		getChildren().setAll( numberAndLimitDisplay, labelAndProgress );
		setSpacing( 1 );
		setAlignment( Pos.CENTER );
		setLimit( limit );
	}

	private Label limitDisplay()
	{
		return LabelBuilder.create().minWidth( 40 ).prefWidth( 45 ).alignment( Pos.CENTER_RIGHT )
				.style( "-fx-text-fill: #f2f2f2; -fx-font-size: 10px; " ).build();
	}

	public ToolbarCounterDisplay( String name )
	{
		this( name, Formatting.NONE );
	}

	@Override
	public void setValue( long value )
	{
		if( formatting == Formatting.TIME )
			numberDisplay.setText( StringUtils.toHhMmSs( value ) );
		else
			numberDisplay.setText( String.valueOf( value ) );
		progress.setProgress( ( double )value / ( double )limit );
	}

	public void setLimit( long newLimit )
	{
		if( newLimit != 0 )
		{
			this.limit = newLimit;

			if( formatting == Formatting.TIME )
			{
				limitDisplay.setText( StringUtils.toHhMmSs( limit ) );
			}
			else
			{
				limitDisplay.setText( String.valueOf( limit ) );
			}

			separationSlash.setVisible( limit != -1 );
			limitDisplay.setVisible( limit != -1 );
			progress.setVisible( limit != -1 );
		}
	}

	private static ProgressBar progressBar()
	{
		return ProgressBarBuilder.create().prefWidth( 78 ).style( "-fx-scale-y: 0.6; " ).visible( false ).build();
	}
}

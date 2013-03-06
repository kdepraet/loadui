package com.eviware.loadui.ui.fx.views.analysis.linechart;

import static javafx.beans.binding.Bindings.when;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.MenuButton;
import javafx.scene.control.Slider;
import javafx.scene.control.SliderBuilder;
import javafx.scene.layout.Region;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.statistics.model.chart.ChartView;
import com.eviware.loadui.api.statistics.model.chart.line.LineSegment;
import com.eviware.loadui.ui.fx.control.skin.StyleableGraphicSlider;
import com.eviware.loadui.ui.fx.util.FXMLUtils;
import com.eviware.loadui.ui.fx.views.analysis.ShortName;

public class LineSegmentView extends SegmentView<LineSegment>
{
	public static final String STROKE_ATTRIBUTE = "stroke";
	public static final String WIDTH_ATTRIBUTE = "width";
	public static final String SCALE_ATTRIBUTE = "scale";

	@FXML
	private MenuButton menuButton;

	private SegmentBox parent;

	private Slider slider;

	private Region sliderNob;

	private final String scalingStyleClass = "scaling";

	protected static final Logger log = LoggerFactory.getLogger( LineSegmentView.class );

	private final BooleanProperty scaling = new SimpleBooleanProperty( true );
	private int scale = 0;
	private final ReadOnlyBooleanProperty isExpandedProperty;

	public LineSegmentView( LineSegment segment, ChartView chartView, ReadOnlyBooleanProperty isExpandedProperty )
	{
		super( segment, chartView );
		this.isExpandedProperty = isExpandedProperty;
		FXMLUtils.load( this );
	}

	@FXML
	private void initialize()
	{
		super.init();
		slider = SliderBuilder.create().snapToTicks( true ).pickOnBounds( true ).visible( false ).min( -6 ).max( 6 )
				.majorTickUnit( 1 ).minorTickCount( 0 ).build();

		segmentLabel.minWidthProperty().bind( Bindings.when( isExpandedProperty ).then( 250 ).otherwise( 180 ) );
		segmentLabel.maxWidthProperty().bind( Bindings.when( isExpandedProperty ).then( 320 ).otherwise( 200 ) );

		String fullName = segment.getStatisticHolder().getLabel() + " " + segment.getVariableName() + " "
				+ segment.getStatisticName();
		String shortName = ShortName.forStatistic( segment.getVariableName(), segment.getStatisticName() );
		segmentLabel.textProperty().bind( when( isExpandedProperty ).then( fullName ).otherwise( shortName ) );

		loadStyles();
		getChildren().addAll( slider );

		scaling.addListener( new InvalidationListener()
		{
			@Override
			public void invalidated( Observable arg0 )
			{
				if( scaling.get() )
				{
					menuButton.setDisable( true );
					getChildren().addAll( slider );
					getStyleClass().addAll( scalingStyleClass );
					slider.visibleProperty().set( true );
					if( sliderNob != null )
					{
						loadNob( slider );
					}
				}
				else
				{
					menuButton.setDisable( false );
					getChildren().removeAll( slider );
					getStyleClass().removeAll( scalingStyleClass );
				}
			}
		} );

		scaling.set( false );

		slider.valueProperty().addListener( new InvalidationListener()
		{
			@Override
			public void invalidated( Observable arg0 )
			{
				int snappedZoom = ( int )Math.round( slider.valueProperty().doubleValue() );

				if( scale != snappedZoom )
				{
					scale = snappedZoom;
					segment.setAttribute( SCALE_ATTRIBUTE, String.valueOf( scale ) );
					parent.updateChart();
				}
			}
		} );

		slider.getChildrenUnmodifiable().addListener( new ListChangeListener<Node>()
		{
			@Override
			public void onChanged( javafx.collections.ListChangeListener.Change<? extends Node> change )
			{
				while( change.next() )
				{
					for( Node node : change.getAddedSubList() )
					{
						if( node instanceof StyleableGraphicSlider )
						{
							loadNob( node );
						}
					}
				}
			}
		} );
	}

	private void loadNob( final Node node )
	{
		Platform.runLater( new Runnable()
		{
			public void run()
			{
				Node nob = node.lookup( ".graphic" );
				if( nob instanceof Region && nob != null )
				{
					sliderNob = ( Region )nob;
					sliderNob.setStyle( "-fx-background-color: " + color + ";" );
				}
				else
				{
					log.warn( "Cannot find sliderNob for slider" );
				}
			}
		} );
	}

	private void loadStyles()
	{
		int newScale = 0;
		try
		{
			newScale = Integer.parseInt( segment.getAttribute( SCALE_ATTRIBUTE, "0" ) );
		}
		catch( NumberFormatException e )
		{
		}
		scale = newScale;
		slider.setValue( scale );
	}

	public void enableScaling( boolean value )
	{
		scaling.set( value );
	}

	public BooleanProperty scalingProperty()
	{
		return scaling;
	}

	public void setContainer( SegmentBox segmentBox )
	{
		parent = segmentBox;
	}

	@FXML
	public void enableScaling()
	{
		if( parent != null )
		{
			parent.enableScaling();
		}
		else
		{
			log.error( "LineSegmentView with label:(" + segmentLabel.getText()
					+ ") dont have the reference to its SegmentBox" );
		}
	}
}

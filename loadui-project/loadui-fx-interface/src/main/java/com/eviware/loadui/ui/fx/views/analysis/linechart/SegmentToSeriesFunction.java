package com.eviware.loadui.ui.fx.views.analysis.linechart;

import static com.eviware.loadui.ui.fx.util.ObservableLists.fromExpression;

import java.util.LinkedList;
import java.util.concurrent.Callable;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.shape.CircleBuilder;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineBuilder;
import javafx.scene.shape.Path;
import javafx.scene.shape.StrokeType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.statistics.DataPoint;
import com.eviware.loadui.api.statistics.model.chart.line.LineSegment;
import com.eviware.loadui.api.statistics.model.chart.line.Segment;
import com.eviware.loadui.api.statistics.model.chart.line.TestEventSegment;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.testevents.TestEvent;
import com.eviware.loadui.ui.fx.api.analysis.ExecutionChart;
import com.eviware.loadui.ui.fx.util.Observables;
import com.google.common.base.Function;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterables;

public final class SegmentToSeriesFunction implements Function<Segment, XYChart.Series<Number, Number>>
{
	ObservableValue<Execution> execution;
	ObservableList<Observable> observables;
	ExecutionChart chart;
	LoadingCache<XYChart.Series<?, ?>, StringProperty> eventSeriesStyles;

	protected static final Logger log = LoggerFactory.getLogger( SegmentToSeriesFunction.class );

	public SegmentToSeriesFunction( ObservableValue<Execution> execution, ObservableList<Observable> observables,
			ExecutionChart chart )
	{
		this.execution = execution;
		this.observables = observables;
		this.chart = chart;
	}

	@Override
	public XYChart.Series<Number, Number> apply( final Segment segment )
	{
		System.out.println( "Segment: " + segment );

		if( segment instanceof LineSegment )
			return lineSegmentToSeries( ( LineSegment )segment );
		else if( segment instanceof LineSegment )
			return eventSegmentToSeries( ( TestEventSegment )segment );
		else
			throw new RuntimeException( "Unsupported Segment type" );
	}

	private static final Function<DataPoint<?>, XYChart.Data<Number, Number>> datapointToChartdata = new Function<DataPoint<?>, XYChart.Data<Number, Number>>()
	{
		@Override
		public XYChart.Data<Number, Number> apply( DataPoint<?> point )
		{
			return new XYChart.Data<Number, Number>( point.getTimestamp(), point.getValue() );
		}
	};

	private Series<Number, Number> lineSegmentToSeries( final LineSegment segment )
	{
		final XYChart.Series<Number, Number> series = new XYChart.Series<>();
		series.setName( segment.getStatisticName() );

		series.setData( fromExpression( new Callable<Iterable<XYChart.Data<Number, Number>>>()
		{
			@Override
			public Iterable<XYChart.Data<Number, Number>> call() throws Exception
			{
				if( segment.isRemoved() )
				{
					return new LinkedList<>();
				}

				Iterable<XYChart.Data<Number, Number>> chartdata = Iterables.transform(
						segment.getStatistic().getPeriod( ( long )chart.getPosition() - 2000,
								( long )chart.getPosition() + chart.getSpan() + 2000, chart.getTickZoomLevel().getLevel(),
								execution.getValue() ), datapointToChartdata );

				// applies the scale to each point
				final Function<XYChart.Data<Number, Number>, XYChart.Data<Number, Number>> chartdataToScaledChartdata = new Function<XYChart.Data<Number, Number>, XYChart.Data<Number, Number>>()
				{
					@Override
					public XYChart.Data<Number, Number> apply( XYChart.Data<Number, Number> point )
					{
						double scaleValue = Math.pow( 10,
								Integer.parseInt( segment.getAttribute( LineSegmentView.SCALE_ATTRIBUTE, "0" ) ) );

						XYChart.Data<Number, Number> dataPoint = new XYChart.Data<Number, Number>( point.getXValue(), point
								.getYValue().doubleValue() * scaleValue );

						dataPoint.setNode( CircleBuilder.create().fill( chart.getColor( segment, execution.getValue() ) )
								.radius( 3 ).build() );

						return dataPoint;
					}
				};

				return Iterables.transform( chartdata, chartdataToScaledChartdata );

			}
		}, observables ) );

		// TODO: Make path color just update when new chart is added
		Observables.group( observables ).addListener( new InvalidationListener()
		{

			@Override
			public void invalidated( Observable arg0 )
			{
				if( series.getNode() instanceof Path )
				{
					( ( Path )series.getNode() ).setStroke( chart.getColor( segment, execution.getValue() ) );
				}

			}
		} );

		return series;
	}

	private XYChart.Series<Number, Number> eventSegmentToSeries( final TestEventSegment segment )
	{
		final XYChart.Series<Number, Number> series = new XYChart.Series<>();
		series.setName( segment.getTypeLabel() );

		series.setData( fromExpression( new Callable<Iterable<XYChart.Data<Number, Number>>>()
		{
			@Override
			public Iterable<XYChart.Data<Number, Number>> call() throws Exception
			{
				if( segment.isRemoved() || execution.getValue() == null )
				{
					return new LinkedList<>();
				}

				return Iterables.transform(
						segment.getTestEventsInRange( execution.getValue(), ( long )chart.getPosition() - 2000,
								( long )chart.getPosition() + chart.getSpan() + 2000, chart.getTickZoomLevel().getLevel() ),
						new Function<TestEvent, XYChart.Data<Number, Number>>()
						{
							@Override
							public XYChart.Data<Number, Number> apply( TestEvent event )
							{
								XYChart.Data<Number, Number> data = new XYChart.Data<Number, Number>( event.getTimestamp(),
										10.0 );

								Line eventLine = LineBuilder.create().endY( 600 ).managed( false )
										.strokeType( StrokeType.OUTSIDE ).build();

								eventLine.setStroke( chart.getColor( segment, execution.getValue() ) );

								data.setNode( eventLine );

								return data;
							}
						} );
			}
		}, observables ) );

		series.nodeProperty().addListener( new ChangeListener<Node>()
		{
			@Override
			public void changed( ObservableValue<? extends Node> arg0, Node arg1, Node newNode )
			{
				newNode.setVisible( false );
			}
		} );
		return series;
	}
}
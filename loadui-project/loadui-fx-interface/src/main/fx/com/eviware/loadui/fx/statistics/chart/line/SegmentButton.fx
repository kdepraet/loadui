/* 
 * Copyright 2011 eviware software ab
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
package com.eviware.loadui.fx.statistics.chart.line;

import javafx.scene.Node;
import javafx.scene.layout.Resizable;
import javafx.scene.layout.LayoutInfo;
import javafx.scene.layout.Priority;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.shape.Polyline;
import javafx.geometry.VPos;

import com.eviware.loadui.fx.FxUtils;
import com.eviware.loadui.fx.ui.node.BaseNode;
import com.eviware.loadui.fx.ui.node.Deletable;
import com.eviware.loadui.fx.util.ModelUtils;

import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.model.chart.LineChartView;
import com.eviware.loadui.api.charting.ChartNamePrettifier;
import com.eviware.loadui.api.charting.line.LineSegmentModel;
import com.eviware.loadui.api.events.WeakEventHandler;

import java.awt.Color;
import java.beans.PropertyChangeEvent;

public class SegmentButton extends BaseNode, Resizable {
	override var styleClass = "segment-button";
	override var width on replace { button.width = width }
	override var height on replace { button.height = height }
	
	def listener = new SegmentListener();
	
	public var compactSegments = true;
	public-init var chartView:LineChartView on replace {
		chartView.getChartGroup().addEventListener( PropertyChangeEvent.class, listener );
	}
	
	def button = Button {
		layoutInfo: LayoutInfo { hfill: true, hgrow: Priority.ALWAYS };
		blocksMouse: false
		action: function():Void {
			//TODO: Implement segment panel.
		}
	}
	
	var lineColor:Color on replace {
		button.style = "-fx-inner-border: {FxUtils.colorToWebString(lineColor)};";
	}
	
	public-init var model:LineSegmentModel on replace {
		def statistic = model.getLineSegment().getStatistic();
		def metricName:String = statistic.getName();
		def dataName:String = statistic.getStatisticVariable().getLabel();
		def singleColumnInCompactMode:Boolean = ChartNamePrettifier.compactNameIsAlone( dataName );
				
		lineColor = model.getColor();
		button.graphic = HBox {
			nodeVPos: VPos.CENTER
			spacing: 3
			content: [
				Label {
					text: ChartNamePrettifier.nameForSource( statistic.getSource() )
					layoutInfo: LayoutInfo { width: 40 }
					visible: bind not compactSegments
					managed: bind not compactSegments
				}, Polyline {
					points: [ 0, 0, 3, 3, 0, 6 ]
					stroke: javafx.scene.paint.Color.rgb( 0x78, 0x78, 0x78 )
					visible: bind not compactSegments
					managed: bind not compactSegments
				}, Label {
					text: bind ModelUtils.getLabelHolder( statistic.getStatisticVariable().getStatisticHolder() ).label
					layoutInfo: LayoutInfo { width: 75 }
					visible: bind not compactSegments
					managed: bind not compactSegments
				}, Polyline {
					points: [ 0, 0, 3, 3, 0, 6 ]
					stroke: javafx.scene.paint.Color.rgb( 0x78, 0x78, 0x78 )
					visible: bind not compactSegments
					managed: bind not compactSegments
				}, Label {
					text: bind if(compactSegments) ChartNamePrettifier.compactDataName( dataName, metricName ) else ChartNamePrettifier.nameForStatistic( dataName )
					layoutInfo: LayoutInfo { width: bind if(not compactSegments) 75 else if (singleColumnInCompactMode) 66 else 37 }
				}, Polyline {
					points: [ 0, 0, 3, 3, 0, 6 ]
					stroke: javafx.scene.paint.Color.rgb( 0x78, 0x78, 0x78 )
					visible: bind not (compactSegments and singleColumnInCompactMode)
					managed: bind not (compactSegments and singleColumnInCompactMode)
				}, Label {
					text: bind if(compactSegments) ChartNamePrettifier.compactMetricName( metricName ) else ChartNamePrettifier.nameForStatistic( metricName )
					layoutInfo: LayoutInfo { width: bind if(compactSegments) 18 else 55 }
					visible: bind not (compactSegments and singleColumnInCompactMode)
					managed: bind not (compactSegments and singleColumnInCompactMode)
				}
			]
		}
	}
	
	override function create():Node {
		button;
	}
	
	override function getPrefHeight( width:Number ):Number {
		button.getPrefHeight( width )
	}
	
	override function getPrefWidth( height:Number ):Number {
		button.getPrefWidth( height )
	}
	
	override function doLayout():Void {
	}
}

class SegmentListener extends WeakEventHandler {
	override function handleEvent( e ):Void {
		def event = e as PropertyChangeEvent;
		if( model.getLineSegment() == event.getSource() ) {
			if( LineSegmentModel.COLOR.equals( event.getPropertyName() ) ) {
				FxUtils.runInFxThread( function():Void { lineColor = event.getNewValue() as Color; } );
			}
		}
	}
}
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
package com.eviware.loadui.impl.statistics;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import com.eviware.loadui.api.statistics.EntryAggregator;
import com.eviware.loadui.api.statistics.StatisticHolder;
import com.eviware.loadui.api.statistics.StatisticVariable;
import com.eviware.loadui.api.statistics.StatisticsManager;
import com.eviware.loadui.api.statistics.store.Entry;
import com.eviware.loadui.api.statistics.store.ExecutionManager;
import com.eviware.loadui.impl.statistics.ThroughputStatisticsWriter.Factory;
import com.eviware.loadui.util.statistics.store.EntryImpl;
import com.eviware.loadui.util.test.BeanInjectorMocker;
import com.google.common.collect.ImmutableMap;

public class ThroughputStatisticsWriterTest
{
	private ThroughputStatisticsWriter writer;

	@Before
	public void setup()
	{

		Factory factory = new ThroughputStatisticsWriter.Factory();

		StatisticVariable variableMock = mock( StatisticVariable.class );
		StatisticHolder statisticHolderMock = mock( StatisticHolder.class );
		when( variableMock.getStatisticHolder() ).thenReturn( statisticHolderMock );

		StatisticsManager statisticsManagerMock = mock( StatisticsManager.class );
		ExecutionManager executionManagerMock = mock( ExecutionManager.class );
		when( statisticsManagerMock.getExecutionManager() ).thenReturn( executionManagerMock );
		when( statisticsManagerMock.getMinimumWriteDelay() ).thenReturn( 1000L );

		BeanInjectorMocker.newInstance();

		writer = factory.createStatisticsWriter( statisticsManagerMock, variableMock,
				Collections.<String, Object> emptyMap() );
	}

	@Test
	public void shouldCalculateTPS()
	{
		Random random = new Random();
		writer.update( 7, random.nextInt( 1000 ) );
		writer.update( 15, random.nextInt( 1000 ) );
		writer.update( 138, random.nextInt( 1000 ) );
		writer.update( 467, random.nextInt( 1000 ) );
		writer.update( 842, random.nextInt( 1000 ) );

		Entry entry = writer.output();

		assertNotNull( entry );
		Number tps = entry.getValue( ThroughputStatisticsWriter.Stats.TPS.name() );
		assertThat( tps, instanceOf( Double.class ) );
		assertThat( ( Double )tps, is( 5.0 ) );
	}

	@Test
	public void shouldCalculateBPS()
	{
		long timestamp = System.currentTimeMillis();
		writer.update( timestamp + 7, 123 );
		writer.update( timestamp + 15, 432 );
		writer.update( timestamp + 138, 143 );
		writer.update( timestamp + 467, 214 );
		writer.update( timestamp + 842, 165 );

		Entry entry = writer.output();

		assertNotNull( entry );
		Number bps = entry.getValue( ThroughputStatisticsWriter.Stats.BPS.name() );
		assertThat( bps, instanceOf( Double.class ) );
		assertThat( ( Double )bps, is( 1077.0 ) );

		writer.update( timestamp + 1010, 143 );
		writer.update( timestamp + 1324, 214 );
		writer.update( timestamp + 1702, 165 );

		entry = writer.output();

		assertNotNull( entry );
		bps = entry.getValue( ThroughputStatisticsWriter.Stats.BPS.name() );
		assertThat( bps, instanceOf( Double.class ) );
		assertThat( ( Double )bps, is( 522.0 ) );
	}

	@Test
	public void shouldAggregateValue()
	{
		Entry e1 = new EntryImpl( 123, ImmutableMap.<String, Number> of( ThroughputStatisticsWriter.Stats.BPS.name(),
				321, ThroughputStatisticsWriter.Stats.TPS.name(), 4 ) );

		Entry e2 = new EntryImpl( 234, ImmutableMap.<String, Number> of( ThroughputStatisticsWriter.Stats.BPS.name(),
				562, ThroughputStatisticsWriter.Stats.TPS.name(), 7 ) );

		Entry e3 = new EntryImpl( 345, ImmutableMap.<String, Number> of( ThroughputStatisticsWriter.Stats.BPS.name(), 93,
				ThroughputStatisticsWriter.Stats.TPS.name(), 11 ) );

		EntryAggregator aggregator = writer.getTrackDescriptor().getEntryAggregator();

		Entry entry = aggregator.aggregate( Collections.<Entry> emptySet(), false );
		assertNull( entry );

		HashSet<Entry> entries = new HashSet<>();
		entries.add( e1 );
		entry = aggregator.aggregate( entries, false );

		assertThat( entry, is( e1 ) );

		entries.clear();
		entries.addAll( Arrays.asList( e1, e2, e3 ) );
		entry = aggregator.aggregate( entries, true );

		assertNotNull( entry );

		Number bps = entry.getValue( ThroughputStatisticsWriter.Stats.BPS.name() );
		Number tps = entry.getValue( ThroughputStatisticsWriter.Stats.TPS.name() );

		assertThat( ( Double )bps, is( 976.0 ) );
		assertThat( ( Double )tps, is( 22.0 ) );

		entry = aggregator.aggregate( entries, false );

		assertNotNull( entry );

		bps = entry.getValue( ThroughputStatisticsWriter.Stats.BPS.name() );
		tps = entry.getValue( ThroughputStatisticsWriter.Stats.TPS.name() );

		assertEquals( ( Double )bps, 325.33, 0.01 );
		assertEquals( ( Double )tps, 7.33, 0.01 );
	}
}

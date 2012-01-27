/* 
 * Copyright 2011 SmartBear Software
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
package com.eviware.loadui.impl.statistics.store;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.loadui.api.addressable.AddressableRegistry;
import com.eviware.loadui.api.messaging.BroadcastMessageEndpoint;
import com.eviware.loadui.api.messaging.MessageEndpoint;
import com.eviware.loadui.api.messaging.MessageListener;
import com.eviware.loadui.api.testevents.TestEvent;
import com.eviware.loadui.api.testevents.TestEvent.Factory;
import com.eviware.loadui.api.testevents.TestEventManager;
import com.eviware.loadui.api.testevents.TestEventRegistry;
import com.eviware.loadui.api.traits.Releasable;
import com.eviware.loadui.impl.statistics.store.testevents.TestEventEntryImpl;
import com.eviware.loadui.util.testevents.AbstractTestEventManager;
import com.eviware.loadui.util.testevents.UnknownTestEvent;

public class TestEventManagerImpl extends AbstractTestEventManager implements Releasable
{
	private static final String CHANNEL = "/" + TestEventManager.class.getSimpleName();

	public static final Logger log = LoggerFactory.getLogger( TestEventManagerImpl.class );

	private final ExecutionManagerImpl manager;
	private final MessageEndpoint endpoint;
	private final AddressableRegistry addressableRegistry;
	private final EventReceiver eventReceiver = new EventReceiver();

	public TestEventManagerImpl( TestEventRegistry testEventRegistry, ExecutionManagerImpl manager,
			BroadcastMessageEndpoint endpoint, AddressableRegistry addressableRegistry )
	{
		super( testEventRegistry );
		this.manager = manager;
		this.endpoint = endpoint;
		this.addressableRegistry = addressableRegistry;

		endpoint.addMessageListener( CHANNEL, eventReceiver );
	}

	@Override
	public <T extends TestEvent> void logTestEvent( TestEvent.Source<T> source, T testEvent )
	{
		@SuppressWarnings( "unchecked" )
		TestEvent.Factory<T> factory = testEventRegistry.lookupFactory( ( Class<T> )testEvent.getType() );

		if( factory != null )
		{
			manager.writeTestEvent( factory.getLabel(), source, testEvent.getTimestamp(),
					factory.getDataForTestEvent( testEvent ), 0 );

			//TODO: Aggregate into interpolation levels.

			TestEvent.Entry entry = new TestEventEntryImpl( testEvent, source.getLabel(), factory.getLabel() );
			for( TestEventObserver observer : observers )
			{
				observer.onTestEvent( entry );
			}
		}
		else
		{
			log.warn( "No TestEvent.Factory capable of storing TestEvent: {}, of type: {} has been registered!",
					testEvent, testEvent.getType() );
		}
	}

	@Override
	public void release()
	{
		endpoint.removeMessageListener( eventReceiver );
	}

	private class EventReceiver implements MessageListener
	{
		@Override
		public void handleMessage( String channel, MessageEndpoint endpoint, Object data )
		{
			@SuppressWarnings( "unchecked" )
			List<Object> args = ( List<Object> )data;

			String label = ( String )args.get( 0 );
			TestEvent.Source<?> source = ( TestEvent.Source<?> )addressableRegistry.lookup( ( String )args.get( 1 ) );
			if( source == null )
			{
				log.debug( "No object found with ID: {}", args.get( 1 ) );
				return;
			}
			long timestamp = ( Long )args.get( 2 );
			byte[] eventData = ( byte[] )args.get( 3 );

			manager.writeTestEvent( label, source, timestamp, eventData, 0 );

			Factory<?> factory = testEventRegistry.lookupFactory( label );
			TestEventEntryImpl entry = null;
			if( factory == null )
			{
				entry = new TestEventEntryImpl( new UnknownTestEvent( timestamp ), source.getLabel(), "Unknown" );
			}
			else
			{
				entry = new TestEventEntryImpl( factory.createTestEvent( timestamp, source.getData(), eventData ),
						source.getLabel(), factory.getLabel() );
			}
			for( TestEventObserver observer : observers )
			{
				observer.onTestEvent( entry );
			}
		}
	}
}

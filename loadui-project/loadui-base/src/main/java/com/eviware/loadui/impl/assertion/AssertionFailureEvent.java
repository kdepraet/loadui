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
package com.eviware.loadui.impl.assertion;

import java.io.IOException;

import com.eviware.loadui.api.addressable.AddressableRegistry;
import com.eviware.loadui.api.annotations.Strong;
import com.eviware.loadui.api.assertion.AssertionItem;
import com.eviware.loadui.util.BeanInjector;
import com.eviware.loadui.util.serialization.SerializationUtils;
import com.eviware.loadui.util.testevents.AbstractTestEvent;

/**
 * TestEvent for a failed assertion.
 * 
 * @author dain.nilsson
 */
public class AssertionFailureEvent extends AbstractTestEvent
{
	public static final String TYPE = "AssertionFailure";

	private final AssertionItem<?> assertionItem;

	private final String valueLabel;
	private final String constraintString;
	private final String valueString;

	public AssertionFailureEvent( long timestamp, AssertionItem<?> assertionItem, Object value )
	{
		this( timestamp, assertionItem, assertionItem.getLabel(), String.valueOf( assertionItem.getConstraint() ), String
				.valueOf( value ) );
	}

	protected AssertionFailureEvent( long timestamp, AssertionItem<?> assertionItem, String valueLabel,
			String constraintString, String valueString )
	{
		super( AssertionFailureEvent.class, timestamp );

		this.assertionItem = assertionItem;
		this.valueLabel = valueLabel;
		this.constraintString = constraintString;
		this.valueString = valueString;
	}

	public AssertionItem<?> getAssertion()
	{
		return assertionItem;
	}

	@Override
	public String toString()
	{
		return String.format( "%s: %s did not meet constraint: %s", valueLabel, valueString, constraintString );
	}

	@Strong
	public static class Group extends AssertionFailureEvent
	{
		private final int occurances;

		public Group( long timestamp, AssertionItem<?> assertionItem, int occurances )
		{
			this( timestamp, assertionItem, assertionItem.getLabel(), String.valueOf( assertionItem.getConstraint() ),
					occurances );
		}

		protected Group( long timestamp, AssertionItem<?> assertionItem, String valueLabel, String constraintString,
				int occurances )
		{
			super( timestamp, assertionItem, valueLabel, constraintString, String.valueOf( "<" + occurances + " values>" ) );

			this.occurances = occurances;
		}

		public int getOccurances()
		{
			return occurances;
		}
	}

	public static final class Factory extends AbstractTestEvent.Factory<AssertionFailureEvent>
	{
		public Factory()
		{
			super( AssertionFailureEvent.class, TYPE );
		}

		@Override
		public AssertionFailureEvent createTestEvent( long timestamp, byte[] sourceData, byte[] entryData )
		{
			AssertionItem<?> assertionItem = null;
			String assertionLabel = null;
			String constraintString = null;
			Object value = null;

			try
			{
				String[] parts = ( String[] )SerializationUtils.deserialize( sourceData );
				assertionItem = ( AssertionItem<?> )BeanInjector.getBean( AddressableRegistry.class ).lookup( parts[0] );
				assertionLabel = parts[1];
				constraintString = parts[2];

				value = SerializationUtils.deserialize( entryData );
			}
			catch( ClassNotFoundException e )
			{
				e.printStackTrace();
			}
			catch( IOException e )
			{
				e.printStackTrace();
			}

			if( value instanceof Integer )
			{
				return new Group( timestamp, assertionItem, assertionLabel, constraintString, ( Integer )value );
			}
			return new AssertionFailureEvent( timestamp, assertionItem, assertionLabel, constraintString, ( String )value );
		}

		@Override
		public byte[] getDataForTestEvent( AssertionFailureEvent testEvent )
		{
			try
			{
				if( testEvent instanceof Group )
				{
					return SerializationUtils.serialize( ( ( Group )testEvent ).occurances );
				}
				return SerializationUtils.serialize( testEvent.valueString );
			}
			catch( IOException e )
			{
				return null;
			}
		}
	}
}

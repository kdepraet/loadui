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
package com.eviware.loadui.util.execution;

import java.util.concurrent.Future;

import com.eviware.loadui.api.lifecycle.ExecutionResult;
import com.eviware.loadui.api.lifecycle.TestExecution;
import com.eviware.loadui.api.lifecycle.TestState;
import com.eviware.loadui.api.model.CanvasItem;

/**
 * Implements most of TestExecution, leaving it up to subclasses to handle
 * complete() as well as changing the state.
 * 
 * @author dain.nilsson
 */
public abstract class AbstractTestExecution implements TestExecution
{
	private final CanvasItem canvas;

	protected TestState state = TestState.ENQUEUED;
	private boolean aborted = false;

	public AbstractTestExecution( CanvasItem canvas )
	{
		this.canvas = canvas;
	}

	@Override
	public TestState getState()
	{
		return state;
	}

	@Override
	public CanvasItem getCanvas()
	{
		return canvas;
	}

	@Override
	public Future<ExecutionResult> abort()
	{
		aborted = true;
		return complete();
	}

	@Override
	public boolean isAborted()
	{
		return aborted;
	}
}
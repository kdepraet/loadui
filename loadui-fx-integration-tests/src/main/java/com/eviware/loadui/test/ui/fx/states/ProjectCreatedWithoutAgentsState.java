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
package com.eviware.loadui.test.ui.fx.states;

import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.ui.fx.GUI;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.Is.is;
import static org.loadui.testfx.GuiTest.find;
import static org.loadui.testfx.GuiTest.waitUntil;
import static org.loadui.testfx.Matchers.visible;

public class ProjectCreatedWithoutAgentsState extends TestState
{

	public static final ProjectCreatedWithoutAgentsState STATE = new ProjectCreatedWithoutAgentsState();

	private ProjectCreatedWithoutAgentsState()
	{
		this( "Project without agents created" );
	}

	protected ProjectCreatedWithoutAgentsState( String name )
	{
		super( name );
	}

	@Override
	protected TestState parentState()
	{
		return OpenSourceFxLoadedState.STATE;
	}

	@Override
	protected void enterFromParent() throws Exception
	{
		final Node projectCarousel = find( "#projectRefCarousel" );
		System.out.println( "ProjectCreateWithoutAgentsState - Creating new project." );
		waitUntil( "#newProjectIcon", is( visible() ) );

		GUI.getOpenSourceGui().getController().drag( "#newProjectIcon" ).to( projectCarousel ).type( "Project 1" )
				.type( KeyCode.TAB ).type( "project-1.xml" ).click( ".check-box" ).click( "#default" );

		waitUntil( ".project-ref-view", is( visible() ) );
	}

	@Override
	protected void exitToParent() throws Exception
	{
		log.debug( "Deleting project." );
		try
		{
			GUI.getOpenSourceGui().getController().click( "#projectRefCarousel .project-ref-view .menu-button" );
			GUI.getOpenSourceGui().getController().click( "#delete-item" );
			GUI.getOpenSourceGui().getController().click( ".confirmation-dialog #default" );
			waitUntil( ".project-ref-view", is( not( visible() ) ) );
		}
		catch( Exception e )
		{
			log.warn( "Problem exiting state {}: {}", STATE, e );
			e.printStackTrace();
		}

	}

}

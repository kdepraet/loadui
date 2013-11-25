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

import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.test.ui.fx.FxIntegrationBase;
import com.eviware.loadui.test.ui.fx.FxIntegrationTestBase;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Integration tests for testing the loadUI controller through its API.
 *
 * @author dain.nilsson
 */
@Category( IntegrationTest.class )
public class ProjectCreatedWithoutAgentsStateTest extends FxIntegrationTestBase
{

	@Override
	public TestState getStartingState()
	{
		return ProjectCreatedWithoutAgentsState.STATE;
	}

	@Test
	public void shouldHaveProject()
	{
		WorkspaceItem workspace = FxIntegrationBase.getWorkspaceItem();
		assertThat( workspace.getProjectRefs().size(), is( 1 ) );
	}

}

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
package com.eviware.loadui.test.ui.fx;

import com.eviware.loadui.api.model.ProjectItem;
import com.eviware.loadui.test.categories.IntegrationTest;
import com.google.code.tempusfugit.concurrency.IntermittentTestRunner;
import com.google.code.tempusfugit.concurrency.annotations.Intermittent;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static com.eviware.loadui.test.ui.fx.FxIntegrationBase.RunBlocking.NON_BLOCKING;
import static com.eviware.loadui.ui.fx.util.test.LoadUiRobot.Component.FIXED_RATE_GENERATOR;
import static com.google.code.tempusfugit.temporal.Duration.seconds;
import static com.google.code.tempusfugit.temporal.Timeout.timeout;
import static com.google.code.tempusfugit.temporal.WaitFor.waitOrTimeout;
import static java.util.concurrent.TimeUnit.SECONDS;

@Category(IntegrationTest.class)
@RunWith( IntermittentTestRunner.class )
@Intermittent( repetition = 5 )
public class ProjectPlaybackTest extends SimpleWebTestBase
{

	@Test
	public void shouldPlayAndStop() throws Exception
	{
		final ProjectItem project = getProjectItem();

		runTestFor( 3, SECONDS, NON_BLOCKING );

		waitOrTimeout( new IsCanvasRunning( project, true ), timeout( seconds( 5 ) ) );
		waitOrTimeout( new IsCanvasRunning( project, false ), timeout( seconds( 5 ) ) );
	}

	@Test
	public void shouldStopOnLimit() throws Exception
	{
		ProjectItem project = getProjectItem();

		long veryHighLoad = 5_000;
		turnKnobIn( FIXED_RATE_GENERATOR ).to( veryHighLoad );

		setMaxConcurrentRequestsTo( 1_000 );

		setTestTimeLimitTo( 4 );

		robot.clickPlayStopButton();

		sleep( 4_000 );
		waitOrTimeout( new IsCanvasRunning( project, false ), timeout( seconds( 10 ) ) );
	}

}

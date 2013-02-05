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
package com.eviware.loadui.fx.async;

import javafx.async.RunnableFuture;
import com.eviware.loadui.api.model.ProjectRef;


//This class is only here because the maven-bundle-plugin fails if there are no Java classes...
public class ProjectRefSetEnabledTask implements RunnableFuture
{
	private ProjectRef ref;
	
	public ProjectRefSetEnabledTask(ProjectRef pref) {
		ref = pref;
	}
	
	public void run() throws Exception {
		ref.setEnabled(true);
	}
}

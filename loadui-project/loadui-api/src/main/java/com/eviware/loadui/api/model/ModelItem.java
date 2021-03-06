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
package com.eviware.loadui.api.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.eviware.loadui.api.addon.AddonHolder;
import com.eviware.loadui.api.traits.Describable;
import com.eviware.loadui.api.traits.Labeled;
import com.eviware.loadui.api.traits.Releasable;

/**
 * The base for all model items in loadUI.
 * 
 * @author dain.nilsson
 */
public interface ModelItem extends BaseItem, AddonHolder, AttributeHolder, PropertyHolder, Labeled.Mutable,
		Describable.Mutable, Releasable
{
	// Properties
	public final String DESCRIPTION_PROPERTY = ModelItem.class.getSimpleName() + ".description";

	/**
	 * Gets the URL to a web site providing help for the ComponentBehavior, or
	 * null if no such web site exists.
	 * 
	 * @return The full URL of a help page for the ModelItem.
	 */
	@Nullable
	public String getHelpUrl();

	/**
	 * Triggers a named action which can be listened for. Actions propagate down
	 * to child ModelItems according to the following:
	 * 
	 * WorkspaceItem > ProjectItem > SceneItem > ComponentItem.
	 * 
	 * @param actionName
	 *           The name of the action to trigger.
	 */
	public void triggerAction( @Nonnull String actionName );

	/**
	 * @return the type of this model item. This is used to identify model items without having to resort to checking
	 * the class of the model items.
	 */
	public ModelItemType getModelItemType();

}

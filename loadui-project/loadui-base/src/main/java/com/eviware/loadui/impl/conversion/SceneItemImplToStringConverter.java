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
package com.eviware.loadui.impl.conversion;

import com.eviware.loadui.config.LoaduiSceneDocumentConfig;
import com.eviware.loadui.impl.model.canvas.SceneItemImpl;
import com.eviware.loadui.util.StringUtils;
import org.springframework.core.convert.converter.Converter;

public class SceneItemImplToStringConverter implements Converter<SceneItemImpl, String>
{
	@Override
	public String convert( SceneItemImpl source )
	{
		LoaduiSceneDocumentConfig doc = LoaduiSceneDocumentConfig.Factory.newInstance();
		doc.addNewLoaduiScene().set( source.getConfig() );

		return StringUtils.serialize( doc.xmlText(), source.getProject().getId() );
	}
}

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
package com.eviware.loadui.fx.ui.form.fields;

import javafx.scene.control.TextBox;
import java.lang.IllegalArgumentException;
import java.lang.NumberFormatException;
import com.eviware.loadui.fx.ui.form.FormField;


/**
 * Constructs a new DoubleInputField using the supplied arguments.
 */
public function build( id:String, label:String, value:Object ) {
	DoubleInputField { id:id, label:label, value:value }
}

/**
 * @author robert
 */

public class DoubleInputField extends TextBox, FormField {
	override var value on replace {
		if( value != null and not ( value instanceof Double ) )
			throw new IllegalArgumentException( "Value must be of type Long!" );

		text = String.valueOf(value);
	}
    
	override var text on replace {
		try {
			value = Double.valueOf(text);
		} catch( e:NumberFormatException ) {
			value = null;
		}
	}
}

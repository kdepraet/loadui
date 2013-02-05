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
/*
*FormItem.fx
*
*Created on feb 22, 2010, 14:30:34 em
*/

package com.eviware.loadui.fx.ui.form;


/**
 * Content item which is placed in a form such as a FieldGroup or FormField.
 *
 * @author dain.nilsson
 */
public mixin class FormItem {

	/**
	 * Any contained FormFields which should be added to the Form.
	 */
	public-read protected var fields:FormField[];
}

package com.eviware.loadui.api.model;

import java.util.Collection;

public interface AttributeHolder
{
	/**
	 * Sets a string attribute for the AttributeHolder, which will be saved when
	 * the AttributeHolder is saved, and loaded when the AttributeHolder is
	 * loaded.
	 * 
	 * @param key
	 *           The name of the attribute to set.
	 * @param value
	 *           The value to set.
	 */
	public void setAttribute( String key, String value );

	/**
	 * Gets a String attribute previously stored for the AttributeHolder.
	 * 
	 * @param key
	 *           The name of the attribute to get.
	 * @param defaultValue
	 *           A default String to return if the attribute does not exist.
	 * @return The value of the attribute, or the default value if the attribute
	 *         does not exist.
	 */
	public String getAttribute( String key, String defaultValue );

	/**
	 * Removes a String attribute previously stored for the AttributeHolder, if
	 * it exists.
	 * 
	 * @param key
	 *           The name of the attribute to remove.
	 */
	public void removeAttribute( String key );

	/**
	 * Gets a list of all attributes stored for the AttributeHolder.
	 * 
	 * @return a Collection<String> of all the attribute keys.
	 */
	public Collection<String> getAttributes();
}

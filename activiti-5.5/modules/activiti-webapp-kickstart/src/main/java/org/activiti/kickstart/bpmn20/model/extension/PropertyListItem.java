/**
 *	The package contains special extensions of properties
 */
package org.activiti.kickstart.bpmn20.model.extension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlValue;

/**
 * JAXB does not support dynamic naming of XML elements. The property list is
 * implemented like a hash map. The class name represents the name of the 
 * element and content is the value of the xml element.
 * 
 * @author Sven Wagner-Boysen
 *
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({
	DummyItem.class
})
public abstract class PropertyListItem {
	
	private static List<Class<? extends PropertyListItem>> classes = new ArrayList<Class<? extends PropertyListItem>>();
	
	private static List<Class<? extends PropertyListItem>> getItemClasses() {
		classes.add(DummyItem.class);
		
		return classes;
	}
	
	/** Default constructor */
	public PropertyListItem() {}
	
	/**
	 * Constructor to set the value of the element directly.
	 * 
	 * @param propertyIdentifier
	 * 		Identifier for the appropriate class
	 * @param value
	 * 		The value of the property
	 */
	public static PropertyListItem addItem(String propertyName, String value) {
		
		/* Find property class */
		Class<? extends PropertyListItem> propertyItemClass = null;
		for(Class<? extends PropertyListItem> propItemClass : getItemClasses()) {
			if(propItemClass.getSuperclass() == null 
					|| !propItemClass.getSuperclass().equals(PropertyListItem.class))
				continue;
			
			PropertyId propId = propItemClass.getAnnotation(PropertyId.class);
			if(propId == null)
				continue;
			
			if(propId.value() != null && propId.value().equals(propertyName)) {
				propertyItemClass = (Class<? extends PropertyListItem>) propItemClass;
			}
		}
		
		/* Create instance of property item */
		if(propertyItemClass == null)
			return null;
		
		try {
			
			PropertyListItem propItem = propertyItemClass.newInstance();
			propItem.setContent(value);
			return propItem;
			
		} catch (Exception e) {
			return null;
		} 
	}
	
	@XmlValue
	protected String content;

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	@Target({
		ElementType.TYPE
	})
	@Retention(RetentionPolicy.RUNTIME)
	public @interface PropertyId {
		String value();
	}
}


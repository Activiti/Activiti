/**
 * 
 */
package org.activiti.kickstart.bpmn20.model.extension.signavio;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.activiti.kickstart.bpmn20.model.extension.AbstractExtensionElement;

/**
 * @author Sven Wagner-Boysen
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SignavioType extends AbstractExtensionElement {
	
	@XmlAttribute
	private SignavioDataObjectType dataObjectType;

	public SignavioType() {
		super();
	}
	
	public SignavioType(SignavioDataObjectType type) {
		super();
		setDataObjectType(type);
	}
	
	/* Getter & Setter */
	
	public void setDataObjectType(SignavioDataObjectType dataObjectType) {
		this.dataObjectType = dataObjectType;
	}

	public SignavioDataObjectType getDataObjectType() {
		return dataObjectType;
	}
}

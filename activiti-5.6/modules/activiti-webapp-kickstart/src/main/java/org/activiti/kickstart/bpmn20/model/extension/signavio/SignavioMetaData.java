package org.activiti.kickstart.bpmn20.model.extension.signavio;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.activiti.kickstart.bpmn20.model.extension.AbstractExtensionElement;

/**
 * Specifies a customer defined attribute and its value.
 * 
 * @author Sven Wagner-Boysen
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SignavioMetaData extends AbstractExtensionElement{
	
	@XmlAttribute
	private String metaKey;
	@XmlAttribute
	private String metaValue;
	
	public SignavioMetaData() {
		super();
	}
	
	public SignavioMetaData(String metaKey, String metaValue) {
		super();
		this.metaKey = metaKey;
		this.metaValue = metaValue;
	}
	
	/* Getter & Setter */
	
	public String getMetaKey() {
		return metaKey;
	}
	public void setMetaKey(String metaKey) {
		this.metaKey = metaKey;
	}
	public String getMetaValue() {
		return metaValue;
	}
	public void setMetaValue(String metaValue) {
		this.metaValue = metaValue;
	}
	
}

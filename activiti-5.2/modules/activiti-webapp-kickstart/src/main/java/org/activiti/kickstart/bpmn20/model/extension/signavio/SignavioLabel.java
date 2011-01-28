package org.activiti.kickstart.bpmn20.model.extension.signavio;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

import org.activiti.kickstart.bpmn20.model.extension.AbstractExtensionElement;

/**
 * Stores label's positions information.
 * 
 * @author Sven Wagner-Boysen
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SignavioLabel extends AbstractExtensionElement {
	
	@XmlAnyAttribute
	private Map<QName, String> labelAttributes;
	
	/*
	 * Constructors 
	 */
	public SignavioLabel() {
		super();
	}
	
	public SignavioLabel(Map<String, String> labelInfo) {
		for(String key : labelInfo.keySet()) {
			getLabelAttributes().put(new QName(key), labelInfo.get(key));
		}
	}
	
	/* Getter & Setter */
	
	public Map<QName, String> getLabelAttributes() {
		if(labelAttributes == null) {
			labelAttributes = new HashMap<QName, String>();
		}
		return labelAttributes;
	}
	
}

package org.activiti.kickstart.bpmn20.model.extension.activiti;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.activiti.kickstart.bpmn20.model.extension.AbstractExtensionElement;

@XmlRootElement(name="field", namespace = "http://activiti.org/bpmn")
@XmlType(propOrder = {"name", "stringValue", "expression"})
@XmlAccessorType(XmlAccessType.FIELD)
public class ActivitFieldExtensionElement extends AbstractExtensionElement {
    
    @XmlAttribute(name = "name")
    private String name;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    @XmlElement(name = "string", namespace = "http://activiti.org/bpmn")
    private String stringValue;
    
    public String getStringValue() {
        return stringValue;
    }
    
    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }
    
    @XmlElement(name = "expression", namespace = "http://activiti.org/bpmn")
    private String expression;
    
    public String getExpression() {
        return expression;
    }
    
    public void setExpression(String expression) {
        this.expression = expression;
    }
    
}

/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.activiti.workflow.simple.alfresco.model.config;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

/**
 * @author Joram Barrez
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class FormFieldControl
{
    
    @XmlAttribute(name="template")
    private String template;
    
    @XmlElements ({
        @XmlElement(name="control-param", type=FormFieldControlParameter.class)
    })
    private List<FormFieldControlParameter> controlParameters = new ArrayList<FormFieldControlParameter>();

    public FormFieldControl() {
    	
    }
    
    public FormFieldControl(String template) {
    	this.template = template;
    }
    
    public String getTemplate()
    {
        return template;
    }

    public void setTemplate(String template)
    {
        this.template = template;
    }
    
    public List<FormFieldControlParameter> getControlParameters()
    {
        return controlParameters;
    }

    public void setControlParameters(List<FormFieldControlParameter> controlParameters)
    {
        this.controlParameters = controlParameters;
    }
    
    public void addControlParameter(FormFieldControlParameter controlParameter)
    {
        controlParameters.add(controlParameter);
    }
    
    public FormFieldControlParameter addControlParameter(String name, String configuration)
    {
        FormFieldControlParameter controlParameter = new FormFieldControlParameter();
        controlParameter.setName(name);
        controlParameter.setConfiguration(configuration);
        
        addControlParameter(controlParameter);
        
        return controlParameter;
    }
    
}

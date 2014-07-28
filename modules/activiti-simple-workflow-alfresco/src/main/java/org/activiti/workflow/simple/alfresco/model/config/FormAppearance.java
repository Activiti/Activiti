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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

/**
 * @author Joram Barrez
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class FormAppearance
{

    @XmlElements({
      @XmlElement(name="set", type=FormSet.class),
      @XmlElement(name="field", type=FormField.class)
    })
    private List<FormAppearanceElement> appearanceElements = new ArrayList<FormAppearanceElement>();

    public List<FormAppearanceElement> getAppearanceElements()
    {
        return appearanceElements;
    }

    public void setAppearanceElements(List<FormAppearanceElement> appearanceElements)
    {
        this.appearanceElements = appearanceElements;
    }

    public void addFormAppearanceElement(FormAppearanceElement appearanceElement)
    {
        appearanceElements.add(appearanceElement);
    }
    
    public FormSet addFormSet(String id, String appearance, String labelId, String template)
    {
        FormSet formSet = new FormSet();
        formSet.setId(id);
        
        if (appearance != null)
        {
            formSet.setAppearance(appearance);
        }
        
        if (labelId != null)
        {
            formSet.setLabelId(labelId);
        }
        
        if (template != null)
        {
            formSet.setTemplate(template);
        }
        
        addFormAppearanceElement(formSet);
        
        return formSet;
    }
    
    public FormField addFormField(String id, String labelId, String set)
    {
        FormField field = new FormField();
        
        field.setId(id);
        
        if (labelId != null)
        {
            field.setLabelId(labelId);
        }
        
        if (set != null)
        {
            field.setSet(set);
        }
        
        addFormAppearanceElement(field);
        
        return field;
    }
    
    public FormSet getFormSet(String id) {
    	if(appearanceElements != null) {
    		for(FormAppearanceElement element : appearanceElements) {
    				if(element instanceof FormSet && id.equals(element.getId())) {
    					return (FormSet) element;
    				}
    		}
    	}
    	return null;
    }
}

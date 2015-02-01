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
public class FormFieldVisibility
{

    @XmlElements ({
        @XmlElement(name="show", type=ShowField.class)
    })
    private List<FormFieldVisibilityElement> visibilityElements = new ArrayList<FormFieldVisibilityElement>();

    public List<FormFieldVisibilityElement> getVisibilityElements()
    {
        return visibilityElements;
    }

    public void setVisibilityElements(List<FormFieldVisibilityElement> visibilityElements)
    {
        this.visibilityElements = visibilityElements;
    }
    
    public void addVisibilityElement(FormFieldVisibilityElement fieldVisibilityElement)
    {
        visibilityElements.add(fieldVisibilityElement);
    }
    
    public void addShowFieldElement(String showFieldId)
    {
        ShowField showField = new ShowField();
        showField.setId(showFieldId);
        addVisibilityElement(showField);
    }
}

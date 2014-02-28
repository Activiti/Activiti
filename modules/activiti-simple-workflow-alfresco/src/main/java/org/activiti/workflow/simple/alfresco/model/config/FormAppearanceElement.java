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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * @author Joram Barrez
 */
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class FormAppearanceElement
{

    @XmlAttribute(name="id")
    private String id;
    
    @XmlAttribute(name="label-id")
    private String labelId;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }
    
    public String getLabelId()
    {
        return labelId;
    }

    public void setLabelId(String labelId)
    {
        this.labelId = labelId;
    }
    
}

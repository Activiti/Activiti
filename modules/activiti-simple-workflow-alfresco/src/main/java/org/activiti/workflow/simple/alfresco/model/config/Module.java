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
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Joram Barrez
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="module")
public class Module
{
    
    @XmlElement(name="id", nillable=false, required=true)
    private String id;
    
    @XmlElementWrapper(name="configurations")
    @XmlElement(name="config")
    private List<Configuration> configurations = new ArrayList<Configuration>();

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public List<Configuration> getConfigurations()
    {
        return configurations;
    }

    public void setConfigurations(List<Configuration> configurations)
    {
        this.configurations = configurations;
    }
    
    public void addConfiguration(Configuration configuration)
    {
        configurations.add(configuration);
    }
    
    public Configuration addConfiguration(String evaluator, String condition)
    {
        Configuration configuration = new Configuration();
        configuration.setEvaluator(evaluator);
        configuration.setCondition(condition);
        addConfiguration(configuration);
        return configuration;
    }
    
}

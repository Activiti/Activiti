/**
 * Activiti app component part of the Activiti project
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.activiti.model.editor;

import java.util.Date;

import com.activiti.domain.editor.AbstractModel;
import com.activiti.domain.editor.AppDefinition;
import com.activiti.model.common.AbstractRepresentation;

/**
 * Pojo representation of an app definition: the metadata (name, description, etc)
 * and the actual model ({@link AppDefinition} instance member).
 *
 */
public class AppDefinitionRepresentation extends AbstractRepresentation {

    private Long id;
    private String name;
    private String description;
    private Integer version;
    private Date created;
    private AppDefinition definition;
    
    public AppDefinitionRepresentation() {
        // Empty constructor for Jackson
    }
    
    public AppDefinitionRepresentation(AbstractModel model) {
        this.id = model.getId();
        this.name = model.getName();
        this.description = model.getDescription();
        this.version = model.getVersion();
        this.created = model.getCreated();
    }
    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public Integer getVersion() {
        return version;
    }
    public void setVersion(Integer version) {
        this.version = version;
    }
    public Date getCreated() {
        return created;
    }
    public void setCreated(Date created) {
        this.created = created;
    }
    public AppDefinition getDefinition() {
        return definition;
    }
    public void setDefinition(AppDefinition definition) {
        this.definition = definition;
    }
}
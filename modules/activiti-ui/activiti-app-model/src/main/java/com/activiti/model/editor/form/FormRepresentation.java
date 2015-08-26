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
package com.activiti.model.editor.form;

import java.util.Date;

import com.activiti.domain.editor.AbstractModel;
import com.activiti.model.common.AbstractRepresentation;


public class FormRepresentation extends AbstractRepresentation {

    protected Long id;
    protected String name;
    protected String description;
    protected Integer version;
    protected Long lastUpdatedBy;
	protected String lastUpdatedByFullName;
	protected Date lastUpdated;
    protected FormDefinitionRepresentation formDefinition;
    
    public FormRepresentation(AbstractModel model) {
        this.id = model.getId();
        this.name = model.getName();
        this.description = model.getDescription();
        this.version = model.getVersion();
        this.lastUpdated = model.getLastUpdated();
        this.lastUpdatedBy = model.getLastUpdatedBy().getId();
        this.lastUpdatedByFullName = model.getLastUpdatedBy().getFullName();
    }
    
    public FormRepresentation() {}
    
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

    public Long getLastUpdatedBy() {
		return lastUpdatedBy;
	}

	public void setLastUpdatedBy(Long lastUpdatedBy) {
		this.lastUpdatedBy = lastUpdatedBy;
	}

	public String getLastUpdatedByFullName() {
		return lastUpdatedByFullName;
	}

	public void setLastUpdatedByFullName(String lastUpdatedByFullName) {
		this.lastUpdatedByFullName = lastUpdatedByFullName;
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public FormDefinitionRepresentation getFormDefinition() {
        return formDefinition;
    }

    public void setFormDefinition(FormDefinitionRepresentation formDefinition) {
        this.formDefinition = formDefinition;
    }
}

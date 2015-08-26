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
import com.activiti.domain.editor.Model;
import com.activiti.domain.editor.ModelHistory;
import com.activiti.domain.editor.SharePermission;
import com.activiti.model.common.AbstractRepresentation;
import com.fasterxml.jackson.annotation.JsonIgnore;



/**
 * Representation of process-models, both current and historic models.
 * 
 * @author Tijs Rademakers
 */
public class ModelRepresentation extends AbstractRepresentation {
	
	protected Long id;
	protected String name;
	protected String description;
	protected Long createdBy;
	protected String createdByFullName;
	protected Long lastUpdatedBy;
	protected String lastUpdatedByFullName;
	protected Date lastUpdated;
	protected boolean latestVersion;
    protected int version;
    protected String comment;
    protected Long referenceId;
    protected Integer modelType;
    protected String permission;
	
	public ModelRepresentation(AbstractModel model) {
	    initialize(model);	
	}
	
	public ModelRepresentation() {
	    
	}
	
	public void initialize(AbstractModel model) {
	    this.id = model.getId();
        this.name = model.getName();
        this.description = model.getDescription();
        this.createdBy = model.getCreatedBy().getId();
        this.createdByFullName = model.getCreatedBy().getFullName();
        this.lastUpdated = model.getLastUpdated();
        this.version = model.getVersion();
        this.lastUpdatedBy = model.getLastUpdatedBy().getId();
        this.lastUpdatedByFullName = model.getLastUpdatedBy().getFullName();
        this.comment = model.getComment();
        this.referenceId = model.getReferenceId();
        this.modelType = model.getModelType();
        
        // When based on a ProcessModel and not history, this is always the latest version
        if (model instanceof Model) {
            this.setLatestVersion(true);
        } else if (model instanceof ModelHistory) {
            this.setLatestVersion(false);
        }
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
	
	public Date getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public Long getCreatedBy() {
        return createdBy;
    }
	
	public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }
	
	public String getCreatedByFullName() {
        return createdByFullName;
    }
	
	public void setCreatedByFullName(String createdByFullName) {
        this.createdByFullName = createdByFullName;
    }
	
	public void setLatestVersion(boolean latestVersion) {
		this.latestVersion = latestVersion;
	}

	public boolean isLatestVersion() {
		return latestVersion;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
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
	
	public void setComment(String comment) {
        this.comment = comment;
    }
	
	public String getComment() {
        return comment;
	}

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public Integer getModelType() {
		return modelType;
	}

	public void setModelType(Integer modelType) {
		this.modelType = modelType;
	}
	
	public String getPermission() {
        return permission;
    }
	
	public void setPermission(String permission) {
        this.permission = permission;
    }
	
	@JsonIgnore
	public void setSharePermission(SharePermission permission) {
	    if (permission != null) {
	        this.permission = permission.toString().toLowerCase();
	    }
	}
	
	public Model toModel() {
		Model model = new Model();
		model.setName(name);
		model.setDescription(description);
		return model;
	}

	/**
	 * Update all editable properties of the given {@link Model} based on the
	 * values in this instance.
	 */
    public void updateModel(Model model) {
        model.setDescription(this.description);
        model.setName(this.name);
    }
}

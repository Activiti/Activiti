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
package com.activiti.domain.editor;

import java.util.Date;


/**
 * @author Tijs Rademakers
 */
public class AppModelDefinition {
	
	protected Long id;
	protected String name;
	protected Integer version;
	protected Integer modelType;
	protected String description;
	protected Long stencilSetId;
	protected String createdByFullName;
	protected Long createdBy;
	protected String lastUpdatedByFullName;
	protected Long lastUpdatedBy;
	protected Date lastUpdated;
	
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
    public Integer getVersion() {
        return version;
    }
    public void setVersion(Integer version) {
        this.version = version;
    }
    public Integer getModelType() {
        return modelType;
    }
    public void setModelType(Integer modelType) {
        this.modelType = modelType;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public Long getStencilSetId() {
        return stencilSetId;
    }
    public void setStencilSetId(Long stencilSetId) {
        this.stencilSetId = stencilSetId;
    }
    public String getCreatedByFullName() {
        return createdByFullName;
    }
    public void setCreatedByFullName(String createdByFullName) {
        this.createdByFullName = createdByFullName;
    }
    public Long getCreatedBy() {
        return createdBy;
    }
    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }
    public String getLastUpdatedByFullName() {
        return lastUpdatedByFullName;
    }
    public void setLastUpdatedByFullName(String lastUpdatedByFullName) {
        this.lastUpdatedByFullName = lastUpdatedByFullName;
    }
    public Long getLastUpdatedBy() {
        return lastUpdatedBy;
    }
    public void setLastUpdatedBy(Long lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }
    public Date getLastUpdated() {
        return lastUpdated;
    }
    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}

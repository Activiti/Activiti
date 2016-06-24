/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

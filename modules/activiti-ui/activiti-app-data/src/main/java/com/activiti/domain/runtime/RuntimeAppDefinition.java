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
package com.activiti.domain.runtime;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.activiti.domain.common.IdBlockSize;

/**
 * A definition of an app deployed in the activiti engine, based on an app definition model,
 * created in the editor.
 * 
 * @author Frederik Heremans
 */
@Entity
@Table(name="RUNTIME_APP_DEF")
public class RuntimeAppDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "runtimeAppDefinitionIdGenerator")
    @TableGenerator(name = "runtimeAppDefinitionIdGenerator", allocationSize = IdBlockSize.DEFAULT_ALLOCATION_SIZE)
    @Column(name="id")
    protected Long id;
    
    @Column(name="name")
    protected String name;

    @Column(name="description")
    protected String description;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="created")
    protected Date created;

    @Column(name="created_by")
    protected String createdBy;
    
    @Column(name="deployment_id")
    protected String deploymentId;
    
    @Column(name="app_definition")
    protected String definition;
    
    @Column(name="model_id")
    protected Long modelId;
    
    @Column(name="tenant_id")
    protected Long tenantId;

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

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public Long getModelId() {
        return modelId;
    }
    
    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }
}

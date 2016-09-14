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
package org.activiti.app.model.runtime;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.activiti.app.model.common.AbstractRepresentation;
import org.activiti.engine.repository.ProcessDefinition;

/**
 * REST representation of a process definition.
 * 
 * @author Tijs Rademakers
 */
public class ProcessDefinitionRepresentation extends AbstractRepresentation {

    protected String id;
    protected String name;
    protected String description;
    protected String key;
    protected String category;
    protected int version;
    protected String deploymentId;
    protected String tenantId;
    protected boolean hasStartForm;
    
    public ProcessDefinitionRepresentation(ProcessDefinition processDefinition) {
        this.id = processDefinition.getId();
        this.name = processDefinition.getName();
        this.description = processDefinition.getDescription();
        this.key = processDefinition.getKey();
        this.category = processDefinition.getCategory();
        this.version = processDefinition.getVersion();
        this.deploymentId = processDefinition.getDeploymentId();
        this.tenantId = processDefinition.getTenantId();
        this.hasStartForm = processDefinition.hasStartFormKey();
    }
    
    public ProcessDefinitionRepresentation() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
    
    public void setHasStartForm(boolean hasStartForm) {
        this.hasStartForm = hasStartForm;
    }
    @JsonProperty("hasStartForm")
    public boolean getHasStartForm() {
        return hasStartForm;
    }
    
}

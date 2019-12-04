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
package org.activiti.engine.impl.persistence.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.bpmn.data.IOSpecification;
import org.activiti.engine.impl.context.Context;

public class ProcessDefinitionEntityImpl extends AbstractEntity implements ProcessDefinitionEntity, Serializable {

  private static final long serialVersionUID = 1L;

  protected String name;
  protected String description;
  protected String key;
  protected int version;
  protected String category;
  protected String deploymentId;
  protected String resourceName;
  protected String tenantId = ProcessEngineConfiguration.NO_TENANT_ID;
  protected Integer historyLevel;
  protected String diagramResourceName;
  protected boolean isGraphicalNotationDefined;
  protected Map<String, Object> variables;
  protected boolean hasStartFormKey;
  protected int suspensionState = SuspensionState.ACTIVE.getStateCode();
  protected boolean isIdentityLinksInitialized;
  protected List<IdentityLinkEntity> definitionIdentityLinkEntities = new ArrayList<IdentityLinkEntity>();
  protected IOSpecification ioSpecification;
  protected Integer appVersion;

    // Backwards compatibility
  protected String engineVersion;
  
  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("suspensionState", this.suspensionState);
    persistentState.put("category", this.category);
    return persistentState;
  }

  // getters and setters
  // //////////////////////////////////////////////////////

  public List<IdentityLinkEntity> getIdentityLinks() {
    if (!isIdentityLinksInitialized) {
      definitionIdentityLinkEntities = Context.getCommandContext().getIdentityLinkEntityManager().findIdentityLinksByProcessDefinitionId(id);
      isIdentityLinksInitialized = true;
    }

    return definitionIdentityLinkEntities;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  public String getDeploymentId() {
    return deploymentId;
  }

  public void setDeploymentId(String deploymentId) {
    this.deploymentId = deploymentId;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public String getResourceName() {
    return resourceName;
  }

  public void setResourceName(String resourceName) {
    this.resourceName = resourceName;
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public Integer getHistoryLevel() {
    return historyLevel;
  }

  public void setHistoryLevel(Integer historyLevel) {
    this.historyLevel = historyLevel;
  }

  public Map<String, Object> getVariables() {
    return variables;
  }

  public void setVariables(Map<String, Object> variables) {
    this.variables = variables;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getDiagramResourceName() {
    return diagramResourceName;
  }

  public void setDiagramResourceName(String diagramResourceName) {
    this.diagramResourceName = diagramResourceName;
  }

  public boolean hasStartFormKey() {
    return hasStartFormKey;
  }

  public boolean getHasStartFormKey() {
    return hasStartFormKey;
  }

  public void setStartFormKey(boolean hasStartFormKey) {
    this.hasStartFormKey = hasStartFormKey;
  }

  public void setHasStartFormKey(boolean hasStartFormKey) {
    this.hasStartFormKey = hasStartFormKey;
  }

  public boolean isGraphicalNotationDefined() {
    return isGraphicalNotationDefined;
  }

  public boolean hasGraphicalNotation() {
    return isGraphicalNotationDefined;
  }

  public void setGraphicalNotationDefined(boolean isGraphicalNotationDefined) {
    this.isGraphicalNotationDefined = isGraphicalNotationDefined;
  }

  public int getSuspensionState() {
    return suspensionState;
  }

  public void setSuspensionState(int suspensionState) {
    this.suspensionState = suspensionState;
  }

  public boolean isSuspended() {
    return suspensionState == SuspensionState.SUSPENDED.getStateCode();
  }

  public String getEngineVersion() {
    return engineVersion;
  }

  public void setEngineVersion(String engineVersion) {
    this.engineVersion = engineVersion;
  }
  
  public IOSpecification getIoSpecification() {
    return ioSpecification;
  }

  public void setIoSpecification(IOSpecification ioSpecification) {
    this.ioSpecification = ioSpecification;
  }
  
  public String toString() {
    return "ProcessDefinitionEntity[" + id + "]";
  }

  public void setAppVersion(Integer appVersion){
      this.appVersion = appVersion;
  }

  public Integer getAppVersion(){
      return this.appVersion;
  }

}

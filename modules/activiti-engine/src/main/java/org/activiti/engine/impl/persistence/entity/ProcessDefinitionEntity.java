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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.event.impl.ActivitiEventSupport;
import org.activiti.engine.impl.bpmn.data.IOSpecification;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.HasRevision;
import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.IdentityLinkType;

/**
 * @author Joram Barrez
 * @author Tijs Rademakers
 */
public class ProcessDefinitionEntity implements ProcessDefinition, PersistentObject, HasRevision, Serializable {

  private static final long serialVersionUID = 1L;

  protected String id;
  protected String name;
  protected String description;
  protected String key;
  protected int revision = 1;
  protected int version;
  protected String category;
  protected String deploymentId;
  protected String resourceName;
  protected String tenantId = ProcessEngineConfiguration.NO_TENANT_ID;
  protected Integer historyLevel;
  protected String diagramResourceName;
  protected boolean isGraphicalNotationDefined;
  protected Map<String, TaskDefinition> taskDefinitions;
  protected Map<String, Object> variables;
  protected boolean hasStartFormKey;
  protected int suspensionState = SuspensionState.ACTIVE.getStateCode();
  protected boolean isIdentityLinksInitialized;
  protected List<IdentityLinkEntity> definitionIdentityLinkEntities = new ArrayList<IdentityLinkEntity>();
  protected Set<Expression> candidateStarterUserIdExpressions = new HashSet<Expression>();
  protected Set<Expression> candidateStarterGroupIdExpressions = new HashSet<Expression>();
  protected transient ActivitiEventSupport eventSupport;
  protected IOSpecification ioSpecification;

  // Backwards compatibility
  protected String engineVersion;

  public ProcessDefinitionEntity() {
    eventSupport = new ActivitiEventSupport();
  }

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    eventSupport = new ActivitiEventSupport();

  }

  public IdentityLinkEntity addIdentityLink(String userId, String groupId) {
    IdentityLinkEntity identityLinkEntity = new IdentityLinkEntity();
    getIdentityLinks().add(identityLinkEntity);
    identityLinkEntity.setProcessDef(this);
    identityLinkEntity.setUserId(userId);
    identityLinkEntity.setGroupId(groupId);
    identityLinkEntity.setType(IdentityLinkType.CANDIDATE);
    identityLinkEntity.insert();
    return identityLinkEntity;
  }

  public void deleteIdentityLink(String userId, String groupId) {
    List<IdentityLinkEntity> identityLinks = Context.getCommandContext().getIdentityLinkEntityManager().findIdentityLinkByProcessDefinitionUserAndGroup(id, userId, groupId);

    for (IdentityLinkEntity identityLink : identityLinks) {
      Context.getCommandContext().getIdentityLinkEntityManager().deleteIdentityLink(identityLink, false);
    }
  }

  public List<IdentityLinkEntity> getIdentityLinks() {
    if (!isIdentityLinksInitialized) {
      definitionIdentityLinkEntities = Context.getCommandContext().getIdentityLinkEntityManager().findIdentityLinksByProcessDefinitionId(id);
      isIdentityLinksInitialized = true;
    }

    return definitionIdentityLinkEntities;
  }

  public String toString() {
    return "ProcessDefinitionEntity[" + id + "]";
  }

  // getters and setters
  // //////////////////////////////////////////////////////

  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("suspensionState", this.suspensionState);
    persistentState.put("category", this.category);
    return persistentState;
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

  public String getId() {
    return id;
  }
  
  public void setId(String id) {
    this.id = id;
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

  public Map<String, TaskDefinition> getTaskDefinitions() {
    return taskDefinitions;
  }

  public void setTaskDefinitions(Map<String, TaskDefinition> taskDefinitions) {
    this.taskDefinitions = taskDefinitions;
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

  public int getRevision() {
    return revision;
  }

  public void setRevision(int revision) {
    this.revision = revision;
  }

  public int getRevisionNext() {
    return revision + 1;
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

  public Set<Expression> getCandidateStarterUserIdExpressions() {
    return candidateStarterUserIdExpressions;
  }

  public void addCandidateStarterUserIdExpression(Expression userId) {
    candidateStarterUserIdExpressions.add(userId);
  }

  public Set<Expression> getCandidateStarterGroupIdExpressions() {
    return candidateStarterGroupIdExpressions;
  }

  public void addCandidateStarterGroupIdExpression(Expression groupId) {
    candidateStarterGroupIdExpressions.add(groupId);
  }

  public ActivitiEventSupport getEventSupport() {
    return eventSupport;
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

}

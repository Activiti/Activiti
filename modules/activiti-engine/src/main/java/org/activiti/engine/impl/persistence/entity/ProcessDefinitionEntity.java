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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.event.impl.ActivitiEventSupport;
import org.activiti.engine.impl.bpmn.data.IOSpecification;
import org.activiti.engine.impl.db.Entity;
import org.activiti.engine.impl.db.HasRevision;
import org.activiti.engine.repository.ProcessDefinition;

/**
 * @author Joram Barrez
 * @author Tijs Rademakers
 */
public interface ProcessDefinitionEntity extends ProcessDefinition, Entity, HasRevision {

  List<IdentityLinkEntity> getIdentityLinks();

  void setKey(String key);

  void setName(String name);

  void setDescription(String description);

  void setDeploymentId(String deploymentId);

  void setVersion(int version);

  void setResourceName(String resourceName);

  void setTenantId(String tenantId);

  Integer getHistoryLevel();

  void setHistoryLevel(Integer historyLevel);

  Map<String, Object> getVariables();

  void setVariables(Map<String, Object> variables);

  void setCategory(String category);

  void setDiagramResourceName(String diagramResourceName);

  boolean getHasStartFormKey();

  void setStartFormKey(boolean hasStartFormKey);

  void setHasStartFormKey(boolean hasStartFormKey);

  boolean isGraphicalNotationDefined();

  void setGraphicalNotationDefined(boolean isGraphicalNotationDefined);

  int getSuspensionState();

  void setSuspensionState(int suspensionState);

  Set<Expression> getCandidateStarterUserIdExpressions();

  void addCandidateStarterUserIdExpression(Expression userId);

  Set<Expression> getCandidateStarterGroupIdExpressions();

  void addCandidateStarterGroupIdExpression(Expression groupId);

  ActivitiEventSupport getEventSupport();

  String getEngineVersion();

  void setEngineVersion(String engineVersion);
  
  IOSpecification getIoSpecification();

  void setIoSpecification(IOSpecification ioSpecification);
  
}

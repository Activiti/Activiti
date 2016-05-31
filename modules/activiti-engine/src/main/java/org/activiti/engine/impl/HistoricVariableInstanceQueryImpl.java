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

package org.activiti.engine.impl;

import java.util.List;
import java.util.Set;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.history.HistoricVariableInstanceQuery;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.HistoricVariableInstanceEntity;
import org.activiti.engine.impl.variable.CacheableVariable;
import org.activiti.engine.impl.variable.JPAEntityListVariableType;
import org.activiti.engine.impl.variable.JPAEntityVariableType;
import org.activiti.engine.impl.variable.VariableTypes;

/**
 * @author Christian Lipphardt (camunda)
 */
public class HistoricVariableInstanceQueryImpl extends AbstractQuery<HistoricVariableInstanceQuery, HistoricVariableInstance> implements
        HistoricVariableInstanceQuery {

  private static final long serialVersionUID = 1L;
  protected String id;
  protected String taskId;
  protected Set<String> taskIds;
  protected String executionId;
  protected Set<String> executionIds;
  protected String processInstanceId;
  protected String activityInstanceId;
  protected String variableName;
  protected String variableNameLike;
  protected boolean excludeTaskRelated = false;
  protected boolean excludeVariableInitialization = false;
  protected QueryVariableValue queryVariableValue;

  public HistoricVariableInstanceQueryImpl() {
  }

  public HistoricVariableInstanceQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }

  public HistoricVariableInstanceQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }
  
  public HistoricVariableInstanceQuery id(String id) {
    this.id = id;
    return this;
  }

  public HistoricVariableInstanceQueryImpl processInstanceId(String processInstanceId) {
    if (processInstanceId == null) {
      throw new ActivitiIllegalArgumentException("processInstanceId is null");
    }
    this.processInstanceId = processInstanceId;
    return this;
  }
  
  public HistoricVariableInstanceQueryImpl executionId(String executionId) {
    if (executionId == null) {
      throw new ActivitiIllegalArgumentException("Execution id is null");
    }
    this.executionId = executionId;
    return this;
  }

  public HistoricVariableInstanceQueryImpl executionIds(Set<String> executionIds) {
    if (executionIds == null) {
      throw new ActivitiIllegalArgumentException("executionIds is null");
    }
    if (executionIds.isEmpty()){
        throw new ActivitiIllegalArgumentException("Set of executionIds is empty");
    }
    this.executionIds = executionIds;
    return this;
  }

  public HistoricVariableInstanceQuery activityInstanceId(String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
    return this;
  }

  public HistoricVariableInstanceQuery taskId(String taskId) {
    if (taskId == null) {
      throw new ActivitiIllegalArgumentException("taskId is null");
    }
    if(excludeTaskRelated) {
      throw new ActivitiIllegalArgumentException("Cannot use taskId together with excludeTaskVariables");
    }
    this.taskId = taskId;
    return this;
  }
  
  public HistoricVariableInstanceQueryImpl taskIds(Set<String> taskIds) {
    if (taskIds == null) {
      throw new ActivitiIllegalArgumentException("taskIds is null");
    }
    if (taskIds.isEmpty()){
        throw new ActivitiIllegalArgumentException("Set of taskIds is empty");
    }
    if (excludeTaskRelated) {
        throw new ActivitiIllegalArgumentException("Cannot use taskIds together with excludeTaskVariables");
    }
    this.taskIds = taskIds;
    return this;
  }
  
  @Override
  public HistoricVariableInstanceQuery excludeTaskVariables() {
    if (taskId != null) {
      throw new ActivitiIllegalArgumentException("Cannot use taskId together with excludeTaskVariables");
    }
    if (taskIds != null) {
      throw new ActivitiIllegalArgumentException("Cannot use taskIds together with excludeTaskVariables");
    }
    excludeTaskRelated = true;
    return this;
  }
  
  public HistoricVariableInstanceQuery excludeVariableInitialization() {
    excludeVariableInitialization = true;
    return this;
  }

  public HistoricVariableInstanceQuery variableName(String variableName) {
    if (variableName == null) {
      throw new ActivitiIllegalArgumentException("variableName is null");
    }
    this.variableName = variableName;
    return this;
  }

  public HistoricVariableInstanceQuery variableValueEquals(String variableName, Object variableValue) {
    if (variableName == null) {
      throw new ActivitiIllegalArgumentException("variableName is null");
    }
    if (variableValue == null) {
      throw new ActivitiIllegalArgumentException("variableValue is null");
    }
    this.variableName = variableName;
    queryVariableValue = new QueryVariableValue(variableName, variableValue, QueryOperator.EQUALS, true);
    return this;
  }
  
  public HistoricVariableInstanceQuery variableValueNotEquals(String variableName, Object variableValue) {
    if (variableName == null) {
      throw new ActivitiIllegalArgumentException("variableName is null");
    }
    if (variableValue == null) {
      throw new ActivitiIllegalArgumentException("variableValue is null");
    }
    this.variableName = variableName;
    queryVariableValue = new QueryVariableValue(variableName, variableValue, QueryOperator.NOT_EQUALS, true);
    return this;
  }
  
  public HistoricVariableInstanceQuery variableValueLike(String variableName, String variableValue) {
    if (variableName == null) {
      throw new ActivitiIllegalArgumentException("variableName is null");
    }
    if (variableValue == null) {
      throw new ActivitiIllegalArgumentException("variableValue is null");
    }
    this.variableName = variableName;
    queryVariableValue = new QueryVariableValue(variableName, variableValue, QueryOperator.LIKE, true);
    return this;
  }
  
  public HistoricVariableInstanceQuery variableValueLikeIgnoreCase(String variableName, String variableValue) {
    if (variableName == null) {
      throw new ActivitiIllegalArgumentException("variableName is null");
    }
    if (variableValue == null) {
      throw new ActivitiIllegalArgumentException("variableValue is null");
    }
    this.variableName = variableName;
    queryVariableValue = new QueryVariableValue(variableName, variableValue.toLowerCase(), QueryOperator.LIKE_IGNORE_CASE, true);
    return this;
  }

  public HistoricVariableInstanceQuery variableNameLike(String variableNameLike) {
    if (variableNameLike == null) {
      throw new ActivitiIllegalArgumentException("variableNameLike is null");
    }
    this.variableNameLike = variableNameLike;
    return this;
  }

  protected void ensureVariablesInitialized() {
    if (this.queryVariableValue != null) {
      VariableTypes variableTypes = Context.getProcessEngineConfiguration().getVariableTypes();
      queryVariableValue.initialize(variableTypes);
    }
  }

  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    ensureVariablesInitialized();
    return commandContext
        .getHistoricVariableInstanceEntityManager()
        .findHistoricVariableInstanceCountByQueryCriteria(this);
  }

  public List<HistoricVariableInstance> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    ensureVariablesInitialized();
    
    List<HistoricVariableInstance> historicVariableInstances = commandContext
            .getHistoricVariableInstanceEntityManager()
            .findHistoricVariableInstancesByQueryCriteria(this, page);
    
    if (excludeVariableInitialization == false) {
      for (HistoricVariableInstance historicVariableInstance: historicVariableInstances) {
        if (historicVariableInstance instanceof HistoricVariableInstanceEntity) {
          HistoricVariableInstanceEntity variableEntity = (HistoricVariableInstanceEntity) historicVariableInstance;
          if(variableEntity != null && variableEntity.getVariableType() != null) {
            variableEntity.getValue();
            
            // make sure JPA entities are cached for later retrieval
            if (JPAEntityVariableType.TYPE_NAME.equals(variableEntity.getVariableType().getTypeName()) || JPAEntityListVariableType.TYPE_NAME.equals(variableEntity.getVariableType().getTypeName())) {
              ((CacheableVariable) variableEntity.getVariableType()).setForceCacheable(true);
            }
          }
        }
      }
    }
    return historicVariableInstances;
  }

  // order by /////////////////////////////////////////////////////////////////

  public HistoricVariableInstanceQuery orderByProcessInstanceId() {
    orderBy(HistoricVariableInstanceQueryProperty.PROCESS_INSTANCE_ID);
    return this;
  }

  public HistoricVariableInstanceQuery orderByVariableName() {
    orderBy(HistoricVariableInstanceQueryProperty.VARIABLE_NAME);
    return this;
  }

  // getters and setters //////////////////////////////////////////////////////

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public String getTaskId() {
    return taskId;
  }

  public String getActivityInstanceId() {
    return activityInstanceId;
  }

  public boolean getExcludeTaskRelated() {
    return excludeTaskRelated;
  }

  public String getVariableName() {
    return variableName;
  }

  public String getVariableNameLike() {
    return variableNameLike;
  }

  public QueryVariableValue getQueryVariableValue() {
    return queryVariableValue;
  }

}

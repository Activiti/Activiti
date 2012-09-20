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

import org.activiti.engine.ActivitiException;
import org.activiti.engine.history.HistoricDetail;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.history.HistoricVariableInstanceQuery;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.HistoricDetailVariableInstanceUpdateEntity;
import org.activiti.engine.impl.persistence.entity.HistoricVariableInstanceEntity;
import org.activiti.engine.impl.variable.VariableTypes;

/**
 * @author Christian Lipphardt (camunda)
 */
public class HistoricVariableInstanceQueryImpl extends AbstractQuery<HistoricVariableInstanceQuery, HistoricVariableInstance> implements
        HistoricVariableInstanceQuery {

  private static final long serialVersionUID = 1L;
  protected String taskId;
  protected String processInstanceId;
  protected String activityInstanceId;
  protected String variableName;
  protected String variableNameLike;
  protected boolean excludeTaskRelated = false;
  protected QueryVariableValue queryVariableValue;

  public HistoricVariableInstanceQueryImpl() {
  }

  public HistoricVariableInstanceQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }

  public HistoricVariableInstanceQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public HistoricVariableInstanceQueryImpl processInstanceId(String processInstanceId) {
    if (processInstanceId == null) {
      throw new ActivitiException("processInstanceId is null");
    }
    this.processInstanceId = processInstanceId;
    return this;
  }

  public HistoricVariableInstanceQuery activityInstanceId(String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
    return this;
  }

  public HistoricVariableInstanceQuery taskId(String taskId) {
    if (taskId == null) {
      throw new ActivitiException("taskId is null");
    }
    this.taskId = taskId;
    return this;
  }

  public HistoricVariableInstanceQuery variableName(String variableName) {
    if (variableName == null) {
      throw new ActivitiException("variableName is null");
    }
    this.variableName = variableName;
    return this;
  }

  public HistoricVariableInstanceQuery variableValueEquals(String variableName, Object variableValue) {
    if (variableName == null) {
      throw new ActivitiException("variableName is null");
    }
    if (variableValue == null) {
      throw new ActivitiException("variableValue is null");
    }
    this.variableName = variableName;
    queryVariableValue = new QueryVariableValue(variableName, variableValue, QueryOperator.EQUALS, true);
    return this;
  }

  public HistoricVariableInstanceQuery variableNameLike(String variableNameLike) {
    if (variableNameLike == null) {
      throw new ActivitiException("variableNameLike is null");
    }
    this.variableNameLike = variableNameLike;
    return this;
  }

  public HistoricVariableInstanceQuery excludeTaskDetails() {
    this.excludeTaskRelated = true;
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
    return commandContext.getHistoricVariableInstanceManager().findHistoricVariableInstanceCountByQueryCriteria(this);
  }

  public List<HistoricVariableInstance> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    ensureVariablesInitialized();
    List<HistoricVariableInstance> historicVariableInstances = commandContext
            .getHistoricVariableInstanceManager()
            .findHistoricVariableInstancesByQueryCriteria(this, page);
    if (historicVariableInstances!=null) {
      for (HistoricVariableInstance historicVariableInstance: historicVariableInstances) {
        if (historicVariableInstance instanceof HistoricVariableInstanceEntity) {
          ((HistoricVariableInstanceEntity)historicVariableInstance).getByteArrayValue();
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

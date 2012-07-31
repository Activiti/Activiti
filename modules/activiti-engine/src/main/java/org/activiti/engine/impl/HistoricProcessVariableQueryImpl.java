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
import org.activiti.engine.history.HistoricProcessVariable;
import org.activiti.engine.history.HistoricProcessVariableQuery;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;


/**
 * @author Christian Lipphardt (camunda)
 */
public class HistoricProcessVariableQueryImpl extends AbstractQuery<HistoricProcessVariableQuery, HistoricProcessVariable> implements HistoricProcessVariableQuery {

  private static final long serialVersionUID = 1L;
  protected String taskId;
  protected String processInstanceId;
  protected String activityInstanceId;
  protected String variableName;
  protected String variableNameLike;
  protected boolean excludeTaskRelated = false;

  public HistoricProcessVariableQueryImpl() {
  }

  public HistoricProcessVariableQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }

  public HistoricProcessVariableQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public HistoricProcessVariableQueryImpl processInstanceId(String processInstanceId) {
    if (processInstanceId == null) {
      throw new ActivitiException("processInstanceId is null");
    }
    this.processInstanceId = processInstanceId;
    return this;
  }

  public HistoricProcessVariableQuery activityInstanceId(String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
    return this;
  }

  public HistoricProcessVariableQuery taskId(String taskId) {
    if (taskId == null) {
      throw new ActivitiException("taskId is null");
    }
    this.taskId = taskId;
    return this;
  }

  public HistoricProcessVariableQuery variableName(String variableName) {
    if (variableName == null) {
      throw new ActivitiException("variableName is null");
    }
    this.variableName = variableName;
    return this;
  }
  
  public HistoricProcessVariableQuery variableNameLike(String variableNameLike) {
    if (variableNameLike == null) {
      throw new ActivitiException("variableNameLike is null");
    }
    this.variableNameLike = variableNameLike;
    return this;
  }
  
  public HistoricProcessVariableQuery excludeTaskDetails() {
    this.excludeTaskRelated = true;
    return this;
  }

  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getHistoricProcessVariableManager()
      .findHistoricProcessVariableCountByQueryCriteria(this);
  }

  public List<HistoricProcessVariable> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext
      .getHistoricProcessVariableManager()
      .findHistoricProcessVariableByQueryCriteria(this, page);
  }
  
  // order by /////////////////////////////////////////////////////////////////
  
  public HistoricProcessVariableQuery orderByProcessInstanceId() {
    orderBy(HistoricProcessVariableQueryProperty.PROCESS_INSTANCE_ID);
    return this;
  }

  public HistoricProcessVariableQuery orderByTime() {
    orderBy(HistoricProcessVariableQueryProperty.TIME);
    return this;
  }

  public HistoricProcessVariableQuery orderByVariableName() {
    orderBy(HistoricProcessVariableQueryProperty.VARIABLE_NAME);
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
  
}

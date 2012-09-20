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

import org.activiti.engine.history.HistoricDetail;
import org.activiti.engine.history.HistoricDetailQuery;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.HistoricDetailVariableInstanceUpdateEntity;


/**
 * @author Tom Baeyens
 */
public class HistoricDetailQueryImpl extends AbstractQuery<HistoricDetailQuery, HistoricDetail> implements HistoricDetailQuery {

  private static final long serialVersionUID = 1L;
  protected String taskId;
  protected String processInstanceId;
  protected String executionId;
  protected String activityId;
  protected String activityInstanceId;
  protected String type;
  protected boolean excludeTaskRelated = false;

  public HistoricDetailQueryImpl() {
  }

  public HistoricDetailQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }

  public HistoricDetailQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public HistoricDetailQuery processInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
    return this;
  }
  
  public HistoricDetailQuery executionId(String executionId) {
    this.executionId = executionId;
    return this;
  }

  public HistoricDetailQuery activityId(String activityId) {
    this.activityId = activityId;
    return this;
  }

  public HistoricDetailQuery activityInstanceId(String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
    return this;
  }

  public HistoricDetailQuery taskId(String taskId) {
    this.taskId = taskId;
    return this;
  }

  public HistoricDetailQuery formProperties() {
    this.type = "FormProperty";
    return this;
  }

  public HistoricDetailQuery variableUpdates() {
    this.type = "VariableUpdate";
    return this;
  }
  
  public HistoricDetailQuery excludeTaskDetails() {
    this.excludeTaskRelated = true;
    return this;
  }

  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getHistoricDetailManager()
      .findHistoricDetailCountByQueryCriteria(this);
  }

  public List<HistoricDetail> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    List<HistoricDetail> historicDetails = commandContext
      .getHistoricDetailManager()
      .findHistoricDetailsByQueryCriteria(this, page);
    if (historicDetails!=null) {
      for (HistoricDetail historicDetail: historicDetails) {
        if (historicDetail instanceof HistoricDetailVariableInstanceUpdateEntity) {
          ((HistoricDetailVariableInstanceUpdateEntity)historicDetail).getByteArrayValue();
        }
      }
    }
    return historicDetails;
  }
  
  // order by /////////////////////////////////////////////////////////////////
  
  public HistoricDetailQuery orderByProcessInstanceId() {
    orderBy(HistoricDetailQueryProperty.PROCESS_INSTANCE_ID);
    return this;
  }

  public HistoricDetailQuery orderByTime() {
    orderBy(HistoricDetailQueryProperty.TIME);
    return this;
  }

  public HistoricDetailQuery orderByVariableName() {
    orderBy(HistoricDetailQueryProperty.VARIABLE_NAME);
    return this;
  }
  
  public HistoricDetailQuery orderByFormPropertyId() {
    orderBy(HistoricDetailQueryProperty.VARIABLE_NAME);
    return this;
  }

  public HistoricDetailQuery orderByVariableRevision() {
    orderBy(HistoricDetailQueryProperty.VARIABLE_REVISION);
    return this;
  }

  public HistoricDetailQuery orderByVariableType() {
    orderBy(HistoricDetailQueryProperty.VARIABLE_TYPE);
    return this;
  }
  
  // getters and setters //////////////////////////////////////////////////////
  
  public String getProcessInstanceId() {
    return processInstanceId;
  }
  
  public String getTaskId() {
    return taskId;
  }
  
  public String getActivityId() {
    return activityId;
  }
  
  public String getType() {
    return type;
  }
  
  public boolean getExcludeTaskRelated() {
    return excludeTaskRelated;
  }
}

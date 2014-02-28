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

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;


/**
 * @author Tom Baeyens
 */
public class HistoricActivityInstanceQueryImpl extends AbstractQuery<HistoricActivityInstanceQuery, HistoricActivityInstance> 
    implements HistoricActivityInstanceQuery {
  
  private static final long serialVersionUID = 1L;
  protected String activityInstanceId;
  protected String processInstanceId;
  protected String executionId;
  protected String processDefinitionId;
  protected String activityId;
  protected String activityName;
  protected String activityType;
  protected String assignee;
  protected String tenantId;
  protected String tenantIdLike;
  protected boolean withoutTenantId;
  protected boolean finished;
  protected boolean unfinished;

  public HistoricActivityInstanceQueryImpl() {
  }
  
  public HistoricActivityInstanceQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }
  
  public HistoricActivityInstanceQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  @Override
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getHistoricActivityInstanceEntityManager()
      .findHistoricActivityInstanceCountByQueryCriteria(this);
  }

  @Override
  public List<HistoricActivityInstance> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext
      .getHistoricActivityInstanceEntityManager()
      .findHistoricActivityInstancesByQueryCriteria(this, page);
  }
  
  public HistoricActivityInstanceQueryImpl processInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
    return this;
  }

  public HistoricActivityInstanceQueryImpl executionId(String executionId) {
    this.executionId = executionId;
    return this;
  }

  public HistoricActivityInstanceQueryImpl processDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  public HistoricActivityInstanceQueryImpl activityId(String activityId) {
    this.activityId = activityId;
    return this;
  }

  public HistoricActivityInstanceQueryImpl activityName(String activityName) {
    this.activityName = activityName;
    return this;
  }

  public HistoricActivityInstanceQueryImpl activityType(String activityType) {
    this.activityType = activityType;
    return this;
  }
  
  public HistoricActivityInstanceQueryImpl taskAssignee(String assignee) {
    this.assignee = assignee;
    return this;
  }
  
  public HistoricActivityInstanceQueryImpl finished() {
    this.finished = true;
    return this;
  }
  
  public HistoricActivityInstanceQueryImpl unfinished() {
    this.unfinished = true;
    return this;
  }
  
  public HistoricActivityInstanceQueryImpl activityTenantId(String tenantId) {
  	if (tenantId == null) {
  		throw new ActivitiIllegalArgumentException("activity tenant id is null");
  	}
  	this.tenantId = tenantId;
  	return this;
  }
  
  public HistoricActivityInstanceQueryImpl activityTenantIdLike(String tenantIdLike) {
  	if (tenantIdLike == null) {
  		throw new ActivitiIllegalArgumentException("activity tenant id is null");
  	}
  	this.tenantIdLike = tenantIdLike;
  	return this;
  }
  
  public HistoricActivityInstanceQueryImpl activityWithoutTenantId() {
  	this.withoutTenantId = true;
  	return this;
  }
  

  // ordering /////////////////////////////////////////////////////////////////

  public HistoricActivityInstanceQueryImpl orderByHistoricActivityInstanceDuration() {
    orderBy(HistoricActivityInstanceQueryProperty.DURATION);
    return this;
  }

  public HistoricActivityInstanceQueryImpl orderByHistoricActivityInstanceEndTime() {
    orderBy(HistoricActivityInstanceQueryProperty.END);
    return this;
  }

  public HistoricActivityInstanceQueryImpl orderByExecutionId() {
    orderBy(HistoricActivityInstanceQueryProperty.EXECUTION_ID);
    return this;
  }

  public HistoricActivityInstanceQueryImpl orderByHistoricActivityInstanceId() {
    orderBy(HistoricActivityInstanceQueryProperty.HISTORIC_ACTIVITY_INSTANCE_ID);
    return this;
  }

  public HistoricActivityInstanceQueryImpl orderByProcessDefinitionId() {
    orderBy(HistoricActivityInstanceQueryProperty.PROCESS_DEFINITION_ID);
    return this;
  }

  public HistoricActivityInstanceQueryImpl orderByProcessInstanceId() {
    orderBy(HistoricActivityInstanceQueryProperty.PROCESS_INSTANCE_ID);
    return this;
  }

  public HistoricActivityInstanceQueryImpl orderByHistoricActivityInstanceStartTime() {
    orderBy(HistoricActivityInstanceQueryProperty.START);
    return this;
  }

  public HistoricActivityInstanceQuery orderByActivityId() {
    orderBy(HistoricActivityInstanceQueryProperty.ACTIVITY_ID);
    return this;
  }

  public HistoricActivityInstanceQueryImpl orderByActivityName() {
    orderBy(HistoricActivityInstanceQueryProperty.ACTIVITY_NAME);
    return this;
  }

  public HistoricActivityInstanceQueryImpl orderByActivityType() {
    orderBy(HistoricActivityInstanceQueryProperty.ACTIVITY_TYPE);
    return this;
  }
  
  public HistoricActivityInstanceQueryImpl orderByTenantId() {
  	orderBy(HistoricActivityInstanceQueryProperty.TENANT_ID);
    return this;
  }

  public HistoricActivityInstanceQueryImpl activityInstanceId(String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
    return this;
  }
  

  // getters and setters //////////////////////////////////////////////////////
  
  public String getProcessInstanceId() {
    return processInstanceId;
  }
  public String getExecutionId() {
    return executionId;
  }
  public String getProcessDefinitionId() {
    return processDefinitionId;
  }
  public String getActivityId() {
    return activityId;
  }
  public String getActivityName() {
    return activityName;
  }
  public String getActivityType() {
    return activityType;
  }
  public String getAssignee() {
    return assignee;
  }
  public boolean isFinished() {
    return finished;
  }
  public boolean isUnfinished() {
    return unfinished;
  }
  public String getActivityInstanceId() {
    return activityInstanceId;
  }
}

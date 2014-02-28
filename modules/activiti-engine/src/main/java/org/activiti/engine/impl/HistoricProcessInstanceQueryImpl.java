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

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;


/**
 * @author Tom Baeyens
 * @author Falko Menge
 * @author Bernd Ruecker
 */
public class HistoricProcessInstanceQueryImpl extends AbstractVariableQueryImpl<HistoricProcessInstanceQuery, HistoricProcessInstance> implements HistoricProcessInstanceQuery {

  private static final long serialVersionUID = 1L;
  protected String processInstanceId;
  protected String processDefinitionId;
  protected String businessKey;
  protected boolean finished = false;
  protected boolean unfinished = false;
  protected String startedBy;
  protected String superProcessInstanceId;
  protected boolean excludeSubprocesses;
  protected List<String> processKeyNotIn;
  protected Date startedBefore;
  protected Date startedAfter;
  protected Date finishedBefore;
  protected Date finishedAfter;
  protected String processDefinitionKey;
  protected Set<String> processInstanceIds;
  protected String involvedUser;
  protected boolean includeProcessVariables;
  protected String tenantId;
  protected String tenantIdLike;
  protected boolean withoutTenantId;
  
  public HistoricProcessInstanceQueryImpl() {
  }
  
  public HistoricProcessInstanceQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }
  
  public HistoricProcessInstanceQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public HistoricProcessInstanceQueryImpl processInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
    return this;
  }

  public HistoricProcessInstanceQuery processInstanceIds(Set<String> processInstanceIds) {
    if (processInstanceIds == null) {
      throw new ActivitiIllegalArgumentException("Set of process instance ids is null");
    }
    if (processInstanceIds.isEmpty()) {
      throw new ActivitiIllegalArgumentException("Set of process instance ids is empty");
    }
    this.processInstanceIds = processInstanceIds;
    return this;
  }

  public HistoricProcessInstanceQueryImpl processDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
    return this;
  }
  
  public HistoricProcessInstanceQuery processDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
    return this;
  }
  
  public HistoricProcessInstanceQuery processInstanceBusinessKey(String businessKey) {
    this.businessKey = businessKey;
    return this;
  }

  public HistoricProcessInstanceQuery finished() {
    this.finished = true;
    return this;
  }
  
  public HistoricProcessInstanceQuery unfinished() {
    this.unfinished = true;
    return this;
  }
  
  public HistoricProcessInstanceQuery startedBy(String userId) {
    this.startedBy = userId;
    return this;
  }
  
  public HistoricProcessInstanceQuery processDefinitionKeyNotIn(List<String> processDefinitionKeys) {
    this.processKeyNotIn = processDefinitionKeys;
    return this;
  }
  
  public HistoricProcessInstanceQuery startedAfter(Date date) {
    startedAfter = date;
    return this;
  }
  
  public HistoricProcessInstanceQuery startedBefore(Date date) {
    startedBefore = date;
    return this;
  }
  
  public HistoricProcessInstanceQuery finishedAfter(Date date) {
    finishedAfter = date;
    finished = true;
    return this;
  }
  
  public HistoricProcessInstanceQuery finishedBefore(Date date) {
    finishedBefore = date;
    finished = true;
    return this;
  }
  
  public HistoricProcessInstanceQuery superProcessInstanceId(String superProcessInstanceId) {
    this.superProcessInstanceId = superProcessInstanceId;
    return this;
  }
  
  public HistoricProcessInstanceQuery excludeSubprocesses(boolean excludeSubprocesses) {
    this.excludeSubprocesses = excludeSubprocesses;
    return this;
  }
  
  @Override
  public HistoricProcessInstanceQuery involvedUser(String userId) {
    this.involvedUser = userId;
    return this;
  }
  
  public HistoricProcessInstanceQuery includeProcessVariables() {
    this.includeProcessVariables = true;
    return this;
  }
  
  public HistoricProcessInstanceQuery processInstanceTenantId(String tenantId) {
  	if (tenantId == null) {
  		throw new ActivitiIllegalArgumentException("process instance tenant id is null");
  	}
  	this.tenantId = tenantId;
  	return this;
  }
  
  public HistoricProcessInstanceQuery processInstanceTenantIdLike(String tenantIdLike) {
  	if (tenantIdLike == null) {
  		throw new ActivitiIllegalArgumentException("process instance tenant id is null");
  	}
  	this.tenantIdLike = tenantIdLike;
  	return this;
  }
  
  public HistoricProcessInstanceQuery processInstanceWithoutTenantId() {
  	this.withoutTenantId = true;
  	return this;
  }
  
  public HistoricProcessInstanceQuery orderByProcessInstanceBusinessKey() {
    return orderBy(HistoricProcessInstanceQueryProperty.BUSINESS_KEY);
  }
  
  public HistoricProcessInstanceQuery orderByProcessInstanceDuration() {
    return orderBy(HistoricProcessInstanceQueryProperty.DURATION);
  }
  
  public HistoricProcessInstanceQuery orderByProcessInstanceStartTime() {
    return orderBy(HistoricProcessInstanceQueryProperty.START_TIME);
  }
  
  public HistoricProcessInstanceQuery orderByProcessInstanceEndTime() {
    return orderBy(HistoricProcessInstanceQueryProperty.END_TIME);
  }
  
  public HistoricProcessInstanceQuery orderByProcessDefinitionId() {
    return orderBy(HistoricProcessInstanceQueryProperty.PROCESS_DEFINITION_ID);
  }
  
  public HistoricProcessInstanceQuery orderByProcessInstanceId() {
    return orderBy(HistoricProcessInstanceQueryProperty.PROCESS_INSTANCE_ID_);
  }
  
  public HistoricProcessInstanceQuery orderByTenantId() {
  	return orderBy(HistoricProcessInstanceQueryProperty.TENANT_ID);
  }
  
  public String getMssqlOrDB2OrderBy() {
    String specialOrderBy = super.getOrderBy();
    if (specialOrderBy != null && specialOrderBy.length() > 0) {
      specialOrderBy = specialOrderBy.replace("RES.", "TEMPRES_");
    }
    return specialOrderBy;
  }
  
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    ensureVariablesInitialized();
    return commandContext
      .getHistoricProcessInstanceEntityManager()
      .findHistoricProcessInstanceCountByQueryCriteria(this);
  }

  public List<HistoricProcessInstance> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    ensureVariablesInitialized();
    if (includeProcessVariables) {
      return commandContext
          .getHistoricProcessInstanceEntityManager()
          .findHistoricProcessInstancesAndVariablesByQueryCriteria(this);
    } else {
      return commandContext
          .getHistoricProcessInstanceEntityManager()
          .findHistoricProcessInstancesByQueryCriteria(this);
    }
  }
  
  public String getBusinessKey() {
    return businessKey;
  }
  public boolean isOpen() {
    return unfinished;
  }
  public String getProcessDefinitionId() {
    return processDefinitionId;
  }
  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }
  public String getProcessDefinitionIdLike() {
    return processDefinitionKey + ":%:%";
  }
  public String getProcessInstanceId() {
    return processInstanceId;
  }
  public Set<String> getProcessInstanceIds() {
    return processInstanceIds;
  }
  public String getStartedBy() {
    return startedBy;
  }
  public String getSuperProcessInstanceId() {
    return superProcessInstanceId;
  }
  public boolean isExcludeSubprocesses() {
    return excludeSubprocesses;
  }
  public List<String> getProcessKeyNotIn() {
    return processKeyNotIn;
  }
  public Date getStartedAfter() {
    return startedAfter;
  }
  public Date getStartedBefore() {
    return startedBefore;
  }
  public Date getFinishedAfter() {
    return finishedAfter;
  }
  public Date getFinishedBefore() {
    return finishedBefore;
  }
  public String getInvolvedUser() {
    return involvedUser;
  }
  
  // below is deprecated and to be removed in 5.12
  
  protected Date startDateBy;
  protected Date startDateOn;
  protected Date finishDateBy;
  protected Date finishDateOn;
  protected Date startDateOnBegin;
  protected Date startDateOnEnd;
  protected Date finishDateOnBegin;
  protected Date finishDateOnEnd;

  @Deprecated
  public HistoricProcessInstanceQuery startDateBy(Date date) {
    this.startDateBy = this.calculateMidnight(date);;
    return this;
  }

  @Deprecated
  public HistoricProcessInstanceQuery startDateOn(Date date) {
    this.startDateOn = date;
    this.startDateOnBegin = this.calculateMidnight(date);
    this.startDateOnEnd = this.calculateBeforeMidnight(date);
    return this;
  }

  @Deprecated
  public HistoricProcessInstanceQuery finishDateBy(Date date) {
    this.finishDateBy = this.calculateBeforeMidnight(date);
    return this;
  }

  @Deprecated
  public HistoricProcessInstanceQuery finishDateOn(Date date) {
    this.finishDateOn = date;
    this.finishDateOnBegin = this.calculateMidnight(date);
    this.finishDateOnEnd = this.calculateBeforeMidnight(date);
    return this;
  }
  
  @Deprecated
  private Date calculateBeforeMidnight(Date date){
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    cal.add(Calendar.DAY_OF_MONTH, 1);
    cal.add(Calendar.SECOND, -1);   
    return cal.getTime();
  }
  
  @Deprecated
  private Date calculateMidnight(Date date){
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    cal.set(Calendar.MILLISECOND, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.HOUR, 0);    
    return cal.getTime();
  }
}
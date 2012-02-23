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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.variable.VariableTypes;
import org.apache.commons.lang.time.DateUtils;


/**
 * @author Tom Baeyens
 * @author Falko Menge
 */
public class HistoricProcessInstanceQueryImpl extends AbstractQuery<HistoricProcessInstanceQuery, HistoricProcessInstance> implements HistoricProcessInstanceQuery {

  private static final long serialVersionUID = 1L;
  protected String processInstanceId;
  protected String processDefinitionId;
  protected String businessKey;
  protected boolean finished = false;
  protected boolean unfinished = false;
  protected String startedBy;
  protected String superProcessInstanceId;
  protected String processDefinitionKey;
  protected Set<String> processInstanceIds;
  protected Date startDateBy;
  protected Date startDateOn;
  protected Date finishDateBy;
  protected Date finishDateOn;
  protected Date startDateOnBegin;
  protected Date startDateOnEnd;
  protected Date finishDateOnBegin;
  protected Date finishDateOnEnd;
  protected List<QueryVariableValue> variables = new ArrayList<QueryVariableValue>();
  
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
      throw new ActivitiException("Set of process instance ids is null");
    }
    if (processInstanceIds.isEmpty()) {
      throw new ActivitiException("Set of process instance ids is empty");
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
  
  public HistoricProcessInstanceQuery superProcessInstanceId(String superProcessInstanceId) {
	 this.superProcessInstanceId = superProcessInstanceId;
	 return this;
  }
  
	public HistoricProcessInstanceQuery startDateBy(Date date) {
		this.startDateBy = this.calculateMidnight(date);;
		return this;
	}

	public HistoricProcessInstanceQuery startDateOn(Date date) {
		this.startDateOn = date;
		this.startDateOnBegin = this.calculateMidnight(date);
		this.startDateOnEnd = this.calculateBeforeMidnight(date);
		return this;
	}

	public HistoricProcessInstanceQuery finishDateBy(Date date) {
		this.finishDateBy = this.calculateBeforeMidnight(date);
		return this;
	}

	public HistoricProcessInstanceQuery finishDateOn(Date date) {
		this.finishDateOn = date;
		this.finishDateOnBegin = this.calculateMidnight(date);
		this.finishDateOnEnd = this.calculateBeforeMidnight(date);
		return this;
	}
	
	private Date calculateBeforeMidnight(Date date){
		Date calc = DateUtils.truncate(date, Calendar.DATE);
		calc = DateUtils.addDays(calc, 1);
		
		return DateUtils.addSeconds(calc, -1);
	}
	
	private Date calculateMidnight(Date date){
		return DateUtils.truncate(date, Calendar.DATE);
	}
	
	/* public HistoricProcessInstanceQuery processVariableEquals(String variableName, Object variableValue) {
		variables.add(new QueryVariableValue(variableName, variableValue, QueryOperator.EQUALS));
		return this;
	} */
	
	protected void ensureVariablesInitialized() {    
	  VariableTypes types = Context.getProcessEngineConfiguration().getVariableTypes();
	  for(QueryVariableValue var : variables) {
	    var.initialize(types);
	  }
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
  
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    ensureVariablesInitialized();
    return commandContext
      .getHistoricProcessInstanceManager()
      .findHistoricProcessInstanceCountByQueryCriteria(this);
  }

  public List<HistoricProcessInstance> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    ensureVariablesInitialized();
    return commandContext
      .getHistoricProcessInstanceManager()
      .findHistoricProcessInstancesByQueryCriteria(this, page);
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
  public void setSuperProcessInstanceId(String superProcessInstanceId) {
	this.superProcessInstanceId = superProcessInstanceId;
  }
  
}
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

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.SuspensionState;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 * @author Frederik Heremans
 * @author Falko Menge
 * @author Daniel Meyer
 */
public class ProcessInstanceQueryImpl extends AbstractVariableQueryImpl<ProcessInstanceQuery, ProcessInstance> implements ProcessInstanceQuery, Serializable {

  private static final long serialVersionUID = 1L;
  protected String executionId;
  protected String businessKey;
  protected boolean includeChildExecutionsWithBusinessKeyQuery;
  protected String processDefinitionId;
  protected String processDefinitionName;
  protected Set<String> processInstanceIds; 
  protected String processDefinitionKey;
  protected String superProcessInstanceId;
  protected String subProcessInstanceId;
  protected boolean excludeSubprocesses;
  protected String involvedUser;
  protected SuspensionState suspensionState;
  protected boolean includeProcessVariables;
  
  protected String tenantId;
  protected String tenantIdLike;
  protected boolean withoutTenantId;
  
  // Unused, see dynamic query
  protected String activityId;
  protected List<EventSubscriptionQueryValue> eventSubscriptions;
  
  public ProcessInstanceQueryImpl() {
  }
  
  public ProcessInstanceQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }
  
  public ProcessInstanceQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public ProcessInstanceQueryImpl processInstanceId(String processInstanceId) {
    if (processInstanceId == null) {
      throw new ActivitiIllegalArgumentException("Process instance id is null");
    }
    this.executionId = processInstanceId;
    return this;
  }
  
  public ProcessInstanceQuery processInstanceIds(Set<String> processInstanceIds) {
    if (processInstanceIds == null) {
      throw new ActivitiIllegalArgumentException("Set of process instance ids is null");
    }
    if (processInstanceIds.isEmpty()) {
      throw new ActivitiIllegalArgumentException("Set of process instance ids is empty");
    }
    this.processInstanceIds = processInstanceIds;
    return this;
  }

  public ProcessInstanceQuery processInstanceBusinessKey(String businessKey) {
    if (businessKey == null) {
      throw new ActivitiIllegalArgumentException("Business key is null");
    }
    this.businessKey = businessKey;
    return this;
  }
  
  public ProcessInstanceQuery processInstanceBusinessKey(String businessKey, String processDefinitionKey) {
    if (businessKey == null) {
      throw new ActivitiIllegalArgumentException("Business key is null");
    }
    this.businessKey = businessKey;
    this.processDefinitionKey = processDefinitionKey;
    return this;
  }
  
  public ProcessInstanceQuery processInstanceTenantId(String tenantId) {
  	if (tenantId == null) {
  		throw new ActivitiIllegalArgumentException("process instance tenant id is null");
  	}
  	this.tenantId = tenantId;
  	return this;
  }
  
  public ProcessInstanceQuery processInstanceTenantIdLike(String tenantIdLike) {
  	if (tenantIdLike == null) {
  		throw new ActivitiIllegalArgumentException("process instance tenant id is null");
  	}
  	this.tenantIdLike = tenantIdLike;
  	return this;
  }
  
  public ProcessInstanceQuery processInstanceWithoutTenantId() {
  	this.withoutTenantId = true;
  	return this;
  }

  @Override
  public ProcessInstanceQuery processDefinitionName(String processDefinitionName) {
    if (processDefinitionName == null) {
	  throw new ActivitiIllegalArgumentException("Process definition name is null");
    }
    this.processDefinitionName = processDefinitionName;
    return this;
  }

  public ProcessInstanceQueryImpl processDefinitionId(String processDefinitionId) {
    if (processDefinitionId == null) {
      throw new ActivitiIllegalArgumentException("Process definition id is null");
    }
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  public ProcessInstanceQueryImpl processDefinitionKey(String processDefinitionKey) {
    if (processDefinitionKey == null) {
      throw new ActivitiIllegalArgumentException("Process definition key is null");
    }
    this.processDefinitionKey = processDefinitionKey;
    return this;
  }
  
  public ProcessInstanceQuery superProcessInstanceId(String superProcessInstanceId) {
    this.superProcessInstanceId = superProcessInstanceId;
    return this;
  }
  
  public ProcessInstanceQuery subProcessInstanceId(String subProcessInstanceId) {
    this.subProcessInstanceId = subProcessInstanceId;
    return this;
  }
  
  public ProcessInstanceQuery excludeSubprocesses(boolean excludeSubprocesses) {
    this.excludeSubprocesses = excludeSubprocesses;
    return this;
  }
  
  public ProcessInstanceQuery involvedUser(String involvedUser) {
    if (involvedUser == null) {
      throw new ActivitiIllegalArgumentException("Involved user is null");
    }
    this.involvedUser = involvedUser;
    return this;
  }

  public ProcessInstanceQuery orderByProcessInstanceId() {
    this.orderProperty = ProcessInstanceQueryProperty.PROCESS_INSTANCE_ID;
    return this;
  }
  
  public ProcessInstanceQuery orderByProcessDefinitionId() {
    this.orderProperty = ProcessInstanceQueryProperty.PROCESS_DEFINITION_ID;
    return this;
  }
  
  public ProcessInstanceQuery orderByProcessDefinitionKey() {
    this.orderProperty = ProcessInstanceQueryProperty.PROCESS_DEFINITION_KEY;
    return this;
  }
  
  public ProcessInstanceQuery orderByTenantId() {
    this.orderProperty = ProcessInstanceQueryProperty.TENANT_ID;
    return this;
  }
  
  public ProcessInstanceQuery active() {
    this.suspensionState = SuspensionState.ACTIVE;
    return this;
  }
  
  public ProcessInstanceQuery suspended() {
    this.suspensionState = SuspensionState.SUSPENDED;
    return this;
  }
  
  public ProcessInstanceQuery includeProcessVariables() {
    this.includeProcessVariables = true;
    return this;
  }
  
  public String getMssqlOrDB2OrderBy() {
    String specialOrderBy = super.getOrderBy();
    if (specialOrderBy != null && specialOrderBy.length() > 0) {
      specialOrderBy = specialOrderBy.replace("RES.", "TEMPRES_");
      specialOrderBy = specialOrderBy.replace("ProcessDefinitionKey", "TEMPP_KEY_");
      specialOrderBy = specialOrderBy.replace("ProcessDefinitionId", "TEMPP_ID_");
    }
    return specialOrderBy;
  }
  
  //results /////////////////////////////////////////////////////////////////
  
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    ensureVariablesInitialized();
    return commandContext
      .getExecutionEntityManager()
      .findProcessInstanceCountByQueryCriteria(this);
  }

  public List<ProcessInstance> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    ensureVariablesInitialized();
    if (includeProcessVariables) {
      return commandContext
          .getExecutionEntityManager()
          .findProcessInstanceAndVariablesByQueryCriteria(this);
    } else {
      return commandContext
          .getExecutionEntityManager()
          .findProcessInstanceByQueryCriteria(this);
    }
  }
  
  //getters /////////////////////////////////////////////////////////////////
  
  public boolean getOnlyProcessInstances() {
    return true; // See dynamic query in runtime.mapping.xml
  }
  public String getProcessInstanceId() {
    return executionId;
  }
  public Set<String> getProcessInstanceIds() {
    return processInstanceIds;
  }
  public String getBusinessKey() {
    return businessKey;
  }
  public boolean isIncludeChildExecutionsWithBusinessKeyQuery() {
    return includeChildExecutionsWithBusinessKeyQuery;
  }
  public String getProcessDefinitionId() {
    return processDefinitionId;
  }
  public String getProcessDefinitionName() {
    return processDefinitionName;
  }
  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }
  public String getActivityId() {
    return null; // Unused, see dynamic query
  }
  public String getSuperProcessInstanceId() {
    return superProcessInstanceId;
  }
  public String getSubProcessInstanceId() {
    return subProcessInstanceId;
  }
  public boolean isExcludeSubprocesses() {
    return excludeSubprocesses;
  }
  public String getInvolvedUser() {
    return involvedUser;
  }
  public SuspensionState getSuspensionState() {
    return suspensionState;
  }  
  public void setSuspensionState(SuspensionState suspensionState) {
    this.suspensionState = suspensionState;
  }  
  public List<EventSubscriptionQueryValue> getEventSubscriptions() {
    return eventSubscriptions;
  }
  public void setEventSubscriptions(List<EventSubscriptionQueryValue> eventSubscriptions) {
    this.eventSubscriptions = eventSubscriptions;
  }
  public String getTenantId() {
		return tenantId;
	}
	public String getTenantIdLike() {
		return tenantIdLike;
	}
	public boolean isWithoutTenantId() {
		return withoutTenantId;
	}

	/**
   * Method needed for ibatis because of re-use of query-xml for executions. ExecutionQuery contains
   * a parentId property.
   */
  public String getParentId() {
    return null;
  }
}

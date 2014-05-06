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

package org.activiti.rest.service.api.runtime.process;

import java.util.List;

import org.activiti.rest.common.api.PaginateRequest;
import org.activiti.rest.service.api.engine.variable.QueryVariable;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;


/**
 * @author Frederik Heremans
 */
public class ExecutionQueryRequest extends PaginateRequest {
  private String id;
  private String activityId;
  private String parentId;
  private String processInstanceId;
  private String processBusinessKey;
  private String processDefinitionId;
  private String processDefinitionKey;
  private String signalEventSubscriptionName;
  private String messageEventSubscriptionName;
  private List<QueryVariable> variables;
  private List<QueryVariable> processInstanceVariables;
  private String tenantId;
  private String tenantIdLike;
  private Boolean withoutTenantId;
  
  @JsonTypeInfo(use=Id.CLASS, defaultImpl=QueryVariable.class)  
  public List<QueryVariable> getVariables() {
    return variables;
  }
  
  public void setVariables(List<QueryVariable> variables) {
    this.variables = variables;
  }
  
  public List<QueryVariable> getProcessInstanceVariables() {
    return processInstanceVariables;
  }
  @JsonTypeInfo(use=Id.CLASS, defaultImpl=QueryVariable.class)  
  public void setProcessInstanceVariables(List<QueryVariable> processInstanceVariables) {
    this.processInstanceVariables = processInstanceVariables;
  }

  
  public String getId() {
    return id;
  }

  
  public void setId(String id) {
    this.id = id;
  }

  
  public String getProcessInstanceId() {
    return processInstanceId;
  }

  
  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  
  public String getProcessBusinessKey() {
    return processBusinessKey;
  }

  
  public void setProcessBusinessKey(String processBusinessKey) {
    this.processBusinessKey = processBusinessKey;
  }

  
  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  
  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  
  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  
  public void setProcessDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }

  
  public String getSignalEventSubscriptionName() {
    return signalEventSubscriptionName;
  }

  
  public void setSignalEventSubscriptionName(String signalEventSubscriptionName) {
    this.signalEventSubscriptionName = signalEventSubscriptionName;
  }

  
  public String getMessageEventSubscriptionName() {
    return messageEventSubscriptionName;
  }

  
  public void setMessageEventSubscriptionName(String messageEventSubscriptionName) {
    this.messageEventSubscriptionName = messageEventSubscriptionName;
  }
  
  
  public String getActivityId() {
    return activityId;
  }
  
  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }
  
  public String getParentId() {
    return parentId;
  }
  
  public void setParentId(String parentId) {
    this.parentId = parentId;
  }

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getTenantIdLike() {
		return tenantIdLike;
	}

	public void setTenantIdLike(String tenantIdLike) {
		this.tenantIdLike = tenantIdLike;
	}

	public Boolean getWithoutTenantId() {
		return withoutTenantId;
	}

	public void setWithoutTenantId(Boolean withoutTenantId) {
		this.withoutTenantId = withoutTenantId;
	}
  
}

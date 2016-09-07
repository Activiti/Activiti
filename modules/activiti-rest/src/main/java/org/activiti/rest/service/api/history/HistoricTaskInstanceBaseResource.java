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

package org.activiti.rest.service.api.history;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.impl.HistoricTaskInstanceQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.activiti.rest.common.api.DataResponse;
import org.activiti.rest.service.api.RestResponseFactory;
import org.activiti.rest.service.api.engine.variable.QueryVariable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Tijs Rademakers
 */
public class HistoricTaskInstanceBaseResource {

  private static Map<String, QueryProperty> allowedSortProperties = new HashMap<String, QueryProperty>();

  static {
    allowedSortProperties.put("deleteReason", HistoricTaskInstanceQueryProperty.DELETE_REASON);
    allowedSortProperties.put("duration", HistoricTaskInstanceQueryProperty.DURATION);
    allowedSortProperties.put("endTime", HistoricTaskInstanceQueryProperty.END);
    allowedSortProperties.put("executionId", HistoricTaskInstanceQueryProperty.EXECUTION_ID);
    allowedSortProperties.put("taskInstanceId", HistoricTaskInstanceQueryProperty.HISTORIC_TASK_INSTANCE_ID);
    allowedSortProperties.put("processDefinitionId", HistoricTaskInstanceQueryProperty.PROCESS_DEFINITION_ID);
    allowedSortProperties.put("processInstanceId", HistoricTaskInstanceQueryProperty.PROCESS_INSTANCE_ID);
    allowedSortProperties.put("start", HistoricTaskInstanceQueryProperty.START);
    allowedSortProperties.put("assignee", HistoricTaskInstanceQueryProperty.TASK_ASSIGNEE);
    allowedSortProperties.put("taskDefinitionKey", HistoricTaskInstanceQueryProperty.TASK_DEFINITION_KEY);
    allowedSortProperties.put("description", HistoricTaskInstanceQueryProperty.TASK_DESCRIPTION);
    allowedSortProperties.put("dueDate", HistoricTaskInstanceQueryProperty.TASK_DUE_DATE);
    allowedSortProperties.put("name", HistoricTaskInstanceQueryProperty.TASK_NAME);
    allowedSortProperties.put("owner", HistoricTaskInstanceQueryProperty.TASK_OWNER);
    allowedSortProperties.put("priority", HistoricTaskInstanceQueryProperty.TASK_PRIORITY);
    allowedSortProperties.put("tenantId", HistoricTaskInstanceQueryProperty.TENANT_ID_);
    
    // Duplicate usage of HistoricTaskInstanceQueryProperty.START, to keep naming consistent and keep backwards-compatibility
    allowedSortProperties.put("startTime", HistoricTaskInstanceQueryProperty.START);
  }
  
  @Autowired
  protected RestResponseFactory restResponseFactory;
  
  @Autowired
  protected HistoryService historyService;

  protected DataResponse getQueryResponse(HistoricTaskInstanceQueryRequest queryRequest, Map<String,String> allRequestParams, String serverRootUrl) {
    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();

    // Populate query based on request
    if (queryRequest.getTaskId() != null) {
      query.taskId(queryRequest.getTaskId());
    }
    if (queryRequest.getProcessInstanceId() != null) {
      query.processInstanceId(queryRequest.getProcessInstanceId());
    }
    if (queryRequest.getProcessBusinessKey() != null) {
      query.processInstanceBusinessKey(queryRequest.getProcessBusinessKey());
    }
    if (queryRequest.getProcessBusinessKeyLike() != null) {
      query.processInstanceBusinessKeyLike(queryRequest.getProcessBusinessKeyLike());
    }
    if (queryRequest.getProcessDefinitionKey() != null) {
      query.processDefinitionKey(queryRequest.getProcessDefinitionKey());
    }
    if (queryRequest.getProcessDefinitionKeyLike() != null) {
    	query.processDefinitionKeyLike(queryRequest.getProcessDefinitionKeyLike());
    }
    if (queryRequest.getProcessDefinitionId() != null) {
      query.processDefinitionId(queryRequest.getProcessDefinitionId());
    }
    if (queryRequest.getProcessDefinitionName() != null) {
      query.processDefinitionName(queryRequest.getProcessDefinitionName());
    }
    if (queryRequest.getProcessDefinitionNameLike() != null) {
    	query.processDefinitionNameLike(queryRequest.getProcessDefinitionNameLike());
    }
    if (queryRequest.getExecutionId() != null) {
      query.executionId(queryRequest.getExecutionId());
    }
    if (queryRequest.getTaskName() != null) {
      query.taskName(queryRequest.getTaskName());
    }
    if (queryRequest.getTaskNameLike() != null) {
      query.taskNameLike(queryRequest.getTaskNameLike());
    }
    if (queryRequest.getTaskDescription() != null) {
      query.taskDescription(queryRequest.getTaskDescription());
    }
    if (queryRequest.getTaskDescriptionLike() != null) {
      query.taskDescriptionLike(queryRequest.getTaskDescriptionLike());
    }
    if (queryRequest.getTaskDefinitionKey() != null) {
      query.taskDefinitionKey(queryRequest.getTaskDefinitionKey());
    }
    if (queryRequest.getTaskDefinitionKeyLike() != null) {
    	query.taskDefinitionKeyLike(queryRequest.getTaskDefinitionKeyLike());
    }
    if (queryRequest.getTaskCategory() != null) {
      query.taskCategory(queryRequest.getTaskCategory());
    }
    if (queryRequest.getTaskDeleteReason() != null) {
      query.taskDeleteReason(queryRequest.getTaskDeleteReason());
    }
    if (queryRequest.getTaskDeleteReasonLike() != null) {
      query.taskDeleteReasonLike(queryRequest.getTaskDeleteReasonLike());
    }
    if (queryRequest.getTaskAssignee() != null) {
      query.taskAssignee(queryRequest.getTaskAssignee());
    }
    if (queryRequest.getTaskAssigneeLike() != null) {
      query.taskAssigneeLike(queryRequest.getTaskAssigneeLike());
    }
    if (queryRequest.getTaskOwner() != null) {
      query.taskOwner(queryRequest.getTaskOwner());
    }
    if (queryRequest.getTaskOwnerLike() != null) {
      query.taskOwnerLike(queryRequest.getTaskOwnerLike());
    }
    if (queryRequest.getTaskInvolvedUser() != null) {
      query.taskInvolvedUser(queryRequest.getTaskInvolvedUser());
    }
    if (queryRequest.getTaskPriority() != null) {
      query.taskPriority(queryRequest.getTaskPriority());
    }
    if (queryRequest.getTaskMinPriority() != null) {
    	query.taskMinPriority(queryRequest.getTaskMinPriority());
    }
    if (queryRequest.getTaskMaxPriority() != null) {
    	query.taskMaxPriority(queryRequest.getTaskMaxPriority());
    }
    if (queryRequest.getTaskPriority() != null) {
    	query.taskPriority(queryRequest.getTaskPriority());
    }
    if (queryRequest.getFinished() != null) {
      if (queryRequest.getFinished()) {
        query.finished();
      } else {
        query.unfinished();
      }
    }
    if (queryRequest.getProcessFinished() != null) {
      if (queryRequest.getProcessFinished()) {
        query.processFinished();
      } else {
        query.processUnfinished();
      }
    }
    if (queryRequest.getParentTaskId() != null) {
      query.taskParentTaskId(queryRequest.getParentTaskId());
    }
    if (queryRequest.getDueDate() != null) {
      query.taskDueDate(queryRequest.getDueDate());
    }
    if (queryRequest.getDueDateAfter() != null) {
      query.taskDueAfter(queryRequest.getDueDateAfter());
    }
    if (queryRequest.getDueDateBefore() != null) {
      query.taskDueBefore(queryRequest.getDueDateBefore());
    }
    if (queryRequest.getWithoutDueDate() != null && queryRequest.getWithoutDueDate()) {
      query.withoutTaskDueDate();
    }
    if (queryRequest.getTaskCreatedOn() != null) {
      query.taskCreatedOn(queryRequest.getTaskCreatedOn());
    }
    if (queryRequest.getTaskCreatedBefore() != null) {
      query.taskCreatedBefore(queryRequest.getTaskCreatedBefore());
    }
    if (queryRequest.getTaskCreatedAfter() != null) {
      query.taskCreatedAfter(queryRequest.getTaskCreatedAfter());
    }
    if (queryRequest.getTaskCreatedOn() != null) {
      query.taskCreatedOn(queryRequest.getTaskCreatedOn());
    }
    if (queryRequest.getTaskCreatedBefore() != null) {
      query.taskCreatedBefore(queryRequest.getTaskCreatedBefore());
    }
    if (queryRequest.getTaskCreatedAfter() != null) {
      query.taskCreatedAfter(queryRequest.getTaskCreatedAfter());
    }
    if (queryRequest.getTaskCompletedOn() != null) {
      query.taskCompletedOn(queryRequest.getTaskCompletedOn());
    }
    if (queryRequest.getTaskCompletedBefore() != null) {
      query.taskCompletedBefore(queryRequest.getTaskCompletedBefore());
    }
    if (queryRequest.getTaskCompletedAfter() != null) {
      query.taskCompletedAfter(queryRequest.getTaskCompletedAfter());
    }
    if (queryRequest.getIncludeTaskLocalVariables() != null) {
      if (queryRequest.getIncludeTaskLocalVariables()) {
        query.includeTaskLocalVariables();
      }
    }
    if (queryRequest.getIncludeProcessVariables() != null) {
      if (queryRequest.getIncludeProcessVariables()) {
        query.includeProcessVariables();
      }
    }
    if (queryRequest.getTaskVariables() != null) {
      addTaskVariables(query, queryRequest.getTaskVariables());
    }
    if (queryRequest.getProcessVariables() != null) {
      addProcessVariables(query, queryRequest.getProcessVariables());
    }
    
    if(queryRequest.getTenantId() != null) {
    	query.taskTenantId(queryRequest.getTenantId());
    }
    
    if(queryRequest.getTenantIdLike() != null) {
    	query.taskTenantIdLike(queryRequest.getTenantIdLike());
    }
    
    if(Boolean.TRUE.equals(queryRequest.getWithoutTenantId())) {
    	query.taskWithoutTenantId();
    }

    if(queryRequest.getTaskCandidateGroup() != null) {
      query.taskCandidateGroup(queryRequest.getTaskCandidateGroup());
    }
    
    return new HistoricTaskInstancePaginateList(restResponseFactory, serverRootUrl).paginateList(
        allRequestParams, queryRequest, query, "taskInstanceId", allowedSortProperties);
  }

  protected void addTaskVariables(HistoricTaskInstanceQuery taskInstanceQuery, List<QueryVariable> variables) {
    for (QueryVariable variable : variables) {
      if (variable.getVariableOperation() == null) {
        throw new ActivitiIllegalArgumentException("Variable operation is missing for variable: " + variable.getName());
      }
      if (variable.getValue() == null) {
        throw new ActivitiIllegalArgumentException("Variable value is missing for variable: " + variable.getName());
      }

      boolean nameLess = variable.getName() == null;

      Object actualValue = restResponseFactory.getVariableValue(variable);

      // A value-only query is only possible using equals-operator
      if (nameLess) {
        throw new ActivitiIllegalArgumentException("Value-only query (without a variable-name) is not supported.");
      }

      switch(variable.getVariableOperation()) {
      
      case EQUALS:
        if(nameLess) {
        	taskInstanceQuery.taskVariableValueEquals(actualValue);
        } else {
        	taskInstanceQuery.taskVariableValueEquals(variable.getName(), actualValue);
        }
        break;
        
      case EQUALS_IGNORE_CASE:
        if(actualValue instanceof String) {
        	taskInstanceQuery.taskVariableValueEqualsIgnoreCase(variable.getName(), (String)actualValue);
        } else {
          throw new ActivitiIllegalArgumentException("Only string variable values are supported when ignoring casing, but was: " + actualValue.getClass().getName());
        }
        break;
        
      case NOT_EQUALS:
      	taskInstanceQuery.taskVariableValueNotEquals(variable.getName(), actualValue);
        break;
        
      case NOT_EQUALS_IGNORE_CASE:
        if(actualValue instanceof String) {
        	taskInstanceQuery.taskVariableValueNotEqualsIgnoreCase(variable.getName(), (String)actualValue);
        } else {
          throw new ActivitiIllegalArgumentException("Only string variable values are supported when ignoring casing, but was: " + actualValue.getClass().getName());
        }
        break;
        
      case GREATER_THAN:
      	taskInstanceQuery.taskVariableValueGreaterThan(variable.getName(), actualValue);
      	break;
      	
      case GREATER_THAN_OR_EQUALS:
      	taskInstanceQuery.taskVariableValueGreaterThanOrEqual(variable.getName(), actualValue);
      	break;
      	
      case LESS_THAN:
      	taskInstanceQuery.taskVariableValueLessThan(variable.getName(), actualValue);
      	break;
      	
      case LESS_THAN_OR_EQUALS:
      	taskInstanceQuery.taskVariableValueLessThanOrEqual(variable.getName(), actualValue);
      	break;
      	
      case LIKE:
      	if(actualValue instanceof String) {
      		taskInstanceQuery.taskVariableValueLike(variable.getName(), (String) actualValue);
      	} else {
      		throw new ActivitiIllegalArgumentException("Only string variable values are supported using like, but was: " + actualValue.getClass().getName());
      	}
      	break;
      default:
        throw new ActivitiIllegalArgumentException("Unsupported variable query operation: " + variable.getVariableOperation());
      }
    }
  }
  
  protected void addProcessVariables(HistoricTaskInstanceQuery taskInstanceQuery, List<QueryVariable> variables) {
    for (QueryVariable variable : variables) {
      if (variable.getVariableOperation() == null) {
        throw new ActivitiIllegalArgumentException("Variable operation is missing for variable: " + variable.getName());
      }
      if (variable.getValue() == null) {
        throw new ActivitiIllegalArgumentException("Variable value is missing for variable: " + variable.getName());
      }

      boolean nameLess = variable.getName() == null;

      Object actualValue = restResponseFactory.getVariableValue(variable);

      // A value-only query is only possible using equals-operator
      if (nameLess) {
        throw new ActivitiIllegalArgumentException("Value-only query (without a variable-name) is not supported.");
      }

      switch(variable.getVariableOperation()) {
      
      case EQUALS:
        taskInstanceQuery.processVariableValueEquals(variable.getName(), actualValue);
        break;
        
      case EQUALS_IGNORE_CASE:
        if(actualValue instanceof String) {
        	taskInstanceQuery.processVariableValueEqualsIgnoreCase(variable.getName(), (String)actualValue);
        } else {
          throw new ActivitiIllegalArgumentException("Only string variable values are supported when ignoring casing, but was: " + actualValue.getClass().getName());
        }
        break;
        
      case NOT_EQUALS:
      	taskInstanceQuery.processVariableValueNotEquals(variable.getName(), actualValue);
        break;
        
      case NOT_EQUALS_IGNORE_CASE:
        if(actualValue instanceof String) {
        	taskInstanceQuery.processVariableValueNotEqualsIgnoreCase(variable.getName(), (String)actualValue);
        } else {
          throw new ActivitiIllegalArgumentException("Only string variable values are supported when ignoring casing, but was: " + actualValue.getClass().getName());
        }
        break;
        
      case GREATER_THAN:
      	taskInstanceQuery.processVariableValueGreaterThan(variable.getName(), actualValue);
      	break;
      	
      case GREATER_THAN_OR_EQUALS:
      	taskInstanceQuery.processVariableValueGreaterThanOrEqual(variable.getName(), actualValue);
      	break;
      	
      case LESS_THAN:
      	taskInstanceQuery.processVariableValueLessThan(variable.getName(), actualValue);
      	break;
      	
      case LESS_THAN_OR_EQUALS:
      	taskInstanceQuery.processVariableValueLessThanOrEqual(variable.getName(), actualValue);
      	break;
      	
      case LIKE:
      	if(actualValue instanceof String) {
      		taskInstanceQuery.processVariableValueLike(variable.getName(), (String) actualValue);
      	} else {
      		throw new ActivitiIllegalArgumentException("Only string variable values are supported using like, but was: " + actualValue.getClass().getName());
      	}
      	break;
      default:
        throw new ActivitiIllegalArgumentException("Unsupported variable query operation: " + variable.getVariableOperation());
      }
    }
  }
}

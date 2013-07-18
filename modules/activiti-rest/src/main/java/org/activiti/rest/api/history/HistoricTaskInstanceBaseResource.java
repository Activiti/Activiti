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

package org.activiti.rest.api.history;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.impl.HistoricTaskInstanceQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.DataResponse;
import org.activiti.rest.api.RestResponseFactory;
import org.activiti.rest.api.SecuredResource;
import org.activiti.rest.api.engine.variable.QueryVariable;
import org.activiti.rest.application.ActivitiRestServicesApplication;
import org.restlet.data.Form;

/**
 * @author Tijs Rademakers
 */
public class HistoricTaskInstanceBaseResource extends SecuredResource {

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
    allowedSortProperties.put("descriptipn", HistoricTaskInstanceQueryProperty.TASK_DESCRIPTION);
    allowedSortProperties.put("dueDate", HistoricTaskInstanceQueryProperty.TASK_DUE_DATE);
    allowedSortProperties.put("name", HistoricTaskInstanceQueryProperty.TASK_NAME);
    allowedSortProperties.put("owner", HistoricTaskInstanceQueryProperty.TASK_OWNER);
    allowedSortProperties.put("priority", HistoricTaskInstanceQueryProperty.TASK_PRIORITY);
  }

  protected DataResponse getQueryResponse(HistoricTaskInstanceQueryRequest queryRequest, Form urlQuery) {
    HistoricTaskInstanceQuery query = ActivitiUtil.getHistoryService().createHistoricTaskInstanceQuery();

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
    if (queryRequest.getProcessDefinitionKey() != null) {
      query.processDefinitionKey(queryRequest.getProcessDefinitionKey());
    }
    if (queryRequest.getProcessDefinitionId() != null) {
      query.processDefinitionId(queryRequest.getProcessDefinitionId());
    }
    if (queryRequest.getProcessDefinitionName() != null) {
      query.processDefinitionName(queryRequest.getProcessDefinitionName());
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
    if (queryRequest.getTaskCreatedOn() != null) {
      query.taskCreatedOn(queryRequest.getTaskCreatedOn());
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

    return new HistoricTaskInstancePaginateList(this).paginateList(urlQuery, query, "taskInstanceId", allowedSortProperties);
  }

  protected void addTaskVariables(HistoricTaskInstanceQuery taskInstanceQuery, List<QueryVariable> variables) {
    RestResponseFactory responseFactory = getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory();
    
    for (QueryVariable variable : variables) {
      if (variable.getVariableOperation() == null) {
        throw new ActivitiIllegalArgumentException("Variable operation is missing for variable: " + variable.getName());
      }
      if (variable.getValue() == null) {
        throw new ActivitiIllegalArgumentException("Variable value is missing for variable: " + variable.getName());
      }

      boolean nameLess = variable.getName() == null;

      Object actualValue = responseFactory.getVariableValue(variable);

      // A value-only query is only possible using equals-operator
      if (nameLess) {
        throw new ActivitiIllegalArgumentException("Value-only query (without a variable-name) is not supported.");
      }

      switch (variable.getVariableOperation()) {

      case EQUALS:
        taskInstanceQuery.taskVariableValueEquals(variable.getName(), actualValue);
        break;

      default:
        throw new ActivitiIllegalArgumentException("Unsupported variable query operation: " + variable.getVariableOperation());
      }
    }
  }
  
  protected void addProcessVariables(HistoricTaskInstanceQuery taskInstanceQuery, List<QueryVariable> variables) {
    RestResponseFactory responseFactory = getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory();
    
    for (QueryVariable variable : variables) {
      if (variable.getVariableOperation() == null) {
        throw new ActivitiIllegalArgumentException("Variable operation is missing for variable: " + variable.getName());
      }
      if (variable.getValue() == null) {
        throw new ActivitiIllegalArgumentException("Variable value is missing for variable: " + variable.getName());
      }

      boolean nameLess = variable.getName() == null;

      Object actualValue = responseFactory.getVariableValue(variable);

      // A value-only query is only possible using equals-operator
      if (nameLess) {
        throw new ActivitiIllegalArgumentException("Value-only query (without a variable-name) is not supported.");
      }

      switch (variable.getVariableOperation()) {

      case EQUALS:
        taskInstanceQuery.processVariableValueEquals(variable.getName(), actualValue);
        break;

      default:
        throw new ActivitiIllegalArgumentException("Unsupported variable query operation: " + variable.getVariableOperation());
      }
    }
  }
}

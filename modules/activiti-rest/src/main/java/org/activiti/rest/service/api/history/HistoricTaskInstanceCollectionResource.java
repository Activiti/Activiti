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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.activiti.rest.common.api.DataResponse;
import org.activiti.rest.common.api.RequestUtil;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Tijs Rademakers
 */
@RestController
@Api(tags = { "History" }, description = "Manage History", authorizations = { @Authorization(value = "basicAuth") })
public class HistoricTaskInstanceCollectionResource extends HistoricTaskInstanceBaseResource {


  @ApiOperation(value = "Get historic task instances", tags = { "History" }, nickname = "listHistoricTaskInstances")
  @ApiImplicitParams({
    @ApiImplicitParam(name = "taskId", dataType = "string", value = "An id of the historic task instance.", paramType = "query"),
    @ApiImplicitParam(name = "processInstanceId", dataType = "string", value = "The process instance id of the historic task instance.", paramType = "query"),
    @ApiImplicitParam(name = "processDefinitionKey", dataType = "string", value = "The process definition key of the historic task instance.", paramType = "query"),
    @ApiImplicitParam(name = "processDefinitionKeyLike", dataType = "string", value = "The process definition key of the historic task instance, which matches the given value.", paramType = "query"),
    @ApiImplicitParam(name = "processDefinitionId", dataType = "string", value = "The process definition id of the historic task instance.", paramType = "query"),
    @ApiImplicitParam(name = "processDefinitionName", dataType = "string", value = "The process definition name of the historic task instance.", paramType = "query"),
    @ApiImplicitParam(name = "processDefinitionNameLike", dataType = "string", value = "The process definition name of the historic task instance, which matches the given value.", paramType = "query"),
    @ApiImplicitParam(name = "processBusinessKey", dataType = "string", value = "The process instance business key of the historic task instance.", paramType = "query"),
    @ApiImplicitParam(name = "processBusinessKeyLike", dataType = "string", value = "The process instance business key of the historic task instance that matches the given value.", paramType = "query"),
    @ApiImplicitParam(name = "executionId", dataType = "string", value = "The execution id of the historic task instance.", paramType = "query"),
    @ApiImplicitParam(name = "taskDefinitionKey", dataType = "string", value = "The task definition key for tasks part of a process", paramType = "query"),
    @ApiImplicitParam(name = "taskName", dataType = "string", value = "The task name of the historic task instance.", paramType = "query"),
    @ApiImplicitParam(name = "taskNameLike", dataType = "string", value = "The task name with like operator for the historic task instance.", paramType = "query"),
    @ApiImplicitParam(name = "taskDescription", dataType = "string", value = "The task description of the historic task instance.", paramType = "query"),
    @ApiImplicitParam(name = "taskDescriptionLike", dataType = "string", value = "The task description with like operator for the historic task instance.", paramType = "query"),
    @ApiImplicitParam(name = "taskCategory", dataType = "string", value = "Select tasks with the given category. Note that this is the task category, not the category of the process definition (namespace within the BPMN Xml).", paramType = "query"),
    @ApiImplicitParam(name = "taskDeleteReason", dataType = "string", value = "The task delete reason of the historic task instance.", paramType = "query"),
    @ApiImplicitParam(name = "taskDeleteReasonLike", dataType = "string", value = "The task delete reason with like operator for the historic task instance.", paramType = "query"),
    @ApiImplicitParam(name = "taskAssignee", dataType = "string", value = "The assignee of the historic task instance.", paramType = "query"),
    @ApiImplicitParam(name = "taskAssigneeLike", dataType = "string", value = "The assignee with like operator for the historic task instance.", paramType = "query"),
    @ApiImplicitParam(name = "taskOwner", dataType = "string", value = "The owner of the historic task instance.", paramType = "query"),
    @ApiImplicitParam(name = "taskOwnerLike", dataType = "string", value = "The owner with like operator for the historic task instance.", paramType = "query"),
    @ApiImplicitParam(name = "taskInvolvedUser", dataType = "string", value = "An involved user of the historic task instance", paramType = "query"),
    @ApiImplicitParam(name = "taskPriority", dataType = "string", value = "The priority of the historic task instance.", paramType = "query"),
    @ApiImplicitParam(name = "finished", dataType = "boolean", value = "Indication if the historic task instance is finished.", paramType = "query"),
    @ApiImplicitParam(name = "processFinished", dataType = "boolean", value = "Indication if the process instance of the historic task instance is finished.", paramType = "query"),
    @ApiImplicitParam(name = "parentTaskId", dataType = "string", value = "An optional parent task id of the historic task instance.", paramType = "query"),
    @ApiImplicitParam(name = "dueDate", dataType = "string", value = "Return only historic task instances that have a due date equal this date.", paramType = "query"),
    @ApiImplicitParam(name = "dueDateAfter", dataType = "string", value = "Return only historic task instances that have a due date after this date.", paramType = "query"),
    @ApiImplicitParam(name = "dueDateBefore", dataType = "string", value = "Return only historic task instances that have a due date before this date.", paramType = "query"),
    @ApiImplicitParam(name = "withoutDueDate", dataType = "boolean", value = "Return only historic task instances that have no due-date. When false is provided as value, this parameter is ignored.", paramType = "query"),
    @ApiImplicitParam(name = "taskCompletedOn", dataType = "string", value = "Return only historic task instances that have been completed on this date.", paramType = "query"),
    @ApiImplicitParam(name = "taskCompletedAfter", dataType = "string", value = "Return only historic task instances that have been completed after this date.", paramType = "query"),
    @ApiImplicitParam(name = "taskCompletedBefore", dataType = "string", value = "Return only historic task instances that have been completed before this date.", paramType = "query"),
    @ApiImplicitParam(name = "taskCreatedOn", dataType = "string", value = "Return only historic task instances that were created on this date.", paramType = "query"),
    @ApiImplicitParam(name = "taskCreatedBefore", dataType = "string", value = "Return only historic task instances that were created before this date.", paramType = "query"),
    @ApiImplicitParam(name = "taskCreatedAfter", dataType = "string", value = "Return only historic task instances that were created after this date.", paramType = "query"),
    @ApiImplicitParam(name = "includeTaskLocalVariables", dataType = "boolean", value = "An indication if the historic task instance local variables should be returned as well.", paramType = "query"),
    @ApiImplicitParam(name = "includeProcessVariables", dataType = "boolean", value = "An indication if the historic task instance global variables should be returned as well.", paramType = "query"),
    @ApiImplicitParam(name = "tenantId", dataType = "string", value = "Only return historic task instances with the given tenantId.", paramType = "query"),
    @ApiImplicitParam(name = "tenantIdLike", dataType = "string", value = "Only return historic task instances with a tenantId like the given value.", paramType = "query"),
    @ApiImplicitParam(name = "withoutTenantId", dataType = "boolean", value = "If true, only returns historic task instances without a tenantId set. If false, the withoutTenantId parameter is ignored.", paramType = "query"),

  })
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Indicates that historic task instances could be queried."),
      @ApiResponse(code = 404, message = "Indicates an parameter was passed in the wrong format. The status-message contains additional information.") })
  @RequestMapping(value = "/history/historic-task-instances", method = RequestMethod.GET, produces = "application/json")
  public DataResponse getHistoricProcessInstances(@ApiParam(hidden=true) @RequestParam Map<String, String> allRequestParams, HttpServletRequest request) {
    // Populate query based on request
    HistoricTaskInstanceQueryRequest queryRequest = new HistoricTaskInstanceQueryRequest();

    if (allRequestParams.get("taskId") != null) {
      queryRequest.setTaskId(allRequestParams.get("taskId"));
    }

    if (allRequestParams.get("processInstanceId") != null) {
      queryRequest.setProcessInstanceId(allRequestParams.get("processInstanceId"));
    }

    if (allRequestParams.get("processBusinessKey") != null) {
      queryRequest.setProcessBusinessKey(allRequestParams.get("processBusinessKey"));
    }

    if (allRequestParams.get("processBusinessKeyLike") != null) {
      queryRequest.setProcessBusinessKeyLike(allRequestParams.get("processBusinessKeyLike"));
    }


    if (allRequestParams.get("processDefinitionKey") != null) {
      queryRequest.setProcessDefinitionKey(allRequestParams.get("processDefinitionKey"));
    }

    if (allRequestParams.get("processDefinitionKeyLike") != null) {
      queryRequest.setProcessDefinitionKeyLike(allRequestParams.get("processDefinitionKeyLike"));
    }

    if (allRequestParams.get("processDefinitionId") != null) {
      queryRequest.setProcessDefinitionId(allRequestParams.get("processDefinitionId"));
    }

    if (allRequestParams.get("processDefinitionName") != null) {
      queryRequest.setProcessDefinitionName(allRequestParams.get("processDefinitionName"));
    }

    if (allRequestParams.get("processDefinitionNameLike") != null) {
      queryRequest.setProcessDefinitionNameLike(allRequestParams.get("processDefinitionNameLike"));
    }

    if (allRequestParams.get("executionId") != null) {
      queryRequest.setExecutionId(allRequestParams.get("executionId"));
    }

    if (allRequestParams.get("taskName") != null) {
      queryRequest.setTaskName(allRequestParams.get("taskName"));
    }

    if (allRequestParams.get("taskNameLike") != null) {
      queryRequest.setTaskNameLike(allRequestParams.get("taskNameLike"));
    }

    if (allRequestParams.get("taskDescription") != null) {
      queryRequest.setTaskDescription(allRequestParams.get("taskDescription"));
    }

    if (allRequestParams.get("taskDescriptionLike") != null) {
      queryRequest.setTaskDescriptionLike(allRequestParams.get("taskDescriptionLike"));
    }

    if (allRequestParams.get("taskDefinitionKey") != null) {
      queryRequest.setTaskDefinitionKey(allRequestParams.get("taskDefinitionKey"));
    }

    if (allRequestParams.containsKey("taskCategory")) {
      queryRequest.setTaskCategory(allRequestParams.get("taskCategory"));
    }

    if (allRequestParams.get("taskDeleteReason") != null) {
      queryRequest.setTaskDeleteReason(allRequestParams.get("taskDeleteReason"));
    }

    if (allRequestParams.get("taskDeleteReasonLike") != null) {
      queryRequest.setTaskDeleteReasonLike(allRequestParams.get("taskDeleteReasonLike"));
    }

    if (allRequestParams.get("taskAssignee") != null) {
      queryRequest.setTaskAssignee(allRequestParams.get("taskAssignee"));
    }

    if (allRequestParams.get("taskAssigneeLike") != null) {
      queryRequest.setTaskAssigneeLike(allRequestParams.get("taskAssigneeLike"));
    }

    if (allRequestParams.get("taskOwner") != null) {
      queryRequest.setTaskOwner(allRequestParams.get("taskOwner"));
    }

    if (allRequestParams.get("taskOwnerLike") != null) {
      queryRequest.setTaskOwnerLike(allRequestParams.get("taskOwnerLike"));
    }

    if (allRequestParams.get("taskInvolvedUser") != null) {
      queryRequest.setTaskInvolvedUser(allRequestParams.get("taskInvolvedUser"));
    }

    if (allRequestParams.get("taskPriority") != null) {
      queryRequest.setTaskPriority(Integer.valueOf(allRequestParams.get("taskPriority")));
    }

    if (allRequestParams.get("taskMinPriority") != null) {
      queryRequest.setTaskMinPriority(Integer.valueOf(allRequestParams.get("taskMinPriority")));
    }

    if (allRequestParams.get("taskMaxPriority") != null) {
      queryRequest.setTaskMaxPriority(Integer.valueOf(allRequestParams.get("taskMaxPriority")));
    }

    if (allRequestParams.get("finished") != null) {
      queryRequest.setFinished(Boolean.valueOf(allRequestParams.get("finished")));
    }

    if (allRequestParams.get("processFinished") != null) {
      queryRequest.setProcessFinished(Boolean.valueOf(allRequestParams.get("processFinished")));
    }

    if (allRequestParams.get("parentTaskId") != null) {
      queryRequest.setParentTaskId(allRequestParams.get("parentTaskId"));
    }

    if (allRequestParams.get("dueDate") != null) {
      queryRequest.setDueDate(RequestUtil.getDate(allRequestParams, "dueDate"));
    }

    if (allRequestParams.get("dueDateAfter") != null) {
      queryRequest.setDueDateAfter(RequestUtil.getDate(allRequestParams, "dueDateAfter"));
    }

    if (allRequestParams.get("dueDateBefore") != null) {
      queryRequest.setDueDateBefore(RequestUtil.getDate(allRequestParams, "dueDateBefore"));
    }

    if (allRequestParams.get("taskCreatedOn") != null) {
      queryRequest.setTaskCreatedOn(RequestUtil.getDate(allRequestParams, "taskCreatedOn"));
    }

    if (allRequestParams.get("taskCreatedBefore") != null) {
      queryRequest.setTaskCreatedBefore(RequestUtil.getDate(allRequestParams, "taskCreatedBefore"));
    }

    if (allRequestParams.get("taskCreatedAfter") != null) {
      queryRequest.setTaskCreatedAfter(RequestUtil.getDate(allRequestParams, "taskCreatedAfter"));
    }

    if (allRequestParams.get("taskCompletedOn") != null) {
      queryRequest.setTaskCompletedOn(RequestUtil.getDate(allRequestParams, "taskCompletedOn"));
    }

    if (allRequestParams.get("taskCompletedBefore") != null) {
      queryRequest.setTaskCompletedBefore(RequestUtil.getDate(allRequestParams, "taskCompletedBefore"));
    }

    if (allRequestParams.get("taskCompletedAfter") != null) {
      queryRequest.setTaskCompletedAfter(RequestUtil.getDate(allRequestParams, "taskCompletedAfter"));
    }

    if (allRequestParams.get("includeTaskLocalVariables") != null) {
      queryRequest.setIncludeTaskLocalVariables(Boolean.valueOf(allRequestParams.get("includeTaskLocalVariables")));
    }

    if (allRequestParams.get("includeProcessVariables") != null) {
      queryRequest.setIncludeProcessVariables(Boolean.valueOf(allRequestParams.get("includeProcessVariables")));
    }

    if (allRequestParams.get("tenantId") != null) {
      queryRequest.setTenantId(allRequestParams.get("tenantId"));
    }

    if (allRequestParams.get("tenantIdLike") != null) {
      queryRequest.setTenantIdLike(allRequestParams.get("tenantIdLike"));
    }

    if (allRequestParams.get("withoutTenantId") != null) {
      queryRequest.setWithoutTenantId(Boolean.valueOf(allRequestParams.get("withoutTenantId")));
    }

    if (allRequestParams.get("taskCandidateGroup") != null) {
      queryRequest.setTaskCandidateGroup(allRequestParams.get("taskCandidateGroup"));
    }

    return getQueryResponse(queryRequest, allRequestParams, request.getRequestURL().toString().replace("/history/historic-task-instances", ""));
  }
}

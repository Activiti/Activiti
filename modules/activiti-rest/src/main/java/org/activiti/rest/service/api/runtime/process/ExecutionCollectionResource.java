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
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.rest.common.api.DataResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Frederik Heremans
 */
@RestController
@Api(tags = { "Executions" }, description = "Manage Executions", authorizations = { @Authorization(value = "basicAuth") })
public class ExecutionCollectionResource extends ExecutionBaseResource {

  @ApiOperation(value = "List of executions", tags = {"Executions"}, nickname = "getExecutions")
  @ApiImplicitParams({
    @ApiImplicitParam(name = "id", dataType = "string", value = "Only return models with the given version.", paramType = "query"),
    @ApiImplicitParam(name = "activityId", dataType = "string", value = "Only return executions with the given activity id.", paramType = "query"),
    @ApiImplicitParam(name = "processDefinitionKey", dataType = "string", value = "Only return process instances with the given process definition key.", paramType = "query"),
    @ApiImplicitParam(name = "processDefinitionId", dataType = "string", value = "Only return process instances with the given process definition id.", paramType = "query"),
    @ApiImplicitParam(name = "processInstanceId", dataType = "string", value = "Only return executions which are part of the process instance with the given id.", paramType = "query"),
    @ApiImplicitParam(name = "messageEventSubscriptionName", dataType = "string", value = "Only return executions which are subscribed to a message with the given name.", paramType = "query"),
    @ApiImplicitParam(name = "signalEventSubscriptionName", dataType = "string", value = "Only return executions which are subscribed to a signal with the given name.", paramType = "query"),
    @ApiImplicitParam(name = "parentId", dataType = "string", value = "Only return executions which are a direct child of the given execution.", paramType = "query"),
    @ApiImplicitParam(name = "tenantId", dataType = "string", value = "Only return process instances with the given tenantId.", paramType = "query"),
    @ApiImplicitParam(name = "tenantIdLike", dataType = "string", value = "Only return process instances with a tenantId like the given value.", paramType = "query"),
    @ApiImplicitParam(name = "withoutTenantId", dataType = "boolean", value = "If true, only returns process instances without a tenantId set. If false, the withoutTenantId parameter is ignored.", paramType = "query"),
    @ApiImplicitParam(name = "sort", dataType = "string", value = "Property to sort on, to be used together with the order.", allowableValues ="processInstanceId ,processDefinitionId,processDefinitionKey ,tenantId", paramType = "query"),
  })
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Indicates request was successful and the executions are returned"),
      @ApiResponse(code = 404, message = "Indicates a parameter was passed in the wrong format . The status-message contains additional information.")
  })
  @RequestMapping(value = "/runtime/executions", method = RequestMethod.GET, produces = "application/json")
  public DataResponse getProcessInstances(@ApiParam(hidden = true) @RequestParam Map<String, String> allRequestParams, HttpServletRequest request) {
    // Populate query based on request
    ExecutionQueryRequest queryRequest = new ExecutionQueryRequest();

    if (allRequestParams.containsKey("id")) {
      queryRequest.setId(allRequestParams.get("id"));
    }

    if (allRequestParams.containsKey("processInstanceId")) {
      queryRequest.setProcessInstanceId(allRequestParams.get("processInstanceId"));
    }

    if (allRequestParams.containsKey("processInstanceBusinessKey")) {
      queryRequest.setProcessBusinessKey(allRequestParams.get("processInstanceBusinessKey"));
    }

    if (allRequestParams.containsKey("processDefinitionKey")) {
      queryRequest.setProcessDefinitionKey(allRequestParams.get("processDefinitionKey"));
    }

    if (allRequestParams.containsKey("processDefinitionId")) {
      queryRequest.setProcessDefinitionId(allRequestParams.get("processDefinitionId"));
    }

    if (allRequestParams.containsKey("messageEventSubscriptionName")) {
      queryRequest.setMessageEventSubscriptionName(allRequestParams.get("messageEventSubscriptionName"));
    }

    if (allRequestParams.containsKey("signalEventSubscriptionName")) {
      queryRequest.setSignalEventSubscriptionName(allRequestParams.get("signalEventSubscriptionName"));
    }

    if (allRequestParams.containsKey("activityId")) {
      queryRequest.setActivityId(allRequestParams.get("activityId"));
    }

    if (allRequestParams.containsKey("parentId")) {
      queryRequest.setParentId(allRequestParams.get("parentId"));
    }

    if (allRequestParams.containsKey("tenantId")) {
      queryRequest.setTenantId(allRequestParams.get("tenantId"));
    }

    if (allRequestParams.containsKey("tenantIdLike")) {
      queryRequest.setTenantIdLike(allRequestParams.get("tenantIdLike"));
    }

    if (allRequestParams.containsKey("withoutTenantId")) {
      if (Boolean.valueOf(allRequestParams.get("withoutTenantId"))) {
        queryRequest.setWithoutTenantId(Boolean.TRUE);
      }
    }

    return getQueryResponse(queryRequest, allRequestParams, request.getRequestURL().toString().replace("/runtime/executions", ""));
  }

  @ApiOperation(value = "Signal event received", tags = {"Executions"})
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Indicates request was successful and the executions are returned"),
      @ApiResponse(code = 404, message = "Indicates a parameter was passed in the wrong format . The status-message contains additional information.")
  })
  @RequestMapping(value = "/runtime/executions", method = RequestMethod.PUT)
  public void executeExecutionAction(@RequestBody ExecutionActionRequest actionRequest, HttpServletResponse response) {
    if (!ExecutionActionRequest.ACTION_SIGNAL_EVENT_RECEIVED.equals(actionRequest.getAction())) {
      throw new ActivitiIllegalArgumentException("Illegal action: '" + actionRequest.getAction() + "'.");
    }

    if (actionRequest.getSignalName() == null) {
      throw new ActivitiIllegalArgumentException("Signal name is required.");
    }

    if (actionRequest.getVariables() != null) {
      runtimeService.signalEventReceived(actionRequest.getSignalName(), getVariablesToSet(actionRequest.getVariables()));
    } else {
      runtimeService.signalEventReceived(actionRequest.getSignalName());
    }
    response.setStatus(HttpStatus.NO_CONTENT.value());
  }
}

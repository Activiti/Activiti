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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Tijs Rademakers
 */
@RestController
@Api(tags = { "History" }, description = "Manage History", authorizations = { @Authorization(value = "basicAuth") })
public class HistoricActivityInstanceCollectionResource extends HistoricActivityInstanceBaseResource {



  @ApiOperation(value = "Get historic activity instances", tags = { "History" }, nickname = "getHistoricActivityInstances")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Indicates that historic activity instances could be queried."),
      @ApiResponse(code = 400, message = "Indicates an parameter was passed in the wrong format. The status-message contains additional information.") })
  @ApiImplicitParams({
    @ApiImplicitParam(name = "activityId", dataType = "string", value = "An id of the activity instance.", paramType = "query"),
    @ApiImplicitParam(name = "activityInstanceId", dataType = "string", value = "An id of the historic activity instance.", paramType = "query"),
    @ApiImplicitParam(name = "activityName",  dataType = "string", value = "The name of the historic activity instance.", paramType = "query"),
    @ApiImplicitParam(name = "activityType",  dataType = "string", value = "The element type of the historic activity instance.", paramType = "query"),
    @ApiImplicitParam(name = "executionId",  dataType = "string", value = "The execution id of the historic activity instance.", paramType = "query"),
    @ApiImplicitParam(name = "finished",  dataType = "boolean", value = "Indication if the historic activity instance is finished.", paramType = "query"),
    @ApiImplicitParam(name = "taskAssignee",  dataType = "string", value = "The assignee of the historic activity instance.", paramType = "query"),
    @ApiImplicitParam(name = "processInstanceId",  dataType = "string", value = "The process instance id of the historic activity instance.", paramType = "query"),
    @ApiImplicitParam(name = "processDefinitionId",  dataType = "string", value = "The process definition id of the historic activity instance.", paramType = "query"),
    @ApiImplicitParam(name = "tenantId",  dataType = "string", value = "Only return instances with the given tenantId.", paramType = "query"),
    @ApiImplicitParam(name = "tenantIdLike",  dataType = "string", value = "Only return instances with a tenantId like the given value.", paramType = "query"),
    @ApiImplicitParam(name = "withoutTenantId",  dataType = "boolean", value = "If true, only returns instances without a tenantId set. If false, the withoutTenantId parameter is ignored.", paramType = "query")
  })
  @RequestMapping(value = "/history/historic-activity-instances", method = RequestMethod.GET, produces = "application/json")
  public DataResponse getHistoricActivityInstances(@ApiParam(hidden = true) @RequestParam Map<String, String> allRequestParams, HttpServletRequest request) {
    HistoricActivityInstanceQueryRequest query = new HistoricActivityInstanceQueryRequest();

    // Populate query based on request
    if (allRequestParams.get("activityId") != null) {
      query.setActivityId(allRequestParams.get("activityId"));
    }

    if (allRequestParams.get("activityInstanceId") != null) {
      query.setActivityInstanceId(allRequestParams.get("activityInstanceId"));
    }

    if (allRequestParams.get("activityName") != null) {
      query.setActivityName(allRequestParams.get("activityName"));
    }

    if (allRequestParams.get("activityType") != null) {
      query.setActivityType(allRequestParams.get("activityType"));
    }

    if (allRequestParams.get("executionId") != null) {
      query.setExecutionId(allRequestParams.get("executionId"));
    }

    if (allRequestParams.get("finished") != null) {
      query.setFinished(Boolean.valueOf(allRequestParams.get("finished")));
    }

    if (allRequestParams.get("taskAssignee") != null) {
      query.setTaskAssignee(allRequestParams.get("taskAssignee"));
    }

    if (allRequestParams.get("processInstanceId") != null) {
      query.setProcessInstanceId(allRequestParams.get("processInstanceId"));
    }

    if (allRequestParams.get("processDefinitionId") != null) {
      query.setProcessDefinitionId(allRequestParams.get("processDefinitionId"));
    }

    if (allRequestParams.get("tenantId") != null) {
      query.setTenantId(allRequestParams.get("tenantId"));
    }

    if (allRequestParams.get("tenantIdLike") != null) {
      query.setTenantIdLike(allRequestParams.get("tenantIdLike"));
    }

    if (allRequestParams.get("withoutTenantId") != null) {
      query.setWithoutTenantId(Boolean.valueOf(allRequestParams.get("withoutTenantId")));
    }

    return getQueryResponse(query, allRequestParams);
  }
}

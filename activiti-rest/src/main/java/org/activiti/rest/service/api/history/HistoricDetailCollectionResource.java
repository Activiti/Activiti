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
public class HistoricDetailCollectionResource extends HistoricDetailBaseResource {


  @ApiOperation(value = "Get historic detail", tags = { "History" }, notes = "")
  @ApiImplicitParams({
    @ApiImplicitParam(name = "id", dataType = "string", value = "The id of the historic detail.", paramType = "query"),
    @ApiImplicitParam(name = "processInstanceId", dataType = "string", value = "The process instance id of the historic detail.", paramType = "query"),
    @ApiImplicitParam(name = "executionId", dataType = "string", value = "The execution id of the historic detail.", paramType = "query"),
    @ApiImplicitParam(name = "activityInstanceId", dataType = "string", value = "The activity instance id of the historic detail.", paramType = "query"),
    @ApiImplicitParam(name = "taskId", dataType = "string", value = "The task id of the historic detail.", paramType = "query"),
    @ApiImplicitParam(name = "selectOnlyFormProperties", dataType = "boolean", value = "Indication to only return form properties in the result.", paramType = "query"),
    @ApiImplicitParam(name = "selectOnlyVariableUpdates", dataType = "boolean", value = "Indication to only return variable updates in the result.", paramType = "query"),
  })
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Indicates that historic detail could be queried."),
      @ApiResponse(code = 400, message = "Indicates an parameter was passed in the wrong format. The status-message contains additional information.") })
  @RequestMapping(value = "/history/historic-detail", method = RequestMethod.GET, produces = "application/json")
  public DataResponse getHistoricDetailInfo(@ApiParam(hidden = true) @RequestParam Map<String, String> allRequestParams, HttpServletRequest request) {
    // Populate query based on request
    HistoricDetailQueryRequest queryRequest = new HistoricDetailQueryRequest();

    if (allRequestParams.get("id") != null) {
      queryRequest.setId(allRequestParams.get("id"));
    }

    if (allRequestParams.get("processInstanceId") != null) {
      queryRequest.setProcessInstanceId(allRequestParams.get("processInstanceId"));
    }

    if (allRequestParams.get("executionId") != null) {
      queryRequest.setExecutionId(allRequestParams.get("executionId"));
    }

    if (allRequestParams.get("activityInstanceId") != null) {
      queryRequest.setActivityInstanceId(allRequestParams.get("activityInstanceId"));
    }

    if (allRequestParams.get("taskId") != null) {
      queryRequest.setTaskId(allRequestParams.get("taskId"));
    }

    if (allRequestParams.get("selectOnlyFormProperties") != null) {
      queryRequest.setSelectOnlyFormProperties(Boolean.valueOf(allRequestParams.get("selectOnlyFormProperties")));
    }

    if (allRequestParams.get("selectOnlyVariableUpdates") != null) {
      queryRequest.setSelectOnlyVariableUpdates(Boolean.valueOf(allRequestParams.get("selectOnlyVariableUpdates")));
    }

    return getQueryResponse(queryRequest, allRequestParams);
  }
}

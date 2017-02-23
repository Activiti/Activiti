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
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.activiti.rest.common.api.DataResponse;
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
public class ExecutionQueryResource extends ExecutionBaseResource {

  @ApiOperation(value = "Query executions", tags = {"Executions"}, nickname = "queryExecutions",
      notes ="The request body can contain all possible filters that can be used in the List executions URL query. On top of these, it’s possible to provide an array of variables and processInstanceVariables to include in the query, with their format described here.\n"
          + "\n" + "The general paging and sorting query-parameters can be used for this URL.")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Indicates request was successful and the executions are returned."),
      @ApiResponse(code = 404, message = "Indicates a parameter was passed in the wrong format . The status-message contains additional information.")
  })
  @RequestMapping(value = "/query/executions", method = RequestMethod.POST, produces = "application/json")
  public DataResponse queryProcessInstances(@RequestBody ExecutionQueryRequest queryRequest,@ApiParam(hidden = true) @RequestParam Map<String, String> allRequestParams, HttpServletRequest request) {

    return getQueryResponse(queryRequest, allRequestParams, request.getRequestURL().toString().replace("/query/executions", ""));
  }
}

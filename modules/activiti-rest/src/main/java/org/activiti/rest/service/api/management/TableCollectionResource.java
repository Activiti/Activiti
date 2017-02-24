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

package org.activiti.rest.service.api.management;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.activiti.engine.ManagementService;
import org.activiti.rest.service.api.RestResponseFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Frederik Heremans
 */
@RestController
@Api(tags = { "Database tables" }, description = "Manage Database tables", authorizations = { @Authorization(value = "basicAuth") })
public class TableCollectionResource {

  @Autowired
  protected RestResponseFactory restResponseFactory;

  @Autowired
  protected ManagementService managementService;

  @ApiOperation(value = " List of tables", tags = {"Database tables"})
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Indicates the request was successful.")
  })
  @RequestMapping(value = "/management/tables", method = RequestMethod.GET, produces = "application/json")
  public List<TableResponse> getTables(HttpServletRequest request) {
    return restResponseFactory.createTableResponseList(managementService.getTableCount());
  }
}

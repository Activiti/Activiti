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

import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.ManagementService;
import org.activiti.rest.service.api.RestResponseFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Frederik Heremans
 */
@RestController
public class TableResource {
  
  @Autowired
  protected RestResponseFactory restResponseFactory;
  
  @Autowired
  protected ManagementService managementService;
  
  @RequestMapping(value="/management/tables/{tableName}", method = RequestMethod.GET, produces = "application/json")
  public TableResponse getTable(@PathVariable String tableName, HttpServletRequest request) {
    Map<String, Long> tableCounts = managementService.getTableCount();

    TableResponse response = null;
    for (Entry<String, Long> entry : tableCounts.entrySet()) {
      if (entry.getKey().equals(tableName)) {
        response = restResponseFactory.createTableResponse(entry.getKey(), entry.getValue());
        break;
      }
    }
   
    if (response == null) {
      throw new ActivitiObjectNotFoundException("Could not find a table with name '" + tableName + "'.", String.class);
    }
    return response;
  }
}

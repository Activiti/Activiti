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

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.ManagementService;
import org.activiti.engine.management.TablePage;
import org.activiti.engine.management.TablePageQuery;
import org.activiti.rest.common.api.DataResponse;
import org.activiti.rest.service.api.RestResponseFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Frederik Heremans
 */
@RestController
public class TableDataResource {
  
  protected static final Integer DEFAULT_RESULT_SIZE = 10;

  @Autowired
  protected RestResponseFactory restResponseFactory;
  
  @Autowired
  protected ManagementService managementService;
  
  @RequestMapping(value="/management/tables/{tableName}/data", method = RequestMethod.GET, produces = "application/json")
  public DataResponse getTableData(@PathVariable String tableName, @RequestParam Map<String,String> allRequestParams) {
    // Check if table exists before continuing
    if (managementService.getTableMetaData(tableName) == null) {
      throw new ActivitiObjectNotFoundException("Could not find a table with name '" + tableName + "'.", String.class);
    }
    
    String orderAsc = allRequestParams.get("orderAscendingColumn");
    String orderDesc = allRequestParams.get("orderDescendingColumn");
    
    if (orderAsc != null && orderDesc != null) {
      throw new ActivitiIllegalArgumentException("Only one of 'orderAscendingColumn' or 'orderDescendingColumn' can be supplied.");
    }
    
    Integer start = null;
    if (allRequestParams.containsKey("start")) {
      start = Integer.valueOf(allRequestParams.get("start"));
    }
    
    if (start == null) {
      start = 0;
    }
    
    Integer size = null;
    if (allRequestParams.containsKey("size")) {
      size = Integer.valueOf(allRequestParams.get("size"));
    }
    
    if (size == null) {
      size = DEFAULT_RESULT_SIZE;
    }
    
    DataResponse response = new DataResponse();
    
    TablePageQuery tablePageQuery = managementService.createTablePageQuery().tableName(tableName);
    
    if (orderAsc != null) {
      tablePageQuery.orderAsc(orderAsc);
      response.setOrder("asc");
      response.setSort(orderAsc);
    }
    
    if (orderDesc != null) {
      tablePageQuery.orderDesc(orderDesc);
      response.setOrder("desc");
      response.setSort(orderDesc);
    }
    
    TablePage listPage = tablePageQuery.listPage(start, size);
    response.setSize(((Long) listPage.getSize()).intValue());
    response.setStart(((Long) listPage.getFirstResult()).intValue());
    response.setTotal(listPage.getTotal());
    response.setData(listPage.getRows());
    
    return response;
  }
}

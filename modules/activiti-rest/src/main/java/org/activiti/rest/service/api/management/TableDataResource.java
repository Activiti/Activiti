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

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.management.TablePage;
import org.activiti.engine.management.TablePageQuery;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.common.api.DataResponse;
import org.activiti.rest.common.api.SecuredResource;
import org.restlet.data.Form;
import org.restlet.resource.Get;

/**
 * @author Frederik Heremans
 */
public class TableDataResource extends SecuredResource {
  
  protected static final Integer DEFAULT_RESULT_SIZE = 10;

  @Get("json")
  public DataResponse getTableData() {
    if(authenticate() == false) return null;

    String tableName = getAttribute("tableName");
    if(tableName == null) {
      throw new ActivitiIllegalArgumentException("The tableName cannot be null");
    }
    
    // Check if table exists before continuing
    if(ActivitiUtil.getManagementService().getTableMetaData(tableName) == null) {
      throw new ActivitiObjectNotFoundException("Could not find a table with name '" + tableName + "'.", String.class);
    }
    
    Form queryForm = getQuery();
    String orderAsc = getQueryParameter("orderAscendingColumn", queryForm);
    String orderDesc = getQueryParameter("orderDescendingColumn", queryForm);
    
    if(orderAsc != null && orderDesc != null) {
      throw new ActivitiIllegalArgumentException("Only one of 'orderAscendingColumn' or 'orderDescendingColumn' can be supplied.");
    }
    
    Integer start = getQueryParameterAsInt("start", queryForm);
    if(start == null) {
      start = 0;
    }
    Integer size = getQueryParameterAsInt("size", queryForm);
    if(size == null) {
      size = DEFAULT_RESULT_SIZE;
    }
    
    DataResponse response = new DataResponse();
    
    TablePageQuery tablePageQuery = ActivitiUtil.getManagementService().createTablePageQuery()
            .tableName(tableName);
    
    if(orderAsc != null) {
      tablePageQuery.orderAsc(orderAsc);
      response.setOrder("asc");
      response.setSort(orderAsc);
    }
    
    if(orderDesc != null) {
      tablePageQuery.orderDesc(orderDesc);
      response.setOrder("desc");
      response.setSort(orderDesc);
    }
    
    TablePage listPage = tablePageQuery.listPage(start, size);
    response.setSize(((Long)listPage.getSize()).intValue());
    response.setStart(((Long)listPage.getFirstResult()).intValue());
    response.setTotal(listPage.getTotal());
    response.setData(listPage.getRows());
    
   return response;
  }
}

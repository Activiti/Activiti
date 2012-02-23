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

package org.activiti.rest.api.management;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.activiti.engine.management.TableMetaData;
import org.activiti.engine.management.TablePage;
import org.activiti.engine.management.TablePageQuery;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.RequestUtil;
import org.activiti.rest.api.SecuredResource;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.restlet.resource.Get;

/**
 * @author Tijs Rademakers
 */
public class TableDataResource extends SecuredResource {
  
  @Get
  public ObjectNode getTableData() {
    if(authenticate(SecuredResource.ADMIN) == false) return null;
    
    String tableName = (String) getRequest().getAttributes().get("tableName");
    int start = RequestUtil.getInteger(getQuery(), "start", 0);
    int size = RequestUtil.getInteger(getQuery(), "size", 10);
    String order = getQuery().getValues("order");
    if(order == null) {
      order = "asc";
    }
    String sort = getQuery().getValues("sort");

    if (sort == null) {
      TableMetaData tmd = ActivitiUtil.getManagementService().getTableMetaData(tableName);
      List<String> columnNames = tmd.getColumnNames();
      if (columnNames.size() > 0) {
        sort = columnNames.get(0);
      }
      else {
        sort = null;
      }
    }

    TablePageQuery query = ActivitiUtil.getManagementService()
      .createTablePageQuery()
      .tableName(tableName);
    if (sort != null) {
      if (order.equals("asc")) {
        query.orderAsc(sort);
      }
      else {
        query.orderDesc(sort);
      }
    }

    TablePage tablePage = query.listPage(start, size);
    
    ObjectNode responseJSON = new ObjectMapper().createObjectNode();
    responseJSON.put("sort", sort);
    responseJSON.put("order", order);
    responseJSON.put("start", tablePage.getFirstResult());
    responseJSON.put("size", tablePage.getSize());
    responseJSON.put("total", tablePage.getTotal());
    
    ArrayNode tableArray = new ObjectMapper().createArrayNode();
    
    for (Map<String, Object> row : tablePage.getRows()) {
      ObjectNode columnJSON = new ObjectMapper().createObjectNode();
      for (String name : row.keySet()) {
        if(row.get(name) instanceof byte[]) {
          columnJSON.put(name, ((byte[]) row.get(name)).length);
        } else if(row.get(name) instanceof Date) {
          columnJSON.put(name, RequestUtil.dateToString((Date) row.get(name)));
        } else {
          columnJSON.put(name, row.get(name).toString());
        }
      }
      tableArray.add(columnJSON);
    }
    
    responseJSON.put("data", tableArray);
    
    return responseJSON;
  }
}

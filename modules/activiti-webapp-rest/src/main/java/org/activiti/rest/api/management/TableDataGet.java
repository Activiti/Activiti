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

import org.activiti.engine.management.TableMetaData;
import org.activiti.engine.management.TablePage;
import org.activiti.engine.management.TablePageQuery;
import org.activiti.rest.util.ActivitiRequest;
import org.activiti.rest.util.ActivitiWebScript;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;

import java.util.List;
import java.util.Map;

/**
 * Returns signalData, metadata and paging info about a table.
 *
 * @author Erik Winlof
 */
public class TableDataGet extends ActivitiWebScript {

  /**
   * Prepares signalData, metadata and paging info about a table for the webscript template.
   *
   * @param req The webscripts request
   * @param status The webscripts status
   * @param cache The webscript cache
   * @param model The webscripts template model
   */
  @Override
  protected void executeWebScript(ActivitiRequest req, Status status, Cache cache, Map<String, Object> model) {
    String tableName = req.getMandatoryPathParameter("tableName");
    int start = req.getInteger("start", 0);
    int size = req.getInteger("size", 10);
    String order = req.getString("order", "asc");
    String sort = req.getString("sort", null);

    if (sort == null)
    {
      TableMetaData tmd = getManagementService().getTableMetaData(tableName);
      List<String> columnNames = tmd.getColumnNames();
      if (columnNames.size() > 0) {
        sort = columnNames.get(0);
      }
      else {
        sort = null;
      }
    }

    TablePageQuery query = getManagementService()
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
    model.put("sort", sort);
    model.put("order", order);
    model.put("start", tablePage.getFirstResult());
    model.put("size", tablePage.getSize());
    model.put("total", tablePage.getTotal());
    model.put("tablePage", tablePage);
  }

}
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

import org.activiti.engine.TablePageQuery;
import org.activiti.rest.util.ActivitiWebScript;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import java.util.Map;

/**
 * Returns data, metadata and paging info about a table.
 *
 * @author Erik Winl�f
 */
public class TableDataGet extends ActivitiWebScript
{

  /**
   * Prepares data, metadata and paging info about a table for the webscript template.
   *
   * @param req The webscripts request
   * @param status The webscripts status
   * @param cache The webscript cache
   * @param model The webscripts template model
   */
  @Override
  protected void executeWebScript(WebScriptRequest req, Status status, Cache cache, Map<String, Object> model)
  {
    String tableName = getMandatoryPathParameter(req, "tableName");
    int size = getInt(req, "size", 10);
    TablePageQuery query = getManagementService().createTablePageQuery()
        .tableName(tableName)
        .start(getInt(req, "start", 10))
        .size(size);
    String sort = getString(req, "sort");
    if (sort != null && !sort.trim().equals("")) {
      String order = getString(req, "order", "asc");
      if (order.equals("asc")) {
        query.orderAsc(sort);
      }
      else {
        query.orderDesc(sort);
      }
    }
    model.put("size", size);
    model.put("tablePage", query.singleResult());
  }

}
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
package org.activiti.rest.util;

import org.activiti.engine.query.Query;
import org.activiti.engine.query.QueryProperty;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for all paged activiti webscripts.
 *
 * @author Erik Winlof
 */

public class ActivitiPagingWebScript extends ActivitiWebScript
{

  /**
   * Maps rest attribute names notation against the engine/database id's
   */
  protected HashMap<String, QueryProperty> properties = new HashMap<String, QueryProperty>();

  /**
   * uses the pagination parameters form the request and makes sure to order the result and set all pagination
   * attributes for the response to render
   *
   * @param req The request containing the pagination parameters
   * @param query The query to get the paged list from
   * @param listName The name model attribute name to use for the result list
   * @param model The model to put the list and the pagination attributes in
   * @param defaultSort THe default sort column (the rest attribute) that later will be mapped to an internal engine name
   */
  protected void paginateList(ActivitiRequest req, Query query, String listName, Map<String, Object> model, String defaultSort){
    // Collect parameters
    int start = req.getInteger("start", 0);
    int size = req.getInteger("size", 10);
    String sort = req.getString("sort", defaultSort);
    String order = req.getString("order", "asc");

    // Sort order
    if (sort != null && properties.size() > 0)
    {
      QueryProperty qp = properties.get(sort);
      if (qp == null) {
        throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Value for param 'sort' is not valid, '" + sort + "' is not a valid property");
      }
      query.orderBy(qp);
      if (order.equals("asc"))
      {
        query.asc();
      }
      else if (order.equals("desc"))
      {
        query.desc();
      }
      else {
        throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Value for param 'order' is not valid : '" + order + "', must be 'asc' or 'desc'");
      }
    }

    // Get result and set pagination parameters
    List list = query.listPage(start, size);
    model.put("start", start);
    model.put("size", list.size()); 
    model.put("sort", sort);
    model.put("order", order);
    model.put("total", query.count());
    model.put(listName, list);
  }

}

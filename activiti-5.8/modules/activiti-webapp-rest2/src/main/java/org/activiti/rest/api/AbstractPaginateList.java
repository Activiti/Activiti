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
 
package org.activiti.rest.api;

import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.AbstractQuery;
import org.activiti.engine.query.Query;
import org.activiti.engine.query.QueryProperty;
import org.restlet.data.Form;

/**
 * @author Tijs Rademakers
 */
public abstract class AbstractPaginateList {

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
  @SuppressWarnings("rawtypes")
  public DataResponse paginateList(Form form, Query query,
      String defaultSort, Map<String, QueryProperty> properties) {
    
    // Collect parameters
    int start = RequestUtil.getInteger(form, "start", 0);
    int size = RequestUtil.getInteger(form, "size", 10);
    String sort = form.getValues("sort");
    if(sort == null) {
      sort = defaultSort;
    }
    String order = form.getValues("order");
    if(order == null) {
      order = "asc";
    }

    // Sort order
    if (sort != null && properties.size() > 0) {
      QueryProperty qp = properties.get(sort);
      if (qp == null) {
        throw new ActivitiException("Value for param 'sort' is not valid, '" + sort + "' is not a valid property");
      }
      ((AbstractQuery) query).orderBy(qp);
      if (order.equals("asc")) {
        query.asc();
      }
      else if (order.equals("desc")) {
        query.desc();
      }
      else {
        throw new ActivitiException("Value for param 'order' is not valid : '" + order + "', must be 'asc' or 'desc'");
      }
    }

    // Get result and set pagination parameters
    List list = processList(query.listPage(start, size));
    DataResponse response = new DataResponse();
    response.setStart(start);
    response.setSize(list.size()); 
    response.setSort(sort);
    response.setOrder(order);
    response.setTotal(query.count());
    response.setData(list);
    return response;
  }
  
  @SuppressWarnings("rawtypes")
  protected abstract List processList(List list);
}

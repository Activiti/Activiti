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
 
package org.activiti.rest.common.api;

import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.AbstractQuery;
import org.activiti.engine.query.Query;
import org.activiti.engine.query.QueryProperty;

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
  public DataResponse paginateList(Map<String, String> requestParams, PaginateRequest paginateRequest, Query query,
      String defaultSort, Map<String, QueryProperty> properties) {
  	
  	if (paginateRequest == null) {
  		paginateRequest = new PaginateRequest();
  	}
  	
  	// In case pagination request is incomplete, fill with values found in URL if possible
  	if (paginateRequest.getStart() == null) {
  		paginateRequest.setStart(RequestUtil.getInteger(requestParams, "start", 0));
  	}
  	
  	if (paginateRequest.getSize() == null) {
  		paginateRequest.setSize(RequestUtil.getInteger(requestParams, "size", 10));
  	}
  	
  	if (paginateRequest.getOrder() == null) {
  		paginateRequest.setOrder(requestParams.get("order"));
  	}
  	
  	if (paginateRequest.getSort() == null) {
  		paginateRequest.setSort(requestParams.get("sort"));
  	}
      
  	// Use defaults for paging, if not set in the PaginationRequest, nor in the URL
  	Integer start = paginateRequest.getStart();
  	if(start == null || start < 0) {
  		start = 0;
  	}
  	
    Integer size = paginateRequest.getSize();
    if(size == null || size < 0) {
    	size = 10;
    }
    
    String sort = paginateRequest.getSort();
    if(sort == null) {
      sort = defaultSort;
    }
    String order = paginateRequest.getOrder();
    if(order == null) {
      order = "asc";
    }

    // Sort order
    if (sort != null && !properties.isEmpty()) {
      QueryProperty qp = properties.get(sort);
      if (qp == null) {
        throw new ActivitiIllegalArgumentException("Value for param 'sort' is not valid, '" + sort + "' is not a valid property");
      }
      ((AbstractQuery) query).orderBy(qp);
      if (order.equals("asc")) {
        query.asc();
      }
      else if (order.equals("desc")) {
        query.desc();
      }
      else {
        throw new ActivitiIllegalArgumentException("Value for param 'order' is not valid : '" + order + "', must be 'asc' or 'desc'");
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
  
  
  /**
   * uses the pagination parameters from the request and makes sure to order the result and set all pagination
   * attributes for the response to render
   *
   * @param req The request containing the pagination parameters
   * @param query The query to get the paged list from
   * @param listName The name model attribute name to use for the result list
   * @param model The model to put the list and the pagination attributes in
   * @param defaultSort THe default sort column (the rest attribute) that later will be mapped to an internal engine name
   */
  @SuppressWarnings("rawtypes")
  public DataResponse paginateList(Map<String, String> requestParams, Query query,
      String defaultSort, Map<String, QueryProperty> properties) {
  	return paginateList(requestParams, null, query, defaultSort, properties);
  }
  
  @SuppressWarnings("rawtypes")
  protected abstract List processList(List list);
}

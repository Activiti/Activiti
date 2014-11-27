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

package org.activiti.rest.service.api.history;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricDetailQuery;
import org.activiti.engine.impl.HistoricDetailQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.activiti.rest.common.api.DataResponse;
import org.activiti.rest.service.api.RestResponseFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Tijs Rademakers
 */
public class HistoricDetailBaseResource {

  private static Map<String, QueryProperty> allowedSortProperties = new HashMap<String, QueryProperty>();

  static {
    allowedSortProperties.put("processInstanceId", HistoricDetailQueryProperty.PROCESS_INSTANCE_ID);
    allowedSortProperties.put("time", HistoricDetailQueryProperty.TIME);
    allowedSortProperties.put("name", HistoricDetailQueryProperty.VARIABLE_NAME);
    allowedSortProperties.put("revision", HistoricDetailQueryProperty.VARIABLE_REVISION);
    allowedSortProperties.put("variableType", HistoricDetailQueryProperty.VARIABLE_TYPE);
  }
  
  @Autowired
  protected RestResponseFactory restResponseFactory;
  
  @Autowired
  protected HistoryService historyService;

  protected DataResponse getQueryResponse(HistoricDetailQueryRequest queryRequest, Map<String,String> allRequestParams) {
    HistoricDetailQuery query = historyService.createHistoricDetailQuery();

    // Populate query based on request
    if (queryRequest.getProcessInstanceId() != null) {
      query.processInstanceId(queryRequest.getProcessInstanceId());
    }
    if (queryRequest.getExecutionId() != null) {
      query.executionId(queryRequest.getExecutionId());
    }
    if (queryRequest.getActivityInstanceId() != null) {
      query.activityInstanceId(queryRequest.getActivityInstanceId());
    }
    if (queryRequest.getTaskId() != null) {
      query.taskId(queryRequest.getTaskId());
    }
    if (queryRequest.getSelectOnlyFormProperties() != null) {
      if (queryRequest.getSelectOnlyFormProperties()) {
        query.formProperties();
      }
    }
    if (queryRequest.getSelectOnlyVariableUpdates() != null) {
      if (queryRequest.getSelectOnlyVariableUpdates()) {
        query.variableUpdates();
      }
    }

    return new HistoricDetailPaginateList(restResponseFactory).paginateList(
        allRequestParams, queryRequest, query, "processInstanceId", allowedSortProperties);
  }
}

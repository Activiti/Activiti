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
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricVariableInstanceQuery;
import org.activiti.engine.impl.HistoricVariableInstanceQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.activiti.rest.common.api.DataResponse;
import org.activiti.rest.service.api.RestResponseFactory;
import org.activiti.rest.service.api.engine.variable.QueryVariable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Tijs Rademakers
 */
public class HistoricVariableInstanceBaseResource {

  private static Map<String, QueryProperty> allowedSortProperties = new HashMap<String, QueryProperty>();

  static {
    allowedSortProperties.put("processInstanceId", HistoricVariableInstanceQueryProperty.PROCESS_INSTANCE_ID);
    allowedSortProperties.put("variableName", HistoricVariableInstanceQueryProperty.VARIABLE_NAME);
  }
  
  @Autowired
  protected RestResponseFactory restResponseFactory;
  
  @Autowired
  protected HistoryService historyService;

  protected DataResponse getQueryResponse(HistoricVariableInstanceQueryRequest queryRequest, Map<String,String> allRequestParams) {
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

    // Populate query based on request
    if(queryRequest.getExcludeTaskVariables() != null) {
      if (queryRequest.getExcludeTaskVariables()) {
        query.excludeTaskVariables();
      }
    }
    
    if (queryRequest.getTaskId() != null) {
      query.taskId(queryRequest.getTaskId());
    }
    
    if(queryRequest.getExecutionId() != null) {
      query.executionId(queryRequest.getExecutionId());
    }

    if (queryRequest.getProcessInstanceId() != null) {
      query.processInstanceId(queryRequest.getProcessInstanceId());
    }
    
    if (queryRequest.getVariableName() != null) {
      query.variableName(queryRequest.getVariableName());
    }
    
    if (queryRequest.getVariableNameLike() != null) {
      query.variableNameLike(queryRequest.getVariableNameLike() );
    }
    
    if (queryRequest.getVariables() != null) {
      addVariables(query, queryRequest.getVariables());
    }
    
    return new HistoricVariableInstancePaginateList(restResponseFactory).paginateList(
        allRequestParams, query, "variableName", allowedSortProperties);
  }
  
  protected void addVariables(HistoricVariableInstanceQuery variableInstanceQuery, List<QueryVariable> variables) {
    for (QueryVariable variable : variables) {
      if (variable.getVariableOperation() == null) {
        throw new ActivitiIllegalArgumentException("Variable operation is missing for variable: " + variable.getName());
      }
      if (variable.getValue() == null) {
        throw new ActivitiIllegalArgumentException("Variable value is missing for variable: " + variable.getName());
      }

      boolean nameLess = variable.getName() == null;

      Object actualValue = restResponseFactory.getVariableValue(variable);

      // A value-only query is only possible using equals-operator
      if (nameLess) {
        throw new ActivitiIllegalArgumentException("Value-only query (without a variable-name) is not supported");
      }

      switch (variable.getVariableOperation()) {

      case EQUALS:
        variableInstanceQuery.variableValueEquals(variable.getName(), actualValue);
        break;

      default:
        throw new ActivitiIllegalArgumentException("Unsupported variable query operation: " + variable.getVariableOperation());
      }
    }
  }
}

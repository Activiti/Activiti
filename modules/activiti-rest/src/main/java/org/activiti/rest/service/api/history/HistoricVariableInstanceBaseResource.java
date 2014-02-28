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
import org.activiti.engine.history.HistoricVariableInstanceQuery;
import org.activiti.engine.impl.HistoricVariableInstanceQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.common.api.DataResponse;
import org.activiti.rest.common.api.SecuredResource;
import org.activiti.rest.service.api.RestResponseFactory;
import org.activiti.rest.service.api.engine.variable.QueryVariable;
import org.activiti.rest.service.application.ActivitiRestServicesApplication;
import org.restlet.data.Form;

/**
 * @author Tijs Rademakers
 */
public class HistoricVariableInstanceBaseResource extends SecuredResource {

  private static Map<String, QueryProperty> allowedSortProperties = new HashMap<String, QueryProperty>();

  static {
    allowedSortProperties.put("processInstanceId", HistoricVariableInstanceQueryProperty.PROCESS_INSTANCE_ID);
    allowedSortProperties.put("variableName", HistoricVariableInstanceQueryProperty.VARIABLE_NAME);
  }

  protected DataResponse getQueryResponse(HistoricVariableInstanceQueryRequest queryRequest, Form urlQuery) {
    HistoricVariableInstanceQuery query = ActivitiUtil.getHistoryService().createHistoricVariableInstanceQuery();

    // Populate query based on request
    if(queryRequest.getExcludeTaskVariables() != null) {
      if (queryRequest.getExcludeTaskVariables()) {
        query.excludeTaskVariables();
      }
    }
    
    if (queryRequest.getTaskId() != null) {
      query.taskId(queryRequest.getTaskId());
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
    
    return new HistoricVariableInstancePaginateList(this).paginateList(urlQuery, query, "variableName", allowedSortProperties);
  }
  
  protected void addVariables(HistoricVariableInstanceQuery variableInstanceQuery, List<QueryVariable> variables) {
    RestResponseFactory responseFactory = getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory();
    
    for (QueryVariable variable : variables) {
      if (variable.getVariableOperation() == null) {
        throw new ActivitiIllegalArgumentException("Variable operation is missing for variable: " + variable.getName());
      }
      if (variable.getValue() == null) {
        throw new ActivitiIllegalArgumentException("Variable value is missing for variable: " + variable.getName());
      }

      boolean nameLess = variable.getName() == null;

      Object actualValue = responseFactory.getVariableValue(variable);

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

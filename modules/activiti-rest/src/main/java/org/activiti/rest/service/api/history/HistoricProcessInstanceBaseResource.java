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
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.impl.HistoricProcessInstanceQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.activiti.rest.common.api.DataResponse;
import org.activiti.rest.service.api.RestResponseFactory;
import org.activiti.rest.service.api.engine.variable.QueryVariable;
import org.activiti.rest.service.api.engine.variable.QueryVariable.QueryVariableOperation;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Tijs Rademakers
 */
public class HistoricProcessInstanceBaseResource {

  private static Map<String, QueryProperty> allowedSortProperties = new HashMap<String, QueryProperty>();

  static {
    allowedSortProperties.put("processInstanceId", HistoricProcessInstanceQueryProperty.PROCESS_INSTANCE_ID_);
    allowedSortProperties.put("processDefinitionId", HistoricProcessInstanceQueryProperty.PROCESS_DEFINITION_ID);
    allowedSortProperties.put("businessKey", HistoricProcessInstanceQueryProperty.BUSINESS_KEY);
    allowedSortProperties.put("startTime", HistoricProcessInstanceQueryProperty.START_TIME);
    allowedSortProperties.put("endTime", HistoricProcessInstanceQueryProperty.END_TIME);
    allowedSortProperties.put("duration", HistoricProcessInstanceQueryProperty.DURATION);
    allowedSortProperties.put("tenantId", HistoricProcessInstanceQueryProperty.TENANT_ID);
  }
  
  @Autowired
  protected RestResponseFactory restResponseFactory;
  
  @Autowired
  protected HistoryService historyService;

  protected DataResponse getQueryResponse(HistoricProcessInstanceQueryRequest queryRequest, Map<String,String> allRequestParams) {
    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();

    // Populate query based on request
    if (queryRequest.getProcessInstanceId() != null) {
      query.processInstanceId(queryRequest.getProcessInstanceId());
    }
    if (queryRequest.getProcessInstanceIds() != null && !queryRequest.getProcessInstanceIds().isEmpty()) {
      query.processInstanceIds(new HashSet<String>(queryRequest.getProcessInstanceIds()));
    }
    if (queryRequest.getProcessDefinitionKey() != null) {
      query.processDefinitionKey(queryRequest.getProcessDefinitionKey());
    }
    if (queryRequest.getProcessDefinitionId() != null) {
      query.processDefinitionId(queryRequest.getProcessDefinitionId());
    }
    if (queryRequest.getProcessBusinessKey() != null) {
      query.processInstanceBusinessKey(queryRequest.getProcessBusinessKey());
    }
    if (queryRequest.getInvolvedUser() != null) {
      query.involvedUser(queryRequest.getInvolvedUser());
    }
    if (queryRequest.getSuperProcessInstanceId() != null) {
      query.superProcessInstanceId(queryRequest.getSuperProcessInstanceId());
    }
    if (queryRequest.getExcludeSubprocesses() != null) {
      query.excludeSubprocesses(queryRequest.getExcludeSubprocesses());
    }
    if (queryRequest.getFinishedAfter() != null) {
      query.finishedAfter(queryRequest.getFinishedAfter());
    }
    if (queryRequest.getFinishedBefore() != null) {
      query.finishedBefore(queryRequest.getFinishedBefore());
    }
    if (queryRequest.getStartedAfter() != null) {
      query.startedAfter(queryRequest.getStartedAfter());
    }
    if (queryRequest.getStartedBefore() != null) {
      query.startedBefore(queryRequest.getStartedBefore());
    }
    if (queryRequest.getStartedBy() != null) {
      query.startedBy(queryRequest.getStartedBy());
    }
    if (queryRequest.getFinished() != null) {
      if (queryRequest.getFinished()) {
        query.finished();
      } else {
        query.unfinished();
      }
    }
    if (queryRequest.getIncludeProcessVariables() != null) {
      if (queryRequest.getIncludeProcessVariables()) {
        query.includeProcessVariables();
      }
    }
    if (queryRequest.getVariables() != null) {
      addVariables(query, queryRequest.getVariables());
    }
    
    if (queryRequest.getTenantId() != null) {
    	query.processInstanceTenantId(queryRequest.getTenantId());
    }
    
    if (queryRequest.getTenantIdLike() != null) {
    	query.processInstanceTenantIdLike(queryRequest.getTenantIdLike());
    }
    
    if (Boolean.TRUE.equals(queryRequest.getWithoutTenantId())) {
    	query.processInstanceWithoutTenantId();
    }

    return new HistoricProcessInstancePaginateList(restResponseFactory).paginateList(
        allRequestParams, queryRequest, query, "processInstanceId", allowedSortProperties);
  }

  protected void addVariables(HistoricProcessInstanceQuery processInstanceQuery, List<QueryVariable> variables) {
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
      if (nameLess && variable.getVariableOperation() != QueryVariableOperation.EQUALS) {
        throw new ActivitiIllegalArgumentException("Value-only query (without a variable-name) is only supported when using 'equals' operation.");
      }

      switch (variable.getVariableOperation()) {

      case EQUALS:
        if (nameLess) {
          processInstanceQuery.variableValueEquals(actualValue);
        } else {
          processInstanceQuery.variableValueEquals(variable.getName(), actualValue);
        }
        break;

      case EQUALS_IGNORE_CASE:
        if (actualValue instanceof String) {
          processInstanceQuery.variableValueEqualsIgnoreCase(variable.getName(), (String) actualValue);
        } else {
          throw new ActivitiIllegalArgumentException("Only string variable values are supported when ignoring casing, but was: "
                  + actualValue.getClass().getName());
        }
        break;

      case NOT_EQUALS:
        processInstanceQuery.variableValueNotEquals(variable.getName(), actualValue);
        break;
        
      case LIKE:
        if (actualValue instanceof String) {
          processInstanceQuery.variableValueLike(variable.getName(), (String) actualValue);
        } else {
          throw new ActivitiIllegalArgumentException("Only string variable values are supported for like, but was: "
                  + actualValue.getClass().getName());
        }
        break;
        
      case LIKE_IGNORE_CASE:
          if (actualValue instanceof String) {
            processInstanceQuery.variableValueLikeIgnoreCase(variable.getName(), (String) actualValue);
          } else {
            throw new ActivitiIllegalArgumentException("Only string variable values are supported for like, but was: "
                    + actualValue.getClass().getName());
          }
          break;
        
      case GREATER_THAN:
        processInstanceQuery.variableValueGreaterThan(variable.getName(), actualValue);
        break;
        
      case GREATER_THAN_OR_EQUALS:
        processInstanceQuery.variableValueGreaterThanOrEqual(variable.getName(), actualValue);
        break;

      case LESS_THAN:
        processInstanceQuery.variableValueLessThan(variable.getName(), actualValue);
        break;
        
      case LESS_THAN_OR_EQUALS:
        processInstanceQuery.variableValueLessThanOrEqual(variable.getName(), actualValue);
        break;

      default:
        throw new ActivitiIllegalArgumentException("Unsupported variable query operation: " + variable.getVariableOperation());
      }
    }
  }
}

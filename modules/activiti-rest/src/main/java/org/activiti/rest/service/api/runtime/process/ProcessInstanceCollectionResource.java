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

package org.activiti.rest.service.api.runtime.process;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceBuilder;
import org.activiti.rest.common.api.DataResponse;
import org.activiti.rest.service.api.engine.variable.RestVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Modified the "createProcessInstance" method to conditionally call a "createProcessInstanceResponse" method with a different signature, which will conditionally return the process variables that
 * exist when the process instance either enters its first wait state or completes. In this case, the different method is always called with a flag of true, which means that it will always return
 * those variables. If variables are not to be returned, the original method is called, which doesn't return the variables.
 * 
 * @author Frederik Heremans
 * @author Ryan Johnston (@rjfsu)
 */
@RestController
public class ProcessInstanceCollectionResource extends BaseProcessInstanceResource {

  @Autowired
  protected HistoryService historyService;

  @RequestMapping(value = "/runtime/process-instances", method = RequestMethod.GET, produces = "application/json")
  public DataResponse getProcessInstances(@RequestParam Map<String, String> allRequestParams, HttpServletRequest request) {
    // Populate query based on request
    ProcessInstanceQueryRequest queryRequest = new ProcessInstanceQueryRequest();

    if (allRequestParams.containsKey("id")) {
      queryRequest.setProcessInstanceId(allRequestParams.get("id"));
    }

    if (allRequestParams.containsKey("processDefinitionKey")) {
      queryRequest.setProcessDefinitionKey(allRequestParams.get("processDefinitionKey"));
    }

    if (allRequestParams.containsKey("processDefinitionId")) {
      queryRequest.setProcessDefinitionId(allRequestParams.get("processDefinitionId"));
    }

    if (allRequestParams.containsKey("businessKey")) {
      queryRequest.setProcessBusinessKey(allRequestParams.get("businessKey"));
    }

    if (allRequestParams.containsKey("involvedUser")) {
      queryRequest.setInvolvedUser(allRequestParams.get("involvedUser"));
    }

    if (allRequestParams.containsKey("suspended")) {
      queryRequest.setSuspended(Boolean.valueOf(allRequestParams.get("suspended")));
    }

    if (allRequestParams.containsKey("superProcessInstanceId")) {
      queryRequest.setSuperProcessInstanceId(allRequestParams.get("superProcessInstanceId"));
    }

    if (allRequestParams.containsKey("subProcessInstanceId")) {
      queryRequest.setSubProcessInstanceId(allRequestParams.get("subProcessInstanceId"));
    }

    if (allRequestParams.containsKey("excludeSubprocesses")) {
      queryRequest.setExcludeSubprocesses(Boolean.valueOf(allRequestParams.get("excludeSubprocesses")));
    }

    if (allRequestParams.containsKey("includeProcessVariables")) {
      queryRequest.setIncludeProcessVariables(Boolean.valueOf(allRequestParams.get("includeProcessVariables")));
    }

    if (allRequestParams.containsKey("tenantId")) {
      queryRequest.setTenantId(allRequestParams.get("tenantId"));
    }

    if (allRequestParams.containsKey("tenantIdLike")) {
      queryRequest.setTenantIdLike(allRequestParams.get("tenantIdLike"));
    }

    if (allRequestParams.containsKey("withoutTenantId")) {
      if (Boolean.valueOf(allRequestParams.get("withoutTenantId"))) {
        queryRequest.setWithoutTenantId(Boolean.TRUE);
      }
    }

    return getQueryResponse(queryRequest, allRequestParams);
  }

  @RequestMapping(value = "/runtime/process-instances", method = RequestMethod.POST, produces = "application/json")
  public ProcessInstanceResponse createProcessInstance(@RequestBody ProcessInstanceCreateRequest request, HttpServletRequest httpRequest, HttpServletResponse response) {

    if (request.getProcessDefinitionId() == null && request.getProcessDefinitionKey() == null && request.getMessage() == null) {
      throw new ActivitiIllegalArgumentException("Either processDefinitionId, processDefinitionKey or message is required.");
    }

    int paramsSet = ((request.getProcessDefinitionId() != null) ? 1 : 0) + ((request.getProcessDefinitionKey() != null) ? 1 : 0) + ((request.getMessage() != null) ? 1 : 0);

    if (paramsSet > 1) {
      throw new ActivitiIllegalArgumentException("Only one of processDefinitionId, processDefinitionKey or message should be set.");
    }

    if (request.isTenantSet()) {
      // Tenant-id can only be used with either key or message
      if (request.getProcessDefinitionId() != null) {
        throw new ActivitiIllegalArgumentException("TenantId can only be used with either processDefinitionKey or message.");
      }
    }

    Map<String, Object> startVariables = null;
    if (request.getVariables() != null) {
      startVariables = new HashMap<String, Object>();
      for (RestVariable variable : request.getVariables()) {
        if (variable.getName() == null) {
          throw new ActivitiIllegalArgumentException("Variable name is required.");
        }
        startVariables.put(variable.getName(), restResponseFactory.getVariableValue(variable));
      }
    }
    
    Map<String, Object> transientVariables = null;
    if (request.getTransientVariables() != null) {
      transientVariables = new HashMap<String, Object>();
      for (RestVariable variable : request.getTransientVariables()) {
        if (variable.getName() == null) {
          throw new ActivitiIllegalArgumentException("Variable name is required.");
        }
        transientVariables.put(variable.getName(), restResponseFactory.getVariableValue(variable));
      }
    }

    // Actually start the instance based on key or id
    try {
      ProcessInstance instance = null;
      
      ProcessInstanceBuilder processInstanceBuilder = runtimeService.createProcessInstanceBuilder();
      if (request.getProcessDefinitionId() != null) {
        processInstanceBuilder.processDefinitionId(request.getProcessDefinitionId());
      }
      if (request.getProcessDefinitionKey() != null) {
        processInstanceBuilder.processDefinitionKey(request.getProcessDefinitionKey());
      }
      if (request.getMessage() != null) {
        processInstanceBuilder.messageName(request.getMessage());
      }
      if (request.getBusinessKey() != null) {
        processInstanceBuilder.businessKey(request.getBusinessKey());
      }
      if (request.isTenantSet()) {
        processInstanceBuilder.tenantId(request.getTenantId());
      }
      if (startVariables != null) {
        processInstanceBuilder.variables(startVariables);
      }
      if (transientVariables != null) {
        processInstanceBuilder.transientVariables(transientVariables);
      }
      
      instance = processInstanceBuilder.start();
      
      response.setStatus(HttpStatus.CREATED.value());

      if (request.getReturnVariables()) {
        Map<String, Object> runtimeVariableMap = null;
        List<HistoricVariableInstance> historicVariableList = null;
        if (instance.isEnded()) {
          historicVariableList = historyService.createHistoricVariableInstanceQuery().processInstanceId(instance.getId()).list();
        } else {
          runtimeVariableMap = runtimeService.getVariables(instance.getId());
        }
        return restResponseFactory.createProcessInstanceResponse(instance, true, runtimeVariableMap, historicVariableList);

      } else {
        return restResponseFactory.createProcessInstanceResponse(instance);
      }

    } catch (ActivitiObjectNotFoundException aonfe) {
      throw new ActivitiIllegalArgumentException(aonfe.getMessage(), aonfe);
    }
  }
}

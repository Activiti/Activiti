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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;

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
@Api(tags = { "Process Instances" }, description = "Manage Process Instances", authorizations = { @Authorization(value = "basicAuth") })
public class ProcessInstanceCollectionResource extends BaseProcessInstanceResource {

  @Autowired
  protected HistoryService historyService;

  @ApiOperation(value = "List of process instances", tags = {"Process Instances"})
  @ApiImplicitParams({
    @ApiImplicitParam(name = "id", dataType = "string", value = "Only return models with the given version.", paramType = "query"),
    @ApiImplicitParam(name = "processDefinitionKey", dataType = "string", value = "Only return process instances with the given process definition key.", paramType = "query"),
    @ApiImplicitParam(name = "processDefinitionId", dataType = "string", value = "Only return process instances with the given process definition id.", paramType = "query"),
    @ApiImplicitParam(name = "businessKey", dataType = "string", value = "Only return process instances with the given businessKey.", paramType = "query"),
    @ApiImplicitParam(name = "involvedUser", dataType = "string", value = "Only return process instances in which the given user is involved.", paramType = "query"),
    @ApiImplicitParam(name = "suspended", dataType = "boolean", value = "If true, only return process instance which are suspended. If false, only return process instances which are not suspended (active).", paramType = "query"),
    @ApiImplicitParam(name = "superProcessInstanceId", dataType = "string", value = "Only return process instances which have the given super process-instance id (for processes that have a call-activities).", paramType = "query"),
    @ApiImplicitParam(name = "subProcessInstanceId", dataType = "string", value = "Only return process instances which have the given sub process-instance id (for processes started as a call-activity).", paramType = "query"),
    @ApiImplicitParam(name = "excludeSubprocesses", dataType = "boolean", value = "Return only process instances which aren’t sub processes.", paramType = "query"),
    @ApiImplicitParam(name = "includeProcessVariables", dataType = "boolean", value = "Indication to include process variables in the result.", paramType = "query"),
    @ApiImplicitParam(name = "tenantId", dataType = "string", value = "Only return process instances with the given tenantId.", paramType = "query"),
    @ApiImplicitParam(name = "tenantIdLike", dataType = "string", value = "Only return process instances with a tenantId like the given value.", paramType = "query"),
    @ApiImplicitParam(name = "withoutTenantId", dataType = "boolean", value = "If true, only returns process instances without a tenantId set. If false, the withoutTenantId parameter is ignored.", paramType = "query"),
    @ApiImplicitParam(name = "sort", dataType = "string", value = "Property to sort on, to be used together with the order.", allowableValues ="id,processDefinitionId,tenantId,processDefinitionKey", paramType = "query"),
  })
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Indicates request was successful and the process-instances are returned"),
      @ApiResponse(code = 404, message = "Indicates a parameter was passed in the wrong format . The status-message contains additional information.")
  })
  @RequestMapping(value = "/runtime/process-instances", method = RequestMethod.GET, produces = "application/json")
  public DataResponse getProcessInstances(@ApiParam(hidden = true) @RequestParam Map<String, String> allRequestParams, HttpServletRequest request) {
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


  @ApiOperation(value = "Start a process instance", tags = {"Process Instances"},
      notes= "## Request body (start by process definition id)\n\n"
          + "```JSON\n {\n" + "   \"processDefinitionId\":\"oneTaskProcess:1:158\",\n" + "   \"businessKey\":\"myBusinessKey\",\n" + "   \"variables\": [\n"
          + "      {\n" + "        \"name\":\"myVar\",\n" + "        \"value\":\"This is a variable\",\n" + "      }\n" + "   ]\n" + "}```"
          + "\n\n"
          + "## Request body (start by process definition key)\n\n"
          + "```JSON\n {\n" + "   \"processDefinitionKey\":\"oneTaskProcess\",\n" + "   \"businessKey\":\"myBusinessKey\",\n" + "   \"tenantId\": \"tenant1\",\n"
          + "   \"variables\": [\n" + "      {\n" + "        \"name\":\"myVar\",\n" + "        \"value\":\"This is a variable\",\n" + "      }\n"
          + "   ]\n" + "}```"
          + "\n\n"
          + "## Request body (start by message)\n\n"
          + "```JSON\n{\n" + "   \"message\":\"newOrderMessage\",\n" + "   \"businessKey\":\"myBusinessKey\",\n" + "   \"tenantId\": \"tenant1\",\n"
          + "   \"variables\": [\n" + "      {\n" + "        \"name\":\"myVar\",\n" + "        \"value\":\"This is a variable\",\n" + "      }\n"
          + "   ]\n" + "}```"
          + "\n\n"
          + "Note that also a *transientVariables* property is accepted as part of this json, that follows the same structure as the *variables* property.\n\n"
          + "Only one of *processDefinitionId*, *processDefinitionKey* or *message* can be used in the request body. "
          + "Parameters *businessKey*, *variables* and *tenantId* are optional. "
          + "If tenantId is omitted, the default tenant will be used. More information about the variable format can be found in the REST variables section. "
          + "Note that the variable-scope that is supplied is ignored, process-variables are always local."
      )
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Indicates the process instance was created."),
      @ApiResponse(code = 400, message = "Indicates either the process-definition was not found (based on id or key), no process is started by sending the given message or an invalid variable has been passed. Status description contains additional information about the error.")
  })
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

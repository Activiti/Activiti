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

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.runtime.Execution;
import org.activiti.rest.service.api.RestResponseFactory;
import org.activiti.rest.service.api.engine.variable.RestVariable;
import org.activiti.rest.service.api.engine.variable.RestVariable.RestVariableScope;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author Tijs Rademakers
 */
@RestController
public class ProcessInstanceVariableCollectionResource extends BaseVariableCollectionResource {
  
  @RequestMapping(value="/runtime/process-instances/{processInstanceId}/variables", method = RequestMethod.GET, produces="application/json")
  public List<RestVariable> getVariables(@PathVariable String processInstanceId, 
      @RequestParam(value="scope", required=false) String scope, HttpServletRequest request) {
    
    Execution execution = getProcessInstanceFromRequest(processInstanceId);
    return processVariables(execution, scope, RestResponseFactory.VARIABLE_PROCESS);
  }
  
  @RequestMapping(value="/runtime/process-instances/{processInstanceId}/variables", method = RequestMethod.PUT, produces="application/json")
  public Object createOrUpdateExecutionVariable(@PathVariable String processInstanceId, 
      HttpServletRequest request, HttpServletResponse response) {
    
    Execution execution = getProcessInstanceFromRequest(processInstanceId);
    return createExecutionVariable(execution, true, RestResponseFactory.VARIABLE_PROCESS, request, response);
  }
  
  
  @RequestMapping(value="/runtime/process-instances/{processInstanceId}/variables", method = RequestMethod.POST, produces="application/json")
  public Object createExecutionVariable(@PathVariable String processInstanceId, 
      HttpServletRequest request, HttpServletResponse response) {
    
    Execution execution = getProcessInstanceFromRequest(processInstanceId);
    return createExecutionVariable(execution, false, RestResponseFactory.VARIABLE_PROCESS, request, response);
  }
  
  @RequestMapping(value="/runtime/process-instances/{processInstanceId}/variables", method = RequestMethod.DELETE)
  public void deleteLocalVariables(@PathVariable String processInstanceId, HttpServletResponse response) {
    Execution execution = getProcessInstanceFromRequest(processInstanceId);
    deleteAllLocalVariables(execution, response);
  }
  
  @Override
  protected void addGlobalVariables(Execution execution, int variableType, Map<String, RestVariable> variableMap) {
    // no global variables
  }

  //For process instance there's only one scope. Using the local variables method for that
  @Override
  protected void addLocalVariables(Execution execution, int variableType, Map<String, RestVariable> variableMap) {
    Map<String, Object> rawVariables = runtimeService.getVariables(execution.getId());
    List<RestVariable> globalVariables = restResponseFactory.createRestVariables(rawVariables, 
        execution.getId(), variableType, RestVariableScope.LOCAL);
    
    // Overlay global variables over local ones. In case they are present the values are not overridden, 
    // since local variables get precedence over global ones at all times.
    for (RestVariable var : globalVariables) {
      if (!variableMap.containsKey(var.getName())) {
        variableMap.put(var.getName(), var);
      }
    }
  }
}

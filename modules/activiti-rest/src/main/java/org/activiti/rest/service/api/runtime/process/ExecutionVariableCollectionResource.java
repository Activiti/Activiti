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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.runtime.Execution;
import org.activiti.rest.service.api.RestResponseFactory;
import org.activiti.rest.service.api.engine.variable.RestVariable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author Frederik Heremans
 */
@RestController
public class ExecutionVariableCollectionResource extends BaseVariableCollectionResource {

  @RequestMapping(value="/runtime/executions/{executionId}/variables", method = RequestMethod.GET, produces="application/json")
  public List<RestVariable> getVariables(@PathVariable String executionId, 
      @RequestParam(value="scope", required=false) String scope, HttpServletRequest request) {
    
    Execution execution = getExecutionFromRequest(executionId);
    return processVariables(execution, scope, RestResponseFactory.VARIABLE_EXECUTION);
  }
  
  @RequestMapping(value="/runtime/executions/{executionId}/variables", method = RequestMethod.PUT, produces="application/json")
  public Object createOrUpdateExecutionVariable(@PathVariable String executionId,
      HttpServletRequest request, HttpServletResponse response) {
    
    Execution execution = getExecutionFromRequest(executionId);
    return createExecutionVariable(execution, true, RestResponseFactory.VARIABLE_EXECUTION, request, response);
  }
  
  
  @RequestMapping(value="/runtime/executions/{executionId}/variables", method = RequestMethod.POST, produces="application/json")
  public Object createExecutionVariable(@PathVariable String executionId,
      HttpServletRequest request, HttpServletResponse response) {
    
    Execution execution = getExecutionFromRequest(executionId);
  	return createExecutionVariable(execution, false, RestResponseFactory.VARIABLE_EXECUTION, request, response);
  }
  
  @RequestMapping(value="/runtime/executions/{executionId}/variables", method = RequestMethod.DELETE)
  public void deleteLocalVariables(@PathVariable String executionId, HttpServletResponse response) {
    Execution execution = getExecutionFromRequest(executionId);
    deleteAllLocalVariables(execution, response);
  }
  
}

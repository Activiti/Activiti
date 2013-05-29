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

package org.activiti.rest.api.process;

import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.engine.variable.RestVariable;
import org.activiti.rest.api.engine.variable.RestVariable.RestVariableScope;
import org.activiti.rest.application.ActivitiRestServicesApplication;


/**
 * @author Frederik Heremans
 */
public class ProcessInstanceVariableCollectionResource extends ExecutionVariableCollectionResource {

  /**
   * Get valid execution from request, in this case a {@link ProcessInstance}.
   */
  protected Execution getExecutionFromRequest() {
    String processInstanceId = getAttribute("processInstanceId");
    
    if (processInstanceId == null) {
      throw new ActivitiIllegalArgumentException("The processInstanceId cannot be null");
    }
    
    Execution execution = ActivitiUtil.getRuntimeService().createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
    if (execution == null) {
      throw new ActivitiObjectNotFoundException("Could not find a process instance with id '" + processInstanceId + "'.", ProcessInstance.class);
    }
    return execution;
  }
  
  protected void addGlobalVariables(Execution execution, Map<String, RestVariable> variableMap) {
    Map<String, Object> rawVariables = ActivitiUtil.getRuntimeService().getVariables(execution.getId());
    List<RestVariable> globalVariables = getApplication(ActivitiRestServicesApplication.class)
            .getRestResponseFactory().createRestVariables(this, rawVariables, null, null, execution.getId(), RestVariableScope.GLOBAL);
    
    // Overlay global variables over local ones. In case they are present the values are not overridden, 
    // since local variables get precedence over global ones at all times.
    for(RestVariable var : globalVariables) {
      if(!variableMap.containsKey(var.getName())) {
        variableMap.put(var.getName(), var);
      }
    }
  }

  
  protected void addLocalVariables(Execution execution, Map<String, RestVariable> variableMap) {
    Map<String, Object> rawLocalvariables = ActivitiUtil.getRuntimeService().getVariablesLocal(execution.getId());
    List<RestVariable> localVariables = getApplication(ActivitiRestServicesApplication.class)
            .getRestResponseFactory().createRestVariables(this, rawLocalvariables, null, null, execution.getId(), RestVariableScope.LOCAL);
    
    for(RestVariable var : localVariables) {
      variableMap.put(var.getName(), var);
    }
  }
  
  @Override
  protected boolean allowProcessInstanceUrl() {
    return true;
  }
}

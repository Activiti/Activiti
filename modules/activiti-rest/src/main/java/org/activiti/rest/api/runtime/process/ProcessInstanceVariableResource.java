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

package org.activiti.rest.api.runtime.process;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.rest.api.ActivitiUtil;
import org.activiti.rest.api.RestResponseFactory;
import org.activiti.rest.api.SecuredResource;
import org.activiti.rest.api.engine.variable.RestVariable;
import org.activiti.rest.api.engine.variable.RestVariable.RestVariableScope;
import org.activiti.rest.application.ActivitiRestServicesApplication;


/**
 * @author Frederik Heremans
 */
public class ProcessInstanceVariableResource extends ExecutionVariableResource {

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
  
  @Override
  protected RestVariable constructRestVariable(SecuredResource securedResource, String variableName, Object value, RestVariableScope variableScope,
          String executionId, boolean includeBinary) {
    return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
            .createRestVariable(this, variableName, value, null, executionId, RestResponseFactory.VARIABLE_PROCESS, includeBinary);
  }
  
  @Override
  protected String getExecutionIdParameter() {
    return "processInstanceId";
  }
  
  @Override
  protected boolean allowProcessInstanceUrl() {
    return true;
  }
}

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

package org.activiti.engine.impl.cmd;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.compatibility.Activiti5CompatibilityHandler;
import org.activiti.engine.impl.form.DefaultFormHandler;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.util.Activiti5Util;
import org.activiti.engine.impl.util.FormHandlerUtil;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;
import org.activiti.engine.repository.ProcessDefinition;

/**
 * Command for retrieving start or task form keys.
 * 
 * @author Falko Menge (camunda)
 */
public class GetFormKeyCmd implements Command<String> {

  protected String taskDefinitionKey;
  protected String processDefinitionId;

  /**
   * Retrieves a start form key.
   */
  public GetFormKeyCmd(String processDefinitionId) {
    setProcessDefinitionId(processDefinitionId);
  }

  /**
   * Retrieves a task form key.
   */
  public GetFormKeyCmd(String processDefinitionId, String taskDefinitionKey) {
    setProcessDefinitionId(processDefinitionId);
    if (taskDefinitionKey == null || taskDefinitionKey.length() < 1) {
      throw new ActivitiIllegalArgumentException("The task definition key is mandatory, but '" + taskDefinitionKey + "' has been provided.");
    }
    this.taskDefinitionKey = taskDefinitionKey;
  }

  protected void setProcessDefinitionId(String processDefinitionId) {
    if (processDefinitionId == null || processDefinitionId.length() < 1) {
      throw new ActivitiIllegalArgumentException("The process definition id is mandatory, but '" + processDefinitionId + "' has been provided.");
    }
    this.processDefinitionId = processDefinitionId;
  }

  public String execute(CommandContext commandContext) {
    ProcessDefinition processDefinition = ProcessDefinitionUtil.getProcessDefinition(processDefinitionId);
    
    if (commandContext.getProcessEngineConfiguration().isActiviti5CompatibilityEnabled() && 
        Activiti5CompatibilityHandler.ACTIVITI_5_ENGINE_TAG.equals(processDefinition.getEngineVersion())) {
      
      return Activiti5Util.getActiviti5CompatibilityHandler().getFormKey(processDefinitionId, taskDefinitionKey); 
    }
    
    DefaultFormHandler formHandler;
    if (taskDefinitionKey == null) {
      // TODO: Maybe add getFormKey() to FormHandler interface to avoid the following cast
      formHandler = (DefaultFormHandler) FormHandlerUtil.getStartFormHandler(commandContext, processDefinition); 
    } else {
      // TODO: Maybe add getFormKey() to FormHandler interface to avoid the following cast
      formHandler = (DefaultFormHandler) FormHandlerUtil.getTaskFormHandlder(processDefinitionId, taskDefinitionKey); 
    }
    String formKey = null;
    if (formHandler.getFormKey() != null) {
      formKey = formHandler.getFormKey().getExpressionText();
    }
    return formKey;
  }

}

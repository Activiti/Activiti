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

import java.io.Serializable;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.compatibility.Activiti5CompatibilityHandler;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.impl.form.FormEngine;
import org.activiti.engine.impl.form.StartFormHandler;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.util.Activiti5Util;
import org.activiti.engine.impl.util.FormHandlerUtil;
import org.activiti.engine.repository.ProcessDefinition;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class GetRenderedStartFormCmd implements Command<Object>, Serializable {

  private static final long serialVersionUID = 1L;
  protected String processDefinitionId;
  protected String formEngineName;

  public GetRenderedStartFormCmd(String processDefinitionId, String formEngineName) {
    this.processDefinitionId = processDefinitionId;
    this.formEngineName = formEngineName;
  }

  public Object execute(CommandContext commandContext) {
    ProcessDefinition processDefinition = commandContext.getProcessEngineConfiguration().getDeploymentManager().findDeployedProcessDefinitionById(processDefinitionId);
    
    if (commandContext.getProcessEngineConfiguration().isActiviti5CompatibilityEnabled() && 
        Activiti5CompatibilityHandler.ACTIVITI_5_ENGINE_TAG.equals(processDefinition.getEngineVersion())) {
      
      return Activiti5Util.getActiviti5CompatibilityHandler().getRenderedStartForm(processDefinitionId, formEngineName); 
    }
    
    if (processDefinition == null) {
      throw new ActivitiObjectNotFoundException("Process Definition '" + processDefinitionId + "' not found", ProcessDefinition.class);
    }
    StartFormHandler startFormHandler = FormHandlerUtil.getStartFormHandler(commandContext, processDefinition); 
    if (startFormHandler == null) {
      return null;
    }

    FormEngine formEngine = commandContext.getProcessEngineConfiguration().getFormEngines().get(formEngineName);

    if (formEngine == null) {
      throw new ActivitiException("No formEngine '" + formEngineName + "' defined process engine configuration");
    }

    StartFormData startForm = startFormHandler.createStartFormData(processDefinition);

    return formEngine.renderStartForm(startForm);
  }
}

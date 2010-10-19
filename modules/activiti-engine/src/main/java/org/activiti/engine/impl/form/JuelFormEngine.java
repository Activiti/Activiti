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
package org.activiti.engine.impl.form;

import org.activiti.engine.form.Form;
import org.activiti.engine.form.StartForm;
import org.activiti.engine.form.TaskForm;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.repository.ResourceEntity;
import org.activiti.engine.impl.scripting.ScriptingEngines;
import org.activiti.engine.impl.task.TaskEntity;


/**
 * @author Tom Baeyens
 */
public class JuelFormEngine implements FormEngine {
  
  public Object renderStartForm(StartForm startForm) {
    if (startForm.getFormKey()==null) {
      return null;
    }
    CommandContext commandContext = CommandContext.getCurrent();
    String formTemplateString = getFormTemplateString(startForm, commandContext);
    ScriptingEngines scriptingEngines = commandContext.getProcessEngineConfiguration().getScriptingEngines();
    return scriptingEngines.evaluate(formTemplateString, ScriptingEngines.DEFAULT_SCRIPTING_LANGUAGE, null);
  }

  public Object renderTaskForm(TaskForm taskForm) {
    if (taskForm.getFormKey()==null) {
      return null;
    }
    CommandContext commandContext = CommandContext.getCurrent();
    String formTemplateString = getFormTemplateString(taskForm, commandContext);
    ScriptingEngines scriptingEngines = commandContext.getProcessEngineConfiguration().getScriptingEngines();
    TaskEntity task = (TaskEntity) taskForm.getTask();
    return scriptingEngines.evaluate(formTemplateString, ScriptingEngines.DEFAULT_SCRIPTING_LANGUAGE, task.getExecution());
  }

  private String getFormTemplateString(Form formInstance, CommandContext commandContext) {
    String deploymentId = formInstance.getDeploymentId();
    String formKey = formInstance.getFormKey();
    
    ResourceEntity resourceStream = commandContext
      .getRepositorySession()
      .findResourceByDeploymentIdAndResourceName(deploymentId, formKey);
    
    byte[] resourceBytes = resourceStream.getBytes();
    String formTemplateString = new String(resourceBytes);
    return formTemplateString;
  }
}

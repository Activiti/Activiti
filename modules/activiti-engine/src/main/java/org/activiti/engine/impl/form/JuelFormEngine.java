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

import java.io.UnsupportedEncodingException;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.form.FormData;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ResourceEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.scripting.ScriptingEngines;


/**
 * @author Tom Baeyens
 */
public class JuelFormEngine implements FormEngine {

  public String getName() {
    return "juel";
  }

  public Object renderStartForm(StartFormData startForm) {
    if (startForm.getFormKey()==null) {
      return null;
    }
    String formTemplateString = getFormTemplateString(startForm, startForm.getFormKey());
    ScriptingEngines scriptingEngines = Context.getProcessEngineConfiguration().getScriptingEngines();
    return scriptingEngines.evaluate(formTemplateString, ScriptingEngines.DEFAULT_SCRIPTING_LANGUAGE, null);
  }

  public Object renderTaskForm(TaskFormData taskForm) {
    if (taskForm.getFormKey()==null) {
      return null;
    }
    String formTemplateString = getFormTemplateString(taskForm, taskForm.getFormKey());
    ScriptingEngines scriptingEngines = Context.getProcessEngineConfiguration().getScriptingEngines();
    TaskEntity task = (TaskEntity) taskForm.getTask();
    return scriptingEngines.evaluate(formTemplateString, ScriptingEngines.DEFAULT_SCRIPTING_LANGUAGE, task.getExecution());
  }

  protected String getFormTemplateString(FormData formInstance, String formKey) {
    String deploymentId = formInstance.getDeploymentId();
    
    ResourceEntity resourceStream = Context
      .getCommandContext()
      .getResourceEntityManager()
      .findResourceByDeploymentIdAndResourceName(deploymentId, formKey);
    
    if (resourceStream == null) {
      throw new ActivitiObjectNotFoundException("Form with formKey '"+formKey+"' does not exist", String.class);
    }
    
    byte[] resourceBytes = resourceStream.getBytes();
    String encoding = "UTF-8";
    String formTemplateString = "";
    try {
      formTemplateString = new String(resourceBytes, encoding);
    } catch (UnsupportedEncodingException e) {
      throw new ActivitiException("Unsupported encoding of :" + encoding, e);
    }
    return formTemplateString;
  }
}

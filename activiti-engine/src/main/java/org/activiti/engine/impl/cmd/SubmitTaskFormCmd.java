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

import java.util.Map;

import org.activiti.engine.compatibility.Activiti5CompatibilityHandler;
import org.activiti.engine.impl.form.TaskFormHandler;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.util.Activiti5Util;
import org.activiti.engine.impl.util.FormHandlerUtil;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class SubmitTaskFormCmd extends AbstractCompleteTaskCmd {

  private static final long serialVersionUID = 1L;

  protected String taskId;
  protected Map<String, String> properties;
  protected boolean completeTask;

  public SubmitTaskFormCmd(String taskId, Map<String, String> properties, boolean completeTask) {
    super(taskId);
    this.taskId = taskId;
    this.properties = properties;
    this.completeTask = completeTask;
  }

  protected Void execute(CommandContext commandContext, TaskEntity task) {
    
    // Backwards compatibility
    if (task.getProcessDefinitionId() != null) {
      if (Activiti5Util.isActiviti5ProcessDefinitionId(commandContext, task.getProcessDefinitionId())) {
        Activiti5CompatibilityHandler activiti5CompatibilityHandler = Activiti5Util.getActiviti5CompatibilityHandler(); 
        activiti5CompatibilityHandler.submitTaskFormData(taskId, properties, completeTask);
        return null;
      }
    }
    
    commandContext.getHistoryManager().recordFormPropertiesSubmitted(task.getExecution(), properties, taskId);
    
    TaskFormHandler taskFormHandler = FormHandlerUtil.getTaskFormHandlder(task);

    if (taskFormHandler != null) {
      taskFormHandler.submitFormProperties(properties, task.getExecution());

      if (completeTask) {
        executeTaskComplete(commandContext, task, null, false);
      }

    }
    
    return null;
  }

  @Override
  protected String getSuspendedTaskException() {
    return "Cannot submit a form to a suspended task";
  }

}

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
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.impl.form.FormEngine;
import org.activiti.engine.impl.form.TaskFormHandler;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.task.Task;


/**
 * @author Tom Baeyens
 */
public class GetRenderedTaskFormCmd  implements Command<Object>, Serializable {

  private static final long serialVersionUID = 1L;
  protected String taskId;
  protected String formEngineName;
  
  public GetRenderedTaskFormCmd(String taskId, String formEngineName) {
    this.taskId = taskId;
    this.formEngineName = formEngineName;
  }


  public Object execute(CommandContext commandContext) {
    TaskEntity task = commandContext
      .getTaskEntityManager()
      .findTaskById(taskId);
    if (task == null) {
      throw new ActivitiObjectNotFoundException("Task '" + taskId +"' not found", Task.class);
    }
    
    if (task.getTaskDefinition() == null) {
      throw new ActivitiException("Task form definition for '" + taskId +"' not found");
    }
    
    TaskFormHandler taskFormHandler = task.getTaskDefinition().getTaskFormHandler();
    if (taskFormHandler == null) {
      return null;
    }
    
    FormEngine formEngine = commandContext
      .getProcessEngineConfiguration()
      .getFormEngines()
      .get(formEngineName);
    
    if (formEngine==null) {
      throw new ActivitiException("No formEngine '" + formEngineName +"' defined process engine configuration");
    }
    
    TaskFormData taskForm = taskFormHandler.createTaskForm(task);
    
    return formEngine.renderTaskForm(taskForm);
  }
}

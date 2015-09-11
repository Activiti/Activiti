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
import org.activiti.engine.impl.form.TaskFormHandler;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.util.FormHandlerUtil;
import org.activiti.engine.task.Task;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class GetTaskFormCmd implements Command<TaskFormData>, Serializable {

  private static final long serialVersionUID = 1L;
  protected String taskId;

  public GetTaskFormCmd(String taskId) {
    this.taskId = taskId;
  }

  public TaskFormData execute(CommandContext commandContext) {
    TaskEntity task = commandContext.getTaskEntityManager().findById(taskId);
    if (task == null) {
      throw new ActivitiObjectNotFoundException("No task found for taskId '" + taskId + "'", Task.class);
    }
    
    TaskFormHandler taskFormHandler = FormHandlerUtil.getTaskFormHandlder(task);
    if (taskFormHandler == null) {
      throw new ActivitiException("No taskFormHandler specified for task '" + taskId + "'");
    }

    return taskFormHandler.createTaskForm(task);
  }

}

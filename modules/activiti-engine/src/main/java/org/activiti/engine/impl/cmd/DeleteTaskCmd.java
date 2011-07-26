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
import java.util.Collection;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;


/**
 * @author Joram Barrez
 */
public class DeleteTaskCmd implements Command<Void>, Serializable {
  
  private static final long serialVersionUID = 1L;
  protected String taskId;
  protected Collection<String> taskIds;
  protected boolean cascade;
  
  public DeleteTaskCmd(String taskId, boolean cascade) {
    this.taskId = taskId;
    this.cascade = cascade;
  }
  
  public DeleteTaskCmd(Collection<String> taskIds, boolean cascade) {
    this.taskIds = taskIds;
    this.cascade = cascade;
  }

  public Void execute(CommandContext commandContext) {
    if (taskId != null) {
      deleteTask(taskId);
    } else if (taskIds != null) {
        for (String taskId : taskIds) {
          deleteTask(taskId);
        }   
    } else {
      throw new ActivitiException("taskId and taskIds are null");
    }
    
    
    return null;
  }

  protected void deleteTask(String taskId) {
    Context
      .getCommandContext()
      .getTaskManager()
      .deleteTask(taskId, cascade);
  }
}

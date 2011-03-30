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

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.cfg.TaskSession;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.task.TaskEntity;


/**
 * @author Tom Baeyens
 */
public class DelegateTaskCmd implements Command<Object> {

  protected String taskId;
  protected String userId;
  
  public DelegateTaskCmd(String taskId, String userId) {
    this.taskId = taskId;
    this.userId = userId;
  }

  public Object execute(CommandContext commandContext) {
    if(taskId == null) {
      throw new ActivitiException("taskId is null");
    }
    
    TaskSession taskSession = commandContext.getTaskSession();
    TaskEntity task = taskSession.findTaskById(taskId);
    if (task == null) {
      throw new ActivitiException("Cannot find task with id " + taskId);
    }

    task.delegate(userId);
    
    return null;
  }

}

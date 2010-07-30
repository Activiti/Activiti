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
import org.activiti.impl.interceptor.CommandContext;
import org.activiti.impl.persistence.PersistenceSession;
import org.activiti.impl.task.TaskImpl;


/**
 * @author Joram Barrez
 */
public class DeleteTaskCmd extends CmdVoid {
  
  protected String taskId;
  
  public DeleteTaskCmd(String taskId) {
    this.taskId = taskId;
  }

  public void executeVoid(CommandContext commandContext) {
    PersistenceSession persistenceSession = commandContext.getPersistenceSession();
    TaskImpl task = persistenceSession.findTask(taskId);
    if (task!=null) {
      task.delete();
    } else {
      throw new ActivitiException("Cannot find task with id " + taskId);
    }
  }
}

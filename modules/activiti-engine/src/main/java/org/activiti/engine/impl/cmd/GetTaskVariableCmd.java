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
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.TaskEntity;


/**
 * @author Tom Baeyens
 */
public class GetTaskVariableCmd implements Command<Object> {

  protected String taskId;
  protected String variableName;
  protected boolean isLocal;

  public GetTaskVariableCmd(String taskId, String variableName, boolean isLocal) {
    this.taskId = taskId;
    this.variableName = variableName;
    this.isLocal = isLocal;
  }

  public Object execute(CommandContext commandContext) {
    if(taskId == null) {
      throw new ActivitiException("taskId is null");
    }
    if(variableName == null) {
      throw new ActivitiException("variableName is null");
    }
    
    TaskEntity task = Context
      .getCommandContext()
      .getTaskManager()
      .findTaskById(taskId);
    
    if (task==null) {
      throw new ActivitiException("task "+taskId+" doesn't exist");
    }
    
    Object value;
    
    if (isLocal) {
      value = task.getVariableLocal(variableName);
    } else {
      value = task.getVariable(variableName);
    }
    
    return value;
  }
}

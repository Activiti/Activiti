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

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.TaskEntity;


/**
 * @author Tom Baeyens
 */
public class SetTaskVariablesCmd implements Command<Object> {

  protected String taskId;
  protected Map<String, ? extends Object> variables;
  protected boolean isLocal;
  
  public SetTaskVariablesCmd(String taskId, Map<String, ? extends Object> variables, boolean isLocal) {
    this.taskId = taskId;
    this.variables = variables;
    this.isLocal = isLocal;
  }

  public Object execute(CommandContext commandContext) {
    if(taskId == null) {
      throw new ActivitiException("taskId is null");
    }
    
    TaskEntity task = Context
      .getCommandContext()
      .getTaskManager()
      .findTaskById(taskId);
    
    if (task==null) {
      throw new ActivitiException("task "+taskId+" doesn't exist");
    }
    
    if (isLocal) {
      task.setVariablesLocal(variables);
    } else {
      task.setVariables(variables);
    }
    
    return null;
  }
}

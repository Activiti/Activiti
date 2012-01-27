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
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.SuspensionState;
import org.activiti.engine.impl.persistence.entity.SuspensionState.SuspensionStateUtil;

/**
 * 
 * @author Daniel Meyer
 */
public abstract class AbstractSetProcessInstanceStateCmd implements Command<Void> {
    
  protected final String executionId;
  

  public AbstractSetProcessInstanceStateCmd(String executionId) {
    this.executionId = executionId;
  }

  public Void execute(CommandContext commandContext) {
    
    if(executionId == null) {
      throw new ActivitiException("ProcessInstanceId cannot be null.");
    }
    
    ExecutionEntity executionEntity = commandContext.getExecutionManager()
      .findExecutionById(executionId);
    
    if(executionEntity == null) {
      throw new ActivitiException("Cannot find processInstance for id '"+executionId+"'.");
    }
    
    if(!executionEntity.isProcessInstance()) {
      throw new ActivitiException("Cannot set suspension state for execution '"+executionId+"': not a process instance.");
    }
    
    SuspensionStateUtil.setSuspensionState(executionEntity, getNewState());
    
    return null;
  }

  protected abstract SuspensionState getNewState();

}

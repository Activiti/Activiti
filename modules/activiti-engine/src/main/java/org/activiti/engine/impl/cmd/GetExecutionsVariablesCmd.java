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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.VariableInstance;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;

/**
 * @author Daisuke Yoshimoto
 */
public class GetExecutionsVariablesCmd implements Command<List<VariableInstance>>, Serializable{

  private static final long serialVersionUID = 1L;
  protected Set<String> executionIds;
  
  public GetExecutionsVariablesCmd(Set<String> executionIds) {
    this.executionIds = executionIds;
  }
  
  @Override
  public List<VariableInstance> execute(CommandContext commandContext) {
    // Verify existance of executions
    if(executionIds == null) {
      throw new ActivitiIllegalArgumentException("executionIds is null");
    }
    if(executionIds.isEmpty()){
        throw new ActivitiIllegalArgumentException("Set of executionIds is empty");
    }
    
    List<VariableInstance> instances = new ArrayList<VariableInstance>();
    List<VariableInstanceEntity> entities = commandContext.getVariableInstanceEntityManager().findVariableInstancesByExecutionIds(executionIds);
    for(VariableInstanceEntity entity : entities){
        entity.getValue();
        instances.add(entity);
    }
    return instances;
  }

}

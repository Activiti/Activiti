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

package org.activiti.engine.impl.persistence.runtime;

import java.util.List;

import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.variable.Type;


/**
 * @author Tom Baeyens
 */
public class ProcessInstanceVariableMap extends VariableInstanceMap {
  
  protected ProcessInstanceEntity processInstance;
  
  public ProcessInstanceVariableMap(ProcessInstanceEntity processInstance) {
    this.processInstance = processInstance;
  }

  @Override
  protected VariableInstanceEntity createVariableInstance(String variableName, Type type, Object value) {
    return VariableInstanceEntity.createAndInsert(variableName, type, value);
  }

  @Override
  protected List<VariableInstanceEntity> findVariableInstanceList() {
    return CommandContext
      .getCurrent()
      .getRuntimeSession()
      .findVariableInstancessByProcessInstanceId(processInstance.getId());
  }

}

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

import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.SuspensionState;
import org.activiti.engine.impl.persistence.entity.SuspensionState.SuspensionStateUtil;
import org.activiti.engine.runtime.ProcessInstance;

/**
 * @author Daniel Meyer
 * @author Joram Barrez
 */
public class ActivateProcessDefinitionCmd extends AbstractSetProcessDefinitionStateCmd {

  public ActivateProcessDefinitionCmd(String processDefinitionId, String processDefinitionKey) {
    super(processDefinitionId, processDefinitionKey);
  }
  
  public ActivateProcessDefinitionCmd(String processDefinitionId, String processDefinitionKey,
          boolean includeProcessInstances, int batchSize) {
  super(processDefinitionId, processDefinitionKey, includeProcessInstances, batchSize);
}

  protected void setState(ProcessDefinitionEntity processDefinitionEntity) {    
      SuspensionStateUtil.setSuspensionState(processDefinitionEntity, SuspensionState.ACTIVE);   
  }
  
  protected AbstractSetProcessInstanceStateCmd getProcessInstanceCmd(ProcessInstance processInstance) {
    return new ActivateProcessInstanceCmd(processInstance.getId());
  }
}

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

import java.util.Date;

import org.activiti.engine.impl.jobexecutor.TimerSuspendProcessDefinitionHandler;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.SuspensionState;
import org.activiti.engine.runtime.ProcessInstance;

/**
 * @author Daniel Meyer
 * @author Joram Barrez
 */
public class SuspendProcessDefinitionCmd extends AbstractSetProcessDefinitionStateCmd {
  
  public SuspendProcessDefinitionCmd(ProcessDefinitionEntity processDefinitionEntity, 
          boolean includeProcessInstances, Date executionDate, String tenantId) {
    super(processDefinitionEntity, includeProcessInstances, executionDate, tenantId);
  }

  public SuspendProcessDefinitionCmd(String processDefinitionId, String processDefinitionKey,
          boolean suspendProcessInstances, Date suspensionDate, String tenantId) {
    super(processDefinitionId, processDefinitionKey, suspendProcessInstances, suspensionDate, tenantId);
  }
  
  protected SuspensionState getProcessDefinitionSuspensionState() {
    return SuspensionState.SUSPENDED;
  }

  protected String getDelayedExecutionJobHandlerType() {
    return TimerSuspendProcessDefinitionHandler.TYPE;
  }
  
  protected AbstractSetProcessInstanceStateCmd getProcessInstanceChangeStateCmd(ProcessInstance processInstance) {
    return new SuspendProcessInstanceCmd(processInstance.getId());
  }
  
}

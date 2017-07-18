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

import org.activiti.engine.ActivitiException;
import org.activiti.engine.compatibility.Activiti5CompatibilityHandler;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.repository.ProcessDefinition;

/**

 */
public class IsActiviti5ProcessDefinitionCmd implements Command<Boolean>, Serializable {

  private static final long serialVersionUID = 1L;
  protected String processDefinitionId;

  public IsActiviti5ProcessDefinitionCmd(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  public Boolean execute(CommandContext commandContext) {
    if (!commandContext.getProcessEngineConfiguration().isActiviti5CompatibilityEnabled()) {
      return false;
    }
    
    ProcessDefinition processDefinition = commandContext.getProcessEngineConfiguration()
        .getDeploymentManager()
        .findDeployedProcessDefinitionById(processDefinitionId);
    
    if (processDefinition.getEngineVersion() != null) {
      if (Activiti5CompatibilityHandler.ACTIVITI_5_ENGINE_TAG.equals(processDefinition.getEngineVersion())) {
        if (commandContext.getProcessEngineConfiguration().isActiviti5CompatibilityEnabled()) {
          return true;
        }
      } else {
        throw new ActivitiException("Invalid 'engine' for process definition " + processDefinition.getId() + " : " + processDefinition.getEngineVersion());
      }
    }
    return false;
  }
}

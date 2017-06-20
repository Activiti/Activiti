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

import java.io.InputStream;
import java.io.Serializable;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.repository.ProcessDefinition;

/**
 * Gives access to a deployed process model, e.g., a BPMN 2.0 XML file, through a stream of bytes.
 * 

 */
public class GetDeploymentProcessModelCmd implements Command<InputStream>, Serializable {

  private static final long serialVersionUID = 1L;
  protected String processDefinitionId;

  public GetDeploymentProcessModelCmd(String processDefinitionId) {
    if (processDefinitionId == null || processDefinitionId.length() < 1) {
      throw new ActivitiIllegalArgumentException("The process definition id is mandatory, but '" + processDefinitionId + "' has been provided.");
    }
    this.processDefinitionId = processDefinitionId;
  }

  public InputStream execute(CommandContext commandContext) {
    ProcessDefinition processDefinition = commandContext.getProcessEngineConfiguration().getDeploymentManager().findDeployedProcessDefinitionById(processDefinitionId);
    String deploymentId = processDefinition.getDeploymentId();
    String resourceName = processDefinition.getResourceName();
    InputStream processModelStream = new GetDeploymentResourceCmd(deploymentId, resourceName).execute(commandContext);
    return processModelStream;
  }

}

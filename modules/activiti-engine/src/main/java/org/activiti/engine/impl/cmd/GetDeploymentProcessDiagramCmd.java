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
import java.util.logging.Logger;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;


/**
 * Gives access to a deployed process diagram, e.g., a PNG image, through a
 * stream of bytes.
 * 
 * @author Falko Menge
 */
public class GetDeploymentProcessDiagramCmd implements Command<InputStream>, Serializable {

  private static final long serialVersionUID = 1L;
  private static Logger log = Logger.getLogger(GetDeploymentProcessDiagramCmd.class.getName());
  
  protected String processDefinitionId;

  public GetDeploymentProcessDiagramCmd(String processDefinitionId) {
    if (processDefinitionId == null || processDefinitionId.length() < 1) {
      throw new ActivitiException("The process definition id is mandatory, but '" + processDefinitionId + "' has been provided.");
    }
    this.processDefinitionId = processDefinitionId;
  }

  public InputStream execute(CommandContext commandContext) {
    ProcessDefinitionEntity processDefinition = Context
            .getProcessEngineConfiguration()
            .getDeploymentCache()
            .findDeployedProcessDefinitionById(processDefinitionId);
    String deploymentId = processDefinition.getDeploymentId();
    String resourceName = processDefinition.getDiagramResourceName();
    if (resourceName == null ) {
      log.info("Resource name is null! No process diagram stream exists.");
      return null;
    } else {
      InputStream processDiagramStream =
              new GetDeploymentResourceCmd(deploymentId, resourceName)
              .execute(commandContext);
      return processDiagramStream;
    }
  }

}

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

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.bpmn.diagram.ProcessDiagramLayoutFactory;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.repository.DiagramLayout;


/**
 * Provides positions and dimensions of elements in a process diagram as
 * provided by {@link GetDeploymentProcessDiagramCmd}.
 *
 * This command requires a process model and a diagram image to be deployed.
 * @author Falko Menge
 */
public class GetDeploymentProcessDiagramLayoutCmd implements Command<DiagramLayout>, Serializable {

  private static final long serialVersionUID = 1L;
  protected String processDefinitionId;

  public GetDeploymentProcessDiagramLayoutCmd(String processDefinitionId) {
    if (processDefinitionId == null || processDefinitionId.length() < 1) {
      throw new ActivitiException("The process definition id is mandatory, but '" + processDefinitionId + "' has been provided.");
    }
    this.processDefinitionId = processDefinitionId;
  }

  public DiagramLayout execute(CommandContext commandContext) {
    InputStream processModelStream =
            new GetDeploymentProcessModelCmd(processDefinitionId)
            .execute(commandContext);
    InputStream processDiagramStream =
            new GetDeploymentProcessDiagramCmd(processDefinitionId)
            .execute(commandContext);
    return new ProcessDiagramLayoutFactory().getProcessDiagramLayout(processModelStream, processDiagramStream);
  }

}

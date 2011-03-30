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
package org.activiti.cycle.impl.connector.signavio.provider;

import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.cycle.Content;
import org.activiti.cycle.MimeType;
import org.activiti.cycle.RenderInfo;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.context.CycleApplicationContext;
import org.activiti.cycle.context.CycleSessionContext;
import org.activiti.cycle.impl.components.RuntimeConnectorList;
import org.activiti.cycle.impl.connector.signavio.SignavioConnectorConfiguration;
import org.activiti.cycle.impl.connector.signavio.SignavioConnectorInterface;
import org.activiti.cycle.impl.connector.signavio.action.ValidateActivitiDeployment;
import org.activiti.cycle.impl.mimetype.PngMimeType;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.bpmn.diagram.ProcessDiagramGenerator;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.repository.ProcessDefinitionEntity;

public abstract class AbstractPngProvider extends SignavioContentRepresentationProvider {

  private static final long serialVersionUID = 1L;
  
  private static Logger log = Logger.getLogger(AbstractPngProvider.class.getName());

  public Content getContent(RepositoryArtifact artifact) {
    try {
      SignavioConnectorInterface signavioConnector = (SignavioConnectorInterface) CycleSessionContext.get(RuntimeConnectorList.class).getConnectorById(
              artifact.getConnectorId());
      Content content = new Content();
      SignavioConnectorConfiguration configuration = (SignavioConnectorConfiguration) signavioConnector.getConfiguration();
      String modelAsPngUrl = configuration.getPngUrl(artifact.getNodeId(), signavioConnector.getSecurityToken());
      InputStream is = new URL(modelAsPngUrl).openStream();
      content.setValue(is);

      if (is.available() <= 201) { // 201 bytes is the missing PNG
        // The Signavio PNG is very often missing if the model was not yet saved
        // in the Modeler
        // so we use the Activiti PNG for the moment to have anything
        try {
          BpmnParse parse = ValidateActivitiDeployment.createParseObject(signavioConnector, artifact);
          try {
            parse.execute();
          }
          catch (ActivitiException ex) {
            // ignore parsing erros 
            // TODO: Think about it
          }
          if (parse.getProcessDefinitions().size()>0) {          
            // TODO: Only get the first pool (breaks for multiple pools!!)
            is = ProcessDiagramGenerator.generatePngDiagram(parse.getProcessDefinitions().get(0));
            content.setValue(is);
          }
        } catch (Exception ex) {
          log.log(Level.SEVERE, "Couldn't create PNG from BPMN 2.0 XML. Ignoring.", ex);
        }
      }

      return content;
    } catch (Exception ex) {
      throw new RepositoryException("Exception while accessing Signavio repository", ex);
    }
  }

  public String getId() {
    return "PNG";
  }

  public MimeType getRepresentationMimeType() {
    return CycleApplicationContext.get(PngMimeType.class);
  }

  public RenderInfo getRenderInfo() {
    return RenderInfo.IMAGE;
  }

  public boolean isForDownload() {
    return true;
  }

}

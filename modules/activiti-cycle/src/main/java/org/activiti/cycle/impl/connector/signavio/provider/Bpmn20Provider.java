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

import org.activiti.cycle.Content;
import org.activiti.cycle.MimeType;
import org.activiti.cycle.RenderInfo;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryArtifactType;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.components.RuntimeConnectorList;
import org.activiti.cycle.context.CycleApplicationContext;
import org.activiti.cycle.context.CycleContextType;
import org.activiti.cycle.context.CycleSessionContext;
import org.activiti.cycle.impl.connector.signavio.SignavioConnectorInterface;
import org.activiti.cycle.impl.connector.signavio.repositoryartifacttype.SignavioBpmn20ArtifactType;
import org.activiti.cycle.impl.mimetype.XmlMimeType;
import org.json.JSONObject;
import org.restlet.Response;

@CycleComponent(context = CycleContextType.APPLICATION)
public class Bpmn20Provider extends SignavioContentRepresentationProvider {

  private static final long serialVersionUID = 1L;

  public static final String NAME = "Raw BPMN 2.0";

  public Content getContent(RepositoryArtifact artifact) {
    try {
      SignavioConnectorInterface signavioConnector = (SignavioConnectorInterface) CycleSessionContext.get(RuntimeConnectorList.class).getConnectorById(artifact.getConnectorId());
      Content content = new Content();
      // use the bpmn2_0_serialization export servlet to provide bpmn20 xml
      // by doing this, we can support different signavio versions instead of
      // the commercial Signavio only
      Response jsonResponse = getJsonResponse(signavioConnector, artifact, "/json");
      JSONObject jsonData = new JSONObject(jsonResponse.getEntity().getText());
      String result = signavioConnector.transformJsonToBpmn20Xml(jsonData.toString());

      // This would have been the alternative that works only for signavio but
      // not for activiti-modeler:

      // Response jpdlResponse = getJsonResponse(artifact, "/bpmn2_0_xml");
      // DomRepresentation xmlData = jpdlResponse.getEntityAsDom();
      // String result = getXmlAsString(xmlData);

      // log.finest("BPMN 2.0 String: " + result);

      content.setValue(result);
      return content;
    } catch (Exception ex) {
      throw new RepositoryException("Error while accessing Signavio repository", ex);
    }
  }

  public String getId() {
    return NAME;
  }
  
  public MimeType getRepresentationMimeType() {
    return CycleApplicationContext.get(XmlMimeType.class);
  }

  public RenderInfo getRenderInfo() {
    return RenderInfo.CODE;
  }

  public RepositoryArtifactType getRepositoryArtifactType() {
    return CycleApplicationContext.get(SignavioBpmn20ArtifactType.class);
  }

}

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
import org.activiti.cycle.impl.connector.signavio.SignavioConnector;
import org.activiti.cycle.impl.connector.signavio.SignavioConnectorInterface;
import org.activiti.cycle.impl.connector.signavio.repositoryartifacttype.SignavioBpmn20ArtifactType;
import org.activiti.cycle.impl.mimetype.JsonMimeType;
import org.activiti.cycle.impl.mimetype.XmlMimeType;
import org.json.JSONObject;
import org.restlet.Response;

@CycleComponent(context = CycleContextType.APPLICATION)
public class JsonProvider extends SignavioContentRepresentationProvider {

  private static final long serialVersionUID = 1L;

  public Content getContent(RepositoryArtifact artifact) {
    try {
      SignavioConnectorInterface signavioConnector = (SignavioConnectorInterface) CycleSessionContext.get(RuntimeConnectorList.class).getConnectorById(artifact.getConnectorId());
      Content content = new Content();
      Response jsonResponse = getJsonResponse(signavioConnector, artifact, "/json");

      String jsonString = jsonResponse.getEntity().getText();
      JSONObject jsonObj = new JSONObject(jsonString);
      content.setValue(jsonObj.toString(2));
      return content;
    } catch (Exception ex) {
      throw new RepositoryException("Error while accessing Signavio repository", ex);
    }
  }

  public String getId() {
    return "JSON";
  }
  
  public MimeType getRepresentationMimeType() {
    return CycleApplicationContext.get(JsonMimeType.class);
  }

  public RenderInfo getRenderInfo() {
    return RenderInfo.CODE;
  }
  
  public boolean isForDownload() {
    return true;
  }

  public RepositoryArtifactType getRepositoryArtifactType() {
    return CycleApplicationContext.get(SignavioBpmn20ArtifactType.class);
  }

}

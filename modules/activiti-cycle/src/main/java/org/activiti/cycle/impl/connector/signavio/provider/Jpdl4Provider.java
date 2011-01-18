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
import org.activiti.cycle.context.CycleApplicationContext;
import org.activiti.cycle.context.CycleContextType;
import org.activiti.cycle.context.CycleSessionContext;
import org.activiti.cycle.impl.components.RuntimeConnectorList;
import org.activiti.cycle.impl.connector.signavio.SignavioConnectorInterface;
import org.activiti.cycle.impl.connector.signavio.repositoryartifacttype.SignavioJpdl4ArtifactType;
import org.activiti.cycle.impl.mimetype.XmlMimeType;
import org.activiti.cycle.impl.transform.XmlToTextTransformation;
import org.restlet.Response;
import org.restlet.ext.xml.DomRepresentation;

@CycleComponent(context = CycleContextType.APPLICATION)
public class Jpdl4Provider extends SignavioContentRepresentationProvider {

  private static final long serialVersionUID = 1L;

  public static final String NAME = "jpdl4";

  public Content getContent(RepositoryArtifact artifact) {
    try {
      SignavioConnectorInterface signavioConnector = (SignavioConnectorInterface) CycleSessionContext.get(RuntimeConnectorList.class).getConnectorById(artifact.getConnectorId());
      Content content = new Content();

      Response jpdlResponse = getJsonResponse(signavioConnector, artifact, "/jpdl4");
      DomRepresentation xmlData = new DomRepresentation(jpdlResponse.getEntity());
      XmlToTextTransformation transformation = CycleApplicationContext.get(XmlToTextTransformation.class);
      String jpdl4AsString = transformation.getXmlAsString(xmlData.getDomSource());
      // log.finest("JPDL4 String: " + jpdl4AsString);
      content.setValue(jpdl4AsString);
      return content;
    } catch (Exception ex) {
      throw new RepositoryException("Exception while accessing Signavio repository", ex);
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
  
  public boolean isForDownload() {
    return true;
  }

  public RepositoryArtifactType getRepositoryArtifactType() {
    return CycleApplicationContext.get(SignavioJpdl4ArtifactType.class);
  }

}

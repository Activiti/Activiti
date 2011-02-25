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
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.context.CycleApplicationContext;
import org.activiti.cycle.context.CycleContextType;
import org.activiti.cycle.context.CycleSessionContext;
import org.activiti.cycle.impl.components.RuntimeConnectorList;
import org.activiti.cycle.impl.connector.signavio.SignavioConnectorInterface;
import org.activiti.cycle.impl.connector.signavio.util.SignavioTransformationHelper;
import org.activiti.cycle.impl.connector.signavio.repositoryartifacttype.SignavioBpmn20ArtifactType;
import org.activiti.cycle.impl.connector.signavio.transform.pattern.RemedyTemporarySignavioIncompatibility;
import org.activiti.cycle.impl.mimetype.XmlMimeType;

@CycleComponent(context = CycleContextType.APPLICATION)
public class ActivitiCompliantBpmn20Provider extends SignavioContentRepresentationProvider {

  private static final long serialVersionUID = 1L;

  public static final String NAME = "Developer Friendly BPMN 2.0";

  public Content getContent(RepositoryArtifact artifact) {
    try {

      SignavioConnectorInterface signavioConnector = (SignavioConnectorInterface) CycleSessionContext.get(RuntimeConnectorList.class).getConnectorById(artifact.getConnectorId());
      Content content = new Content();

      String bpmnXml = createBpmnXml(signavioConnector, artifact);
      // log.finest("BPMN 2.0 String: " + bpmnXml);
      content.setValue(bpmnXml);
      return content;
    } catch (Exception ex) {
      throw new RepositoryException("Error while accessing Signavio repository", ex);
    }
  }

  public String getId() {
    return NAME;
  }

  public RenderInfo getRenderInfo() {
    return RenderInfo.CODE;
  }
  
  public MimeType getRepresentationMimeType() {
    return CycleApplicationContext.get(XmlMimeType.class);
  }

  public RepositoryArtifactType getRepositoryArtifactType() {
    return CycleApplicationContext.get(SignavioBpmn20ArtifactType.class);
  }
  
  public boolean isForDownload() {
    return true;
  }

  public static String createBpmnXml(RepositoryConnector connector, RepositoryArtifact artifact) {
    String sourceJson = getBpmn20Json(artifact);
    String transformedJson = SignavioTransformationHelper.applyJsonTransformations(sourceJson);
    String bpmnXml = transformToBpmn20((SignavioConnectorInterface) connector, transformedJson, artifact.getMetadata().getName());
    return bpmnXml;
  }

  public static String getBpmn20Json(RepositoryArtifact artifact) {
    return CycleApplicationContext.get(JsonProvider.class).getContent(artifact).asString();
  }

  public static String transformToBpmn20(SignavioConnectorInterface connector, String transformedJson, String name) {
    String bpmnXml = connector.transformJsonToBpmn20Xml(transformedJson);
    bpmnXml = new RemedyTemporarySignavioIncompatibility().transformBpmn20Xml(bpmnXml, name);
    return bpmnXml;
  }

}

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
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.impl.connector.signavio.SignavioConnector;
import org.activiti.cycle.impl.connector.signavio.SignavioPluginDefinition;
import org.activiti.cycle.impl.connector.signavio.util.SignavioTransformationHelper;
import org.activiti.cycle.impl.connector.signavio.transform.pattern.RemedyTemporarySignavioIncompatibility;

public class ActivitiCompliantBpmn20Provider extends SignavioContentRepresentationProvider {

  public static final String NAME = "Developer Friendly BPMN 2.0";

  public void addValueToContent(Content content, SignavioConnector connector, RepositoryArtifact artifact) {
    try {
      String bpmnXml = createBpmnXml(connector, artifact);
      log.finest("BPMN 2.0 String: " + bpmnXml);
      content.setValue(bpmnXml);
    } catch (Exception ex) {
      throw new RepositoryException("Error while accessing Signavio repository", ex);
    }
  }
  
  public static String createBpmnXml(RepositoryConnector connector, RepositoryArtifact artifact) {
    String sourceJson = getBpmn20Json((SignavioConnector) connector, artifact);
    String transformedJson = SignavioTransformationHelper.applyJsonTransformations(sourceJson);
    String bpmnXml = transformToBpmn20((SignavioConnector) connector, transformedJson, artifact.getMetadata().getName());
    return bpmnXml;
  }

  public static String getBpmn20Json(RepositoryConnector connector, RepositoryArtifact artifact) {
    return connector.getContent(artifact.getNodeId(), SignavioPluginDefinition.CONTENT_REPRESENTATION_ID_JSON).asString();
  }

  public static String transformToBpmn20(SignavioConnector connector, String transformedJson, String name) {
    String bpmnXml = connector.transformJsonToBpmn20Xml(transformedJson);
    bpmnXml = new RemedyTemporarySignavioIncompatibility().transformBpmn20Xml(bpmnXml, name);
    return bpmnXml;
  }

}

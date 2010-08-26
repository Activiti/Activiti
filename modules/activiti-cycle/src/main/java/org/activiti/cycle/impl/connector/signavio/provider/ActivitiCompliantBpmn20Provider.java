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
import org.activiti.cycle.ContentType;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.impl.transform.signavio.AdjustShapeNamesTransformation;
import org.activiti.cycle.impl.transform.signavio.BpmnPoolExtraction;
import org.activiti.cycle.impl.transform.signavio.ExchangeSignavioUuidWithNameTransformation;
import org.activiti.cycle.impl.transform.signavio.RemedyTemporarySignavioIncompatibilityTransformation;
import org.json.JSONObject;
import org.restlet.data.Response;

public class ActivitiCompliantBpmn20Provider extends SignavioContentRepresentationProvider {

  public static final String NAME = "Developer Friendly BPMN 2.0";

  public ActivitiCompliantBpmn20Provider() {
    super(NAME, ContentType.XML, true);
  }

  public void addValueToContent(Content content, RepositoryArtifact artifact) {
    try {
      // use the bpmn2_0_serialization export servlet to provide bpmn20 xml
      // by doing this, we can support different signavio versions instead of
      // the commercial Signavio only
      Response jsonResponse = getJsonResponse(artifact, "/json");
      JSONObject jsonData = new JSONObject(jsonResponse.getEntity().getText());

      jsonData = new BpmnPoolExtraction("Engine Pool").transform(jsonData);
      jsonData = new AdjustShapeNamesTransformation().transform(jsonData);
      jsonData = new ExchangeSignavioUuidWithNameTransformation().transform(jsonData);

      String bpmnXml = getConnector(artifact).transformJsonToBpmn20Xml(jsonData.toString());
      
      bpmnXml = new RemedyTemporarySignavioIncompatibilityTransformation().transformBpmn20Xml(bpmnXml);

      
      // This would have been the alternative that works only for signavio but
      // not for activiti-modeler:
      
      // Response jpdlResponse = getJsonResponse(artifact, "/bpmn2_0_xml");
      // DomRepresentation xmlData = jpdlResponse.getEntityAsDom();
      // String result = getXmlAsString(xmlData);

      log.finest("BPMN 2.0 String: " + bpmnXml);

      content.setValue(bpmnXml);
    } catch (Exception ex) {
      throw new RepositoryException("Error while accessing Signavio repository", ex);
    }
  }

}

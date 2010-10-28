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
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.impl.connector.signavio.SignavioConnector;
import org.restlet.Response;
import org.restlet.ext.xml.DomRepresentation;

public class Jpdl4Provider extends SignavioContentRepresentationProvider {

  public static final String NAME = "jpdl4";

  @Override
  public void addValueToContent(Content content, SignavioConnector connector, RepositoryArtifact artifact) {
    try {
      Response jpdlResponse = getJsonResponse(connector, artifact, "/jpdl4");
      DomRepresentation xmlData = new DomRepresentation(jpdlResponse.getEntity());
      String jpdl4AsString = getXmlAsString(xmlData);
      log.finest("JPDL4 String: " + jpdl4AsString);
      content.setValue(jpdl4AsString);
    } catch (Exception ex) {
      throw new RepositoryException("Exception while accessing Signavio repository", ex);
    }
  }

}

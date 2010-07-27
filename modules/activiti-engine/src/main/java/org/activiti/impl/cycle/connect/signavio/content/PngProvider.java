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
package org.activiti.impl.cycle.connect.signavio.content;

import org.activiti.impl.cycle.connect.signavio.SignavioConnector;

/**
 * 
 * @author bernd.ruecker@camunda.com
 */
public class PngProvider { // implements ContentLinkProvider {

  private final SignavioConnector connector;

  public PngProvider(SignavioConnector connector) {
    this.connector = connector;
  }

  // public void addToContentLinks(Map<String, ContentLink> contentLink,
  // RepositoryArtifact artifact) {
  // ContentLink link = new ContentLink();
  // link.setType(ContentRepresentationType.XML);
  // link.setName("jpdl4");
  // link.setSourceSystemUrl(connector.getSignavioConfiguration().getSignavioUrl()
  // + artifact.getLocalUrlPart() + "/png?token=" +
  // connector.getSecurityToken());
  //
  // // SignavioConnector connector = (SignavioConnector)
  // getFile().getConnector();
  // // String imageUrl = connector.getModelAsPngUrl(getFile());
  // // // TODO: Think about how to get this from the Signavio Connector by the
  // API in the best way
  // // return "<img src='" + imageUrl + "' />";
  // }
}

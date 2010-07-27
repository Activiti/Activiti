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

import org.activiti.impl.cycle.connect.signavio.SignavioConnectorConfiguration;

/**
 * @author bernd.ruecker@camunda.com
 */
public class JPdl4Provider { // implements ContentLinkProvider {

  private final SignavioConnectorConfiguration conf;

  public JPdl4Provider(SignavioConnectorConfiguration conf) {
    this.conf = conf;
  }

  // public void addToContentLinks(Map<String, ContentLink> contentLink,
  // RepositoryArtifact artifact) {
  // ContentLink link = new ContentLink();
  // link.setType(ContentRepresentationType.XML);
  // link.setName("jpdl4");
  // link.setSourceSystemUrl(conf.getSignavioUrl() + artifact.getLocalUrlPart()
  // + "/jpdl4");
  //
  // // SignavioConnector connector = (SignavioConnector)
  // // getFile().getConnector();
  // // String jpdl4 = connector.getModelAsJpdl4Representation(getFile());
  // // return jpdl4;
  // }


}

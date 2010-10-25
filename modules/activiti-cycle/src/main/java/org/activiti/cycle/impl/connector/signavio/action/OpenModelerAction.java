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
package org.activiti.cycle.impl.connector.signavio.action;

import java.net.MalformedURLException;
import java.net.URL;

import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.impl.CreateUrlActionImpl;
import org.activiti.cycle.impl.connector.signavio.SignavioConnector;

/**
 * Action to open the signavio modeler
 * 
 * @author bernd.ruecker@camunda.com
 */
public class OpenModelerAction extends CreateUrlActionImpl {

  private static final long serialVersionUID = 1L;
  
  public OpenModelerAction() {
    // TODO: remove when real labels are introduced in the GUI
    super("Open modeler action");
  }

  public URL getUrl(RepositoryConnector connector, RepositoryArtifact artifact) {
    try {
      return new URL(((SignavioConnector) connector).getConfiguration().getEditorUrl(artifact.getNodeId()));
    } catch (MalformedURLException ex) {
      throw new RepositoryException("Error while creating URL for opening Signavio modeler", ex);
    }
  }
  
}

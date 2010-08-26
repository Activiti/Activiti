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

import org.activiti.cycle.OpenUrlAction;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.impl.connector.signavio.SignavioConnectorConfiguration;

/**
 * Action to open the signavio modeler
 * 
 * @author bernd.ruecker@camunda.com
 */
public class OpenModelerAction extends OpenUrlAction {

  private String modelUrl;

  @Override
  public String getLabel() {
    return "open modeler";
  }
  
  @Override
  public void setArtifact(RepositoryArtifact artifact) {
    super.setArtifact(artifact);
    
    // save model URL immediately to have the original artifact id, not the
    // maybe changed one.
    // Now it is really time to rethink the Action creation and id overwriting
    // mechanism
    modelUrl = ((SignavioConnectorConfiguration) artifact.getOriginalConnector().getConfiguration()).getEditorUrl(artifact.getId());
  }

  @Override
  public URL getUrl() {
    try {
      return new URL(modelUrl);
    } catch (MalformedURLException ex) {
      throw new RepositoryException("Error while creating URL for opening Signavio modeler", ex);
    }
  }
  
}

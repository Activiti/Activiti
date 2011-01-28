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
import java.util.HashSet;
import java.util.Set;

import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryArtifactType;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.RepositoryException;
import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.context.CycleApplicationContext;
import org.activiti.cycle.context.CycleContextType;
import org.activiti.cycle.impl.action.CreateUrlActionImpl;
import org.activiti.cycle.impl.connector.signavio.SignavioConnectorConfiguration;
import org.activiti.cycle.impl.connector.signavio.SignavioConnectorInterface;
import org.activiti.cycle.impl.connector.signavio.repositoryartifacttype.SignavioBpmn20ArtifactType;

/**
 * Action to open the signavio modeler
 * 
 * @author bernd.ruecker@camunda.com
 */
@CycleComponent(context = CycleContextType.APPLICATION)
public class OpenModelerAction extends CreateUrlActionImpl {

  private static final long serialVersionUID = 1L;

  private Set<RepositoryArtifactType> types = new HashSet<RepositoryArtifactType>();

  public OpenModelerAction() {
    // TODO: remove when real labels are introduced in the GUI
    super("modeler");
    types.add(CycleApplicationContext.get(SignavioBpmn20ArtifactType.class));
  }

  public URL getUrl(RepositoryConnector connector, RepositoryArtifact artifact) {
    try {
      SignavioConnectorInterface signavioConnector = (SignavioConnectorInterface) connector;
      SignavioConnectorConfiguration configuration = (SignavioConnectorConfiguration) signavioConnector.getConfiguration();

      return new URL(configuration.getEditorUrl(artifact.getNodeId()));
    } catch (MalformedURLException ex) {
      throw new RepositoryException("Error while creating URL for opening Signavio modeler", ex);
    }
  }

  public Set<RepositoryArtifactType> getArtifactTypes() {
    return types;
  }

}

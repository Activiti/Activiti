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
import org.activiti.cycle.impl.connector.signavio.SignavioConnector;
import org.activiti.cycle.impl.connector.signavio.util.MoviBuilder;


public class MoviProvider extends SignavioContentRepresentationProvider {
  
  @Override
  public void addValueToContent(Content content, SignavioConnector connector, RepositoryArtifact artifact) {
    content.setValue(new MoviBuilder(connector.getConfiguration().getModelUrl(artifact.getId())).toString());
//    content.setValue("http://oryx-project.org/backend/poem/model/8842");
  }

}

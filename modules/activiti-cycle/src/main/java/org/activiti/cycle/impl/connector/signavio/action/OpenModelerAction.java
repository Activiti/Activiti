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

import java.util.Map;

import org.activiti.cycle.ArtifactAction;
import org.activiti.cycle.RepositoryNode;
import org.activiti.cycle.impl.connector.signavio.SignavioConnector;

/**
 * TODO: Wie die URL zurueckgeben um das in der GUI zu verwursten?
 * 
 * @author bernd.ruecker@camunda.com
 * @author christian.lipphardt@camunda.com
 */
public class OpenModelerAction extends ArtifactAction {

  @Override
  public void execute() {
  }

  @Override
  public void execute(RepositoryNode itemInfo) {
  }

  @Override
  public void execute(RepositoryNode itemInfo, Map<String, Object> param) {
  }

  @Override
  public String getGuiRepresentation() {
    return "OWN_WINDOW";
  }

  @Override
  public String getGuiRepresentationUrl() {
    SignavioConnector signavioConnector = (SignavioConnector) getFile().getConnector();
    return signavioConnector.getModellerUrl(getFile());
    // TODO: Think about how to get this from the Signavio Connector by the API in the best way
  }

  @Override
  public String getName() {
    return "open modeller";
  }

}

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
package org.activiti.impl.cycle.connect.signavio.actions;

import java.util.Map;

import org.activiti.impl.cycle.connect.api.RepositoryNode;
import org.activiti.impl.cycle.connect.api.actions.FileAction;
import org.activiti.impl.cycle.connect.api.actions.FileActionGuiRepresentation;

/**
 * @author bernd.ruecker@camunda.com
 * @author christian.lipphardt@camunda.com
 */
public class ShowjPdl4Action extends FileAction {

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
  public FileActionGuiRepresentation getGuiRepresentation() {
    return FileActionGuiRepresentation.DEFAULT_PANE;
  }

  @Override
  public String getGuiRepresentationContent() {
    return null;
    // TODO: Implement
    
    // SignavioConnector connector = (SignavioConnector)
    // getFile().getConnector();
    // String jpdl4 = connector.getModelAsJpdl4Representation(getFile());
    // return jpdl4;
  }

  @Override
  public String getName() {
    return "Show jPDL 4";
  }

  @Override
  public String getGuiRepresentationAsString() {
    return FileActionGuiRepresentation.DEFAULT_PANE.toString();
  }


}

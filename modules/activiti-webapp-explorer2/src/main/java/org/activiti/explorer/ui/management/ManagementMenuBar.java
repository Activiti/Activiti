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
package org.activiti.explorer.ui.management;

import org.activiti.explorer.ExplorerApplication;
import org.activiti.explorer.ui.management.db.DatabasePage;
import org.activiti.explorer.ui.management.deployment.DeploymentPage;
import org.activiti.explorer.ui.management.deployment.NewDeploymentListener;

import com.vaadin.ui.MenuBar;

/**
 * @author Joram Barrez
 */
public class ManagementMenuBar extends MenuBar {

  private static final long serialVersionUID = 529403088210949174L;
  
  public ManagementMenuBar() {
    setWidth("100%");
    
    // Database
    MenuItem databaseItem = addItem("Database", new Command() {
      public void menuSelected(MenuItem selectedItem) {
        ExplorerApplication.getCurrent().switchView(new DatabasePage());
      }
    });
    
    // Deployments
    MenuItem deploymentsItem = addItem("Deployments", null);
    
    deploymentsItem.addItem("Show all", new Command() {
      public void menuSelected(MenuItem selectedItem) {
        ExplorerApplication.getCurrent().switchView(new DeploymentPage());
      }
    });
    deploymentsItem.addItem("Upload new", new NewDeploymentListener());
    
  }
  
}

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

package org.activiti.explorer.ui.alfresco;

import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.custom.ToolbarEntry.ToolbarCommand;
import org.activiti.explorer.ui.management.ManagementMenuBar;


/**
 * @author Joram Barrez
 */
public class AlfrescoManagementMenuBar extends ManagementMenuBar {

  private static final long serialVersionUID = 1L;
  
  public static final String ENTRY_PROCESS_DEFINITIONS = "processDefinitions"; 
  public static final String ENTRY_PROCESS_INSTANCES = "processInstances";

  protected void initToolbarEntries() {
    addDeploymentsToolbarEntry();
    addJobsToolbarEntry();
    addProcessDefinitionsEntry();
    addProcessInstancesEntry();
    addDatabaseToolbarEntry();
  }
  
  protected void addProcessDefinitionsEntry() {
    addToolbarEntry(ENTRY_PROCESS_DEFINITIONS, i18nManager.getMessage(Messages.PROCESS_MENU_DEPLOYED_DEFINITIONS), new ToolbarCommand() {
      public void toolBarItemSelected() {
        viewManager.showDeployedProcessDefinitionPage();
      }
    });
  }
  
  protected void addProcessInstancesEntry() {
    addToolbarEntry(ENTRY_PROCESS_INSTANCES, i18nManager.getMessage(Messages.PROCESS_MENU_INSTANCES), new ToolbarCommand() {
      public void toolBarItemSelected() {
        viewManager.showProcessInstancePage();
      }
    });
  }

}

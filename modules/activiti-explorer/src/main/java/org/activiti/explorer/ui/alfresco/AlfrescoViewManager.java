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

import org.activiti.explorer.DefaultViewManager;
import org.activiti.explorer.ViewManager;
import org.activiti.explorer.ui.management.processinstance.ProcessInstancePage;


/**
 * @author Joram Barrez
 */
public class AlfrescoViewManager extends DefaultViewManager {
  
  public void showDefaultPage() {
    mainWindow.showDefaultContent();
    showDeploymentPage();
  }
  
  @Override
  public void showDeployedProcessDefinitionPage() {
    switchView(new AlfrescoProcessDefinitionPage(), ViewManager.MAIN_NAVIGATION_PROCESS, AlfrescoManagementMenuBar.ENTRY_PROCESS_DEFINITIONS);
  }
  
  @Override
  public void showDeployedProcessDefinitionPage(String processDefinitionId) {
    switchView(new AlfrescoProcessDefinitionPage(processDefinitionId), ViewManager.MAIN_NAVIGATION_PROCESS, AlfrescoManagementMenuBar.ENTRY_PROCESS_DEFINITIONS);
  }
  
  @Override
  public void showProcessInstancePage() {
    switchView(new ProcessInstancePage(), ViewManager.MAIN_NAVIGATION_MANAGE, AlfrescoManagementMenuBar.ENTRY_PROCESS_INSTANCES);
  }
  
  @Override
  public void showProcessInstancePage(String processInstanceId) {
    switchView(new ProcessInstancePage(), ViewManager.MAIN_NAVIGATION_MANAGE, AlfrescoManagementMenuBar.ENTRY_PROCESS_INSTANCES);
  }
  
}

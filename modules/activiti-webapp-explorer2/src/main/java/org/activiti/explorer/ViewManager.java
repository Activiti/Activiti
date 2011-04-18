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

package org.activiti.explorer;

import org.activiti.explorer.ui.AbstractPage;
import org.activiti.explorer.ui.MainWindow;
import org.activiti.explorer.ui.flow.FlowMenuBar;
import org.activiti.explorer.ui.flow.FlowPage;
import org.activiti.explorer.ui.flow.MyFlowsPage;
import org.activiti.explorer.ui.management.ManagementMenuBar;
import org.activiti.explorer.ui.management.db.DatabasePage;
import org.activiti.explorer.ui.management.deployment.DeploymentPage;
import org.activiti.explorer.ui.management.identity.GroupPage;
import org.activiti.explorer.ui.management.identity.UserPage;
import org.activiti.explorer.ui.management.job.JobPage;
import org.activiti.explorer.ui.profile.ProfilePopupWindow;
import org.activiti.explorer.ui.task.TaskInboxPage;
import org.activiti.explorer.ui.task.TaskMenuBar;
import org.activiti.explorer.ui.task.TaskQueuedPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.vaadin.ui.Window;


/**
 * @author Joram Barrez
 */
@Component
@Scope(value="session")
public class ViewManager {
  
  public static final String MAIN_NAVIGATION_TASKS = "tasks";
  public static final String MAIN_NAVIGATION_FLOWS = "flows";
  public static final String MAIN_NAVIGATION_MANAGE = "manage";
  public static final String MAIN_NAVIGATION_REPORTS = "reports";
  
  @Autowired
  protected MainWindow mainWindow;
  
  public ViewManager() {
  }
  
  public void showLoginPage() {
    mainWindow.showLoginPage();
  }
  
  public void showDefaultContent() {
    mainWindow.showDefaultContent();
  }
  
  public void showPopupWindow(Window window) {
    mainWindow.addWindow(window);
  }
  
  // Tasks
  
  public void showTaskInboxPage() {
    switchView(new TaskInboxPage(), MAIN_NAVIGATION_TASKS, TaskMenuBar.ENTRY_INBOX);
  }
  
  public void showTaskInboxPage(String taskId) {
    switchView(new TaskInboxPage(), MAIN_NAVIGATION_TASKS, TaskMenuBar.ENTRY_INBOX);
  }
  
  public void showTaskQueuedPage(String groupId) {
    switchView(new TaskQueuedPage(groupId), MAIN_NAVIGATION_TASKS, TaskMenuBar.ENTRY_QUEUED);
  }
  
  public void showTaskQueuedPage(String groupId, String taskId) {
    switchView(new TaskQueuedPage(groupId, taskId), MAIN_NAVIGATION_TASKS, TaskMenuBar.ENTRY_QUEUED);
  }
  
  // Flows
  
  public void showFlowPage() {
    switchView(new FlowPage(), MAIN_NAVIGATION_FLOWS, FlowMenuBar.ENTRY_LAUNCH_FLOWS);
  }
  
  public void showFlowPage(String processDefinitionId) {
    switchView(new FlowPage(processDefinitionId), MAIN_NAVIGATION_FLOWS, FlowMenuBar.ENTRY_LAUNCH_FLOWS);
  }
  
  public void showMyFlowsPage() {
    switchView(new MyFlowsPage(), MAIN_NAVIGATION_FLOWS, FlowMenuBar.ENTRY_MY_FLOWS);
  }
  
  public void showMyFlowsPage(String processInstanceId) {
    switchView(new MyFlowsPage(processInstanceId), MAIN_NAVIGATION_FLOWS, FlowMenuBar.ENTRY_MY_FLOWS);
  }
  
  // Management
  
  public void showDatabasePage() {
    switchView(new DatabasePage(), MAIN_NAVIGATION_MANAGE, ManagementMenuBar.ENTRY_DATABASE);
  }
  
  public void showDatabasePage(String tableName) {
    switchView(new DatabasePage(tableName), MAIN_NAVIGATION_MANAGE, ManagementMenuBar.ENTRY_DATABASE);
  }
  
  public void showDeploymentPage() {
    switchView(new DeploymentPage(), MAIN_NAVIGATION_MANAGE, ManagementMenuBar.ENTRY_DEPLOYMENTS);
  }
  
  public void showDeploymentPage(String deploymentId) {
    switchView(new DeploymentPage(deploymentId), MAIN_NAVIGATION_MANAGE, ManagementMenuBar.ENTRY_DEPLOYMENTS);
  }
  
  public void showJobPage() {
    switchView(new JobPage(), MAIN_NAVIGATION_MANAGE, ManagementMenuBar.ENTRY_JOBS);
  }
  
  public void showJobPage(String jobId) {
    switchView(new JobPage(jobId), MAIN_NAVIGATION_MANAGE, ManagementMenuBar.ENTRY_JOBS);
  }
  
  public void showUserPage() {
    switchView(new UserPage(), MAIN_NAVIGATION_MANAGE, ManagementMenuBar.ENTRY_USERS);
  }
  
  public void showUserPage(String userId) {
    switchView(new UserPage(userId), MAIN_NAVIGATION_MANAGE, ManagementMenuBar.ENTRY_USERS);
  }
  
  public void showGroupPage() {
    switchView(new GroupPage(), MAIN_NAVIGATION_MANAGE, ManagementMenuBar.ENTRY_GROUPS);
  }
  
  public void showGroupPage(String groupId) {
    switchView(new GroupPage(groupId), MAIN_NAVIGATION_MANAGE, ManagementMenuBar.ENTRY_GROUPS);
  }
  
  // Profile
  
  public void showProfilePopup(String userId) {
    showPopupWindow(new ProfilePopupWindow(userId));
  }
  
  // Helper
  
  protected void switchView(AbstractPage page, String mainMenuActive, String subMenuActive) {
    mainWindow.setMainNavigation(mainMenuActive);
    mainWindow.switchView(page);
    page.getToolBar().setActiveEntry(subMenuActive); // Must be set AFTER adding page to window (toolbar will be created in atach())
  }
  
}

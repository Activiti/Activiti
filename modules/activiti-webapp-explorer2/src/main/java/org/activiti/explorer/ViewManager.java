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

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
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
import org.activiti.explorer.ui.task.CasesPage;
import org.activiti.explorer.ui.task.InboxPage;
import org.activiti.explorer.ui.task.InvolvedPage;
import org.activiti.explorer.ui.task.QueuedPage;
import org.activiti.explorer.ui.task.TaskMenuBar;
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

  protected TaskService taskService;
  
  public ViewManager() {
    this.taskService = ProcessEngines.getDefaultProcessEngine().getTaskService();
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
  
  /**
   * Generic method which will figure out to which
   * task page must be jumped, based on the task data.
   * 
   * Note that, if possible, it is always more
   * performant to use the more specific showXXXPage() methods.
   */
  public void showTaskPage(String taskId) {
    Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
    String loggedInUserId = ExplorerApp.get().getLoggedInUser().getId();
    if (loggedInUserId.equals(task.getOwner())) {
      showCasesPage(taskId);
    } else if (loggedInUserId.equals(task.getAssignee())) {
      showInboxPage(taskId);
    } else if (taskService.createTaskQuery().taskInvolvedUser(loggedInUserId).count() == 1) {
      showInvolvedPage(taskId);
    } else {
      ExplorerApp.get().getNotificationManager().showErrorNotification(
              Messages.NAVIGATION_ERROR_NOT_INVOLVED_TITLE, 
              ExplorerApp.get().getI18nManager().getMessage(Messages.NAVIGATION_ERROR_NOT_INVOLVED, taskId));
    }
  }
  
  public void showCasesPage() {
    switchView(new CasesPage(), MAIN_NAVIGATION_TASKS, TaskMenuBar.ENTRY_CASES);
  }
  
  public void showCasesPage(String taskId) {
    switchView(new CasesPage(taskId), MAIN_NAVIGATION_TASKS, TaskMenuBar.ENTRY_CASES);
  }
  
  public void showInboxPage() {
    switchView(new InboxPage(), MAIN_NAVIGATION_TASKS, TaskMenuBar.ENTRY_INBOX);
  }
  
  public void showInboxPage(String taskId) {
    switchView(new InboxPage(taskId), MAIN_NAVIGATION_TASKS, TaskMenuBar.ENTRY_INBOX);
  }
  
  public void showQueuedPage(String groupId) {
    switchView(new QueuedPage(groupId), MAIN_NAVIGATION_TASKS, TaskMenuBar.ENTRY_QUEUED);
  }
  
  public void showQueuedPage(String groupId, String taskId) {
    switchView(new QueuedPage(groupId, taskId), MAIN_NAVIGATION_TASKS, TaskMenuBar.ENTRY_QUEUED);
  }
  
  public void showInvolvedPage() {
    switchView(new InvolvedPage(), MAIN_NAVIGATION_TASKS, TaskMenuBar.ENTRY_INVOLVED);
  }
  
  public void showInvolvedPage(String taskId) {
    switchView(new InvolvedPage(taskId), MAIN_NAVIGATION_TASKS, TaskMenuBar.ENTRY_INVOLVED);
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

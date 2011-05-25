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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.TaskService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.activiti.explorer.ui.AbstractTablePage;
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
import org.activiti.explorer.ui.task.ArchivedPage;
import org.activiti.explorer.ui.task.CasesPage;
import org.activiti.explorer.ui.task.InboxPage;
import org.activiti.explorer.ui.task.InvolvedPage;
import org.activiti.explorer.ui.task.QueuedPage;
import org.activiti.explorer.ui.task.TaskMenuBar;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.ui.Window;


/**
 * @author Joram Barrez
 */
public class ViewManager implements Serializable {
  
  private static final long serialVersionUID = 1L;
  public static final String MAIN_NAVIGATION_TASKS = "tasks";
  public static final String MAIN_NAVIGATION_FLOWS = "flows";
  public static final String MAIN_NAVIGATION_MANAGE = "manage";
  public static final String MAIN_NAVIGATION_REPORTS = "reports";
  
  protected AbstractTablePage currentPage;
  
  @Autowired
  protected MainWindow mainWindow;

  protected TaskService taskService;
  protected HistoryService historyService;
  protected IdentityService identityService;
  
  public ViewManager() {
    this.taskService = ProcessEngines.getDefaultProcessEngine().getTaskService();
    this.historyService = ProcessEngines.getDefaultProcessEngine().getHistoryService();
    this.identityService = ProcessEngines.getDefaultProcessEngine().getIdentityService();
  }
  
  public void showLoginPage() {
    mainWindow.showLoginPage();
  }
  
  public void showDefaultContent() {
    mainWindow.showDefaultContent();
    showInboxPage();
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
    
    if (task == null) {
      // If no runtime task exists, our only hope is the archive page
      boolean isOwner = historyService.createHistoricTaskInstanceQuery()
        .taskId(taskId).taskOwner(loggedInUserId).count() == 1;
      if (isOwner) {
        showArchivedPage(taskId);
      } else {
        showNavigationError(taskId);
      }
    } else if (loggedInUserId.equals(task.getOwner())) {
      showCasesPage(taskId);
    } else if (loggedInUserId.equals(task.getAssignee())) {
      showInboxPage(taskId);
    } else if (taskService.createTaskQuery().taskInvolvedUser(loggedInUserId).count() == 1) {
      showInvolvedPage(taskId);
    } else {
      // queued
      List<String> groupIds = getGroupIds(loggedInUserId);
      List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(task.getId());
      Iterator<IdentityLink> identityLinkIterator = identityLinks.iterator();
        
      boolean pageFound = false;
      while (!pageFound && identityLinkIterator.hasNext()) {
        IdentityLink identityLink = identityLinkIterator.next();
        if (identityLink.getGroupId() != null && groupIds.contains(identityLink.getGroupId())) {
          showQueuedPage(identityLink.getGroupId(), task.getId());
          pageFound = true;
        }
      }
      
      // We've tried hard enough, the user now gets a notification. He deserves it.
      if (!pageFound) {
        showNavigationError(taskId);
      }
    }
  }

  protected List<String> getGroupIds(String userId) {
    List<String> groupIds = new ArrayList<String>();
    List<Group> groups = identityService.createGroupQuery().groupMember(userId).list();
    for (Group group : groups) {
      groupIds.add(group.getId());
    }
    
    return groupIds;
  }
  
  protected void showNavigationError(String taskId) {
    ExplorerApp.get().getNotificationManager().showErrorNotification(
            Messages.NAVIGATION_ERROR_NOT_INVOLVED_TITLE, 
            ExplorerApp.get().getI18nManager().getMessage(Messages.NAVIGATION_ERROR_NOT_INVOLVED, taskId));
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
  
  public void showArchivedPage() {
    switchView(new ArchivedPage(), MAIN_NAVIGATION_TASKS, TaskMenuBar.ENTRY_ARCHIVED);
  }
  
  public void showArchivedPage(String taskId) {
    switchView(new ArchivedPage(taskId), MAIN_NAVIGATION_TASKS, TaskMenuBar.ENTRY_ARCHIVED);
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
  
  // Repositories
  
  public void showRepositoryPage() {
    
  }
  
  // Profile
  
  public void showProfilePopup(String userId) {
    showPopupWindow(new ProfilePopupWindow(userId));
  }
  
  // Helper
  
  protected void switchView(AbstractTablePage page, String mainMenuActive, String subMenuActive) {
    currentPage = page;
    mainWindow.setMainNavigation(mainMenuActive);
    mainWindow.switchView(page);
    page.getToolBar().setActiveEntry(subMenuActive); // Must be set AFTER adding page to window (toolbar will be created in atach())
  }
  
  public AbstractTablePage getCurrentPage() {
    return currentPage;
  }
  
  public void setMainWindow(MainWindow mainWindow) {
    this.mainWindow = mainWindow;
  }
  
}

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

import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Event;
import org.activiti.explorer.ui.MainWindow;
import org.activiti.explorer.ui.flow.FlowPage;
import org.activiti.explorer.ui.flow.MyFlowsPage;
import org.activiti.explorer.ui.management.db.DatabasePage;
import org.activiti.explorer.ui.management.deployment.DeploymentPage;
import org.activiti.explorer.ui.management.job.JobPage;
import org.activiti.explorer.ui.profile.ProfilePanel;
import org.activiti.explorer.ui.profile.ProfilePopupWindow;
import org.activiti.explorer.ui.task.TaskCommentPopupWindow;
import org.activiti.explorer.ui.task.TaskInboxPage;
import org.activiti.explorer.ui.task.TaskQueuedPage;

import com.vaadin.ui.Window;


/**
 * @author Joram Barrez
 */
public class ViewManager {
  
  protected MainWindow mainWindow;
  
  public ViewManager(MainWindow mainWindow) {
    this.mainWindow = mainWindow;
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
    mainWindow.switchView(new TaskInboxPage());
  }
  
  public void showTaskInboxPage(String taskId) {
    mainWindow.switchView(new TaskInboxPage(taskId));
  }
  
  public void showTaskQueuedPage(String groupId) {
    mainWindow.switchView(new TaskQueuedPage(groupId));
  }
  
  public void showTaskQueuedPage(String groupId, String taskId) {
    mainWindow.switchView(new TaskQueuedPage(groupId, taskId));
  }
  
  public void showTaskCommentPopup(org.activiti.engine.task.Event comment) {
    showPopupWindow(new TaskCommentPopupWindow(comment));
  }
  
  // Flows
  
  public void showFlowPage() {
    mainWindow.switchView(new FlowPage());
  }
  
  public void showFlowPage(String processDefinitionId) {
    mainWindow.switchView(new FlowPage(processDefinitionId));
  }
  
  public void showMyFlowsPage() {
    mainWindow.switchView(new MyFlowsPage());
  }
  
  public void showMyFlowsPage(String processInstanceId) {
    mainWindow.switchView(new MyFlowsPage(processInstanceId));
  }
  
  // Management
  
  public void showDatabasePage() {
    mainWindow.switchView(new DatabasePage());
  }
  
  public void showDatabasePage(String tableName) {
    mainWindow.switchView(new DatabasePage(tableName));
  }
  
  public void showDeploymentPage() {
    mainWindow.switchView(new DeploymentPage());
  }
  
  public void showDeploymentPage(String deploymentId) {
    mainWindow.switchView(new DeploymentPage(deploymentId));
  }
  
  public void showJobPage() {
    mainWindow.switchView(new JobPage());
  }
  
  public void showJobPage(String jobId) {
    mainWindow.switchView(new JobPage(jobId));
  }
  
  // Profile
  
  public void showProfilePopup(String userId) {
    showPopupWindow(new ProfilePopupWindow(userId));
  }

}

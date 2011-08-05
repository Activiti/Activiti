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

import com.vaadin.ui.Window;


/**
 * @author Joram Barrez
 */
public interface ViewManager {
  
  static final String MAIN_NAVIGATION_TASK = "task";
  static final String MAIN_NAVIGATION_PROCESS = "process";
  static final String MAIN_NAVIGATION_MANAGE = "manage";
  static final String MAIN_NAVIGATION_REPORT = "report";
  
  // Generic
  
  void showLoginPage();
  
  void showDefaultPage();
  
  void showPopupWindow(Window window);
  
  // Tasks
  
  /**
   * Generic method which will figure out to which
   * task page must be jumped, based on the task data.
   * 
   * Note that, if possible, it is always more
   * performant to use the more specific showXXXPage() methods.
   */
  void showTaskPage(String taskId);
  
  void showTasksPage();
  
  void showTasksPage(String taskId);
  
  void showInboxPage();
  
  void showInboxPage(String taskId);
  
  void showQueuedPage(String groupId);
  
  void showQueuedPage(String groupId, String taskId);
  
  void showInvolvedPage();
  
  void showInvolvedPage(String taskId);
  
  void showArchivedPage();
  
  void showArchivedPage(String taskId);
  
  // Process
  
  void showProcessDefinitionPage();
  
  void showProcessDefinitionPage(String processDefinitionId);
  
  void showMyProcessInstancesPage();
  
  void showMyProcessInstancesPage(String processInstanceId);
  
  // Management
  
  void showDatabasePage();
  
  void showDatabasePage(String tableName);
  
  void showDeploymentPage();
  
  void showDeploymentPage(String deploymentId);
  
  void showJobPage();
  
  void showJobPage(String jobId);
  
  void showUserPage();
  
  void showUserPage(String userId);
  
  void showGroupPage();
  
  void showGroupPage(String groupId);
  
  void showProcessInstancePage();
  
  void showProcessInstancePage(String processInstanceId);
  
  // Profile
  
  void showProfilePopup(String userId);
  
}

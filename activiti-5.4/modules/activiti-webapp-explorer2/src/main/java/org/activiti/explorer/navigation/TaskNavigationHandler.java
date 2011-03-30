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

package org.activiti.explorer.navigation;

import org.activiti.explorer.ExplorerApplication;
import org.activiti.explorer.ui.task.TaskInboxPage;
import org.activiti.explorer.ui.task.TaskQueuedPage;

/**
 * @author Frederik Heremans
 */
public class TaskNavigationHandler implements NavigationHandler {

  public static final String TASK_URI_PART = "tasks";
  public static final String CATEGORY_INBOX = "inbox";
  public static final String CATEGORY_QUEUED = "queued";
  
  public static final String PARAMETER_CATEGORY = "category";
  public static final String PARAMETER_GROUP = "group";

  public String getTrigger() {
    return TASK_URI_PART;
  }

  public void handleNavigation(UriFragment uriFragment) {

    String category = uriFragment.getParameter(PARAMETER_CATEGORY);
    String taskId = uriFragment.getUriPart(1);
    
    if(CATEGORY_QUEUED.equals(category)) {
      showQueuedTasks(taskId, uriFragment);
    } else {
      // Default is the inbox
      showInbox(taskId, uriFragment);
    }
  }

  protected void showQueuedTasks(String taskId, UriFragment uriFragment) {
    String groupId = uriFragment.getParameter(PARAMETER_GROUP);
    if(groupId != null) {
      ExplorerApplication.getCurrent().switchView(new TaskQueuedPage(groupId, taskId));      
    } else {
      // When no group is available, just show the inbox
      showInbox(taskId, uriFragment);
      ExplorerApplication.getCurrent().showErrorNotification(
              "Cannot view queued tasks", "No groupId was provided, can't show queued tasks");
    }
  }

  protected void showInbox(String taskId, UriFragment uriFragment) {
    if (taskId != null) {
      ExplorerApplication.getCurrent().switchView(new TaskInboxPage(taskId));
    } else {
      ExplorerApplication.getCurrent().switchView(new TaskInboxPage());
    }
  }

}

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

/**
 * @author Frederik Heremans
 */
public class TaskNavigationHandler implements NavigationHandler {

  public static final String TASK_URI_PART = "tasks";
  public static final String CATEGORY_INBOX = "inbox";

  public String getTrigger() {
    return TASK_URI_PART;
  }

  public void handleNavigation(UriFragment uriFragment) {

    // String category = uriFragment.getParameter(CATEGORY_INBOX);
    String taskId = null;
    if (uriFragment.getUriParts().size() == 2) {
      taskId = uriFragment.getUriPart(1);
    }

    if (taskId != null) {
      ExplorerApplication.getCurrent().switchView(new TaskInboxPage(taskId));
    } else {
      ExplorerApplication.getCurrent().switchView(new TaskInboxPage());
    }

  }

}

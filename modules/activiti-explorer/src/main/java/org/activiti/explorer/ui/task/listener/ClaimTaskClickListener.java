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

package org.activiti.explorer.ui.task.listener;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.TaskService;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.NotificationManager;
import org.activiti.explorer.ViewManager;

import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;


/**
 * Listener that claims a task and opens it in the user's inbox.
 * 
 * @author Frederik Heremans
 */
public class ClaimTaskClickListener implements ClickListener {

  private static final long serialVersionUID = 6322369324898642379L;
  
  protected String taskId;

  protected transient TaskService taskService;
  protected I18nManager i18nManager;
  protected ViewManager viewManager;
  protected NotificationManager notificationManager;
  
  public ClaimTaskClickListener(String taskId, TaskService taskService) {
    this.taskId = taskId;
    this.taskService = taskService;
    this.viewManager = ExplorerApp.get().getViewManager();
    this.i18nManager = ExplorerApp.get().getI18nManager();
    this.notificationManager = ExplorerApp.get().getNotificationManager();
  }

  public void buttonClick(ClickEvent event) {
    try {
      taskService.claim(taskId, ExplorerApp.get().getLoggedInUser().getId());
      notificationManager.showInformationNotification(Messages.TASK_CLAIM_SUCCESS);
      viewManager.showInboxPage(taskId);
    } catch(ActivitiException ae) {
      notificationManager.showErrorNotification(Messages.TASK_CLAIM_FAILED, ae.getMessage());
    }
  }

}

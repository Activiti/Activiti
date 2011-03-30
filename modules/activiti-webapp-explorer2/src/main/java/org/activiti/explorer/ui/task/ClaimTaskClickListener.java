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

package org.activiti.explorer.ui.task;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.TaskService;
import org.activiti.explorer.ExplorerApplication;
import org.activiti.explorer.Messages;

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
  protected TaskService taskService;
  
  public ClaimTaskClickListener(String taskId, TaskService taskService) {
    this.taskId = taskId;
    this.taskService = taskService;
  }

  public void buttonClick(ClickEvent event) {
    ExplorerApplication app = ExplorerApplication.getCurrent();
    try {
      taskService.claim(taskId, ExplorerApplication.getCurrent().getLoggedInUser().getId());
      app.showInformationNotification(app.getMessage(Messages.TASK_CLAIM_SUCCESS));
      app.switchView(new TaskInboxPage(taskId));
    } catch(ActivitiException ae) {
      app.showErrorNotification(app.getMessage(Messages.TASK_CLAIM_FAILED), ae.getMessage());
    }
  }

}

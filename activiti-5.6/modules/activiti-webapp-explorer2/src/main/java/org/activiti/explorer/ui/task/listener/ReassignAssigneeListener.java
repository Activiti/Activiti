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

import java.util.Arrays;
import java.util.List;

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.task.Task;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.custom.SelectUsersPopupWindow;
import org.activiti.explorer.ui.event.SubmitEvent;
import org.activiti.explorer.ui.event.SubmitEventListener;
import org.activiti.explorer.ui.task.TaskDetailPanel;

import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;


/**
 * @author Joram Barrez
 */
public class ReassignAssigneeListener implements ClickListener {

  private static final long serialVersionUID = 1L;
  
  protected Task task;
  protected TaskDetailPanel taskDetailPanel;
  protected I18nManager i18nManager;
  
  public ReassignAssigneeListener(Task task, TaskDetailPanel taskDetailPanel) {
    this.task = task;
    this.taskDetailPanel = taskDetailPanel;
    this.i18nManager = ExplorerApp.get().getI18nManager();
  }
  
  public void buttonClick(ClickEvent event) {
    List<String> ignoredIds = null;
    if (task.getAssignee() != null) {
      ignoredIds = Arrays.asList(task.getAssignee());
    }
    
    final SelectUsersPopupWindow involvePeoplePopupWindow = 
        new SelectUsersPopupWindow(i18nManager.getMessage(Messages.TASK_ASSIGNEE_REASSIGN), false, ignoredIds);
    
    involvePeoplePopupWindow.addListener(new SubmitEventListener() {
      protected void submitted(SubmitEvent event) {
        // Update assignee
        String selectedUser = involvePeoplePopupWindow.getSelectedUserId();
        task.setAssignee(selectedUser);
        ProcessEngines.getDefaultProcessEngine().getTaskService().setAssignee(task.getId(), selectedUser);
        
        // Update UI
        taskDetailPanel.notifyAssigneeChanged();
      }
      protected void cancelled(SubmitEvent event) {
      }
    });
    
    ExplorerApp.get().getViewManager().showPopupWindow(involvePeoplePopupWindow);
  }
  
}

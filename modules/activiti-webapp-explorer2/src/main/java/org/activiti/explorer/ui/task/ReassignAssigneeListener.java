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

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.task.Task;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.event.SubmitEvent;
import org.activiti.explorer.ui.event.SubmitEventListener;

import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;


/**
 * @author Joram Barrez
 */
public class ReassignAssigneeListener implements ClickListener {

  private static final long serialVersionUID = 1L;
  
  protected Task task;
  protected TaskInvolvedPeopleComponent taskInvolvedPeopleComponent;
  protected I18nManager i18nManager;
  
  public ReassignAssigneeListener(Task task, TaskInvolvedPeopleComponent taskInvolvedPeopleComponent) { // changeAssigne == false -> changing owner
    this.task = task;
    this.taskInvolvedPeopleComponent = taskInvolvedPeopleComponent;
    this.i18nManager = ExplorerApp.get().getI18nManager();
  }
  
  public void buttonClick(ClickEvent event) {
    final InvolvePeoplePopupWindow involvePeoplePopupWindow = 
        new InvolvePeoplePopupWindow(i18nManager.getMessage(Messages.TASK_ASSIGNEE_REASSIGN), task, false);
    
    involvePeoplePopupWindow.addListener(new SubmitEventListener() {
      protected void submitted(SubmitEvent event) {
        // Update assignee
        String selectedUser = involvePeoplePopupWindow.getSelectedUserId();
        ProcessEngines.getDefaultProcessEngine().getTaskService().setAssignee(task.getId(), selectedUser);
        
        // Update UI
        taskInvolvedPeopleComponent.refreshAssignee();
      }
      protected void cancelled(SubmitEvent event) {
      }
    });
    
    ExplorerApp.get().getViewManager().showPopupWindow(involvePeoplePopupWindow);
  }
  
}

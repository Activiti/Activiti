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

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ViewManager;
import org.activiti.explorer.ui.custom.ConfirmationDialogPopupWindow;
import org.activiti.explorer.ui.event.ConfirmationEvent;
import org.activiti.explorer.ui.event.ConfirmationEventListener;
import org.activiti.explorer.ui.task.SubTaskComponent;

import com.vaadin.event.MouseEvents.ClickEvent;
import com.vaadin.event.MouseEvents.ClickListener;


/**
 * Listener attached to the delete icon for a subtask.
 * Used in the {@link SubTaskComponent}.
 * 
 * @author Joram Barrez
 */
public class DeleteSubTaskClickListener implements ClickListener {
  
  private static final long serialVersionUID = 1L;
  
  protected HistoricTaskInstance subTask;
  protected SubTaskComponent subTaskComponent;
  
  public DeleteSubTaskClickListener(HistoricTaskInstance subTask, SubTaskComponent subTaskComponent) {
    this.subTask = subTask;
    this.subTaskComponent = subTaskComponent;
  }

  public void click(ClickEvent event) {
    I18nManager i18nManager = ExplorerApp.get().getI18nManager();
    ViewManager viewManager = ExplorerApp.get().getViewManager();
    final TaskService taskService = ProcessEngines.getDefaultProcessEngine().getTaskService();
    
    ConfirmationDialogPopupWindow popup = new ConfirmationDialogPopupWindow(
            i18nManager.getMessage(Messages.TASK_CONFIRM_DELETE_SUBTASK, subTask.getName()));
    popup.addListener(new ConfirmationEventListener() {
      private static final long serialVersionUID = 1L;
      protected void rejected(ConfirmationEvent event) {
      }
      protected void confirmed(ConfirmationEvent event) {
        // delete subtask
        taskService.deleteTask(subTask.getId());
        
        // Refresh UI
        subTaskComponent.refreshSubTasks();
      }
    });
    
    viewManager.showPopupWindow(popup);
  }

}

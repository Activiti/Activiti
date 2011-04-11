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

import java.util.List;

import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.TaskService;
import org.activiti.engine.identity.Group;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.LoggedInUser;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ViewManager;

import com.vaadin.ui.MenuBar;



/**
 * @author Joram Barrez
 */
public class TaskMenuBar extends MenuBar {
  
  private static final long serialVersionUID = 7957488256766569264L;
  
  protected TaskService taskService;
  protected IdentityService identityService;
  protected ViewManager viewManager;
  protected I18nManager i18nManager;
  
  public TaskMenuBar() {
    this.taskService = ProcessEngines.getDefaultProcessEngine().getTaskService();
    this.identityService = ProcessEngines.getDefaultProcessEngine().getIdentityService();
    this.viewManager = ExplorerApp.get().getViewManager();
    this.i18nManager = ExplorerApp.get().getI18nManager();
    
    init();
  }
  
  protected void init() {
    setWidth("100%");
    
    // TODO: the counts should be done later by eg a Refresher component
    
    LoggedInUser user = ExplorerApp.get().getLoggedInUser();

    // Inbox
    long inboxCount = taskService.createTaskQuery().taskAssignee(user.getId()).count();
    addItem(i18nManager.getMessage(Messages.TASK_MENU_INBOX)+ "("+inboxCount+")", new Command() {
      public void menuSelected(MenuItem selectedItem) {
        viewManager.showTaskInboxPage();
      }
    });
    
    // Queued
    List<Group> groups = user.getGroups();
    MenuItem queuedItem = addItem(i18nManager.getMessage(Messages.TASK_MENU_QUEUED), null);
    long queuedCount = 0;
    for (final Group group : groups) {
      long groupCount = taskService.createTaskQuery().taskCandidateGroup(group.getId()).count();
      queuedCount += groupCount;
      queuedItem.addItem(group.getName() + " ("+groupCount+")", new Command() {
        public void menuSelected(MenuItem selectedItem) {
          viewManager.showTaskQueuedPage(group.getId());
        }
      });
    }
    queuedItem.setText(queuedItem.getText() + " ("+queuedCount+")");
  }
  
}

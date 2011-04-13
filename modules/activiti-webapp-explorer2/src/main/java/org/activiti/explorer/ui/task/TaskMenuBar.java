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
import org.activiti.explorer.ui.custom.ToolBar;
import org.activiti.explorer.ui.custom.ToolbarEntry;
import org.activiti.explorer.ui.custom.ToolbarEntry.ToolbarCommand;
import org.activiti.explorer.ui.custom.ToolbarPopupEntry;



/**
 * The menu bar which is shown when 'Tasks' is selected in the main menu.
 * 
 * @author Joram Barrez
 * @author Frederik Heremans
 */
public class TaskMenuBar extends ToolBar {
  
  private static final long serialVersionUID = 7957488256766569264L;
  
  public static final String ENTRY_INBOX = "inbox";
  public static final String ENTRY_QUEUED = "queued";
  
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
    
    ToolbarEntry inboxEntry = addToolbarEntry(ENTRY_INBOX, i18nManager.getMessage(Messages.TASK_MENU_INBOX), new ToolbarCommand() {
      public void toolBarItemSelected() {
        viewManager.showTaskInboxPage();
      }
    });
    inboxEntry.setCount(inboxCount);
    
    // Queued
    List<Group> groups = user.getGroups();
    ToolbarPopupEntry queuedItem = addPopupEntry(ENTRY_QUEUED, (i18nManager.getMessage(Messages.TASK_MENU_QUEUED)));
    long queuedCount = 0;
    for (final Group group : groups) {
      long groupCount = taskService.createTaskQuery().taskCandidateGroup(group.getId()).count();
      
      queuedItem.addMenuItem(group.getName() + " (" + groupCount + ")", new ToolbarCommand() {
        public void toolBarItemSelected() {
          viewManager.showTaskQueuedPage(group.getId());
        }
      });
      
      queuedCount += groupCount;
    }
    queuedItem.setCount(queuedCount);
  }
  
}

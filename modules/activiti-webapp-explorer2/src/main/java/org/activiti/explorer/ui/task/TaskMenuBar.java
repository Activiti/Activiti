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
import org.activiti.engine.identity.Group;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ViewManager;
import org.activiti.explorer.identity.LoggedInUser;
import org.activiti.explorer.ui.Images;
import org.activiti.explorer.ui.custom.ToolBar;
import org.activiti.explorer.ui.custom.ToolbarEntry;
import org.activiti.explorer.ui.custom.ToolbarEntry.ToolbarCommand;
import org.activiti.explorer.ui.custom.ToolbarPopupEntry;
import org.activiti.explorer.ui.task.data.ArchivedListQuery;
import org.activiti.explorer.ui.task.data.TasksListQuery;
import org.activiti.explorer.ui.task.data.InboxListQuery;
import org.activiti.explorer.ui.task.data.InvolvedListQuery;
import org.activiti.explorer.ui.task.data.QueuedListQuery;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

/**
 * The menu bar which is shown when 'Tasks' is selected in the main menu.
 * 
 * @author Joram Barrez
 * @author Frederik Heremans
 */
public class TaskMenuBar extends ToolBar {
  
  private static final long serialVersionUID = 1L;
  
  public static final String ENTRY_TASKS = "tasks";
  public static final String ENTRY_INBOX = "inbox";
  public static final String ENTRY_QUEUED = "queued";
  public static final String ENTRY_INVOLVED = "involved";
  public static final String ENTRY_ARCHIVED = "archived";
  
  protected IdentityService identityService;
  protected ViewManager viewManager;
  protected I18nManager i18nManager;
  
  public TaskMenuBar() {
    this.identityService = ProcessEngines.getDefaultProcessEngine().getIdentityService();
    this.viewManager = ExplorerApp.get().getViewManager();
    this.i18nManager = ExplorerApp.get().getI18nManager();
    
    initItems();
    initActions();
  }
  
  protected void initItems() {
    setWidth("100%");

    // TODO: the counts should be done later by eg a Refresher component

    // Inbox
    long inboxCount = new InboxListQuery().size(); 
    ToolbarEntry inboxEntry = addToolbarEntry(ENTRY_INBOX, i18nManager.getMessage(Messages.TASK_MENU_INBOX), new ToolbarCommand() {
      public void toolBarItemSelected() {
        viewManager.showInboxPage();
      }
    });
    inboxEntry.setCount(inboxCount);
    
    // Tasks
    LoggedInUser user = ExplorerApp.get().getLoggedInUser();
    long tasksCount = new TasksListQuery().size(); 
    ToolbarEntry tasksEntry = addToolbarEntry(ENTRY_TASKS, i18nManager.getMessage(Messages.TASK_MENU_TASKS), new ToolbarCommand() {
      public void toolBarItemSelected() {
        viewManager.showTasksPage();
      }
    });
    tasksEntry.setCount(tasksCount);
    
    // Queued
    List<Group> groups = identityService.createGroupQuery().groupMember(user.getId()).list();
    ToolbarPopupEntry queuedItem = addPopupEntry(ENTRY_QUEUED, (i18nManager.getMessage(Messages.TASK_MENU_QUEUED)));
    long queuedCount = 0;
    for (final Group group : groups) {
      if (group.getType().equals("assignment")) {
        long groupCount = new QueuedListQuery(group.getId()).size(); 
        
        queuedItem.addMenuItem(group.getName() + " (" + groupCount + ")", new ToolbarCommand() {
          public void toolBarItemSelected() {
            viewManager.showQueuedPage(group.getId());
          }
        });
        
        queuedCount += groupCount;
      }
    }
    queuedItem.setCount(queuedCount);
    
    // Involved
    long involvedCount = new InvolvedListQuery().size(); 
    ToolbarEntry involvedEntry = addToolbarEntry(ENTRY_INVOLVED, i18nManager.getMessage(Messages.TASK_MENU_INVOLVED), new ToolbarCommand() {
      public void toolBarItemSelected() {
        viewManager.showInvolvedPage();
      }
    });
    involvedEntry.setCount(involvedCount);
    
    // Archived
    long archivedCount = new ArchivedListQuery().size(); 
    ToolbarEntry archivedEntry = addToolbarEntry(ENTRY_ARCHIVED, i18nManager.getMessage(Messages.TASK_MENU_ARCHIVED), new ToolbarCommand() {
      public void toolBarItemSelected() {
        viewManager.showArchivedPage();
      }
    });
    archivedEntry.setCount(archivedCount);
  }
  
  protected void initActions() {
    Button newCaseButton = new Button();
    newCaseButton.setCaption(i18nManager.getMessage(Messages.TASK_NEW));
    newCaseButton.setIcon(Images.TASK_16);
    addButton(newCaseButton);
    
    newCaseButton.addListener(new ClickListener() {
      public void buttonClick(ClickEvent event) {
        NewCasePopupWindow newTaskPopupWindow = new NewCasePopupWindow();
        viewManager.showPopupWindow(newTaskPopupWindow);
      }
    });
  }
  
}

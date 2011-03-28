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
import org.activiti.explorer.ExplorerApplication;
import org.activiti.explorer.Messages;

import com.vaadin.ui.MenuBar;



/**
 * @author Joram Barrez
 */
public class TaskMenuBar extends MenuBar {
  
  private static final long serialVersionUID = 7957488256766569264L;
  protected TaskService taskService;
  protected IdentityService identityService;
  
  public TaskMenuBar() {
    this.taskService = ProcessEngines.getDefaultProcessEngine().getTaskService();
    this.identityService = ProcessEngines.getDefaultProcessEngine().getIdentityService();
    
    setWidth("100%");
    
    // TODO: the counts should be done later by eg a Refresher component
    
    // Inbox
    String userId = ExplorerApplication.getCurrent().getLoggedInUser().getId();
    long inboxCount = taskService.createTaskQuery().taskAssignee(userId).count();
    addItem(ExplorerApplication.getCurrent().getMessage(Messages.TASK_MENU_INBOX)+ "("+inboxCount+")", new Command() {
      public void menuSelected(MenuItem selectedItem) {
        ExplorerApplication.getCurrent().switchView(new TaskInboxPage());
      }
    });
    
    // Queued
    List<Group> groups = identityService.createGroupQuery().groupMember(userId).orderByGroupName().asc().list();
    MenuItem queuedItem = addItem(ExplorerApplication.getCurrent().getMessage(Messages.TASK_MENU_QUEUED), null);
    long queuedCount = 0;
    for (final Group group : groups) {
      long groupCount = taskService.createTaskQuery().taskCandidateGroup(group.getId()).count();
      queuedCount += groupCount;
      queuedItem.addItem(group.getName() + " ("+groupCount+")", new Command() {
        public void menuSelected(MenuItem selectedItem) {
          ExplorerApplication.getCurrent().switchView(new TaskQueuedPage(group.getId()));
        }
      });
    }
    queuedItem.setText(queuedItem.getText() + " ("+queuedCount+")");
  }
  
}

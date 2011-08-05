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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.TaskService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ViewManager;

/**
 * @author Frederik Heremans
 * @author Joram Barrez
 */
public class TaskNavigator implements Navigator {

  public static final String TASK_URI_PART = "tasks";
  public static final String CATEGORY_TASKS = "tasks";
  public static final String CATEGORY_INBOX = "inbox";
  public static final String CATEGORY_QUEUED = "queued";
  public static final String CATEGORY_INVOLVED = "involved";
  public static final String CATEGORY_ARCHIVED = "archived";
  
  public static final String PARAMETER_CATEGORY = "category";
  public static final String PARAMETER_GROUP = "group";
  
  protected TaskService taskService;
  protected HistoryService historyService;
  protected IdentityService identityService;
  
  public TaskNavigator() {
    this.taskService = ProcessEngines.getDefaultProcessEngine().getTaskService();
    this.identityService = ProcessEngines.getDefaultProcessEngine().getIdentityService();
    this.historyService = ProcessEngines.getDefaultProcessEngine().getHistoryService();
  }
  
  public String getTrigger() {
    return TASK_URI_PART;
  }

  public void handleNavigation(UriFragment uriFragment) {
    String category = uriFragment.getParameter(PARAMETER_CATEGORY);
    String taskId = uriFragment.getUriPart(1);
    
    if (taskId == null) {
      directToCategoryPage(category, uriFragment);
    } else {
      directToSpecificTaskPage(category, taskId, uriFragment);
    }
  }
  
  protected void directToCategoryPage(String category, UriFragment uriFragment) {
    ViewManager viewManager = ExplorerApp.get().getViewManager();
    if (CATEGORY_TASKS.equals(category)) {
      viewManager.showTasksPage();
    } else if (CATEGORY_INBOX.equals(category)) {
      viewManager.showInboxPage();
    } else if(CATEGORY_QUEUED.equals(category)) {
      viewManager.showQueuedPage(uriFragment.getParameter(PARAMETER_GROUP));
    } else if (CATEGORY_INVOLVED.equals(category)){
      viewManager.showInvolvedPage();
    } else if (CATEGORY_ARCHIVED.equals(category)) {
      viewManager.showArchivedPage();
    } else {
      throw new ActivitiException("Couldn't find a matching category");
    }
  }
  
  protected void directToSpecificTaskPage(String category, String taskId, UriFragment uriFragment) {
    Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
    ViewManager viewManager = ExplorerApp.get().getViewManager();
    String loggedInUserId = ExplorerApp.get().getLoggedInUser().getId();
    
    boolean pageFound = false;
    if (CATEGORY_TASKS.equals(category)) {
      if (loggedInUserId.equals(task.getOwner())) {
        viewManager.showTasksPage(task.getId());
        pageFound = true;
      }
    } else if (CATEGORY_INBOX.equals(category)) {
      if (loggedInUserId.equals(task.getAssignee())) {
        viewManager.showInboxPage(task.getId());
        pageFound = true;
      }
    } else if(CATEGORY_QUEUED.equals(category)) {
      String groupId = uriFragment.getParameter(PARAMETER_GROUP);
      
      boolean isTaskAssignedToGroup = taskService.createTaskQuery()
        .taskId(task.getId())
        .taskCandidateGroup(groupId)
        .count() == 1;
      
      boolean isUserMemberOfGroup = identityService.createGroupQuery()
        .groupMember(loggedInUserId)
        .groupId(groupId)
        .count() == 1;
      
      if (isTaskAssignedToGroup && isUserMemberOfGroup) {
        viewManager.showQueuedPage(groupId, task.getId());
        pageFound = true;
      }
        
    } else if (CATEGORY_INVOLVED.equals(category)){
      boolean isUserInvolved = taskService.createTaskQuery()
        .taskInvolvedUser(loggedInUserId)
        .count() == 1;
      
      if (isUserInvolved) {
        viewManager.showInvolvedPage(task.getId());
        pageFound = true;
      }
    } else if (CATEGORY_ARCHIVED.equals(category)) {
      if (task == null) {
        boolean isOwner = historyService.createHistoricTaskInstanceQuery()
          .taskId(taskId)
          .taskOwner(loggedInUserId)
          .finished()
          .count() == 1;
        
        if (isOwner) {
          viewManager.showArchivedPage(taskId);
          pageFound = true;
        }
      }
    } else {
      throw new ActivitiException("Couldn't find a matching category");
    }
    
    if (!pageFound) {
      // If URL doesnt match anymore, we must use the task data to redirect to the right page
      viewManager.showTaskPage(taskId);
    }
  }
 
  protected void showNavigationError(String taskId) {
    String description = ExplorerApp.get().getI18nManager().getMessage(Messages.NAVIGATION_ERROR_NOT_INVOLVED, taskId);
    ExplorerApp.get().getNotificationManager().showErrorNotification(Messages.NAVIGATION_ERROR_NOT_INVOLVED_TITLE, description);
  }
  
  protected List<String> getGroupIds(String userId) {
    List<String> groupIds = new ArrayList<String>();
    List<Group> groups = identityService.createGroupQuery().groupMember(userId).list();
    for (Group group : groups) {
      groupIds.add(group.getId());
    }
    
    return groupIds;
  }

}

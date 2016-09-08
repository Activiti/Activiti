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
package org.activiti.app.service.editor;

import java.util.ArrayList;
import java.util.List;

import org.activiti.app.model.idm.UserRepresentation;
import org.activiti.app.model.runtime.TaskRepresentation;
import org.activiti.app.security.SecurityUtils;
import org.activiti.app.service.api.UserCache;
import org.activiti.app.service.api.UserCache.CachedUser;
import org.activiti.app.service.exception.BadRequestException;
import org.activiti.app.service.exception.NotFoundException;
import org.activiti.app.service.exception.NotPermittedException;
import org.activiti.app.service.runtime.PermissionService;
import org.activiti.app.service.util.TaskUtil;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricIdentityLink;
import org.activiti.engine.identity.User;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author Tijs Rademakers
 */
@Service
public class ActivitiTaskActionService {

  private static final Logger logger = LoggerFactory.getLogger(ActivitiTaskActionService.class);

  @Autowired
  protected RepositoryService repositoryService;
  
  @Autowired
  protected TaskService taskService;

  @Autowired
  protected PermissionService permissionService;

  @Autowired
  protected IdentityService identityService;
  
  @Autowired
  protected HistoryService historyService;
  
  @Autowired
  protected UserCache userCache;

  public void completeTask(String taskId) {
    User currentUser = SecurityUtils.getCurrentUserObject();
    Task task = taskService.createTaskQuery().taskId(taskId).singleResult();

    if (task == null) {
      throw new NotFoundException("Task with id: " + taskId + " does not exist");
    }

    if (!permissionService.isTaskOwnerOrAssignee(currentUser, task)) {
      if (!permissionService.validateIfUserIsInitiatorAndCanCompleteTask(currentUser, task)) {
        throw new NotPermittedException();
      }
    }

    try {
      taskService.complete(task.getId());
    } catch (ActivitiException e) {
      logger.error("Error completing task " + taskId, e);
      throw new BadRequestException("Task " + taskId + " can't be completed", e);
    }
  }

  public TaskRepresentation assignTask(String taskId, ObjectNode requestNode) {
    User currentUser = SecurityUtils.getCurrentUserObject();
    Task task = taskService.createTaskQuery().taskId(taskId).singleResult();

    if (task == null) {
      throw new NotFoundException("Task with id: " + taskId + " does not exist");
    }

    checkTaskPermissions(taskId, currentUser, task);

    if (requestNode.get("assignee") != null) {

      // This method can only be called by someone in a tenant. Check if the user is part of the tenant
      String assigneeIdString = requestNode.get("assignee").asText();

      CachedUser cachedUser = userCache.getUser(assigneeIdString);
      if (cachedUser == null) {
        throw new BadRequestException("Invalid assignee id");
      }
      assignTask(currentUser, task, assigneeIdString);

    } else {
      throw new BadRequestException("Assignee is required");
    }

    task = taskService.createTaskQuery().taskId(taskId).singleResult();
    TaskRepresentation rep = new TaskRepresentation(task);
    TaskUtil.fillPermissionInformation(rep, task, currentUser, identityService, historyService, repositoryService);

    populateAssignee(task, rep);
    rep.setInvolvedPeople(getInvolvedUsers(taskId));
    return rep;
  }

  public void involveUser(String taskId, ObjectNode requestNode) {
    Task task = taskService.createTaskQuery().taskId(taskId).singleResult();

    if (task == null) {
      throw new NotFoundException("Task with id: " + taskId + " does not exist");
    }

    User currentUser = SecurityUtils.getCurrentUserObject();
    permissionService.validateReadPermissionOnTask(currentUser, task.getId());

    if (requestNode.get("userId") != null) {
      String userId = requestNode.get("userId").asText();
      CachedUser user = userCache.getUser(userId);
      if (user == null) {
        throw new BadRequestException("Invalid user id");
      }
      taskService.addUserIdentityLink(taskId, userId.toString(), IdentityLinkType.PARTICIPANT);

    } else {
      throw new BadRequestException("User id is required");
    }

  }

  public void removeInvolvedUser(String taskId, ObjectNode requestNode) {
    Task task = taskService.createTaskQuery().taskId(taskId).singleResult();

    if (task == null) {
      throw new NotFoundException("Task with id: " + taskId + " does not exist");
    }

    permissionService.validateReadPermissionOnTask(SecurityUtils.getCurrentUserObject(), task.getId());

    String assigneeString = null;
    if (requestNode.get("userId") != null) {
      String userId = requestNode.get("userId").asText();
      if (userCache.getUser(userId) == null) {
        throw new BadRequestException("Invalid user id");
      }
      assigneeString = String.valueOf(userId);

    } else if (requestNode.get("email") != null) {

      String email = requestNode.get("email").asText();
      assigneeString = email;

    } else {
      throw new BadRequestException("User id or email is required");
    }

    taskService.deleteUserIdentityLink(taskId, assigneeString, IdentityLinkType.PARTICIPANT);
  }

  public void claimTask(String taskId) {

    User currentUser = SecurityUtils.getCurrentUserObject();
    Task task = taskService.createTaskQuery().taskId(taskId).singleResult();

    if (task == null) {
      throw new NotFoundException("Task with id: " + taskId + " does not exist");
    }

    permissionService.validateReadPermissionOnTask(currentUser, task.getId());

    try {
      taskService.claim(task.getId(), String.valueOf(currentUser.getId()));
    } catch (ActivitiException e) {
      throw new BadRequestException("Task " + taskId + " can't be claimed", e);
    }
  }

  protected void checkTaskPermissions(String taskId, User currentUser, Task task) {
    permissionService.validateReadPermissionOnTask(currentUser, task.getId());
  }

  protected String validateEmail(ObjectNode requestNode) {
    String email = requestNode.get("email") != null ? requestNode.get("email").asText() : null;
    if (email == null) {
      throw new BadRequestException("Email is mandatory");
    }
    return email;
  }

  protected void assignTask(User currentUser, Task task, String assigneeIdString) {
    try {
      String oldAssignee = task.getAssignee();
      taskService.setAssignee(task.getId(), assigneeIdString);

      // If the old assignee user wasn't part of the involved users yet, make it so
      addIdentiyLinkForUser(task, oldAssignee, IdentityLinkType.PARTICIPANT);

      // If the current user wasn't part of the involved users yet, make it so
      String currentUserIdString = String.valueOf(currentUser.getId());
      addIdentiyLinkForUser(task, currentUserIdString, IdentityLinkType.PARTICIPANT);

    } catch (ActivitiException e) {
      throw new BadRequestException("Task " + task.getId() + " can't be assigned", e);
    }
  }

  protected void addIdentiyLinkForUser(Task task, String userId, String linkType) {
    List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(task.getId());
    boolean isOldUserInvolved = false;
    for (IdentityLink identityLink : identityLinks) {
      if (userId.equals(identityLink.getUserId()) && (identityLink.getType().equals(IdentityLinkType.PARTICIPANT) || identityLink.getType().equals(IdentityLinkType.CANDIDATE))) {
        isOldUserInvolved = true;
      }
    }
    if (!isOldUserInvolved) {
      taskService.addUserIdentityLink(task.getId(), userId, linkType);
    }
  }
  
  protected void populateAssignee(TaskInfo task, TaskRepresentation rep) {
    if (task.getAssignee() != null) {
      CachedUser cachedUser = userCache.getUser(task.getAssignee());
      if (cachedUser != null && cachedUser.getUser() != null) {
        rep.setAssignee(new UserRepresentation(cachedUser.getUser()));
      }
    }
  }

  protected List<UserRepresentation> getInvolvedUsers(String taskId) {
    List<HistoricIdentityLink> idLinks = historyService.getHistoricIdentityLinksForTask(taskId);
    List<UserRepresentation> result = new ArrayList<UserRepresentation>(idLinks.size());

    for (HistoricIdentityLink link : idLinks) {
      // Only include users and non-assignee links
      if (link.getUserId() != null && !IdentityLinkType.ASSIGNEE.equals(link.getType())) {
        CachedUser cachedUser = userCache.getUser(link.getUserId());
        if (cachedUser != null && cachedUser.getUser() != null) {
          result.add(new UserRepresentation(cachedUser.getUser()));
        }
      }
    }
    return result;
  }
}

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
package org.activiti.app.rest.runtime;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.activiti.app.model.idm.UserRepresentation;
import org.activiti.app.model.runtime.TaskRepresentation;
import org.activiti.app.model.runtime.TaskUpdateRepresentation;
import org.activiti.app.security.SecurityUtils;
import org.activiti.app.service.api.UserCache;
import org.activiti.app.service.api.UserCache.CachedUser;
import org.activiti.app.service.exception.NotFoundException;
import org.activiti.app.service.runtime.PermissionService;
import org.activiti.app.service.util.TaskUtil;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricIdentityLink;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.identity.User;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author jbarrez
 */
public abstract class AbstractTaskResource {

  private static final Logger logger = LoggerFactory.getLogger(AbstractTaskResource.class);

  @Autowired
  protected TaskService taskService;

  @Autowired
  protected HistoryService historyService;

  @Autowired
  protected UserCache userCache;

  @Autowired
  protected PermissionService permissionService;

  @Autowired
  protected RepositoryService repositoryService;

  @Autowired
  protected IdentityService identityService;

  public TaskRepresentation getTask(String taskId, HttpServletResponse response) {
    User currentUser = SecurityUtils.getCurrentUserObject();
    HistoricTaskInstance task = permissionService.validateReadPermissionOnTask(currentUser, taskId);

    ProcessDefinition processDefinition = null;
    if (StringUtils.isNotEmpty(task.getProcessDefinitionId())) {
      try {
        processDefinition = repositoryService.getProcessDefinition(task.getProcessDefinitionId());
      } catch (ActivitiException e) {
        logger.error("Error getting process definition " + task.getProcessDefinitionId(), e);
      }
    }

    TaskRepresentation rep = new TaskRepresentation(task, processDefinition);
    TaskUtil.fillPermissionInformation(rep, task, currentUser, identityService, historyService, repositoryService);

    // Populate the people
    populateAssignee(task, rep);
    rep.setInvolvedPeople(getInvolvedUsers(taskId));

    return rep;
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

  public TaskRepresentation updateTask(String taskId, TaskUpdateRepresentation updated) {
    Task task = taskService.createTaskQuery().taskId(taskId).singleResult();

    if (task == null) {
      throw new NotFoundException("Task with id: " + taskId + " does not exist");
    }

    permissionService.validateReadPermissionOnTask(SecurityUtils.getCurrentUserObject(), task.getId());

    if (updated.isNameSet()) {
      task.setName(updated.getName());
    }

    if (updated.isDescriptionSet()) {
      task.setDescription(updated.getDescription());
    }

    if (updated.isDueDateSet()) {
      task.setDueDate(updated.getDueDate());
    }

    taskService.saveTask(task);

    return new TaskRepresentation(task);
  }

}
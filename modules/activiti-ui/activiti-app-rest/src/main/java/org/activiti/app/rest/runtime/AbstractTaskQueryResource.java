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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.activiti.app.model.common.ResultListDataRepresentation;
import org.activiti.app.model.idm.UserRepresentation;
import org.activiti.app.model.runtime.TaskRepresentation;
import org.activiti.app.security.SecurityUtils;
import org.activiti.app.service.api.UserCache;
import org.activiti.app.service.api.UserCache.CachedUser;
import org.activiti.app.service.exception.BadRequestException;
import org.activiti.app.service.runtime.PermissionService;
import org.activiti.editor.language.json.converter.util.CollectionUtils;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.TaskInfo;
import org.activiti.engine.task.TaskInfoQueryWrapper;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.ISO8601Utils;

public abstract class AbstractTaskQueryResource {

  private static final String SORT_CREATED_ASC = "created-asc";
  private static final String SORT_CREATED_DESC = "created-desc";
  private static final String SORT_DUE_ASC = "due-asc";
  private static final String SORT_DUE_DESC = "due-desc";

  private static final int DEFAULT_PAGE_SIZE = 25;

  @Inject
  protected RepositoryService repositoryService;

  @Inject
  protected TaskService taskService;

  @Inject
  protected RuntimeService runtimeService;

  @Inject
  protected HistoryService historyService;

  @Inject
  protected UserCache userCache;

  @Inject
  protected PermissionService permissionService;
  
  public ResultListDataRepresentation listTasks(ObjectNode requestNode) {

    if (requestNode == null) {
      throw new BadRequestException("No request found");
    }
    User currentUser = SecurityUtils.getCurrentUserObject();

    JsonNode stateNode = requestNode.get("state");
    TaskInfoQueryWrapper taskInfoQueryWrapper = null;
    if (stateNode != null && "completed".equals(stateNode.asText())) {
      HistoricTaskInstanceQuery historicTaskInstanceQuery = historyService.createHistoricTaskInstanceQuery();
      historicTaskInstanceQuery.finished();
      taskInfoQueryWrapper = new TaskInfoQueryWrapper(historicTaskInstanceQuery);
    } else {
      taskInfoQueryWrapper = new TaskInfoQueryWrapper(taskService.createTaskQuery());
    }

    JsonNode deploymentKeyNode = requestNode.get("deploymentKey");
    if (deploymentKeyNode != null && deploymentKeyNode.isNull() == false) {
      List<Deployment> deployments = repositoryService.createDeploymentQuery().deploymentKey(deploymentKeyNode.asText()).list();
      List<String> deploymentIds = new ArrayList<String>(deployments.size());
      for (Deployment deployment : deployments) {
        deploymentIds.add(deployment.getId());
      }
      
      taskInfoQueryWrapper.getTaskInfoQuery().or()
        .deploymentIdIn(deploymentIds)
        .taskCategory(deploymentKeyNode.asText())
        .endOr();
    }

    JsonNode processInstanceIdNode = requestNode.get("processInstanceId");
    if (processInstanceIdNode != null && processInstanceIdNode.isNull() == false) {
      handleProcessInstanceFiltering(currentUser, taskInfoQueryWrapper, processInstanceIdNode);
    }

    JsonNode textNode = requestNode.get("text");
    if (textNode != null && textNode.isNull() == false) {
      handleTextFiltering(taskInfoQueryWrapper, textNode);
    }

    JsonNode assignmentNode = requestNode.get("assignment");
    if (assignmentNode != null && assignmentNode.isNull() == false) {
      handleAssignment(taskInfoQueryWrapper, assignmentNode, currentUser);
    }

    JsonNode processDefinitionNode = requestNode.get("processDefinitionId");
    if (processDefinitionNode != null && processDefinitionNode.isNull() == false) {
      handleProcessDefinition(taskInfoQueryWrapper, processDefinitionNode);
    }

    JsonNode dueBeforeNode = requestNode.get("dueBefore");
    if (dueBeforeNode != null && dueBeforeNode.isNull() == false) {
      handleDueBefore(taskInfoQueryWrapper, dueBeforeNode);
    }

    JsonNode dueAfterNode = requestNode.get("dueAfter");
    if (dueAfterNode != null && dueAfterNode.isNull() == false) {
      handleDueAfter(taskInfoQueryWrapper, dueAfterNode);
    }

    JsonNode sortNode = requestNode.get("sort");
    if (sortNode != null) {
      handleSorting(taskInfoQueryWrapper, sortNode);
    }

    int page = 0;
    JsonNode pageNode = requestNode.get("page");
    if (pageNode != null && !pageNode.isNull()) {
      page = pageNode.asInt(0);
    }

    int size = DEFAULT_PAGE_SIZE;
    JsonNode sizeNode = requestNode.get("size");
    if (sizeNode != null && !sizeNode.isNull()) {
      size = sizeNode.asInt(DEFAULT_PAGE_SIZE);
    }

    List<? extends TaskInfo> tasks = taskInfoQueryWrapper.getTaskInfoQuery().listPage(page * size, size);

    JsonNode includeProcessInstanceNode = requestNode.get("includeProcessInstance");
    // todo Once a ProcessInstanceInfo class has been implement use it instead rather than just the name
    Map<String, String> processInstancesNames = new HashMap<String, String>();
    if (includeProcessInstanceNode != null) {
      handleIncludeProcessInstance(taskInfoQueryWrapper, includeProcessInstanceNode, tasks, processInstancesNames);
    }

    ResultListDataRepresentation result = new ResultListDataRepresentation(convertTaskInfoList(tasks, processInstancesNames));

    // In case we're not on the first page and the size exceeds the page size, we need to do an additional count for the total
    if (page != 0 || tasks.size() == size) {
      Long totalCount = taskInfoQueryWrapper.getTaskInfoQuery().count();
      result.setTotal(Long.valueOf(totalCount.intValue()));
      result.setStart(page * size);
    }

    return result;
  }

  private void handleProcessInstanceFiltering(User currentUser, TaskInfoQueryWrapper taskInfoQueryWrapper, JsonNode processInstanceIdNode) {
    String processInstanceId = processInstanceIdNode.asText();
    taskInfoQueryWrapper.getTaskInfoQuery().processInstanceId(processInstanceId);
  }

  private void handleTextFiltering(TaskInfoQueryWrapper taskInfoQueryWrapper, JsonNode textNode) {
    String text = textNode.asText();

    // [4/9/2014] Used to be an or on description too, but doesnt work combined with the or query for an app.
    // (Would need a change in Activiti)
    taskInfoQueryWrapper.getTaskInfoQuery().taskNameLikeIgnoreCase("%" + text + "%");
  }

  private void handleAssignment(TaskInfoQueryWrapper taskInfoQueryWrapper, JsonNode assignmentNode, User currentUser) {
    String assignment = assignmentNode.asText();
    if (assignment.length() > 0) {
      String currentUserId = String.valueOf(currentUser.getId());
      if ("assignee".equals(assignment)) {
        taskInfoQueryWrapper.getTaskInfoQuery().taskAssignee(currentUserId);
      } else if ("candidate".equals(assignment)) {
        taskInfoQueryWrapper.getTaskInfoQuery().taskCandidateUser(currentUserId);
      } else if (assignment.startsWith("group_")) {
        String groupIdString = assignment.replace("group_", "");
        try {
          Long.valueOf(groupIdString);
        } catch (NumberFormatException e) {
          throw new BadRequestException("Invalid group id");
        }
        taskInfoQueryWrapper.getTaskInfoQuery().taskCandidateGroup(groupIdString);
      } else { // Default = involved
        taskInfoQueryWrapper.getTaskInfoQuery().taskInvolvedUser(currentUserId);
      }
    }
  }

  private void handleProcessDefinition(TaskInfoQueryWrapper taskInfoQueryWrapper, JsonNode processDefinitionIdNode) {
    String processDefinitionId = processDefinitionIdNode.asText();
    taskInfoQueryWrapper.getTaskInfoQuery().processDefinitionId(processDefinitionId);
  }

  private void handleDueBefore(TaskInfoQueryWrapper taskInfoQueryWrapper, JsonNode dueBeforeNode) {
    String date = dueBeforeNode.asText();
    Date d = ISO8601Utils.parse(date);
    taskInfoQueryWrapper.getTaskInfoQuery().taskDueBefore(d);
  }

  private void handleDueAfter(TaskInfoQueryWrapper taskInfoQueryWrapper, JsonNode dueAfterNode) {
    String date = dueAfterNode.asText();
    Date d = ISO8601Utils.parse(date);
    taskInfoQueryWrapper.getTaskInfoQuery().taskDueAfter(d);
  }

  private void handleSorting(TaskInfoQueryWrapper taskInfoQueryWrapper, JsonNode sortNode) {
    String sort = sortNode.asText();

    if (SORT_CREATED_ASC.equals(sort)) {
      taskInfoQueryWrapper.getTaskInfoQuery().orderByTaskCreateTime().asc();
    } else if (SORT_CREATED_DESC.equals(sort)) {
      taskInfoQueryWrapper.getTaskInfoQuery().orderByTaskCreateTime().desc();
    } else if (SORT_DUE_ASC.equals(sort)) {
      taskInfoQueryWrapper.getTaskInfoQuery().orderByDueDateNullsLast().asc();
    } else if (SORT_DUE_DESC.equals(sort)) {
      taskInfoQueryWrapper.getTaskInfoQuery().orderByDueDateNullsLast().desc();
    } else {
      // Default
      taskInfoQueryWrapper.getTaskInfoQuery().orderByTaskCreateTime().desc();
    }
  }

  private void handleIncludeProcessInstance(TaskInfoQueryWrapper taskInfoQueryWrapper, JsonNode includeProcessInstanceNode, List<? extends TaskInfo> tasks, Map<String, String> processInstanceNames) {
    if (includeProcessInstanceNode.asBoolean() && CollectionUtils.isNotEmpty(tasks)) {
      Set<String> processInstanceIds = new HashSet<String>();
      for (TaskInfo task : tasks) {
        if (task.getProcessInstanceId() != null) {
          processInstanceIds.add(task.getProcessInstanceId());
        }
      }
      if (CollectionUtils.isNotEmpty(processInstanceIds)) {
        if (taskInfoQueryWrapper.getTaskInfoQuery() instanceof HistoricTaskInstanceQuery) {
          List<HistoricProcessInstance> processInstances = historyService.createHistoricProcessInstanceQuery().processInstanceIds(processInstanceIds).list();
          for (HistoricProcessInstance processInstance : processInstances) {
            processInstanceNames.put(processInstance.getId(), processInstance.getName());
          }
        } else {
          List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().processInstanceIds(processInstanceIds).list();
          for (ProcessInstance processInstance : processInstances) {
            processInstanceNames.put(processInstance.getId(), processInstance.getName());
          }
        }
      }
    }
  }

  protected List<TaskRepresentation> convertTaskInfoList(List<? extends TaskInfo> tasks, Map<String, String> processInstanceNames) {
    List<TaskRepresentation> result = new ArrayList<TaskRepresentation>();
    if (CollectionUtils.isNotEmpty(tasks)) {
      for (TaskInfo task : tasks) {
        ProcessDefinitionEntity processDefinition = null;
        if (task.getProcessDefinitionId() != null) {
          processDefinition = (ProcessDefinitionEntity) repositoryService.getProcessDefinition(task.getProcessDefinitionId());
        }
        TaskRepresentation representation = new TaskRepresentation(task, processDefinition, processInstanceNames.get(task.getProcessInstanceId()));
        
        if (StringUtils.isNotEmpty(task.getAssignee())) {
          CachedUser cachedUser = userCache.getUser(task.getAssignee());
          if (cachedUser != null && cachedUser.getUser() != null) {
            User assignee = cachedUser.getUser();
            representation.setAssignee(new UserRepresentation(assignee));
          }
        }
        
        result.add(representation);
      }
    }
    return result;
  }
}

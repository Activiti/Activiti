/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.test.api.task;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.activiti.api.runtime.shared.identity.UserGroupManager;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.activiti.engine.test.Deployment;
import org.mockito.Mockito;

/**
 */
public class TaskQueryTest extends PluggableActivitiTestCase {

  private List<String> taskIds;

  private static final String KERMIT = "kermit";
  private static final List<String> KERMITSGROUPS = asList("management","accountancy");

  private static final String GONZO = "gonzo";
  private static final List<String> GONZOSGROUPS = asList();

  private static final String FOZZIE = "fozzie";
  private static final List<String> FOZZIESGROUPS = asList("management");

  private static final String SCOOTER = "scooter";
  private static final List<String> SCOOTERSGROUPS = null;

  private UserGroupManager userGroupManager = Mockito.mock(UserGroupManager.class);


  public void setUp() throws Exception {
    ProcessEngineConfigurationImpl engineConfiguration = (ProcessEngineConfigurationImpl)cachedProcessEngine.getProcessEngineConfiguration();
    engineConfiguration.setUserGroupManager(userGroupManager);
    taskIds = generateTestTasks();
  }

  public void tearDown() throws Exception {

    taskService.deleteTasks(taskIds, true);
  }

  public void testBasicTaskPropertiesNotNull() {
    Task task = taskService.createTaskQuery().taskId(taskIds.get(0)).singleResult();
    assertThat(task.getDescription()).isNotNull();
    assertThat(task.getId()).isNotNull();
    assertThat(task.getName()).isNotNull();
    assertThat(task.getCreateTime()).isNotNull();
  }

  public void testQueryNoCriteria() {
    TaskQuery query = taskService.createTaskQuery();
    assertThat(query.count()).isEqualTo(12);
    assertThat(query.list()).hasSize(12);
    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> query.singleResult());
  }

  public void testQueryByTaskId() {
    TaskQuery query = taskService.createTaskQuery().taskId(taskIds.get(0));
    assertThat(query.singleResult()).isNotNull();
    assertThat(query.list()).hasSize(1);
    assertThat(query.count()).isEqualTo(1);
  }

  public void testQueryByTaskIdOr() {
    TaskQuery query = taskService.createTaskQuery().or().taskId(taskIds.get(0)).taskName("INVALID NAME").endOr();
    assertThat(query.singleResult()).isNotNull();
    assertThat(query.list()).hasSize(1);
    assertThat(query.count()).isEqualTo(1);
  }

  public void testQueryByInvalidTaskId() {
    TaskQuery query = taskService.createTaskQuery().taskId("invalid");
    assertThat(query.singleResult()).isNull();
    assertThat(query.list()).hasSize(0);
    assertThat(query.count()).isEqualTo(0);

    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> taskService.createTaskQuery().taskId(null));
  }

  public void testQueryByInvalidTaskIdOr() {
    TaskQuery query = taskService.createTaskQuery().or().taskId("invalid").taskName("invalid");
    assertThat(query.singleResult()).isNull();
    assertThat(query.list()).hasSize(0);
    assertThat(query.count()).isEqualTo(0);

    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> taskService.createTaskQuery().taskId(null));
  }

  public void testQueryByName() {
    TaskQuery query = taskService.createTaskQuery().taskName("testTask");
    assertThat(query.list()).hasSize(6);
    assertThat(query.count()).isEqualTo(6);

    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> query.singleResult());
  }

  public void testQueryByNameOr() {
    TaskQuery query = taskService.createTaskQuery().or().taskName("testTask").taskId("invalid");
    assertThat(query.list()).hasSize(6);
    assertThat(query.count()).isEqualTo(6);

    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> query.singleResult());
  }

  public void testQueryByInvalidName() {
    TaskQuery query = taskService.createTaskQuery().taskName("invalid");
    assertThat(query.singleResult()).isNull();
    assertThat(query.list()).hasSize(0);
    assertThat(query.count()).isEqualTo(0);

    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> taskService.createTaskQuery().or().taskId("invalid").taskName(null).singleResult());
  }

  public void testQueryByInvalidNameOr() {
    TaskQuery query = taskService.createTaskQuery().or().taskId("invalid").taskName("invalid");
    assertThat(query.singleResult()).isNull();
    assertThat(query.list()).hasSize(0);
    assertThat(query.count()).isEqualTo(0);

    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> taskService.createTaskQuery().or().taskId("invalid").taskName(null).singleResult());
  }

  public void testQueryByNameIn() {
    final List<String> taskNameList = new ArrayList<String>(2);
    taskNameList.add("testTask");
    taskNameList.add("gonzoTask");

    TaskQuery query = taskService.createTaskQuery().taskNameIn(taskNameList);
    assertThat(query.list()).hasSize(7);
    assertThat(query.count()).isEqualTo(7);

    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> query.singleResult());
  }

  public void testQueryByNameInIgnoreCase() {
    final List<String> taskNameList = new ArrayList<String>(2);
    taskNameList.add("testtask");
    taskNameList.add("gonzotask");

    TaskQuery query = taskService.createTaskQuery().taskNameInIgnoreCase(taskNameList);
    assertThat(query.list()).hasSize(7);
    assertThat(query.count()).isEqualTo(7);

    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> query.singleResult());
  }

  public void testQueryByNameInOr() {
    final List<String> taskNameList = new ArrayList<String>(2);
    taskNameList.add("testTask");
    taskNameList.add("gonzoTask");

    TaskQuery query = taskService.createTaskQuery().or().taskNameIn(taskNameList).taskId("invalid");
    assertThat(query.list()).hasSize(7);
    assertThat(query.count()).isEqualTo(7);

    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> query.singleResult());
  }

  public void testQueryByNameInIgnoreCaseOr() {
    final List<String> taskNameList = new ArrayList<String>(2);
    taskNameList.add("testtask");
    taskNameList.add("gonzotask");

    TaskQuery query = taskService.createTaskQuery().or().taskNameInIgnoreCase(taskNameList).taskId("invalid");
    assertThat(query.list()).hasSize(7);
    assertThat(query.count()).isEqualTo(7);

    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> query.singleResult());
  }

  public void testQueryByInvalidNameIn() {
    final List<String> taskNameList = new ArrayList<String>(1);
    taskNameList.add("invalid");

    TaskQuery query = taskService.createTaskQuery().taskNameIn(taskNameList);
    assertThat(query.list()).hasSize(0);
    assertThat(query.count()).isEqualTo(0);

    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> taskService.createTaskQuery().or().taskId("invalid").taskNameIn(null).singleResult());
  }

  public void testQueryByInvalidNameInIgnoreCase() {
    final List<String> taskNameList = new ArrayList<String>(1);
    taskNameList.add("invalid");

    TaskQuery query = taskService.createTaskQuery().taskNameInIgnoreCase(taskNameList);
    assertThat(query.list()).hasSize(0);
    assertThat(query.count()).isEqualTo(0);

    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> taskService.createTaskQuery().or().taskId("invalid").taskNameIn(null).singleResult());
  }

  public void testQueryByInvalidNameInOr() {
    final List<String> taskNameList = new ArrayList<String>(2);
    taskNameList.add("invalid");

    TaskQuery query = taskService.createTaskQuery().or().taskNameIn(taskNameList).taskId("invalid");
    assertThat(query.list()).hasSize(0);
    assertThat(query.count()).isEqualTo(0);

    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> taskService.createTaskQuery().or().taskId("invalid").taskNameIn(null).singleResult());
  }

  public void testQueryByInvalidNameInIgnoreCaseOr() {
    final List<String> taskNameList = new ArrayList<String>(2);
    taskNameList.add("invalid");

    TaskQuery query = taskService.createTaskQuery().or().taskNameInIgnoreCase(taskNameList).taskId("invalid");
    assertThat(query.list()).hasSize(0);
    assertThat(query.count()).isEqualTo(0);

    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> taskService.createTaskQuery().or().taskId("invalid").taskNameIn(null).singleResult());
  }

  public void testQueryByNameLike() {
    TaskQuery query = taskService.createTaskQuery().taskNameLike("gonzo%");
    assertThat(query.singleResult()).isNotNull();
    assertThat(query.list()).hasSize(1);
    assertThat(query.count()).isEqualTo(1);
  }

  public void testQueryByNameLikeOr() {
    TaskQuery query = taskService.createTaskQuery().or().taskId("invalid").taskNameLike("gonzo%");
    assertThat(query.singleResult()).isNotNull();
    assertThat(query.list()).hasSize(1);
    assertThat(query.count()).isEqualTo(1);
  }

  public void testQueryByInvalidNameLike() {
    TaskQuery query = taskService.createTaskQuery().taskNameLike("1");
    assertThat(query.singleResult()).isNull();
    assertThat(query.list()).hasSize(0);
    assertThat(query.count()).isEqualTo(0);

    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> taskService.createTaskQuery().or().taskId("invalid").taskNameLike(null).singleResult());
  }

  public void testQueryByInvalidNameLikeOr() {
    TaskQuery query = taskService.createTaskQuery().or().taskId("invalid").taskNameLike("1");
    assertThat(query.singleResult()).isNull();
    assertThat(query.list()).hasSize(0);
    assertThat(query.count()).isEqualTo(0);

    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> taskService.createTaskQuery().or().taskId("invalid").taskNameLike(null).singleResult());
  }

  public void testQueryByDescription() {
    TaskQuery query = taskService.createTaskQuery().taskDescription("testTask description");
    assertThat(query.list()).hasSize(6);
    assertThat(query.count()).isEqualTo(6);

    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> query.singleResult());
  }

  public void testQueryByDescriptionOr() {
    TaskQuery query = taskService.createTaskQuery().or().taskId("invalid").taskDescription("testTask description");
    assertThat(query.list()).hasSize(6);
    assertThat(query.count()).isEqualTo(6);

    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> query.singleResult());
  }

  public void testQueryByInvalidDescription() {
    TaskQuery query = taskService.createTaskQuery().taskDescription("invalid");
    assertThat(query.singleResult()).isNull();
    assertThat(query.list()).hasSize(0);
    assertThat(query.count()).isEqualTo(0);

    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> taskService.createTaskQuery().or().taskId("invalid").taskDescription(null).list());
  }

  public void testQueryByInvalidDescriptionOr() {
    TaskQuery query = taskService.createTaskQuery().or().taskId("invalid").taskDescription("invalid");
    assertThat(query.singleResult()).isNull();
    assertThat(query.list()).hasSize(0);
    assertThat(query.count()).isEqualTo(0);

    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> taskService.createTaskQuery().or().taskId("invalid").taskDescription(null).list());
  }

  public void testQueryByDescriptionLike() {
    TaskQuery query = taskService.createTaskQuery().taskDescriptionLike("%gonzo%");
    assertThat(query.singleResult()).isNotNull();
    assertThat(query.list()).hasSize(1);
    assertThat(query.count()).isEqualTo(1);
  }

  public void testQueryByDescriptionLikeOr() {
    TaskQuery query = taskService.createTaskQuery().or().taskId("invalid").taskDescriptionLike("%gonzo%");
    assertThat(query.singleResult()).isNotNull();
    assertThat(query.list()).hasSize(1);
    assertThat(query.count()).isEqualTo(1);
  }

  public void testQueryByInvalidDescriptionLike() {
    TaskQuery query = taskService.createTaskQuery().taskDescriptionLike("invalid");
    assertThat(query.singleResult()).isNull();
    assertThat(query.list()).hasSize(0);
    assertThat(query.count()).isEqualTo(0);

    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> taskService.createTaskQuery().or().taskId("invalid").taskDescriptionLike(null).list());
  }

  public void testQueryByInvalidDescriptionLikeOr() {
    TaskQuery query = taskService.createTaskQuery().or().taskId("invalid").taskDescriptionLike("invalid");
    assertThat(query.singleResult()).isNull();
    assertThat(query.list()).hasSize(0);
    assertThat(query.count()).isEqualTo(0);

    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> taskService.createTaskQuery().or().taskId("invalid").taskDescriptionLike(null).list());
  }

  public void testQueryByPriorityTenThrowsException() {
    TaskQuery query = taskService.createTaskQuery().taskPriority(10);
    assertThat(query.list()).hasSize(2);
    assertThat(query.count()).isEqualTo(2);

    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> query.singleResult());
  }

  public void testQueryByPriority() {
    TaskQuery query = taskService.createTaskQuery().taskPriority(100);
    assertThat(query.singleResult()).isNull();
    assertThat(query.list()).hasSize(0);
    assertThat(query.count()).isEqualTo(0);

    query = taskService.createTaskQuery().taskMinPriority(4);
    assertThat(query.list()).hasSize(3);

    query = taskService.createTaskQuery().taskMinPriority(10);
    assertThat(query.list()).hasSize(2);

    query = taskService.createTaskQuery().taskMaxPriority(10);
    assertThat(query.list()).hasSize(12);

    query = taskService.createTaskQuery().taskMaxPriority(3);
    assertThat(query.list()).hasSize(9);
  }

  public void testQueryByPriorityTenOrThrowsException() {
    TaskQuery query = taskService.createTaskQuery().or().taskId("invalid").taskPriority(10);
    assertThat(query.list()).hasSize(2);
    assertThat(query.count()).isEqualTo(2);

    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> query.singleResult());
  }

  public void testQueryByPriorityOr() {
    TaskQuery query = taskService.createTaskQuery().or().taskId("invalid").taskPriority(100);
    assertThat(query.singleResult()).isNull();
    assertThat(query.list()).hasSize(0);
    assertThat(query.count()).isEqualTo(0);

    query = taskService.createTaskQuery().or().taskId("invalid").taskMinPriority(4);
    assertThat(query.list()).hasSize(3);

    query = taskService.createTaskQuery().or().taskId("invalid").taskMinPriority(10);
    assertThat(query.list()).hasSize(2);

    query = taskService.createTaskQuery().or().taskId("invalid").taskMaxPriority(10);
    assertThat(query.list()).hasSize(12);

    query = taskService.createTaskQuery().or().taskId("invalid").taskMaxPriority(3);
    assertThat(query.list()).hasSize(9);
  }

  public void testQueryByInvalidPriority() {
    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> taskService.createTaskQuery().taskPriority(null));
  }

  public void testQueryByInvalidPriorityOr() {
    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> taskService.createTaskQuery().or().taskId("invalid").taskPriority(null));
  }

  public void testQueryByAssignee() {
    TaskQuery query = taskService.createTaskQuery().taskAssignee(GONZO);
    assertThat(query.count()).isEqualTo(1);
    assertThat(query.list()).hasSize(1);
    assertThat(query.singleResult()).isNotNull();

    query = taskService.createTaskQuery().taskAssignee(KERMIT);
    assertThat(query.count()).isEqualTo(0);
    assertThat(query.list()).hasSize(0);
    assertThat(query.singleResult()).isNull();
  }

  public void testQueryByAssigneeOr() {
    TaskQuery query = taskService.createTaskQuery().or().taskId("invalid").taskAssignee(GONZO);
    assertThat(query.count()).isEqualTo(1);
    assertThat(query.list()).hasSize(1);
    assertThat(query.singleResult()).isNotNull();

    query = taskService.createTaskQuery().or().taskId("invalid").taskAssignee(KERMIT);
    assertThat(query.count()).isEqualTo(0);
    assertThat(query.list()).hasSize(0);
    assertThat(query.singleResult()).isNull();
  }

  public void testQueryByAssigneeIds() {
	    TaskQuery query = taskService.createTaskQuery().taskAssigneeIds(asList(GONZO, KERMIT));
	    assertThat(query.count()).isEqualTo(1);
	    assertThat(query.list()).hasSize(1);
	    assertThat(query.singleResult()).isNotNull();

	    query = taskService.createTaskQuery().taskAssigneeIds(asList(KERMIT, "kermit2"));
	    assertThat(query.count()).isEqualTo(0);
	    assertThat(query.list()).hasSize(0);
	    assertThat(query.singleResult()).isNull();

	    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
	      // History
	      assertThat(historyService.createHistoricTaskInstanceQuery().taskAssigneeIds(asList(GONZO, KERMIT)).count()).isEqualTo(1);
	      assertThat(historyService.createHistoricTaskInstanceQuery().taskAssigneeIds(asList(KERMIT, "kermit2")).count()).isEqualTo(0);
	    }

	    Task adhocTask = taskService.newTask();
	    adhocTask.setName("test");
	    adhocTask.setAssignee("testAssignee");
	    taskService.saveTask(adhocTask);

	    query = taskService.createTaskQuery().taskAssigneeIds(asList(GONZO, "testAssignee"));
	    assertThat(query.count()).isEqualTo(2);
	    assertThat(query.list()).hasSize(2);

	    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
	      // History
	      assertThat(historyService.createHistoricTaskInstanceQuery().taskAssigneeIds(asList(GONZO, "testAssignee")).count()).isEqualTo(2);
	    }

	    taskService.deleteTask(adhocTask.getId(), true);
	  }

	  public void testQueryByAssigneeIdsOr() {
	    TaskQuery query = taskService.createTaskQuery().or().taskId("invalid").taskAssigneeIds(asList(GONZO, KERMIT));
	    assertThat(query.count()).isEqualTo(1);
	    assertThat(query.list()).hasSize(1);
	    assertThat(query.singleResult()).isNotNull();

	    query = taskService.createTaskQuery().or().taskId("invalid").taskAssigneeIds(asList(KERMIT, "kermit2"));
	    assertThat(query.count()).isEqualTo(0);
	    assertThat(query.list()).hasSize(0);
	    assertThat(query.singleResult()).isNull();

	    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
	      assertThat(historyService.createHistoricTaskInstanceQuery().or().taskId("invalid").taskAssigneeIds(asList(GONZO, KERMIT)).count()).isEqualTo(1);
	      assertThat(historyService.createHistoricTaskInstanceQuery().or().taskId("invalid").taskAssigneeIds(asList(KERMIT, "kermit2")).count()).isEqualTo(0);
	    }

	    Task adhocTask = taskService.newTask();
	    adhocTask.setName("test");
	    adhocTask.setAssignee("testAssignee");
	    taskService.saveTask(adhocTask);

	    query = taskService.createTaskQuery().or().taskId("invalid").taskAssigneeIds(asList(GONZO, "testAssignee"));
	    assertThat(query.count()).isEqualTo(2);
	    assertThat(query.list()).hasSize(2);

	    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
	      assertThat(historyService.createHistoricTaskInstanceQuery().or().taskId("invalid").taskAssigneeIds(asList(GONZO, "testAssignee")).count()).isEqualTo(2);
	    }

	    taskService.deleteTask(adhocTask.getId(), true);
	}

  public void testQueryByInvolvedUser() {
    try {
      Task adhocTask = taskService.newTask();
      adhocTask.setAssignee(KERMIT);
      adhocTask.setOwner(FOZZIE);
      taskService.saveTask(adhocTask);
      taskService.addUserIdentityLink(adhocTask.getId(), GONZO, "customType");

      assertThat(taskService.getIdentityLinksForTask(adhocTask.getId())).hasSize(3);

      assertThat(taskService.createTaskQuery().taskId(adhocTask.getId()).taskInvolvedUser(GONZO).count()).isEqualTo(1);
      assertThat(taskService.createTaskQuery().taskId(adhocTask.getId()).taskInvolvedUser(KERMIT).count()).isEqualTo(1);
      assertThat(taskService.createTaskQuery().taskId(adhocTask.getId()).taskInvolvedUser(FOZZIE).count()).isEqualTo(1);

    } finally {
      List<Task> allTasks = taskService.createTaskQuery().list();
      for(Task task : allTasks) {
        if(task.getExecutionId() == null) {
          taskService.deleteTask(task.getId());
          if(processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
            historyService.deleteHistoricTaskInstance(task.getId());
          }
        }
      }
    }
  }

  public void testQueryByInvolvedGroup() {
    try {
      Task adhocTask = taskService.newTask();
      adhocTask.setAssignee(KERMIT);
      adhocTask.setOwner(FOZZIE);
      taskService.saveTask(adhocTask);
      taskService.addGroupIdentityLink(adhocTask.getId(), "group1", IdentityLinkType.PARTICIPANT);

      List<String> groups = new ArrayList<String>();
      groups.add("group1");

      assertThat(taskService.getIdentityLinksForTask(adhocTask.getId())).hasSize(3);
      assertThat(taskService.createTaskQuery()
          .taskId(adhocTask.getId()).taskInvolvedGroupsIn(groups).count()).isEqualTo(1);
    } finally {
      List<Task> allTasks = taskService.createTaskQuery().list();
      for (Task task : allTasks) {
        if (task.getExecutionId() == null) {
          taskService.deleteTask(task.getId());
          if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
            historyService.deleteHistoricTaskInstance(task.getId());
          }
        }
      }
    }
  }

  public void testQueryByInvolvedUserOr() {
    try {
      Task adhocTask = taskService.newTask();
      adhocTask.setAssignee(KERMIT);
      adhocTask.setOwner(FOZZIE);
      taskService.saveTask(adhocTask);
      taskService.addUserIdentityLink(adhocTask.getId(), GONZO, "customType");

      assertThat(taskService.getIdentityLinksForTask(adhocTask.getId())).hasSize(3);

      assertThat(taskService.createTaskQuery().taskId(adhocTask.getId()).or().taskId("invalid").taskInvolvedUser(GONZO).count()).isEqualTo(1);
      assertThat(taskService.createTaskQuery().taskId(adhocTask.getId()).or().taskId("invalid").taskInvolvedUser(KERMIT).count()).isEqualTo(1);
      assertThat(taskService.createTaskQuery().taskId(adhocTask.getId()).or().taskId("invalid").taskInvolvedUser(FOZZIE).count()).isEqualTo(1);

    } finally {
      List<Task> allTasks = taskService.createTaskQuery().list();
      for (Task task : allTasks) {
        if (task.getExecutionId() == null) {
          taskService.deleteTask(task.getId());
          if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
            historyService.deleteHistoricTaskInstance(task.getId());
          }
        }
      }
    }
  }

  public void testQueryByNullAssignee() {
    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> taskService.createTaskQuery().taskAssignee(null).list());
  }

  public void testQueryByNullAssigneeOr() {
      assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
          .isThrownBy(() -> taskService.createTaskQuery().or().taskId("invalid").taskAssignee(null).list());
  }

  public void testQueryByUnassigned() {
    TaskQuery query = taskService.createTaskQuery().taskUnassigned();
    assertThat(query.count()).isEqualTo(11);
    assertThat(query.list()).hasSize(11);
  }

  public void testQueryByUnassignedOr() {
    TaskQuery query = taskService.createTaskQuery().or().taskId("invalid").taskUnassigned();
    assertThat(query.count()).isEqualTo(11);
    assertThat(query.list()).hasSize(11);
  }

  public void testQueryByCandidateUser() {
    TaskQuery query = taskService.createTaskQuery().taskCandidateUser(KERMIT, KERMITSGROUPS);
    assertThat(query.count()).isEqualTo(11);
    assertThat(query.list()).hasSize(11);
    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> query.singleResult());

    TaskQuery queryFozzie = taskService.createTaskQuery().taskCandidateUser(FOZZIE, FOZZIESGROUPS);
    assertThat(queryFozzie.count()).isEqualTo(3);
    assertThat(queryFozzie.list()).hasSize(3);
    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> queryFozzie.singleResult());
  }

  public void testQueryByCandidateUserOr() {
    TaskQuery query = taskService.createTaskQuery().or().taskId("invalid").taskCandidateUser(KERMIT, KERMITSGROUPS);
    assertThat(query.count()).isEqualTo(11);
    assertThat(query.list()).hasSize(11);
    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> query.singleResult());

    TaskQuery queryFozzie = taskService.createTaskQuery().or().taskId("invalid").taskCandidateUser(FOZZIE, FOZZIESGROUPS);
    assertThat(queryFozzie.count()).isEqualTo(3);
    assertThat(queryFozzie.list()).hasSize(3);
    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> queryFozzie.singleResult());
  }

  public void testQueryByNullCandidateUser() {
    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> taskService.createTaskQuery().taskCandidateUser(null,null).list());
  }

  public void testQueryByNullCandidateUserOr() {
    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> taskService.createTaskQuery().or().taskId("invalid").taskCandidateUser(null,null).list());
  }

  public void testQueryByCandidateGroup() {
    TaskQuery query = taskService.createTaskQuery().taskCandidateGroup("management");
    assertThat(query.count()).isEqualTo(3);
    assertThat(query.list()).hasSize(3);
    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> query.singleResult());

    TaskQuery querySales = taskService.createTaskQuery().taskCandidateGroup("sales");
    assertThat(querySales.count()).isEqualTo(0);
    assertThat(querySales.list()).hasSize(0);
  }

  public void testQueryByCandidateGroupOr() {
    TaskQuery query = taskService.createTaskQuery().or().taskId("invalid").taskCandidateGroup("management");
    assertThat(query.count()).isEqualTo(3);
    assertThat(query.list()).hasSize(3);
    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> query.singleResult());

    TaskQuery querySales = taskService.createTaskQuery().or().taskId("invalid").taskCandidateGroup("sales");
    assertThat(querySales.count()).isEqualTo(0);
    assertThat(querySales.list()).hasSize(0);
  }

  public void testQueryByCandidateOrAssigned() {
    TaskQuery query = taskService.createTaskQuery().taskCandidateOrAssigned(KERMIT,KERMITSGROUPS);
    assertThat(query.count()).isEqualTo(11);
    List<Task> tasks = query.list();
    assertThat(tasks).hasSize(11);

    // if dbIdentityUsed set false in process engine configuration of using
    // custom session factory of GroupIdentityManager
    ArrayList<String> candidateGroups = new ArrayList<String>();
    candidateGroups.add("management");
    candidateGroups.add("accountancy");
    candidateGroups.add("noexist");
    query = taskService.createTaskQuery().taskCandidateGroupIn(candidateGroups).taskCandidateOrAssigned(KERMIT,KERMITSGROUPS);
    assertThat(query.count()).isEqualTo(11);
    tasks = query.list();
    assertThat(tasks).hasSize(11);

    query = taskService.createTaskQuery().taskCandidateOrAssigned(FOZZIE,FOZZIESGROUPS);
    assertThat(query.count()).isEqualTo(3);
    assertThat(query.list()).hasSize(3);

    // create a new task that no identity link and assignee to kermit
    Task task = taskService.newTask();
    task.setName("assigneeToKermit");
    task.setDescription("testTask description");
    task.setPriority(3);
    task.setAssignee(KERMIT);
    taskService.saveTask(task);

    query = taskService.createTaskQuery().taskCandidateOrAssigned(KERMIT,KERMITSGROUPS);
    assertThat(query.count()).isEqualTo(12);
    tasks = query.list();
    assertThat(tasks).hasSize(12);

    Task assigneeToKermit = taskService.createTaskQuery().taskName("assigneeToKermit").singleResult();
    taskService.deleteTask(assigneeToKermit.getId());
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
      historyService.deleteHistoricTaskInstance(assigneeToKermit.getId());
    }
  }


  public void testQueryByCandidateOrAssignedWithUserGroupProxy() {
    //don't specify groups in query calls, instead get them through UserGroupLookupProxy (which could be remote service)

    Mockito.when(userGroupManager.getUserGroups(KERMIT)).thenReturn(KERMITSGROUPS);
    Mockito.when(userGroupManager.getUserGroups(GONZO)).thenReturn(GONZOSGROUPS);
    Mockito.when(userGroupManager.getUserGroups(FOZZIE)).thenReturn(FOZZIESGROUPS);

    TaskQuery query = taskService.createTaskQuery().taskCandidateOrAssigned(KERMIT);
    assertThat(query.count()).isEqualTo(11);
    List<Task> tasks = query.list();
    assertThat(tasks).hasSize(11);

    // if dbIdentityUsed set false in process engine configuration of using
    // custom session factory of GroupIdentityManager
    ArrayList<String> candidateGroups = new ArrayList<String>();
    candidateGroups.add("management");
    candidateGroups.add("accountancy");
    candidateGroups.add("noexist");
    query = taskService.createTaskQuery().taskCandidateGroupIn(candidateGroups).taskCandidateOrAssigned(KERMIT);
    assertThat(query.count()).isEqualTo(11);
    tasks = query.list();
    assertThat(tasks).hasSize(11);

    query = taskService.createTaskQuery().taskCandidateOrAssigned(FOZZIE);
    assertThat(query.count()).isEqualTo(3);
    assertThat(query.list()).hasSize(3);

    // create a new task that no identity link and assignee to kermit
    Task task = taskService.newTask();
    task.setName("assigneeToKermit");
    task.setDescription("testTask description");
    task.setPriority(3);
    task.setAssignee(KERMIT);
    taskService.saveTask(task);

    query = taskService.createTaskQuery().taskCandidateOrAssigned(KERMIT);
    assertThat(query.count()).isEqualTo(12);
    tasks = query.list();
    assertThat(tasks).hasSize(12);

    Task assigneeToKermit = taskService.createTaskQuery().taskName("assigneeToKermit").singleResult();
    taskService.deleteTask(assigneeToKermit.getId());
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
      historyService.deleteHistoricTaskInstance(assigneeToKermit.getId());
    }
  }


  public void testQueryByCandidateOrAssignedWithNoGroups() {
    //don't specify groups in query calls, instead get them through UserGroupLookupProxy (which could be remote service)

    Mockito.when(userGroupManager.getUserGroups(SCOOTER)).thenReturn(SCOOTERSGROUPS);


    // create a new task that no identity link and assignee to scooter
    Task task = taskService.newTask();
    task.setName("assigneeToScooter");
    task.setDescription("testTask description");
    task.setPriority(3);
    task.setAssignee(SCOOTER);
    taskService.saveTask(task);


    //should see task as assignee
    TaskQuery query = taskService.createTaskQuery().taskCandidateOrAssigned(SCOOTER);
    assertThat(query.count()).isEqualTo(1);
    List<Task> tasks = query.list();
    assertThat(tasks).hasSize(1);

    //also if an or condition is used
    query = taskService.createTaskQuery().or().taskId("dummyidforor").taskCandidateOrAssigned(SCOOTER);
    assertThat(query.count()).isEqualTo(1);
    tasks = query.list();
    assertThat(tasks).hasSize(1);

    taskService.addCandidateUser(task.getId(),SCOOTER);

    //should see task as both assignee and candidate
    query = taskService.createTaskQuery().or().taskId("dummyidforor").taskCandidateOrAssigned(SCOOTER);
    assertThat(query.count()).isEqualTo(1);
    tasks = query.list();
    assertThat(tasks).hasSize(1);

    //also if an or condition is used
    query = taskService.createTaskQuery().or().taskId("dummyidforor").taskCandidateOrAssigned(SCOOTER);
    assertThat(query.count()).isEqualTo(1);
    tasks = query.list();
    assertThat(tasks).hasSize(1);

    Task assigneeToScooter = taskService.createTaskQuery().taskName("assigneeToScooter").singleResult();
    taskService.deleteTask(assigneeToScooter.getId());
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
      historyService.deleteHistoricTaskInstance(assigneeToScooter.getId());
    }
  }


  public void testQueryByCandidateOrAssignedOr() {
    TaskQuery query = taskService.createTaskQuery().or().taskId("invalid").taskCandidateOrAssigned(KERMIT,KERMITSGROUPS);
    assertThat(query.count()).isEqualTo(11);
    List<Task> tasks = query.list();
    assertThat(tasks).hasSize(11);

    // if dbIdentityUsed set false in process engine configuration of using
    // custom session factory of GroupIdentityManager
    ArrayList<String> candidateGroups = new ArrayList<String>();
    candidateGroups.add("management");
    candidateGroups.add("accountancy");
    candidateGroups.add("noexist");
    query = taskService.createTaskQuery().or().taskId("invalid").taskCandidateGroupIn(candidateGroups).taskCandidateOrAssigned(KERMIT,KERMITSGROUPS);
    query = taskService.createTaskQuery().or().taskId("invalid").taskCandidateGroupIn(candidateGroups).taskCandidateOrAssigned(KERMIT,KERMITSGROUPS);
    assertThat(query.count()).isEqualTo(11);
    tasks = query.list();
    assertThat(tasks).hasSize(11);

    query = taskService.createTaskQuery().or().taskId("invalid").taskCandidateOrAssigned(FOZZIE,FOZZIESGROUPS);
    assertThat(query.count()).isEqualTo(3);
    assertThat(query.list()).hasSize(3);

    // create a new task that no identity link and assignee to kermit
    Task task = taskService.newTask();
    task.setName("assigneeToKermit");
    task.setDescription("testTask description");
    task.setPriority(3);
    task.setAssignee(KERMIT);
    taskService.saveTask(task);

    query = taskService.createTaskQuery().or().taskId("invalid").taskCandidateOrAssigned(KERMIT,KERMITSGROUPS);
    assertThat(query.count()).isEqualTo(12);
    tasks = query.list();
    assertThat(tasks).hasSize(12);

    Task assigneeToKermit = taskService.createTaskQuery().or().taskId("invalid").taskName("assigneeToKermit").singleResult();
    taskService.deleteTask(assigneeToKermit.getId());
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
      historyService.deleteHistoricTaskInstance(assigneeToKermit.getId());
    }
  }

  public void testQueryByNullCandidateGroup() {
    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> taskService.createTaskQuery().taskCandidateGroup(null).list());
  }

  public void testQueryByNullCandidateGroupOr() {
    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> taskService.createTaskQuery().or().taskId("invalid").taskCandidateGroup(null).list());
  }

  public void testQueryByCandidateGroupIn() {
    List<String> groups = asList("management", "accountancy");
    TaskQuery queryForException = taskService.createTaskQuery().taskCandidateGroupIn(groups);
    assertThat(queryForException.count()).isEqualTo(5);
    assertThat(queryForException.list()).hasSize(5);

    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> queryForException.singleResult());

    TaskQuery query = taskService.createTaskQuery().taskCandidateUser(KERMIT, groups);
    assertThat(query.count()).isEqualTo(11);
    assertThat(query.list()).hasSize(11);

    query = taskService.createTaskQuery().taskCandidateUser(KERMIT, asList("unexisting"));
    assertThat(query.count()).isEqualTo(6);
    assertThat(query.list()).hasSize(6);

    query = taskService.createTaskQuery().taskCandidateUser("unexisting", asList("unexisting"));
    assertThat(query.count()).isEqualTo(0);
    assertThat(query.list()).hasSize(0);

    // Unexisting groups or groups that don't have candidate tasks shouldn't influence other results
    groups = asList("management", "accountancy", "sales", "unexising");
    query = taskService.createTaskQuery().taskCandidateGroupIn(groups);
    assertThat(query.count()).isEqualTo(5);
    assertThat(query.list()).hasSize(5);
  }

  public void testQueryByCandidateGroupInOr() {
    List<String> groups = asList("management", "accountancy");
    TaskQuery queryForException = taskService.createTaskQuery().or().taskId("invalid").taskCandidateGroupIn(groups);
    assertThat(queryForException.count()).isEqualTo(5);
    assertThat(queryForException.list()).hasSize(5);

    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> queryForException.singleResult());

    TaskQuery query = taskService.createTaskQuery().or().taskCandidateUser(KERMIT,KERMITSGROUPS).taskCandidateGroupIn(groups).endOr();
    assertThat(query.count()).isEqualTo(11);
    assertThat(query.list()).hasSize(11);

    query = taskService.createTaskQuery().or().taskCandidateUser(KERMIT,KERMITSGROUPS).taskCandidateGroup("unexisting").endOr();
    assertThat(query.count()).isEqualTo(6);
    assertThat(query.list()).hasSize(6);

    query = taskService.createTaskQuery().or().taskCandidateUser("unexisting",null).taskCandidateGroup("unexisting").endOr();
    assertThat(query.count()).isEqualTo(0);
    assertThat(query.list()).hasSize(0);

    query = taskService.createTaskQuery().or().taskCandidateUser(KERMIT,KERMITSGROUPS).taskCandidateGroupIn(groups).endOr()
        .or().taskCandidateUser(GONZO,GONZOSGROUPS).taskCandidateGroupIn(groups);
    assertThat(query.count()).isEqualTo(5);
    assertThat(query.list()).hasSize(5);

    // Unexisting groups or groups that don't have candidate tasks shouldn't influence other results
    groups = asList("management", "accountancy", "sales", "unexising");
    query = taskService.createTaskQuery().or().taskId("invalid").taskCandidateGroupIn(groups);
    assertThat(query.count()).isEqualTo(5);
    assertThat(query.list()).hasSize(5);
  }


  public void testQueryByCandidateGroupInOrUsingUserGroupLookupProxy() {
    //don't specify groups in query calls, instead get them through UserGroupLookupProxy (which could be remote service)

    Mockito.when(userGroupManager.getUserGroups(KERMIT)).thenReturn(KERMITSGROUPS);
    Mockito.when(userGroupManager.getUserGroups(GONZO)).thenReturn(GONZOSGROUPS);

    List<String> groups = asList("management", "accountancy");
    TaskQuery queryForException = taskService.createTaskQuery().or().taskId("invalid").taskCandidateGroupIn(groups);
    assertThat(queryForException.count()).isEqualTo(5);
    assertThat(queryForException.list()).hasSize(5);

    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> queryForException.singleResult());

    TaskQuery query = taskService.createTaskQuery().or().taskCandidateUser(KERMIT).taskCandidateGroupIn(groups).endOr();
    assertThat(query.count()).isEqualTo(11);
    assertThat(query.list()).hasSize(11);

    query = taskService.createTaskQuery().or().taskCandidateUser(KERMIT).taskCandidateGroup("unexisting").endOr();
    assertThat(query.count()).isEqualTo(6);
    assertThat(query.list()).hasSize(6);

    query = taskService.createTaskQuery().or().taskCandidateUser(KERMIT).taskCandidateGroupIn(groups).endOr()
            .or().taskCandidateUser(GONZO).taskCandidateGroupIn(groups);
    assertThat(query.count()).isEqualTo(5);
    assertThat(query.list()).hasSize(5);

  }

  public void testQueryByNullCandidateGroupIn() {
    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> taskService.createTaskQuery().taskCandidateGroupIn(null).list());
    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> taskService.createTaskQuery().taskCandidateGroupIn(new ArrayList<String>()).list());
  }

  public void testQueryByNullCandidateGroupInOr() {
    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> taskService.createTaskQuery().or().taskId("invalid").taskCandidateGroupIn(null).list());
    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> taskService.createTaskQuery().or().taskId("invalid").taskCandidateGroupIn(new ArrayList<String>()).list());
  }

  public void testQueryByDelegationState() {
    TaskQuery query = taskService.createTaskQuery().taskDelegationState(null);
    assertThat(query.count()).isEqualTo(12);
    assertThat(query.list()).hasSize(12);
    query = taskService.createTaskQuery().taskDelegationState(DelegationState.PENDING);
    assertThat(query.count()).isEqualTo(0);
    assertThat(query.list()).hasSize(0);
    query = taskService.createTaskQuery().taskDelegationState(DelegationState.RESOLVED);
    assertThat(query.count()).isEqualTo(0);
    assertThat(query.list()).hasSize(0);

    String taskId = taskService.createTaskQuery().taskAssignee(GONZO).singleResult().getId();
    taskService.delegateTask(taskId, KERMIT);

    query = taskService.createTaskQuery().taskDelegationState(null);
    assertThat(query.count()).isEqualTo(11);
    assertThat(query.list()).hasSize(11);
    query = taskService.createTaskQuery().taskDelegationState(DelegationState.PENDING);
    assertThat(query.count()).isEqualTo(1);
    assertThat(query.list()).hasSize(1);
    query = taskService.createTaskQuery().taskDelegationState(DelegationState.RESOLVED);
    assertThat(query.count()).isEqualTo(0);
    assertThat(query.list()).hasSize(0);

    taskService.resolveTask(taskId);

    query = taskService.createTaskQuery().taskDelegationState(null);
    assertThat(query.count()).isEqualTo(11);
    assertThat(query.list()).hasSize(11);
    query = taskService.createTaskQuery().taskDelegationState(DelegationState.PENDING);
    assertThat(query.count()).isEqualTo(0);
    assertThat(query.list()).hasSize(0);
    query = taskService.createTaskQuery().taskDelegationState(DelegationState.RESOLVED);
    assertThat(query.count()).isEqualTo(1);
    assertThat(query.list()).hasSize(1);
  }

  public void testQueryByDelegationStateOr() {
    TaskQuery query = taskService.createTaskQuery().or().taskId("invalid").taskDelegationState(null);
    assertThat(query.count()).isEqualTo(12);
    assertThat(query.list()).hasSize(12);
    query = taskService.createTaskQuery().or().taskId("invalid").taskDelegationState(DelegationState.PENDING);
    assertThat(query.count()).isEqualTo(0);
    assertThat(query.list()).hasSize(0);
    query = taskService.createTaskQuery().or().taskId("invalid").taskDelegationState(DelegationState.RESOLVED);
    assertThat(query.count()).isEqualTo(0);
    assertThat(query.list()).hasSize(0);

    String taskId = taskService.createTaskQuery().or().taskId("invalid").taskAssignee(GONZO).singleResult().getId();
    taskService.delegateTask(taskId, KERMIT);

    query = taskService.createTaskQuery().or().taskId("invalid").taskDelegationState(null);
    assertThat(query.count()).isEqualTo(11);
    assertThat(query.list()).hasSize(11);
    query = taskService.createTaskQuery().or().taskId("invalid").taskDelegationState(DelegationState.PENDING);
    assertThat(query.count()).isEqualTo(1);
    assertThat(query.list()).hasSize(1);
    query = taskService.createTaskQuery().or().taskId("invalid").taskDelegationState(DelegationState.RESOLVED);
    assertThat(query.count()).isEqualTo(0);
    assertThat(query.list()).hasSize(0);

    taskService.resolveTask(taskId);

    query = taskService.createTaskQuery().or().taskId("invalid").taskDelegationState(null);
    assertThat(query.count()).isEqualTo(11);
    assertThat(query.list()).hasSize(11);
    query = taskService.createTaskQuery().or().taskId("invalid").taskDelegationState(DelegationState.PENDING);
    assertThat(query.count()).isEqualTo(0);
    assertThat(query.list()).hasSize(0);
    query = taskService.createTaskQuery().or().taskId("invalid").taskDelegationState(DelegationState.RESOLVED);
    assertThat(query.count()).isEqualTo(1);
    assertThat(query.list()).hasSize(1);
  }

  public void testQueryCreatedOn() throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");

    // Exact matching of createTime, should result in 6 tasks
    Date createTime = sdf.parse("01/01/2001 01:01:01.000");

    TaskQuery query = taskService.createTaskQuery().taskCreatedOn(createTime);
    assertThat(query.count()).isEqualTo(6);
    assertThat(query.list()).hasSize(6);
  }

  public void testQueryCreatedOnOr() throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");

    // Exact matching of createTime, should result in 6 tasks
    Date createTime = sdf.parse("01/01/2001 01:01:01.000");

    TaskQuery query = taskService.createTaskQuery().or().taskId("invalid").taskCreatedOn(createTime);
    assertThat(query.count()).isEqualTo(6);
    assertThat(query.list()).hasSize(6);
  }

  public void testQueryCreatedBefore() throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");

    // Should result in 7 tasks
    Date before = sdf.parse("03/02/2002 02:02:02.000");

    TaskQuery query = taskService.createTaskQuery().taskCreatedBefore(before);
    assertThat(query.count()).isEqualTo(7);
    assertThat(query.list()).hasSize(7);

    before = sdf.parse("01/01/2001 01:01:01.000");
    query = taskService.createTaskQuery().taskCreatedBefore(before);
    assertThat(query.count()).isEqualTo(0);
    assertThat(query.list()).hasSize(0);
  }

  public void testQueryCreatedBeforeOr() throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");

    // Should result in 7 tasks
    Date before = sdf.parse("03/02/2002 02:02:02.000");

    TaskQuery query = taskService.createTaskQuery().or().taskId("invalid").taskCreatedBefore(before);
    assertThat(query.count()).isEqualTo(7);
    assertThat(query.list()).hasSize(7);

    before = sdf.parse("01/01/2001 01:01:01.000");
    query = taskService.createTaskQuery().or().taskId("invalid").taskCreatedBefore(before);
    assertThat(query.count()).isEqualTo(0);
    assertThat(query.list()).hasSize(0);
  }

  public void testQueryCreatedAfter() throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");

    // Should result in 3 tasks
    Date after = sdf.parse("03/03/2003 03:03:03.000");

    TaskQuery query = taskService.createTaskQuery().taskCreatedAfter(after);
    assertThat(query.count()).isEqualTo(3);
    assertThat(query.list()).hasSize(3);

    after = sdf.parse("05/05/2005 05:05:05.000");
    query = taskService.createTaskQuery().taskCreatedAfter(after);
    assertThat(query.count()).isEqualTo(0);
    assertThat(query.list()).hasSize(0);
  }

  public void testQueryCreatedAfterOr() throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");

    // Should result in 3 tasks
    Date after = sdf.parse("03/03/2003 03:03:03.000");

    TaskQuery query = taskService.createTaskQuery().or().taskId("invalid").taskCreatedAfter(after);
    assertThat(query.count()).isEqualTo(3);
    assertThat(query.list()).hasSize(3);

    after = sdf.parse("05/05/2005 05:05:05.000");
    query = taskService.createTaskQuery().or().taskId("invalid").taskCreatedAfter(after);
    assertThat(query.count()).isEqualTo(0);
    assertThat(query.list()).hasSize(0);
  }

  @Deployment(resources = "org/activiti/engine/test/api/task/taskDefinitionProcess.bpmn20.xml")
  public void testTaskDefinitionKey() throws Exception {

    // Start process instance, 2 tasks will be available
    runtimeService.startProcessInstanceByKey("taskDefinitionKeyProcess");

    // 1 task should exist with key "taskKey1"
    List<Task> tasks = taskService.createTaskQuery().taskDefinitionKey("taskKey1").list();
    assertThat(tasks).isNotNull();
    assertThat(tasks).hasSize(1);

    assertThat(tasks.get(0).getTaskDefinitionKey()).isEqualTo("taskKey1");

    // No task should be found with unexisting key
    Long count = taskService.createTaskQuery().taskDefinitionKey("unexistingKey").count();
    assertThat(count.longValue()).isEqualTo(0L);
  }

  @Deployment(resources = "org/activiti/engine/test/api/task/taskDefinitionProcess.bpmn20.xml")
  public void testTaskDefinitionKeyOr() throws Exception {

    // Start process instance, 2 tasks will be available
    runtimeService.startProcessInstanceByKey("taskDefinitionKeyProcess");

    // 1 task should exist with key "taskKey1"
    List<Task> tasks = taskService.createTaskQuery().or().taskId("invalid").taskDefinitionKey("taskKey1").list();
    assertThat(tasks).isNotNull();
    assertThat(tasks).hasSize(1);

    assertThat(tasks.get(0).getTaskDefinitionKey()).isEqualTo("taskKey1");

    // No task should be found with unexisting key
    Long count = taskService.createTaskQuery().or().taskId("invalid").taskDefinitionKey("unexistingKey").count();
    assertThat(count.longValue()).isEqualTo(0L);
  }

  @Deployment(resources = "org/activiti/engine/test/api/task/taskDefinitionProcess.bpmn20.xml")
  public void testTaskDefinitionKeyLike() throws Exception {

    // Start process instance, 2 tasks will be available
    runtimeService.startProcessInstanceByKey("taskDefinitionKeyProcess");

    // Ends with matching, TaskKey1 and TaskKey123 match
    List<Task> tasks = taskService.createTaskQuery().taskDefinitionKeyLike("taskKey1%").orderByTaskName().asc().list();
    assertThat(tasks).isNotNull();
    assertThat(tasks).hasSize(2);

    assertThat(tasks.get(0).getTaskDefinitionKey()).isEqualTo("taskKey1");
    assertThat(tasks.get(1).getTaskDefinitionKey()).isEqualTo("taskKey123");

    // Starts with matching, TaskKey123 matches
    tasks = taskService.createTaskQuery().taskDefinitionKeyLike("%123").orderByTaskName().asc().list();
    assertThat(tasks).isNotNull();
    assertThat(tasks).hasSize(1);

    assertThat(tasks.get(0).getTaskDefinitionKey()).isEqualTo("taskKey123");

    // Contains matching, TaskKey123 matches
    tasks = taskService.createTaskQuery().taskDefinitionKeyLike("%Key12%").orderByTaskName().asc().list();
    assertThat(tasks).isNotNull();
    assertThat(tasks).hasSize(1);

    assertThat(tasks.get(0).getTaskDefinitionKey()).isEqualTo("taskKey123");

    // No task should be found with unexisting key
    Long count = taskService.createTaskQuery().taskDefinitionKeyLike("%unexistingKey%").count();
    assertThat(count.longValue()).isEqualTo(0L);
  }

  @Deployment(resources = "org/activiti/engine/test/api/task/taskDefinitionProcess.bpmn20.xml")
  public void testTaskDefinitionKeyLikeOr() throws Exception {

    // Start process instance, 2 tasks will be available
    runtimeService.startProcessInstanceByKey("taskDefinitionKeyProcess");

    // Ends with matching, TaskKey1 and TaskKey123 match
    List<Task> tasks = taskService.createTaskQuery().or().taskId("invalid").taskDefinitionKeyLike("taskKey1%").orderByTaskName().asc().list();
    assertThat(tasks).isNotNull();
    assertThat(tasks).hasSize(2);

    assertThat(tasks.get(0).getTaskDefinitionKey()).isEqualTo("taskKey1");
    assertThat(tasks.get(1).getTaskDefinitionKey()).isEqualTo("taskKey123");

    // Starts with matching, TaskKey123 matches
    tasks = taskService.createTaskQuery().or().taskDefinitionKeyLike("%123").taskId("invalid").orderByTaskName().asc().list();
    assertThat(tasks).isNotNull();
    assertThat(tasks).hasSize(1);

    assertThat(tasks.get(0).getTaskDefinitionKey()).isEqualTo("taskKey123");

    // Contains matching, TaskKey123 matches
    tasks = taskService.createTaskQuery().or().taskDefinitionKeyLike("%Key12%").taskId("invalid").orderByTaskName().asc().list();
    assertThat(tasks).isNotNull();
    assertThat(tasks).hasSize(1);

    assertThat(tasks.get(0).getTaskDefinitionKey()).isEqualTo("taskKey123");

    // No task should be found with unexisting key
    Long count = taskService.createTaskQuery().or().taskId("invalid").taskDefinitionKeyLike("%unexistingKey%").count();
    assertThat(count.longValue()).isEqualTo(0L);
  }

  @Deployment
  public void testTaskVariableValueEquals() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

    // No task should be found for an unexisting var
    assertThat(taskService.createTaskQuery().taskVariableValueEquals("unexistingVar", "value").count()).isEqualTo(0);

    // Create a map with a variable for all default types
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("longVar", 928374L);
    variables.put("shortVar", (short) 123);
    variables.put("integerVar", 1234);
    variables.put("stringVar", "stringValue");
    variables.put("booleanVar", true);
    Date date = Calendar.getInstance().getTime();
    variables.put("dateVar", date);
    variables.put("nullVar", null);

    taskService.setVariablesLocal(task.getId(), variables);

    // Test query matches
    assertThat(taskService.createTaskQuery().taskVariableValueEquals("longVar", 928374L).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().taskVariableValueEquals("shortVar", (short) 123).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().taskVariableValueEquals("integerVar", 1234).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().taskVariableValueEquals("stringVar", "stringValue").count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().taskVariableValueEquals("booleanVar", true).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().taskVariableValueEquals("dateVar", date).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().taskVariableValueEquals("nullVar", null).count()).isEqualTo(1);

    // Test query for other values on existing variables
    assertThat(taskService.createTaskQuery().taskVariableValueEquals("longVar", 999L).count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().taskVariableValueEquals("shortVar", (short) 999).count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().taskVariableValueEquals("integerVar", 999).count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().taskVariableValueEquals("stringVar", "999").count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().taskVariableValueEquals("booleanVar", false).count()).isEqualTo(0);
    Calendar otherDate = Calendar.getInstance();
    otherDate.add(Calendar.YEAR, 1);
    assertThat(taskService.createTaskQuery().taskVariableValueEquals("dateVar", otherDate.getTime()).count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().taskVariableValueEquals("nullVar", "999").count()).isEqualTo(0);

    // Test query for not equals
    assertThat(taskService.createTaskQuery().taskVariableValueNotEquals("longVar", 999L).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().taskVariableValueNotEquals("shortVar", (short) 999).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().taskVariableValueNotEquals("integerVar", 999).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().taskVariableValueNotEquals("stringVar", "999").count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().taskVariableValueNotEquals("booleanVar", false).count()).isEqualTo(1);

    // Test value-only variable equals
    assertThat(taskService.createTaskQuery().taskVariableValueEquals(928374L).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().taskVariableValueEquals((short) 123).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().taskVariableValueEquals(1234).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().taskVariableValueEquals("stringValue").count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().taskVariableValueEquals(true).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().taskVariableValueEquals(date).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().taskVariableValueEquals(null).count()).isEqualTo(1);

    assertThat(taskService.createTaskQuery().taskVariableValueEquals(999999L).count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().taskVariableValueEquals((short) 999).count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().taskVariableValueEquals(9999).count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().taskVariableValueEquals("unexistingstringvalue").count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().taskVariableValueEquals(false).count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().taskVariableValueEquals(otherDate.getTime()).count()).isEqualTo(0);

    assertThat(taskService.createTaskQuery().taskVariableValueLike("stringVar", "string%").count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().taskVariableValueLike("stringVar", "String%").count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().taskVariableValueLike("stringVar", "%Value").count()).isEqualTo(1);

    assertThat(taskService.createTaskQuery().taskVariableValueGreaterThan("integerVar", 1000).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().taskVariableValueGreaterThan("integerVar", 1234).count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().taskVariableValueGreaterThan("integerVar", 1240).count()).isEqualTo(0);

    assertThat(taskService.createTaskQuery().taskVariableValueGreaterThanOrEqual("integerVar", 1000).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().taskVariableValueGreaterThanOrEqual("integerVar", 1234).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().taskVariableValueGreaterThanOrEqual("integerVar", 1240).count()).isEqualTo(0);

    assertThat(taskService.createTaskQuery().taskVariableValueLessThan("integerVar", 1240).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().taskVariableValueLessThan("integerVar", 1234).count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().taskVariableValueLessThan("integerVar", 1000).count()).isEqualTo(0);

    assertThat(taskService.createTaskQuery().taskVariableValueLessThanOrEqual("integerVar", 1240).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().taskVariableValueLessThanOrEqual("integerVar", 1234).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().taskVariableValueLessThanOrEqual("integerVar", 1000).count()).isEqualTo(0);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/task/TaskQueryTest.testTaskVariableValueEquals.bpmn20.xml" })
  public void testTaskVariableValueEqualsOr() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

    // No task should be found for an unexisting var
    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueEquals("unexistingVar", "value").count()).isEqualTo(0);

    // Create a map with a variable for all default types
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("longVar", 928374L);
    variables.put("shortVar", (short) 123);
    variables.put("integerVar", 1234);
    variables.put("stringVar", "stringValue");
    variables.put("booleanVar", true);
    Date date = Calendar.getInstance().getTime();
    variables.put("dateVar", date);
    variables.put("nullVar", null);

    taskService.setVariablesLocal(task.getId(), variables);

    // Test query matches
    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueEquals("longVar", 928374L).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueEquals("shortVar", (short) 123).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueEquals("integerVar", 1234).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueEquals("stringVar", "stringValue").count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueEquals("booleanVar", true).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueEquals("dateVar", date).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueEquals("nullVar", null).count()).isEqualTo(1);

    // Test query for other values on existing variables
    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueEquals("longVar", 999L).count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueEquals("shortVar", (short) 999).count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueEquals("integerVar", 999).count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueEquals("stringVar", "999").count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueEquals("booleanVar", false).count()).isEqualTo(0);
    Calendar otherDate = Calendar.getInstance();
    otherDate.add(Calendar.YEAR, 1);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueEquals("dateVar", otherDate.getTime()).count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueEquals("nullVar", "999").count()).isEqualTo(0);

    // Test query for not equals
    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueNotEquals("longVar", 999L).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueNotEquals("shortVar", (short) 999).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueNotEquals("integerVar", 999).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueNotEquals("stringVar", "999").count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueNotEquals("booleanVar", false).count()).isEqualTo(1);

    // Test value-only variable equals
    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueEquals(928374L).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueEquals((short) 123).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueEquals(1234).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueEquals("stringValue").count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueEquals(true).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueEquals(date).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueEquals(null).count()).isEqualTo(1);

    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueEquals(999999L).count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueEquals((short) 999).count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueEquals(9999).count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueEquals("unexistingstringvalue").count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueEquals(false).count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueEquals(otherDate.getTime()).count()).isEqualTo(0);

    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueLike("stringVar", "string%").count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueLike("stringVar", "String%").count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueLike("stringVar", "%Value").count()).isEqualTo(1);

    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueGreaterThan("integerVar", 1000).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueGreaterThan("integerVar", 1234).count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueGreaterThan("integerVar", 1240).count()).isEqualTo(0);

    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueGreaterThanOrEqual("integerVar", 1000).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueGreaterThanOrEqual("integerVar", 1234).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueGreaterThanOrEqual("integerVar", 1240).count()).isEqualTo(0);

    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueLessThan("integerVar", 1240).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueLessThan("integerVar", 1234).count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueLessThan("integerVar", 1000).count()).isEqualTo(0);

    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueLessThanOrEqual("integerVar", 1240).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueLessThanOrEqual("integerVar", 1234).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueLessThanOrEqual("integerVar", 1000).count()).isEqualTo(0);
  }

  @Deployment
  public void testProcessVariableValueEquals() throws Exception {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("longVar", 928374L);
    variables.put("shortVar", (short) 123);
    variables.put("integerVar", 1234);
    variables.put("stringVar", "stringValue");
    variables.put("booleanVar", true);
    Date date = Calendar.getInstance().getTime();
    variables.put("dateVar", date);
    variables.put("nullVar", null);

    // Start process-instance with all types of variables
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);

    // Test query matches
    assertThat(taskService.createTaskQuery().processVariableValueEquals("longVar", 928374L).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().processVariableValueEquals("shortVar", (short) 123).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().processVariableValueEquals("integerVar", 1234).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().processVariableValueEquals("stringVar", "stringValue").count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().processVariableValueEquals("booleanVar", true).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().processVariableValueEquals("dateVar", date).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().processVariableValueEquals("nullVar", null).count()).isEqualTo(1);

    // Test query for other values on existing variables
    assertThat(taskService.createTaskQuery().processVariableValueEquals("longVar", 999L).count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().processVariableValueEquals("shortVar", (short) 999).count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().processVariableValueEquals("integerVar", 999).count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().processVariableValueEquals("stringVar", "999").count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().processVariableValueEquals("booleanVar", false).count()).isEqualTo(0);
    Calendar otherDate = Calendar.getInstance();
    otherDate.add(Calendar.YEAR, 1);
    assertThat(taskService.createTaskQuery().processVariableValueEquals("dateVar", otherDate.getTime()).count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().processVariableValueEquals("nullVar", "999").count()).isEqualTo(0);

    // Test querying for task variables don't match the process-variables
    assertThat(taskService.createTaskQuery().taskVariableValueEquals("longVar", 928374L).count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().taskVariableValueEquals("shortVar", (short) 123).count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().taskVariableValueEquals("integerVar", 1234).count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().taskVariableValueEquals("stringVar", "stringValue").count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().taskVariableValueEquals("booleanVar", true).count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().taskVariableValueEquals("dateVar", date).count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().taskVariableValueEquals("nullVar", null).count()).isEqualTo(0);

    // Test querying for task variables not equals
    assertThat(taskService.createTaskQuery().processVariableValueNotEquals("longVar", 999L).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().processVariableValueNotEquals("shortVar", (short) 999).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().processVariableValueNotEquals("integerVar", 999).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().processVariableValueNotEquals("stringVar", "999").count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().processVariableValueNotEquals("booleanVar", false).count()).isEqualTo(1);

    // and query for the existing variable with NOT should result in nothing
    // found:
    assertThat(taskService.createTaskQuery().processVariableValueNotEquals("longVar", 928374L).count()).isEqualTo(0);

    // Test value-only variable equals
    assertThat(taskService.createTaskQuery().processVariableValueEquals(928374L).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().processVariableValueEquals((short) 123).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().processVariableValueEquals(1234).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().processVariableValueEquals("stringValue").count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().processVariableValueEquals(true).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().processVariableValueEquals(date).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().processVariableValueEquals(null).count()).isEqualTo(1);

    assertThat(taskService.createTaskQuery().processVariableValueEquals(999999L).count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().processVariableValueEquals((short) 999).count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().processVariableValueEquals(9999).count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().processVariableValueEquals("unexistingstringvalue").count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().processVariableValueEquals(false).count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().processVariableValueEquals(otherDate.getTime()).count()).isEqualTo(0);

    // Test combination of task-variable and process-variable
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.setVariableLocal(task.getId(), "taskVar", "theValue");
    taskService.setVariableLocal(task.getId(), "longVar", 928374L);

    assertThat(taskService.createTaskQuery().processVariableValueEquals("longVar", 928374L).taskVariableValueEquals("taskVar", "theValue").count()).isEqualTo(1);

    assertThat(taskService.createTaskQuery().processVariableValueEquals("longVar", 928374L).taskVariableValueEquals("longVar", 928374L).count()).isEqualTo(1);

    assertThat(taskService.createTaskQuery().processVariableValueEquals(928374L).taskVariableValueEquals("theValue").count()).isEqualTo(1);

    assertThat(taskService.createTaskQuery().processVariableValueEquals(928374L).taskVariableValueEquals(928374L).count()).isEqualTo(1);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/task/TaskQueryTest.testProcessVariableValueEquals.bpmn20.xml" })
  public void testProcessVariableValueEqualsOn() throws Exception {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("longVar", 928374L);
    variables.put("shortVar", (short) 123);
    variables.put("integerVar", 1234);
    variables.put("stringVar", "stringValue");
    variables.put("booleanVar", true);
    Date date = Calendar.getInstance().getTime();
    variables.put("dateVar", date);
    variables.put("nullVar", null);

    // Start process-instance with all types of variables
    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);

    // Test query matches
    assertThat(taskService.createTaskQuery().or().taskId("invalid").processVariableValueEquals("longVar", 928374L).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").processVariableValueEquals("shortVar", (short) 123).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").processVariableValueEquals("integerVar", 1234).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").processVariableValueEquals("stringVar", "stringValue").count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").processVariableValueEquals("booleanVar", true).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").processVariableValueEquals("dateVar", date).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").processVariableValueEquals("nullVar", null).count()).isEqualTo(1);

    // Test query for other values on existing variables
    assertThat(taskService.createTaskQuery().or().taskId("invalid").processVariableValueEquals("longVar", 999L).count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").processVariableValueEquals("shortVar", (short) 999).count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").processVariableValueEquals("integerVar", 999).count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").processVariableValueEquals("stringVar", "999").count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").processVariableValueEquals("booleanVar", false).count()).isEqualTo(0);
    Calendar otherDate = Calendar.getInstance();
    otherDate.add(Calendar.YEAR, 1);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").processVariableValueEquals("dateVar", otherDate.getTime()).count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").processVariableValueEquals("nullVar", "999").count()).isEqualTo(0);

    // Test querying for task variables don't match the process-variables
    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueEquals("longVar", 928374L).count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueEquals("shortVar", (short) 123).count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueEquals("integerVar", 1234).count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueEquals("stringVar", "stringValue").count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueEquals("booleanVar", true).count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueEquals("dateVar", date).count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").taskVariableValueEquals("nullVar", null).count()).isEqualTo(0);

    // Test querying for task variables not equals
    assertThat(taskService.createTaskQuery().or().taskId("invalid").processVariableValueNotEquals("longVar", 999L).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").processVariableValueNotEquals("shortVar", (short) 999).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").processVariableValueNotEquals("integerVar", 999).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").processVariableValueNotEquals("stringVar", "999").count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").processVariableValueNotEquals("booleanVar", false).count()).isEqualTo(1);

    // and query for the existing variable with NOT should result in nothing
    // found:
    assertThat(taskService.createTaskQuery().processVariableValueNotEquals("longVar", 928374L).count()).isEqualTo(0);

    // Test value-only variable equals
    assertThat(taskService.createTaskQuery().or().taskId("invalid").processVariableValueEquals(928374L).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").processVariableValueEquals((short) 123).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").processVariableValueEquals(1234).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").processVariableValueEquals("stringValue").count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").processVariableValueEquals(true).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").processVariableValueEquals(date).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").processVariableValueEquals(null).count()).isEqualTo(1);

    assertThat(taskService.createTaskQuery().or().taskId("invalid").processVariableValueEquals(999999L).count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").processVariableValueEquals((short) 999).count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").processVariableValueEquals(9999).count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").processVariableValueEquals("unexistingstringvalue").count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").processVariableValueEquals(false).count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").processVariableValueEquals(otherDate.getTime()).count()).isEqualTo(0);
  }

  @Deployment(resources = "org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml")
  public void testVariableValueEqualsIgnoreCase() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(task).isNotNull();

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("mixed", "AzerTY");
    variables.put("upper", "AZERTY");
    variables.put("lower", "azerty");
    taskService.setVariablesLocal(task.getId(), variables);

    assertThat(taskService.createTaskQuery().taskVariableValueEqualsIgnoreCase("mixed", "azerTY").count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().taskVariableValueEqualsIgnoreCase("mixed", "azerty").count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().taskVariableValueEqualsIgnoreCase("mixed", "uiop").count()).isEqualTo(0);

    assertThat(taskService.createTaskQuery().taskVariableValueEqualsIgnoreCase("upper", "azerTY").count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().taskVariableValueEqualsIgnoreCase("upper", "azerty").count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().taskVariableValueEqualsIgnoreCase("upper", "uiop").count()).isEqualTo(0);

    assertThat(taskService.createTaskQuery().taskVariableValueEqualsIgnoreCase("lower", "azerTY").count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().taskVariableValueEqualsIgnoreCase("lower", "azerty").count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().taskVariableValueEqualsIgnoreCase("lower", "uiop").count()).isEqualTo(0);

    // Test not-equals
    assertThat(taskService.createTaskQuery().taskVariableValueNotEqualsIgnoreCase("mixed", "azerTY").count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().taskVariableValueNotEqualsIgnoreCase("mixed", "azerty").count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().taskVariableValueNotEqualsIgnoreCase("mixed", "uiop").count()).isEqualTo(1);

    assertThat(taskService.createTaskQuery().taskVariableValueNotEqualsIgnoreCase("upper", "azerTY").count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().taskVariableValueNotEqualsIgnoreCase("upper", "azerty").count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().taskVariableValueNotEqualsIgnoreCase("upper", "uiop").count()).isEqualTo(1);

    assertThat(taskService.createTaskQuery().taskVariableValueNotEqualsIgnoreCase("lower", "azerTY").count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().taskVariableValueNotEqualsIgnoreCase("lower", "azerty").count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().taskVariableValueNotEqualsIgnoreCase("lower", "uiop").count()).isEqualTo(1);

  }

  @Deployment(resources = "org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml")
  public void testProcessVariableValueEqualsIgnoreCase() throws Exception {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("mixed", "AzerTY");
    variables.put("upper", "AZERTY");
    variables.put("lower", "azerty");

    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);

    assertThat(taskService.createTaskQuery().processVariableValueEqualsIgnoreCase("mixed", "azerTY").count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().processVariableValueEqualsIgnoreCase("mixed", "azerty").count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().processVariableValueEqualsIgnoreCase("mixed", "uiop").count()).isEqualTo(0);

    assertThat(taskService.createTaskQuery().processVariableValueEqualsIgnoreCase("upper", "azerTY").count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().processVariableValueEqualsIgnoreCase("upper", "azerty").count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().processVariableValueEqualsIgnoreCase("upper", "uiop").count()).isEqualTo(0);

    assertThat(taskService.createTaskQuery().processVariableValueEqualsIgnoreCase("lower", "azerTY").count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().processVariableValueEqualsIgnoreCase("lower", "azerty").count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().processVariableValueEqualsIgnoreCase("lower", "uiop").count()).isEqualTo(0);
  }

  @Deployment(resources = "org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml")
  public void testProcessVariableValueLike() throws Exception {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("mixed", "AzerTY");

    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);

    assertThat(taskService.createTaskQuery().processVariableValueLike("mixed", "Azer%").count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().processVariableValueLike("mixed", "A%").count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().processVariableValueLike("mixed", "a%").count()).isEqualTo(0);
  }

  @Deployment(resources="org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml")
  public void testProcessVariableValueLikeIgnoreCase() throws Exception {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("mixed", "AzerTY");

    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);

    assertThat(taskService.createTaskQuery().processVariableValueLikeIgnoreCase("mixed", "azer%").count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().processVariableValueLikeIgnoreCase("mixed", "a%").count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().processVariableValueLikeIgnoreCase("mixed", "Azz%").count()).isEqualTo(0);
  }

  @Deployment(resources="org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml")
  public void testProcessVariableValueGreaterThan() throws Exception {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("number", 10);

    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);

    assertThat(taskService.createTaskQuery().processVariableValueGreaterThan("number", 5).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().processVariableValueGreaterThan("number", 10).count()).isEqualTo(0);
  }

  @Deployment(resources = "org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml")
  public void testProcessVariableValueGreaterThanOrEquals() throws Exception {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("number", 10);

    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);

    assertThat(taskService.createTaskQuery().processVariableValueGreaterThanOrEqual("number", 5).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().processVariableValueGreaterThanOrEqual("number", 10).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().processVariableValueGreaterThanOrEqual("number", 11).count()).isEqualTo(0);
  }

  @Deployment(resources = "org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml")
  public void testProcessVariableValueLessThan() throws Exception {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("number", 10);

    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);

    assertThat(taskService.createTaskQuery().processVariableValueLessThan("number", 12).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().processVariableValueLessThan("number", 10).count()).isEqualTo(0);
  }

  @Deployment(resources = "org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml")
  public void testProcessVariableValueLessThanOrEquals() throws Exception {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("number", 10);

    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);

    assertThat(taskService.createTaskQuery().processVariableValueLessThanOrEqual("number", 12).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().processVariableValueLessThanOrEqual("number", 10).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().processVariableValueLessThanOrEqual("number", 8).count()).isEqualTo(0);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml" })
  public void testProcessDefinitionId() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    List<Task> tasks = taskService.createTaskQuery().processDefinitionId(processInstance.getProcessDefinitionId()).list();
    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).getProcessInstanceId()).isEqualTo(processInstance.getId());

    assertThat(taskService.createTaskQuery().processDefinitionId("unexisting").count()).isEqualTo(0);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml" })
  public void testProcessDefinitionIdOr() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    List<Task> tasks = taskService.createTaskQuery().or().taskId("invalid").processDefinitionId(processInstance.getProcessDefinitionId()).list();
    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).getProcessInstanceId()).isEqualTo(processInstance.getId());

    tasks = taskService.createTaskQuery()
        .or()
          .taskId("invalid")
          .processDefinitionId(processInstance.getProcessDefinitionId())
        .endOr()
        .or()
          .processDefinitionKey("oneTaskProcess")
          .processDefinitionId("invalid")
        .endOr()
        .list();
    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).getProcessInstanceId()).isEqualTo(processInstance.getId());

    assertThat(taskService.createTaskQuery()
        .or()
        .taskId("invalid")
        .processDefinitionId("unexisting").count())
        .isEqualTo(0);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml" })
  public void testProcessDefinitionKey() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    List<Task> tasks = taskService.createTaskQuery().processDefinitionKey("oneTaskProcess").list();
    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).getProcessInstanceId()).isEqualTo(processInstance.getId());

    assertThat(taskService.createTaskQuery().processDefinitionKey("unexisting").count()).isEqualTo(0);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml" })
  public void testProcessDefinitionKeyOr() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    List<Task> tasks = taskService.createTaskQuery().or().taskId("invalid").processDefinitionKey("oneTaskProcess").list();
    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).getProcessInstanceId()).isEqualTo(processInstance.getId());

    assertThat(taskService.createTaskQuery().or().taskId("invalid").processDefinitionKey("unexisting").count()).isEqualTo(0);

    assertThat(taskService.createTaskQuery().or().taskId(taskIds.get(0)).processDefinitionKey("unexisting").endOr().count()).isEqualTo(1);
  }

  @Deployment(resources={"org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml"})
  public void testProcessDefinitionKeyIn() throws Exception {
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    List<String> includeIds = new ArrayList<String>();

    assertThat(taskService.createTaskQuery().processDefinitionKeyIn(includeIds).count()).isEqualTo(13);
    includeIds.add("unexisting");
    assertThat(taskService.createTaskQuery().processDefinitionKeyIn(includeIds).count()).isEqualTo(0);
    includeIds.add("oneTaskProcess");
    assertThat(taskService.createTaskQuery().processDefinitionKeyIn(includeIds).count()).isEqualTo(1);
  }

  @Deployment(resources={"org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml"})
  public void testProcessDefinitionKeyInOr() throws Exception {
    runtimeService.startProcessInstanceByKey("oneTaskProcess");

    List<String> includeIds = new ArrayList<String>();
    assertThat(taskService.createTaskQuery()
        .or().taskId("invalid")
        .processDefinitionKeyIn(includeIds)
        .count()).isEqualTo(0);

    includeIds.add("unexisting");
    assertThat(taskService.createTaskQuery()
        .or().taskId("invalid")
        .processDefinitionKeyIn(includeIds)
        .count()).isEqualTo(0);

    includeIds.add("oneTaskProcess");
    assertThat(taskService.createTaskQuery()
        .or().taskId("invalid")
        .processDefinitionKeyIn(includeIds)
        .count()).isEqualTo(1);
  }

  @Deployment(resources={"org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml"})
  public void testProcessDefinitionName() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    List<Task> tasks = taskService.createTaskQuery().processDefinitionName("The One Task Process").list();
    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).getProcessInstanceId()).isEqualTo(processInstance.getId());

    assertThat(taskService.createTaskQuery().processDefinitionName("unexisting").count()).isEqualTo(0);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml" })
  public void testProcessDefinitionNameOr() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    List<Task> tasks = taskService.createTaskQuery().or().taskId("invalid").processDefinitionName("The One Task Process").list();
    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).getProcessInstanceId()).isEqualTo(processInstance.getId());

    assertThat(taskService.createTaskQuery().or().taskId("invalid").processDefinitionName("unexisting").count()).isEqualTo(0);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml" })
  public void testProcessCategoryIn() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    final Task task = taskService.createTaskQuery().processCategoryIn(singletonList("Examples")).singleResult();
    assertThat(task).isNotNull();
    assertThat(task.getTaskDefinitionKey()).isEqualTo("theTask");
    assertThat(task.getProcessInstanceId()).isEqualTo(processInstance.getId());

    assertThat(taskService.createTaskQuery().processCategoryIn(singletonList("unexisting")).count()).isEqualTo(0);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml" })
  public void testProcessCategoryInOr() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    Task task = taskService.createTaskQuery()
        .or()
        .taskId("invalid")
        .processCategoryIn(singletonList("Examples")).singleResult();
    assertThat(task).isNotNull();
    assertThat(task.getTaskDefinitionKey()).isEqualTo("theTask");
    assertThat(task.getProcessInstanceId()).isEqualTo(processInstance.getId());

    task = taskService.createTaskQuery()
        .or()
          .taskId("invalid")
          .processCategoryIn(singletonList("Examples"))
        .endOr()
        .or()
          .taskId(task.getId())
          .processCategoryIn(singletonList("Examples2"))
        .endOr()
        .singleResult();
    assertThat(task).isNotNull();
    assertThat(task.getTaskDefinitionKey()).isEqualTo("theTask");
    assertThat(task.getProcessInstanceId()).isEqualTo(processInstance.getId());

    assertThat(taskService.createTaskQuery().or().taskId("invalid").processCategoryIn(singletonList("unexisting")).count()).isEqualTo(0);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml" })
  public void testProcessCategoryNotIn() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    final Task task = taskService.createTaskQuery().processCategoryNotIn(singletonList("unexisting")).singleResult();
    assertThat(task).isNotNull();
    assertThat(task.getTaskDefinitionKey()).isEqualTo("theTask");
    assertThat(task.getProcessInstanceId()).isEqualTo(processInstance.getId());

    assertThat(taskService.createTaskQuery().processCategoryNotIn(singletonList("Examples")).count()).isEqualTo(0);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml" })
  public void testProcessCategoryNotInOr() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    final Task task = taskService.createTaskQuery().or().taskId("invalid").processCategoryNotIn(singletonList("unexisting")).singleResult();
    assertThat(task).isNotNull();
    assertThat(task.getTaskDefinitionKey()).isEqualTo("theTask");
    assertThat(task.getProcessInstanceId()).isEqualTo(processInstance.getId());

    assertThat(taskService.createTaskQuery().or().taskId("invalid").processCategoryNotIn(singletonList("Examples")).count()).isEqualTo(0);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml" })
  public void testProcessInstanceIdIn() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    final Task task = taskService.createTaskQuery().processInstanceIdIn(singletonList(processInstance.getId())).singleResult();
    assertThat(task).isNotNull();
    assertThat(task.getTaskDefinitionKey()).isEqualTo("theTask");
    assertThat(task.getProcessInstanceId()).isEqualTo(processInstance.getId());

    assertThat(taskService.createTaskQuery().processInstanceIdIn(singletonList("unexisting")).count()).isEqualTo(0);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml" })
  public void testProcessInstanceIdInOr() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    final Task task = taskService.createTaskQuery().or().taskId("invalid").processInstanceIdIn(singletonList(
                    processInstance.getId())).singleResult();
    assertThat(task).isNotNull();
    assertThat(task.getTaskDefinitionKey()).isEqualTo("theTask");
    assertThat(task.getProcessInstanceId()).isEqualTo(processInstance.getId());

    assertThat(taskService.createTaskQuery().or().taskId("invalid").processInstanceIdIn(singletonList("unexisting")).count()).isEqualTo(0);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml" })
  public void testProcessInstanceIdInMultiple() throws Exception {
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    assertThat(taskService.createTaskQuery().processInstanceIdIn(asList(processInstance1.getId(), processInstance2.getId())).count()).isEqualTo(2);
    assertThat(taskService.createTaskQuery().processInstanceIdIn(asList(processInstance1.getId(), processInstance2.getId(), "unexisting")).count()).isEqualTo(2);

    assertThat(taskService.createTaskQuery().processInstanceIdIn(asList("unexisting1", "unexisting2")).count()).isEqualTo(0);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml" })
  public void testProcessInstanceIdInOrMultiple() throws Exception {
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    assertThat(taskService.createTaskQuery().or().taskId("invalid").processInstanceIdIn(asList(processInstance1.getId(), processInstance2.getId())).count()).isEqualTo(2);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").processInstanceIdIn(asList(processInstance1.getId(), processInstance2.getId(), "unexisting")).count()).isEqualTo(2);

    assertThat(taskService.createTaskQuery().or().taskId("invalid").processInstanceIdIn(asList("unexisting1", "unexisting2")).count()).isEqualTo(0);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml" })
  public void testProcessInstanceBusinessKey() throws Exception {
    runtimeService.startProcessInstanceByKey("oneTaskProcess", "BUSINESS-KEY-1");

    assertThat(taskService.createTaskQuery().processDefinitionName("The One Task Process").processInstanceBusinessKey("BUSINESS-KEY-1").list()).hasSize(1);
    assertThat(taskService.createTaskQuery().processInstanceBusinessKey("BUSINESS-KEY-1").list()).hasSize(1);
    assertThat(taskService.createTaskQuery().processInstanceBusinessKey("NON-EXISTING").count()).isEqualTo(0);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml" })
  public void testProcessInstanceBusinessKeyOr() throws Exception {
    runtimeService.startProcessInstanceByKey("oneTaskProcess", "BUSINESS-KEY-1");

    assertThat(taskService.createTaskQuery().processDefinitionName("The One Task Process").or().taskId("invalid").processInstanceBusinessKey("BUSINESS-KEY-1").list()).hasSize(1);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").processInstanceBusinessKey("BUSINESS-KEY-1").list()).hasSize(1);
    assertThat(taskService.createTaskQuery().or().taskId("invalid").processInstanceBusinessKey("NON-EXISTING").count()).isEqualTo(0);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml" })
  public void testTaskDueDate() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

    // Set due-date on task
    Date dueDate = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").parse("01/02/2003 01:12:13");
    task.setDueDate(dueDate);
    taskService.saveTask(task);

    assertThat(taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskDueDate(dueDate).count()).isEqualTo(1);

    Calendar otherDate = Calendar.getInstance();
    otherDate.add(Calendar.YEAR, 1);
    assertThat(taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskDueDate(otherDate.getTime()).count()).isEqualTo(0);

    Calendar priorDate = Calendar.getInstance();
    priorDate.setTime(dueDate);
    priorDate.roll(Calendar.YEAR, -1);

    assertThat(taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskDueAfter(priorDate.getTime()).count()).isEqualTo(1);

    assertThat(taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskDueBefore(otherDate.getTime()).count()).isEqualTo(1);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml" })
  public void testTaskDueDateOr() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

    // Set due-date on task
    Date dueDate = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").parse("01/02/2003 01:12:13");
    task.setDueDate(dueDate);
    taskService.saveTask(task);

    assertThat(taskService.createTaskQuery().processInstanceId(processInstance.getId()).or().taskId("invalid").taskDueDate(dueDate).count()).isEqualTo(1);

    Calendar otherDate = Calendar.getInstance();
    otherDate.add(Calendar.YEAR, 1);
    assertThat(taskService.createTaskQuery().processInstanceId(processInstance.getId()).or().taskId("invalid").taskDueDate(otherDate.getTime()).count()).isEqualTo(0);

    Calendar priorDate = Calendar.getInstance();
    priorDate.setTime(dueDate);
    priorDate.roll(Calendar.YEAR, -1);

    assertThat(taskService.createTaskQuery().processInstanceId(processInstance.getId()).or().taskId("invalid").taskDueAfter(priorDate.getTime()).count()).isEqualTo(1);

    assertThat(taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskDueBefore(otherDate.getTime()).count()).isEqualTo(1);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml" })
  public void testTaskDueBefore() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

    // Set due-date on task
    Calendar dueDateCal = Calendar.getInstance();
    task.setDueDate(dueDateCal.getTime());
    taskService.saveTask(task);

    Calendar oneHourAgo = Calendar.getInstance();
    oneHourAgo.setTime(dueDateCal.getTime());
    oneHourAgo.add(Calendar.HOUR, -1);

    Calendar oneHourLater = Calendar.getInstance();
    oneHourLater.setTime(dueDateCal.getTime());
    oneHourLater.add(Calendar.HOUR, 1);

    assertThat(taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskDueBefore(oneHourLater.getTime()).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskDueBefore(oneHourAgo.getTime()).count()).isEqualTo(0);

    // Update due-date to null, shouldn't show up anymore in query that
    // matched before
    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    task.setDueDate(null);
    taskService.saveTask(task);

    assertThat(taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskDueBefore(oneHourLater.getTime()).count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskDueBefore(oneHourAgo.getTime()).count()).isEqualTo(0);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml" })
  public void testTaskDueBeforeOr() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

    // Set due-date on task
    Calendar dueDateCal = Calendar.getInstance();
    task.setDueDate(dueDateCal.getTime());
    taskService.saveTask(task);

    Calendar oneHourAgo = Calendar.getInstance();
    oneHourAgo.setTime(dueDateCal.getTime());
    oneHourAgo.add(Calendar.HOUR, -1);

    Calendar oneHourLater = Calendar.getInstance();
    oneHourLater.setTime(dueDateCal.getTime());
    oneHourLater.add(Calendar.HOUR, 1);

    assertThat(taskService.createTaskQuery().processInstanceId(processInstance.getId()).or().taskId("invalid").taskDueBefore(oneHourLater.getTime()).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().processInstanceId(processInstance.getId()).or().taskId("invalid").taskDueBefore(oneHourAgo.getTime()).count()).isEqualTo(0);

    // Update due-date to null, shouldn't show up anymore in query that
    // matched before
    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    task.setDueDate(null);
    taskService.saveTask(task);

    assertThat(taskService.createTaskQuery().processInstanceId(processInstance.getId()).or().taskId("invalid").taskDueBefore(oneHourLater.getTime()).count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().processInstanceId(processInstance.getId()).or().taskId("invalid").taskDueBefore(oneHourAgo.getTime()).count()).isEqualTo(0);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml" })
  public void testTaskDueAfter() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

    // Set due-date on task
    Calendar dueDateCal = Calendar.getInstance();
    task.setDueDate(dueDateCal.getTime());
    taskService.saveTask(task);

    Calendar oneHourAgo = Calendar.getInstance();
    oneHourAgo.setTime(dueDateCal.getTime());
    oneHourAgo.add(Calendar.HOUR, -1);

    Calendar oneHourLater = Calendar.getInstance();
    oneHourLater.setTime(dueDateCal.getTime());
    oneHourLater.add(Calendar.HOUR, 1);

    assertThat(taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskDueAfter(oneHourAgo.getTime()).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskDueAfter(oneHourLater.getTime()).count()).isEqualTo(0);

    // Update due-date to null, shouldn't show up anymore in query that
    // matched before
    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    task.setDueDate(null);
    taskService.saveTask(task);

    assertThat(taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskDueAfter(oneHourLater.getTime()).count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().processInstanceId(processInstance.getId()).taskDueAfter(oneHourAgo.getTime()).count()).isEqualTo(0);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml" })
  public void testTaskDueAfterOn() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

    // Set due-date on task
    Calendar dueDateCal = Calendar.getInstance();
    task.setDueDate(dueDateCal.getTime());
    taskService.saveTask(task);

    Calendar oneHourAgo = Calendar.getInstance();
    oneHourAgo.setTime(dueDateCal.getTime());
    oneHourAgo.add(Calendar.HOUR, -1);

    Calendar oneHourLater = Calendar.getInstance();
    oneHourLater.setTime(dueDateCal.getTime());
    oneHourLater.add(Calendar.HOUR, 1);

    assertThat(taskService.createTaskQuery().processInstanceId(processInstance.getId()).or().taskId("invalid").taskDueAfter(oneHourAgo.getTime()).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().processInstanceId(processInstance.getId()).or().taskId("invalid").taskDueAfter(oneHourLater.getTime()).count()).isEqualTo(0);

    // Update due-date to null, shouldn't show up anymore in query that
    // matched before
    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    task.setDueDate(null);
    taskService.saveTask(task);

    assertThat(taskService.createTaskQuery().processInstanceId(processInstance.getId()).or().taskId("invalid").taskDueAfter(oneHourLater.getTime()).count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().processInstanceId(processInstance.getId()).or().taskId("invalid").taskDueAfter(oneHourAgo.getTime()).count()).isEqualTo(0);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml" })
  public void testTaskWithoutDueDate() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).withoutTaskDueDate().singleResult();

    // Set due-date on task
    Date dueDate = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").parse("01/02/2003 01:12:13");
    task.setDueDate(dueDate);
    taskService.saveTask(task);

    assertThat(taskService.createTaskQuery().processInstanceId(processInstance.getId()).withoutTaskDueDate().count()).isEqualTo(0);

    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

    // Clear due-date on task
    task.setDueDate(null);
    taskService.saveTask(task);

    assertThat(taskService.createTaskQuery().processInstanceId(processInstance.getId()).withoutTaskDueDate().count()).isEqualTo(1);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml" })
  public void testTaskWithoutDueDateOr() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).or().taskId("invalid").withoutTaskDueDate().singleResult();

    // Set due-date on task
    Date dueDate = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").parse("01/02/2003 01:12:13");
    task.setDueDate(dueDate);
    taskService.saveTask(task);

    assertThat(taskService.createTaskQuery().processInstanceId(processInstance.getId()).or().taskId("invalid").withoutTaskDueDate().count()).isEqualTo(0);

    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

    // Clear due-date on task
    task.setDueDate(null);
    taskService.saveTask(task);

    assertThat(taskService.createTaskQuery().processInstanceId(processInstance.getId()).or().taskId("invalid").withoutTaskDueDate().count()).isEqualTo(1);
  }

  public void testQueryPaging() {
    TaskQuery query = taskService.createTaskQuery().taskCandidateUser(KERMIT,KERMITSGROUPS);

    assertThat(query.listPage(0, Integer.MAX_VALUE)).hasSize(11);

    // Verifying the un-paged results
    assertThat(query.count()).isEqualTo(11);
    assertThat(query.list()).hasSize(11);

    // Verifying paged results
    assertThat(query.listPage(0, 2)).hasSize(2);
    assertThat(query.listPage(2, 2)).hasSize(2);
    assertThat(query.listPage(4, 3)).hasSize(3);
    assertThat(query.listPage(10, 3)).hasSize(1);
    assertThat(query.listPage(10, 1)).hasSize(1);

    // Verifying odd usages
    assertThat(query.listPage(-1, -1)).hasSize(0);
    assertThat(query.listPage(11, 2)).hasSize(0); // 10 is the last index
                                                   // with a result
    assertThat(query.listPage(0, 15)).hasSize(11); // there are only 11
                                                    // tasks
  }

  public void testQuerySorting() {
    assertThat(taskService.createTaskQuery().orderByTaskId().asc().list()).hasSize(12);
    assertThat(taskService.createTaskQuery().orderByTaskName().asc().list()).hasSize(12);
    assertThat(taskService.createTaskQuery().orderByTaskPriority().asc().list()).hasSize(12);
    assertThat(taskService.createTaskQuery().orderByTaskAssignee().asc().list()).hasSize(12);
    assertThat(taskService.createTaskQuery().orderByTaskDescription().asc().list()).hasSize(12);
    assertThat(taskService.createTaskQuery().orderByProcessInstanceId().asc().list()).hasSize(12);
    assertThat(taskService.createTaskQuery().orderByExecutionId().asc().list()).hasSize(12);
    assertThat(taskService.createTaskQuery().orderByTaskCreateTime().asc().list()).hasSize(12);
    assertThat(taskService.createTaskQuery().orderByTaskDueDate().asc().list()).hasSize(12);

    assertThat(taskService.createTaskQuery().orderByTaskId().desc().list()).hasSize(12);
    assertThat(taskService.createTaskQuery().orderByTaskName().desc().list()).hasSize(12);
    assertThat(taskService.createTaskQuery().orderByTaskPriority().desc().list()).hasSize(12);
    assertThat(taskService.createTaskQuery().orderByTaskAssignee().desc().list()).hasSize(12);
    assertThat(taskService.createTaskQuery().orderByTaskDescription().desc().list()).hasSize(12);
    assertThat(taskService.createTaskQuery().orderByProcessInstanceId().desc().list()).hasSize(12);
    assertThat(taskService.createTaskQuery().orderByExecutionId().desc().list()).hasSize(12);
    assertThat(taskService.createTaskQuery().orderByTaskCreateTime().desc().list()).hasSize(12);
    assertThat(taskService.createTaskQuery().orderByTaskDueDate().desc().list()).hasSize(12);

    assertThat(taskService.createTaskQuery().orderByTaskId().taskName("testTask").asc().list()).hasSize(6);
    assertThat(taskService.createTaskQuery().orderByTaskId().taskName("testTask").desc().list()).hasSize(6);
  }

  public void testNativeQueryPaging() {
    assertThat(managementService.getTableName(Task.class)).isEqualTo("ACT_RU_TASK");
    assertThat(managementService.getTableName(TaskEntity.class)).isEqualTo("ACT_RU_TASK");
    assertThat(taskService.createNativeTaskQuery().sql("SELECT * FROM " + managementService.getTableName(Task.class)).listPage(0, 5)).hasSize(5);
    assertThat(taskService.createNativeTaskQuery().sql("SELECT * FROM " + managementService.getTableName(Task.class)).listPage(10, 12)).hasSize(2);
  }

  public void testNativeQuery() {
    assertThat(managementService.getTableName(Task.class)).isEqualTo("ACT_RU_TASK");
    assertThat(managementService.getTableName(TaskEntity.class)).isEqualTo("ACT_RU_TASK");
    assertThat(taskService.createNativeTaskQuery().sql("SELECT * FROM " + managementService.getTableName(Task.class)).list()).hasSize(12);
    assertThat(taskService.createNativeTaskQuery().sql("SELECT count(*) FROM " + managementService.getTableName(Task.class)).count()).isEqualTo(12);

    assertThat(taskService.createNativeTaskQuery().sql("SELECT count(*) FROM ACT_RU_TASK T1, ACT_RU_TASK T2").count()).isEqualTo(144);

    // join task and variable instances
    assertThat(
        taskService.createNativeTaskQuery()
            .sql("SELECT count(*) FROM " + managementService.getTableName(Task.class) + " T1, " + managementService.getTableName(VariableInstanceEntity.class) + " V1 WHERE V1.TASK_ID_ = T1.ID_")
            .count()).isEqualTo(1);
    List<Task> tasks = taskService.createNativeTaskQuery()
        .sql("SELECT * FROM " + managementService.getTableName(Task.class) + " T1, " + managementService.getTableName(VariableInstanceEntity.class) + " V1 WHERE V1.TASK_ID_ = T1.ID_").list();
    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).getName()).isEqualTo("gonzoTask");

    // select with distinct
    assertThat(taskService.createNativeTaskQuery().sql("SELECT DISTINCT T1.* FROM ACT_RU_TASK T1").list()).hasSize(12);

    assertThat(taskService.createNativeTaskQuery().sql("SELECT count(*) FROM " + managementService.getTableName(Task.class) + " T WHERE T.NAME_ = 'gonzoTask'").count()).isEqualTo(1);
    assertThat(taskService.createNativeTaskQuery().sql("SELECT * FROM " + managementService.getTableName(Task.class) + " T WHERE T.NAME_ = 'gonzoTask'").list()).hasSize(1);

    // use parameters
    assertThat(taskService.createNativeTaskQuery().sql("SELECT count(*) FROM " + managementService.getTableName(Task.class) + " T WHERE T.NAME_ = #{taskName}").parameter("taskName", "gonzoTask").count()).isEqualTo(1);
  }

  /**
   * Test confirming fix for ACT-1731
   */
  @Deployment(resources = { "org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml" })
  public void testIncludeBinaryVariables() throws Exception {
    // Start process with a binary variable
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", singletonMap("binaryVariable", "It is I, le binary".getBytes()));
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(task).isNotNull();
    taskService.setVariableLocal(task.getId(), "binaryTaskVariable", (Object) "It is I, le binary".getBytes());

    // Query task, including processVariables
    task = taskService.createTaskQuery().taskId(task.getId()).includeProcessVariables().singleResult();
    assertThat(task).isNotNull();
    assertThat(task.getProcessVariables()).isNotNull();
    byte[] bytes = (byte[]) task.getProcessVariables().get("binaryVariable");
    assertThat(new String(bytes)).isEqualTo("It is I, le binary");

    // Query task, including taskVariables
    task = taskService.createTaskQuery().taskId(task.getId()).includeTaskLocalVariables().singleResult();
    assertThat(task).isNotNull();
    assertThat(task.getTaskLocalVariables()).isNotNull();
    bytes = (byte[]) task.getTaskLocalVariables().get("binaryTaskVariable");
    assertThat(new String(bytes)).isEqualTo("It is I, le binary");
  }

  /**
   * Test confirming fix for ACT-1731
   */
  @Deployment(resources = { "org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml" })
  public void testIncludeBinaryVariablesOr() throws Exception {
    // Start process with a binary variable
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", singletonMap("binaryVariable", (Object) "It is I, le binary".getBytes()));
    Task task = taskService.createTaskQuery().or().taskName("invalid").processInstanceId(processInstance.getId()).singleResult();
    assertThat(task).isNotNull();
    taskService.setVariableLocal(task.getId(), "binaryTaskVariable", (Object) "It is I, le binary".getBytes());

    // Query task, including processVariables
    task = taskService.createTaskQuery().or().taskName("invalid").taskId(task.getId()).includeProcessVariables().singleResult();
    assertThat(task).isNotNull();
    assertThat(task.getProcessVariables()).isNotNull();
    byte[] bytes = (byte[]) task.getProcessVariables().get("binaryVariable");
    assertThat(new String(bytes)).isEqualTo("It is I, le binary");

    // Query task, including taskVariables
    task = taskService.createTaskQuery().or().taskName("invalid").taskId(task.getId()).includeTaskLocalVariables().singleResult();
    assertThat(task).isNotNull();
    assertThat(task.getTaskLocalVariables()).isNotNull();
    bytes = (byte[]) task.getTaskLocalVariables().get("binaryTaskVariable");
    assertThat(new String(bytes)).isEqualTo("It is I, le binary");
  }

  @Deployment(resources = { "org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml" })
  public void testQueryByDeploymentId() throws Exception {
    org.activiti.engine.repository.Deployment deployment = repositoryService.createDeploymentQuery().singleResult();
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    assertThat(taskService.createTaskQuery().deploymentId(deployment.getId()).singleResult()).isNotNull();
    assertThat(taskService.createTaskQuery().deploymentId(deployment.getId()).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().deploymentId("invalid").singleResult()).isNull();
    assertThat(taskService.createTaskQuery().deploymentId("invalid").count()).isEqualTo(0);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml" })
  public void testQueryByDeploymentIdOr() throws Exception {
    org.activiti.engine.repository.Deployment deployment = repositoryService.createDeploymentQuery().singleResult();
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    assertThat(taskService.createTaskQuery().or().taskId("invalid").deploymentId(deployment.getId()).singleResult()).isNotNull();
    assertThat(taskService.createTaskQuery().or().taskId("invalid").deploymentId(deployment.getId()).count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().deploymentId("invalid").singleResult()).isNull();
    assertThat(taskService.createTaskQuery().or().taskId("invalid").deploymentId("invalid").count()).isEqualTo(0);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml" })
  public void testQueryByDeploymentIdIn() throws Exception {
    org.activiti.engine.repository.Deployment deployment = repositoryService.createDeploymentQuery().singleResult();
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    List<String> deploymentIds = new ArrayList<String>();
    deploymentIds.add(deployment.getId());
    assertThat(taskService.createTaskQuery().deploymentIdIn(deploymentIds).singleResult()).isNotNull();
    assertThat(taskService.createTaskQuery().deploymentIdIn(deploymentIds).count()).isEqualTo(1);

    deploymentIds.add("invalid");
    assertThat(taskService.createTaskQuery().deploymentIdIn(deploymentIds).singleResult()).isNotNull();
    assertThat(taskService.createTaskQuery().deploymentIdIn(deploymentIds).count()).isEqualTo(1);

    deploymentIds = new ArrayList<String>();
    deploymentIds.add("invalid");
    assertThat(taskService.createTaskQuery().deploymentIdIn(deploymentIds).singleResult()).isNull();
    assertThat(taskService.createTaskQuery().deploymentIdIn(deploymentIds).count()).isEqualTo(0);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml" })
  public void testQueryByDeploymentIdInOr() throws Exception {
    org.activiti.engine.repository.Deployment deployment = repositoryService.createDeploymentQuery().singleResult();
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    List<String> deploymentIds = new ArrayList<String>();
    deploymentIds.add(deployment.getId());
    assertThat(taskService.createTaskQuery().or().taskId("invalid").deploymentIdIn(deploymentIds).singleResult()).isNotNull();

    assertThat(taskService.createTaskQuery().or().taskId("invalid").deploymentIdIn(deploymentIds).count()).isEqualTo(1);

    deploymentIds.add("invalid");
    assertThat(taskService.createTaskQuery().or().taskId("invalid").deploymentIdIn(deploymentIds).singleResult()).isNotNull();

    assertThat(taskService.createTaskQuery().or().taskId("invalid").deploymentIdIn(deploymentIds).count()).isEqualTo(1);

    deploymentIds = new ArrayList<String>();
    deploymentIds.add("invalid");
    assertThat(taskService.createTaskQuery().deploymentIdIn(deploymentIds).singleResult()).isNull();
    assertThat(taskService.createTaskQuery().or().taskId("invalid").deploymentIdIn(deploymentIds).count()).isEqualTo(0);
  }

  public void testQueryByTaskNameLikeIgnoreCase() {

    // Runtime
    assertThat(taskService.createTaskQuery().taskNameLikeIgnoreCase("%task%").count()).isEqualTo(12);
    assertThat(taskService.createTaskQuery().taskNameLikeIgnoreCase("%Task%").count()).isEqualTo(12);
    assertThat(taskService.createTaskQuery().taskNameLikeIgnoreCase("%TASK%").count()).isEqualTo(12);
    assertThat(taskService.createTaskQuery().taskNameLikeIgnoreCase("%TasK%").count()).isEqualTo(12);
    assertThat(taskService.createTaskQuery().taskNameLikeIgnoreCase("gonzo%").count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().taskNameLikeIgnoreCase("%Gonzo%").count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().taskNameLikeIgnoreCase("Task%").count()).isEqualTo(0);

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
      // History
      assertThat(historyService.createHistoricTaskInstanceQuery().taskNameLikeIgnoreCase("%task%").count()).isEqualTo(12);
      assertThat(historyService.createHistoricTaskInstanceQuery().taskNameLikeIgnoreCase("%Task%").count()).isEqualTo(12);
      assertThat(historyService.createHistoricTaskInstanceQuery().taskNameLikeIgnoreCase("%TASK%").count()).isEqualTo(12);
      assertThat(historyService.createHistoricTaskInstanceQuery().taskNameLikeIgnoreCase("%TasK%").count()).isEqualTo(12);
      assertThat(historyService.createHistoricTaskInstanceQuery().taskNameLikeIgnoreCase("gonzo%").count()).isEqualTo(1);
      assertThat(historyService.createHistoricTaskInstanceQuery().taskNameLikeIgnoreCase("%Gonzo%").count()).isEqualTo(1);
      assertThat(historyService.createHistoricTaskInstanceQuery().taskNameLikeIgnoreCase("Task%").count()).isEqualTo(0);
    }
  }

  public void testQueryByTaskNameOrDescriptionLikeIgnoreCase() {

    // Runtime
    assertThat(taskService.createTaskQuery().or().taskNameLikeIgnoreCase("%task%").taskDescriptionLikeIgnoreCase("%task%").endOr().count()).isEqualTo(12);

    assertThat(taskService.createTaskQuery().or().taskNameLikeIgnoreCase("ACCOUN%").taskDescriptionLikeIgnoreCase("%ESCR%").endOr().count()).isEqualTo(9);

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
      // History
      assertThat(historyService.createHistoricTaskInstanceQuery().or().taskNameLikeIgnoreCase("%task%").taskDescriptionLikeIgnoreCase("%task%").endOr().count()).isEqualTo(12);

      assertThat(historyService.createHistoricTaskInstanceQuery().or().taskNameLikeIgnoreCase("ACCOUN%").taskDescriptionLikeIgnoreCase("%ESCR%").endOr().count()).isEqualTo(9);
    }

  }

  public void testQueryByTaskDescriptionLikeIgnoreCase() {

    // Runtime
    assertThat(taskService.createTaskQuery().taskDescriptionLikeIgnoreCase("%task%").count()).isEqualTo(6);
    assertThat(taskService.createTaskQuery().taskDescriptionLikeIgnoreCase("%Task%").count()).isEqualTo(6);
    assertThat(taskService.createTaskQuery().taskDescriptionLikeIgnoreCase("%TASK%").count()).isEqualTo(6);
    assertThat(taskService.createTaskQuery().taskDescriptionLikeIgnoreCase("%TaSk%").count()).isEqualTo(6);
    assertThat(taskService.createTaskQuery().taskDescriptionLikeIgnoreCase("task%").count()).isEqualTo(0);
    assertThat(taskService.createTaskQuery().taskDescriptionLikeIgnoreCase("gonzo%").count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().taskDescriptionLikeIgnoreCase("Gonzo%").count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().taskDescriptionLikeIgnoreCase("%manage%").count()).isEqualTo(0);

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
      // History
      assertThat(historyService.createHistoricTaskInstanceQuery().taskDescriptionLikeIgnoreCase("%task%").count()).isEqualTo(6);
      assertThat(historyService.createHistoricTaskInstanceQuery().taskDescriptionLikeIgnoreCase("%Task%").count()).isEqualTo(6);
      assertThat(historyService.createHistoricTaskInstanceQuery().taskDescriptionLikeIgnoreCase("%TASK%").count()).isEqualTo(6);
      assertThat(historyService.createHistoricTaskInstanceQuery().taskDescriptionLikeIgnoreCase("%TaSk%").count()).isEqualTo(6);
      assertThat(historyService.createHistoricTaskInstanceQuery().taskDescriptionLikeIgnoreCase("task%").count()).isEqualTo(0);
      assertThat(historyService.createHistoricTaskInstanceQuery().taskDescriptionLikeIgnoreCase("gonzo%").count()).isEqualTo(1);
      assertThat(historyService.createHistoricTaskInstanceQuery().taskDescriptionLikeIgnoreCase("Gonzo%").count()).isEqualTo(1);
      assertThat(historyService.createHistoricTaskInstanceQuery().taskDescriptionLikeIgnoreCase("%manage%").count()).isEqualTo(0);
    }
  }

  public void testQueryByAssigneeLikeIgnoreCase() {

    // Runtime
    assertThat(taskService.createTaskQuery().taskAssigneeLikeIgnoreCase("%gonzo%").count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().taskAssigneeLikeIgnoreCase("%GONZO%").count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().taskAssigneeLikeIgnoreCase("%Gon%").count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().taskAssigneeLikeIgnoreCase("gon%").count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().taskAssigneeLikeIgnoreCase("%nzo%").count()).isEqualTo(1);
    assertThat(taskService.createTaskQuery().taskAssigneeLikeIgnoreCase("%doesnotexist%").count()).isEqualTo(0);

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
      // History
      assertThat(historyService.createHistoricTaskInstanceQuery().taskAssigneeLikeIgnoreCase("%gonzo%").count()).isEqualTo(1);
      assertThat(historyService.createHistoricTaskInstanceQuery().taskAssigneeLikeIgnoreCase("%GONZO%").count()).isEqualTo(1);
      assertThat(historyService.createHistoricTaskInstanceQuery().taskAssigneeLikeIgnoreCase("%Gon%").count()).isEqualTo(1);
      assertThat(historyService.createHistoricTaskInstanceQuery().taskAssigneeLikeIgnoreCase("gon%").count()).isEqualTo(1);
      assertThat(historyService.createHistoricTaskInstanceQuery().taskAssigneeLikeIgnoreCase("%nzo%").count()).isEqualTo(1);
      assertThat(historyService.createHistoricTaskInstanceQuery().taskAssigneeLikeIgnoreCase("%doesnotexist%").count()).isEqualTo(0);
    }
  }

  public void testQueryByOwnerLikeIgnoreCase() {

    // Runtime
    assertThat(taskService.createTaskQuery().taskOwnerLikeIgnoreCase("%gonzo%").count()).isEqualTo(6);
    assertThat(taskService.createTaskQuery().taskOwnerLikeIgnoreCase("%GONZO%").count()).isEqualTo(6);
    assertThat(taskService.createTaskQuery().taskOwnerLikeIgnoreCase("%Gon%").count()).isEqualTo(6);
    assertThat(taskService.createTaskQuery().taskOwnerLikeIgnoreCase("gon%").count()).isEqualTo(6);
    assertThat(taskService.createTaskQuery().taskOwnerLikeIgnoreCase("%nzo%").count()).isEqualTo(6);
    assertThat(taskService.createTaskQuery().taskOwnerLikeIgnoreCase("%doesnotexist%").count()).isEqualTo(0);

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
      // History
      assertThat(historyService.createHistoricTaskInstanceQuery().taskOwnerLikeIgnoreCase("%gonzo%").count()).isEqualTo(6);
      assertThat(historyService.createHistoricTaskInstanceQuery().taskOwnerLikeIgnoreCase("%GONZO%").count()).isEqualTo(6);
      assertThat(historyService.createHistoricTaskInstanceQuery().taskOwnerLikeIgnoreCase("%Gon%").count()).isEqualTo(6);
      assertThat(historyService.createHistoricTaskInstanceQuery().taskOwnerLikeIgnoreCase("gon%").count()).isEqualTo(6);
      assertThat(historyService.createHistoricTaskInstanceQuery().taskOwnerLikeIgnoreCase("%nzo%").count()).isEqualTo(6);
      assertThat(historyService.createHistoricTaskInstanceQuery().taskOwnerLikeIgnoreCase("%doesnotexist%").count()).isEqualTo(0);
    }
  }

  @Deployment(resources = { "org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml" })
  public void testQueryByBusinessKeyLikeIgnoreCase() {
    runtimeService.startProcessInstanceByKey("oneTaskProcess", "BUSINESS-KEY-1");
    runtimeService.startProcessInstanceByKey("oneTaskProcess", "Business-Key-2");
    runtimeService.startProcessInstanceByKey("oneTaskProcess", "KeY-3");

    // Runtime
    assertThat(taskService.createTaskQuery().processInstanceBusinessKeyLikeIgnoreCase("%key%").count()).isEqualTo(3);
    assertThat(taskService.createTaskQuery().processInstanceBusinessKeyLikeIgnoreCase("%KEY%").count()).isEqualTo(3);
    assertThat(taskService.createTaskQuery().processInstanceBusinessKeyLikeIgnoreCase("%EY%").count()).isEqualTo(3);
    assertThat(taskService.createTaskQuery().processInstanceBusinessKeyLikeIgnoreCase("%business%").count()).isEqualTo(2);
    assertThat(taskService.createTaskQuery().processInstanceBusinessKeyLikeIgnoreCase("business%").count()).isEqualTo(2);
    assertThat(taskService.createTaskQuery().processInstanceBusinessKeyLikeIgnoreCase("%doesnotexist%").count()).isEqualTo(0);

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
      // History
      assertThat(historyService.createHistoricTaskInstanceQuery().processInstanceBusinessKeyLikeIgnoreCase("%key%").count()).isEqualTo(3);
      assertThat(historyService.createHistoricTaskInstanceQuery().processInstanceBusinessKeyLikeIgnoreCase("%KEY%").count()).isEqualTo(3);
      assertThat(historyService.createHistoricTaskInstanceQuery().processInstanceBusinessKeyLikeIgnoreCase("%EY%").count()).isEqualTo(3);
      assertThat(historyService.createHistoricTaskInstanceQuery().processInstanceBusinessKeyLikeIgnoreCase("%business%").count()).isEqualTo(2);
      assertThat(historyService.createHistoricTaskInstanceQuery().processInstanceBusinessKeyLikeIgnoreCase("business%").count()).isEqualTo(2);
      assertThat(historyService.createHistoricTaskInstanceQuery().processInstanceBusinessKeyLikeIgnoreCase("%doesnotexist%").count()).isEqualTo(0);
    }
  }

  @Deployment(resources = { "org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml" })
  public void testQueryByProcessDefinitionKeyLikeIgnoreCase() {

    // Runtime
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.startProcessInstanceByKey("oneTaskProcess");

    assertThat(taskService.createTaskQuery().processDefinitionKeyLikeIgnoreCase("%one%").count()).isEqualTo(4);
    assertThat(taskService.createTaskQuery().processDefinitionKeyLikeIgnoreCase("%ONE%").count()).isEqualTo(4);
    assertThat(taskService.createTaskQuery().processDefinitionKeyLikeIgnoreCase("ON%").count()).isEqualTo(4);
    assertThat(taskService.createTaskQuery().processDefinitionKeyLikeIgnoreCase("%fake%").count()).isEqualTo(0);

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
      // History
      assertThat(historyService.createHistoricTaskInstanceQuery().processDefinitionKeyLikeIgnoreCase("%one%").count()).isEqualTo(4);
      assertThat(historyService.createHistoricTaskInstanceQuery().processDefinitionKeyLikeIgnoreCase("%ONE%").count()).isEqualTo(4);
      assertThat(historyService.createHistoricTaskInstanceQuery().processDefinitionKeyLikeIgnoreCase("ON%").count()).isEqualTo(4);
      assertThat(historyService.createHistoricTaskInstanceQuery().processDefinitionKeyLikeIgnoreCase("%fake%").count()).isEqualTo(0);
    }
  }

  public void testCombinationOfOrAndLikeIgnoreCase() {

    // Runtime
    assertThat(taskService.createTaskQuery().or().taskNameLikeIgnoreCase("%task%").taskDescriptionLikeIgnoreCase("%desc%").taskAssigneeLikeIgnoreCase("Gonz%").taskOwnerLike("G%").endOr()
        .count())
        .isEqualTo(12);

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
      // History
      assertThat(historyService.createHistoricTaskInstanceQuery().or().taskNameLikeIgnoreCase("%task%").taskDescriptionLikeIgnoreCase("%desc%").taskAssigneeLikeIgnoreCase("Gonz%")
          .taskOwnerLike("G%").endOr().count())
          .isEqualTo(12);
    }
  }

  // Test for https://jira.codehaus.org/browse/ACT-2103
  public void testTaskLocalAndProcessInstanceVariableEqualsInOr() {

  	deployOneTaskTestProcess();
  	for (int i=0; i<10; i++) {
  		runtimeService.startProcessInstanceByKey("oneTaskProcess");
  	}

  	List<Task> allTasks = taskService.createTaskQuery().processDefinitionKey("oneTaskProcess").list();
  	assertThat(allTasks).hasSize(10);

  	// Give two tasks a task local variable
  	taskService.setVariableLocal(allTasks.get(0).getId(), "localVar", "someValue");
  	taskService.setVariableLocal(allTasks.get(1).getId(), "localVar", "someValue");

  	// Give three tasks a proc inst var
  	runtimeService.setVariable(allTasks.get(2).getProcessInstanceId(), "var", "theValue");
  	runtimeService.setVariable(allTasks.get(3).getProcessInstanceId(), "var", "theValue");
  	runtimeService.setVariable(allTasks.get(4).getProcessInstanceId(), "var", "theValue");

  	assertThat(taskService.createTaskQuery().taskVariableValueEquals("localVar", "someValue").list()).hasSize(2);
  	assertThat(taskService.createTaskQuery().processVariableValueEquals("var", "theValue").list()).hasSize(3);

  	assertThat(taskService.createTaskQuery().or()
  			.taskVariableValueEquals("localVar", "someValue")
  			.processVariableValueEquals("var", "theValue")
  			.endOr().list()).hasSize(5);

  	assertThat(taskService.createTaskQuery()
  	    .or()
          .taskVariableValueEquals("localVar", "someValue")
          .processVariableValueEquals("var", "theValue")
        .endOr()
        .or()
          .processDefinitionKey("oneTaskProcess")
          .processDefinitionId("notexisting")
        .endOr()
        .list())
        .hasSize(5);
  }

  @Deployment(resources={"org/activiti/engine/test/api/task/TaskQueryTest.testProcessDefinition.bpmn20.xml"})
  public void testLocalizeTasks() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    List<Task> tasks = taskService.createTaskQuery().processDefinitionId(processInstance.getProcessDefinitionId()).list();
    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).getName()).isEqualTo("my task");
    assertThat(tasks.get(0).getDescription()).isEqualTo("My Task Description");

    tasks = taskService.createTaskQuery().processDefinitionId(processInstance.getProcessDefinitionId()).locale("es").list();
    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).getName()).isEqualTo("Mi Tarea");
    assertThat(tasks.get(0).getDescription()).isEqualTo("Mi Tarea Descripción");

    ObjectNode infoNode = dynamicBpmnService.getProcessDefinitionInfo(processInstance.getProcessDefinitionId());

    dynamicBpmnService.changeLocalizationName("en-GB", "theTask", "My 'en-GB' localized name", infoNode);
    dynamicBpmnService.changeLocalizationDescription("en-GB", "theTask", "My 'en-GB' localized description", infoNode);
    dynamicBpmnService.saveProcessDefinitionInfo(processInstance.getProcessDefinitionId(), infoNode);

    dynamicBpmnService.changeLocalizationName("en", "theTask", "My 'en' localized name", infoNode);
    dynamicBpmnService.changeLocalizationDescription("en", "theTask", "My 'en' localized description", infoNode);
    dynamicBpmnService.saveProcessDefinitionInfo(processInstance.getProcessDefinitionId(), infoNode);

    tasks = taskService.createTaskQuery().processDefinitionId(processInstance.getProcessDefinitionId()).list();
    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).getName()).isEqualTo("my task");
    assertThat(tasks.get(0).getDescription()).isEqualTo("My Task Description");

    tasks = taskService.createTaskQuery().processDefinitionId(processInstance.getProcessDefinitionId()).locale("es").list();
    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).getName()).isEqualTo("Mi Tarea");
    assertThat(tasks.get(0).getDescription()).isEqualTo("Mi Tarea Descripción");

    tasks = taskService.createTaskQuery().processDefinitionId(processInstance.getProcessDefinitionId()).locale("en-GB").list();
    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).getName()).isEqualTo("My 'en-GB' localized name");
    assertThat(tasks.get(0).getDescription()).isEqualTo("My 'en-GB' localized description");

    tasks = taskService.createTaskQuery().processDefinitionId(processInstance.getProcessDefinitionId()).listPage(0, 10);
    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).getName()).isEqualTo("my task");
    assertThat(tasks.get(0).getDescription()).isEqualTo("My Task Description");

    tasks = taskService.createTaskQuery().processDefinitionId(processInstance.getProcessDefinitionId()).locale("es").listPage(0, 10);
    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).getName()).isEqualTo("Mi Tarea");
    assertThat(tasks.get(0).getDescription()).isEqualTo("Mi Tarea Descripción");

    tasks = taskService.createTaskQuery().processDefinitionId(processInstance.getProcessDefinitionId()).locale("en-GB").listPage(0, 10);
    assertThat(tasks).hasSize(1);
    assertThat(tasks.get(0).getName()).isEqualTo("My 'en-GB' localized name");
    assertThat(tasks.get(0).getDescription()).isEqualTo("My 'en-GB' localized description");

    Task task = taskService.createTaskQuery().processDefinitionId(processInstance.getProcessDefinitionId()).singleResult();
    assertThat(task.getName()).isEqualTo("my task");
    assertThat(task.getDescription()).isEqualTo("My Task Description");

    task = taskService.createTaskQuery().processDefinitionId(processInstance.getProcessDefinitionId()).locale("es").singleResult();
    assertThat(task.getName()).isEqualTo("Mi Tarea");
    assertThat(task.getDescription()).isEqualTo("Mi Tarea Descripción");

    task = taskService.createTaskQuery().processDefinitionId(processInstance.getProcessDefinitionId()).locale("en-GB").singleResult();
    assertThat(task.getName()).isEqualTo("My 'en-GB' localized name");
    assertThat(task.getDescription()).isEqualTo("My 'en-GB' localized description");

    task = taskService.createTaskQuery().processDefinitionId(processInstance.getProcessDefinitionId()).singleResult();
    assertThat(task.getName()).isEqualTo("my task");
    assertThat(task.getDescription()).isEqualTo("My Task Description");

    task = taskService.createTaskQuery().processDefinitionId(processInstance.getProcessDefinitionId()).locale("en").singleResult();
    assertThat(task.getName()).isEqualTo("My 'en' localized name");
    assertThat(task.getDescription()).isEqualTo("My 'en' localized description");

    task = taskService.createTaskQuery().processDefinitionId(processInstance.getProcessDefinitionId()).locale("en-AU").withLocalizationFallback().singleResult();
    assertThat(task.getName()).isEqualTo("My 'en' localized name");
    assertThat(task.getDescription()).isEqualTo("My 'en' localized description");
  }

  /**
   * Generates some test tasks. - 6 tasks where kermit is a candidate - 1 tasks where gonzo is assignee - 2 tasks assigned to management group - 2 tasks assigned to accountancy group - 1 task assigned
   * to both the management and accountancy group
   */
  private List<String> generateTestTasks() throws Exception {
    List<String> ids = new ArrayList<String>();

    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");
    // 6 tasks for kermit
    processEngineConfiguration.getClock().setCurrentTime(sdf.parse("01/01/2001 01:01:01.000"));
    for (int i = 0; i < 6; i++) {
      Task task = taskService.newTask();
      task.setName("testTask");
      task.setDescription("testTask description");
      task.setOwner(GONZO);
      task.setPriority(3);
      taskService.saveTask(task);
      ids.add(task.getId());
      taskService.addCandidateUser(task.getId(), KERMIT);
    }

    processEngineConfiguration.getClock().setCurrentTime(sdf.parse("02/02/2002 02:02:02.000"));
    // 1 task for gonzo
    Task task = taskService.newTask();
    task.setName("gonzoTask");
    task.setDescription("gonzo description");
    task.setPriority(4);
    taskService.saveTask(task);
    taskService.setAssignee(task.getId(), GONZO);
    taskService.setVariable(task.getId(), "testVar", "someVariable");
    ids.add(task.getId());

    processEngineConfiguration.getClock().setCurrentTime(sdf.parse("03/03/2003 03:03:03.000"));
    // 2 tasks for management group
    for (int i = 0; i < 2; i++) {
      task = taskService.newTask();
      task.setName("managementTask");
      task.setPriority(10);
      taskService.saveTask(task);
      taskService.addCandidateGroup(task.getId(), "management");
      ids.add(task.getId());
    }

    processEngineConfiguration.getClock().setCurrentTime(sdf.parse("04/04/2004 04:04:04.000"));
    // 2 tasks for accountancy group
    for (int i = 0; i < 2; i++) {
      task = taskService.newTask();
      task.setName("accountancyTask");
      task.setDescription("accountancy description");
      taskService.saveTask(task);
      taskService.addCandidateGroup(task.getId(), "accountancy");
      ids.add(task.getId());
    }

    processEngineConfiguration.getClock().setCurrentTime(sdf.parse("05/05/2005 05:05:05.000"));
    // 1 task assigned to management and accountancy group
    task = taskService.newTask();
    task.setName("managementAndAccountancyTask");
    taskService.saveTask(task);
    taskService.addCandidateGroup(task.getId(), "management");
    taskService.addCandidateGroup(task.getId(), "accountancy");
    ids.add(task.getId());

    return ids;
  }

}

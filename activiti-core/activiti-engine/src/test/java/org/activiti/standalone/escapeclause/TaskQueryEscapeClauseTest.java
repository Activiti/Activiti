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

package org.activiti.standalone.escapeclause;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;

public class TaskQueryEscapeClauseTest extends AbstractEscapeClauseTestCase {

  private String deploymentOneId;

  private String deploymentTwoId;

  private ProcessInstance processInstance1;

  private ProcessInstance processInstance2;

  private Task task1;

  private Task task2;

  @Override
  protected void setUp() throws Exception {
    deploymentOneId = repositoryService
      .createDeployment()
      .tenantId("One%")
      .addClasspathResource("org/activiti/standalone/escapeclause/oneTaskProcessEscapeClauseTest.bpmn20.xml")
      .deploy()
      .getId();

    deploymentTwoId = repositoryService
      .createDeployment()
      .tenantId("Two_")
      .addClasspathResource("org/activiti/standalone/escapeclause/oneTaskProcessEscapeClauseTest.bpmn20.xml")
      .deploy()
      .getId();

    processInstance1 = runtimeService.startProcessInstanceByKeyAndTenantId("oneTaskProcess", "One%", "One%");
    runtimeService.setProcessInstanceName(processInstance1.getId(), "One%");

    processInstance2 = runtimeService.startProcessInstanceByKeyAndTenantId("oneTaskProcess", "Two_", "Two_");
    runtimeService.setProcessInstanceName(processInstance2.getId(), "Two_");

    task1 = taskService.createTaskQuery().processInstanceId(processInstance1.getId()).singleResult();
    taskService.setAssignee(task1.getId(), "assignee%");
    taskService.setOwner(task1.getId(), "owner%");
    taskService.setVariableLocal(task1.getId(), "var1", "One%");

    task2 = taskService.createTaskQuery().processInstanceId(processInstance2.getId()).singleResult();
    task2.setName("my task_");
    task2.setDescription("documentation_");
    taskService.saveTask(task2);
    taskService.setAssignee(task2.getId(), "assignee_");
    taskService.setOwner(task2.getId(), "owner_");
    taskService.setVariableLocal(task2.getId(), "var1", "Two_");

    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    repositoryService.deleteDeployment(deploymentOneId, true);
    repositoryService.deleteDeployment(deploymentTwoId, true);
  }

  @Test
  public void testQueryByNameLike(){
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // nameLike
        Task task = taskService.createTaskQuery().taskNameLike("%\\%%").singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getId()).isEqualTo(task1.getId());

        task = taskService.createTaskQuery().taskNameLike("%\\_%").singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getId()).isEqualTo(task2.getId());

        // orQuery
        task = taskService.createTaskQuery().or().taskNameLike("%\\%%").processDefinitionId("undefined").singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getId()).isEqualTo(task1.getId());

        task = taskService.createTaskQuery().or().taskNameLike("%\\_%").processDefinitionId("undefined").singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getId()).isEqualTo(task2.getId());
    }
  }

  @Test
  public void testQueryByNameLikeIgnoreCase(){
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // nameLikeIgnoreCase
        Task task = taskService.createTaskQuery().taskNameLikeIgnoreCase("%\\%%").singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getId()).isEqualTo(task1.getId());

        task = taskService.createTaskQuery().taskNameLikeIgnoreCase("%\\_%").singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getId()).isEqualTo(task2.getId());

        // orQuery
        task = taskService.createTaskQuery().or().taskNameLikeIgnoreCase("%\\%%").processDefinitionId("undefined").singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getId()).isEqualTo(task1.getId());

        task = taskService.createTaskQuery().or().taskNameLikeIgnoreCase("%\\_%").processDefinitionId("undefined").singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getId()).isEqualTo(task2.getId());
    }
  }

  @Test
  public void testQueryByDescriptionLike(){
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // descriptionLike
        Task task = taskService.createTaskQuery().taskDescriptionLike("%\\%%").singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getId()).isEqualTo(task1.getId());

        task = taskService.createTaskQuery().taskDescriptionLike("%\\_%").singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getId()).isEqualTo(task2.getId());

        // orQuery
        task = taskService.createTaskQuery().or().taskDescriptionLike("%\\%%").processDefinitionId("undefined").singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getId()).isEqualTo(task1.getId());

        task = taskService.createTaskQuery().or().taskDescriptionLike("%\\_%").processDefinitionId("undefined").singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getId()).isEqualTo(task2.getId());
    }
  }

  @Test
  public void testQueryByDescriptionLikeIgnoreCase(){
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // descriptionLikeIgnoreCase
        Task task = taskService.createTaskQuery().taskDescriptionLikeIgnoreCase("%\\%%").singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getId()).isEqualTo(task1.getId());

        task = taskService.createTaskQuery().taskDescriptionLikeIgnoreCase("%\\_%").singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getId()).isEqualTo(task2.getId());

        // orQuery
        task = taskService.createTaskQuery().or().taskDescriptionLikeIgnoreCase("%\\%%").processDefinitionId("undefined").singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getId()).isEqualTo(task1.getId());

        task = taskService.createTaskQuery().or().taskDescriptionLikeIgnoreCase("%\\_%").processDefinitionId("undefined").singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getId()).isEqualTo(task2.getId());
    }
  }

  @Test
  public void testQueryByAssigneeLike(){
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // assigneeLike
        Task task = taskService.createTaskQuery().taskAssigneeLike("%\\%%").singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getId()).isEqualTo(task1.getId());

        task = taskService.createTaskQuery().taskAssigneeLike("%\\_%").singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getId()).isEqualTo(task2.getId());

        // orQuery
        /*
        task = taskService.createTaskQuery().or().taskAssigneeLike("%\\%%").processDefinitionId("undefined").singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getId()).isEqualTo(task1.getId());

        task = taskService.createTaskQuery().or().taskAssigneeLike("%\\_%").processDefinitionId("undefined").singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getId()).isEqualTo(task2.getId());
        */
    }
  }

  @Test
  public void testQueryByAssigneeLikeIgnoreCase(){
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // assigneeLikeIgnoreCase
        Task task = taskService.createTaskQuery().taskAssigneeLike("%\\%%").singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getId()).isEqualTo(task1.getId());

        task = taskService.createTaskQuery().taskAssigneeLike("%\\_%").singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getId()).isEqualTo(task2.getId());

        // orQuery
        /*
        task = taskService.createTaskQuery().or().taskAssigneeLike("%\\%%").processDefinitionId("undefined").singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getId()).isEqualTo(task1.getId());

        task = taskService.createTaskQuery().or().taskAssigneeLike("%\\_%").processDefinitionId("undefined").singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getId()).isEqualTo(task2.getId());
        */
    }
  }

  @Test
  public void testQueryByOwnerLike(){
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // taskOwnerLike
        Task task = taskService.createTaskQuery().taskOwnerLike("%\\%%").singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getId()).isEqualTo(task1.getId());

        task = taskService.createTaskQuery().taskOwnerLike("%\\_%").singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getId()).isEqualTo(task2.getId());

        // orQuery
        task = taskService.createTaskQuery().or().taskOwnerLike("%\\%%").processDefinitionId("undefined").singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getId()).isEqualTo(task1.getId());

        task = taskService.createTaskQuery().or().taskOwnerLike("%\\_%").processDefinitionId("undefined").singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getId()).isEqualTo(task2.getId());
    }
  }

  @Test
  public void testQueryByOwnerLikeIgnoreCase(){
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // taskOwnerLikeIgnoreCase
        Task task = taskService.createTaskQuery().taskOwnerLikeIgnoreCase("%\\%%").singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getId()).isEqualTo(task1.getId());

        task = taskService.createTaskQuery().taskOwnerLikeIgnoreCase("%\\_%").singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getId()).isEqualTo(task2.getId());

        // orQuery
        task = taskService.createTaskQuery().or().taskOwnerLikeIgnoreCase("%\\%%").processDefinitionId("undefined").singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getId()).isEqualTo(task1.getId());

        task = taskService.createTaskQuery().or().taskOwnerLikeIgnoreCase("%\\_%").processDefinitionId("undefined").singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getId()).isEqualTo(task2.getId());
    }
  }

  @Test
  public void testQueryByProcessInstanceBusinessKeyLike(){
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // processInstanceBusinessKeyLike
        Task task = taskService.createTaskQuery().processInstanceBusinessKeyLike("%\\%%").singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getId()).isEqualTo(task1.getId());

        task = taskService.createTaskQuery().processInstanceBusinessKeyLike("%\\_%").singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getId()).isEqualTo(task2.getId());

        // orQuery
        task = taskService.createTaskQuery().or().processInstanceBusinessKeyLike("%\\%%").processDefinitionId("undefined").singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getId()).isEqualTo(task1.getId());

        task = taskService.createTaskQuery().or().processInstanceBusinessKeyLike("%\\_%").processDefinitionId("undefined").singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getId()).isEqualTo(task2.getId());
    }
  }

  @Test
  public void testQueryByProcessInstanceBusinessKeyLikeIgnoreCase(){
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // processInstanceBusinessKeyLike
        Task task = taskService.createTaskQuery().processInstanceBusinessKeyLikeIgnoreCase("%\\%%").singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getId()).isEqualTo(task1.getId());

        task = taskService.createTaskQuery().processInstanceBusinessKeyLikeIgnoreCase("%\\_%").singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getId()).isEqualTo(task2.getId());

        // orQuery
        /*
        task = taskService.createTaskQuery().or().processInstanceBusinessKeyLikeIgnoreCase("%\\%%").processDefinitionId("undefined").singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getId()).isEqualTo(task1.getId());

        task = taskService.createTaskQuery().or().processInstanceBusinessKeyLikeIgnoreCase("%\\_%").processDefinitionId("undefined").singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getId()).isEqualTo(task2.getId());
        */
    }
  }

  @Test
  public void testQueryByKeyLike(){
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // taskDefinitionKeyLike
        Task task = taskService.createTaskQuery().taskDefinitionKeyLike("%\\%%").singleResult();
        assertThat(task).isNull();

        task = taskService.createTaskQuery().taskDefinitionKeyLike("%\\_%").singleResult();
        assertThat(task).isNull();

        // orQuery
        task = taskService.createTaskQuery().or().taskDefinitionKeyLike("%\\%%").processDefinitionId("undefined").singleResult();
        assertThat(task).isNull();

        task = taskService.createTaskQuery().or().taskDefinitionKeyLike("%\\_%").processDefinitionId("undefined").singleResult();
        assertThat(task).isNull();
    }
  }

  @Test
  public void testQueryByProcessDefinitionKeyLike(){
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // processDefinitionKeyLike
        Task task = taskService.createTaskQuery().processDefinitionKeyLike("%\\%%").singleResult();
        assertThat(task).isNull();

        task = taskService.createTaskQuery().processDefinitionKeyLike("%\\_%").singleResult();
        assertThat(task).isNull();

        // orQuery
        task = taskService.createTaskQuery().or().processDefinitionKeyLike("%\\%%").processDefinitionId("undefined").singleResult();
        assertThat(task).isNull();

        task = taskService.createTaskQuery().or().processDefinitionKeyLike("%\\_%").processDefinitionId("undefined").singleResult();
        assertThat(task).isNull();
    }
  }

  @Test
  public void testQueryByProcessDefinitionKeyLikeIgnoreCase(){
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // processDefinitionKeyLikeIgnoreCase
        Task task = taskService.createTaskQuery().processDefinitionKeyLikeIgnoreCase("%\\%%").singleResult();
        assertThat(task).isNull();

        task = taskService.createTaskQuery().processDefinitionKeyLikeIgnoreCase("%\\_%").singleResult();
        assertThat(task).isNull();

        // orQuery
        task = taskService.createTaskQuery().or().processDefinitionKeyLikeIgnoreCase("%\\%%").processDefinitionId("undefined").singleResult();
        assertThat(task).isNull();

        task = taskService.createTaskQuery().or().processDefinitionKeyLikeIgnoreCase("%\\_%").processDefinitionId("undefined").singleResult();
        assertThat(task).isNull();
    }
  }

  @Test
  public void testQueryByProcessDefinitionNameLike(){
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // processDefinitionNameLike
        List<Task> list = taskService.createTaskQuery().processDefinitionNameLike("%\\%%").orderByTaskCreateTime().asc().list();
        assertThat(list).hasSize(2);
        List<String> taskIds = new ArrayList<String>(2);
        taskIds.add(list.get(0).getId());
        taskIds.add(list.get(1).getId());
        assertThat(taskIds.contains(task1.getId())).isTrue();
        assertThat(taskIds.contains(task2.getId())).isTrue();

        // orQuery
        list = taskService.createTaskQuery().or().processDefinitionNameLike("%\\%%").processDefinitionId("undefined").orderByTaskCreateTime().asc().list();
        assertThat(list).hasSize(2);
        taskIds = new ArrayList<String>(2);
        taskIds.add(list.get(0).getId());
        taskIds.add(list.get(1).getId());
        assertThat(taskIds.contains(task1.getId())).isTrue();
        assertThat(taskIds.contains(task2.getId())).isTrue();
    }
  }

  @Test
  public void testQueryLikeByQueryVariableValue() {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // taskVariableValueLike
        Task task = taskService.createTaskQuery().taskVariableValueLike("var1", "%\\%%").singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getId()).isEqualTo(task1.getId());

        task = taskService.createTaskQuery().taskVariableValueLike("var1", "%\\_%").singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getId()).isEqualTo(task2.getId());

        // orQuery
        task = taskService.createTaskQuery().or().taskVariableValueLike("var1", "%\\%%").processDefinitionId("undefined").singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getId()).isEqualTo(task1.getId());

        task = taskService.createTaskQuery().or().taskVariableValueLike("var1", "%\\_%").processDefinitionId("undefined").singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getId()).isEqualTo(task2.getId());
    }
  }

  public void testQueryLikeIgnoreCaseByQueryVariableValue() {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // taskVariableValueLikeIgnoreCase
        Task task = taskService.createTaskQuery().taskVariableValueLikeIgnoreCase("var1", "%\\%%").singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getId()).isEqualTo(task1.getId());

        task = taskService.createTaskQuery().taskVariableValueLikeIgnoreCase("var1", "%\\_%").singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getId()).isEqualTo(task2.getId());

        // orQuery
        task = taskService.createTaskQuery().or().taskVariableValueLikeIgnoreCase("var1", "%\\%%").processDefinitionId("undefined").singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getId()).isEqualTo(task1.getId());

        task = taskService.createTaskQuery().or().taskVariableValueLikeIgnoreCase("var1", "%\\_%").processDefinitionId("undefined").singleResult();
        assertThat(task).isNotNull();
        assertThat(task.getId()).isEqualTo(task2.getId());
    }
  }
}

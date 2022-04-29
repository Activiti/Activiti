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

package org.activiti.engine.test.api.history;

import static org.activiti.engine.impl.util.CollectionUtil.map;
import static org.activiti.engine.impl.util.CollectionUtil.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.task.Task;


public class HistoricProcessInstanceAndVariablesQueryTest extends PluggableActivitiTestCase {

  private static String PROCESS_DEFINITION_KEY = "oneTaskProcess";
  private static String PROCESS_DEFINITION_KEY_2 = "oneTaskProcess2";
  private static String PROCESS_DEFINITION_NAME_2 = "oneTaskProcess2Name";
  private static String PROCESS_DEFINITION_CATEGORY_2 = "org.activiti.enginge.test.api.runtime.2Category";
  private static String PROCESS_DEFINITION_KEY_3 = "oneTaskProcess3";

  private List<String> processInstanceIds;

  /**
   * Setup starts 4 process instances of oneTaskProcess and 1 instance of oneTaskProcess2
   */
  protected void setUp() throws Exception {
    super.setUp();
    repositoryService.createDeployment()
      .addClasspathResource("org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml")
      .addClasspathResource("org/activiti/engine/test/api/runtime/oneTaskProcess2.bpmn20.xml")
      .addClasspathResource("org/activiti/engine/test/api/runtime/oneTaskProcess3.bpmn20.xml")
      .deploy();

    processInstanceIds = new ArrayList<String>();
    for (int i = 0; i < 4; i++) {
      processInstanceIds.add(runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY, i + "", map(
          "test", "test",
          "test2", "test2"
      )).getId());
      if (i == 0) {
        Task task = taskService.createTaskQuery().processInstanceId(processInstanceIds.get(0)).singleResult();
        taskService.complete(task.getId());
      }
    }
    processInstanceIds.add(runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY_2, "1", singletonMap("anothertest", 123)).getId());
    processInstanceIds.add(runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY_3, "1", singletonMap("casetest", "MyTest")).getId());
  }

  protected void tearDown() throws Exception {
    for (org.activiti.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
    super.tearDown();
  }

  public void testQuery() {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery().includeProcessVariables().variableValueEquals("anothertest", 123).singleResult();
      Map<String, Object> variableMap = processInstance.getProcessVariables();
      assertThat(variableMap).hasSize(1);
      assertThat(variableMap.get("anothertest")).isEqualTo(123);

      List<HistoricProcessInstance> instanceList = historyService.createHistoricProcessInstanceQuery().includeProcessVariables().list();
      assertThat(instanceList).hasSize(6);

      instanceList = historyService.createHistoricProcessInstanceQuery().includeProcessVariables().processDefinitionKey(PROCESS_DEFINITION_KEY).list();
      assertThat(instanceList).hasSize(4);
      processInstance = instanceList.get(0);
      variableMap = processInstance.getProcessVariables();
      assertThat(variableMap).hasSize(2);
      assertThat(variableMap.get("test")).isEqualTo("test");
      assertThat(variableMap.get("test2")).isEqualTo("test2");
      assertThat(instanceList.get(0).getProcessDefinitionKey()).isEqualTo(PROCESS_DEFINITION_KEY);

      processInstance = historyService.createHistoricProcessInstanceQuery().includeProcessVariables().processDefinitionKey(PROCESS_DEFINITION_KEY_2).singleResult();
      variableMap = processInstance.getProcessVariables();
      assertThat(variableMap).hasSize(1);
      assertThat(variableMap.get("anothertest")).isEqualTo(123);

      processInstance = historyService.createHistoricProcessInstanceQuery().includeProcessVariables().finished().singleResult();
      variableMap = processInstance.getProcessVariables();
      assertThat(variableMap).hasSize(2);
      assertThat(variableMap.get("test")).isEqualTo("test");
      assertThat(variableMap.get("test2")).isEqualTo("test2");

      instanceList = historyService.createHistoricProcessInstanceQuery().includeProcessVariables().listPage(0, 50);
      assertThat(instanceList).hasSize(6);
      assertThat(historyService.createHistoricProcessInstanceQuery().includeProcessVariables().count()).isEqualTo(6);

      instanceList = historyService.createHistoricProcessInstanceQuery()
          .variableValueEquals("test", "test")
          .includeProcessVariables()
          .listPage(0, 50);
      assertThat(instanceList).hasSize(4);
      assertThat(historyService.createHistoricProcessInstanceQuery().variableValueEquals("test", "test").includeProcessVariables().count()).isEqualTo(4);

      instanceList = historyService.createHistoricProcessInstanceQuery()
          .variableValueLike("test", "te%")
          .includeProcessVariables()
          .list();
      assertThat(instanceList).hasSize(4);
      assertThat(historyService.createHistoricProcessInstanceQuery().variableValueLike("test", "te%").includeProcessVariables().count()).isEqualTo(4);

      instanceList = historyService.createHistoricProcessInstanceQuery()
          .variableValueLike("test2", "te%2")
          .includeProcessVariables()
          .list();
      assertThat(instanceList).hasSize(4);
      assertThat(historyService.createHistoricProcessInstanceQuery().variableValueLike("test2", "te%2").includeProcessVariables().count()).isEqualTo(4);

      instanceList = historyService.createHistoricProcessInstanceQuery()
          .variableValueLikeIgnoreCase("test", "te%")
          .includeProcessVariables()
          .list();
      assertThat(instanceList).hasSize(4);
      assertThat(historyService.createHistoricProcessInstanceQuery().variableValueLikeIgnoreCase("test", "te%").includeProcessVariables().count()).isEqualTo(4);

      instanceList = historyService.createHistoricProcessInstanceQuery()
          .variableValueLikeIgnoreCase("test", "t3%")
          .includeProcessVariables()
          .list();
      assertThat(instanceList).hasSize(0);
      assertThat(historyService.createHistoricProcessInstanceQuery().variableValueLikeIgnoreCase("test", "t3%").includeProcessVariables().count()).isEqualTo(0);

      instanceList = historyService.createHistoricProcessInstanceQuery().includeProcessVariables().listPage(0, 50);
      assertThat(instanceList).hasSize(6);
      assertThat(historyService.createHistoricProcessInstanceQuery().includeProcessVariables().count()).isEqualTo(6);

      instanceList = historyService.createHistoricProcessInstanceQuery()
          .variableValueEquals("test", "test")
          .includeProcessVariables()
          .listPage(0, 1);
      assertThat(instanceList).hasSize(1);
      processInstance = instanceList.get(0);
      variableMap = processInstance.getProcessVariables();
      assertThat(variableMap).hasSize(2);
      assertThat(variableMap.get("test")).isEqualTo("test");
      assertThat(variableMap.get("test2")).isEqualTo("test2");
      assertThat(historyService.createHistoricProcessInstanceQuery().variableValueEquals("test", "test").includeProcessVariables().count()).isEqualTo(4);

      instanceList = historyService.createHistoricProcessInstanceQuery().includeProcessVariables().processDefinitionKey(PROCESS_DEFINITION_KEY).listPage(1, 2);
      assertThat(instanceList).hasSize(2);
      processInstance = instanceList.get(0);
      variableMap = processInstance.getProcessVariables();
      assertThat(variableMap).hasSize(2);
      assertThat(variableMap.get("test")).isEqualTo("test");
      assertThat(variableMap.get("test2")).isEqualTo("test2");

      instanceList = historyService.createHistoricProcessInstanceQuery().includeProcessVariables().processDefinitionKey(PROCESS_DEFINITION_KEY).listPage(3, 4);
      assertThat(instanceList).hasSize(1);
      processInstance = instanceList.get(0);
      variableMap = processInstance.getProcessVariables();
      assertThat(variableMap).hasSize(2);
      assertThat(variableMap.get("test")).isEqualTo("test");
      assertThat(variableMap.get("test2")).isEqualTo("test2");

      instanceList = historyService.createHistoricProcessInstanceQuery().includeProcessVariables().processDefinitionKey(PROCESS_DEFINITION_KEY).listPage(4, 2);
      assertThat(instanceList).hasSize(0);

      instanceList = historyService.createHistoricProcessInstanceQuery().variableValueEquals("test", "test").includeProcessVariables().orderByProcessInstanceId().asc().listPage(0, 50);
      assertThat(instanceList).hasSize(4);
    }
  }

  public void testQueryByprocessDefinition() {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // DeploymentId
        String deploymentId = repositoryService.createDeploymentQuery().list().get(0).getId();
        HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery().includeProcessVariables()
                .variableValueEquals("anothertest", 123).deploymentId(deploymentId).singleResult();
        Map<String, Object> variableMap = processInstance.getProcessVariables();
        assertThat(variableMap).hasSize(1);
        assertThat(variableMap.get("anothertest")).isEqualTo(123);
        assertThat(processInstance.getDeploymentId()).isEqualTo(deploymentId);

        processInstance = historyService.createHistoricProcessInstanceQuery().includeProcessVariables()
                .variableValueEquals("anothertest", "invalid").deploymentId(deploymentId).singleResult();
        assertThat(processInstance).isNull();

      // ProcessDefinitionName
        processInstance = historyService.createHistoricProcessInstanceQuery().includeProcessVariables()
                .variableValueEquals("anothertest", 123).processDefinitionName(PROCESS_DEFINITION_NAME_2).singleResult();
        variableMap = processInstance.getProcessVariables();
        assertThat(variableMap).hasSize(1);
        assertThat(variableMap.get("anothertest")).isEqualTo(123);
        assertThat(processInstance.getProcessDefinitionName()).isEqualTo(PROCESS_DEFINITION_NAME_2);

        processInstance = historyService.createHistoricProcessInstanceQuery().includeProcessVariables()
                .variableValueEquals("test", "test").processDefinitionName(PROCESS_DEFINITION_NAME_2).singleResult();
        assertThat(processInstance).isNull();

        // ProcessDefinitionCategory
        processInstance = historyService.createHistoricProcessInstanceQuery().includeProcessVariables()
                .variableValueEquals("anothertest", 123).processDefinitionCategory(PROCESS_DEFINITION_CATEGORY_2).singleResult();
        variableMap = processInstance.getProcessVariables();
        assertThat(variableMap).hasSize(1);
        assertThat(variableMap.get("anothertest")).isEqualTo(123);

        processInstance = historyService.createHistoricProcessInstanceQuery().includeProcessVariables()
                .variableValueEquals("test", "test").processDefinitionCategory(PROCESS_DEFINITION_CATEGORY_2).singleResult();
        assertThat(processInstance).isNull();
    }
  }

  public void testOrQuery() {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery().includeProcessVariables().or().variableValueEquals("anothertest", 123)
          .processDefinitionId("undefined").endOr().singleResult();
      Map<String, Object> variableMap = processInstance.getProcessVariables();
      assertThat(variableMap).hasSize(1);
      assertThat(variableMap.get("anothertest")).isEqualTo(123);

      processInstance = historyService.createHistoricProcessInstanceQuery().includeProcessVariables()
              .or()
                .variableValueEquals("anothertest", 123)
                .processDefinitionId("undefined")
              .endOr()
              .or()
                .processDefinitionKey(PROCESS_DEFINITION_KEY_2)
                .processDefinitionId("undefined")
              .endOr()
              .singleResult();
      variableMap = processInstance.getProcessVariables();
      assertThat(variableMap).hasSize(1);
      assertThat(variableMap.get("anothertest")).isEqualTo(123);

      processInstance = historyService.createHistoricProcessInstanceQuery().includeProcessVariables()
          .or()
            .variableValueEquals("anothertest", 123)
            .processDefinitionId("undefined")
          .endOr()
          .or()
            .processDefinitionKey(PROCESS_DEFINITION_KEY)
            .processDefinitionId("undefined")
          .endOr()
          .singleResult();
      assertThat(processInstance).isNull();

      processInstance = historyService.createHistoricProcessInstanceQuery().includeProcessVariables()
          .or()
            .variableValueLikeIgnoreCase("casetest", "mytest")
            .processDefinitionId("undefined")
          .endOr()
          .singleResult();
      assertThat(processInstance).isNotNull();
      variableMap = processInstance.getProcessVariables();
      assertThat(variableMap).hasSize(1);
      assertThat(variableMap.get("casetest")).isEqualTo("MyTest");

      List<HistoricProcessInstance> instanceList = historyService.createHistoricProcessInstanceQuery().includeProcessVariables().or()
          .processDefinitionKey(PROCESS_DEFINITION_KEY).processDefinitionId("undefined").endOr().list();
      assertThat(instanceList).hasSize(4);
      processInstance = instanceList.get(0);
      variableMap = processInstance.getProcessVariables();
      assertThat(variableMap).hasSize(2);
      assertThat(variableMap.get("test")).isEqualTo("test");
      assertThat(variableMap.get("test2")).isEqualTo("test2");

      processInstance = historyService.createHistoricProcessInstanceQuery().includeProcessVariables().or().processDefinitionKey(PROCESS_DEFINITION_KEY_2).processDefinitionId("undefined").endOr()
          .singleResult();
      variableMap = processInstance.getProcessVariables();
      assertThat(variableMap).hasSize(1);
      assertThat(variableMap.get("anothertest")).isEqualTo(123);

      processInstance = historyService.createHistoricProcessInstanceQuery().includeProcessVariables().or().finished().processDefinitionId("undefined").endOr().singleResult();
      variableMap = processInstance.getProcessVariables();
      assertThat(variableMap).hasSize(2);
      assertThat(variableMap.get("test")).isEqualTo("test");
      assertThat(variableMap.get("test2")).isEqualTo("test2");

      instanceList = historyService.createHistoricProcessInstanceQuery().or().variableValueEquals("test", "test").processDefinitionId("undefined").endOr().includeProcessVariables().listPage(0, 50);
      assertThat(instanceList).hasSize(4);

      instanceList = historyService.createHistoricProcessInstanceQuery().or().variableValueEquals("test", "test").processDefinitionId("undefined").endOr().includeProcessVariables().listPage(0, 1);
      assertThat(instanceList).hasSize(1);
      processInstance = instanceList.get(0);
      variableMap = processInstance.getProcessVariables();
      assertThat(variableMap).hasSize(2);
      assertThat(variableMap.get("test")).isEqualTo("test");
      assertThat(variableMap.get("test2")).isEqualTo("test2");

      instanceList = historyService.createHistoricProcessInstanceQuery().includeProcessVariables().or().processDefinitionKey(PROCESS_DEFINITION_KEY).processDefinitionId("undefined").endOr()
          .listPage(1, 2);
      assertThat(instanceList).hasSize(2);
      processInstance = instanceList.get(0);
      variableMap = processInstance.getProcessVariables();
      assertThat(variableMap).hasSize(2);
      assertThat(variableMap.get("test")).isEqualTo("test");
      assertThat(variableMap.get("test2")).isEqualTo("test2");

      instanceList = historyService.createHistoricProcessInstanceQuery().includeProcessVariables().or().processDefinitionKey(PROCESS_DEFINITION_KEY).processDefinitionId("undefined").endOr()
          .listPage(3, 4);
      assertThat(instanceList).hasSize(1);
      processInstance = instanceList.get(0);
      variableMap = processInstance.getProcessVariables();
      assertThat(variableMap).hasSize(2);
      assertThat(variableMap.get("test")).isEqualTo("test");
      assertThat(variableMap.get("test2")).isEqualTo("test2");

      instanceList = historyService.createHistoricProcessInstanceQuery().includeProcessVariables().or().processDefinitionKey(PROCESS_DEFINITION_KEY).processDefinitionId("undefined").endOr()
          .listPage(4, 2);
      assertThat(instanceList).hasSize(0);

      instanceList = historyService.createHistoricProcessInstanceQuery().or().variableValueEquals("test", "test").processDefinitionId("undefined").endOr().includeProcessVariables()
          .orderByProcessInstanceId().asc().listPage(0, 50);
      assertThat(instanceList).hasSize(4);

      instanceList = historyService.createHistoricProcessInstanceQuery()
          .or()
            .variableValueEquals("test", "test")
            .processDefinitionId("undefined")
          .endOr()
          .or()
            .processDefinitionKey(PROCESS_DEFINITION_KEY)
            .processDefinitionId("undefined")
          .endOr()
          .includeProcessVariables()
          .orderByProcessInstanceId()
          .asc()
          .listPage(0, 50);
      assertThat(instanceList).hasSize(4);
    }
  }

  public void testOrQueryMultipleVariableValues() {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      HistoricProcessInstanceQuery query0 = historyService.createHistoricProcessInstanceQuery().includeProcessVariables().or();
      for (int i = 0; i < 20; i++) {
          query0 = query0.variableValueEquals("anothertest", i);
      }
      query0 = query0.processDefinitionId("undefined").endOr();

      assertThat(query0.singleResult()).isNull();

      HistoricProcessInstanceQuery query1 = historyService.createHistoricProcessInstanceQuery().includeProcessVariables().or().variableValueEquals("anothertest", 123);
      for (int i = 0; i < 20; i++) {
          query1 = query1.variableValueEquals("anothertest", i);
      }
      query1 = query1.processDefinitionId("undefined").endOr();

      HistoricProcessInstance processInstance = query1.singleResult();
      Map<String, Object> variableMap = processInstance.getProcessVariables();
      assertThat(variableMap).hasSize(1);
      assertThat(variableMap.get("anothertest")).isEqualTo(123);

      HistoricProcessInstanceQuery query2 = historyService.createHistoricProcessInstanceQuery().includeProcessVariables()
              .or();
      for (int i = 0; i < 20; i++) {
          query2 = query2.variableValueEquals("anothertest", i);
      }
      query2 = query2.processDefinitionId("undefined")
              .endOr()
              .or()
                .processDefinitionKey(PROCESS_DEFINITION_KEY_2)
                .processDefinitionId("undefined")
              .endOr();
      assertThat(query2.singleResult()).isNull();

      HistoricProcessInstanceQuery query3 = historyService.createHistoricProcessInstanceQuery().includeProcessVariables()
              .or().variableValueEquals("anothertest", 123);
      for (int i = 0; i < 20; i++) {
          query3 = query3.variableValueEquals("anothertest", i);
      }
      query3 = query3.processDefinitionId("undefined")
              .endOr()
              .or()
                .processDefinitionKey(PROCESS_DEFINITION_KEY_2)
                .processDefinitionId("undefined")
              .endOr();
      variableMap = query3.singleResult().getProcessVariables();
      assertThat(variableMap).hasSize(1);
      assertThat(variableMap.get("anothertest")).isEqualTo(123);
    }
  }

  public void testOrQueryByprocessDefinition() {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        // DeploymentId
        String deploymentId = repositoryService.createDeploymentQuery().list().get(0).getId();
        HistoricProcessInstanceQuery historicprocessInstanceQuery = historyService.createHistoricProcessInstanceQuery().includeProcessVariables()
                .or().variableValueEquals("anothertest", "invalid").deploymentId(deploymentId).endOr();
        assertThat(historicprocessInstanceQuery.list()).hasSize(6);
        assertThat(historicprocessInstanceQuery.count()).isEqualTo(6);
        Map<String, Object> variableMap = historicprocessInstanceQuery.list().get(4).getProcessVariables();
        assertThat(variableMap).hasSize(1);
        assertThat(variableMap.get("anothertest")).isEqualTo(123);
        for(HistoricProcessInstance processInstance : historicprocessInstanceQuery.list()){
          assertThat(processInstance.getDeploymentId()).isEqualTo(deploymentId);
        }

        HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery().includeProcessVariables()
                .or().variableValueEquals("anothertest", "invalid").deploymentId("invalid").endOr().singleResult();
        assertThat(processInstance).isNull();

        // ProcessDefinitionName
        processInstance = historyService.createHistoricProcessInstanceQuery().includeProcessVariables()
                .or().variableValueEquals("anothertest", "invalid").processDefinitionName(PROCESS_DEFINITION_NAME_2).endOr().singleResult();
        variableMap = processInstance.getProcessVariables();
        assertThat(variableMap).hasSize(1);
        assertThat(variableMap.get("anothertest")).isEqualTo(123);
        assertThat(processInstance.getProcessDefinitionName()).isEqualTo(PROCESS_DEFINITION_NAME_2);

        processInstance = historyService.createHistoricProcessInstanceQuery().includeProcessVariables()
                .or().variableValueEquals("anothertest", "invalid").processDefinitionName("invalid").endOr().singleResult();
        assertThat(processInstance).isNull();

        // ProcessDefinitionCategory
        processInstance = historyService.createHistoricProcessInstanceQuery().includeProcessVariables()
                .or().variableValueEquals("anothertest", "invalid").processDefinitionCategory(PROCESS_DEFINITION_CATEGORY_2).endOr().singleResult();
        variableMap = processInstance.getProcessVariables();
        assertThat(variableMap).hasSize(1);
        assertThat(variableMap.get("anothertest")).isEqualTo(123);

        processInstance = historyService.createHistoricProcessInstanceQuery().includeProcessVariables()
                .or().variableValueEquals("anothertest", "invalid").processDefinitionCategory("invalid").endOr().singleResult();
        assertThat(processInstance).isNull();
    }
  }
}

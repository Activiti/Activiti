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
package org.activiti.engine.test.api.runtime;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.test.Deployment;

/**
 */
public class ProcessInstanceQueryTest extends PluggableActivitiTestCase {

  private static final int PROCESS_DEFINITION_KEY_DEPLOY_COUNT = 4;
  private static final int PROCESS_DEFINITION_KEY_2_DEPLOY_COUNT = 1;
  private static final int PROCESS_DEPLOY_COUNT = PROCESS_DEFINITION_KEY_DEPLOY_COUNT + PROCESS_DEFINITION_KEY_2_DEPLOY_COUNT;
  private static final String PROCESS_DEFINITION_KEY = "oneTaskProcess";
  private static final String PROCESS_DEFINITION_KEY_2 = "oneTaskProcess2";
  private static final String PROCESS_DEFINITION_NAME = "oneTaskProcessName";
  private static final String PROCESS_DEFINITION_NAME_2 = "oneTaskProcess2Name";
  private static final String PROCESS_DEFINITION_CATEGORY = "org.activiti.enginge.test.api.runtime.Category";
  private static final String PROCESS_DEFINITION_CATEGORY_2 = "org.activiti.enginge.test.api.runtime.2Category";


  private org.activiti.engine.repository.Deployment deployment;
  private List<String> processInstanceIds;

  /**
   * Setup starts 4 process instances of oneTaskProcess and 1 instance of oneTaskProcess2
   */
  protected void setUp() throws Exception {
    super.setUp();
    deployment = repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml")
        .addClasspathResource("org/activiti/engine/test/api/runtime/oneTaskProcess2.bpmn20.xml").deploy();

    processInstanceIds = new ArrayList<String>();
    for (int i = 0; i < PROCESS_DEFINITION_KEY_DEPLOY_COUNT; i++) {
      processInstanceIds.add(runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY, i + "").getId());
    }
    processInstanceIds.add(runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY_2, "1").getId());
  }

  protected void tearDown() throws Exception {
    for (org.activiti.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
    super.tearDown();
  }

  public void testQueryNoSpecificsList() {
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
    assertThat(query.count()).isEqualTo(PROCESS_DEPLOY_COUNT);
    assertThat(query.list()).hasSize(PROCESS_DEPLOY_COUNT);
  }

  public void testQueryNoSpecificsSingleResult() {
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> query.singleResult());
  }

  public void testQueryByProcessDefinitionKeySingleResult() {
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().processDefinitionKey(PROCESS_DEFINITION_KEY_2);
    assertThat(query.count()).isEqualTo(PROCESS_DEFINITION_KEY_2_DEPLOY_COUNT);
    assertThat(query.list()).hasSize(PROCESS_DEFINITION_KEY_2_DEPLOY_COUNT);
    assertThat(query.singleResult()).isNotNull();
  }

  public void testQueryByInvalidProcessDefinitionKey() {
    assertThat(runtimeService.createProcessInstanceQuery().processDefinitionKey("invalid").singleResult()).isNull();
    assertThat(runtimeService.createProcessInstanceQuery().processDefinitionKey("invalid").list()).hasSize(0);
  }

  public void testQueryByProcessDefinitionKeyMultipleResults() {
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().processDefinitionKey(PROCESS_DEFINITION_KEY);
    assertThat(query.count()).isEqualTo(PROCESS_DEFINITION_KEY_DEPLOY_COUNT);
    assertThat(query.list()).hasSize(PROCESS_DEFINITION_KEY_DEPLOY_COUNT);

    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> query.singleResult());
  }

  public void testQueryByProcessDefinitionKeys() {
    final Set<String> processDefinitionKeySet = new HashSet<String>(2);
    processDefinitionKeySet.add(PROCESS_DEFINITION_KEY);
    processDefinitionKeySet.add(PROCESS_DEFINITION_KEY_2);

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().processDefinitionKeys(processDefinitionKeySet);
    assertThat(query.count()).isEqualTo(PROCESS_DEPLOY_COUNT);
    assertThat(query.list()).hasSize(PROCESS_DEPLOY_COUNT);
    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> query.singleResult());
  }

  public void testQueryByInvalidProcessDefinitionKeys() {
    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> runtimeService.createProcessInstanceQuery().processDefinitionKeys(null));

    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> runtimeService.createProcessInstanceQuery().processDefinitionKeys(emptySet()));
  }

  public void testQueryByProcessInstanceId() {
    for (String processInstanceId : processInstanceIds) {
      assertThat(runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult()).isNotNull();
      assertThat(runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).list()).hasSize(1);
    }
  }

  public void testQueryByProcessDefinitionCategory() {
    assertThat(runtimeService.createProcessInstanceQuery().processDefinitionCategory(PROCESS_DEFINITION_CATEGORY).count()).isEqualTo(PROCESS_DEFINITION_KEY_DEPLOY_COUNT);
    assertThat(runtimeService.createProcessInstanceQuery().processDefinitionCategory(PROCESS_DEFINITION_CATEGORY_2).count()).isEqualTo(PROCESS_DEFINITION_KEY_2_DEPLOY_COUNT);
  }

  public void testOrQueryByProcessDefinitionCategory() {
    assertThat(runtimeService.createProcessInstanceQuery().or().processDefinitionCategory(PROCESS_DEFINITION_CATEGORY).processDefinitionId("undefined").endOr().count()).isEqualTo(PROCESS_DEFINITION_KEY_DEPLOY_COUNT);
    assertThat(runtimeService.createProcessInstanceQuery().or().processDefinitionCategory(PROCESS_DEFINITION_CATEGORY_2).processDefinitionId("undefined").endOr().count()).isEqualTo(PROCESS_DEFINITION_KEY_2_DEPLOY_COUNT);
  }

  public void testQueryByProcessInstanceName() {
    runtimeService.setProcessInstanceName(processInstanceIds.get(0), "new name");
    assertThat(runtimeService.createProcessInstanceQuery().processInstanceName("new name").singleResult()).isNotNull();
    assertThat(runtimeService.createProcessInstanceQuery().processInstanceName("new name").list()).hasSize(1);

    assertThat(runtimeService.createProcessInstanceQuery().processInstanceName("unexisting").singleResult()).isNull();
  }

  public void testOrQueryByProcessInstanceName() {
    runtimeService.setProcessInstanceName(processInstanceIds.get(0), "new name");
    assertThat(runtimeService.createProcessInstanceQuery().or().processInstanceName("new name").processDefinitionId("undefined").endOr().singleResult()).isNotNull();
    assertThat(runtimeService.createProcessInstanceQuery().or().processInstanceName("new name").processDefinitionId("undefined").endOr().list()).hasSize(1);

    assertThat(runtimeService.createProcessInstanceQuery()
        .or()
          .processInstanceName("new name")
          .processDefinitionId("undefined")
        .endOr()
        .or()
          .processDefinitionKey(PROCESS_DEFINITION_KEY)
          .processDefinitionId("undefined")
        .singleResult()).isNotNull();

    assertThat(runtimeService.createProcessInstanceQuery().or().processInstanceName("unexisting").processDefinitionId("undefined").endOr().singleResult()).isNull();

    assertThat(runtimeService.createProcessInstanceQuery()
        .or()
          .processInstanceName("unexisting")
          .processDefinitionId("undefined")
        .endOr()
        .or()
          .processDefinitionKey(PROCESS_DEFINITION_KEY)
          .processDefinitionId("undefined")
        .endOr()
        .singleResult()).isNull();
  }

  public void testQueryByProcessInstanceNameLike() {
    runtimeService.setProcessInstanceName(processInstanceIds.get(0), "new name");
    assertThat(runtimeService.createProcessInstanceQuery().processInstanceNameLike("% name").singleResult()).isNotNull();
    assertThat(runtimeService.createProcessInstanceQuery().processInstanceNameLike("new name").list()).hasSize(1);

    assertThat(runtimeService.createProcessInstanceQuery().processInstanceNameLike("%nope").singleResult()).isNull();
  }

  public void testOrQueryByProcessInstanceNameLike() {
    runtimeService.setProcessInstanceName(processInstanceIds.get(0), "new name");
    assertThat(runtimeService.createProcessInstanceQuery().or().processInstanceNameLike("% name").processDefinitionId("undefined").endOr().singleResult()).isNotNull();
    assertThat(runtimeService.createProcessInstanceQuery().or().processInstanceNameLike("new name").processDefinitionId("undefined").endOr().list()).hasSize(1);

    assertThat(runtimeService.createProcessInstanceQuery().or().processInstanceNameLike("%nope").processDefinitionId("undefined").endOr().singleResult()).isNull();
  }

  public void testOrQueryByProcessInstanceNameLikeIgnoreCase() {
    runtimeService.setProcessInstanceName(processInstanceIds.get(0), "new name");
    runtimeService.setProcessInstanceName(processInstanceIds.get(1), "other Name!");

    // Runtime
    assertThat(runtimeService.createProcessInstanceQuery().or().processInstanceNameLikeIgnoreCase("%name%").processDefinitionId("undefined").endOr().list()).hasSize(2);
    assertThat(runtimeService.createProcessInstanceQuery().processInstanceNameLikeIgnoreCase("%name%").list()).hasSize(2);
    assertThat(runtimeService.createProcessInstanceQuery().processInstanceNameLikeIgnoreCase("%NAME%").list()).hasSize(2);
    assertThat(runtimeService.createProcessInstanceQuery().processInstanceNameLikeIgnoreCase("%NaM%").list()).hasSize(2);
    assertThat(runtimeService.createProcessInstanceQuery().processInstanceNameLikeIgnoreCase("%the%").list()).hasSize(1);
    assertThat(runtimeService.createProcessInstanceQuery().processInstanceNameLikeIgnoreCase("new%").list()).hasSize(1);
    assertThat(runtimeService.createProcessInstanceQuery().or().processInstanceNameLikeIgnoreCase("%nope").processDefinitionId("undefined").endOr().singleResult()).isNull();

    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
      // History
      assertThat(historyService.createHistoricProcessInstanceQuery().or().processInstanceNameLikeIgnoreCase("%name%").processDefinitionId("undefined").endOr().list()).hasSize(2);
      assertThat(historyService.createHistoricProcessInstanceQuery().processInstanceNameLikeIgnoreCase("%name%").list()).hasSize(2);
      assertThat(historyService.createHistoricProcessInstanceQuery().processInstanceNameLikeIgnoreCase("%NAME%").list()).hasSize(2);
      assertThat(historyService.createHistoricProcessInstanceQuery().processInstanceNameLikeIgnoreCase("%NaM%").list()).hasSize(2);
      assertThat(historyService.createHistoricProcessInstanceQuery().processInstanceNameLikeIgnoreCase("%the%").list()).hasSize(1);
      assertThat(historyService.createHistoricProcessInstanceQuery().processInstanceNameLikeIgnoreCase("new%").list()).hasSize(1);
      assertThat(historyService.createHistoricProcessInstanceQuery().or().processInstanceNameLikeIgnoreCase("%nope").processDefinitionId("undefined").endOr().singleResult()).isNull();
    }
  }

  public void testQueryByBusinessKeyAndProcessDefinitionKey() {
    assertThat(runtimeService.createProcessInstanceQuery().processInstanceBusinessKey("0", PROCESS_DEFINITION_KEY).count()).isEqualTo(1);
    assertThat(runtimeService.createProcessInstanceQuery().processInstanceBusinessKey("1", PROCESS_DEFINITION_KEY).count()).isEqualTo(1);
    assertThat(runtimeService.createProcessInstanceQuery().processInstanceBusinessKey("2", PROCESS_DEFINITION_KEY).count()).isEqualTo(1);
    assertThat(runtimeService.createProcessInstanceQuery().processInstanceBusinessKey("3", PROCESS_DEFINITION_KEY).count()).isEqualTo(1);
    assertThat(runtimeService.createProcessInstanceQuery().processInstanceBusinessKey("1", PROCESS_DEFINITION_KEY_2).count()).isEqualTo(1);
  }

  public void testQueryByBusinessKey() {
    assertThat(runtimeService.createProcessInstanceQuery().processInstanceBusinessKey("0").count()).isEqualTo(1);
    assertThat(runtimeService.createProcessInstanceQuery().processInstanceBusinessKey("1").count()).isEqualTo(2);
  }

  public void testQueryByInvalidBusinessKey() {
    assertThat(runtimeService.createProcessInstanceQuery().processInstanceBusinessKey("invalid").count()).isEqualTo(0);

    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(null).count());
  }

  public void testQueryByProcessDefinitionId() {
    final ProcessDefinition processDefinition1 = repositoryService.createProcessDefinitionQuery().processDefinitionKey(PROCESS_DEFINITION_KEY).singleResult();
    ProcessInstanceQuery query1 = runtimeService.createProcessInstanceQuery().processDefinitionId(processDefinition1.getId());
    assertThat(query1.count()).isEqualTo(PROCESS_DEFINITION_KEY_DEPLOY_COUNT);
    assertThat(query1.list()).hasSize(PROCESS_DEFINITION_KEY_DEPLOY_COUNT);
    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> query1.singleResult());

    final ProcessDefinition processDefinition2 = repositoryService.createProcessDefinitionQuery().processDefinitionKey(PROCESS_DEFINITION_KEY_2).singleResult();
    ProcessInstanceQuery query2 = runtimeService.createProcessInstanceQuery().processDefinitionId(processDefinition2.getId());
    assertThat(query2.count()).isEqualTo(PROCESS_DEFINITION_KEY_2_DEPLOY_COUNT);
    assertThat(query2.list()).hasSize(PROCESS_DEFINITION_KEY_2_DEPLOY_COUNT);
    assertThat(query2.singleResult()).isNotNull();
  }

  public void testQueryByProcessDefinitionIds() {
    final ProcessDefinition processDefinition1 = repositoryService.createProcessDefinitionQuery().processDefinitionKey(PROCESS_DEFINITION_KEY).singleResult();
    final ProcessDefinition processDefinition2 = repositoryService.createProcessDefinitionQuery().processDefinitionKey(PROCESS_DEFINITION_KEY_2).singleResult();

    final Set<String> processDefinitionIdSet = new HashSet<String>(2);
    processDefinitionIdSet.add(processDefinition1.getId());
    processDefinitionIdSet.add(processDefinition2.getId());

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().processDefinitionIds(processDefinitionIdSet);
    assertThat(query.count()).isEqualTo(PROCESS_DEPLOY_COUNT);
    assertThat(query.list()).hasSize(PROCESS_DEPLOY_COUNT);
    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> query.singleResult());
  }

  public void testQueryByInvalidProcessDefinitionIds() {
    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> runtimeService.createProcessInstanceQuery().processDefinitionIds(null));

    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> runtimeService.createProcessInstanceQuery().processDefinitionIds(emptySet()));
  }

  public void testQueryByProcessDefinitionName() {
    assertThat(runtimeService.createProcessInstanceQuery().processDefinitionName(PROCESS_DEFINITION_NAME).count()).isEqualTo(PROCESS_DEFINITION_KEY_DEPLOY_COUNT);
    assertThat(runtimeService.createProcessInstanceQuery().processDefinitionName(PROCESS_DEFINITION_NAME_2).count()).isEqualTo(PROCESS_DEFINITION_KEY_2_DEPLOY_COUNT);
  }

  public void testOrQueryByProcessDefinitionName() {
    assertThat(runtimeService.createProcessInstanceQuery().or().processDefinitionName(PROCESS_DEFINITION_NAME).processDefinitionId("undefined").endOr().count())
        .isEqualTo(PROCESS_DEFINITION_KEY_DEPLOY_COUNT);
    assertThat(runtimeService.createProcessInstanceQuery().or().processDefinitionName(PROCESS_DEFINITION_NAME_2).processDefinitionId("undefined").endOr().count())
        .isEqualTo(PROCESS_DEFINITION_KEY_2_DEPLOY_COUNT);
  }

  public void testQueryByInvalidProcessDefinitionName() {
    assertThat(runtimeService.createProcessInstanceQuery().processDefinitionName("invalid").singleResult()).isNull();
    assertThat(runtimeService.createProcessInstanceQuery().processDefinitionName("invalid").count()).isEqualTo(0);
  }

  public void testQueryByDeploymentId() {
    List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery().deploymentId(deployment.getId()).list();
    assertThat(instances).hasSize(PROCESS_DEPLOY_COUNT);
    ProcessInstance processInstance = instances.get(0);
    assertThat(processInstance.getDeploymentId()).isEqualTo(deployment.getId());
    assertThat(processInstance.getProcessDefinitionVersion()).isEqualTo(Integer.valueOf(1));
    assertThat(processInstance.getProcessDefinitionKey()).isEqualTo(PROCESS_DEFINITION_KEY);
    assertThat(processInstance.getProcessDefinitionName()).isEqualTo("oneTaskProcessName");
    assertThat(runtimeService.createProcessInstanceQuery().deploymentId(deployment.getId()).count()).isEqualTo(PROCESS_DEPLOY_COUNT);
  }

  public void testQueryByDeploymentIdIn() {
    List<String> deploymentIds = new ArrayList<String>();
    deploymentIds.add(deployment.getId());
    List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery().deploymentIdIn(deploymentIds).list();
    assertThat(instances).hasSize(PROCESS_DEPLOY_COUNT);

    ProcessInstance processInstance = instances.get(0);
    assertThat(processInstance.getDeploymentId()).isEqualTo(deployment.getId());
    assertThat(processInstance.getProcessDefinitionVersion()).isEqualTo(Integer.valueOf(1));
    assertThat(processInstance.getProcessDefinitionKey()).isEqualTo(PROCESS_DEFINITION_KEY);
    assertThat(processInstance.getProcessDefinitionName()).isEqualTo("oneTaskProcessName");

    assertThat(runtimeService.createProcessInstanceQuery().deploymentIdIn(deploymentIds).count()).isEqualTo(PROCESS_DEPLOY_COUNT);
  }

  public void testOrQueryByDeploymentId() {
    List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery().or().deploymentId(deployment.getId()).processDefinitionId("undefined").endOr().list();
    assertThat(instances).hasSize(PROCESS_DEPLOY_COUNT);
    ProcessInstance processInstance = instances.get(0);
    assertThat(processInstance.getDeploymentId()).isEqualTo(deployment.getId());
    assertThat(processInstance.getProcessDefinitionVersion()).isEqualTo(Integer.valueOf(1));
    assertThat(processInstance.getProcessDefinitionKey()).isEqualTo(PROCESS_DEFINITION_KEY);
    assertThat(processInstance.getProcessDefinitionName()).isEqualTo("oneTaskProcessName");

    instances = runtimeService.createProcessInstanceQuery()
        .or()
          .deploymentId(deployment.getId())
          .processDefinitionId("undefined")
        .endOr()
        .or()
          .processDefinitionKey(PROCESS_DEFINITION_KEY)
          .processDefinitionId("undefined")
        .endOr()
        .list();
    assertThat(instances).hasSize(4);

    instances = runtimeService.createProcessInstanceQuery()
        .or()
          .deploymentId(deployment.getId())
          .processDefinitionId("undefined")
        .endOr()
        .or()
          .processDefinitionKey("undefined")
          .processDefinitionId("undefined")
        .endOr()
        .list();
    assertThat(instances).hasSize(0);

    assertThat(runtimeService.createProcessInstanceQuery().or().deploymentId(deployment.getId()).processDefinitionId("undefined").endOr().count()).isEqualTo(PROCESS_DEPLOY_COUNT);

    assertThat(runtimeService.createProcessInstanceQuery()
        .or()
          .deploymentId(deployment.getId())
          .processDefinitionId("undefined")
        .endOr()
        .or()
          .processDefinitionKey(PROCESS_DEFINITION_KEY)
          .processDefinitionId("undefined")
        .endOr()
        .count()).isEqualTo(4);

    assertThat(runtimeService.createProcessInstanceQuery()
        .or()
          .deploymentId(deployment.getId())
          .processDefinitionId("undefined")
        .endOr()
        .or()
          .processDefinitionKey("undefined")
          .processDefinitionId("undefined")
        .endOr()
        .count()).isEqualTo(0);
  }

  public void testOrQueryByDeploymentIdIn() {
    List<String> deploymentIds = singletonList(deployment.getId());
    List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery().or().deploymentIdIn(deploymentIds).processDefinitionId("undefined").endOr().list();
    assertThat(instances).hasSize(PROCESS_DEPLOY_COUNT);

    ProcessInstance processInstance = instances.get(0);
    assertThat(processInstance.getDeploymentId()).isEqualTo(deployment.getId());
    assertThat(processInstance.getProcessDefinitionVersion()).isEqualTo(Integer.valueOf(1));
    assertThat(processInstance.getProcessDefinitionKey()).isEqualTo(PROCESS_DEFINITION_KEY);
    assertThat(processInstance.getProcessDefinitionName()).isEqualTo("oneTaskProcessName");

    assertThat(runtimeService.createProcessInstanceQuery().or().deploymentIdIn(deploymentIds).processDefinitionId("undefined").endOr().count()).isEqualTo(PROCESS_DEPLOY_COUNT);
  }

  public void testQueryByInvalidDeploymentId() {
    assertThat(runtimeService.createProcessInstanceQuery().deploymentId("invalid").singleResult()).isNull();
    assertThat(runtimeService.createProcessInstanceQuery().deploymentId("invalid").count()).isEqualTo(0);
  }

  public void testOrQueryByInvalidDeploymentId() {
    assertThat(runtimeService.createProcessInstanceQuery().or().deploymentId("invalid").processDefinitionId("undefined").endOr().singleResult()).isNull();
    assertThat(runtimeService.createProcessInstanceQuery().or().deploymentId("invalid").processDefinitionId("undefined").endOr().count()).isEqualTo(0);
  }

  public void testQueryByInvalidProcessInstanceId() {
    assertThat(runtimeService.createProcessInstanceQuery().processInstanceId("I do not exist").singleResult()).isNull();
    assertThat(runtimeService.createProcessInstanceQuery().processInstanceId("I do not exist").list()).hasSize(0);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/runtime/superProcess.bpmn20.xml", "org/activiti/engine/test/api/runtime/subProcess.bpmn20.xml" })
  public void testQueryBySuperProcessInstanceId() {
    ProcessInstance superProcessInstance = runtimeService.startProcessInstanceByKey("subProcessQueryTest");

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().superProcessInstanceId(superProcessInstance.getId());
    ProcessInstance subProcessInstance = query.singleResult();
    assertThat(subProcessInstance).isNotNull();
    assertThat(query.list()).hasSize(1);
    assertThat(query.count()).isEqualTo(1);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/runtime/superProcess.bpmn20.xml", "org/activiti/engine/test/api/runtime/subProcess.bpmn20.xml" })
  public void testQueryParentProcessInstanceIdResultMapping() {
    ProcessInstance superProcessInstance = runtimeService.startProcessInstanceByKey("subProcessQueryTest");

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().superProcessInstanceId(superProcessInstance.getId());
    ProcessInstance subProcessInstance = query.singleResult();
    assertThat(subProcessInstance).isNotNull();
    assertThat(query.list()).hasSize(1);
    assertThat(query.count()).isEqualTo(1);
    assertThat(subProcessInstance.getParentProcessInstanceId()).isEqualTo(superProcessInstance.getId());
  }

  @Deployment(resources = { "org/activiti/engine/test/api/runtime/superProcess.bpmn20.xml", "org/activiti/engine/test/api/runtime/subProcess.bpmn20.xml" })
  public void testQueryWithVariablesParentProcessInstanceIdResultMapping() {
    ProcessInstance superProcessInstance = runtimeService.startProcessInstanceByKey("subProcessQueryTest");

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery()
                                               .superProcessInstanceId(superProcessInstance.getId())
                                               .includeProcessVariables();

    ProcessInstance subProcessInstance = query.singleResult();
    assertThat(subProcessInstance).isNotNull();
    assertThat(query.list()).hasSize(1);
    assertThat(query.count()).isEqualTo(1);
    assertThat(subProcessInstance.getParentProcessInstanceId()).isEqualTo(superProcessInstance.getId());
  }

  @Deployment(resources = { "org/activiti/engine/test/api/runtime/superProcess.bpmn20.xml", "org/activiti/engine/test/api/runtime/subProcess.bpmn20.xml" })
  public void testOrQueryBySuperProcessInstanceId() {
    ProcessInstance superProcessInstance = runtimeService.startProcessInstanceByKey("subProcessQueryTest");

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().or().superProcessInstanceId(superProcessInstance.getId()).processDefinitionId("undefined").endOr();
    ProcessInstance subProcessInstance = query.singleResult();
    assertThat(subProcessInstance).isNotNull();
    assertThat(query.list()).hasSize(1);
    assertThat(query.count()).isEqualTo(1);
  }

  public void testQueryByInvalidSuperProcessInstanceId() {
    assertThat(runtimeService.createProcessInstanceQuery().superProcessInstanceId("invalid").singleResult()).isNull();
    assertThat(runtimeService.createProcessInstanceQuery().superProcessInstanceId("invalid").list()).hasSize(0);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/runtime/superProcess.bpmn20.xml", "org/activiti/engine/test/api/runtime/subProcess.bpmn20.xml" })
  public void testQueryBySubProcessInstanceId() {
    ProcessInstance superProcessInstance = runtimeService.startProcessInstanceByKey("subProcessQueryTest");

    ProcessInstance subProcessInstance = runtimeService.createProcessInstanceQuery().superProcessInstanceId(superProcessInstance.getId()).singleResult();
    assertThat(subProcessInstance).isNotNull();
    assertThat(runtimeService.createProcessInstanceQuery().subProcessInstanceId(subProcessInstance.getId()).singleResult().getId()).isEqualTo(superProcessInstance.getId());
  }

  @Deployment(resources = { "org/activiti/engine/test/api/runtime/superProcess.bpmn20.xml", "org/activiti/engine/test/api/runtime/subProcess.bpmn20.xml" })
  public void testOrQueryBySubProcessInstanceId() {
    ProcessInstance superProcessInstance = runtimeService.startProcessInstanceByKey("subProcessQueryTest");

    ProcessInstance subProcessInstance = runtimeService.createProcessInstanceQuery().or().superProcessInstanceId(superProcessInstance.getId()).processDefinitionId("undefined").singleResult();
    assertThat(subProcessInstance).isNotNull();
    assertThat(runtimeService.createProcessInstanceQuery().or().subProcessInstanceId(subProcessInstance.getId()).processDefinitionId("undefined").singleResult()
        .getId()).isEqualTo(superProcessInstance.getId());
  }

  public void testQueryByInvalidSubProcessInstanceId() {
    assertThat(runtimeService.createProcessInstanceQuery().subProcessInstanceId("invalid").singleResult()).isNull();
    assertThat(runtimeService.createProcessInstanceQuery().subProcessInstanceId("invalid").list()).hasSize(0);
  }

  // Nested subprocess make the query complexer, hence this test
  @Deployment(resources = { "org/activiti/engine/test/api/runtime/superProcessWithNestedSubProcess.bpmn20.xml", "org/activiti/engine/test/api/runtime/nestedSubProcess.bpmn20.xml",
      "org/activiti/engine/test/api/runtime/subProcess.bpmn20.xml" })
  public void testQueryBySuperProcessInstanceIdNested() {
    ProcessInstance superProcessInstance = runtimeService.startProcessInstanceByKey("nestedSubProcessQueryTest");

    ProcessInstance subProcessInstance = runtimeService.createProcessInstanceQuery().superProcessInstanceId(superProcessInstance.getId()).singleResult();
    assertThat(subProcessInstance).isNotNull();

    ProcessInstance nestedSubProcessInstance = runtimeService.createProcessInstanceQuery().superProcessInstanceId(subProcessInstance.getId()).singleResult();
    assertThat(nestedSubProcessInstance).isNotNull();
  }

  // Nested subprocess make the query complexer, hence this test
  @Deployment(resources = { "org/activiti/engine/test/api/runtime/superProcessWithNestedSubProcess.bpmn20.xml", "org/activiti/engine/test/api/runtime/nestedSubProcess.bpmn20.xml",
      "org/activiti/engine/test/api/runtime/subProcess.bpmn20.xml" })
  public void testQueryBySubProcessInstanceIdNested() {
    ProcessInstance superProcessInstance = runtimeService.startProcessInstanceByKey("nestedSubProcessQueryTest");

    ProcessInstance subProcessInstance = runtimeService.createProcessInstanceQuery().superProcessInstanceId(superProcessInstance.getId()).singleResult();
    assertThat(runtimeService.createProcessInstanceQuery().subProcessInstanceId(subProcessInstance.getId()).singleResult().getId()).isEqualTo(superProcessInstance.getId());

    ProcessInstance nestedSubProcessInstance = runtimeService.createProcessInstanceQuery().superProcessInstanceId(subProcessInstance.getId()).singleResult();
    assertThat(runtimeService.createProcessInstanceQuery().subProcessInstanceId(nestedSubProcessInstance.getId()).singleResult().getId()).isEqualTo(subProcessInstance.getId());
  }

  @Deployment(resources = { "org/activiti/engine/test/api/runtime/superProcessWithNestedSubProcess.bpmn20.xml", "org/activiti/engine/test/api/runtime/nestedSubProcess.bpmn20.xml",
      "org/activiti/engine/test/api/runtime/subProcess.bpmn20.xml" })
  public void testQueryWithExcludeSubprocesses() {
    ProcessInstance superProcessInstance = runtimeService.startProcessInstanceByKey("nestedSubProcessQueryTest");
    ProcessInstance subProcessInstance = runtimeService.createProcessInstanceQuery().superProcessInstanceId(superProcessInstance.getId()).singleResult();
    ProcessInstance nestedSubProcessInstance = runtimeService.createProcessInstanceQuery().superProcessInstanceId(subProcessInstance.getId()).singleResult();

    List<ProcessInstance> instanceList = runtimeService.createProcessInstanceQuery().excludeSubprocesses(true).list();
    assertThat(instanceList).hasSize(6);

    boolean superProcessFound = false;
    boolean subProcessFound = false;
    boolean nestedSubProcessFound = false;
    for (ProcessInstance processInstance : instanceList) {
      if (processInstance.getId().equals(superProcessInstance.getId())) {
        superProcessFound = true;
      } else if (processInstance.getId().equals(subProcessInstance.getId())) {
        subProcessFound = true;
      } else if (processInstance.getId().equals(nestedSubProcessInstance.getId())) {
        nestedSubProcessFound = true;
      }
    }
    assertThat(superProcessFound).isTrue();
    assertThat(subProcessFound).isFalse();
    assertThat(nestedSubProcessFound).isFalse();

    instanceList = runtimeService.createProcessInstanceQuery().excludeSubprocesses(false).list();
    assertThat(instanceList).hasSize(8);
  }

  public void testQueryPaging() {
    assertThat(runtimeService.createProcessInstanceQuery().processDefinitionKey(PROCESS_DEFINITION_KEY).count()).isEqualTo(PROCESS_DEFINITION_KEY_DEPLOY_COUNT);
    assertThat(runtimeService.createProcessInstanceQuery().processDefinitionKey(PROCESS_DEFINITION_KEY).listPage(0, 2)).hasSize(2);
    assertThat(runtimeService.createProcessInstanceQuery().processDefinitionKey(PROCESS_DEFINITION_KEY).listPage(1, 3)).hasSize(3);
  }

  public void testQuerySorting() {
    assertThat(runtimeService.createProcessInstanceQuery().orderByProcessInstanceId().asc().list()).hasSize(PROCESS_DEPLOY_COUNT);
    assertThat(runtimeService.createProcessInstanceQuery().orderByProcessDefinitionId().asc().list()).hasSize(PROCESS_DEPLOY_COUNT);
    assertThat(runtimeService.createProcessInstanceQuery().orderByProcessDefinitionKey().asc().list()).hasSize(PROCESS_DEPLOY_COUNT);

    assertThat(runtimeService.createProcessInstanceQuery().orderByProcessInstanceId().desc().list()).hasSize(PROCESS_DEPLOY_COUNT);
    assertThat(runtimeService.createProcessInstanceQuery().orderByProcessDefinitionId().desc().list()).hasSize(PROCESS_DEPLOY_COUNT);
    assertThat(runtimeService.createProcessInstanceQuery().orderByProcessDefinitionKey().desc().list()).hasSize(PROCESS_DEPLOY_COUNT);

    assertThat(runtimeService.createProcessInstanceQuery().processDefinitionKey(PROCESS_DEFINITION_KEY).orderByProcessInstanceId().asc().list()).hasSize(PROCESS_DEFINITION_KEY_DEPLOY_COUNT);
    assertThat(runtimeService.createProcessInstanceQuery().processDefinitionKey(PROCESS_DEFINITION_KEY).orderByProcessInstanceId().desc().list()).hasSize(PROCESS_DEFINITION_KEY_DEPLOY_COUNT);
  }

  public void testQueryInvalidSorting() {
    // asc - desc not called -> exception
    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> runtimeService.createProcessInstanceQuery().orderByProcessDefinitionId().list());
  }

  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testQueryStringVariable() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("stringVar", "abcdef");
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<String, Object>();
    vars.put("stringVar", "abcdef");
    vars.put("stringVar2", "ghijkl");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<String, Object>();
    vars.put("stringVar", "azerty");
    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    // Test EQUAL on single string variable, should result in 2 matches
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().variableValueEquals("stringVar", "abcdef");
    List<ProcessInstance> processInstances = query.list();
    assertThat(processInstances).isNotNull();
    assertThat(processInstances).hasSize(2);

    // Test EQUAL on two string variables, should result in single match
    query = runtimeService.createProcessInstanceQuery().variableValueEquals("stringVar", "abcdef").variableValueEquals("stringVar2", "ghijkl");
    ProcessInstance resultInstance = query.singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance2.getId());

    // Test NOT_EQUAL, should return only 1 resultInstance
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueNotEquals("stringVar", "abcdef").singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance3.getId());

    // Test GREATER_THAN, should return only matching 'azerty'
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThan("stringVar", "abcdef").singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance3.getId());

    resultInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThan("stringVar", "z").singleResult();
    assertThat(resultInstance).isNull();

    // Test GREATER_THAN_OR_EQUAL, should return 3 results
    assertThat(runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("stringVar", "abcdef").count()).isEqualTo(3);
    assertThat(runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("stringVar", "z").count()).isEqualTo(0);

    // Test LESS_THAN, should return 2 results
    processInstances = runtimeService.createProcessInstanceQuery().variableValueLessThan("stringVar", "abcdeg").list();
    assertThat(processInstances).hasSize(2);
    List<String> expecedIds = asList(processInstance1.getId(), processInstance2.getId());
    List<String> ids = new ArrayList(asList(processInstances.get(0).getId(), processInstances.get(1).getId()));
    ids.removeAll(expecedIds);
    assertThat(ids.isEmpty()).isTrue();

    assertThat(runtimeService.createProcessInstanceQuery().variableValueLessThan("stringVar", "abcdef").count()).isEqualTo(0);
    assertThat(runtimeService.createProcessInstanceQuery().variableValueLessThanOrEqual("stringVar", "z").count()).isEqualTo(3);

    // Test LESS_THAN_OR_EQUAL
    processInstances = runtimeService.createProcessInstanceQuery().variableValueLessThanOrEqual("stringVar", "abcdef").list();
    assertThat(processInstances).hasSize(2);
    expecedIds = asList(processInstance1.getId(), processInstance2.getId());
    ids = new ArrayList(asList(processInstances.get(0).getId(), processInstances.get(1).getId()));
    ids.removeAll(expecedIds);
    assertThat(ids.isEmpty()).isTrue();

    assertThat(runtimeService.createProcessInstanceQuery().variableValueLessThanOrEqual("stringVar", "z").count()).isEqualTo(3);
    assertThat(runtimeService.createProcessInstanceQuery().variableValueLessThanOrEqual("stringVar", "aa").count()).isEqualTo(0);

    // Test LIKE
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueLike("stringVar", "azert%").singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance3.getId());

    resultInstance = runtimeService.createProcessInstanceQuery().variableValueLike("stringVar", "%y").singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance3.getId());

    resultInstance = runtimeService.createProcessInstanceQuery().variableValueLike("stringVar", "%zer%").singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance3.getId());

    assertThat(runtimeService.createProcessInstanceQuery().variableValueLike("stringVar", "a%").count()).isEqualTo(3);
    assertThat(runtimeService.createProcessInstanceQuery().variableValueLike("stringVar", "%x%").count()).isEqualTo(0);

    // Test value-only matching
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueEquals("azerty").singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance3.getId());

    processInstances = runtimeService.createProcessInstanceQuery().variableValueEquals("abcdef").list();
    assertThat(processInstances).hasSize(2);
    expecedIds = asList(processInstance1.getId(), processInstance2.getId());
    ids = new ArrayList(asList(processInstances.get(0).getId(), processInstances.get(1).getId()));
    ids.removeAll(expecedIds);
    assertThat(ids.isEmpty()).isTrue();

    resultInstance = runtimeService.createProcessInstanceQuery().variableValueEquals("notmatchinganyvalues").singleResult();
    assertThat(resultInstance).isNull();

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance3.getId(), "test");
  }

  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testQueryLongVariable() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("longVar", 12345L);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<String, Object>();
    vars.put("longVar", 12345L);
    vars.put("longVar2", 67890L);
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<String, Object>();
    vars.put("longVar", 55555L);
    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    // Query on single long variable, should result in 2 matches
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().variableValueEquals("longVar", 12345L);
    List<ProcessInstance> processInstances = query.list();
    assertThat(processInstances).isNotNull();
    assertThat(processInstances).hasSize(2);

    // Query on two long variables, should result in single match
    query = runtimeService.createProcessInstanceQuery().variableValueEquals("longVar", 12345L).variableValueEquals("longVar2", 67890L);
    ProcessInstance resultInstance = query.singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance2.getId());

    // Query with unexisting variable value
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueEquals("longVar", 999L).singleResult();
    assertThat(resultInstance).isNull();

    // Test NOT_EQUALS
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueNotEquals("longVar", 12345L).singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance3.getId());

    // Test GREATER_THAN
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThan("longVar", 44444L).singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance3.getId());

    assertThat(runtimeService.createProcessInstanceQuery().variableValueGreaterThan("longVar", 55555L).count()).isEqualTo(0);
    assertThat(runtimeService.createProcessInstanceQuery().variableValueGreaterThan("longVar", 1L).count()).isEqualTo(3);

    // Test GREATER_THAN_OR_EQUAL
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("longVar", 44444L).singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance3.getId());

    resultInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("longVar", 55555L).singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance3.getId());

    assertThat(runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("longVar", 1L).count()).isEqualTo(3);

    // Test LESS_THAN
    processInstances = runtimeService.createProcessInstanceQuery().variableValueLessThan("longVar", 55555L).list();
    assertThat(processInstances).hasSize(2);

    List<String> expectedIds = asList(processInstance1.getId(), processInstance2.getId());
    List<String> ids = new ArrayList(asList(processInstances.get(0).getId(), processInstances.get(1).getId()));
    ids.removeAll(expectedIds);
    assertThat(ids.isEmpty()).isTrue();

    assertThat(runtimeService.createProcessInstanceQuery().variableValueLessThan("longVar", 12345L).count()).isEqualTo(0);
    assertThat(runtimeService.createProcessInstanceQuery().variableValueLessThan("longVar", 66666L).count()).isEqualTo(3);

    // Test LESS_THAN_OR_EQUAL
    processInstances = runtimeService.createProcessInstanceQuery().variableValueLessThanOrEqual("longVar", 55555L).list();
    assertThat(processInstances).hasSize(3);

    assertThat(runtimeService.createProcessInstanceQuery().variableValueLessThanOrEqual("longVar", 12344L).count()).isEqualTo(0);

    // Test value-only matching
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueEquals(55555L).singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance3.getId());

    processInstances = runtimeService.createProcessInstanceQuery().variableValueEquals(12345L).list();
    assertThat(processInstances).hasSize(2);
    expectedIds = asList(processInstance1.getId(), processInstance2.getId());
    ids = new ArrayList(asList(processInstances.get(0).getId(), processInstances.get(1).getId()));
    ids.removeAll(expectedIds);
    assertThat(ids.isEmpty()).isTrue();

    resultInstance = runtimeService.createProcessInstanceQuery().variableValueEquals(999L).singleResult();
    assertThat(resultInstance).isNull();

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance3.getId(), "test");
  }

  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testQueryDoubleVariable() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("doubleVar", 12345.6789);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<String, Object>();
    vars.put("doubleVar", 12345.6789);
    vars.put("doubleVar2", 9876.54321);
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<String, Object>();
    vars.put("doubleVar", 55555.5555);
    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    // Query on single double variable, should result in 2 matches
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().variableValueEquals("doubleVar", 12345.6789);
    List<ProcessInstance> processInstances = query.list();
    assertThat(processInstances).isNotNull();
    assertThat(processInstances).hasSize(2);

    // Query on two double variables, should result in single value
    query = runtimeService.createProcessInstanceQuery().variableValueEquals("doubleVar", 12345.6789).variableValueEquals("doubleVar2", 9876.54321);
    ProcessInstance resultInstance = query.singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance2.getId());

    // Query with unexisting variable value
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueEquals("doubleVar", 9999.99).singleResult();
    assertThat(resultInstance).isNull();

    // Test NOT_EQUALS
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueNotEquals("doubleVar", 12345.6789).singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance3.getId());

    // Test GREATER_THAN
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThan("doubleVar", 44444.4444).singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance3.getId());

    assertThat(runtimeService.createProcessInstanceQuery().variableValueGreaterThan("doubleVar", 55555.5555).count()).isEqualTo(0);
    assertThat(runtimeService.createProcessInstanceQuery().variableValueGreaterThan("doubleVar", 1.234).count()).isEqualTo(3);

    // Test GREATER_THAN_OR_EQUAL
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("doubleVar", 44444.4444).singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance3.getId());

    resultInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("doubleVar", 55555.5555).singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance3.getId());

    assertThat(runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("doubleVar", 1.234).count()).isEqualTo(3);

    // Test LESS_THAN
    processInstances = runtimeService.createProcessInstanceQuery().variableValueLessThan("doubleVar", 55555.5555).list();
    assertThat(processInstances).hasSize(2);

    List<String> expectedIds = asList(processInstance1.getId(), processInstance2.getId());
    List<String> ids = new ArrayList(asList(processInstances.get(0).getId(), processInstances.get(1).getId()));
    ids.removeAll(expectedIds);
    assertThat(ids.isEmpty()).isTrue();

    assertThat(runtimeService.createProcessInstanceQuery().variableValueLessThan("doubleVar", 12345.6789).count()).isEqualTo(0);
    assertThat(runtimeService.createProcessInstanceQuery().variableValueLessThan("doubleVar", 66666.6666).count()).isEqualTo(3);

    // Test LESS_THAN_OR_EQUAL
    processInstances = runtimeService.createProcessInstanceQuery().variableValueLessThanOrEqual("doubleVar", 55555.5555).list();
    assertThat(processInstances).hasSize(3);

    assertThat(runtimeService.createProcessInstanceQuery().variableValueLessThanOrEqual("doubleVar", 12344.6789).count()).isEqualTo(0);

    // Test value-only matching
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueEquals(55555.5555).singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance3.getId());

    processInstances = runtimeService.createProcessInstanceQuery().variableValueEquals(12345.6789).list();
    assertThat(processInstances).hasSize(2);
    expectedIds = asList(processInstance1.getId(), processInstance2.getId());
    ids = new ArrayList(asList(processInstances.get(0).getId(), processInstances.get(1).getId()));
    ids.removeAll(expectedIds);
    assertThat(ids.isEmpty()).isTrue();

    resultInstance = runtimeService.createProcessInstanceQuery().variableValueEquals(999.999).singleResult();
    assertThat(resultInstance).isNull();

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance3.getId(), "test");
  }

  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testQueryIntegerVariable() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("integerVar", 12345);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<String, Object>();
    vars.put("integerVar", 12345);
    vars.put("integerVar2", 67890);
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<String, Object>();
    vars.put("integerVar", 55555);
    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    // Query on single integer variable, should result in 2 matches
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().variableValueEquals("integerVar", 12345);
    List<ProcessInstance> processInstances = query.list();
    assertThat(processInstances).isNotNull();
    assertThat(processInstances).hasSize(2);

    // Query on two integer variables, should result in single value
    query = runtimeService.createProcessInstanceQuery().variableValueEquals("integerVar", 12345).variableValueEquals("integerVar2", 67890);
    ProcessInstance resultInstance = query.singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance2.getId());

    // Query with unexisting variable value
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueEquals("integerVar", 9999).singleResult();
    assertThat(resultInstance).isNull();

    // Test NOT_EQUALS
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueNotEquals("integerVar", 12345).singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance3.getId());

    // Test GREATER_THAN
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThan("integerVar", 44444).singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance3.getId());

    assertThat(runtimeService.createProcessInstanceQuery().variableValueGreaterThan("integerVar", 55555).count()).isEqualTo(0);
    assertThat(runtimeService.createProcessInstanceQuery().variableValueGreaterThan("integerVar", 1).count()).isEqualTo(3);

    // Test GREATER_THAN_OR_EQUAL
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("integerVar", 44444).singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance3.getId());

    resultInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("integerVar", 55555).singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance3.getId());

    assertThat(runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("integerVar", 1).count()).isEqualTo(3);

    // Test LESS_THAN
    processInstances = runtimeService.createProcessInstanceQuery().variableValueLessThan("integerVar", 55555).list();
    assertThat(processInstances).hasSize(2);

    List<String> expectedIds = asList(processInstance1.getId(), processInstance2.getId());
    List<String> ids = new ArrayList(asList(processInstances.get(0).getId(), processInstances.get(1).getId()));
    ids.removeAll(expectedIds);
    assertThat(ids.isEmpty()).isTrue();

    assertThat(runtimeService.createProcessInstanceQuery().variableValueLessThan("integerVar", 12345).count()).isEqualTo(0);
    assertThat(runtimeService.createProcessInstanceQuery().variableValueLessThan("integerVar", 66666).count()).isEqualTo(3);

    // Test LESS_THAN_OR_EQUAL
    processInstances = runtimeService.createProcessInstanceQuery().variableValueLessThanOrEqual("integerVar", 55555).list();
    assertThat(processInstances).hasSize(3);

    assertThat(runtimeService.createProcessInstanceQuery().variableValueLessThanOrEqual("integerVar", 12344).count()).isEqualTo(0);

    // Test value-only matching
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueEquals(55555).singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance3.getId());

    processInstances = runtimeService.createProcessInstanceQuery().variableValueEquals(12345).list();
    assertThat(processInstances).hasSize(2);
    expectedIds = asList(processInstance1.getId(), processInstance2.getId());
    ids = new ArrayList<String>(asList(processInstances.get(0).getId(), processInstances.get(1).getId()));
    ids.removeAll(expectedIds);
    assertThat(ids.isEmpty()).isTrue();

    resultInstance = runtimeService.createProcessInstanceQuery().variableValueEquals(9999).singleResult();
    assertThat(resultInstance).isNull();

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance3.getId(), "test");
  }

  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testOrQueryIntegerVariable() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("integerVar", 12345);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<String, Object>();
    vars.put("integerVar", 12345);
    vars.put("integerVar2", 67890);
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<String, Object>();
    vars.put("integerVar", 55555);
    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    // Query on single integer variable, should result in 2 matches
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().or().variableValueEquals("integerVar", 12345).processDefinitionId("undefined").endOr();
    List<ProcessInstance> processInstances = query.list();
    assertThat(processInstances).isNotNull();
    assertThat(processInstances).hasSize(2);

    query = runtimeService.createProcessInstanceQuery()
        .or()
          .variableValueEquals("integerVar", 12345)
          .processDefinitionId("undefined")
        .endOr()
        .or()
          .processDefinitionKey("oneTaskProcess")
          .processDefinitionId("undefined")
        .endOr();
    processInstances = query.list();
    assertThat(processInstances).isNotNull();
    assertThat(processInstances).hasSize(2);

    // Query on two integer variables, should result in single value
    query = runtimeService.createProcessInstanceQuery().variableValueEquals("integerVar", 12345).or().variableValueEquals("integerVar2", 67890).processDefinitionId("undefined").endOr();
    ProcessInstance resultInstance = query.singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance2.getId());

    // Query with unexisting variable value
    resultInstance = runtimeService.createProcessInstanceQuery().or().variableValueEquals("integerVar", 9999).processDefinitionId("undefined").endOr().singleResult();
    assertThat(resultInstance).isNull();

    // Test NOT_EQUALS
    resultInstance = runtimeService.createProcessInstanceQuery().or().variableValueNotEquals("integerVar", 12345).processDefinitionId("undefined").endOr().singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance3.getId());

    // Test GREATER_THAN
    resultInstance = runtimeService.createProcessInstanceQuery().or().variableValueGreaterThan("integerVar", 44444).processDefinitionId("undefined").endOr().singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance3.getId());

    resultInstance = runtimeService.createProcessInstanceQuery()
        .or()
          .variableValueGreaterThan("integerVar", 44444)
          .processDefinitionId("undefined")
        .endOr()
        .or()
          .processDefinitionKey("oneTaskProcess")
          .processDefinitionId("undefined")
        .endOr()
        .singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance3.getId());

    assertThat(runtimeService.createProcessInstanceQuery().or().variableValueGreaterThan("integerVar", 55555).processDefinitionId("undefined").endOr().count()).isEqualTo(0);
    assertThat(runtimeService.createProcessInstanceQuery().or().variableValueGreaterThan("integerVar", 1).processDefinitionId("undefined").endOr().count()).isEqualTo(3);

    // Test GREATER_THAN_OR_EQUAL
    resultInstance = runtimeService.createProcessInstanceQuery().or().variableValueGreaterThanOrEqual("integerVar", 44444).processDefinitionId("undefined").endOr().singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance3.getId());

    resultInstance = runtimeService.createProcessInstanceQuery().or().variableValueGreaterThanOrEqual("integerVar", 55555).processDefinitionId("undefined").endOr().singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance3.getId());

    assertThat(runtimeService.createProcessInstanceQuery().or().variableValueGreaterThanOrEqual("integerVar", 1).processDefinitionId("undefined").endOr().count()).isEqualTo(3);

    // Test LESS_THAN
    processInstances = runtimeService.createProcessInstanceQuery().or().variableValueLessThan("integerVar", 55555).processDefinitionId("undefined").endOr().list();
    assertThat(processInstances).hasSize(2);

    List<String> expectedIds = asList(processInstance1.getId(), processInstance2.getId());
    List<String> ids = new ArrayList<String>(asList(processInstances.get(0).getId(), processInstances.get(1).getId()));
    ids.removeAll(expectedIds);
    assertThat(ids.isEmpty()).isTrue();

    assertThat(runtimeService.createProcessInstanceQuery().or().variableValueLessThan("integerVar", 12345).processDefinitionId("undefined").endOr().count()).isEqualTo(0);
    assertThat(runtimeService.createProcessInstanceQuery().or().variableValueLessThan("integerVar", 66666).processDefinitionId("undefined").endOr().count()).isEqualTo(3);

    // Test LESS_THAN_OR_EQUAL
    processInstances = runtimeService.createProcessInstanceQuery().or().variableValueLessThanOrEqual("integerVar", 55555).processDefinitionId("undefined").endOr().list();
    assertThat(processInstances).hasSize(3);

    assertThat(runtimeService.createProcessInstanceQuery().or().variableValueLessThanOrEqual("integerVar", 12344).processDefinitionId("undefined").endOr().count()).isEqualTo(0);

    // Test value-only matching
    resultInstance = runtimeService.createProcessInstanceQuery().or().variableValueEquals(55555).processDefinitionId("undefined").endOr().singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance3.getId());

    processInstances = runtimeService.createProcessInstanceQuery().or().variableValueEquals(12345).processDefinitionId("undefined").endOr().list();
    assertThat(processInstances).hasSize(2);
    expectedIds = asList(processInstance1.getId(), processInstance2.getId());
    ids = new ArrayList<String>(asList(processInstances.get(0).getId(), processInstances.get(1).getId()));
    ids.removeAll(expectedIds);
    assertThat(ids.isEmpty()).isTrue();

    resultInstance = runtimeService.createProcessInstanceQuery().or().variableValueEquals(9999).processDefinitionId("undefined").endOr().singleResult();
    assertThat(resultInstance).isNull();

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance3.getId(), "test");
  }

  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testQueryShortVariable() {
    Map<String, Object> vars = new HashMap<String, Object>();
    short shortVar = 1234;
    vars.put("shortVar", shortVar);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    short shortVar2 = 6789;
    vars = new HashMap<String, Object>();
    vars.put("shortVar", shortVar);
    vars.put("shortVar2", shortVar2);
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<String, Object>();
    vars.put("shortVar", (short) 5555);
    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    // Query on single short variable, should result in 2 matches
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().variableValueEquals("shortVar", shortVar);
    List<ProcessInstance> processInstances = query.list();
    assertThat(processInstances).isNotNull();
    assertThat(processInstances).hasSize(2);

    // Query on two short variables, should result in single value
    query = runtimeService.createProcessInstanceQuery().variableValueEquals("shortVar", shortVar).variableValueEquals("shortVar2", shortVar2);
    ProcessInstance resultInstance = query.singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance2.getId());

    // Query with unexisting variable value
    short unexistingValue = (short) 9999;
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueEquals("shortVar", unexistingValue).singleResult();
    assertThat(resultInstance).isNull();

    // Test NOT_EQUALS
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueNotEquals("shortVar", (short) 1234).singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance3.getId());

    // Test GREATER_THAN
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThan("shortVar", (short) 4444).singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance3.getId());

    assertThat(runtimeService.createProcessInstanceQuery().variableValueGreaterThan("shortVar", (short) 5555).count()).isEqualTo(0);
    assertThat(runtimeService.createProcessInstanceQuery().variableValueGreaterThan("shortVar", (short) 1).count()).isEqualTo(3);

    // Test GREATER_THAN_OR_EQUAL
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("shortVar", (short) 4444).singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance3.getId());

    resultInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("shortVar", (short) 5555).singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance3.getId());

    assertThat(runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("shortVar", (short) 1).count()).isEqualTo(3);

    // Test LESS_THAN
    processInstances = runtimeService.createProcessInstanceQuery().variableValueLessThan("shortVar", (short) 5555).list();
    assertThat(processInstances).hasSize(2);

    List<String> expectedIds = asList(processInstance1.getId(), processInstance2.getId());
    List<String> ids = new ArrayList<String>(asList(processInstances.get(0).getId(), processInstances.get(1).getId()));
    ids.removeAll(expectedIds);
    assertThat(ids.isEmpty()).isTrue();

    assertThat(runtimeService.createProcessInstanceQuery().variableValueLessThan("shortVar", (short) 1234).count()).isEqualTo(0);
    assertThat(runtimeService.createProcessInstanceQuery().variableValueLessThan("shortVar", (short) 6666).count()).isEqualTo(3);

    // Test LESS_THAN_OR_EQUAL
    processInstances = runtimeService.createProcessInstanceQuery().variableValueLessThanOrEqual("shortVar", (short) 5555).list();
    assertThat(processInstances).hasSize(3);

    assertThat(runtimeService.createProcessInstanceQuery().variableValueLessThanOrEqual("shortVar", (short) 1233).count()).isEqualTo(0);

    // Test value-only matching
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueEquals((short) 5555).singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance3.getId());

    processInstances = runtimeService.createProcessInstanceQuery().variableValueEquals((short) 1234).list();
    assertThat(processInstances).hasSize(2);
    expectedIds = asList(processInstance1.getId(), processInstance2.getId());
    ids = new ArrayList<String>(asList(processInstances.get(0).getId(), processInstances.get(1).getId()));
    ids.removeAll(expectedIds);
    assertThat(ids.isEmpty()).isTrue();

    resultInstance = runtimeService.createProcessInstanceQuery().variableValueEquals((short) 999).singleResult();
    assertThat(resultInstance).isNull();

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance3.getId(), "test");
  }

  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testQueryDateVariable() throws Exception {
    Map<String, Object> vars = new HashMap<String, Object>();
    Date date1 = Calendar.getInstance().getTime();
    vars.put("dateVar", date1);

    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    Calendar cal2 = Calendar.getInstance();
    cal2.add(Calendar.SECOND, 1);

    Date date2 = cal2.getTime();
    vars = new HashMap<String, Object>();
    vars.put("dateVar", date1);
    vars.put("dateVar2", date2);
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    Calendar nextYear = Calendar.getInstance();
    nextYear.add(Calendar.YEAR, 1);
    vars = new HashMap<String, Object>();
    vars.put("dateVar", nextYear.getTime());
    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    Calendar nextMonth = Calendar.getInstance();
    nextMonth.add(Calendar.MONTH, 1);

    Calendar twoYearsLater = Calendar.getInstance();
    twoYearsLater.add(Calendar.YEAR, 2);

    Calendar oneYearAgo = Calendar.getInstance();
    oneYearAgo.add(Calendar.YEAR, -1);

    // Query on single short variable, should result in 2 matches
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().variableValueEquals("dateVar", date1);
    List<ProcessInstance> processInstances = query.list();
    assertThat(processInstances).isNotNull();
    assertThat(processInstances).hasSize(2);

    // Query on two short variables, should result in single value
    query = runtimeService.createProcessInstanceQuery().variableValueEquals("dateVar", date1).variableValueEquals("dateVar2", date2);
    ProcessInstance resultInstance = query.singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance2.getId());

    // Query with unexisting variable value
    Date unexistingDate = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").parse("01/01/1989 12:00:00");
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueEquals("dateVar", unexistingDate).singleResult();
    assertThat(resultInstance).isNull();

    // Test NOT_EQUALS
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueNotEquals("dateVar", date1).singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance3.getId());

    // Test GREATER_THAN
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThan("dateVar", nextMonth.getTime()).singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance3.getId());

    assertThat(runtimeService.createProcessInstanceQuery().variableValueGreaterThan("dateVar", nextYear.getTime()).count()).isEqualTo(0);
    assertThat(runtimeService.createProcessInstanceQuery().variableValueGreaterThan("dateVar", oneYearAgo.getTime()).count()).isEqualTo(3);

    // Test GREATER_THAN_OR_EQUAL
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("dateVar", nextMonth.getTime()).singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance3.getId());

    resultInstance = runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("dateVar", nextYear.getTime()).singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance3.getId());

    assertThat(runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("dateVar", oneYearAgo.getTime()).count()).isEqualTo(3);

    // Test LESS_THAN
    processInstances = runtimeService.createProcessInstanceQuery().variableValueLessThan("dateVar", nextYear.getTime()).list();
    assertThat(processInstances).hasSize(2);

    List<String> expectedIds = asList(processInstance1.getId(), processInstance2.getId());
    List<String> ids = new ArrayList<String>(asList(processInstances.get(0).getId(), processInstances.get(1).getId()));
    ids.removeAll(expectedIds);
    assertThat(ids.isEmpty()).isTrue();

    assertThat(runtimeService.createProcessInstanceQuery().variableValueLessThan("dateVar", date1).count()).isEqualTo(0);
    assertThat(runtimeService.createProcessInstanceQuery().variableValueLessThan("dateVar", twoYearsLater.getTime()).count()).isEqualTo(3);

    // Test LESS_THAN_OR_EQUAL
    processInstances = runtimeService.createProcessInstanceQuery().variableValueLessThanOrEqual("dateVar", nextYear.getTime()).list();
    assertThat(processInstances).hasSize(3);

    assertThat(runtimeService.createProcessInstanceQuery().variableValueLessThanOrEqual("dateVar", oneYearAgo.getTime()).count()).isEqualTo(0);

    // Test value-only matching
    resultInstance = runtimeService.createProcessInstanceQuery().variableValueEquals(nextYear.getTime()).singleResult();
    assertThat(resultInstance).isNotNull();
    assertThat(resultInstance.getId()).isEqualTo(processInstance3.getId());

    processInstances = runtimeService.createProcessInstanceQuery().variableValueEquals(date1).list();
    assertThat(processInstances).hasSize(2);
    expectedIds = asList(processInstance1.getId(), processInstance2.getId());
    ids = new ArrayList<String>(asList(processInstances.get(0).getId(), processInstances.get(1).getId()));
    ids.removeAll(expectedIds);
    assertThat(ids.isEmpty()).isTrue();

    resultInstance = runtimeService.createProcessInstanceQuery().variableValueEquals(twoYearsLater.getTime()).singleResult();
    assertThat(resultInstance).isNull();

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance3.getId(), "test");
  }

  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testBooleanVariable() throws Exception {

    // TEST EQUALS
    HashMap<String, Object> vars = new HashMap<String, Object>();
    vars.put("booleanVar", true);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<String, Object>();
    vars.put("booleanVar", false);
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery().variableValueEquals("booleanVar", true).list();

    assertThat(instances).isNotNull();
    assertThat(instances).hasSize(1);
    assertThat(instances.get(0).getId()).isEqualTo(processInstance1.getId());

    instances = runtimeService.createProcessInstanceQuery().variableValueEquals("booleanVar", false).list();

    assertThat(instances).isNotNull();
    assertThat(instances).hasSize(1);
    assertThat(instances.get(0).getId()).isEqualTo(processInstance2.getId());

    // TEST NOT_EQUALS
    instances = runtimeService.createProcessInstanceQuery().variableValueNotEquals("booleanVar", true).list();

    assertThat(instances).isNotNull();
    assertThat(instances).hasSize(1);
    assertThat(instances.get(0).getId()).isEqualTo(processInstance2.getId());

    instances = runtimeService.createProcessInstanceQuery().variableValueNotEquals("booleanVar", false).list();

    assertThat(instances).isNotNull();
    assertThat(instances).hasSize(1);
    assertThat(instances.get(0).getId()).isEqualTo(processInstance1.getId());

    // Test value-only matching
    instances = runtimeService.createProcessInstanceQuery().variableValueEquals(true).list();
    assertThat(instances).isNotNull();
    assertThat(instances).hasSize(1);
    assertThat(instances.get(0).getId()).isEqualTo(processInstance1.getId());

    // Test unsupported operations
    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> runtimeService.createProcessInstanceQuery().variableValueGreaterThan("booleanVar", true))
      .withMessageContaining("Booleans and null cannot be used in 'greater than' condition");

    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("booleanVar", true))
      .withMessageContaining("Booleans and null cannot be used in 'greater than or equal' condition");

    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> runtimeService.createProcessInstanceQuery().variableValueLessThan("booleanVar", true))
      .withMessageContaining("Booleans and null cannot be used in 'less than' condition");

    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> runtimeService.createProcessInstanceQuery().variableValueLessThanOrEqual("booleanVar", true))
      .withMessageContaining("Booleans and null cannot be used in 'less than or equal' condition");

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");

    // Test value-only matching, no results present
    instances = runtimeService.createProcessInstanceQuery().variableValueEquals(true).list();
    assertThat(instances).isNotNull();
    assertThat(instances).hasSize(0);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testQueryVariablesUpdatedToNullValue() {
    // Start process instance with different types of variables
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("longVar", 928374L);
    variables.put("shortVar", (short) 123);
    variables.put("integerVar", 1234);
    variables.put("stringVar", "coca-cola");
    variables.put("dateVar", new Date());
    variables.put("booleanVar", true);
    variables.put("nullVar", null);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().variableValueEquals("longVar", null).variableValueEquals("shortVar", null).variableValueEquals("integerVar", null)
        .variableValueEquals("stringVar", null).variableValueEquals("booleanVar", null).variableValueEquals("dateVar", null);

    ProcessInstanceQuery notQuery = runtimeService.createProcessInstanceQuery().variableValueNotEquals("longVar", null).variableValueNotEquals("shortVar", null)
        .variableValueNotEquals("integerVar", null).variableValueNotEquals("stringVar", null).variableValueNotEquals("booleanVar", null).variableValueNotEquals("dateVar", null);

    assertThat(query.singleResult()).isNull();
    assertThat(notQuery.singleResult()).isNotNull();

    // Set all existing variables values to null
    runtimeService.setVariable(processInstance.getId(), "longVar", null);
    runtimeService.setVariable(processInstance.getId(), "shortVar", null);
    runtimeService.setVariable(processInstance.getId(), "integerVar", null);
    runtimeService.setVariable(processInstance.getId(), "stringVar", null);
    runtimeService.setVariable(processInstance.getId(), "dateVar", null);
    runtimeService.setVariable(processInstance.getId(), "nullVar", null);
    runtimeService.setVariable(processInstance.getId(), "booleanVar", null);

    Execution queryResult = query.singleResult();
    assertThat(queryResult).isNotNull();
    assertThat(queryResult.getId()).isEqualTo(processInstance.getId());
    assertThat(notQuery.singleResult()).isNull();
  }

  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testQueryNullVariable() throws Exception {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("nullVar", null);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<String, Object>();
    vars.put("nullVar", "notnull");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<String, Object>();
    vars.put("nullVarLong", "notnull");
    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<String, Object>();
    vars.put("nullVarDouble", "notnull");
    ProcessInstance processInstance4 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    vars = new HashMap<String, Object>();
    vars.put("nullVarByte", "testbytes".getBytes());
    ProcessInstance processInstance5 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    // Query on null value, should return one value
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().variableValueEquals("nullVar", null);
    List<ProcessInstance> processInstances = query.list();
    assertThat(processInstances).isNotNull();
    assertThat(processInstances).hasSize(1);
    assertThat(processInstances.get(0).getId()).isEqualTo(processInstance1.getId());

    // Test NOT_EQUALS null
    assertThat(runtimeService.createProcessInstanceQuery().variableValueNotEquals("nullVar", null).count()).isEqualTo(1);
    assertThat(runtimeService.createProcessInstanceQuery().variableValueNotEquals("nullVarLong", null).count()).isEqualTo(1);
    assertThat(runtimeService.createProcessInstanceQuery().variableValueNotEquals("nullVarDouble", null).count()).isEqualTo(1);
    // When a byte-array reference is present, the variable is not considered
    // null
    assertThat(runtimeService.createProcessInstanceQuery().variableValueNotEquals("nullVarByte", null).count()).isEqualTo(1);

    // Test value-only
    assertThat(runtimeService.createProcessInstanceQuery().variableValueEquals(null).count()).isEqualTo(1);

    // All other variable queries with null should throw exception
    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> runtimeService.createProcessInstanceQuery().variableValueGreaterThan("nullVar", null))
      .withMessageContaining("Booleans and null cannot be used in 'greater than' condition");

    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual("nullVar", null))
      .withMessageContaining("Booleans and null cannot be used in 'greater than or equal' condition");

    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> runtimeService.createProcessInstanceQuery().variableValueLessThan("nullVar", null))
      .withMessageContaining("Booleans and null cannot be used in 'less than' condition");

    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> runtimeService.createProcessInstanceQuery().variableValueLessThanOrEqual("nullVar", null))
      .withMessageContaining("Booleans and null cannot be used in 'less than or equal' condition");

    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> runtimeService.createProcessInstanceQuery().variableValueLike("nullVar", null))
      .withMessageContaining("Only string values can be used with 'like' condition");

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance3.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance4.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance5.getId(), "test");

    // Test value-only, no more null-variables exist
    assertThat(runtimeService.createProcessInstanceQuery().variableValueEquals(null).count()).isEqualTo(0);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testQueryEqualsIgnoreCase() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("mixed", "AbCdEfG");
    vars.put("upper", "ABCDEFG");
    vars.put("lower", "abcdefg");
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    ProcessInstance instance = runtimeService.createProcessInstanceQuery().variableValueEqualsIgnoreCase("mixed", "abcdefg").singleResult();
    assertThat(instance).isNotNull();
    assertThat(instance.getId()).isEqualTo(processInstance1.getId());

    instance = runtimeService.createProcessInstanceQuery().variableValueEqualsIgnoreCase("lower", "abcdefg").singleResult();
    assertThat(instance).isNotNull();
    assertThat(instance.getId()).isEqualTo(processInstance1.getId());

    instance = runtimeService.createProcessInstanceQuery().variableValueEqualsIgnoreCase("upper", "abcdefg").singleResult();
    assertThat(instance).isNotNull();
    assertThat(instance.getId()).isEqualTo(processInstance1.getId());

    // Pass in non-lower-case string
    instance = runtimeService.createProcessInstanceQuery().variableValueEqualsIgnoreCase("upper", "ABCdefg").singleResult();
    assertThat(instance).isNotNull();
    assertThat(instance.getId()).isEqualTo(processInstance1.getId());

    // Pass in null-value, should cause exception
    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> runtimeService.createProcessInstanceQuery().variableValueEqualsIgnoreCase("upper", null).singleResult())
      .withMessageContaining("value is null");

    // Pass in null name, should cause exception
    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> runtimeService.createProcessInstanceQuery().variableValueEqualsIgnoreCase(null, "abcdefg").singleResult())
      .withMessageContaining("name is null");

    // Test NOT equals
    instance = runtimeService.createProcessInstanceQuery().variableValueNotEqualsIgnoreCase("upper", "UIOP").singleResult();
    assertThat(instance).isNotNull();

    // Should return result when using "ABCdefg" case-insensitive while
    // normal not-equals won't
    instance = runtimeService.createProcessInstanceQuery().variableValueNotEqualsIgnoreCase("upper", "ABCdefg").singleResult();
    assertThat(instance).isNull();
    instance = runtimeService.createProcessInstanceQuery().variableValueNotEquals("upper", "ABCdefg").singleResult();
    assertThat(instance).isNotNull();

    // Pass in null-value, should cause exception
    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> runtimeService.createProcessInstanceQuery().variableValueNotEqualsIgnoreCase("upper", null).singleResult())
      .withMessageContaining("value is null");

    // Pass in null name, should cause exception
    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> runtimeService.createProcessInstanceQuery().variableValueNotEqualsIgnoreCase(null, "abcdefg").singleResult())
      .withMessageContaining("name is null");
  }

  @Deployment(resources={"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryLikeIgnoreCase() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("mixed", "AbCdEfG");
    vars.put("upper", "ABCDEFG");
    vars.put("lower", "abcdefg");
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    ProcessInstance instance = runtimeService.createProcessInstanceQuery().variableValueLikeIgnoreCase("mixed", "abcd%").singleResult();
    assertThat(instance).isNotNull();
    assertThat(instance.getId()).isEqualTo(processInstance1.getId());

    instance = runtimeService.createProcessInstanceQuery().variableValueLikeIgnoreCase("lower", "abcde%").singleResult();
    assertThat(instance).isNotNull();
    assertThat(instance.getId()).isEqualTo(processInstance1.getId());

    instance = runtimeService.createProcessInstanceQuery().variableValueLikeIgnoreCase("upper", "abcd%").singleResult();
    assertThat(instance).isNotNull();
    assertThat(instance.getId()).isEqualTo(processInstance1.getId());

    // Pass in non-lower-case string
    instance = runtimeService.createProcessInstanceQuery().variableValueLikeIgnoreCase("upper", "ABCde%").singleResult();
    assertThat(instance).isNotNull();
    assertThat(instance.getId()).isEqualTo(processInstance1.getId());

    // Pass in null-value, should cause exception
    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> runtimeService.createProcessInstanceQuery().variableValueEqualsIgnoreCase("upper", null).singleResult())
      .withMessage("value is null");

    // Pass in null name, should cause exception
    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> runtimeService.createProcessInstanceQuery().variableValueEqualsIgnoreCase(null, "abcdefg").singleResult())
      .withMessage("name is null");
  }

  @Deployment(resources={
    "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testQueryInvalidTypes() throws Exception {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("bytesVar", "test".getBytes());
    vars.put("serializableVar", new DummySerializable());

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> runtimeService.createProcessInstanceQuery().variableValueEquals("bytesVar", "test".getBytes()).list())
      .withMessage("Variables of type ByteArray cannot be used to query");

    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> runtimeService.createProcessInstanceQuery().variableValueEquals("serializableVar", new DummySerializable()).list())
      .withMessage("Variables of type ByteArray cannot be used to query");

    runtimeService.deleteProcessInstance(processInstance.getId(), "test");
  }

  public void testQueryVariablesNullNameArgument() {
    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> runtimeService.createProcessInstanceQuery().variableValueEquals(null, "value"))
      .withMessage("name is null");
    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> runtimeService.createProcessInstanceQuery().variableValueNotEquals(null, "value"))
      .withMessage("name is null");
    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> runtimeService.createProcessInstanceQuery().variableValueGreaterThan(null, "value"))
      .withMessage("name is null");
    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> runtimeService.createProcessInstanceQuery().variableValueGreaterThanOrEqual(null, "value"))
      .withMessage("name is null");
    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> runtimeService.createProcessInstanceQuery().variableValueLessThan(null, "value"))
      .withMessage("name is null");
    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> runtimeService.createProcessInstanceQuery().variableValueLessThanOrEqual(null, "value"))
      .withMessage("name is null");
    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> runtimeService.createProcessInstanceQuery().variableValueLike(null, "value"))
      .withMessage("name is null");
  }

  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testQueryAllVariableTypes() throws Exception {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("nullVar", null);
    vars.put("stringVar", "string");
    vars.put("longVar", 10L);
    vars.put("doubleVar", 1.2);
    vars.put("integerVar", 1234);
    vars.put("booleanVar", true);
    vars.put("shortVar", (short) 123);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().variableValueEquals("nullVar", null).variableValueEquals("stringVar", "string").variableValueEquals("longVar", 10L)
        .variableValueEquals("doubleVar", 1.2).variableValueEquals("integerVar", 1234).variableValueEquals("booleanVar", true).variableValueEquals("shortVar", (short) 123);

    List<ProcessInstance> processInstances = query.list();
    assertThat(processInstances).isNotNull();
    assertThat(processInstances).hasSize(1);
    assertThat(processInstances.get(0).getId()).isEqualTo(processInstance.getId());

    runtimeService.deleteProcessInstance(processInstance.getId(), "test");
  }

  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testClashingValues() throws Exception {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("var", 1234L);

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);

    Map<String, Object> vars2 = new HashMap<String, Object>();
    vars2.put("var", 1234);

    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("oneTaskProcess", vars2);

    List<ProcessInstance> foundInstances = runtimeService.createProcessInstanceQuery().processDefinitionKey("oneTaskProcess").variableValueEquals("var", 1234L).list();

    assertThat(foundInstances).hasSize(1);
    assertThat(foundInstances.get(0).getId()).isEqualTo(processInstance.getId());

    runtimeService.deleteProcessInstance(processInstance.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");
  }

  public void testQueryByProcessInstanceIds() {
    Set<String> processInstanceIds = new HashSet<String>(this.processInstanceIds);

    // start an instance that will not be part of the query
    runtimeService.startProcessInstanceByKey("oneTaskProcess2", "2");

    ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery().processInstanceIds(processInstanceIds);
    assertThat(processInstanceQuery.count()).isEqualTo(5);

    List<ProcessInstance> processInstances = processInstanceQuery.list();
    assertThat(processInstances).isNotNull();
    assertThat(processInstances).hasSize(5);

    for (ProcessInstance processInstance : processInstances) {
      assertThat(processInstanceIds.contains(processInstance.getId())).isTrue();
    }
  }

  public void testQueryByProcessInstanceIdsEmpty() {
    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> runtimeService.createProcessInstanceQuery().processInstanceIds(new HashSet<String>()))
      .withMessage("Set of process instance ids is empty");
  }

  public void testQueryByProcessInstanceIdsNull() {
    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> runtimeService.createProcessInstanceQuery().processInstanceIds(null))
      .withMessage("Set of process instance ids is null");
  }

  public void testNativeQuery() {
    // just test that the query will be constructed and executed, details are tested in the TaskQueryTest
    assertThat(managementService.getTableName(ProcessInstance.class)).isEqualTo("ACT_RU_EXECUTION");

    long piCount = runtimeService.createProcessInstanceQuery().count();

    // There are 2 executions for each process instance
    assertThat(runtimeService.createNativeProcessInstanceQuery().sql("SELECT * FROM " + managementService.getTableName(ProcessInstance.class)).list()).hasSize((int) piCount * 2);
    assertThat(runtimeService.createNativeProcessInstanceQuery().sql("SELECT count(*) FROM " + managementService.getTableName(ProcessInstance.class)).count()).isEqualTo(piCount*2);
  }

  /**
   * Test confirming fix for ACT-1731
   */
  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testIncludeBinaryVariables() throws Exception {
    // Start process with a binary variable
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", singletonMap("binaryVariable", (Object) "It is I, le binary".getBytes()));

    processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).includeProcessVariables().singleResult();
    assertThat(processInstance).isNotNull();
    // Query process, including variables
    byte[] bytes = (byte[]) processInstance.getProcessVariables().get("binaryVariable");
    assertThat(new String(bytes)).isEqualTo("It is I, le binary");
  }

  public void testNativeQueryPaging() {
    assertThat(runtimeService.createNativeProcessInstanceQuery().sql("SELECT * FROM " + managementService.getTableName(ProcessInstance.class)).listPage(0, 5)).hasSize(5);
  }

  public void testLocalizeProcess() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    List<ProcessInstance> processes = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).list();
    assertThat(processes).hasSize(1);
    assertThat(processes.get(0).getName()).isNull();
    assertThat(processes.get(0).getDescription()).isNull();

    ObjectNode infoNode = dynamicBpmnService.getProcessDefinitionInfo(processInstance.getProcessDefinitionId());
    dynamicBpmnService.changeLocalizationName("en-GB", "oneTaskProcess", "The One Task Process 'en-GB' localized name", infoNode);
    dynamicBpmnService.changeLocalizationDescription("en-GB", "oneTaskProcess", "The One Task Process 'en-GB' localized description", infoNode);
    dynamicBpmnService.saveProcessDefinitionInfo(processInstance.getProcessDefinitionId(), infoNode);

    dynamicBpmnService.changeLocalizationName("en", "oneTaskProcess", "The One Task Process 'en' localized name", infoNode);
    dynamicBpmnService.changeLocalizationDescription("en", "oneTaskProcess", "The One Task Process 'en' localized description", infoNode);
    dynamicBpmnService.saveProcessDefinitionInfo(processInstance.getProcessDefinitionId(), infoNode);

    processes = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).list();
    assertThat(processes).hasSize(1);
    assertThat(processes.get(0).getName()).isNull();
    assertThat(processes.get(0).getDescription()).isNull();

    processes = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).locale("es").list();
    assertThat(processes).hasSize(1);
    assertThat(processes.get(0).getName()).isEqualTo("Nombre del proceso");
    assertThat(processes.get(0).getDescription()).isEqualTo("Descripción del proceso");

    processes = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).locale("en-GB").list();
    assertThat(processes).hasSize(1);
    assertThat(processes.get(0).getName()).isEqualTo("The One Task Process 'en-GB' localized name");
    assertThat(processes.get(0).getDescription()).isEqualTo("The One Task Process 'en-GB' localized description");

    processes = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).listPage(0, 10);
    assertThat(processes).hasSize(1);
    assertThat(processes.get(0).getName()).isNull();
    assertThat(processes.get(0).getDescription()).isNull();

    processes = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).locale("es").listPage(0,10);
    assertThat(processes).hasSize(1);
    assertThat(processes.get(0).getName()).isEqualTo("Nombre del proceso");
    assertThat(processes.get(0).getDescription()).isEqualTo("Descripción del proceso");

    processes = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).locale("en-GB").listPage(0, 10);
    assertThat(processes).hasSize(1);
    assertThat(processes.get(0).getName()).isEqualTo("The One Task Process 'en-GB' localized name");
    assertThat(processes.get(0).getDescription()).isEqualTo("The One Task Process 'en-GB' localized description");

    processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(processInstance.getName()).isNull();
    assertThat(processInstance.getDescription()).isNull();

    processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).locale("es").singleResult();
    assertThat(processInstance.getName()).isEqualTo("Nombre del proceso");
    assertThat(processInstance.getDescription()).isEqualTo("Descripción del proceso");

    processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).locale("en-GB").singleResult();
    assertThat(processInstance.getName()).isEqualTo("The One Task Process 'en-GB' localized name");
    assertThat(processInstance.getDescription()).isEqualTo("The One Task Process 'en-GB' localized description");

    processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(processInstance.getName()).isNull();
    assertThat(processInstance.getDescription()).isNull();

    processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).locale("en").singleResult();
    assertThat(processInstance.getName()).isEqualTo("The One Task Process 'en' localized name");
    assertThat(processInstance.getDescription()).isEqualTo("The One Task Process 'en' localized description");

    processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).locale("en-AU").withLocalizationFallback().singleResult();
    assertThat(processInstance.getName()).isEqualTo("The One Task Process 'en' localized name");
    assertThat(processInstance.getDescription()).isEqualTo("The One Task Process 'en' localized description");
  }

  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testQueryStartedBefore() throws Exception {
    Calendar calendar = new GregorianCalendar();
    calendar.set(Calendar.YEAR, 2010);
    calendar.set(Calendar.MONTH, 8);
    calendar.set(Calendar.DAY_OF_MONTH, 30);
    calendar.set(Calendar.HOUR_OF_DAY, 12);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    Date noon = calendar.getTime();

    processEngineConfiguration.getClock().setCurrentTime(noon);

    calendar.add(Calendar.HOUR_OF_DAY, 1);
    Date hourLater = calendar.getTime();

    runtimeService.startProcessInstanceByKey("oneTaskProcess");

    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().startedBefore(hourLater).list();

    assertThat(processInstances).hasSize(1);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testQueryStartedAfter() throws Exception {
    Calendar calendar = new GregorianCalendar();
    calendar.set(Calendar.YEAR, 2030);
    calendar.set(Calendar.MONTH, 8);
    calendar.set(Calendar.DAY_OF_MONTH, 30);
    calendar.set(Calendar.HOUR_OF_DAY, 12);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    Date noon = calendar.getTime();

    processEngineConfiguration.getClock().setCurrentTime(noon);

    calendar.add(Calendar.HOUR_OF_DAY, -1);
    Date hourEarlier = calendar.getTime();

    runtimeService.startProcessInstanceByKey("oneTaskProcess");

    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().startedAfter(hourEarlier).list();

    assertThat(processInstances).hasSize(1);
  }

  @Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testQueryStartedBy() throws Exception {
    final String authenticatedUser = "user1";
    Authentication.setAuthenticatedUserId(authenticatedUser);
    runtimeService.startProcessInstanceByKey("oneTaskProcess");

    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().startedBy(authenticatedUser).list();

    assertThat(processInstances).hasSize(1);
  }
}

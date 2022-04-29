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


package org.activiti.engine.test.api.repository;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class ProcessDefinitionQueryTest extends PluggableActivitiTestCase {

  private String deploymentOneId;
  private String deploymentTwoId;

  @Override
  protected void setUp() throws Exception {
    deploymentOneId = repositoryService.createDeployment().name("org/activiti/engine/test/repository/one.bpmn20.xml").addClasspathResource("org/activiti/engine/test/repository/one.bpmn20.xml")
        .addClasspathResource("org/activiti/engine/test/repository/two.bpmn20.xml").deploy().getId();

    deploymentTwoId = repositoryService.createDeployment().name("org/activiti/engine/test/repository/one.bpmn20.xml").addClasspathResource("org/activiti/engine/test/repository/one.bpmn20.xml")
        .deploy().getId();

    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    repositoryService.deleteDeployment(deploymentOneId, true);
    repositoryService.deleteDeployment(deploymentTwoId, true);
  }

  public void testProcessDefinitionProperties() {
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().orderByProcessDefinitionName().asc().orderByProcessDefinitionVersion().asc()
        .orderByProcessDefinitionCategory().asc().list();

    ProcessDefinition processDefinition = processDefinitions.get(0);
    assertThat(processDefinition.getKey()).isEqualTo("one");
    assertThat(processDefinition.getName()).isEqualTo("One");
    assertThat(processDefinition.getId().startsWith("one:1")).isTrue();
    assertThat(processDefinition.getCategory()).isEqualTo("Examples");

    processDefinition = processDefinitions.get(1);
    assertThat(processDefinition.getKey()).isEqualTo("one");
    assertThat(processDefinition.getName()).isEqualTo("One");
    assertThat(processDefinition.getId().startsWith("one:2")).isTrue();
    assertThat(processDefinition.getCategory()).isEqualTo("Examples");

    processDefinition = processDefinitions.get(2);
    assertThat(processDefinition.getKey()).isEqualTo("two");
    assertThat(processDefinition.getName()).isEqualTo("Two");
    assertThat(processDefinition.getId().startsWith("two:1")).isTrue();
    assertThat(processDefinition.getCategory()).isEqualTo("Examples2");
  }

  public void testQueryByDeploymentId() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().deploymentId(deploymentOneId);
    verifyQueryResults(query, 2);
  }

  public void testQueryByInvalidDeploymentId() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().deploymentId("invalid");
    verifyQueryResults(query, 0);

    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> repositoryService.createProcessDefinitionQuery().deploymentId(null));
  }

  public void testQueryByName() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionName("Two");
    verifyQueryResults(query, 1);

    query = repositoryService.createProcessDefinitionQuery().processDefinitionName("One");
    verifyQueryResults(query, 2);
  }

  public void testQueryByInvalidName() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionName("invalid");
    verifyQueryResults(query, 0);

    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> repositoryService.createProcessDefinitionQuery().processDefinitionName(null));
  }

  public void testQueryByNameLike() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionNameLike("%w%");
    verifyQueryResults(query, 1);
  }

  public void testQueryByInvalidNameLike() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionNameLike("%invalid%");
    verifyQueryResults(query, 0);
  }

  public void testQueryByKey() {
    // process one
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionKey("one");
    verifyQueryResults(query, 2);

    // process two
    query = repositoryService.createProcessDefinitionQuery().processDefinitionKey("two");
    verifyQueryResults(query, 1);
  }

  public void testQueryByKeys() {
    Set<String> one = new HashSet<String>();
    one.add("one");
    Set<String> two = new HashSet<String>();
    two.add("two");
    Set<String> oneAndTwo = new HashSet<String>();
    oneAndTwo.addAll(one);
    oneAndTwo.addAll(two);

    // process one
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionKeys(one);
    verifyQueryResults(query, 2);

    // process two
    query = repositoryService.createProcessDefinitionQuery().processDefinitionKeys(two);
    verifyQueryResults(query, 1);

    query = repositoryService.createProcessDefinitionQuery().processDefinitionKeys(oneAndTwo);
    verifyQueryResults(query, 3);
  }

  public void testQueryByInvalidKey() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionKey("invalid");
    verifyQueryResults(query, 0);

    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> repositoryService.createProcessDefinitionQuery().processDefinitionKey(null));
  }

  public void testQueryByKeyLike() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionKeyLike("%o%");
    verifyQueryResults(query, 3);
  }

  public void testQueryByInvalidKeyLike() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionKeyLike("%invalid%");
    verifyQueryResults(query, 0);

    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> repositoryService.createProcessDefinitionQuery().processDefinitionKeyLike(null));
  }

  public void testQueryByCategory() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionCategory("Examples");
    verifyQueryResults(query, 2);
  }

  public void testQueryByCategoryLike() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionCategoryLike("%Example%");
    verifyQueryResults(query, 3);

    query = repositoryService.createProcessDefinitionQuery().processDefinitionCategoryLike("%amples2");
    verifyQueryResults(query, 1);
  }

  public void testQueryByVersion() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionVersion(2);
    verifyQueryResults(query, 1);

    query = repositoryService.createProcessDefinitionQuery().processDefinitionVersion(1);
    verifyQueryResults(query, 2);
  }

  public void testQueryByInvalidVersion() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionVersion(3);
    verifyQueryResults(query, 0);

    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> repositoryService.createProcessDefinitionQuery().processDefinitionVersion(-1).list());

    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> repositoryService.createProcessDefinitionQuery().processDefinitionVersion(null).list());
  }

  public void testQueryByKeyAndVersion() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionKey("one").processDefinitionVersion(1);
    verifyQueryResults(query, 1);

    query = repositoryService.createProcessDefinitionQuery().processDefinitionKey("one").processDefinitionVersion(2);
    verifyQueryResults(query, 1);

    query = repositoryService.createProcessDefinitionQuery().processDefinitionKey("one").processDefinitionVersion(3);
    verifyQueryResults(query, 0);
  }

  public void testQueryByLatest() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().latestVersion();
    verifyQueryResults(query, 2);

    query = repositoryService.createProcessDefinitionQuery().processDefinitionKey("one").latestVersion();
    verifyQueryResults(query, 1);

    query = repositoryService.createProcessDefinitionQuery().processDefinitionKey("two").latestVersion();
    verifyQueryResults(query, 1);
  }

  public void testQuerySorting() {

    // asc

    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().orderByProcessDefinitionId().asc();
    verifyQueryResults(query, 3);

    query = repositoryService.createProcessDefinitionQuery().orderByDeploymentId().asc();
    verifyQueryResults(query, 3);

    query = repositoryService.createProcessDefinitionQuery().orderByProcessDefinitionKey().asc();
    verifyQueryResults(query, 3);

    query = repositoryService.createProcessDefinitionQuery().orderByProcessDefinitionVersion().asc();
    verifyQueryResults(query, 3);

    // desc

    query = repositoryService.createProcessDefinitionQuery().orderByProcessDefinitionId().desc();
    verifyQueryResults(query, 3);

    query = repositoryService.createProcessDefinitionQuery().orderByDeploymentId().desc();
    verifyQueryResults(query, 3);

    query = repositoryService.createProcessDefinitionQuery().orderByProcessDefinitionKey().desc();
    verifyQueryResults(query, 3);

    query = repositoryService.createProcessDefinitionQuery().orderByProcessDefinitionVersion().desc();
    verifyQueryResults(query, 3);

    // Typical use case
    query = repositoryService.createProcessDefinitionQuery().orderByProcessDefinitionKey().asc().orderByProcessDefinitionVersion().desc();
    List<ProcessDefinition> processDefinitions = query.list();
    assertThat(processDefinitions).hasSize(3);

    assertThat(processDefinitions.get(0).getKey()).isEqualTo("one");
    assertThat(processDefinitions.get(0).getVersion()).isEqualTo(2);
    assertThat(processDefinitions.get(1).getKey()).isEqualTo("one");
    assertThat(processDefinitions.get(1).getVersion()).isEqualTo(1);
    assertThat(processDefinitions.get(2).getKey()).isEqualTo("two");
    assertThat(processDefinitions.get(2).getVersion()).isEqualTo(1);
  }

  private void verifyQueryResults(ProcessDefinitionQuery query, int countExpected) {
    assertThat(query.list()).hasSize(countExpected);
    assertThat(query.count()).isEqualTo(countExpected);

    if (countExpected == 1) {
      assertThat(query.singleResult()).isNotNull();
    } else if (countExpected > 1) {
      verifySingleResultFails(query);
    } else if (countExpected == 0) {
      assertThat(query.singleResult()).isNull();
    }
  }

  private void verifySingleResultFails(ProcessDefinitionQuery query) {
    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> query.singleResult());
  }

  public void testQueryByMessageSubscription() {
    Deployment deployment = repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/api/repository/processWithNewBookingMessage.bpmn20.xml")
        .addClasspathResource("org/activiti/engine/test/api/repository/processWithNewInvoiceMessage.bpmn20.xml").deploy();

    assertThat(repositoryService.createProcessDefinitionQuery().messageEventSubscriptionName("newInvoiceMessage").count()).isEqualTo(1);

    assertThat(repositoryService.createProcessDefinitionQuery().messageEventSubscriptionName("newBookingMessage").count()).isEqualTo(1);

    assertThat(repositoryService.createProcessDefinitionQuery().messageEventSubscriptionName("bogus").count()).isEqualTo(0);

    repositoryService.deleteDeployment(deployment.getId());
  }

  public void testNativeQuery() {
    assertThat(managementService.getTableName(ProcessDefinition.class)).isEqualTo("ACT_RE_PROCDEF");
    assertThat(managementService.getTableName(ProcessDefinitionEntity.class)).isEqualTo("ACT_RE_PROCDEF");
    String tableName = managementService.getTableName(ProcessDefinition.class);
    String baseQuerySql = "SELECT * FROM " + tableName;

    assertThat(repositoryService.createNativeProcessDefinitionQuery().sql(baseQuerySql).list()).hasSize(3);

    assertThat(repositoryService.createNativeProcessDefinitionQuery().sql(baseQuerySql + " where KEY_ like #{key}").parameter("key", "%o%").list()).hasSize(3);

    assertThat(repositoryService.createNativeProcessDefinitionQuery().sql(baseQuerySql + " where NAME_ = #{name}").parameter("name", "One").list()).hasSize(2);

    // paging
    assertThat(repositoryService.createNativeProcessDefinitionQuery().sql(baseQuerySql).listPage(0, 2)).hasSize(2);
    assertThat(repositoryService.createNativeProcessDefinitionQuery().sql(baseQuerySql).listPage(1, 3)).hasSize(2);
  }

  public void testQueryByProcessDefinitionIds() {
  	List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();
  	Set<String> ids = new HashSet<String>();
  	for (ProcessDefinition processDefinition : processDefinitions) {
  		ids.add(processDefinition.getId());
  	}

  	List<ProcessDefinition> queryResults = repositoryService.createProcessDefinitionQuery().processDefinitionIds(ids).list();
  	assertThat(ids).hasSize(queryResults.size());
  	for (ProcessDefinition processDefinition : queryResults) {
  		assertThat(ids.contains(processDefinition.getId())).isTrue();
  	}
  }

  public void testQueryWithNullArgs() {
    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> repositoryService.createProcessDefinitionQuery().processDefinitionKeys(null));
  }

  public void testQueryWithEmptyIdSet() {
    List<ProcessDefinition> processDefinitionList = repositoryService.createProcessDefinitionQuery().processDefinitionIds(new HashSet<>(0)).list();
    assertThat(processDefinitionList).isNotEmpty();
  }

}

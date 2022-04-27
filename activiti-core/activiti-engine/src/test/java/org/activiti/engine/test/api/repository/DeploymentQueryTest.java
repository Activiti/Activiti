/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentQuery;

import java.util.List;


public class DeploymentQueryTest extends PluggableActivitiTestCase {

  private String deploymentOneId;

  private String deploymentTwoId;

  @Override
  protected void setUp() throws Exception {
    deploymentOneId = repositoryService.createDeployment().name("org/activiti/engine/test/repository/one.bpmn20.xml").category("testCategory")
        .addClasspathResource("org/activiti/engine/test/repository/one.bpmn20.xml").deploy().getId();

    deploymentTwoId = repositoryService.createDeployment().name("org/activiti/engine/test/repository/two.bpmn20.xml").addClasspathResource("org/activiti/engine/test/repository/two.bpmn20.xml")
        .deploy().getId();

    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    repositoryService.deleteDeployment(deploymentOneId, true);
    repositoryService.deleteDeployment(deploymentTwoId, true);
  }

  public void testQueryNoCriteria() {
    DeploymentQuery query = repositoryService.createDeploymentQuery();
    assertThat(query.list()).hasSize(2);
    assertThat(query.count()).isEqualTo(2);

    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> query.singleResult());
  }

  public void testQueryByDeploymentId() {
    DeploymentQuery query = repositoryService.createDeploymentQuery().deploymentId(deploymentOneId);
    assertThat(query.singleResult()).isNotNull();
    assertThat(query.list()).hasSize(1);
    assertThat(query.count()).isEqualTo(1);
  }

  public void testQueryByInvalidDeploymentId() {
    DeploymentQuery query = repositoryService.createDeploymentQuery().deploymentId("invalid");
    assertThat(query.singleResult()).isNull();
    assertThat(query.list()).hasSize(0);
    assertThat(query.count()).isEqualTo(0);

    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> repositoryService.createDeploymentQuery().deploymentId(null));
  }

  public void testQueryByName() {
    DeploymentQuery query = repositoryService.createDeploymentQuery().deploymentName("org/activiti/engine/test/repository/two.bpmn20.xml");
    assertThat(query.singleResult()).isNotNull();
    assertThat(query.list()).hasSize(1);
    assertThat(query.count()).isEqualTo(1);
  }

  public void testQueryByInvalidName() {
    DeploymentQuery query = repositoryService.createDeploymentQuery().deploymentName("invalid");
    assertThat(query.singleResult()).isNull();
    assertThat(query.list()).hasSize(0);
    assertThat(query.count()).isEqualTo(0);

    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> repositoryService.createDeploymentQuery().deploymentName(null));
  }

  public void testQueryByNameLike() {
    DeploymentQuery query = repositoryService.createDeploymentQuery().deploymentNameLike("%activiti%");
    assertThat(query.list()).hasSize(2);
    assertThat(query.count()).isEqualTo(2);

    assertThatExceptionOfType(ActivitiException.class)
      .isThrownBy(() -> query.singleResult());
  }

  public void testQueryByInvalidNameLike() {
    DeploymentQuery query = repositoryService.createDeploymentQuery().deploymentNameLike("invalid");
    assertThat(query.singleResult()).isNull();
    assertThat(query.list()).hasSize(0);
    assertThat(query.count()).isEqualTo(0);

    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> repositoryService.createDeploymentQuery().deploymentNameLike(null));
  }

  public void testQueryByNameAndCategory() {
    DeploymentQuery query = repositoryService.createDeploymentQuery().deploymentCategory("testCategory").deploymentNameLike("%activiti%");
    assertThat(query.list()).hasSize(1);
    assertThat(query.count()).isEqualTo(1);
    assertThat(query.singleResult()).isNotNull();
  }

  public void testQueryByProcessDefinitionKey() {
    DeploymentQuery query = repositoryService.createDeploymentQuery().processDefinitionKey("one");
    assertThat(query.list()).hasSize(1);
    assertThat(query.count()).isEqualTo(1);
    assertThat(query.singleResult()).isNotNull();
  }

  public void testQueryByProcessDefinitionKeyLike() {
    DeploymentQuery query = repositoryService.createDeploymentQuery().processDefinitionKeyLike("%o%");
    assertThat(query.list()).hasSize(2);
    assertThat(query.count()).isEqualTo(2);
  }

  public void testQueryByInvalidProcessDefinitionKeyLike() {
    DeploymentQuery query = repositoryService.createDeploymentQuery().processDefinitionKeyLike("invalid");
    assertThat(query.list()).hasSize(0);
    assertThat(query.count()).isEqualTo(0);
  }

  public void testVerifyDeploymentProperties() {
    List<Deployment> deployments = repositoryService.createDeploymentQuery().orderByDeploymentName().asc().list();

    Deployment deploymentOne = deployments.get(0);
    assertThat(deploymentOne.getName()).isEqualTo("org/activiti/engine/test/repository/one.bpmn20.xml");
    assertThat(deploymentOne.getId()).isEqualTo(deploymentOneId);

    Deployment deploymentTwo = deployments.get(1);
    assertThat(deploymentTwo.getName()).isEqualTo("org/activiti/engine/test/repository/two.bpmn20.xml");
    assertThat(deploymentTwo.getId()).isEqualTo(deploymentTwoId);

    deployments = repositoryService.createDeploymentQuery().deploymentNameLike("%one%").orderByDeploymentName().asc().list();

    assertThat(deployments.get(0).getName()).isEqualTo("org/activiti/engine/test/repository/one.bpmn20.xml");
    assertThat(deployments).hasSize(1);

    assertThat(repositoryService.createDeploymentQuery().orderByDeploymentId().asc().list()).hasSize(2);

    assertThat(repositoryService.createDeploymentQuery().orderByDeploymenTime().asc().list()).hasSize(2);

  }

  public void testNativeQuery() {
    assertThat(managementService.getTableName(Deployment.class)).isEqualTo("ACT_RE_DEPLOYMENT");
    assertThat(managementService.getTableName(DeploymentEntity.class)).isEqualTo("ACT_RE_DEPLOYMENT");
    String tableName = managementService.getTableName(Deployment.class);
    String baseQuerySql = "SELECT * FROM " + tableName;

    assertThat(repositoryService.createNativeDeploymentQuery().sql(baseQuerySql).list()).hasSize(2);

    assertThat(repositoryService.createNativeDeploymentQuery().sql(baseQuerySql + " where NAME_ = #{name}").parameter("name", "org/activiti/engine/test/repository/one.bpmn20.xml").list()).hasSize(1);

    // paging
    assertThat(repositoryService.createNativeDeploymentQuery().sql(baseQuerySql).listPage(0, 2)).hasSize(2);
    assertThat(repositoryService.createNativeDeploymentQuery().sql(baseQuerySql).listPage(1, 3)).hasSize(1);
  }

}

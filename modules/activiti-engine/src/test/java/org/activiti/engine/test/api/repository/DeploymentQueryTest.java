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

package org.activiti.engine.test.api.repository;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentQuery;

import java.util.List;


/**
 * @author Tom Baeyens
 */
public class DeploymentQueryTest extends PluggableActivitiTestCase {
  
  private String deploymentOneId;
  
  private String deploymentTwoId;
  
  @Override
  protected void setUp() throws Exception {
    deploymentOneId = repositoryService
      .createDeployment()
      .name("org/activiti/engine/test/repository/one.bpmn20.xml")
      .category("testCategory")
      .addClasspathResource("org/activiti/engine/test/repository/one.bpmn20.xml")
      .deploy()
      .getId();

    deploymentTwoId = repositoryService
      .createDeployment()
      .name("org/activiti/engine/test/repository/two.bpmn20.xml")
      .addClasspathResource("org/activiti/engine/test/repository/two.bpmn20.xml")
      .deploy()
      .getId();
    
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
    assertEquals(2, query.list().size());
    assertEquals(2, query.count());
    
    try {
      query.singleResult();
      fail();
    } catch (ActivitiException e) {}
  }
  
  public void testQueryByDeploymentId() {
    DeploymentQuery query = repositoryService.createDeploymentQuery().deploymentId(deploymentOneId);
    assertNotNull(query.singleResult());
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }
  
  public void testQueryByInvalidDeploymentId() {
    DeploymentQuery query = repositoryService.createDeploymentQuery().deploymentId("invalid");
    assertNull(query.singleResult());
    assertEquals(0, query.list().size());
    assertEquals(0, query.count());
    
    try {
      repositoryService.createDeploymentQuery().deploymentId(null);
      fail();
    } catch (ActivitiIllegalArgumentException e) {}
  }
  
  public void testQueryByName() {
    DeploymentQuery query = repositoryService.createDeploymentQuery().deploymentName("org/activiti/engine/test/repository/two.bpmn20.xml");
    assertNotNull(query.singleResult());
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }
  
  public void testQueryByInvalidName() {
    DeploymentQuery query = repositoryService.createDeploymentQuery().deploymentName("invalid");
    assertNull(query.singleResult());
    assertEquals(0, query.list().size());
    assertEquals(0, query.count());
    
    try {
      repositoryService.createDeploymentQuery().deploymentName(null);
      fail();
    } catch (ActivitiIllegalArgumentException e) {}
  }
  
  public void testQueryByNameLike() {
    DeploymentQuery query = repositoryService.createDeploymentQuery().deploymentNameLike("%activiti%");
    assertEquals(2, query.list().size());
    assertEquals(2, query.count());
    
    try {
      query.singleResult();
      fail();
    } catch (ActivitiException e) {}
  }
  
  public void testQueryByInvalidNameLike() {
    DeploymentQuery query = repositoryService.createDeploymentQuery().deploymentNameLike("invalid");
    assertNull(query.singleResult());
    assertEquals(0, query.list().size());
    assertEquals(0, query.count());
    
    try {
      repositoryService.createDeploymentQuery().deploymentNameLike(null);
      fail();
    } catch (ActivitiIllegalArgumentException e) {}
  }
  
  public void testQueryByNameAndCategory() {
    DeploymentQuery query = repositoryService.createDeploymentQuery().deploymentCategory("testCategory").deploymentNameLike("%activiti%");
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
    assertNotNull(query.singleResult());
  }
  
  public void testQueryByProcessDefinitionKey() {
    DeploymentQuery query = repositoryService.createDeploymentQuery().processDefinitionKey("one");
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
    assertNotNull(query.singleResult());
  }

  public void testQueryByProcessDefinitionKeyLike() {
    DeploymentQuery query = repositoryService.createDeploymentQuery().processDefinitionKeyLike("%o%");
    assertEquals(2, query.list().size());
    assertEquals(2, query.count());
  }
  
  public void testQueryByInvalidProcessDefinitionKeyLike() {
    DeploymentQuery query = repositoryService.createDeploymentQuery().processDefinitionKeyLike("invalid");
    assertEquals(0, query.list().size());
    assertEquals(0, query.count());
  }
  
  public void testVerifyDeploymentProperties() {
    List<Deployment> deployments = repositoryService.createDeploymentQuery()
      .orderByDeploymentName()
      .asc()
      .list();
    
    Deployment deploymentOne = deployments.get(0);
    assertEquals("org/activiti/engine/test/repository/one.bpmn20.xml", deploymentOne.getName());
    assertEquals(deploymentOneId, deploymentOne.getId());

    Deployment deploymentTwo = deployments.get(1);
    assertEquals("org/activiti/engine/test/repository/two.bpmn20.xml", deploymentTwo.getName());
    assertEquals(deploymentTwoId, deploymentTwo.getId());
    
    deployments = repositoryService.createDeploymentQuery()
      .deploymentNameLike("%one%")
       .orderByDeploymentName()
      .asc()
      .list();
    
    assertEquals("org/activiti/engine/test/repository/one.bpmn20.xml", deployments.get(0).getName());
    assertEquals(1, deployments.size());

    assertEquals(2, repositoryService.createDeploymentQuery()
      .orderByDeploymentId()
      .asc()
      .list()
      .size());

    assertEquals(2, repositoryService.createDeploymentQuery()
      .orderByDeploymenTime()
      .asc()
      .list()
      .size());

  }

  public void testNativeQuery() {
    assertEquals("ACT_RE_DEPLOYMENT", managementService.getTableName(Deployment.class));
    assertEquals("ACT_RE_DEPLOYMENT", managementService.getTableName(DeploymentEntity.class));
    String tableName = managementService.getTableName(Deployment.class);
    String baseQuerySql = "SELECT * FROM " + tableName;

    assertEquals(2, repositoryService.createNativeDeploymentQuery().sql(baseQuerySql).list().size());

    assertEquals(1, repositoryService.createNativeDeploymentQuery().sql(baseQuerySql + " where NAME_ = #{name}")
        .parameter("name", "org/activiti/engine/test/repository/one.bpmn20.xml").list().size());

    // paging
    assertEquals(2, repositoryService.createNativeDeploymentQuery().sql(baseQuerySql).listPage(0, 2).size());
    assertEquals(1, repositoryService.createNativeDeploymentQuery().sql(baseQuerySql).listPage(1, 3).size());
  }

}

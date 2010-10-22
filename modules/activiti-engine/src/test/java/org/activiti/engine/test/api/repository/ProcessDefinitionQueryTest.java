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

import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.test.ActivitiInternalTestCase;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;


/**
 * @author Joram Barrez
 */
public class ProcessDefinitionQueryTest extends ActivitiInternalTestCase {
  
  private String deploymentOneId;
  private String deploymentTwoId;
  
  @Override
  protected void setUp() throws Exception {
    deploymentOneId = repositoryService
      .createDeployment()
      .name("org/activiti/engine/test/repository/one.bpmn20.xml")
      .addClasspathResource("org/activiti/engine/test/repository/one.bpmn20.xml")
      .addClasspathResource("org/activiti/engine/test/repository/two.bpmn20.xml")
      .deploy()
      .getId();

    deploymentTwoId = repositoryService
      .createDeployment()
      .name("org/activiti/engine/test/repository/one.bpmn20.xml")
      .addClasspathResource("org/activiti/engine/test/repository/one.bpmn20.xml")
      .deploy()
      .getId();
    
    super.setUp();
  }
  
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    repositoryService.deleteDeploymentCascade(deploymentOneId);
    repositoryService.deleteDeploymentCascade(deploymentTwoId);
  }
  
  public void testQueryByDeploymentId() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().deploymentId(deploymentOneId);
    verifyQueryResults(query, 2);
  }
  
  public void testQueryByInvalidDeploymentId() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().deploymentId("invalid");
    verifyQueryResults(query, 0);
    
    try {
      repositoryService.createProcessDefinitionQuery().deploymentId(null);
      fail();
    } catch (ActivitiException e) {}
  }
  
  public void testQueryByName() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().name("Two");
    verifyQueryResults(query, 1);
    
    query = repositoryService.createProcessDefinitionQuery().name("One");
    verifyQueryResults(query, 2);
  }
  
  public void testQueryByInvalidName() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().name("invalid");
    verifyQueryResults(query, 0);
    
    try {
      repositoryService.createProcessDefinitionQuery().name(null);
      fail();
    } catch (ActivitiException e) {}
  }
  
  public void testQueryByNameLike() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().nameLike("%o%");
    verifyQueryResults(query, 1);
  }
  
  public void testQueryByInvalidNameLike() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().nameLike("%invalid%");
    verifyQueryResults(query, 0);
  }
  
  public void testQueryByKey() {
    // process one    
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().key("one");
    verifyQueryResults(query, 2);
    
    // process two
    query = repositoryService.createProcessDefinitionQuery().key("two");
    verifyQueryResults(query, 1);
  }
  
  public void testQueryByInvalidKey() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().key("invalid");
    verifyQueryResults(query, 0);
    
    try {
      repositoryService.createProcessDefinitionQuery().key(null);
      fail();
    } catch (ActivitiException e) {}
  }
  
  public void testQueryByKeyLike() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().keyLike("%o%");
    verifyQueryResults(query, 3);
  }
  
  public void testQueryByInvalidKeyLike() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().keyLike("%invalid%");
    verifyQueryResults(query, 0);
    
    try {
      repositoryService.createProcessDefinitionQuery().keyLike(null);
      fail();
    } catch (ActivitiException e) {}
  }

  public void testQueryByVersion() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().version(2);
    verifyQueryResults(query, 1);
    
    query = repositoryService.createProcessDefinitionQuery().version(1);
    verifyQueryResults(query, 2);
  }
  
  public void testQueryByInvalidVersion() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().version(3);
    verifyQueryResults(query, 0);
    
    try {
      repositoryService.createProcessDefinitionQuery().version(-1).list();
      fail();
    } catch (ActivitiException e) {}
    
    try {
      repositoryService.createProcessDefinitionQuery().version(null).list();
      fail();
    } catch (ActivitiException e) {}
  }
  
  public void testQueryByKeyAndVersion() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().key("one").version(1);
    verifyQueryResults(query, 1);
    
    query = repositoryService.createProcessDefinitionQuery().key("one").version(2);
    verifyQueryResults(query, 1);
    
    query = repositoryService.createProcessDefinitionQuery().key("one").version(3);
    verifyQueryResults(query, 0);
  }
  
  public void testQueryByLatest() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().latest();
    verifyQueryResults(query, 2);
    
    query = repositoryService.createProcessDefinitionQuery().key("one").latest();
    verifyQueryResults(query, 1);
    
    query = repositoryService.createProcessDefinitionQuery().key("two").latest();
    verifyQueryResults(query, 1);
  }
  
  public void testInvalidUsageOfLatest() {
    try {
      repositoryService.createProcessDefinitionQuery().processDefinitionId("test").latest().list();
      fail();
    } catch (ActivitiException e) {}
    
    try {
      repositoryService.createProcessDefinitionQuery().name("test").latest().list();
      fail();
    } catch (ActivitiException e) {}
    
    try {
      repositoryService.createProcessDefinitionQuery().nameLike("test").latest().list();
      fail();
    } catch (ActivitiException e) {}
    
    try {
      repositoryService.createProcessDefinitionQuery().version(1).latest().list();
      fail();
    } catch (ActivitiException e) {}
    
    try {
      repositoryService.createProcessDefinitionQuery().deploymentId("test").latest().list();
      fail();
    } catch (ActivitiException e) {}
  }
  
  public void testQuerySorting() {
    
    // asc 
    
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().orderByProcessDefinitionId().asc();
    verifyQueryResults(query, 3);
    
    query = repositoryService.createProcessDefinitionQuery().orderByDeploymentId().asc();
    verifyQueryResults(query, 3);
    
    query = repositoryService.createProcessDefinitionQuery().orderByKey().asc();
    verifyQueryResults(query, 3);
    
    query = repositoryService.createProcessDefinitionQuery().orderByVersion().asc();
    verifyQueryResults(query, 3);
    
    // desc
    
    query = repositoryService.createProcessDefinitionQuery().orderByProcessDefinitionId().desc();
    verifyQueryResults(query, 3);
    
    query = repositoryService.createProcessDefinitionQuery().orderByDeploymentId().desc();
    verifyQueryResults(query, 3);
    
    query = repositoryService.createProcessDefinitionQuery().orderByKey().desc();
    verifyQueryResults(query, 3);
    
    query = repositoryService.createProcessDefinitionQuery().orderByVersion().desc();
    verifyQueryResults(query, 3);
    
    // Typical use case
    query = repositoryService.createProcessDefinitionQuery().orderByKey().asc().orderByVersion().desc();
    List<ProcessDefinition> processDefinitions = query.list();
    assertEquals(3, processDefinitions.size());
    
    assertEquals("one", processDefinitions.get(0).getKey());
    assertEquals(2, processDefinitions.get(0).getVersion());
    assertEquals("one", processDefinitions.get(1).getKey());
    assertEquals(1, processDefinitions.get(1).getVersion());
    assertEquals("two", processDefinitions.get(2).getKey());
    assertEquals(1, processDefinitions.get(2).getVersion());
  }
  
  private void verifyQueryResults(ProcessDefinitionQuery query, int countExpected) {
    assertEquals(countExpected, query.list().size());
    assertEquals(countExpected, query.count());
    
    if (countExpected == 1) {
      assertNotNull(query.singleResult());
    } else if (countExpected > 1){
      verifySingleResultFails(query);
    } else if (countExpected == 0) {
      assertNull(query.singleResult());
    }
  }
  
  private void verifySingleResultFails(ProcessDefinitionQuery query) {
    try {
      query.singleResult();
      fail();
    } catch (ActivitiException e) {}
  }
  
}

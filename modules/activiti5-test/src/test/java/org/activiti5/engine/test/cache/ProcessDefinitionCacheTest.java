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

package org.activiti5.engine.test.cache;

import java.util.List;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.activiti.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.activiti.engine.repository.DeploymentProperties;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti5.engine.impl.test.PvmTestCase;


/**
 * Test cases for testing functionality when the process engine is rebooted.
 * 
 * @author Joram Barrez
 */
public class ProcessDefinitionCacheTest extends PvmTestCase {
  
  // Test for a bug: when the process engine is rebooted the
  // cache is cleaned and the deployed process definition is
  // removed from the process cache. This led to problems because
  // the id wasnt fetched from the DB after a redeploy.
  public void testStartProcessInstanceByIdAfterReboot() {
    
    // In case this test is run in a test suite, previous engines might 
    // have been initialized and cached.  First we close the 
    // existing process engines to make sure that the db is clean
    // and that there are no existing process engines involved.
    ProcessEngines.destroy();

    // Creating the DB schema (without building a process engine)
    ProcessEngineConfigurationImpl processEngineConfiguration = new StandaloneInMemProcessEngineConfiguration();
    processEngineConfiguration.setProcessEngineName("reboot-test-schema");
    processEngineConfiguration.setJdbcUrl("jdbc:h2:mem:activiti-reboot-test;DB_CLOSE_DELAY=1000");
    processEngineConfiguration.setActiviti5CompatibilityEnabled(true);
    ProcessEngine schemaProcessEngine = processEngineConfiguration.buildProcessEngine();
    
    // Create process engine and deploy test process
    StandaloneProcessEngineConfiguration standaloneProcessEngineConfiguration = new StandaloneProcessEngineConfiguration();
    standaloneProcessEngineConfiguration.setProcessEngineName("reboot-test");
    standaloneProcessEngineConfiguration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_FALSE);
    standaloneProcessEngineConfiguration.setJdbcUrl("jdbc:h2:mem:activiti-reboot-test;DB_CLOSE_DELAY=1000");
    standaloneProcessEngineConfiguration.setAsyncExecutorActivate(false);
    standaloneProcessEngineConfiguration.setActiviti5CompatibilityEnabled(true);
    ProcessEngine processEngine = standaloneProcessEngineConfiguration.buildProcessEngine();;
     
    processEngine.getRepositoryService()
        .createDeployment()
        .deploymentProperty(DeploymentProperties.DEPLOY_AS_ACTIVITI5_PROCESS_DEFINITION, Boolean.TRUE)
        .addClasspathResource("org/activiti5/engine/test/cache/originalProcess.bpmn20.xml")
        .deploy();
  
    // verify existance of process definiton
    List<ProcessDefinition> processDefinitions = processEngine
        .getRepositoryService()
        .createProcessDefinitionQuery()
        .list();
     
    assertEquals(1, processDefinitions.size());
     
    // Start a new Process instance
    ProcessInstance processInstance = processEngine.getRuntimeService().startProcessInstanceById(processDefinitions.get(0).getId());
    String processInstanceId = processInstance.getId();
    assertNotNull(processInstance);
        
    // Close the process engine
    processEngine.close();
    assertNotNull(processEngine.getRuntimeService());
     
    // Reboot the process engine
    standaloneProcessEngineConfiguration = new StandaloneProcessEngineConfiguration();
    standaloneProcessEngineConfiguration.setProcessEngineName("reboot-test");
    standaloneProcessEngineConfiguration.setDatabaseSchemaUpdate(org.activiti5.engine.ProcessEngineConfiguration.DB_SCHEMA_UPDATE_FALSE);
    standaloneProcessEngineConfiguration.setJdbcUrl("jdbc:h2:mem:activiti-reboot-test;DB_CLOSE_DELAY=1000");
    standaloneProcessEngineConfiguration.setAsyncExecutorActivate(false);
    standaloneProcessEngineConfiguration.setActiviti5CompatibilityEnabled(true);
    processEngine = standaloneProcessEngineConfiguration.buildProcessEngine();
     
    // Check if the existing process instance is still alive
    processInstance = processEngine
        .getRuntimeService()
        .createProcessInstanceQuery()
        .processInstanceId(processInstanceId)
        .singleResult();
     
    assertNotNull(processInstance);
     
    // Complete the task.  That will end the process instance
    TaskService taskService = processEngine.getTaskService();
    Task task = taskService
        .createTaskQuery()
        .list()
        .get(0);
    taskService.complete(task.getId());
     
    // Check if the process instance has really ended.  This means that the process definition has 
    // re-loaded into the process definition cache
    processInstance = processEngine
        .getRuntimeService()
        .createProcessInstanceQuery()
        .processInstanceId(processInstanceId)
        .singleResult();

    assertNull(processInstance);
     
    // Extra check to see if a new process instance can be started as well
    processInstance = processEngine.getRuntimeService().startProcessInstanceById(processDefinitions.get(0).getId());
    assertNotNull(processInstance);

    // close the process engine
    processEngine.close();
      
    // Cleanup schema
    schemaProcessEngine.close();
  }
  
  public void testDeployRevisedProcessAfterDeleteOnOtherProcessEngine() {
    
    // Setup both process engines
    StandaloneProcessEngineConfiguration standaloneProcessEngineConfiguration = new StandaloneProcessEngineConfiguration();
    standaloneProcessEngineConfiguration.setProcessEngineName("reboot-test-schema");
    standaloneProcessEngineConfiguration.setDatabaseSchemaUpdate(org.activiti5.engine.ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
    standaloneProcessEngineConfiguration.setJdbcUrl("jdbc:h2:mem:activiti-process-cache-test;DB_CLOSE_DELAY=1000");
    standaloneProcessEngineConfiguration.setAsyncExecutorActivate(false);
    standaloneProcessEngineConfiguration.setActiviti5CompatibilityEnabled(true);
    ProcessEngine processEngine1 = standaloneProcessEngineConfiguration.buildProcessEngine();
    RepositoryService repositoryService1 = processEngine1.getRepositoryService();
    
    StandaloneProcessEngineConfiguration standaloneProcessEngineConfiguration2 = new StandaloneProcessEngineConfiguration();
    standaloneProcessEngineConfiguration2.setProcessEngineName("reboot-test");
    standaloneProcessEngineConfiguration2.setDatabaseSchemaUpdate(org.activiti5.engine.ProcessEngineConfiguration.DB_SCHEMA_UPDATE_FALSE);
    standaloneProcessEngineConfiguration2.setJdbcUrl("jdbc:h2:mem:activiti-process-cache-test;DB_CLOSE_DELAY=1000");
    standaloneProcessEngineConfiguration2.setAsyncExecutorActivate(false);
    standaloneProcessEngineConfiguration2.setActiviti5CompatibilityEnabled(true);
    ProcessEngine processEngine2  = standaloneProcessEngineConfiguration2.buildProcessEngine();
    RepositoryService repositoryService2 = processEngine2.getRepositoryService();
    RuntimeService runtimeService2 = processEngine2.getRuntimeService();
    TaskService taskService2 = processEngine2.getTaskService();
    
    // Deploy first version of process: start->originalTask->end on first process engine
    String deploymentId = repositoryService1.createDeployment()
      .addClasspathResource("org/activiti5/engine/test/cache/originalProcess.bpmn20.xml")
      .deploymentProperty(DeploymentProperties.DEPLOY_AS_ACTIVITI5_PROCESS_DEFINITION, Boolean.TRUE)
      .deploy()
      .getId();
    
    // Start process instance on second engine
    String processDefinitionId = repositoryService2.createProcessDefinitionQuery().singleResult().getId();
    runtimeService2.startProcessInstanceById(processDefinitionId);
    Task task = taskService2.createTaskQuery().singleResult();
    assertEquals("original task", task.getName());
    
    // Delete the deployment on second process engine
    repositoryService2.deleteDeployment(deploymentId, true);
    assertEquals(0, repositoryService2.createDeploymentQuery().count());
    assertEquals(0, runtimeService2.createProcessInstanceQuery().count());
    
    // deploy a revised version of the process: start->revisedTask->end on first process engine
    //
    // Before the bugfix, this would set the cache on the first process engine,
    // but the second process engine still has the original process definition in his cache.
    // Since there is a deployment delete in between, the new generated process definition id is the same 
    // as in the original deployment, making the second process engine using the old cached process definition.
    deploymentId = repositoryService1.createDeployment()
      .addClasspathResource("org/activiti5/engine/test/cache/revisedProcess.bpmn20.xml")
      .deploymentProperty(DeploymentProperties.DEPLOY_AS_ACTIVITI5_PROCESS_DEFINITION, Boolean.TRUE)
      .deploy()
      .getId();
    
    // Start process instance on second process engine -> must use revised process definition
    repositoryService2.createProcessDefinitionQuery().singleResult().getId();
    runtimeService2.startProcessInstanceByKey("oneTaskProcess");
    task = taskService2.createTaskQuery().singleResult();
    assertEquals("revised task", task.getName());
    
    // cleanup
    repositoryService1.deleteDeployment(deploymentId, true);
    processEngine1.close();
    processEngine2.close();
  }
 
}

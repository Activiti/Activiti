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

package org.activiti.engine.test.db;

import java.util.List;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.activiti.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.activiti.engine.impl.test.AbstractActivitiTestCase;
import org.activiti.engine.impl.test.PvmTestCase;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;


/**
 * Test cases for testing functionality when the process engine is rebooted.
 * 
 * @author Joram Barrez
 */
public class EngineRebootProcessDefinitionCacheTest extends PvmTestCase {
  
  // Test for a bug: when the process engine is rebooted the
  // cache is cleaned and the deployed process definition is
  // removed from the process cache. This led to problems because
  // the id wasnt fetched from the DB after a redeploy.
  public void testStartProcessInstanceByIdAfterReboot() {
    
    // In case this test is run in a test suite, previous engines might 
    // have been initialized and cached.  First we close the 
    // existing process engines to make sure that the db is clean
    // and that there are no existing process engines involved.
    AbstractActivitiTestCase.closeCachedProcessEngines();

    // Creating the DB schema (without building a process engine)
    ProcessEngineConfigurationImpl processEngineConfiguration = new StandaloneInMemProcessEngineConfiguration();
    processEngineConfiguration.setJdbcUrl("jdbc:h2:mem:activiti-reboot-test;DB_CLOSE_DELAY=1000");
    ProcessEngine schemaProcessEngine = processEngineConfiguration.buildProcessEngine();
    
    // Create process engine and deploy test process
     ProcessEngine processEngine = new StandaloneProcessEngineConfiguration()
       .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_FALSE)
       .setJdbcUrl("jdbc:h2:mem:activiti-reboot-test;DB_CLOSE_DELAY=1000")
       .setJobExecutorActivate(false)
       .buildProcessEngine();
     
     processEngine.getRepositoryService()
       .createDeployment()
       .addClasspathResource("org/activiti/engine/test/db/EngineRebootProcessDefinitionCacheTest.bpmn20.xml")
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
     processEngine = new StandaloneProcessEngineConfiguration()
       .setDatabaseSchemaUpdate(org.activiti.engine.ProcessEngineConfiguration.DB_SCHEMA_UPDATE_FALSE)
       .setJdbcUrl("jdbc:h2:mem:activiti-reboot-test;DB_CLOSE_DELAY=1000")
       .setJobExecutorActivate(false)
       .buildProcessEngine();
     
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
}

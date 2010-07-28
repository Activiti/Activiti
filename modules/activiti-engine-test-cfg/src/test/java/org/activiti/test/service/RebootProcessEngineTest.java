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

package org.activiti.test.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.activiti.engine.ProcessEngineBuilder;
import org.activiti.engine.ProcessDefinition;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessInstance;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.test.LogInitializer;
import org.junit.Rule;
import org.junit.Test;


/**
 * Test cases for testing functionality when the process engine is rebooted.
 * 
 * @author Joram Barrez
 */
public class RebootProcessEngineTest {
  
  @Rule
  public LogInitializer logSetup = new LogInitializer();
  
  // Test for a bug: when the process engine is rebooted the
  // cache is cleaned and the deployed process definition is
  // removed from the process cache. This led to problems because
  // the id wasnt fetched from the DB after a redeploy.
  @Test
  public void testStartProcessInstanceByIdAfterReboot() {
    
    // Create process engine and deploy test process
     ProcessEngine processEngine = buildProcessEngine("activiti.properties");
     processEngine.getProcessService()
       .createDeployment()
       .addClasspathResource("org/activiti/test/service/RebootProcessEngineTestProcess.bpmn20.xml")
       .deploy();
  
     // verify existance of process definiton
     List<ProcessDefinition> processDefinitions = processEngine.getProcessService().findProcessDefinitions();
     assertEquals(1, processDefinitions.size());
     
     // Start a new Process instance
     ProcessInstance processInstance = processEngine.getProcessService().startProcessInstanceById(processDefinitions.get(0).getId());
     assertNotNull(processInstance);
        
     // Reboot the process engine
     processEngine.close();
     assertNotNull(processEngine.getProcessService());
     processEngine = buildProcessEngine("activiti.reboot.properties");
        
     // Check if process instances still can be started
     processInstance = processEngine.getProcessService().startProcessInstanceById(processDefinitions.get(0).getId());
     assertNotNull(processInstance);
      
     // Cleanup schema
     // TODO: how to do this through the API?
     ((ProcessEngineImpl) processEngine).getPersistenceSessionFactory().dbSchemaDrop();
   }

  private ProcessEngine buildProcessEngine(String propertyFileName) {
    ProcessEngine processEngine = new ProcessEngineBuilder()
       .configureFromPropertiesResource("org/activiti/test/service/" + propertyFileName)
       .buildProcessEngine();
    return processEngine;
  }

}

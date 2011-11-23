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

import org.activiti.engine.ActivitiException;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.cmd.SetProcessDefinitionVersionCmd;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;


/**
 * @author Falko Menge
 */
public class ProcessInstanceMigrationTest extends PluggableActivitiTestCase {
  
  private static final String TEST_PROCESS = "org/activiti/engine/test/db/ProcessInstanceMigrationTest.testSetProcessDefinitionVersion.bpmn20.xml";
  private static final String TEST_PROCESS_ACTIVITY_MISSING = "org/activiti/engine/test/db/ProcessInstanceMigrationTest.testSetProcessDefinitionVersionActivityMissing.bpmn20.xml";

  public void testSetProcessDefinitionVersionEmptyArguments() {
    try {
      new SetProcessDefinitionVersionCmd(null, "23");    
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("The process instance id is mandatory, but 'null' has been provided.", ae.getMessage());
    }

    try {
      new SetProcessDefinitionVersionCmd("", "23");    
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("The process instance id is mandatory, but '' has been provided.", ae.getMessage());
    }

    try {
      new SetProcessDefinitionVersionCmd("42", null);    
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("The process definition id is mandatory, but 'null' has been provided.", ae.getMessage());
    }

    try {
      new SetProcessDefinitionVersionCmd("42", "");    
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("The process definition id is mandatory, but '' has been provided.", ae.getMessage());
    }
  }

  public void testSetProcessDefinitionVersionNonExistingPI() {
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    try {
      commandExecutor.execute(new SetProcessDefinitionVersionCmd("42", "23"));    
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("No process instance found for id = '42'.", ae.getMessage());
    }
  }
  
  @Deployment(resources = {TEST_PROCESS})
  public void testSetProcessDefinitionVersionNonExistingPD() {
    // start process instance
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("receiveTask");

    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    try {
      commandExecutor.execute(new SetProcessDefinitionVersionCmd(pi.getId(), "23"));    
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("no deployed process definition found with id '23'", ae.getMessage());
    }
  }
  
  @Deployment(resources = {TEST_PROCESS, "org/activiti/engine/test/db/processOne.bpmn20.xml"})
  public void testSetProcessDefinitionVersionDifferentPD() {
    // start process instance
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("receiveTask");

    ProcessDefinition newProcessDefinition = repositoryService
      .createProcessDefinitionQuery()
      .processDefinitionKey("processOne")
      .singleResult();
    
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    try {
      commandExecutor.execute(new SetProcessDefinitionVersionCmd(pi.getId(), newProcessDefinition.getId()));    
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("The key of the new process definition (key = 'processOne') is not equal to that of the process definition (key = 'receiveTask') currently used by the process instance (id = '", ae.getMessage());
    }
  }
  
  @Deployment(resources = {TEST_PROCESS})
  public void testSetProcessDefinitionVersionActivityMissing() {
    // start process instance
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("receiveTask");

    // check that receive task has been reached
    Execution execution = runtimeService.createExecutionQuery()
      .processInstanceId(pi.getId())
      .activityId("waitState1")
      .singleResult();
    assertNotNull(execution);
    
    // deploy new version of the process definition
    org.activiti.engine.repository.Deployment deployment = repositoryService
      .createDeployment()
      .addClasspathResource(TEST_PROCESS_ACTIVITY_MISSING)
      .deploy();
    assertEquals(2, repositoryService.createProcessDefinitionQuery().count());

    // migrate process instance to new process definition version
    ProcessDefinition newProcessDefinition = repositoryService
      .createProcessDefinitionQuery()
      .latestVersion()
      .singleResult();
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    SetProcessDefinitionVersionCmd setProcessDefinitionVersionCmd = new SetProcessDefinitionVersionCmd(pi.getId(), newProcessDefinition.getId());
    try {
      commandExecutor.execute(setProcessDefinitionVersionCmd);
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("The new process definition (key = 'receiveTask') does not contain the current activity (id = 'waitState1') of the process instance (id = '", ae.getMessage());
    }

    // undeploy "manually" deployed process definition
    repositoryService.deleteDeployment(deployment.getId(), true);
  }

  @Deployment
  public void testSetProcessDefinitionVersion() {
    // start process instance
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("receiveTask");

    // check that receive task has been reached
    Execution execution = runtimeService.createExecutionQuery()
      .processInstanceId(pi.getId())
      .activityId("waitState1")
      .singleResult();
    assertNotNull(execution);
    
    // deploy new version of the process definition
    org.activiti.engine.repository.Deployment deployment = repositoryService
      .createDeployment()
      .addClasspathResource(TEST_PROCESS)
      .deploy();
    assertEquals(2, repositoryService.createProcessDefinitionQuery().count());

    // migrate process instance to new process definition version
    ProcessDefinition newProcessDefinition = repositoryService
      .createProcessDefinitionQuery()
      .latestVersion()
      .singleResult();
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    commandExecutor.execute(new SetProcessDefinitionVersionCmd(pi.getId(), newProcessDefinition.getId()));

    // signal process instance
    runtimeService.signal(execution.getId());

    // check that the instance now uses the new process definition version
    pi = runtimeService
      .createProcessInstanceQuery()
      .processInstanceId(pi.getId())
      .singleResult();
    assertEquals(newProcessDefinition.getId(), pi.getProcessDefinitionId());
    
    // check history
    HistoricProcessInstance historicPI = historyService
      .createHistoricProcessInstanceQuery()
      .processInstanceId(pi.getId())
      .singleResult();
    assertEquals(newProcessDefinition.getId(), historicPI.getProcessDefinitionId());

    // undeploy "manually" deployed process definition
    repositoryService.deleteDeployment(deployment.getId(), true);
  }

}

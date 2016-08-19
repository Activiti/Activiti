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

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.cmd.SetProcessDefinitionVersionCmd;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;


/**
 * @author Falko Menge
 * @author Tijs Rademakers
 */
public class ProcessInstanceMigrationTest extends PluggableActivitiTestCase {
  
  private static final String TEST_PROCESS_WITH_PARALLEL_GATEWAY = "org/activiti/examples/bpmn/gateway/ParallelGatewayTest.testForkJoin.bpmn20.xml";
  private static final String TEST_PROCESS = "org/activiti/engine/test/db/ProcessInstanceMigrationTest.testSetProcessDefinitionVersion.bpmn20.xml";
  private static final String TEST_PROCESS_ACTIVITY_MISSING = "org/activiti/engine/test/db/ProcessInstanceMigrationTest.testSetProcessDefinitionVersionActivityMissing.bpmn20.xml";

  private static final String TEST_PROCESS_CALL_ACTIVITY = "org/activiti/engine/test/db/ProcessInstanceMigrationTest.withCallActivity.bpmn20.xml";
  private static final String TEST_PROCESS_USER_TASK_V1 = "org/activiti/engine/test/db/ProcessInstanceMigrationTest.testSetProcessDefinitionVersionWithTask.bpmn20.xml";
  private static final String TEST_PROCESS_USER_TASK_V2 = "org/activiti/engine/test/db/ProcessInstanceMigrationTest.testSetProcessDefinitionVersionWithTaskV2.bpmn20.xml";
  private static final String TEST_PROCESS_NESTED_SUB_EXECUTIONS = "org/activiti/engine/test/db/ProcessInstanceMigrationTest.testSetProcessDefinitionVersionSubExecutionsNested.bpmn20.xml";

  public void testSetProcessDefinitionVersionEmptyArguments() {
    try {
      new SetProcessDefinitionVersionCmd(null, 23);    
      fail("ActivitiException expected");
    } catch (ActivitiIllegalArgumentException ae) {
      assertTextPresent("The process instance id is mandatory, but 'null' has been provided.", ae.getMessage());
    }

    try {
      new SetProcessDefinitionVersionCmd("", 23);    
      fail("ActivitiException expected");
    } catch (ActivitiIllegalArgumentException ae) {
      assertTextPresent("The process instance id is mandatory, but '' has been provided.", ae.getMessage());
    }

    try {
      new SetProcessDefinitionVersionCmd("42", null);    
      fail("ActivitiException expected");
    } catch (ActivitiIllegalArgumentException ae) {
      assertTextPresent("The process definition version is mandatory, but 'null' has been provided.", ae.getMessage());
    }

    try {
      new SetProcessDefinitionVersionCmd("42", -1);    
      fail("ActivitiException expected");
    } catch (ActivitiIllegalArgumentException ae) {
      assertTextPresent("The process definition version must be positive, but '-1' has been provided.", ae.getMessage());
    }
  }

  public void testSetProcessDefinitionVersionNonExistingPI() {
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutor();
    try {
      commandExecutor.execute(new SetProcessDefinitionVersionCmd("42", 23));    
      fail("ActivitiException expected");
    } catch (ActivitiObjectNotFoundException ae) {
      assertTextPresent("No process instance found for id = '42'.", ae.getMessage());
      assertEquals(ProcessInstance.class, ae.getObjectClass());
    }
  }
  
  @Deployment(resources = {TEST_PROCESS_WITH_PARALLEL_GATEWAY})
  public void testSetProcessDefinitionVersionPIIsSubExecution() {
    // start process instance
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("forkJoin");

    Execution execution = runtimeService.createExecutionQuery()
      .activityId("receivePayment")
      .singleResult();
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutor();
    SetProcessDefinitionVersionCmd command = new SetProcessDefinitionVersionCmd(execution.getId(), 1);
    try {
      commandExecutor.execute(command);
      fail("ActivitiException expected");
    } catch (ActivitiException ae) {
      assertTextPresent("A process instance id is required, but the provided id '"+execution.getId()+"' points to a child execution of process instance '"+pi.getId()+"'. Please invoke the "+command.getClass().getSimpleName()+" with a root execution id.", ae.getMessage());
    }
  }

  @Deployment(resources = {TEST_PROCESS})
  public void testSetProcessDefinitionVersionNonExistingPD() {
    // start process instance
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("receiveTask");

    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutor();
    try {
      commandExecutor.execute(new SetProcessDefinitionVersionCmd(pi.getId(), 23));    
      fail("ActivitiException expected");
    } catch (ActivitiObjectNotFoundException ae) {
      assertTextPresent("no processes deployed with key = 'receiveTask' and version = '23'", ae.getMessage());
      assertEquals(ProcessDefinition.class, ae.getObjectClass());
    }
  }
  
  @Deployment(resources = {TEST_PROCESS})
  public void testSetProcessDefinitionVersionActivityMissing() {
    // start process instance
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("receiveTask");

    // check that receive task has been reached
    Execution execution = runtimeService.createExecutionQuery()
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
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutor();
    SetProcessDefinitionVersionCmd setProcessDefinitionVersionCmd = new SetProcessDefinitionVersionCmd(pi.getId(), 2);
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
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutor();
    commandExecutor.execute(new SetProcessDefinitionVersionCmd(pi.getId(), 2));

    // signal process instance
    runtimeService.signal(execution.getId());

    // check that the instance now uses the new process definition version
    ProcessDefinition newProcessDefinition = repositoryService
      .createProcessDefinitionQuery()
      .processDefinitionVersion(2)
      .singleResult();
    
    pi = runtimeService
      .createProcessInstanceQuery()
      .processInstanceId(pi.getId())
      .singleResult();
    assertEquals(newProcessDefinition.getId(), pi.getProcessDefinitionId());
    
    // check history
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      HistoricProcessInstance historicPI = historyService
        .createHistoricProcessInstanceQuery()
        .processInstanceId(pi.getId())
        .singleResult();
      assertEquals(newProcessDefinition.getId(), historicPI.getProcessDefinitionId());
      
      List<HistoricActivityInstance> historicActivities = historyService
          .createHistoricActivityInstanceQuery()
          .processInstanceId(pi.getId())
          .unfinished()
          .list();
      assertEquals(1, historicActivities.size());
      assertEquals(newProcessDefinition.getId(), historicActivities.get(0).getProcessDefinitionId());
    }

    // undeploy "manually" deployed process definition
    repositoryService.deleteDeployment(deployment.getId(), true);
  }

  @Deployment(resources = {TEST_PROCESS_WITH_PARALLEL_GATEWAY})
  public void testSetProcessDefinitionVersionSubExecutions() {
    // start process instance
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("forkJoin");

    // check that the user tasks have been reached
    assertEquals(2, taskService.createTaskQuery().count());
    
    // deploy new version of the process definition
    org.activiti.engine.repository.Deployment deployment = repositoryService
      .createDeployment()
      .addClasspathResource(TEST_PROCESS_WITH_PARALLEL_GATEWAY)
      .deploy();
    assertEquals(2, repositoryService.createProcessDefinitionQuery().count());

    // migrate process instance to new process definition version
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutor();
    commandExecutor.execute(new SetProcessDefinitionVersionCmd(pi.getId(), 2));

    // check that all executions of the instance now use the new process definition version
    ProcessDefinition newProcessDefinition = repositoryService
      .createProcessDefinitionQuery()
      .processDefinitionVersion(2)
      .singleResult();
    List<Execution> executions = runtimeService
      .createExecutionQuery()
      .processInstanceId(pi.getId())
      .list();
    for (Execution execution : executions) {
      assertEquals(newProcessDefinition.getId(), ((ExecutionEntity) execution).getProcessDefinitionId());
    }
    
    // undeploy "manually" deployed process definition
    repositoryService.deleteDeployment(deployment.getId(), true);
  }

  @Deployment(resources = {TEST_PROCESS_CALL_ACTIVITY})
  public void testSetProcessDefinitionVersionWithCallActivity() {
    // start process instance
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("parentProcess");

    // check that receive task has been reached
    Execution execution = runtimeService.createExecutionQuery()
      .activityId("waitState1")
      .processDefinitionKey("childProcess")
      .singleResult();
    assertNotNull(execution);
    
    // deploy new version of the process definition
    org.activiti.engine.repository.Deployment deployment = repositoryService
      .createDeployment()
      .addClasspathResource(TEST_PROCESS_CALL_ACTIVITY)
      .deploy();
    assertEquals(2, repositoryService.createProcessDefinitionQuery().processDefinitionKey("parentProcess").count());

    // migrate process instance to new process definition version
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutor();
    commandExecutor.execute(new SetProcessDefinitionVersionCmd(pi.getId(), 2));

    // signal process instance
    runtimeService.signal(execution.getId());

    // should be finished now
    assertEquals(0, runtimeService.createProcessInstanceQuery().processInstanceId(pi.getId()).count());

    // undeploy "manually" deployed process definition
    repositoryService.deleteDeployment(deployment.getId(), true);
  }
  
  @Deployment(resources = {TEST_PROCESS_USER_TASK_V1})
  public void testSetProcessDefinitionVersionWithWithTask() {
    try {
      // start process instance
      ProcessInstance pi = runtimeService.startProcessInstanceByKey("userTask");
  
      // check that user task has been reached    
      assertEquals(1, taskService.createTaskQuery().processInstanceId(pi.getId()).count());
      
      // deploy new version of the process definition
      org.activiti.engine.repository.Deployment deployment = repositoryService
        .createDeployment()
        .addClasspathResource(TEST_PROCESS_USER_TASK_V2)
        .deploy();
      assertEquals(2, repositoryService.createProcessDefinitionQuery().processDefinitionKey("userTask").count());
      
      ProcessDefinition newProcessDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey("userTask").processDefinitionVersion(2).singleResult();
  
      // migrate process instance to new process definition version
      processEngineConfiguration.getCommandExecutor().execute(new SetProcessDefinitionVersionCmd(pi.getId(), 2));
      
      // check UserTask
      Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
      assertEquals(newProcessDefinition.getId(), task.getProcessDefinitionId());
      assertEquals("testFormKey", formService.getTaskFormData(task.getId()).getFormKey());
      
      if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
        HistoricTaskInstance historicTask = historyService.createHistoricTaskInstanceQuery().processInstanceId(pi.getId()).singleResult();
        assertEquals(newProcessDefinition.getId(), historicTask.getProcessDefinitionId());
        assertEquals("testFormKey", formService.getTaskFormData(historicTask.getId()).getFormKey());
      }
  
      // continue
      taskService.complete(task.getId());
  
      assertProcessEnded(pi.getId());
  
      // undeploy "manually" deployed process definition
      repositoryService.deleteDeployment(deployment.getId(), true);
      
    } catch (Exception ex) {
      fail(ex.getMessage());
    }
  }

  @Deployment(resources = {TEST_PROCESS_NESTED_SUB_EXECUTIONS})
  public void testSetProcessDefinitionVersionSubExecutionsNested() {
    // start process instance
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("forkJoinNested");

    // check that the user tasks have been reached
    assertEquals(2, taskService.createTaskQuery().count());

    // deploy new version of the process definition
    org.activiti.engine.repository.Deployment deployment = repositoryService
            .createDeployment()
            .addClasspathResource(TEST_PROCESS_NESTED_SUB_EXECUTIONS)
            .deploy();
    assertEquals(2, repositoryService.createProcessDefinitionQuery().count());

    // migrate process instance to new process definition version
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutor();
    commandExecutor.execute(new SetProcessDefinitionVersionCmd(pi.getId(), 2));

    // check that all executions of the instance now use the new process definition version
    ProcessDefinition newProcessDefinition = repositoryService
            .createProcessDefinitionQuery()
            .processDefinitionVersion(2)
            .singleResult();
    List<Execution> executions = runtimeService
            .createExecutionQuery()
            .processInstanceId(pi.getId())
            .list();
    for (Execution execution : executions) {
        assertEquals(newProcessDefinition.getId(), ((ExecutionEntity) execution).getProcessDefinitionId());
    }

    // undeploy "manually" deployed process definition
    repositoryService.deleteDeployment(deployment.getId(), true);
  }
  
}

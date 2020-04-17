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

package org.activiti.engine.impl.test;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.DynamicBpmnService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandConfig;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;

import junit.framework.AssertionFailedError;


public abstract class AbstractActivitiTestCase extends AbstractTestCase {

  private static final List<String> TABLENAMES_EXCLUDED_FROM_DB_CLEAN_CHECK = singletonList("ACT_GE_PROPERTY");

  protected ProcessEngine processEngine;

  protected String deploymentIdFromDeploymentAnnotation;
  protected List<String> deploymentIdsForAutoCleanup = new ArrayList<String>();
  protected Throwable exception;

  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected RepositoryService repositoryService;
  protected RuntimeService runtimeService;
  protected TaskService taskService;
  protected HistoryService historyService;
  protected ManagementService managementService;
  protected DynamicBpmnService dynamicBpmnService;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    // Always reset authenticated user to avoid any mistakes
    Authentication.setAuthenticatedUserId(null);
  }

  protected abstract void initializeProcessEngine();

  // Default: do nothing
  protected void closeDownProcessEngine() {
  }

  @Override
  public void runBare() throws Throwable {
    initializeProcessEngine();
    if (repositoryService == null) {
      initializeServices();
    }

    try {

      deploymentIdFromDeploymentAnnotation = TestHelper.annotationDeploymentSetUp(processEngine, getClass(), getName());

      super.runBare();

      validateHistoryData();

    } catch (AssertionFailedError e) {
      log.error(EMPTY_LINE);
      log.error("ASSERTION FAILED: {}", e, e);
      exception = e;
      throw e;

    } catch (Throwable e) {
      log.error(EMPTY_LINE);
      log.error("EXCEPTION: {}", e, e);
      exception = e;
      throw e;

    } finally {

      if (deploymentIdFromDeploymentAnnotation != null) {
        TestHelper.annotationDeploymentTearDown(processEngine, deploymentIdFromDeploymentAnnotation, getClass(), getName());
        deploymentIdFromDeploymentAnnotation = null;
      }

      for (String autoDeletedDeploymentId : deploymentIdsForAutoCleanup) {
        repositoryService.deleteDeployment(autoDeletedDeploymentId, true);
      }
      deploymentIdsForAutoCleanup.clear();

      assertAndEnsureCleanDb();
      processEngineConfiguration.getClock().reset();

      // Can't do this in the teardown, as the teardown will be called as part of the super.runBare
      closeDownProcessEngine();
    }
  }

  protected void validateHistoryData() {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {

      List<HistoricProcessInstance> historicProcessInstances =
          historyService.createHistoricProcessInstanceQuery().finished().list();

      for (HistoricProcessInstance historicProcessInstance : historicProcessInstances) {

        assertThat(historicProcessInstance.getProcessDefinitionId()).as("Historic process instance has no process definition id").isNotNull();
        assertThat(historicProcessInstance.getProcessDefinitionKey()).as("Historic process instance has no process definition key").isNotNull();
        assertThat(historicProcessInstance.getProcessDefinitionVersion()).as("Historic process instance has no process definition version").isNotNull();
        assertThat(historicProcessInstance.getDeploymentId()).as("Historic process instance has no process definition key").isNotNull();
        assertThat(historicProcessInstance.getStartActivityId()).as("Historic process instance has no start activiti id").isNotNull();
        assertThat(historicProcessInstance.getStartTime()).as("Historic process instance has no start time").isNotNull();
        assertThat(historicProcessInstance.getEndTime()).as("Historic process instance has no end time").isNotNull();

        String processInstanceId = historicProcessInstance.getId();

        // tasks
        List<HistoricTaskInstance> historicTaskInstances = historyService.createHistoricTaskInstanceQuery()
            .processInstanceId(processInstanceId).list();
        if (historicTaskInstances != null && historicTaskInstances.size() > 0) {
          for (HistoricTaskInstance historicTaskInstance : historicTaskInstances) {
            assertThat(historicTaskInstance.getProcessInstanceId()).isEqualTo(processInstanceId);
            if (historicTaskInstance.getClaimTime() != null) {
              assertThat(historicTaskInstance.getWorkTimeInMillis()).as("Historic task " + historicTaskInstance.getTaskDefinitionKey() + " has no work time").isNotNull();
            }
            assertThat(historicTaskInstance.getId()).as("Historic task " + historicTaskInstance.getTaskDefinitionKey() + " has no id").isNotNull();
            assertThat(historicTaskInstance.getProcessInstanceId()).as("Historic task " + historicTaskInstance.getTaskDefinitionKey() + " has no process instance id").isNotNull();
            assertThat(historicTaskInstance.getExecutionId()).as("Historic task " + historicTaskInstance.getTaskDefinitionKey() + " has no execution id").isNotNull();
            assertThat(historicTaskInstance.getProcessDefinitionId()).as("Historic task " + historicTaskInstance.getTaskDefinitionKey() + " has no process definition id").isNotNull();
            assertThat(historicTaskInstance.getTaskDefinitionKey()).as("Historic task " + historicTaskInstance.getTaskDefinitionKey() + " has no task definition key").isNotNull();
            assertThat(historicTaskInstance.getCreateTime()).as("Historic task " + historicTaskInstance.getTaskDefinitionKey() + " has no create time").isNotNull();
            assertThat(historicTaskInstance.getStartTime()).as("Historic task " + historicTaskInstance.getTaskDefinitionKey() + " has no start time").isNotNull();
            assertThat(historicTaskInstance.getEndTime()).as("Historic task " + historicTaskInstance.getTaskDefinitionKey() + " has no end time").isNotNull();
          }
        }

        // activities
        List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery()
            .processInstanceId(processInstanceId).list();
        if (historicActivityInstances != null && historicActivityInstances.size() > 0) {
          for (HistoricActivityInstance historicActivityInstance : historicActivityInstances) {
            assertThat(historicActivityInstance.getProcessInstanceId()).isEqualTo(processInstanceId);
            assertThat(historicActivityInstance.getActivityId()).as("Historic activity instance " + historicActivityInstance.getActivityId() + " has no activity id").isNotNull();
            assertThat(historicActivityInstance.getActivityType()).as("Historic activity instance " + historicActivityInstance.getActivityId() + " has no activity type").isNotNull();
            assertThat(historicActivityInstance.getProcessDefinitionId()).as("Historic activity instance " + historicActivityInstance.getActivityId() + " has no process definition id").isNotNull();
            assertThat(historicActivityInstance.getProcessInstanceId()).as("Historic activity instance " + historicActivityInstance.getActivityId() + " has no process instance id").isNotNull();
            assertThat(historicActivityInstance.getExecutionId()).as("Historic activity instance " + historicActivityInstance.getActivityId() + " has no execution id").isNotNull();
            assertThat(historicActivityInstance.getStartTime()).as("Historic activity instance " + historicActivityInstance.getActivityId() + " has no start time").isNotNull();
            assertThat(historicActivityInstance.getEndTime()).as("Historic activity instance " + historicActivityInstance.getActivityId() + " has no end time").isNotNull();
          }
        }
      }

    }
  }

  /**
   * Each test is assumed to clean up all DB content it entered. After a test method executed, this method scans all tables to see if the DB is completely clean. It throws AssertionFailed in case the
   * DB is not clean. If the DB is not clean, it is cleaned by performing a create a drop.
   */
  protected void assertAndEnsureCleanDb() throws Throwable {
    log.debug("verifying that db is clean after test");
    Map<String, Long> tableCounts = managementService.getTableCount();
    StringBuilder outputMessage = new StringBuilder();
    for (String tableName : tableCounts.keySet()) {
      String tableNameWithoutPrefix = tableName.replace(processEngineConfiguration.getDatabaseTablePrefix(), "");
      if (!TABLENAMES_EXCLUDED_FROM_DB_CLEAN_CHECK.contains(tableNameWithoutPrefix)) {
        Long count = tableCounts.get(tableName);
        if (count != 0L) {
          outputMessage.append("  ").append(tableName).append(": ").append(count).append(" record(s) ");
        }
      }
    }
    if (outputMessage.length() > 0) {
      outputMessage.insert(0, "DB NOT CLEAN: \n");
      log.error(EMPTY_LINE);
      log.error(outputMessage.toString());

      log.info("dropping and recreating db");

      CommandExecutor commandExecutor = ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration().getCommandExecutor();
      CommandConfig config = new CommandConfig().transactionNotSupported();
      commandExecutor.execute(config, new Command<Object>() {
        public Object execute(CommandContext commandContext) {
          DbSqlSession session = commandContext.getDbSqlSession();
          session.dbSchemaDrop();
          session.dbSchemaCreate();
          return null;
        }
      });

      if (exception != null) {
        throw exception;
      } else {
        fail(outputMessage.toString());
      }
    } else {
      log.info("database was clean");
    }
  }

  protected void initializeServices() {
    processEngineConfiguration = ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration();
    repositoryService = processEngine.getRepositoryService();
    runtimeService = processEngine.getRuntimeService();
    taskService = processEngine.getTaskService();
    historyService = processEngine.getHistoryService();
    managementService = processEngine.getManagementService();
    dynamicBpmnService = processEngine.getDynamicBpmnService();
  }

  public void assertProcessEnded(final String processInstanceId) {
    ProcessInstance processInstance = processEngine.getRuntimeService().createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();

    if (processInstance != null) {
      throw new AssertionFailedError("Expected finished process instance '" + processInstanceId + "' but it was still in the db");
    }

    // Verify historical data if end times are correctly set
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {

      // process instance
      HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
      assertThat(historicProcessInstance.getId()).isEqualTo(processInstanceId);
      assertThat(historicProcessInstance.getStartTime()).as("Historic process instance has no start time").isNotNull();
      assertThat(historicProcessInstance.getEndTime()).as("Historic process instance has no end time").isNotNull();

      // tasks
      List<HistoricTaskInstance> historicTaskInstances = historyService.createHistoricTaskInstanceQuery()
          .processInstanceId(processInstanceId).list();
      if (historicTaskInstances != null && historicTaskInstances.size() > 0) {
        for (HistoricTaskInstance historicTaskInstance : historicTaskInstances) {
          assertThat(historicTaskInstance.getProcessInstanceId()).isEqualTo(processInstanceId);
          assertThat(historicTaskInstance.getStartTime()).as("Historic task " + historicTaskInstance.getTaskDefinitionKey() + " has no start time").isNotNull();
          assertThat(historicTaskInstance.getEndTime()).as("Historic task " + historicTaskInstance.getTaskDefinitionKey() + " has no end time").isNotNull();
        }
      }

      // activities
      List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstanceId).list();
      if (historicActivityInstances != null && historicActivityInstances.size() > 0) {
        for (HistoricActivityInstance historicActivityInstance : historicActivityInstances) {
          assertThat(historicActivityInstance.getProcessInstanceId()).isEqualTo(processInstanceId);
          assertThat(historicActivityInstance.getStartTime()).as("Historic activity instance " + historicActivityInstance.getActivityId() + " has no start time").isNotNull();
          assertThat(historicActivityInstance.getEndTime()).as("Historic activity instance " + historicActivityInstance.getActivityId() + " has no end time").isNotNull();
        }
      }
    }
  }

  public void waitForJobExecutorToProcessAllJobs(long maxMillisToWait, long intervalMillis) {
    JobTestHelper.waitForJobExecutorToProcessAllJobs(processEngineConfiguration, managementService, maxMillisToWait, intervalMillis);
  }

  public void waitForJobExecutorOnCondition(long maxMillisToWait, long intervalMillis, Callable<Boolean> condition) {
    JobTestHelper.waitForJobExecutorOnCondition(processEngineConfiguration, maxMillisToWait, intervalMillis, condition);
  }

  public void executeJobExecutorForTime(long maxMillisToWait, long intervalMillis) {
    JobTestHelper.executeJobExecutorForTime(processEngineConfiguration, maxMillisToWait, intervalMillis);
  }

  public void waitForJobExecutorToProcessAllJobsAndExecutableTimerJobs(long maxMillisToWait, long intervalMillis) {
    JobTestHelper.waitForJobExecutorToProcessAllJobsAndExecutableTimerJobs(processEngineConfiguration, managementService, maxMillisToWait, intervalMillis);
  }

  /**
   * Since the 'one task process' is used everywhere the actual process content doesn't matter, instead of copying around the BPMN 2.0 xml one could use this methodwhich gives a {@link BpmnModel}
   * version of the same process back.
   */
  public BpmnModel createOneTaskTestProcess() {
    BpmnModel model = new BpmnModel();
    org.activiti.bpmn.model.Process process = new org.activiti.bpmn.model.Process();
    model.addProcess(process);
    process.setId("oneTaskProcess");
    process.setName("The one task process");

    StartEvent startEvent = new StartEvent();
    startEvent.setId("start");
    process.addFlowElement(startEvent);

    UserTask userTask = new UserTask();
    userTask.setName("The Task");
    userTask.setId("theTask");
    userTask.setAssignee("kermit");
    process.addFlowElement(userTask);

    EndEvent endEvent = new EndEvent();
    endEvent.setId("theEnd");
    process.addFlowElement(endEvent);

    process.addFlowElement(new SequenceFlow("start", "theTask"));
    process.addFlowElement(new SequenceFlow("theTask", "theEnd"));

    return model;
  }

  public BpmnModel createTwoTasksTestProcess() {
    BpmnModel model = new BpmnModel();
    org.activiti.bpmn.model.Process process = new org.activiti.bpmn.model.Process();
    model.addProcess(process);
    process.setId("twoTasksProcess");
    process.setName("The two tasks process");

    StartEvent startEvent = new StartEvent();
    startEvent.setId("start");
    process.addFlowElement(startEvent);

    UserTask userTask = new UserTask();
    userTask.setName("The First Task");
    userTask.setId("task1");
    userTask.setAssignee("kermit");
    process.addFlowElement(userTask);

    UserTask userTask2 = new UserTask();
    userTask2.setName("The Second Task");
    userTask2.setId("task2");
    userTask2.setAssignee("kermit");
    process.addFlowElement(userTask2);

    EndEvent endEvent = new EndEvent();
    endEvent.setId("theEnd");
    process.addFlowElement(endEvent);

    process.addFlowElement(new SequenceFlow("start", "task1"));
    process.addFlowElement(new SequenceFlow("start", "task2"));
    process.addFlowElement(new SequenceFlow("task1", "theEnd"));
    process.addFlowElement(new SequenceFlow("task2", "theEnd"));

    return model;
  }

  /**
   * Creates and deploys the one task process. See {@link #createOneTaskTestProcess()}.
   *
   * @return The process definition id (NOT the process definition key) of deployed one task process.
   */
  public String deployOneTaskTestProcess() {
    BpmnModel bpmnModel = createOneTaskTestProcess();
    Deployment deployment = repositoryService.createDeployment().addBpmnModel("oneTasktest.bpmn20.xml", bpmnModel).deploy();

    deploymentIdsForAutoCleanup.add(deployment.getId()); // For auto-cleanup

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId()).singleResult();
    return processDefinition.getId();
  }

  public String deployTwoTasksTestProcess() {
    BpmnModel bpmnModel = createTwoTasksTestProcess();
    Deployment deployment = repositoryService.createDeployment().addBpmnModel("twoTasksTestProcess.bpmn20.xml", bpmnModel).deploy();

    deploymentIdsForAutoCleanup.add(deployment.getId()); // For auto-cleanup

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId()).singleResult();
    return processDefinition.getId();
  }

  //
  // HELPERS
  //

  protected void assertHistoricTasksDeleteReason(ProcessInstance processInstance, String expectedDeleteReason, String ... taskNames) {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
      for (String taskName : taskNames) {
        List<HistoricTaskInstance> historicTaskInstances = historyService.createHistoricTaskInstanceQuery()
            .processInstanceId(processInstance.getId()).taskName(taskName).list();
        assertThat(historicTaskInstances.size() > 0).isTrue();
        for (HistoricTaskInstance historicTaskInstance : historicTaskInstances) {
          assertThat(historicTaskInstance.getEndTime()).isNotNull();
          if (expectedDeleteReason == null) {
            assertThat(historicTaskInstance.getDeleteReason()).isNull();
          } else {
            assertThat(historicTaskInstance.getDeleteReason().startsWith(expectedDeleteReason)).isTrue();
          }
        }
      }
    }
  }

  protected void assertHistoricActivitiesDeleteReason(ProcessInstance processInstance, String expectedDeleteReason, String ... activityIds) {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {
      for (String activityId : activityIds) {
        List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery()
            .activityId(activityId).processInstanceId(processInstance.getId()).list();
        assertThat(historicActivityInstances).as("Could not find historic activities").isNotEmpty();
        for (HistoricActivityInstance historicActivityInstance : historicActivityInstances) {
          assertThat(historicActivityInstance.getEndTime()).isNotNull();
          if (expectedDeleteReason == null) {
            assertThat(historicActivityInstance.getDeleteReason()).isNull();
          } else {
            assertThat(historicActivityInstance.getDeleteReason().startsWith(expectedDeleteReason)).isTrue();
          }
        }
      }
    }
  }

}

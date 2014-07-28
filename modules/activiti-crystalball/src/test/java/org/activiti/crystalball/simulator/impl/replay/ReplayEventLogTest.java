package org.activiti.crystalball.simulator.impl.replay;

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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.activiti.crystalball.simulator.ReplaySimulationRun;
import org.activiti.crystalball.simulator.SimpleEventCalendar;
import org.activiti.crystalball.simulator.SimulationDebugger;
import org.activiti.crystalball.simulator.SimulationEvent;
import org.activiti.crystalball.simulator.SimulationEventComparator;
import org.activiti.crystalball.simulator.SimulationEventHandler;
import org.activiti.crystalball.simulator.delegate.event.Function;
import org.activiti.crystalball.simulator.delegate.event.impl.EventLogProcessInstanceCreateTransformer;
import org.activiti.crystalball.simulator.delegate.event.impl.EventLogTransformer;
import org.activiti.crystalball.simulator.delegate.event.impl.EventLogUserTaskCompleteTransformer;
import org.activiti.crystalball.simulator.impl.StartReplayLogEventHandler;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.event.EventLogEntry;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.el.NoExecutionVariableScope;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;

/**
 * @author martin.grofcik
 */
public class ReplayEventLogTest {

  // Process instance start event
  private static final String PROCESS_INSTANCE_START_EVENT_TYPE = "PROCESS_INSTANCE_START";
  private static final String PROCESS_DEFINITION_ID_KEY = "processDefinitionId";
  private static final String VARIABLES_KEY = "variables";
  // User task completed event
  private static final String USER_TASK_COMPLETED_EVENT_TYPE = "USER_TASK_COMPLETED";

  private static final String USERTASK_PROCESS = "oneTaskProcess";
  private static final String BUSINESS_KEY = "testBusinessKey";
  private static final String TEST_VALUE = "TestValue";
  private static final String TEST_VARIABLE = "testVariable";
  private static final String TASK_TEST_VALUE = "TaskTestValue";
  private static final String TASK_TEST_VARIABLE = "taskTestVariable";

  private static final String THE_USERTASK_PROCESS = "org/activiti/crystalball/simulator/impl/playback/PlaybackProcessStartTest.testUserTask.bpmn20.xml";

  @Test
  public void testProcessInstanceStartEvents() throws Exception {
    ProcessEngineImpl processEngine = initProcessEngine();

    TaskService taskService = processEngine.getTaskService();
    RuntimeService runtimeService = processEngine.getRuntimeService();
    ManagementService managementService = processEngine.getManagementService();
    HistoryService historyService = processEngine.getHistoryService();

    // record events
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put(TEST_VARIABLE, TEST_VALUE);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(USERTASK_PROCESS, BUSINESS_KEY, variables);

    Task task = taskService.createTaskQuery().taskDefinitionKey("userTask").singleResult();
    TimeUnit.MILLISECONDS.sleep(50);
    variables = new HashMap<String, Object>();
    variables.put(TASK_TEST_VARIABLE, TASK_TEST_VALUE);
    taskService.complete(task.getId(), variables);

    // transform log events
    List<EventLogEntry> eventLogEntries = managementService.getEventLogEntries(null, null);

    EventLogTransformer transformer = new EventLogTransformer(getTransformers());

    List<SimulationEvent> simulationEvents = transformer.transform(eventLogEntries);

    SimpleEventCalendar eventCalendar = new SimpleEventCalendar(processEngine.getProcessEngineConfiguration().getClock(), new SimulationEventComparator());
    eventCalendar.addEvents(simulationEvents);

    // replay process instance run
    final SimulationDebugger simRun = new ReplaySimulationRun(processEngine, eventCalendar, getReplayHandlers(processInstance.getId()));

    simRun.init(new NoExecutionVariableScope());

    // original process is finished - there should not be any running process instance/task
    assertEquals(0, runtimeService.createProcessInstanceQuery().processDefinitionKey(USERTASK_PROCESS).count());
    assertEquals(0, taskService.createTaskQuery().taskDefinitionKey("userTask").count());

    simRun.step();

    // replay process was started
    ProcessInstance replayProcessInstance = runtimeService.createProcessInstanceQuery()
        .processDefinitionKey(USERTASK_PROCESS)
        .singleResult();
    assertNotNull(replayProcessInstance);
    assertTrue(replayProcessInstance.getId().equals(processInstance.getId()) == false);
    assertEquals(TEST_VALUE, runtimeService.getVariable(replayProcessInstance.getId(), TEST_VARIABLE));
    // there should be one task
    assertEquals(1, taskService.createTaskQuery().taskDefinitionKey("userTask").count());

    simRun.step();

    // userTask was completed - replay process was finished
    assertEquals(0, runtimeService.createProcessInstanceQuery().processDefinitionKey(USERTASK_PROCESS).count());
    assertEquals(0, taskService.createTaskQuery().taskDefinitionKey("userTask").count());
    HistoricVariableInstance variableInstance = historyService.createHistoricVariableInstanceQuery()
        .processInstanceId(replayProcessInstance.getId())
        .variableName(TASK_TEST_VARIABLE)
        .singleResult();
    assertNotNull(variableInstance);
    assertEquals(TASK_TEST_VALUE, variableInstance.getValue());

    // close simulation
    simRun.close();
    processEngine.close();
    ProcessEngines.destroy();
  }

  private ProcessEngineImpl initProcessEngine() {
    ProcessEngineConfigurationImpl configuration = getProcessEngineConfiguration();
    ProcessEngineImpl processEngine = (ProcessEngineImpl) configuration.buildProcessEngine();

    processEngine.getRepositoryService().
        createDeployment().
        addClasspathResource(THE_USERTASK_PROCESS).
        deploy();
    return processEngine;
  }

  private ProcessEngineConfigurationImpl getProcessEngineConfiguration() {
    ProcessEngineConfigurationImpl configuration = new org.activiti.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration();
    configuration.
      setEnableDatabaseEventLogging(true).
      setDatabaseSchemaUpdate("create-drop").
      setJobExecutorActivate(false);
    return configuration;
  }

  private static List<Function<EventLogEntry, SimulationEvent>> getTransformers() {
    List<Function<EventLogEntry, SimulationEvent>> transformers = new ArrayList<Function<EventLogEntry, SimulationEvent>>();
    transformers.add(new EventLogProcessInstanceCreateTransformer(PROCESS_INSTANCE_START_EVENT_TYPE, PROCESS_DEFINITION_ID_KEY, BUSINESS_KEY, VARIABLES_KEY));
    transformers.add(new EventLogUserTaskCompleteTransformer(USER_TASK_COMPLETED_EVENT_TYPE));
    return transformers;
  }

  public static Map<String, SimulationEventHandler> getReplayHandlers(String processInstanceId) {
    Map<String, SimulationEventHandler> handlers = new HashMap<String, SimulationEventHandler>();
    handlers.put(PROCESS_INSTANCE_START_EVENT_TYPE, new StartReplayLogEventHandler(processInstanceId, PROCESS_DEFINITION_ID_KEY, BUSINESS_KEY, VARIABLES_KEY));
    handlers.put(USER_TASK_COMPLETED_EVENT_TYPE, new ReplayUserTaskCompleteEventHandler());
    return handlers;
  }
}
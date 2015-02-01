package org.activiti.crystalball.simulator;

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


import org.activiti.crystalball.simulator.delegate.event.Function;
import org.activiti.crystalball.simulator.delegate.event.impl.DeploymentCreateTransformer;
import org.activiti.crystalball.simulator.delegate.event.impl.InMemoryRecordActivitiEventListener;
import org.activiti.crystalball.simulator.delegate.event.impl.ProcessInstanceCreateTransformer;
import org.activiti.crystalball.simulator.delegate.event.impl.UserTaskCompleteTransformer;
import org.activiti.crystalball.simulator.impl.*;
import org.activiti.crystalball.simulator.impl.clock.DefaultClockFactory;
import org.activiti.crystalball.simulator.impl.clock.ThreadLocalClock;
import org.activiti.crystalball.simulator.impl.playback.PlaybackUserTaskCompleteEventHandler;
import org.activiti.engine.*;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.el.NoExecutionVariableScope;
import org.activiti.engine.impl.util.DefaultClockImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.Clock;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * @author martin.grofcik
 */
public class SimpleSimulationRunTest {
  //deployment created
  private static final String DEPLOYMENT_CREATED_EVENT_TYPE = "DEPLOYMENT_CREATED_EVENT";
  private static final String DEPLOYMENT_RESOURCES_KEY = "deploymentResources";

  // Process instance start event
  private static final String PROCESS_INSTANCE_START_EVENT_TYPE = "PROCESS_INSTANCE_START";
  private static final String PROCESS_DEFINITION_ID_KEY = "processDefinitionId";
  private static final String VARIABLES_KEY = "variables";
  // User task completed event
  private static final String USER_TASK_COMPLETED_EVENT_TYPE = "USER_TASK_COMPLETED";

  private static final String BUSINESS_KEY = "testBusinessKey";

  public static final String TEST_VALUE = "TestValue";
  public static final String TEST_VARIABLE = "testVariable";

  private static final String USERTASK_PROCESS = "org/activiti/crystalball/simulator/impl/playback/PlaybackProcessStartTest.testUserTask.bpmn20.xml";

  protected InMemoryRecordActivitiEventListener listener;

  @Before
  public void initListener() {
    listener = new InMemoryRecordActivitiEventListener(getTransformers());
  }

  @After
  public void cleanupListener() {
    listener = null;
  }

  @Test
  public void testStep() throws Exception {

    recordEvents();

    SimulationDebugger simDebugger = createDebugger();

    simDebugger.init(new NoExecutionVariableScope());

    RuntimeService runtimeService = SimulationRunContext.getRuntimeService();
    TaskService taskService = SimulationRunContext.getTaskService();
    HistoryService historyService = SimulationRunContext.getHistoryService();

    // debuger step - deploy processDefinition
    simDebugger.step();
    step0Check(SimulationRunContext.getRepositoryService());

    // debuger step - start process and stay on the userTask
    simDebugger.step();
    step1Check(runtimeService, taskService);

    // debugger step - complete userTask and finish process
    simDebugger.step();
    step2Check(runtimeService, taskService);

    checkStatus(historyService);

    simDebugger.close();
    ProcessEngines.destroy();
  }

  private void step2Check(RuntimeService runtimeService, TaskService taskService) {ProcessInstance procInstance = runtimeService.createProcessInstanceQuery().active().processInstanceBusinessKey("oneTaskProcessBusinessKey").singleResult();
    assertNull(procInstance);
    Task t = taskService.createTaskQuery().active().taskDefinitionKey("userTask").singleResult();
    assertNull(t);
  }

  @Test
  public void testRunToTime() throws Exception {

    recordEvents();

    SimulationDebugger simDebugger = createDebugger();

    simDebugger.init(new NoExecutionVariableScope());

    RuntimeService runtimeService = SimulationRunContext.getRuntimeService();
    TaskService taskService = SimulationRunContext.getTaskService();
    HistoryService historyService = SimulationRunContext.getHistoryService();

    simDebugger.runTo(0);
    ProcessInstance procInstance = runtimeService.createProcessInstanceQuery().active().processInstanceBusinessKey("oneTaskProcessBusinessKey").singleResult();
    assertNull(procInstance);

    // debuger step - deploy process
    simDebugger.runTo(1);
    step0Check(SimulationRunContext.getRepositoryService());

    // debuger step - start process and stay on the userTask
    simDebugger.runTo(1001);
    step1Check(runtimeService, taskService);

    // process engine should be in the same state as before
    simDebugger.runTo(2000);
    step1Check(runtimeService, taskService);

    // debugger step - complete userTask and finish process
    simDebugger.runTo(2500);
    step2Check(runtimeService, taskService);

    checkStatus(historyService);

    simDebugger.close();
    ProcessEngines.destroy();
  }

  @Test(expected = RuntimeException.class )
  public void testRunToTimeInThePast() throws Exception {

    recordEvents();
    SimulationDebugger simDebugger = createDebugger();
    simDebugger.init(new NoExecutionVariableScope());
    try {
      simDebugger.runTo(-1);
      fail("RuntimeException expected - unable to execute event from the past");
    } finally {
      simDebugger.close();
      ProcessEngines.destroy();
    }
  }

  @Test
  public void testRunToEvent() throws Exception {

    recordEvents();
    SimulationDebugger simDebugger = createDebugger();
    simDebugger.init(new NoExecutionVariableScope());
    try {
      simDebugger.runTo(USER_TASK_COMPLETED_EVENT_TYPE);
      step1Check(SimulationRunContext.getRuntimeService(), SimulationRunContext.getTaskService());
      simDebugger.runContinue();
    } finally {
      simDebugger.close();
      ProcessEngines.destroy();
    }
  }

  @Test(expected = RuntimeException.class)
  public void testRunToNonExistingEvent() throws Exception {

    recordEvents();
    SimulationDebugger simDebugger = createDebugger();
    simDebugger.init(new NoExecutionVariableScope());
    try {
      simDebugger.runTo("");
      checkStatus(SimulationRunContext.getHistoryService());
    } finally {
      simDebugger.close();
      ProcessEngines.destroy();
    }
  }

  private void step0Check(RepositoryService repositoryService) {
    Deployment deployment;
    deployment = repositoryService.createDeploymentQuery().singleResult();
    assertNotNull(deployment);
  }

  private void step1Check(RuntimeService runtimeService, TaskService taskService) {ProcessInstance procInstance;
    procInstance = runtimeService.createProcessInstanceQuery().active().processInstanceBusinessKey("oneTaskProcessBusinessKey").singleResult();
    assertNotNull(procInstance);
    Task t = taskService.createTaskQuery().active().taskDefinitionKey("userTask").singleResult();
    assertNotNull(t);
  }


  @Test
  public void testRunContinue() throws Exception {
    recordEvents();

    SimulationDebugger simDebugger = createDebugger();

    simDebugger.init(new NoExecutionVariableScope());
    simDebugger.runContinue();
    checkStatus(SimulationRunContext.getHistoryService());

    simDebugger.close();
    ProcessEngines.destroy();
  }

  private SimulationDebugger createDebugger() {
    final SimpleSimulationRun.Builder builder = new SimpleSimulationRun.Builder();
    // init simulation run
    Clock clock = new ThreadLocalClock(new DefaultClockFactory());
    ProcessEngineConfigurationImpl config = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration.createProcessEngineConfigurationFromResourceDefault();
    config.setClock(clock);

    SimulationProcessEngineFactory simulationProcessEngineFactory = new SimulationProcessEngineFactory(config);
    builder.processEngine(simulationProcessEngineFactory.getObject())
      .eventCalendar((new SimpleEventCalendarFactory(clock, new SimulationEventComparator(), listener.getSimulationEvents())).getObject())
      .eventHandlers(getHandlers());
    return builder.build();
  }

  private void checkStatus(HistoryService historyService) {
    final HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().
      finished().
      singleResult();
    assertNotNull(historicProcessInstance);
    assertEquals("oneTaskProcessBusinessKey", historicProcessInstance.getBusinessKey());
    HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().taskDefinitionKey("userTask").singleResult();
    assertEquals("user1", historicTaskInstance.getAssignee());
  }

  private void recordEvents() {
    Clock clock = new DefaultClockImpl();
    clock.setCurrentTime(new Date(0));
    ProcessEngineConfigurationImpl config = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration.createProcessEngineConfigurationFromResourceDefault();
    config.setClock(clock);

    ProcessEngine processEngine = (new RecordableProcessEngineFactory(config, listener))
      .getObject();

    processEngine.getRepositoryService().createDeployment().
      addClasspathResource(USERTASK_PROCESS).
      deploy();
    EventRecorderTestUtils.increaseTime(clock);

    TaskService taskService = processEngine.getTaskService();

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put(TEST_VARIABLE, TEST_VALUE);
    processEngine.getRuntimeService().startProcessInstanceByKey("oneTaskProcess", "oneTaskProcessBusinessKey", variables);
    EventRecorderTestUtils.increaseTime(clock);
    Task task = taskService.createTaskQuery().taskDefinitionKey("userTask").singleResult();
    taskService.complete(task.getId());
    checkStatus(processEngine.getHistoryService());
    EventRecorderTestUtils.closeProcessEngine(processEngine, listener);
    ProcessEngines.destroy();
  }

  private List<Function<ActivitiEvent, SimulationEvent>> getTransformers() {
    List<Function<ActivitiEvent, SimulationEvent>> transformers = new ArrayList<Function<ActivitiEvent, SimulationEvent>>();
    transformers.add(new DeploymentCreateTransformer(DEPLOYMENT_CREATED_EVENT_TYPE, DEPLOYMENT_RESOURCES_KEY));
    transformers.add(new ProcessInstanceCreateTransformer(PROCESS_INSTANCE_START_EVENT_TYPE, PROCESS_DEFINITION_ID_KEY, BUSINESS_KEY, VARIABLES_KEY));
    transformers.add(new UserTaskCompleteTransformer(USER_TASK_COMPLETED_EVENT_TYPE));
    return transformers;
  }

  public static Map<String, SimulationEventHandler> getHandlers() {
    Map<String, SimulationEventHandler> handlers = new HashMap<String, SimulationEventHandler>();
    handlers.put(DEPLOYMENT_CREATED_EVENT_TYPE, new DeployResourcesEventHandler(DEPLOYMENT_RESOURCES_KEY));
    handlers.put(PROCESS_INSTANCE_START_EVENT_TYPE, new StartProcessByIdEventHandler(PROCESS_DEFINITION_ID_KEY, BUSINESS_KEY, VARIABLES_KEY));
    handlers.put(USER_TASK_COMPLETED_EVENT_TYPE, new PlaybackUserTaskCompleteEventHandler());
    return handlers;
  }
}

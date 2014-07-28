package org.activiti.crystalball.simulator.impl.playback;

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


import org.activiti.crystalball.simulator.*;
import org.activiti.crystalball.simulator.delegate.event.Function;
import org.activiti.crystalball.simulator.delegate.event.impl.DeploymentCreateTransformer;
import org.activiti.crystalball.simulator.delegate.event.impl.InMemoryRecordActivitiEventListener;
import org.activiti.crystalball.simulator.delegate.event.impl.ProcessInstanceCreateTransformer;
import org.activiti.crystalball.simulator.delegate.event.impl.UserTaskCompleteTransformer;
import org.activiti.crystalball.simulator.impl.*;
import org.activiti.crystalball.simulator.impl.clock.DefaultClockFactory;
import org.activiti.crystalball.simulator.impl.clock.ThreadLocalClock;
import org.activiti.engine.*;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.el.NoExecutionVariableScope;
import org.activiti.engine.impl.util.DefaultClockImpl;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Clock;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author martin.grofcik
 */
public class PlaybackRunTest {
  //deployment created
  private static final String DEPLOYMENT_CREATED_EVENT_TYPE = "DEPLOYMENT_CREATED_EVENT";
  private static final String DEPLOYMENT_RESOURCES_KEY = "deploymentResources";

  // Process instance start event
  private static final String PROCESS_INSTANCE_START_EVENT_TYPE = "PROCESS_INSTANCE_START";
  private static final String PROCESS_DEFINITION_ID_KEY = "processDefinitionId";
  private static final String VARIABLES_KEY = "variables";
  // User task completed event
  private static final String USER_TASK_COMPLETED_EVENT_TYPE = "USER_TASK_COMPLETED";

  private static final String SIMPLEST_PROCESS = "theSimplestProcess";
  private static final String BUSINESS_KEY = "testBusinessKey";
  private static final String TEST_VALUE = "TestValue";
  private static final String TEST_VARIABLE = "testVariable";

  protected InMemoryRecordActivitiEventListener listener = new InMemoryRecordActivitiEventListener(getTransformers());

  private static final String THE_SIMPLEST_PROCESS = "org/activiti/crystalball/simulator/impl/playback/PlaybackProcessStartTest.testDemo.bpmn20.xml";

  @Test
  public void testProcessInstanceStartEvents() throws Exception {
    recordEvents();

    final SimpleSimulationRun.Builder builder = new SimpleSimulationRun.Builder();
    // init simulation run
    Clock clock = new ThreadLocalClock(new DefaultClockFactory());
    ProcessEngineConfigurationImpl config = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration.createProcessEngineConfigurationFromResourceDefault();
    config.setClock(clock);
    SimulationProcessEngineFactory simulationProcessEngineFactory = new SimulationProcessEngineFactory(config);
    final ProcessEngineImpl simProcessEngine = simulationProcessEngineFactory.getObject();

    builder.processEngine(simProcessEngine)
      .eventCalendar((new SimpleEventCalendarFactory(clock, new SimulationEventComparator(), listener.getSimulationEvents())).getObject())
      .eventHandlers(getHandlers());
    SimpleSimulationRun simRun = builder.build();

    simRun.execute(new NoExecutionVariableScope());

    checkStatus(simProcessEngine);

    simProcessEngine.getProcessEngineConfiguration().setDatabaseSchemaUpdate("create-drop");
    simProcessEngine.close();
    ProcessEngines.destroy();
  }

  private void recordEvents() {
    Clock clock = new DefaultClockImpl();
    ProcessEngineConfigurationImpl config = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration.createProcessEngineConfigurationFromResourceDefault();
    config.setClock(clock);
    ProcessEngine processEngine = (new RecordableProcessEngineFactory(config, listener)).getObject();

    processEngine.getProcessEngineConfiguration().setClock(clock);
    processEngine.getRepositoryService().createDeployment().
      addClasspathResource(THE_SIMPLEST_PROCESS).
      deploy();

    Map<String,Object> variables = new HashMap<String, Object>();
    variables.put(TEST_VARIABLE, TEST_VALUE);
    processEngine.getRuntimeService().startProcessInstanceByKey(SIMPLEST_PROCESS, BUSINESS_KEY, variables);
    checkStatus(processEngine);
    EventRecorderTestUtils.closeProcessEngine(processEngine, listener);
    ProcessEngines.destroy();
  }

  private void checkStatus(ProcessEngine processEngine) {
    HistoryService historyService = processEngine.getHistoryService();
    final HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().
                                                            finished().
                                                            includeProcessVariables().
                                                            singleResult();
    assertNotNull(historicProcessInstance);
    RepositoryService repositoryService = processEngine.getRepositoryService();
    final ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().
                                                processDefinitionId(historicProcessInstance.getProcessDefinitionId()).
                                                singleResult();
    assertEquals(SIMPLEST_PROCESS, processDefinition.getKey());

    assertEquals(1, historicProcessInstance.getProcessVariables().size());
    assertEquals(TEST_VALUE, historicProcessInstance.getProcessVariables().get(TEST_VARIABLE));
    assertEquals(BUSINESS_KEY, historicProcessInstance.getBusinessKey());
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
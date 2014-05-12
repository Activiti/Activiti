package org.activiti.crystalball.simulator.impl.playback;

import org.activiti.crystalball.simulator.*;
import org.activiti.crystalball.simulator.delegate.event.Function;
import org.activiti.crystalball.simulator.delegate.event.impl.InMemoryRecordActivitiEventListener;
import org.activiti.crystalball.simulator.delegate.event.impl.ProcessInstanceCreateTransformer;
import org.activiti.crystalball.simulator.delegate.event.impl.UserTaskCompleteTransformer;
import org.activiti.crystalball.simulator.impl.DefaultSimulationProcessEngineFactory;
import org.activiti.crystalball.simulator.impl.EventRecorderTestUtils;
import org.activiti.crystalball.simulator.impl.RecordableProcessEngineFactory;
import org.activiti.crystalball.simulator.impl.StartProcessEventHandler;
import org.activiti.crystalball.simulator.impl.clock.DefaultClockFactory;
import org.activiti.crystalball.simulator.impl.clock.ThreadLocalClock;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.ProcessEngineImpl;
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
    DefaultSimulationProcessEngineFactory simulationProcessEngineFactory = new DefaultSimulationProcessEngineFactory(THE_SIMPLEST_PROCESS, clock);
    final ProcessEngineImpl simProcessEngine = simulationProcessEngineFactory.getObject();

    builder.processEngine(simProcessEngine)
      .eventCalendar((new SimpleEventCalendarFactory(clock, new SimulationEventComparator(), listener.getSimulationEvents())).getObject())
      .eventHandlers(getHandlers());
    SimpleSimulationRun simRun = builder.build();

    simRun.execute();

    checkStatus(simProcessEngine);

    simProcessEngine.getProcessEngineConfiguration().setDatabaseSchemaUpdate("create-drop");
    simProcessEngine.close();
    ProcessEngines.destroy();
  }

  private void recordEvents() {
    Clock clock = new DefaultClockImpl();
    ProcessEngine processEngine = (new RecordableProcessEngineFactory(THE_SIMPLEST_PROCESS, clock, listener))
                                  .getObject();
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
    transformers.add(new ProcessInstanceCreateTransformer(PROCESS_INSTANCE_START_EVENT_TYPE, PROCESS_DEFINITION_ID_KEY, BUSINESS_KEY, VARIABLES_KEY));
    transformers.add(new UserTaskCompleteTransformer(USER_TASK_COMPLETED_EVENT_TYPE));
    return transformers;
  }

  public static Map<String, SimulationEventHandler> getHandlers() {
    Map<String, SimulationEventHandler> handlers = new HashMap<String, SimulationEventHandler>();
    handlers.put(PROCESS_INSTANCE_START_EVENT_TYPE, new StartProcessEventHandler(PROCESS_DEFINITION_ID_KEY, BUSINESS_KEY, VARIABLES_KEY));
    handlers.put(USER_TASK_COMPLETED_EVENT_TYPE, new PlaybackUserTaskCompleteEventHandler());
    return handlers;
  }
}
package org.activiti.crystalball.simulator.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.activiti.engine.impl.test.ResourceActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;

/**
 * This class tests ScriptEventHandler
 *
 * @author martin.grofcik
 */
public class ScriptEventHandlerTest extends ResourceActivitiTestCase {

  public ScriptEventHandlerTest() {
    super("org/activiti/crystalball/simulator/impl/ScriptEventHandlerTest.cfg.xml");
  }

  @Deployment
  public void testSimpleScriptExecution() throws Exception {
    ProcessInstance simulationExperiment = runtimeService.startProcessInstanceByKey("resultVariableSimulationRun");
    // all simulationManager executions are finished
    assertEquals(1, runtimeService.createExecutionQuery().count());

    String simulationRunResult = (String) runtimeService.getVariable(simulationExperiment.getProcessInstanceId(), "simulationRunResult");
    // simulation run check - process variable has to be set to the value.
    assertThat(simulationRunResult, is("Hello world!"));

    // process end
    runtimeService.signal(simulationExperiment.getId());
    // no process instance is running
    assertEquals( 0, runtimeService.createExecutionQuery().count());
  }

}

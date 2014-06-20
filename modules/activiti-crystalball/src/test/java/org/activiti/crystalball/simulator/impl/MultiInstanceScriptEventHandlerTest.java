package org.activiti.crystalball.simulator.impl;

import org.activiti.engine.impl.test.ResourceActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * This class tests ScriptEventHandler with multi instance simulation run.
 *
 * @author martin.grofcik
 */
public class MultiInstanceScriptEventHandlerTest extends ResourceActivitiTestCase {

  public MultiInstanceScriptEventHandlerTest() {
    super("org/activiti/crystalball/simulator/impl/MultiInstanceScriptEventHandlerTest.cfg.xml");
  }

  @Deployment
  public void testSequentialSimulationRun() throws Exception {
    ProcessInstance simulationExperiment = runtimeService.startProcessInstanceByKey("multiInstanceResultVariablesSimulationRun");
    // all simulationManager executions are finished
    assertEquals(1, runtimeService.createExecutionQuery().count());

    // simulation run check - process variables has to be set to the value. "Hello worldX!"
    String simulationRunResult = (String) runtimeService.getVariable(simulationExperiment.getProcessInstanceId(), "simulationRunResult-0");
    assertThat(simulationRunResult, is("Hello world0!"));
    simulationRunResult = (String) runtimeService.getVariable(simulationExperiment.getProcessInstanceId(), "simulationRunResult-1");
    assertThat(simulationRunResult, is("Hello world1!"));
    simulationRunResult = (String) runtimeService.getVariable(simulationExperiment.getProcessInstanceId(), "simulationRunResult-2");
    assertThat(simulationRunResult, is("Hello world2!"));
    simulationRunResult = (String) runtimeService.getVariable(simulationExperiment.getProcessInstanceId(), "simulationRunResult-3");
    assertThat(simulationRunResult, is("Hello world3!"));
    simulationRunResult = (String) runtimeService.getVariable(simulationExperiment.getProcessInstanceId(), "simulationRunResult-4");
    assertThat(simulationRunResult, is("Hello world4!"));

    // process end
    runtimeService.signal(simulationExperiment.getId());
    // no process instance is running
    assertEquals(0, runtimeService.createExecutionQuery().count());
  }

}

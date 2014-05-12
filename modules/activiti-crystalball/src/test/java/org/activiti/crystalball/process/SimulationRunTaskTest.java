package org.activiti.crystalball.process;

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.impl.test.ResourceActivitiTestCase;
import org.activiti.engine.test.Deployment;

/**
 * This class provides the first insight into simulation run driven by process definition
 *
 * @author martin.grofcik
 */
public class SimulationRunTaskTest extends ResourceActivitiTestCase {

  public SimulationRunTaskTest() {
    super("org/activiti/crystalball/process/SimulationRunTaskTest.cfg.xml");
  }

  @Deployment
  public void testBasicSimulationRun() {
    runtimeService.startProcessInstanceByKey("basicSimulationRun");
    // all executions are finished
    assertEquals(0, runtimeService.createExecutionQuery().count());
  }

  @Override
  protected void closeDownProcessEngine() {
    super.closeDownProcessEngine();
    ProcessEngines.destroy();
  }
}

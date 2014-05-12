package org.activiti.crystalball.process;

import org.activiti.crystalball.simulator.SimulationRun;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.delegate.Expression;

/**
 * This class implement task which runs simulation experiment
 *
 * @author martin.grofcik
 */
public class SimulationRunTask implements JavaDelegate {

  private Expression simulationRunExpression;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    SimulationRun simulationRun = (SimulationRun) simulationRunExpression.getValue(execution);
    simulationRun.execute();
  }

  @SuppressWarnings("UnusedDeclaration")
  public void setSimulationRun(Expression simulationRun) {
    this.simulationRunExpression = simulationRun;
  }
}

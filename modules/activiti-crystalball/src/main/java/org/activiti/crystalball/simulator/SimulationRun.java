package org.activiti.crystalball.simulator;

/**
 * This is basic interface for SimRun implementation
 * it allows to execute simulation without any break
 *
 * @author martin.grofcik
 */
public interface SimulationRun {

  /**
   * executes simulation run according to configuration already set
   * @throws Exception
   */
  void execute() throws Exception;

}

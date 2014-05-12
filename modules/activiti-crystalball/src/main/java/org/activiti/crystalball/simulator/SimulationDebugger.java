package org.activiti.crystalball.simulator;

/**
 * Allows to run simulation in debug mode
 *
 * @author martin.grofcik
 */
public interface SimulationDebugger {
  /**
   * initialize simulation run
   */
  void init();

  /**
   * step one simulation event forward
   */
  void step();

  /**
   * continue in the simulation run
   */
  void runContinue();

  /**
   * execute simulation run till simulationTime
   */
  void runTo(long simulationTime);

  /**
   * execute simulation run till simulation event of the specific type
   */
  void runTo(String simulationEventType);

  /**
   * close simulation run
   */
  void close();
}

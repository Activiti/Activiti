package org.activiti.crystalball.simulator.delegate.event.impl;

import org.activiti.crystalball.simulator.SimulationEvent;
import org.activiti.crystalball.simulator.delegate.event.Function;
import org.activiti.engine.delegate.event.ActivitiEvent;

/**
 * This class provides abstract base for ActivitiEvent  -> SimulationEvent transformation
 *
 * @author martin.grofcik
 */
public abstract class Activiti2SimulationEventFunction implements Function<ActivitiEvent, SimulationEvent> {
  protected final String simulationEventType;

  public Activiti2SimulationEventFunction(String simulationEventType) {this.simulationEventType = simulationEventType;}
}

package org.activiti.crystalball.simulator.delegate.event.impl;

import org.activiti.crystalball.simulator.SimulationEvent;
import org.activiti.crystalball.simulator.delegate.event.Function;
import org.activiti.engine.delegate.event.ActivitiEvent;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author martin.grofcik
 */
public class InMemoryRecordActivitiEventListener extends AbstractRecordActivitiEventListener {

	private Collection<SimulationEvent> events;

  public InMemoryRecordActivitiEventListener(List<Function<ActivitiEvent, SimulationEvent>> transformers) {
    super(transformers);
    events = new HashSet<SimulationEvent>();
  }

  public Collection<SimulationEvent> getSimulationEvents()
  {
    return events;
  }


  @Override
  protected void store(Collection<SimulationEvent> simulationEvents) {
    events.addAll(simulationEvents);
  }

}

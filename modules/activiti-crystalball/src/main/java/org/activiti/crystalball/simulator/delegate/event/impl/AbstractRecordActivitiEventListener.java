package org.activiti.crystalball.simulator.delegate.event.impl;

import org.activiti.crystalball.simulator.SimulationEvent;
import org.activiti.crystalball.simulator.delegate.event.Function;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class provides abstract base to records Activiti events
 *
 * @author martin.grofcik
 */
public abstract class AbstractRecordActivitiEventListener implements ActivitiEventListener {
  protected List<Function<ActivitiEvent, SimulationEvent>> transformers;

  public AbstractRecordActivitiEventListener(List<Function<ActivitiEvent, SimulationEvent>> transformers) {this.transformers = transformers;}

  @SuppressWarnings("UnusedDeclaration")
  public abstract Collection<SimulationEvent> getSimulationEvents();

  @Override
  public void onEvent(ActivitiEvent event) {
    Collection<SimulationEvent> simulationEvents = transform(event);
    store(simulationEvents);
  }

  protected abstract void store(Collection<SimulationEvent> simulationEvents);

  protected Collection<SimulationEvent> transform(ActivitiEvent event) {
    List<SimulationEvent> simEvents = new ArrayList<SimulationEvent>();
    for (Function<ActivitiEvent, SimulationEvent> t : transformers) {
      SimulationEvent simEvent = t.apply(event);
      if (simEvent != null)
        simEvents.add(simEvent);
    }
    return simEvents;
  }

  @Override
	public boolean isFailOnException() {
		return true;
	}
}

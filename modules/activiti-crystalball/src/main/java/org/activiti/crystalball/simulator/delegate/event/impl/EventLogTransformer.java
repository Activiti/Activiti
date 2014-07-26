package org.activiti.crystalball.simulator.delegate.event.impl;

import org.activiti.crystalball.simulator.SimulationEvent;
import org.activiti.crystalball.simulator.delegate.event.Function;
import org.activiti.engine.event.EventLogEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class transforms event log events into simulation events
 */
public class EventLogTransformer {
  protected List<Function<EventLogEntry, SimulationEvent>> transformers;

  public EventLogTransformer(List<Function<EventLogEntry, SimulationEvent>> transformers) {this.transformers = transformers;}

  public List<SimulationEvent> transform(List<EventLogEntry> eventLog) {
    List<SimulationEvent> simulationEvents = new ArrayList<SimulationEvent>();
    for (EventLogEntry logEntry : eventLog) {
      simulationEvents.addAll(transformEntry(logEntry));
    }
    return simulationEvents;
  }
  protected Collection<SimulationEvent> transformEntry(EventLogEntry event) {
    List<SimulationEvent> simEvents = new ArrayList<SimulationEvent>();
    for (Function<EventLogEntry, SimulationEvent> t : transformers) {
      SimulationEvent simEvent = t.apply(event);
      if (simEvent != null)
        simEvents.add(simEvent);
    }
    return simEvents;
  }

}

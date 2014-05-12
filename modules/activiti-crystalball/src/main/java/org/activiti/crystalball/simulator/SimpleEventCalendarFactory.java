package org.activiti.crystalball.simulator;

import org.activiti.engine.runtime.ClockReader;
import org.springframework.beans.factory.FactoryBean;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

/**
 * @author martin.grofcik
 */
public class SimpleEventCalendarFactory implements FactoryBean<EventCalendar> {

  protected final Collection<SimulationEvent> simulationEvents;
  protected Comparator<SimulationEvent> eventComparator;
  protected final ClockReader clockReader;

	public SimpleEventCalendarFactory(ClockReader clockReader, Comparator<SimulationEvent> eventComparator, Collection<SimulationEvent> simulationEvents) {
    this.clockReader = clockReader;
    this.eventComparator = eventComparator;
    this.simulationEvents = simulationEvents;
	}

  @SuppressWarnings("UnusedDeclaration")
  public SimpleEventCalendarFactory(ClockReader clockReader, Comparator<SimulationEvent> eventComparator) {
    this.eventComparator = eventComparator;
    this.clockReader = clockReader;
    this.simulationEvents = Collections.emptyList();
  }

  @Override
	public SimpleEventCalendar getObject() {
    SimpleEventCalendar simpleEventCalendar = new SimpleEventCalendar(clockReader, eventComparator);
    simpleEventCalendar.addEvents(simulationEvents);
    return simpleEventCalendar;
  }

	@Override
	public Class<?> getObjectType() {
		return SimpleEventCalendar.class;
	}

	@Override
	public boolean isSingleton() {
		return false;
	}

}

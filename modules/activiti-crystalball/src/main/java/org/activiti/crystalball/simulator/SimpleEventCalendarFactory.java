package org.activiti.crystalball.simulator;

/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


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

package org.activiti.crystalball.simulator;

import org.activiti.engine.runtime.ClockReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * @author martin.grofcik
 */
public class SimpleEventCalendar implements EventCalendar {

  private static Logger log = LoggerFactory.getLogger(SimpleEventCalendar.class.getName());

  private static final int NULL = -1;

  protected List<SimulationEvent> eventList = new ArrayList<SimulationEvent>();
	protected int minIndex = NULL;
	protected Comparator<SimulationEvent> eventComparator;
  protected final ClockReader clockReader;


	public SimpleEventCalendar(ClockReader clockReader, Comparator<SimulationEvent> eventComparator) {
    this.clockReader = clockReader;
    this.eventComparator = eventComparator;
	}
	
	@Override
    public boolean isEmpty() {
		return minIndex == NULL;
	}

  @Override
  public SimulationEvent peekFirstEvent() {
    if (minIndex == NULL)
      return null;

    return eventList.get( minIndex);
  }

  @Override
  public SimulationEvent removeFirstEvent() {
		if (minIndex == NULL)
			return null;
		
		SimulationEvent minEvent = eventList.remove( minIndex );

		if (minEvent.hasSimulationTime() && minEvent.getSimulationTime() < this.clockReader.getCurrentTime().getTime())
			throw new RuntimeException("Unable to execute event from the past");
		
		if (eventList.isEmpty()) { 
			minIndex = NULL;
		} else {
			minIndex = 0;
			SimulationEvent event = eventList.get(0);
			for ( int i = 1; i < eventList.size(); i++ ) {
				if (eventComparator.compare( eventList.get( i ), event ) < 0) {
					minIndex = i;
					event = eventList.get( i );
				}
			}
		}
		return minEvent;
	}
	
	@Override
  public void addEvent(SimulationEvent event) {
    log.debug("Scheduling new event [{}]",event);
    if (event != null && isMinimal(event))
			minIndex = eventList.size();
		eventList.add(event);			
	}

  @Override
  public void clear() {
    eventList.clear();
    minIndex = NULL;
  }

  /**
	 * is event the first event in the calendar?
	 * @param event - used in comparison
	 * @return is minimal event decision
	 */
	private boolean isMinimal(SimulationEvent event) {
    return minIndex == NULL || eventComparator.compare(event, eventList.get(minIndex)) < 0;
  }

  public void addEvents(Collection<SimulationEvent> simulationEvents) {
    for (SimulationEvent event : simulationEvents) {
      addEvent(event);
    }
  }
}

package org.activiti.crystalball.simulator;

import java.util.Comparator;

/**
 * @author martin.grofcik
 */
public class SimulationEventComparator implements Comparator<SimulationEvent> {

	@Override
	public int compare(SimulationEvent o1, SimulationEvent o2) {
    // the highest priority has simulation time
		if ( o1.getSimulationTime() < o2.getSimulationTime())
			return -1;
		if ( o1.getSimulationTime() > o2.getSimulationTime()) 
			return 1;
    //in case of equal simulation time, take priority into account
    if (o1.getPriority() < o2.getPriority())
      return -1;
    if (o1.getPriority() > o2.getPriority())
      return 1;
		return 0;
	}

}

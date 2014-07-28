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

package org.activiti.crystalball.simulator.delegate.event.impl;

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

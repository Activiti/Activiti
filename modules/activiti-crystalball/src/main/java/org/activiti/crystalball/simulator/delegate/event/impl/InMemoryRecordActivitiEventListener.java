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

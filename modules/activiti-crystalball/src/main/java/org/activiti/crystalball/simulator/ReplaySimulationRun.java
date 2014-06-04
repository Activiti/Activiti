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


import org.activiti.engine.impl.ProcessEngineImpl;

import java.util.Map;

/**
 * This class provides simulation run for replay purposes
 * replay uses real time and running engine to execute simulation events.
 *
 * @author martin.grofcik
 */
public class ReplaySimulationRun extends AbstractSimulationRun {

  private final EventCalendar eventCalendar;

  public ReplaySimulationRun(ProcessEngineImpl processEngine, Map<String, SimulationEventHandler> customEventHandlerMap) {
    this(processEngine, new SimpleEventCalendar(processEngine.getProcessEngineConfiguration().getClock(), new SimulationEventComparator()), customEventHandlerMap);
  }

  public ReplaySimulationRun(ProcessEngineImpl processEngine, EventCalendar eventCalendar, Map<String, SimulationEventHandler> customEventHandlerMap) {
    super(customEventHandlerMap);
    this.processEngine = processEngine;
    this.eventCalendar = eventCalendar;
  }

  @Override
  protected void initSimulationRunContext() {
    SimulationRunContext.setEventCalendar(eventCalendar);
    SimulationRunContext.setProcessEngine(processEngine);
  }

  /**
   * simulation does not end - it can live forever.
   * @param event - is it end of the simulation run?
   * @return false
   */
  @Override
  protected boolean simulationEnd(SimulationEvent event) {
    return false;
  }

  /**
   * do not affect existing engine
   */
  @Override
  public void close() {
    SimulationRunContext.getEventCalendar().clear();
    SimulationRunContext.removeEventCalendar();
    SimulationRunContext.removeProcessEngine();
  }
}

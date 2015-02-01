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


import org.activiti.crystalball.simulator.impl.AcquireJobNotificationEventHandler;
import org.activiti.crystalball.simulator.impl.NoopEventHandler;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.jobexecutor.JobExecutor;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

/**
 * @author martin.grofcik
 */
public class SimpleSimulationRun extends AbstractSimulationRun {

	protected EventCalendar eventCalendar;

  /** simulation start date*/
  protected Date simulationStartDate = new Date(0);
  protected Date dueDate = null;

  protected SimpleSimulationRun(Builder builder) {
    super(builder.eventHandlers);
    this.eventCalendar = builder.getEventCalendar();
		this.processEngine = builder.getProcessEngine();
    // init internal event handler map.
		eventHandlerMap.put(SimulationEvent.TYPE_END_SIMULATION, new NoopEventHandler());
    if ( builder.getJobExecutor() != null)
      eventHandlerMap.put(SimulationEvent.TYPE_ACQUIRE_JOB_NOTIFICATION_EVENT, new AcquireJobNotificationEventHandler(builder.getJobExecutor()));
  }

  @Override
  public void close() {
    // remove simulation from simulation context
      SimulationRunContext.getEventCalendar().clear();
      SimulationRunContext.removeEventCalendar();
      SimulationRunContext.getProcessEngine().close();
      SimulationRunContext.removeProcessEngine();
  }

  @Override
  protected void initSimulationRunContext(VariableScope execution) {// init new process engine
    try {
    // add context in which simulation run is executed
    SimulationRunContext.setEventCalendar(eventCalendar);
    SimulationRunContext.setProcessEngine(processEngine);
    SimulationRunContext.setExecution(execution);

    // run simulation
    // init context and task calendar and simulation time is set to current
    SimulationRunContext.getClock().setCurrentTime(simulationStartDate);

    if (dueDate != null)
      SimulationRunContext.getEventCalendar().addEvent(new SimulationEvent.Builder( SimulationEvent.TYPE_END_SIMULATION).
        simulationTime(dueDate.getTime()).
        build());
    } catch (Exception e) {
      throw new ActivitiException("Unable to initialize simulation run", e);
    }
  }

  @Override
  protected boolean simulationEnd( SimulationEvent event) {
    if ( event != null && event.getType().equals(SimulationEvent.TYPE_BREAK_SIMULATION))
      return true;
		if ( dueDate != null)
			return event == null || (SimulationRunContext.getClock().getCurrentTime().after( dueDate ));
		return  event == null;
	}

  public static class Builder {
        private Map<String, SimulationEventHandler> eventHandlers = Collections.emptyMap();
        private ProcessEngineImpl processEngine;
        private EventCalendar eventCalendar;
        private JobExecutor jobExecutor;

        public JobExecutor getJobExecutor() {
            return jobExecutor;
        }

        public Builder jobExecutor(JobExecutor jobExecutor) {
            this.jobExecutor = jobExecutor;
            return this;
        }

        public ProcessEngineImpl getProcessEngine() {
            return processEngine;
        }

        public Builder processEngine(ProcessEngineImpl processEngine) {
            this.processEngine = processEngine;
            return this;
        }

        public EventCalendar getEventCalendar() {
            return eventCalendar;
        }

        public Builder eventCalendar(EventCalendar eventCalendar) {
            this.eventCalendar = eventCalendar;
            return this;
        }

        public Builder eventHandlers(Map<String, SimulationEventHandler> eventHandlersMap) {
            this.eventHandlers = eventHandlersMap;
            return this;
        }

        public SimpleSimulationRun build() {
            return new SimpleSimulationRun(this);
        }
    }
}

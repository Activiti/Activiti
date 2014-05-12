package org.activiti.crystalball.simulator;

import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.jobexecutor.JobExecutor;
import org.springframework.beans.factory.FactoryBean;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author martin.grofcik
 */
public class SimpleSimulationRunFactory implements FactoryBean<SimulationRun> {

    protected Map<String, SimulationEventHandler> customEventHandlerMap;
    protected HashMap<String, SimulationEventHandler> eventHandlerMap;
    protected ProcessEngineImpl processEngine;
    protected EventCalendar eventCalendar;
    protected JobExecutor jobExecutor;

    public SimpleSimulationRunFactory() {
	}
	
	@Override
	public SimulationRun getObject() throws Exception {
    return new SimpleSimulationRun.Builder().
      eventHandlers(customEventHandlerMap).
      processEngine(processEngine).
      eventCalendar(eventCalendar).
      jobExecutor(jobExecutor).
      build();
  }

	@Override
	public Class<? extends SimulationRun> getObjectType() {
		return SimpleSimulationRun.class;
	}

	@Override
	public boolean isSingleton() {
		return false;
	}

    @SuppressWarnings("UnusedDeclaration")
    public void setCustomEventHandlerMap(Map<String, SimulationEventHandler> customEventHandlerMap) {
        this.customEventHandlerMap = customEventHandlerMap;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setEventHandlerMap(HashMap<String, SimulationEventHandler> eventHandlerMap) {
        this.eventHandlerMap = eventHandlerMap;
    }

    public void setProcessEngine(ProcessEngineImpl processEngine) {
        this.processEngine = processEngine;
    }

    public void setEventCalendar(EventCalendar eventCalendar) {
        this.eventCalendar = eventCalendar;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setJobExecutor(JobExecutor jobExecutor) {
        this.jobExecutor = jobExecutor;
    }
}

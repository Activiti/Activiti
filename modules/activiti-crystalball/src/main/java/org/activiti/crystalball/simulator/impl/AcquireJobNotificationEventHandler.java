package org.activiti.crystalball.simulator.impl;

import org.activiti.crystalball.simulator.SimulationEvent;
import org.activiti.crystalball.simulator.SimulationEventHandler;
import org.activiti.crystalball.simulator.SimulationRunContext;
import org.activiti.crystalball.simulator.processengine.jobexecutor.SimulationDefaultJobExecutor;
import org.activiti.engine.impl.jobexecutor.JobExecutor;
import org.activiti.engine.impl.jobexecutor.SimulationAcquireJobsRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Notify AcquireJobsRunnable to continue in execution 
 *
 * @author martin.grofcik
 */
public class AcquireJobNotificationEventHandler implements
		SimulationEventHandler {

	private static Logger log = LoggerFactory.getLogger(AcquireJobNotificationEventHandler.class);

	JobExecutor jobExecutor = null;
	
	public AcquireJobNotificationEventHandler(JobExecutor jobExecutor) {
		this.jobExecutor = jobExecutor;
	}
	
	@Override
	public void init() {
        log.info(jobExecutor.getName() + " starting to acquire jobs");
        jobExecutor.start();

    SimulationEvent event = new SimulationEvent.Builder(SimulationEvent.TYPE_ACQUIRE_JOB_NOTIFICATION_EVENT).
      simulationTime(SimulationRunContext.getClock().getCurrentTime().getTime()).
      property(((SimulationDefaultJobExecutor) jobExecutor).getAcquireJobsRunnable()).
      build();
    SimulationRunContext.getEventCalendar().addEvent(event);
        
	}

	@Override
	public void handle(SimulationEvent event) {
        log.debug(" starting to acquire jobs [" + event + "]");
		SimulationAcquireJobsRunnable acquireJobs = (SimulationAcquireJobsRunnable) event.getProperty();
		acquireJobs.run();
	}

}

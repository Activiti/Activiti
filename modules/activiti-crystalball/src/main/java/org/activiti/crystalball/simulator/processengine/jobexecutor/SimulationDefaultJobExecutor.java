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
package org.activiti.crystalball.simulator.processengine.jobexecutor;

import java.util.List;

import org.activiti.engine.impl.cmd.AcquireJobsCmd;
import org.activiti.engine.impl.jobexecutor.AcquireJobsRunnable;
import org.activiti.engine.impl.jobexecutor.DefaultJobExecutor;
import org.activiti.engine.impl.jobexecutor.ExecuteJobsRunnable;
import org.activiti.engine.impl.jobexecutor.SimulationAcquireJobsRunnable;

/**
 * simulation is driven by simulation time. That's why JobExecutor 
 * has to be driven by simulation time too.  
 *
 * @author martin.grofcik
 */
public class SimulationDefaultJobExecutor extends DefaultJobExecutor {
	
	public SimulationDefaultJobExecutor() {
	}
	
	/**
	 * use SimulationAcquireJobsRunnable instead of AcquireJobsRunnable. 
	 * To use simulation time instead of "real" time 
	 */
	@Override
	protected void ensureInitialization() { 
	    acquireJobsCmd = new AcquireJobsCmd(this);
	    acquireJobsRunnable = new SimulationAcquireJobsRunnable(this);  
	}
	
	/**
	 * do not execute new thread - simulation time can move too forward.
	 * 
	 */
	public void executeJobs(List<String> jobIds) {
			(new ExecuteJobsRunnable(this, jobIds)).run();
	}

	public AcquireJobsRunnable getAcquireJobsRunnable() {
		return acquireJobsRunnable;
	}
	
	  protected void startJobAcquisitionThread() {
//			if (jobAcquisitionThread == null) {
//				jobAcquisitionThread = new Thread(acquireJobsRunnable);
//				jobAcquisitionThread.start();
//			}
		}
		
		protected void stopJobAcquisitionThread() {
//			try {
//				jobAcquisitionThread.join();
//			} catch (InterruptedException e) {
//				log.log(
//						Level.WARNING,
//						"Interrupted while waiting for the job Acquisition thread to terminate",
//						e);
//			}	
//			jobAcquisitionThread = null;
		}
}

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

package org.activiti.engine.impl.jobexecutor;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.activiti.engine.impl.cmd.AcquireJobsCmd;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.runtime.ClockReader;
import org.activiti.engine.runtime.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Interface to the work management component of activiti.</p>
 * 
 * <p>This component is responsible for performing all background work 
 * ({@link Job Jobs}) scheduled by activiti.</p>
 * 
 * <p>You should generally only have one of these per Activiti instance (process 
 * engine) in a JVM.
 * In clustered situations, you can have multiple of these running against the
 * same queue + pending job list.</p>
 * 
 * @author Daniel Meyer
 * @author Joram Barrez
 */
public abstract class JobExecutor {
  
  private static Logger log = LoggerFactory.getLogger(JobExecutor.class);

  protected String name = "JobExecutor["+getClass().getName()+"]";
  protected CommandExecutor commandExecutor;
  protected Command<AcquiredJobs> acquireJobsCmd;
  protected AcquireJobsRunnable acquireJobsRunnable;
  protected RejectedJobsHandler rejectedJobsHandler;
  protected Thread jobAcquisitionThread;
  
  protected boolean isAutoActivate = false;
  protected boolean isActive = false;

  /**
   * To avoid deadlocks, the default for this is one.
   * This way, in a clustered setup, multiple job executors can acquire jobs
   * without creating a deadlock due to fetching multiple jobs at once and
   * trying to lock them all at once.
   * 
   * In a non-clustered setup, this setting can be changed to any value > 0
   * without problems.
   * 
   * See https://activiti.atlassian.net/browse/ACT-1879 for more information.
   */
  protected int maxJobsPerAcquisition = 1;
  protected long waitTimeInMillis = 5000L;
  protected String lockOwner = UUID.randomUUID().toString();
  protected int lockTimeInMillis = 5 * 60 * 1000;
  protected ClockReader clockReader;
  
  /** Starts the job executor */
  public void start() {
    if (isActive) {
      return;
    }
    log.info("Starting up the JobExecutor[{}].", getClass().getName());
    ensureInitialization();    
    startExecutingJobs();
    isActive = true;
  }
  
  /** Shuts down the whole job executor */
  public synchronized void shutdown() {
    if (!isActive) {
      return;
    }
    log.info("Shutting down the JobExecutor[{}].", getClass().getName());
    acquireJobsRunnable.stop();
    stopExecutingJobs();
    ensureCleanup();   
    isActive = false;
  }
  
  /** Possibility to ensure everything is nicely initialized before starting the threads */
  protected void ensureInitialization() { 
  	if (acquireJobsCmd == null) {
  		acquireJobsCmd = new AcquireJobsCmd(this);
  	}
  	if (acquireJobsRunnable == null) {
  		acquireJobsRunnable = new AcquireJobsRunnableImpl(this);
  	}
  }
  
  /** Possibility to clean up resources */
  protected void ensureCleanup() {  
    acquireJobsCmd = null;
    acquireJobsRunnable = null;  
  }
  
  /** 
   * Called when a new job was added by the process engine to which
   * this job executor belongs. This is a hint, that for example
   * the acquiring needs to start again when it would be sleeping.
   */
  public void jobWasAdded() {
    if(isActive) {
      acquireJobsRunnable.jobWasAdded();
    }
  }
  
  /** Starts the acquisition thread */
  protected void startJobAcquisitionThread() {
		if (jobAcquisitionThread == null) {
			jobAcquisitionThread = new Thread(acquireJobsRunnable);
		}
		jobAcquisitionThread.start();
	}
	
  /** Stops the acquisition thread */
	protected void stopJobAcquisitionThread() {
		try {
			jobAcquisitionThread.join();
		} catch (InterruptedException e) {
			log.warn("Interrupted while waiting for the job Acquisition thread to terminate", e);
		}	
		jobAcquisitionThread = null;
	}
  
  /* Need to be implemented by concrete subclasses */
  
	public abstract void executeJobs(List<String> jobIds);
  protected abstract void startExecutingJobs();
  protected abstract void stopExecutingJobs(); 
  
  /* Can be overridden by subclasses if wanted */
  public void jobDone(JobEntity job) {
  	// Default: do nothing
  }
  
  /* Can be overridden by subclasses if wanted */
  public void jobDone(String jobId) {
  	// Default: do nothing
  }
  
  // getters and setters //////////////////////////////////////////////////////

  public CommandExecutor getCommandExecutor() {
    return commandExecutor;
  }

  public long getWaitTimeInMillis() {
    return waitTimeInMillis;
  }

  public void setWaitTimeInMillis(int waitTimeInMillis) {
    this.waitTimeInMillis = waitTimeInMillis;
  }

  public int getLockTimeInMillis() {
    return lockTimeInMillis;
  }

  public void setLockTimeInMillis(int lockTimeInMillis) {
    this.lockTimeInMillis = lockTimeInMillis;
  }

  public String getLockOwner() {
    return lockOwner;
  }

  public void setLockOwner(String lockOwner) {
    this.lockOwner = lockOwner;
  }

  public boolean isAutoActivate() {
    return isAutoActivate;
  }

  public void setCommandExecutor(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
  }

  public void setAutoActivate(boolean isAutoActivate) {
    this.isAutoActivate = isAutoActivate;
  }

  public int getMaxJobsPerAcquisition() {
    return maxJobsPerAcquisition;
  }
  
  public void setMaxJobsPerAcquisition(int maxJobsPerAcquisition) {
    this.maxJobsPerAcquisition = maxJobsPerAcquisition;
  }

  public String getName() {
    return name;
  }
  
  public Command<AcquiredJobs> getAcquireJobsCmd() {
    return acquireJobsCmd;
  }
  
  public void setAcquireJobsCmd(Command<AcquiredJobs> acquireJobsCmd) {
    this.acquireJobsCmd = acquireJobsCmd;
  }
    
  public AcquireJobsRunnable getAcquireJobsRunnable() {
		return acquireJobsRunnable;
	}

	public void setAcquireJobsRunnable(AcquireJobsRunnable acquireJobsRunnable) {
		this.acquireJobsRunnable = acquireJobsRunnable;
	}

	public boolean isActive() {
    return isActive;
  }
  
  public RejectedJobsHandler getRejectedJobsHandler() {
    return rejectedJobsHandler;
  }
    
  public void setRejectedJobsHandler(RejectedJobsHandler rejectedJobsHandler) {
    this.rejectedJobsHandler = rejectedJobsHandler;
  }
  
  public Date getCurrentTime() {
    return clockReader.getCurrentTime();
  }

  public void setClockReader(ClockReader clockReader) {
    this.clockReader = clockReader;
  }
}

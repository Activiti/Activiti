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

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.activiti.engine.impl.cmd.AcquireJobsCmd;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.runtime.Job;

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
 */
public abstract class JobExecutor {
  
  private static Logger log = Logger.getLogger(JobExecutor.class.getName());

  protected String name = "JobExecutor["+getClass().getName()+"]";
  protected CommandExecutor commandExecutor;
  protected Command<AcquiredJobs> acquireJobsCmd;
  protected AcquireJobsRunnable acquireJobsRunnable;
  
  protected boolean isAutoActivate = false;
  protected boolean isActive = false;
  
  protected int maxJobsPerAcquisition = 3;
  protected int waitTimeInMillis = 5 * 1000;
  protected String lockOwner = UUID.randomUUID().toString();
  protected int lockTimeInMillis = 5 * 60 * 1000;
      
  public void start() {
    if (isActive) {
      return;
    }
    log.info("Starting up the JobExecutor["+getClass().getName()+"].");
    ensureInitialization();    
    startExecutingJobs();
    isActive = true;
  }
  
  public synchronized void shutdown() {
    if (!isActive) {
      return;
    }
    log.info("Shutting down the JobExecutor["+getClass().getName()+"].");
    acquireJobsRunnable.stop();
    stopExecutingJobs();
    ensureCleanup();   
    isActive = false;
  }
  
  protected void ensureInitialization() { 
    acquireJobsCmd = new AcquireJobsCmd(this);
    acquireJobsRunnable = new AcquireJobsRunnable(this);  
  }
  
  protected void ensureCleanup() {  
    acquireJobsCmd = null;
    acquireJobsRunnable = null;  
  }
  
  public void jobWasAdded() {
    if(isActive) {
      acquireJobsRunnable.jobWasAdded();
    }
  }
  
  protected abstract void startExecutingJobs();
  protected abstract void stopExecutingJobs(); 
  protected abstract void executeJobs(List<String> jobIds);
  
  // getters and setters //////////////////////////////////////////////////////

  public CommandExecutor getCommandExecutor() {
    return commandExecutor;
  }

  public int getWaitTimeInMillis() {
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
    
  public boolean isActive() {
    return isActive;
  }
      
}

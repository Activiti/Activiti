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
package org.activiti.impl.jobexecutor;

import java.util.Collection;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.impl.cmd.AcquireJobCmd;
import org.activiti.impl.interceptor.CommandExecutor;

/**
 * Background thread responsible for retrieving the list
 *  of Jobs currently awaiting processing from the queue, 
 *  and passing them to the {@link JobExecutor} to be run.
 * There should only ever be one of these per 
 *  {@link JobExecutor}.
 * Note that in a clustered Environment, there can be multiple
 *  of these per queue, so we need locking/transactions to 
 *  ensure we don't fetch Jobs someone else already has, and
 *  we need to ensure we're not too greedy about picking up
 *  jobs.
 */
public class JobAcquisitionThread extends Thread {
  
  private static Logger log = Logger.getLogger(JobAcquisitionThread.class.getName());

  private JobExecutor jobExecutor;
  protected String lockOwner;
  protected boolean isActive = false;
  
  protected JobAcquisitionThread(JobExecutor jobExecutor) {
    super("PendingJobsFetcher");
    this.jobExecutor = jobExecutor;
    this.lockOwner = UUID.randomUUID().toString();
  }
  
  public void run() {
    log.info(getName() + " starting to acquire jobs");
    this.isActive = true;
    
    CommandExecutor commandExecutor = jobExecutor.getCommandExecutor();

    while(isActive) {
      Collection<String> jobIds = commandExecutor.execute(new AcquireJobCmd(lockOwner, jobExecutor.getLockTimeInMillis()));
      
    }
    log.info(getName() + " stopped");
  }
  
  /**
   * Triggers a shutdown
   */
  public void shutdown() {
    if(isActive) {
      log.info(getName() + " is shutting down");
      isActive = false;
      interrupt();
    }
  }

  public void jobWasAdded() {
    // TODO
  }

  
  public JobExecutor getJobExecutor() {
    return jobExecutor;
  }

  public String getLockOwner() {
    return lockOwner;
  }

  
  public void setLockOwner(String lockOwner) {
    this.lockOwner = lockOwner;
  }

  
  public boolean isActive() {
    return isActive;
  }
}

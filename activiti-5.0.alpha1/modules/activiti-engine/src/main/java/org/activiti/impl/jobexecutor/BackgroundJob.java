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

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.impl.Cmd;
import org.activiti.impl.CmdExecutor;
import org.activiti.impl.ProcessEngineImpl;
import org.activiti.impl.execution.JobImpl;
import org.activiti.impl.persistence.PersistenceSession;
import org.activiti.impl.tx.TransactionContext;

/**
 * An executable wrapper around a single Job, 
 *  which need to be executed in the background.
 */
public class BackgroundJob implements Runnable, Cmd<Object> {
  private static Logger log = Logger.getLogger(BackgroundJob.class.getName());
  protected final ProcessEngineImpl processEngine;
  protected final HistoricJobsList historicJobsList;
  protected final CmdExecutor cmdExecutor;
  
  private long jobId;
  
  public BackgroundJob(long jobId, CmdExecutor cmdExecutor,
          ProcessEngineImpl processEngine, HistoricJobsList historicJobsList) {
    this.jobId = jobId;
    this.cmdExecutor = cmdExecutor;
    this.processEngine = processEngine;
    this.historicJobsList = historicJobsList;
  }
  
  /**
   * Runs the single Job.
   */
  public void run() {
    cmdExecutor.execute(this, processEngine);
  }
  
  /**
   * Runs one job.
   */
  public Object execute(TransactionContext transactionContext) {
    HistoricJob jobHistory = new HistoricJob(jobId, "(unknown)");
    
    try {
      PersistenceSession persistenceSession = 
        transactionContext.getTransactionalObject(PersistenceSession.class);

      JobImpl job = persistenceSession.findJob(jobId);
      if(job == null) {
        throw new NullPointerException("Job with id " + jobId + " chouldn't be found");
      }
      
      // TODO what to do with the result?
      Object result = cmdExecutor.execute(job, processEngine);
      
      // TODO how to indicate it worked?
      // TODO handle problems
      // TODO handle repeated failures
      
      // All done, record us as having completed OK
      jobHistory.setCompletionDate(new Date());
    } catch(RuntimeException e) {
      log.log(
          Level.SEVERE,
          "Error running background job # " + jobId,
          e
      );
      jobHistory.setException(e);
    }
    
    // Add this job to the historic list
    historicJobsList.record(jobHistory);
    
    return null;
  }
  
  public long getJobId() {
    return jobId;
  }
  protected void setCurrentJobId(long jobId) {
    this.jobId = jobId;
  }
  
  public String toString() {
    return "Background Execution of Job # " + jobId;
  }
}

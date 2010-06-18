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
import java.util.logging.Logger;

import org.activiti.impl.CmdExecutor;
import org.activiti.impl.ProcessEngineImpl;

/**
 * An executable wrapper around a sequence
 *  of dependent jobs, which need to be executed
 *  in the background, and on the same Thread. 
 */
public class BackgroundJobCollection extends BackgroundJob {
  private static Logger log = Logger.getLogger(BackgroundJobCollection.class.getName());
  
  private final Collection<Long> jobIds;
  
  public BackgroundJobCollection(Collection<Long> jobIds, CmdExecutor cmdExecutor,
      ProcessEngineImpl processEngine, HistoricJobsList historicJobsList) {
    super(-1, cmdExecutor, processEngine, historicJobsList);
    this.jobIds = jobIds;
  }
  
  /**
   * Runs the job(s) in order, in the same
   *  Thread.
   */
  @Override
  public void run() {
    for(long jobId : jobIds) {
      setCurrentJobId(jobId);
      cmdExecutor.execute(this, processEngine);
    }
  }
  
  public Collection<Long> getJobIds() {
    return jobIds;
  }
  
  public String toString() {
    return "Background Execution of Job Sequence, #s " + jobIds;
  }
}

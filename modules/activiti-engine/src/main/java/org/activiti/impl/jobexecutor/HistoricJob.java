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
import java.util.logging.Logger;

/**
 * Holds details on a Job that's been run.
 * Normally put into a short FIFO list by the
 *  {@link JobExecutor}, but rarely kept
 *  around for that long
 */
public class HistoricJob {
  private static Logger log = Logger.getLogger(HistoricJob.class.getName());
  
  private long jobId;
  private String jobDescription;
  private Date startDate;
  private Date completionDate;
  private Exception exception;
  
  public HistoricJob(long jobId, String jobDescription) {
    this.jobId = jobId;
    this.jobDescription = jobDescription;
    this.startDate = new Date();
  }

  /**
   * Returns the date that the job completed
   *  successfully.
   * Null if job is in progress, or it failed.
   */
  public Date getCompletionDate() {
    return completionDate;
  }
  public void setCompletionDate(Date completionDate) {
    this.completionDate = completionDate;
  }

  public Exception getException() {
    return exception;
  }
  public void setException(Exception exception) {
    this.exception = exception;
  }

  public long getJobId() {
    return jobId;
  }

  public String getJobDescription() {
    return jobDescription;
  }

  public Date getStartDate() {
    return startDate;
  }
}

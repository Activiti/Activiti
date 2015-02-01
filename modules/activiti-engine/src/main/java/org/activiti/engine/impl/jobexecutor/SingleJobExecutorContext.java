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

import java.util.LinkedList;
import java.util.List;

import org.activiti.engine.impl.persistence.entity.JobEntity;

/**
 * @author Joram Barrez
 */
public class SingleJobExecutorContext implements JobExecutorContext {

  protected List<JobEntity> currentProcessorJobQueue = new LinkedList<JobEntity>();
  protected JobEntity currentJob;
        
  public List<JobEntity> getCurrentProcessorJobQueue() {
    return currentProcessorJobQueue;
  }

  public boolean isExecutingExclusiveJob() {
    return currentJob == null ? false : currentJob.isExclusive();
  }
     
  public void setCurrentJob(JobEntity currentJob) {
    this.currentJob = currentJob;
  }
    
  public JobEntity getCurrentJob() {
    return currentJob;
  }
}

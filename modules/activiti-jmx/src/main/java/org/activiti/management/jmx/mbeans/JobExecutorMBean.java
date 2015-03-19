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
package org.activiti.management.jmx.mbeans;

import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.jobexecutor.JobExecutor;
import org.activiti.management.jmx.annotations.ManagedAttribute;
import org.activiti.management.jmx.annotations.ManagedOperation;
import org.activiti.management.jmx.annotations.ManagedResource;

/**
 * @author Saeid Mirzaei
 */
@ManagedResource(description = "Job executor MBean")
public class JobExecutorMBean {

  JobExecutor jobExecutor;

  public JobExecutorMBean(ProcessEngineConfiguration processEngineConfig) {
    jobExecutor = processEngineConfig.getJobExecutor();
    
    
  }

  @ManagedAttribute(description = "check if the job executor is activated")
  public boolean isJobExecutorActivated() {
    return jobExecutor != null && jobExecutor.isActive();
  }

  @ManagedOperation(description = "set job executor activate")
  public void setJobExecutorActivate(Boolean active) {
    if (active)
      jobExecutor.start();
    else
      jobExecutor.shutdown();


  }

}

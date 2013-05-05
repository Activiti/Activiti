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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Daniel Meyer
 * @author Tijs Rademakers
 */
public class CallerRunsRejectedJobsHandler implements RejectedJobsHandler {
  
  private static Logger log = LoggerFactory.getLogger(CallerRunsRejectedJobsHandler.class);

  public void jobsRejected(JobExecutor jobExecutor, List<String> jobIds) {
    try {
      // execute rejected work in caller thread (potentially blocking job acquisition)
      new ExecuteJobsRunnable(jobExecutor, jobIds).run();
    } catch (Exception e) {
      log.error("Failed to execute rejected jobs " + jobIds, e);
    }
  }

}

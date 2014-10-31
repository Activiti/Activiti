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


import org.activiti.engine.impl.asyncexecutor.AsyncExecutor;
import org.activiti.engine.impl.cfg.TransactionListener;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Tijs Rademakers
 */
public class AsyncJobAddedNotification implements TransactionListener {
  
  private static Logger log = LoggerFactory.getLogger(AsyncJobAddedNotification.class);
  
  protected JobEntity job;
  protected AsyncExecutor asyncExecutor;
  
  public AsyncJobAddedNotification(JobEntity job, AsyncExecutor asyncExecutor) {
    this.job = job;
    this.asyncExecutor = asyncExecutor;
  }

  public void execute(CommandContext commandContext) {
    log.debug("notifying job executor of new job");
    asyncExecutor.executeAsyncJob(job);
  }
}

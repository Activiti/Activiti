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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.enterprise.concurrent.ManagedThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Simple JSR-236 job executor to allocate threads through {@link ManagedThreadFactory}. Falls back to {@link DefaultJobExecutor} 
 * when a thread factory was not referenced in configuration.
 * 
 * In Java EE 7, all application servers should provide access to a {@link ManagedThreadFactory}.
 * 
 * @author Dimitris Mandalidis
 */
public class ManagedJobExecutor extends DefaultJobExecutor {

  private static Logger log = LoggerFactory.getLogger(ManagedJobExecutor.class);
  
  protected ManagedThreadFactory threadFactory;
  
  public ManagedThreadFactory getThreadFactory() {
    return threadFactory;
  }
  
  
  public void setThreadFactory(ManagedThreadFactory threadFactory) {
    this.threadFactory = threadFactory;
  }
  
  @Override
  protected void startExecutingJobs() {
    if (threadFactory == null) {
      log.warn("A managed thread factory was not found, falling back to self-managed threads");
      super.startExecutingJobs();
    } else {
      if (threadPoolQueue==null) {
        threadPoolQueue = new ArrayBlockingQueue<Runnable>(queueSize);
      }
      if (threadPoolExecutor==null) {
        threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.MILLISECONDS, threadPoolQueue, threadFactory);      
        threadPoolExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
      }
    
      startJobAcquisitionThread();
    }
  }
  
}

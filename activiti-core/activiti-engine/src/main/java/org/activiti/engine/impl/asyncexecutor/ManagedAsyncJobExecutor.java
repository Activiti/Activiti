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
package org.activiti.engine.impl.asyncexecutor;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.enterprise.concurrent.ManagedThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple JSR-236 async job executor to allocate threads through {@link ManagedThreadFactory}. Falls back to {@link AsyncExecutor} when a thread factory was not referenced in configuration.
 * 
 * In Java EE 7, all application servers should provide access to a {@link ManagedThreadFactory}.
 * 

 */
public class ManagedAsyncJobExecutor extends DefaultAsyncJobExecutor {

  private static Logger log = LoggerFactory.getLogger(ManagedAsyncJobExecutor.class);

  protected ManagedThreadFactory threadFactory;

  public ManagedThreadFactory getThreadFactory() {
    return threadFactory;
  }

  public void setThreadFactory(ManagedThreadFactory threadFactory) {
    this.threadFactory = threadFactory;
  }

  protected void initAsyncJobExecutionThreadPool() {
    if (threadFactory == null) {
      log.warn("A managed thread factory was not found, falling back to self-managed threads");
      super.initAsyncJobExecutionThreadPool();
    } else {
      if (threadPoolQueue == null) {
        log.info("Creating thread pool queue of size {}", queueSize);
        threadPoolQueue = new ArrayBlockingQueue<Runnable>(queueSize);
      }

      if (executorService == null) {
        log.info("Creating executor service with corePoolSize {}, maxPoolSize {} and keepAliveTime {}", corePoolSize, maxPoolSize, keepAliveTime);

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.MILLISECONDS, threadPoolQueue, threadFactory);
        threadPoolExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executorService = threadPoolExecutor;

      }

      startJobAcquisitionThread();
    }
  }
}

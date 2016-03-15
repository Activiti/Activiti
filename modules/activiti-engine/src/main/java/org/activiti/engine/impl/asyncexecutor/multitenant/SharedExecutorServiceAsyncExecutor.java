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
package org.activiti.engine.impl.asyncexecutor.multitenant;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.activiti.engine.impl.asyncexecutor.AsyncExecutor;
import org.activiti.engine.impl.asyncexecutor.DefaultAsyncJobExecutor;
import org.activiti.engine.impl.asyncexecutor.ExecuteAsyncRunnableFactory;
import org.activiti.engine.impl.cfg.multitenant.TenantInfoHolder;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Multi tenant {@link AsyncExecutor}.
 * 
 * For each tenant, there will be acquire threads, but only one {@link ExecutorService} will be used
 * once the jobs are acquired.
 * 
 * @author Joram Barrez
 */
public class SharedExecutorServiceAsyncExecutor extends DefaultAsyncJobExecutor implements TenantAwareAsyncExecutor {
  
  private static final Logger logger = LoggerFactory.getLogger(SharedExecutorServiceAsyncExecutor.class);
  
  protected TenantInfoHolder tenantInfoHolder;
  
  protected Map<String, Thread> timerJobAcquisitionThreads = new HashMap<String, Thread>();
  protected Map<String, TenantAwareAcquireTimerJobsRunnable> timerJobAcquisitionRunnables 
    = new HashMap<String, TenantAwareAcquireTimerJobsRunnable>();
  
  protected Map<String, Thread> asyncJobAcquisitionThreads = new HashMap<String, Thread>();
  protected Map<String, TenantAwareAcquireAsyncJobsDueRunnable> asyncJobAcquisitionRunnables 
    = new HashMap<String, TenantAwareAcquireAsyncJobsDueRunnable>();
  
  public SharedExecutorServiceAsyncExecutor(TenantInfoHolder tenantInfoHolder) {
    this.tenantInfoHolder = tenantInfoHolder;
    
    setExecuteAsyncRunnableFactory(new ExecuteAsyncRunnableFactory() {
      
      public Runnable createExecuteAsyncRunnable(JobEntity jobEntity, CommandExecutor commandExecutor) {
        
        // Here, the runnable will be created by for example the acquire thread, which has already set the current id.
        // But it will be executed later on, by the executorService and thus we need to set it explicitely again then
        
        return new TenantAwareExecuteAsyncRunnable(jobEntity, commandExecutor, 
            SharedExecutorServiceAsyncExecutor.this.tenantInfoHolder, 
            SharedExecutorServiceAsyncExecutor.this.tenantInfoHolder.getCurrentTenantId());
      }
      
    });
  }

  @Override
  public Set<String> getTenantIds() {
    return timerJobAcquisitionThreads.keySet();
  }
  
  public void addTenantAsyncExecutor(String tenantId, boolean startExecutor) {
    
    TenantAwareAcquireTimerJobsRunnable timerRunnable = new TenantAwareAcquireTimerJobsRunnable(this, tenantInfoHolder, tenantId);
    timerJobAcquisitionRunnables.put(tenantId, timerRunnable);
    timerJobAcquisitionThreads.put(tenantId, new Thread(timerRunnable));
    
    TenantAwareAcquireAsyncJobsDueRunnable asyncJobsRunnable = new TenantAwareAcquireAsyncJobsDueRunnable(this, tenantInfoHolder, tenantId);
    asyncJobAcquisitionRunnables.put(tenantId, asyncJobsRunnable);
    asyncJobAcquisitionThreads.put(tenantId, new Thread(asyncJobsRunnable));
    
    if (startExecutor) {
      startTimerJobAcquisitionForTenant(tenantId);
      startAsyncJobAcquisitionForTenant(tenantId);
    }
  }
  
  @Override
  public void removeTenantAsyncExecutor(String tenantId) {
    stopThreadsForTenant(tenantId);
  }
  
  @Override
  protected void startJobAcquisitionThread() {
    for (String tenantId : timerJobAcquisitionThreads.keySet()) {
      startTimerJobAcquisitionForTenant(tenantId);
    }
    
    for (String tenantId : asyncJobAcquisitionThreads.keySet()) {
      asyncJobAcquisitionThreads.get(tenantId).start();
    }
  }

  protected  void startTimerJobAcquisitionForTenant(String tenantId) {
    timerJobAcquisitionThreads.get(tenantId).start();
  }
  
  protected  void startAsyncJobAcquisitionForTenant(String tenantId) {
    asyncJobAcquisitionThreads.get(tenantId).start();
  }
  
  @Override
  protected void stopJobAcquisitionThread() {
    for (String tenantId : timerJobAcquisitionRunnables.keySet()) {
      stopThreadsForTenant(tenantId);
    }
  }

  protected void stopThreadsForTenant(String tenantId) {
    timerJobAcquisitionRunnables.get(tenantId).stop();
    asyncJobAcquisitionRunnables.get(tenantId).stop();
    
    try {
      timerJobAcquisitionThreads.get(tenantId).join();
    } catch (InterruptedException e) {
      logger.warn("Interrupted while waiting for the timer job acquisition thread to terminate", e);
    }
    
    try {
      asyncJobAcquisitionThreads.get(tenantId).join();
    } catch (InterruptedException e) {
      logger.warn("Interrupted while waiting for the timer job acquisition thread to terminate", e);
    }
  }

}

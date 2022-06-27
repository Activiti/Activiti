/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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

import org.activiti.engine.impl.asyncexecutor.AsyncExecutor;
import org.activiti.engine.impl.asyncexecutor.DefaultAsyncJobExecutor;
import org.activiti.engine.impl.asyncexecutor.JobManager;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cfg.multitenant.TenantInfoHolder;
import org.activiti.engine.runtime.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link AsyncExecutor} that has one {@link AsyncExecutor} per tenant.
 * So each tenant has its own acquiring threads and it's own threadpool for executing jobs.
 *

 */
public class ExecutorPerTenantAsyncExecutor implements TenantAwareAsyncExecutor {

  private static final Logger logger = LoggerFactory.getLogger(ExecutorPerTenantAsyncExecutor.class);

  protected TenantInfoHolder tenantInfoHolder;
  protected TenantAwareAsyncExecutorFactory tenantAwareAyncExecutorFactory;

  protected Map<String, AsyncExecutor> tenantExecutors = new HashMap<String, AsyncExecutor>();

  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected boolean active;
  protected boolean autoActivate;

  public ExecutorPerTenantAsyncExecutor(TenantInfoHolder tenantInfoHolder) {
    this(tenantInfoHolder, null);
  }

  public ExecutorPerTenantAsyncExecutor(TenantInfoHolder tenantInfoHolder, TenantAwareAsyncExecutorFactory tenantAwareAyncExecutorFactory) {
    this.tenantInfoHolder = tenantInfoHolder;
    this.tenantAwareAyncExecutorFactory = tenantAwareAyncExecutorFactory;
  }

  @Override
  public Set<String> getTenantIds() {
    return tenantExecutors.keySet();
  }

  public void addTenantAsyncExecutor(String tenantId, boolean startExecutor) {
    AsyncExecutor tenantExecutor = null;

    if (tenantAwareAyncExecutorFactory == null) {
      tenantExecutor = new DefaultAsyncJobExecutor();
    } else {
      tenantExecutor = tenantAwareAyncExecutorFactory.createAsyncExecutor(tenantId);
    }

    tenantExecutor.setProcessEngineConfiguration(processEngineConfiguration);

    if (tenantExecutor instanceof DefaultAsyncJobExecutor) {
      DefaultAsyncJobExecutor defaultAsyncJobExecutor = (DefaultAsyncJobExecutor) tenantExecutor;
      defaultAsyncJobExecutor.setAsyncJobsDueRunnable(new TenantAwareAcquireAsyncJobsDueRunnable(defaultAsyncJobExecutor, tenantInfoHolder, tenantId));
      defaultAsyncJobExecutor.setTimerJobRunnable(new TenantAwareAcquireTimerJobsRunnable(defaultAsyncJobExecutor, tenantInfoHolder, tenantId));
      defaultAsyncJobExecutor.setExecuteAsyncRunnableFactory(new TenantAwareExecuteAsyncRunnableFactory(tenantInfoHolder, tenantId));
      defaultAsyncJobExecutor.setResetExpiredJobsRunnable(new TenantAwareResetExpiredJobsRunnable(defaultAsyncJobExecutor, tenantInfoHolder, tenantId));
    }

    tenantExecutors.put(tenantId, tenantExecutor);

    if (startExecutor) {
      tenantExecutor.start();
    }
  }

    @Override
    public void removeTenantAsyncExecutor(String tenantId) {
      shutdownTenantExecutor(tenantId);
      tenantExecutors.remove(tenantId);
    }

  protected AsyncExecutor determineAsyncExecutor() {
    return tenantExecutors.get(tenantInfoHolder.getCurrentTenantId());
  }

  public boolean executeAsyncJob(Job job) {
    return determineAsyncExecutor().executeAsyncJob(job);
  }

  public JobManager getJobManager() {
    // Should never be accessed on this class, should be accessed on the actual AsyncExecutor
    throw new UnsupportedOperationException();
  }

  @Override
  public void setProcessEngineConfiguration(ProcessEngineConfigurationImpl processEngineConfiguration) {
    this.processEngineConfiguration = processEngineConfiguration;
    for (AsyncExecutor asyncExecutor : tenantExecutors.values()) {
      asyncExecutor.setProcessEngineConfiguration(processEngineConfiguration);
    }
  }

  @Override
  public ProcessEngineConfigurationImpl getProcessEngineConfiguration() {
    throw new UnsupportedOperationException();
  }

  public boolean isAutoActivate() {
    return autoActivate;
  }

  public void setAutoActivate(boolean isAutoActivate) {
    autoActivate = isAutoActivate;
  }

  public boolean isActive() {
    return active;
  }

  public void start() {
    for (AsyncExecutor asyncExecutor : tenantExecutors.values()) {
      asyncExecutor.start();
    }
    active = true;
  }

  public synchronized void shutdown() {
    for (String tenantId : tenantExecutors.keySet()) {
      shutdownTenantExecutor(tenantId);
    }
    active = false;
  }

  protected void shutdownTenantExecutor(String tenantId) {
    logger.info("Shutting down async executor for tenant " + tenantId);
    tenantExecutors.get(tenantId).shutdown();
  }

  public String getLockOwner() {
    return determineAsyncExecutor().getLockOwner();
  }

  public int getTimerLockTimeInMillis() {
    return determineAsyncExecutor().getTimerLockTimeInMillis();
  }

  public void setTimerLockTimeInMillis(int lockTimeInMillis) {
    for (AsyncExecutor asyncExecutor : tenantExecutors.values()) {
      asyncExecutor.setTimerLockTimeInMillis(lockTimeInMillis);
    }
  }

  public int getAsyncJobLockTimeInMillis() {
    return determineAsyncExecutor().getAsyncJobLockTimeInMillis();
  }

  public void setAsyncJobLockTimeInMillis(int lockTimeInMillis) {
    for (AsyncExecutor asyncExecutor : tenantExecutors.values()) {
      asyncExecutor.setAsyncJobLockTimeInMillis(lockTimeInMillis);
    }
  }

  public int getDefaultTimerJobAcquireWaitTimeInMillis() {
    return determineAsyncExecutor().getDefaultTimerJobAcquireWaitTimeInMillis();
  }

  public void setDefaultTimerJobAcquireWaitTimeInMillis(int waitTimeInMillis) {
    for (AsyncExecutor asyncExecutor : tenantExecutors.values()) {
      asyncExecutor.setDefaultTimerJobAcquireWaitTimeInMillis(waitTimeInMillis);
    }
  }

  public int getDefaultAsyncJobAcquireWaitTimeInMillis() {
    return determineAsyncExecutor().getDefaultAsyncJobAcquireWaitTimeInMillis();
  }

  public void setDefaultAsyncJobAcquireWaitTimeInMillis(int waitTimeInMillis) {
    for (AsyncExecutor asyncExecutor : tenantExecutors.values()) {
      asyncExecutor.setDefaultAsyncJobAcquireWaitTimeInMillis(waitTimeInMillis);
    }
  }

  public int getDefaultQueueSizeFullWaitTimeInMillis() {
    return determineAsyncExecutor().getDefaultQueueSizeFullWaitTimeInMillis();
  }

  public void setDefaultQueueSizeFullWaitTimeInMillis(int defaultQueueSizeFullWaitTimeInMillis) {
    for (AsyncExecutor asyncExecutor : tenantExecutors.values()) {
      asyncExecutor.setDefaultQueueSizeFullWaitTimeInMillis(defaultQueueSizeFullWaitTimeInMillis);
    }
  }

  public int getMaxAsyncJobsDuePerAcquisition() {
    return determineAsyncExecutor().getMaxAsyncJobsDuePerAcquisition();
  }

  public void setMaxAsyncJobsDuePerAcquisition(int maxJobs) {
    for (AsyncExecutor asyncExecutor : tenantExecutors.values()) {
      asyncExecutor.setMaxAsyncJobsDuePerAcquisition(maxJobs);
    }
  }

  public int getMaxTimerJobsPerAcquisition() {
    return determineAsyncExecutor().getMaxTimerJobsPerAcquisition();
  }

  public void setMaxTimerJobsPerAcquisition(int maxJobs) {
    for (AsyncExecutor asyncExecutor : tenantExecutors.values()) {
      asyncExecutor.setMaxTimerJobsPerAcquisition(maxJobs);
    }
  }

  public int getRetryWaitTimeInMillis() {
    return determineAsyncExecutor().getRetryWaitTimeInMillis();
  }

  public void setRetryWaitTimeInMillis(int retryWaitTimeInMillis) {
    for (AsyncExecutor asyncExecutor : tenantExecutors.values()) {
      asyncExecutor.setRetryWaitTimeInMillis(retryWaitTimeInMillis);
    }
  }

  @Override
  public int getResetExpiredJobsInterval() {
    return determineAsyncExecutor().getResetExpiredJobsInterval();
  }

  @Override
  public void setResetExpiredJobsInterval(int resetExpiredJobsInterval) {
    for (AsyncExecutor asyncExecutor : tenantExecutors.values()) {
      asyncExecutor.setResetExpiredJobsInterval(resetExpiredJobsInterval);
    }
  }

  @Override
  public int getResetExpiredJobsPageSize() {
    return determineAsyncExecutor().getResetExpiredJobsPageSize();
  }

  @Override
  public void setResetExpiredJobsPageSize(int resetExpiredJobsPageSize) {
    for (AsyncExecutor asyncExecutor : tenantExecutors.values()) {
      asyncExecutor.setResetExpiredJobsPageSize(resetExpiredJobsPageSize);
    }
  }

}

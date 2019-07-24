package org.activiti.spring.boot;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.activiti.async-executor")
public class AsyncExecutorProperties {
    private int retryWaitTimeInMillis = 500;
    
    /**
     * The number of retries for a job. Default value is 3.
     */
    private int numberOfRetries = 3;

    /**
     * The minimal number of threads that are kept alive in the threadpool for job
     * execution. Default value = 2. 
     */
    private int corePoolSize = 2;

    /**
     * The maximum number of threads that are created in the threadpool for job
     * execution. Default value = 10. 
     */
    private int maxPoolSize = 10;

    /**
     * The time (in milliseconds) a thread used for job execution must be kept
     * alive before it is destroyed. Default setting is 5 seconds. Having a
     * setting > 0 takes resources, but in the case of many job executions it
     * avoids creating new threads all the time. If 0, threads will be destroyed
     * after they've been used for job execution.
     *
     */
    private long keepAliveTime = 5000L;

    /**
     * The size of the queue on which jobs to be executed are placed, before they
     * are actually executed. Default value = 100. 
     */
    private int queueSize = 100;

    /**
     * The time (in seconds) that is waited to gracefully shut down the threadpool
     * used for job execution when the a shutdown on the executor (or process
     * engine) is requested. Default value = 60.
     *
     */
    private long secondsToWaitOnShutdown = 60L;

    /**
     * The number of timer jobs that are acquired during one query (before a job
     * is executed, an acquirement thread fetches jobs from the database and puts
     * them on the queue).
     *
     * Default value = 1, as this lowers the potential on optimistic locking
     * exceptions. Change this value if you know what you are doing.
     *
     */
    private int maxTimerJobsPerAcquisition = 1;

    /**
     * The number of async jobs that are acquired during one query (before a job
     * is executed, an acquirement thread fetches jobs from the database and puts
     * them on the queue).
     *
     * Default value = 1, as this lowers the potential on optimistic locking
     * exceptions. Change this value if you know what you are doing.
     *
     */
    private int maxAsyncJobsDuePerAcquisition = 1;

    /**
     * The time (in milliseconds) the timer acquisition thread will wait to
     * execute the next acquirement query. This happens when no new timer jobs
     * were found or when less timer jobs have been fetched than set. Default value = 10
     * seconds.
     *
     */
    private int defaultTimerJobAcquireWaitTimeInMillis = 10 * 1000;

    /**
     * The time (in milliseconds) the async job acquisition thread will wait to
     * execute the next acquirement query. This happens when no new async jobs
     * were found or when less async jobs have been fetched than set. 
     * 
     * Default value = 10 seconds.
     *
     */
    private int defaultAsyncJobAcquireWaitTimeInMillis = 10 * 1000;

    /**
     * The time (in milliseconds) the async job (both timer and async continuations) acquisition thread will
     * wait when the queueu is full to execute the next query. By default set to 0 (for backwards compatibility)
     */
    private int defaultQueueSizeFullWaitTime = 0;

    /**
     * The amount of time (in milliseconds) a timer job is locked when acquired by
     * the async executor. During this period of time, no other async executor
     * will try to acquire and lock this job.
     *
     * Default value = 5 minutes;
     *
     */
    private int timerLockTimeInMillis = 5 * 60 * 1000;

    /**
     * The amount of time (in milliseconds) an async job is locked when acquired
     * by the async executor. During this period of time, no other async executor
     * will try to acquire and lock this job.
     *
     * Default value = 5 minutes;
     *
     */
    private int asyncJobLockTimeInMillis = 5 * 60 * 1000;

    /**
     * The amount of time (in milliseconds) that is between two consecutive checks
     * of 'expired jobs'. Expired jobs are jobs that were locked (a lock owner + time
     * was written by some executor, but the job was never completed).
     *
     * During such a check, jobs that are expired are again made available,
     * meaning the lock owner and lock time will be removed. Other executors
     * will now be able to pick it up.
     *
     * A job is deemed expired if the lock time is before the current date.
     *
     * By default one minute.
     */
    private int resetExpiredJobsInterval = 60 * 1000;

    /**
     * The 'cleanup' thread resets expired jobs  so they can be re-acquired by other executors. 
     * This setting defines the size of the page being used when fetching these expired jobs.
     */
    private int resetExpiredJobsPageSize = 3;

    /**
     * Set this to true when using the message queue based job executor. Default is false.
     */
    private boolean messageQueueMode = false;
    
    public int getCorePoolSize() {
        return corePoolSize;
    }
    
    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }
    
    public int getMaxPoolSize() {
        return maxPoolSize;
    }
    
    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }
    
    public long getKeepAliveTime() {
        return keepAliveTime;
    }
    
    public void setKeepAliveTime(long keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
    }
    
    public int getQueueSize() {
        return queueSize;
    }
    
    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }
    
    public long getSecondsToWaitOnShutdown() {
        return secondsToWaitOnShutdown;
    }
    
    public void setSecondsToWaitOnShutdown(long secondsToWaitOnShutdown) {
        this.secondsToWaitOnShutdown = secondsToWaitOnShutdown;
    }
    
    public boolean isMessageQueueMode() {
        return messageQueueMode;
    }
    
    public void setMessageQueueMode(boolean isMessageQueueMode) {
        this.messageQueueMode = isMessageQueueMode;
    }
    
    public int getMaxTimerJobsPerAcquisition() {
        return maxTimerJobsPerAcquisition;
    }
    
    public void setMaxTimerJobsPerAcquisition(int maxTimerJobsPerAcquisition) {
        this.maxTimerJobsPerAcquisition = maxTimerJobsPerAcquisition;
    }
    
    public int getMaxAsyncJobsDuePerAcquisition() {
        return maxAsyncJobsDuePerAcquisition;
    }
    
    public void setMaxAsyncJobsDuePerAcquisition(int maxAsyncJobsDuePerAcquisition) {
        this.maxAsyncJobsDuePerAcquisition = maxAsyncJobsDuePerAcquisition;
    }
    
    public int getDefaultTimerJobAcquireWaitTimeInMillis() {
        return defaultTimerJobAcquireWaitTimeInMillis;
    }
    
    public void setDefaultTimerJobAcquireWaitTimeInMillis(int defaultTimerJobAcquireWaitTimeInMillis) {
        this.defaultTimerJobAcquireWaitTimeInMillis = defaultTimerJobAcquireWaitTimeInMillis;
    }
    
    public int getDefaultAsyncJobAcquireWaitTimeInMillis() {
        return defaultAsyncJobAcquireWaitTimeInMillis;
    }
    
    public void setDefaultAsyncJobAcquireWaitTimeInMillis(int defaultAsyncJobAcquireWaitTimeInMillis) {
        this.defaultAsyncJobAcquireWaitTimeInMillis = defaultAsyncJobAcquireWaitTimeInMillis;
    }
    
    public int getDefaultQueueSizeFullWaitTime() {
        return defaultQueueSizeFullWaitTime;
    }
    
    public void setDefaultQueueSizeFullWaitTime(int defaultQueueSizeFullWaitTime) {
        this.defaultQueueSizeFullWaitTime = defaultQueueSizeFullWaitTime;
    }
    
    public int getTimerLockTimeInMillis() {
        return timerLockTimeInMillis;
    }
    
    public void setTimerLockTimeInMillis(int timerLockTimeInMillis) {
        this.timerLockTimeInMillis = timerLockTimeInMillis;
    }
    
    public int getAsyncJobLockTimeInMillis() {
        return asyncJobLockTimeInMillis;
    }
    
    public void setAsyncJobLockTimeInMillis(int asyncJobLockTimeInMillis) {
        this.asyncJobLockTimeInMillis = asyncJobLockTimeInMillis;
    }
    
    public int getRetryWaitTimeInMillis() {
        return retryWaitTimeInMillis;
    }
    
    public void setRetryWaitTimeInMillis(int retryWaitTimeInMillis) {
        this.retryWaitTimeInMillis = retryWaitTimeInMillis;
    }
    
    public int getResetExpiredJobsInterval() {
        return resetExpiredJobsInterval;
    }
    
    public void setResetExpiredJobsInterval(int resetExpiredJobsInterval) {
        this.resetExpiredJobsInterval = resetExpiredJobsInterval;
    }
    
    public int getResetExpiredJobsPageSize() {
        return resetExpiredJobsPageSize;
    }
    
    public void setResetExpiredJobsPageSize(int resetExpiredJobsPageSize) {
        this.resetExpiredJobsPageSize = resetExpiredJobsPageSize;
    }

    public int getNumberOfRetries() {
        return numberOfRetries;
    }

    public void setNumberOfRetries(int numberOfRetries) {
        this.numberOfRetries = numberOfRetries;
    }
}

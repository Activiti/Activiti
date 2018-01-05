package org.activiti.async.executor.hazelcast;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.activiti.engine.impl.asyncexecutor.AsyncExecutor;
import org.activiti.engine.impl.asyncexecutor.DefaultAsyncJobExecutor;
import org.activiti.engine.impl.asyncexecutor.ExecuteAsyncRunnable;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.core.IQueue;
import com.hazelcast.monitor.LocalQueueStats;

/**
 * Note: very experimental and untested!
 *
 * Implementation of the Activiti {@link AsyncExecutor} using a distributed queue where the jobs
 * to be executed are put on. One of the distributed nodes will take the job off the queue, 
 * and hand it off the local thread pool.
 *
 * Needs a config file (hazelcast.xml on the classpath) that defines a queue with the name 'activiti':
 *
 * for example:
 *
 * <hazelcast xsi:schemaLocation="http://www.hazelcast.com/schema/config hazelcast-config-3.3.xsd"
 xmlns="http://www.hazelcast.com/schema/config"
 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
 <group>
 ...
 </group>

 <network>
 ...
 </network>


 <queue name="activiti">
 <!--
 Maximum size of the queue. When a JVM's local queue size reaches the maximum,
 all put/offer operations will get blocked until the queue size
 of the JVM goes down below the maximum.
 Any integer between 0 and Integer.MAX_VALUE. 0 means
 Integer.MAX_VALUE. Default is 0.
 -->
 <max-size>1024</max-size>
 <!--
 Number of backups. If 1 is set as the backup-count for example,
 then all entries of the map will be copied to another JVM for
 fail-safety. 0 means no backup.
 -->
 <backup-count>0</backup-count>

 <!--
 Number of async backups. 0 means no backup.
 -->
 <async-backup-count>0</async-backup-count>

 <empty-queue-ttl>-1</empty-queue-ttl>
 </queue>


 </hazelcast>
 *
 * @author Joram Barrez
 */
public class HazelCastDistributedQueueBasedAsyncExecutor extends DefaultAsyncJobExecutor {

    private static final Logger logger = LoggerFactory.getLogger(HazelCastDistributedQueueBasedAsyncExecutor.class);

    private static final String QUEUE_NAME = "activiti";

    // Runtime
    protected HazelcastInstance hazelcastInstance;
    protected IQueue<JobEntity> jobQueue;

    protected Thread jobQueueListenerThread;
    protected BlockingQueue<Runnable> threadPoolQueue;
    protected ExecutorService executorService;

    @Override
    public void start() {
        if (isActive) {
            return;
        }

        logger.info("Starting up the Hazelcast async job executor [{}].", getClass().getName());

        hazelcastInstance = Hazelcast.newHazelcastInstance();
        jobQueue = hazelcastInstance.getQueue(QUEUE_NAME);

        threadPoolQueue = new ArrayBlockingQueue<Runnable>(queueSize);
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.MILLISECONDS, threadPoolQueue);
        threadPoolExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executorService = threadPoolExecutor;

        super.start();

        // Needs to be done afterwards, since it depends on isActive
        initJobQueueListener();
    }

    protected void initJobQueueListener() {
        jobQueueListenerThread = new Thread(new Runnable() {

            public void run() {
                while (isActive) {
                    JobEntity job = null;
                    try {
                        job = jobQueue.take(); // Blocking
                    } catch (InterruptedException e1) {
                        logger.info("jobQueueListenerThread interrupted. This is fine if the job executor is shutting down");
                        // Do nothing, this can happen when shutting down
                    } catch (HazelcastInstanceNotActiveException notActiveException) {
                        logger.info("Hazel cast not active exception caught. This is fine if the job executor is shutting down");
                    }

                    if (job != null) {
                        executorService.execute(new ExecuteAsyncRunnable(job, commandExecutor));
                    }
                }
            }
        });
        jobQueueListenerThread.start();
    }

    @Override
    public void shutdown() {

        super.shutdown();

        // Shut down local execution service
        try {
            logger.info("Shutting down local executor service");
            executorService.shutdown();
            executorService.awaitTermination(secondsToWaitOnShutdown, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.warn("Exception while waiting for executor service shutdown", e);
        }

        // Shut down hazelcast
        try {
            LocalQueueStats localQueueStats = jobQueue.getLocalQueueStats();
            logger.info("This async job executor has processed " + localQueueStats.getPollOperationCount());
            hazelcastInstance.shutdown();
        } catch (HazelcastInstanceNotActiveException e) {
            // Nothing to do
        }

        // Shut down listener thread
        isActive = false;
        try {
            logger.info("Shutting down jobQueueListenerThread");
            jobQueueListenerThread.interrupt();
            jobQueueListenerThread.join();
        } catch (InterruptedException e) {
            logger.warn("jobQueueListenerThread join was interrupted", e);
        }
    }

    @Override
    public boolean executeAsyncJob(JobEntity job) {
        try {
            jobQueue.put(job);
            return true;
        } catch (InterruptedException e) {
            // When a RejectedExecutionException is caught, this means that the queue for holding the jobs
            // that are to be executed is full and can't store more.
            // Return false so the job can be unlocked and (if wanted) the acquiring can be throttled.
            return false;
        }
    }

}

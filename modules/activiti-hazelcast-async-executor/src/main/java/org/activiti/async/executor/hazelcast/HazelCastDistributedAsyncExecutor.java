package org.activiti.async.executor.hazelcast;

import java.io.Serializable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.impl.asyncexecutor.AsyncExecutor;
import org.activiti.engine.impl.asyncexecutor.DefaultAsyncJobExecutor;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cmd.ExecuteAsyncJobCmd;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.monitor.LocalExecutorStats;

/**
 * Note: very experimental and untested!
 *
 * Implementation of the Activiti {@link AsyncExecutor} using a distributed {@link ExecutorService}
 * from {@link Hazelcast}, the {@link IExecutorService}.
 *
 * Needs a config file (hazelcast.xml on the classpath) that defines a executor service with the name 'activiti': 
 *
 * <hazelcast xsi:schemaLocation="http://www.hazelcast.com/schema/config hazelcast-config-3.3.xsd"
 xmlns="http://www.hazelcast.com/schema/config"
 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
 <group>
 ..
 </group>

 <network>
 ..
 </network>

 <partition-group enabled="false"/>

 <executor-service name="activiti">
 <pool-size>200</pool-size>
 Queue capacity. 0 means Integer.MAX_VALUE.
 <queue-capacity>1024</queue-capacity>
 </executor-service>

 ...
 *
 * @author Joram Barrez
 */
public class HazelCastDistributedAsyncExecutor extends DefaultAsyncJobExecutor {

    private static final Logger logger = LoggerFactory.getLogger(HazelCastDistributedAsyncExecutor.class);

    private static final String EXECUTOR_NAME = "activiti";

    // Runtime
    private HazelcastInstance hazelcastInstance;
    private IExecutorService executorService;

    @Override
    public void start() {
        if (isActive) {
            return;
        }

        logger.info("Starting up the Hazelcast async job executor [{}].", getClass().getName());

        hazelcastInstance = Hazelcast.newHazelcastInstance();
        executorService = hazelcastInstance.getExecutorService(EXECUTOR_NAME);

        // Starts up all acquire threads, etc
        super.start();
    }

    @Override
    public void shutdown() {

        super.shutdown();

        try {
            executorService.shutdown();
            executorService.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.warn("Exception while waiting for executor service shutdown", e);
        }

        LocalExecutorStats localExecutorStats = executorService.getLocalExecutorStats();
        logger.info("This async job executor has processed " + localExecutorStats.getCompletedTaskCount()
                + " jobs. Total execution time = " + localExecutorStats.getTotalExecutionLatency());

        hazelcastInstance.shutdown();
    }

    @Override
    public boolean executeAsyncJob(JobEntity job) {
        try {
            executorService.submit(new DistributedExecuteJobRunnable(job));
            return true;
        } catch (RejectedExecutionException e) {
            logger.info("Async job execution rejected. Executing job in calling thread.");
            // Execute in calling thread so the job executor can be freed
            commandExecutor.execute(new ExecuteAsyncJobCmd(job));
            return false;
        }
    }

    public static class DistributedExecuteJobRunnable implements Runnable,
                                                                 Serializable {

        private static final long serialVersionUID = -6294645802377574363L;

        protected JobEntity job;

        public DistributedExecuteJobRunnable(JobEntity job) {
            this.job = job;
        }

        @Override
        public void run() {
            CommandExecutor commandExecutor = ((ProcessEngineConfigurationImpl) ProcessEngines.getDefaultProcessEngine()
                    .getProcessEngineConfiguration()).getCommandExecutor();
            commandExecutor.execute(new ExecuteAsyncJobCmd(job));
        }
    }
}

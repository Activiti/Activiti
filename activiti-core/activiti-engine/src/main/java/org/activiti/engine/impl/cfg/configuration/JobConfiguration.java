/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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

package org.activiti.engine.impl.cfg.configuration;

import java.util.HashMap;
import java.util.List;

import org.activiti.engine.impl.asyncexecutor.DefaultAsyncJobExecutor;
import org.activiti.engine.impl.asyncexecutor.DefaultJobManager;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.jobexecutor.AsyncContinuationJobHandler;
import org.activiti.engine.impl.jobexecutor.JobHandler;
import org.activiti.engine.impl.jobexecutor.ProcessEventJobHandler;
import org.activiti.engine.impl.jobexecutor.TimerActivateProcessDefinitionHandler;
import org.activiti.engine.impl.jobexecutor.TimerStartEventJobHandler;
import org.activiti.engine.impl.jobexecutor.TimerSuspendProcessDefinitionHandler;
import org.activiti.engine.impl.jobexecutor.TriggerTimerEventJobHandler;

/**
 * Job configuration for job execution components like job handlers, 
 * job manager, and async executor.
 */
public class JobConfiguration {

    private final ProcessEngineConfigurationImpl config;

    public JobConfiguration(ProcessEngineConfigurationImpl config) {
        this.config = config;
    }

    /**
     * Initialize all job components.
     */
    public void configure() {
        initJobHandlers();
        initJobManager();
        initAsyncExecutor();
    }

    public void initJobHandlers() {
        config.setJobHandlers(new HashMap<String, JobHandler>());

        AsyncContinuationJobHandler asyncContinuationJobHandler = new AsyncContinuationJobHandler();
        config.getJobHandlers().put(asyncContinuationJobHandler.getType(), asyncContinuationJobHandler);

        TriggerTimerEventJobHandler triggerTimerEventJobHandler = new TriggerTimerEventJobHandler();
        config.getJobHandlers().put(triggerTimerEventJobHandler.getType(), triggerTimerEventJobHandler);

        TimerStartEventJobHandler timerStartEvent = new TimerStartEventJobHandler();
        config.getJobHandlers().put(timerStartEvent.getType(), timerStartEvent);

        TimerSuspendProcessDefinitionHandler suspendProcessDefinitionHandler =
            new TimerSuspendProcessDefinitionHandler();
        config.getJobHandlers().put(suspendProcessDefinitionHandler.getType(), suspendProcessDefinitionHandler);

        TimerActivateProcessDefinitionHandler activateProcessDefinitionHandler =
            new TimerActivateProcessDefinitionHandler();
        config.getJobHandlers().put(activateProcessDefinitionHandler.getType(), activateProcessDefinitionHandler);

        ProcessEventJobHandler processEventJobHandler = new ProcessEventJobHandler();
        config.getJobHandlers().put(processEventJobHandler.getType(), processEventJobHandler);

        // if we have custom job handlers, register them
        List<JobHandler> customJobHandlers = config.getCustomJobHandlers();
        if (customJobHandlers != null) {
            for (JobHandler customJobHandler : customJobHandlers) {
                config.getJobHandlers().put(customJobHandler.getType(), customJobHandler);
            }
        }
    }

    public void initJobManager() {
        if (config.getJobManager() == null) {
            config.setJobManager(new DefaultJobManager(config));
        }

        config.getJobManager().setProcessEngineConfiguration(config);
    }

    public void initAsyncExecutor() {
        if (config.getAsyncExecutor() == null) {
            DefaultAsyncJobExecutor defaultAsyncExecutor = new DefaultAsyncJobExecutor();
            defaultAsyncExecutor.applyConfig(config);
            config.setAsyncExecutor(defaultAsyncExecutor);
        }

        config.getAsyncExecutor().setProcessEngineConfiguration(config);
        config.getAsyncExecutor().setAutoActivate(config.isAsyncExecutorActivate());
    }
}
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
package org.activiti.spring.boot;

import org.activiti.engine.ApplicationStatusHolder;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;

public class ShutdownListener implements ApplicationListener<ContextClosedEvent>, PriorityOrdered {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShutdownListener.class);

    private final ProcessEngineConfigurationImpl processEngineConfiguration;

    public ShutdownListener(ProcessEngineConfigurationImpl processEngineConfiguration) {
        this.processEngineConfiguration = processEngineConfiguration;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        if (event.getApplicationContext().getParent() == null) {
            LOGGER.info("Setting application status to shutdown");
            processEngineConfiguration.getAsyncExecutor().shutdown();
            ApplicationStatusHolder.shutdown();
        }
    }
    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}

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

import org.activiti.engine.impl.asyncexecutor.AsyncExecutor;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextClosedEvent;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ShutdownListenerIT {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ProcessEngineConfigurationImpl processEngineConfiguration;

    private AsyncExecutor asyncExecutor;

    @BeforeEach
    public void setUp() {
        asyncExecutor = processEngineConfiguration.getAsyncExecutor();
        asyncExecutor.start();
    }

    @Test
    public void should_shutdownAsyncExecutor_when_shutdownApplication() {
        assertThat(asyncExecutor.isActive()).isTrue();
        // Trigger the shutdown event
        applicationContext.publishEvent(new ContextClosedEvent(applicationContext));
        assertThat(asyncExecutor.isActive()).isFalse();
    }

}

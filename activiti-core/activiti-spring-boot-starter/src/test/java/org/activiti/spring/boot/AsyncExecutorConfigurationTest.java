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
package org.activiti.spring.boot;

import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.spring.SpringAsyncExecutor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource("classpath:application-async-executor.properties")
public class AsyncExecutorConfigurationTest {

    @Autowired
    private SpringAsyncExecutor asyncExecutor;

    @Autowired
    private AsyncExecutorProperties properties;

    @Test
    public void shouldConfigureAsyncExecutorProperties() {
        assertThat(asyncExecutor.getDefaultAsyncJobAcquireWaitTimeInMillis()).isEqualTo(properties.getDefaultAsyncJobAcquireWaitTimeInMillis());
        assertThat(asyncExecutor.getDefaultTimerJobAcquireWaitTimeInMillis()).isEqualTo(properties.getDefaultTimerJobAcquireWaitTimeInMillis());
        assertThat(asyncExecutor.isMessageQueueMode()).isEqualTo(properties.isMessageQueueMode());
        assertThat(asyncExecutor.getAsyncJobLockTimeInMillis()).isEqualTo(properties.getAsyncJobLockTimeInMillis());
        assertThat(asyncExecutor.getCorePoolSize()).isEqualTo(properties.getCorePoolSize());
        assertThat(asyncExecutor.getDefaultQueueSizeFullWaitTimeInMillis()).isEqualTo(properties.getDefaultQueueSizeFullWaitTime());
        assertThat(asyncExecutor.getKeepAliveTime()).isEqualTo(properties.getKeepAliveTime());
        assertThat(asyncExecutor.getMaxAsyncJobsDuePerAcquisition()).isEqualTo(properties.getMaxAsyncJobsDuePerAcquisition());
        assertThat(asyncExecutor.getRetryWaitTimeInMillis()).isEqualTo(properties.getRetryWaitTimeInMillis());
        assertThat(asyncExecutor.getQueueSize()).isEqualTo(properties.getQueueSize());
        assertThat(asyncExecutor.getResetExpiredJobsInterval()).isEqualTo(properties.getResetExpiredJobsInterval());
        assertThat(asyncExecutor.getResetExpiredJobsPageSize()).isEqualTo(properties.getResetExpiredJobsPageSize());
        assertThat(asyncExecutor.getSecondsToWaitOnShutdown()).isEqualTo(properties.getSecondsToWaitOnShutdown());
        assertThat(asyncExecutor.getTimerLockTimeInMillis()).isEqualTo(properties.getTimerLockTimeInMillis());
    }
}

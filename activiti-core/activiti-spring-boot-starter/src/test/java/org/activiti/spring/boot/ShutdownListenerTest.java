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
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class ShutdownListenerTest {

    @Test
    void should_MarkAsShutdown_when_CloseApplicationContext() {
        ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(Application.class);
        contextRunner.run((context) -> {
            assertThat(ApplicationStatusHolder.isRunning()).isTrue();
            assertThat(ApplicationStatusHolder.isShutdownInProgress()).isFalse();
            context.close();
            assertThat(ApplicationStatusHolder.isRunning()).isFalse();
            assertThat(ApplicationStatusHolder.isShutdownInProgress()).isTrue();
        });
    }

}

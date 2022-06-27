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
package org.activiti.test.conf;

import org.activiti.test.operations.AwaitableProcessOperations;
import org.activiti.test.operations.AwaitableTaskOperations;
import org.activiti.test.operations.ProcessOperations;
import org.activiti.test.operations.TaskOperations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AssertionsAPIAutoConfiguration {

    @Bean
    public ProcessOperations processOperations(ProcessOperations processRuntimeOperations,
                                               @Value("${activiti.assertions.await.enabled:false}") boolean awaitEnabled) {
        return new AwaitableProcessOperations(processRuntimeOperations,
                                              awaitEnabled);
    }

    @Bean
    public TaskOperations taskOperations(TaskOperations taskRuntimeOperations,
                                         @Value("${activiti.assertions.await.enabled:false}") boolean awaitEnabled) {
        return new AwaitableTaskOperations(
                taskRuntimeOperations,
                awaitEnabled);
    }
}

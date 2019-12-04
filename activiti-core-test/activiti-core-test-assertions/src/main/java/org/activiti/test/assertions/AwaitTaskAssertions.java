/*
 * Copyright 2019 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.test.assertions;

import org.activiti.api.task.model.Task;
import org.activiti.test.matchers.OperationScopeMatcher;
import org.activiti.test.matchers.ProcessTaskMatcher;
import org.activiti.test.matchers.TaskResultMatcher;

import static org.awaitility.Awaitility.await;

public class AwaitTaskAssertions implements TaskAssertions {

    private TaskAssertions taskAssertions;

    public AwaitTaskAssertions(TaskAssertions taskAssertions) {
        this.taskAssertions = taskAssertions;
    }

    @Override
    public TaskAssertions expectEvents(OperationScopeMatcher... matchers) {
        await().untilAsserted(() -> taskAssertions.expectEvents(matchers));
        return this;
    }

    @Override
    public TaskAssertions expectFields(TaskResultMatcher... matchers) {
        await().untilAsserted(() -> taskAssertions.expectFields(matchers));
        return this;
    }

    @Override
    public TaskAssertions expect(ProcessTaskMatcher... matchers) {
        await().untilAsserted(() -> taskAssertions.expect(matchers));
        return this;
    }

    @Override
    public Task andReturn() {
        return taskAssertions.andReturn();
    }
}

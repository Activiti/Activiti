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
package org.activiti.test.assertions;

import java.util.List;

import org.activiti.api.model.shared.event.RuntimeEvent;
import org.activiti.api.task.model.Task;
import org.activiti.test.EventSource;
import org.activiti.test.TaskSource;
import org.activiti.test.matchers.OperationScopeMatcher;
import org.activiti.test.matchers.ProcessTaskMatcher;
import org.activiti.test.matchers.TaskResultMatcher;

import static org.activiti.test.matchers.OperationScopeImpl.scope;

public class TaskAssertionsImpl implements TaskAssertions {

    private Task task;

    private EventSource eventSource;

    private List<TaskSource> taskSources;

    public TaskAssertionsImpl(Task task,
                              List<TaskSource> taskSources,
                              EventSource eventSource) {
        this.task = task;
        this.taskSources = taskSources;
        this.eventSource = eventSource;
    }

    @Override
    public TaskAssertions expectEvents(OperationScopeMatcher... matchers) {
        List<RuntimeEvent<?, ?>> events = eventSource.getEvents();
        for (OperationScopeMatcher matcher : matchers) {
            matcher.match(scope(task.getProcessInstanceId(),
                                task.getId()),
                          events);
        }
        return this;
    }

    @Override
    public TaskAssertions expectFields(TaskResultMatcher... matchers) {
        for (TaskResultMatcher matcher : matchers) {
            matcher.match(task);
        }
        return this;
    }

    @Override
    public TaskAssertions expect(ProcessTaskMatcher... matchers) {
        for (ProcessTaskMatcher matcher : matchers) {
            matcher.match(task.getProcessInstanceId(),
                          taskSources);
        }
        return this;
    }

    @Override
    public Task andReturn() {
        return task;
    }
}

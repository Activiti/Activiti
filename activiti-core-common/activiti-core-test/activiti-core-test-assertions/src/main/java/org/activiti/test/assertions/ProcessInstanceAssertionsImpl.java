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
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.test.EventSource;
import org.activiti.test.TaskSource;
import org.activiti.test.matchers.OperationScopeMatcher;
import org.activiti.test.matchers.ProcessResultMatcher;
import org.activiti.test.matchers.ProcessTaskMatcher;

import static org.activiti.test.matchers.OperationScopeImpl.processInstanceScope;

public class ProcessInstanceAssertionsImpl implements ProcessInstanceAssertions {

    private EventSource eventSource;

    private List<TaskSource> taskSources;
    private ProcessInstance processInstance;

    public ProcessInstanceAssertionsImpl(EventSource eventSource,
                                         List<TaskSource> taskSources,
                                         ProcessInstance processInstance) {
        this.eventSource = eventSource;
        this.taskSources = taskSources;
        this.processInstance = processInstance;
    }

    @Override
    public ProcessInstanceAssertions expectFields(ProcessResultMatcher... processResultMatcher) {
        List<RuntimeEvent<?, ?>> events = eventSource.getEvents();
        for (ProcessResultMatcher matcher : processResultMatcher) {
            matcher.match(processInstance);
        }
        return this;
    }

    @Override
    public ProcessInstanceAssertions expectEvents(OperationScopeMatcher... matchers) {
        List<RuntimeEvent<?, ?>> events = eventSource.getEvents();
        for (OperationScopeMatcher matcher : matchers) {
            matcher.match(processInstanceScope(processInstance.getId()),
                          events);
        }
        return this;
    }

    @Override
    public ProcessInstanceAssertions expect(ProcessTaskMatcher... matchers) {
        for (ProcessTaskMatcher matcher : matchers) {
            matcher.match(processInstance.getId(),
                          taskSources);
        }
        return this;
    }

    @Override
    public ProcessInstance andReturn() {
        return processInstance;
    }
}

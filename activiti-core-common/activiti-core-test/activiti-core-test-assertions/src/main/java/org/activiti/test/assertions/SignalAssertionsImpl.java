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

import java.util.List;

import org.activiti.api.model.shared.event.RuntimeEvent;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.test.EventSource;
import org.activiti.test.matchers.OperationScopeMatcher;

import static org.activiti.test.matchers.OperationScopeImpl.processInstanceScope;

public class SignalAssertionsImpl implements SignalAssertions {

    private EventSource eventSource;

    public SignalAssertionsImpl(EventSource eventSource) {

        this.eventSource = eventSource;
    }

    @Override
    public SignalAssertions expectEventsOnProcessInstance(ProcessInstance processInstance,
                                                          OperationScopeMatcher... matchers) {
        List<RuntimeEvent<?, ?>> events = eventSource.getEvents();
        for (OperationScopeMatcher matcher : matchers) {
            matcher.match(processInstanceScope(processInstance.getId()),
                          events);
        }
        return this;
    }
}

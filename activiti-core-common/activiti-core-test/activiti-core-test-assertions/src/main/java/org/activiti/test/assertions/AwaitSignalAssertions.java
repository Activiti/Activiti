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

import org.activiti.api.process.model.ProcessInstance;
import org.activiti.test.matchers.OperationScopeMatcher;

import static org.awaitility.Awaitility.await;

public class AwaitSignalAssertions implements SignalAssertions {

    private SignalAssertions signalAssertions;

    public AwaitSignalAssertions(SignalAssertions signalAssertions) {
        this.signalAssertions = signalAssertions;
    }

    @Override
    public SignalAssertions expectEventsOnProcessInstance(ProcessInstance processInstance,
                                                          OperationScopeMatcher... matchers) {
        await().untilAsserted(() -> signalAssertions.expectEventsOnProcessInstance(processInstance,
                                                                                   matchers));
        return this;
    }
}

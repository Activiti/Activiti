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
package org.activiti.test.operations;

import org.activiti.api.process.model.payloads.SignalPayload;
import org.activiti.api.process.model.payloads.StartProcessPayload;
import org.activiti.test.assertions.AwaitProcessInstanceAssertions;
import org.activiti.test.assertions.AwaitSignalAssertions;
import org.activiti.test.assertions.ProcessInstanceAssertions;
import org.activiti.test.assertions.SignalAssertions;

public class AwaitableProcessOperations implements ProcessOperations {

    private ProcessOperations processOperations;
    private boolean awaitEnabled;

    public AwaitableProcessOperations(ProcessOperations processOperations,
                                      boolean awaitEnabled) {
        this.processOperations = processOperations;
        this.awaitEnabled = awaitEnabled;
    }

    @Override
    public ProcessInstanceAssertions start(StartProcessPayload startProcessPayload)  {

        ProcessInstanceAssertions processInstanceAssertions = processOperations.start(startProcessPayload);
        if (awaitEnabled){
            processInstanceAssertions = new AwaitProcessInstanceAssertions(processInstanceAssertions);
        }
        return processInstanceAssertions;
    }

    @Override
    public SignalAssertions signal(SignalPayload signalPayload) {
        SignalAssertions signalAssertions = processOperations.signal(signalPayload);
        if (awaitEnabled) {
            signalAssertions = new AwaitSignalAssertions(signalAssertions);
        }
        return signalAssertions;
    }


}

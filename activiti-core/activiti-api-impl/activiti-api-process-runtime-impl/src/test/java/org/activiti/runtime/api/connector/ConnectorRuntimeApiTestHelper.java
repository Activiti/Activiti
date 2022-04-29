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
package org.activiti.runtime.api.connector;

import org.activiti.bpmn.model.ServiceTask;
import org.activiti.engine.delegate.DelegateExecution;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class ConnectorRuntimeApiTestHelper {

    public static DelegateExecution buildExecution(String connector) {
        DelegateExecution execution = mock(DelegateExecution.class);
        ServiceTask serviceTask = mock(ServiceTask.class);
        given(serviceTask.getImplementation()).willReturn(connector);
        given(execution.getCurrentFlowElement()).willReturn(serviceTask);
        return execution;
    }

}

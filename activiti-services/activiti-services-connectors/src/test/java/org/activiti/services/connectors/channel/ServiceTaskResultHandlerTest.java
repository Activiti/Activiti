/*
 * Copyright 2017 Alfresco, Inc. and/or its affiliates.
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

package org.activiti.services.connectors.channel;

import java.util.Collections;
import java.util.Map;

import org.activiti.engine.RuntimeService;
import org.activiti.services.connectors.model.ServiceTaskResult;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class ServiceTaskResultHandlerTest {

    @InjectMocks
    private ServiceTaskResultHandler handler;

    @Mock
    private RuntimeService runtimeService;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void receiveShouldTriggerTheExecution() throws Exception {
        //given
        String executionId = "execId";
        Map<String, Object> variables = Collections.singletonMap("var1",
                                                                 "v");
        ServiceTaskResult serviceTaskResult = new ServiceTaskResult("resultId",
                                                                    executionId,
                                                                    variables);

        //when
        handler.receive(serviceTaskResult);

        //then
        verify(runtimeService).trigger(executionId, variables);
    }
}
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
import org.activiti.engine.impl.persistence.entity.integration.IntegrationContextEntityImpl;
import org.activiti.engine.integration.IntegrationContextService;
import org.activiti.services.connectors.model.IntegrationResult;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class ServiceTaskIntegrationResultHandlerTest {


    @InjectMocks
    private ServiceTaskIntegrationResultHandler handler;

    @Mock
    private RuntimeService runtimeService;

    @Mock
    private IntegrationContextService integrationContextService;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void receiveShouldTriggerTheExecution() throws Exception {
        //given
        String executionId = "execId";
        String correlationId = "corId";

        IntegrationContextEntityImpl integrationContext = new IntegrationContextEntityImpl();
        integrationContext.setCorrelationId(correlationId);
        integrationContext.setExecutionId(executionId);

        given(integrationContextService.findIntegrationContextByCorrelationId(correlationId)).willReturn(integrationContext);
        Map<String, Object> variables = Collections.singletonMap("var1",
                                                                 "v");
        IntegrationResult integrationResult = new IntegrationResult("resultId",
                                                                    correlationId,
                                                                    variables);

        //when
        handler.receive(integrationResult);

        //then
        verify(runtimeService).trigger(executionId,
                                       variables);
    }
}
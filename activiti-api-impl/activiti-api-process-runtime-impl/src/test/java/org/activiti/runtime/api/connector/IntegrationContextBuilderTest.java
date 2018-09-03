/*
 * Copyright 2018 Alfresco, Inc. and/or its affiliates.
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

package org.activiti.runtime.api.connector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.api.process.model.IntegrationContext;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.persistence.entity.integration.IntegrationContextEntity;
import org.activiti.model.connector.Action;
import org.activiti.model.connector.Connector;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class IntegrationContextBuilderTest{

    @InjectMocks
    private IntegrationContextBuilder integrationContextBuilder;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void inBoundVariablesSetFromDelegateExecutionBecauseConnectorsAreNotSet(){
        DelegateExecution delegateExecution = ConnectorRuntimeApiTestHelper.buildExecution("connector.action1");
        Map<String, Object> vars = new HashMap<>();
        vars.put("var1", "value");
        given(delegateExecution.getVariables()).willReturn(vars);

        IntegrationContext integrationContext  = integrationContextBuilder.from(buildIntegrationContextEntity(), delegateExecution, null);
        assertThat(integrationContext.getInBoundVariables()).isEqualTo(vars);
    }



    private IntegrationContextEntity buildIntegrationContextEntity(){
        IntegrationContextEntity integrationContextEntity = mock(IntegrationContextEntity.class);
        given(integrationContextEntity.getId()).willReturn("id");
        return integrationContextEntity;
    }









}

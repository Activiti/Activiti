/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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
package org.activiti.engine.impl.cmd.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.MockitoAnnotations.initMocks;

import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.integration.IntegrationContextEntity;
import org.activiti.engine.impl.persistence.entity.integration.IntegrationContextManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class RetrieveIntegrationContextCmdTest {

    @Mock
    private CommandContext commandContext;

    @Mock
    private ProcessEngineConfigurationImpl processEngineConfiguration;

    @Mock
    private IntegrationContextManager integrationContextManager;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        given(commandContext.getProcessEngineConfiguration())
            .willReturn(processEngineConfiguration);
        given(processEngineConfiguration.getIntegrationContextManager())
            .willReturn(integrationContextManager);
    }

    @Test
    public void executeShouldReturnResultOfIntegrationContextManager() {
        //given
        String id = "id";
        RetrieveIntegrationContextsCmd command = new RetrieveIntegrationContextsCmd(
            id
        );

        IntegrationContextEntity contextEntity = mock(
            IntegrationContextEntity.class
        );
        given(integrationContextManager.findById(id)).willReturn(contextEntity);

        //when
        IntegrationContextEntity executeResult = command.execute(
            commandContext
        );

        //then
        assertThat(executeResult).isEqualTo(contextEntity);
    }
}

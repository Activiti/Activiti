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

package org.activiti.engine.integration;

import org.activiti.engine.impl.cmd.integration.RetrieveIntegrationContextCmd;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.entity.integration.IntegrationContextEntity;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.MockitoAnnotations.initMocks;

public class IntegrationContextServiceImplTest {

    @InjectMocks
    private IntegrationContextServiceImpl integrationContextService;

    @Mock
    private CommandExecutor commandExecutor;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void findIntegrationContextByExecutionIdShouldExecuteRetrieveIntegrationContextCmd() throws Exception {
        //given
        IntegrationContextEntity entity = mock(IntegrationContextEntity.class);
        given(commandExecutor.execute(any(RetrieveIntegrationContextCmd.class))).willReturn(entity);

        //when
        IntegrationContextEntity commandResult = integrationContextService.findIntegrationContextByExecutionId("execId");

        //then
        assertThat(commandResult).isEqualTo(entity);
    }

}
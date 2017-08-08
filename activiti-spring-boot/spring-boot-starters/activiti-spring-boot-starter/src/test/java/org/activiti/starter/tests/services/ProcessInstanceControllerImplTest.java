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

package org.activiti.starter.tests.services;

import org.activiti.services.core.ProcessEngineWrapper;
import org.activiti.services.core.model.commands.ActivateProcessInstanceCmd;
import org.activiti.services.core.model.commands.SuspendProcessInstanceCmd;
import org.activiti.services.rest.controllers.ProcessInstanceControllerImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class ProcessInstanceControllerImplTest {

    @InjectMocks
    private ProcessInstanceControllerImpl controller;

    @Mock
    private ProcessEngineWrapper processEngineWrapper;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void suspendShouldCallSuspendOnRuntimeService() throws Exception {
        //given
        String processInstanceId = "7";

        //when
        controller.suspend(processInstanceId);

        //then
        verify(processEngineWrapper).suspend(any(SuspendProcessInstanceCmd.class));
    }

    @Test
    public void activateShouldCallActivateOnRuntimeService() throws Exception {
        //given
        String processInstanceId = "7";

        //when
        controller.activate(processInstanceId);

        //then
        verify(processEngineWrapper).activate(any(ActivateProcessInstanceCmd.class));
    }
}
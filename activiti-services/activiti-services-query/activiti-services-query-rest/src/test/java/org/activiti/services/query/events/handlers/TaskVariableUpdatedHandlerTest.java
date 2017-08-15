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

package org.activiti.services.query.events.handlers;

import com.querydsl.core.types.Predicate;
import org.activiti.services.query.model.Variable;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class TaskVariableUpdatedHandlerTest {

    @InjectMocks
    private TaskVariableUpdatedHandler handler;

    @Mock
    private VariableUpdater variableUpdater;


    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void handleShouldUpdateVariableValue() throws Exception {
        //given
        String taskId = "10";
        Variable updatedVariable = new Variable();
        updatedVariable.setName("var");
        updatedVariable.setType("string");
        updatedVariable.setValue("content");
        updatedVariable.setTaskId(taskId);

        //when
        handler.handle(updatedVariable);

        //then
        verify(variableUpdater).update(eq(updatedVariable), any(Predicate.class), anyString());
    }

}
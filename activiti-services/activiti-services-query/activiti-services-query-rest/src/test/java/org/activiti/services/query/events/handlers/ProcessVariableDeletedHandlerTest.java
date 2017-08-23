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
import org.activiti.services.query.app.repository.EntityFinder;
import org.activiti.services.query.app.repository.VariableRepository;
import org.activiti.services.query.events.VariableDeletedEvent;
import org.activiti.services.query.model.Variable;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class ProcessVariableDeletedHandlerTest {

    @InjectMocks
    private ProcessVariableDeletedHandler handler;

    @Mock
    private VariableRepository variableRepository;

    @Mock
    private EntityFinder entityFinder;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void handleRemoveVariableFromProcessAndDeleteIt() throws Exception {
        //given
        VariableDeletedEvent event = new VariableDeletedEvent();
        event.setProcessInstanceId("10");
        event.setVariableName("var");

        Variable variable = new Variable();
        given(entityFinder.findOne(eq(variableRepository), any(Predicate.class), anyString())).willReturn(variable);

        //when
        handler.handle(event);

        //then
        verify(variableRepository).delete(variable);
    }

}
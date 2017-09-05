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

import java.util.Date;

import com.querydsl.core.types.Predicate;
import org.activiti.services.query.model.Variable;
import org.activiti.services.query.app.repository.EntityFinder;
import org.activiti.services.query.app.repository.VariableRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.activiti.test.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class VariableUpdaterTest {

    @InjectMocks
    private VariableUpdater updater;

    @Mock
    private EntityFinder entityFinder;

    @Mock
    private VariableRepository variableRepository;


    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void updateShouldUpdateVariableRetrievedByPredicate() throws Exception {
        //given
        Variable currentVariable = new Variable();

        Predicate predicate = mock(Predicate.class);
        given(entityFinder.findOne(variableRepository, predicate, "error")).willReturn(currentVariable);

        Date now = new Date();
        Variable updatedVariable = new Variable();
        updatedVariable.setType("string");
        updatedVariable.setValue("content");
        updatedVariable.setLastUpdatedTime(now);

        //when
        updater.update(updatedVariable,
                       predicate, "error");

        //then
        assertThat(currentVariable)
                .hasType("string")
                .hasValue("content")
                .hasLastUpdatedTime(now);
        verify(variableRepository).save(currentVariable);
    }

}
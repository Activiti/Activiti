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
package org.activiti.engine.impl.bpmn.helper;

import java.util.Map;

import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.junit.Test;

import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class SubProcessVariableSnapshotterTest {

    private SubProcessVariableSnapshotter snapshotter = new SubProcessVariableSnapshotter();

    @Test
    public void setVariablesSnapshots_should_set_all_source_local_variables_in_the_snapshot_holder() throws Exception {
        //given
        Map<String, Object> variables = singletonMap("subCount", 1L);
        ExecutionEntity subProcessExecution = buildExecutionEntity(variables);

        ExecutionEntity snapshotHolderExecution = mock(ExecutionEntity.class);

        //when
        snapshotter.setVariablesSnapshots(subProcessExecution, snapshotHolderExecution);

        //then
        verify(snapshotHolderExecution).setVariablesLocal(variables);
    }

    private ExecutionEntity buildExecutionEntity(Map<String, Object> variables) {
        ExecutionEntity subProcessExecution = mock(ExecutionEntity.class);
        when(subProcessExecution.getVariablesLocal()).thenReturn(variables);
        return subProcessExecution;
    }

    @Test
    public void setVariablesSnapshots_should_set_parent_variables_in_the_snapshot_holder_when_parent_is_multi_instance() throws Exception {
        //given
        Map<String, Object> parentVariables = singletonMap("parentCount", 1L);
        ExecutionEntity parentExecution = buildExecutionEntity(parentVariables);
        when(parentExecution.isMultiInstanceRoot()).thenReturn(true);

        Map<String, Object> localVariables = singletonMap("subCount", 1L);
        ExecutionEntity subProcessExecution = buildExecutionEntity(parentExecution, localVariables);

        ExecutionEntity snapshotHolderExecution = mock(ExecutionEntity.class);

        //when
        snapshotter.setVariablesSnapshots(subProcessExecution, snapshotHolderExecution);

        //then
        verify(snapshotHolderExecution).setVariablesLocal(localVariables);
        verify(snapshotHolderExecution).setVariablesLocal(parentVariables);
    }

    private ExecutionEntity buildExecutionEntity(ExecutionEntity parentExecution, Map<String, Object> localVariables) {
        ExecutionEntity subProcessExecution = buildExecutionEntity(localVariables);
        when(subProcessExecution.getParent()).thenReturn(parentExecution);
        return subProcessExecution;
    }

    @Test
    public void setVariablesSnapshots_should_not_set_parent_variables_in_the_snapshot_holder_when_parent_is_not_multi_instance() throws Exception {
        //given
        Map<String, Object> parentVariables = singletonMap("parentCount", 1L);
        ExecutionEntity parentExecution = buildExecutionEntity(parentVariables);
        when(parentExecution.isMultiInstanceRoot()).thenReturn(false);

        Map<String, Object> localVariables = singletonMap("subCount", 1L);
        ExecutionEntity subProcessExecution = buildExecutionEntity(parentExecution, localVariables);

        ExecutionEntity snapshotHolderExecution = mock(ExecutionEntity.class);

        //when
        snapshotter.setVariablesSnapshots(subProcessExecution, snapshotHolderExecution);

        //then
        verify(snapshotHolderExecution).setVariablesLocal(localVariables);
        verify(snapshotHolderExecution, never()).setVariablesLocal(parentVariables);
    }


}

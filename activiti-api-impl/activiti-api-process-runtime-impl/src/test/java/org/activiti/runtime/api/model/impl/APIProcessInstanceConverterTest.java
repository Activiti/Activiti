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

package org.activiti.runtime.api.model.impl;

import java.util.Date;

import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.ProcessInstance.ProcessInstanceStatus;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.activiti.engine.impl.persistence.entity.SuspensionState;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class APIProcessInstanceConverterTest {

    private static final String BUSINESS_KEY = "businessKey";
    private static final String START_USER_ID = "startUserId";
    private static final String DESCRIPTION = "description";
    private static final String NAME = "name";
    private static final int PROCESS_DEFINITION_VERSION = 1;
    private static final String PARENT_PROCESS_INSTANCE_ID = "parentProcessInstanceId";
    private static final String PROCESS_DEFINITION_KEY = "processDefinitionKey";
    private static final String PROCESS_DEFINITION_ID = "processDefinitionId";
    private static final String PROCESS_INSTANCE_ID = "processInstanceId";
    
    private APIProcessInstanceConverter subject = new APIProcessInstanceConverter();
    private Date startTime = new Date();
    
    @Test
    public void shouldConvertFromInternalProcessInstanceWithRunningStatus() {
        //given
        ExecutionEntity internalProcessInstance = anInternalProcessInstance(startTime);

        internalProcessInstance.setActive(true);
        
        //when
        ProcessInstance result = subject.from(internalProcessInstance);

        //then
        assertValidProcessInstanceResult(result);
        assertThat(result.getStatus()).isEqualTo(ProcessInstanceStatus.RUNNING);
    }

    @Test
    public void shouldConvertFromInternalProcessInstanceWithSuspendedStatus() {
        //given
        ExecutionEntity internalProcessInstance = anInternalProcessInstance(startTime);

        internalProcessInstance.setSuspensionState(SuspensionState.SUSPENDED.getStateCode());
        
        //when
        ProcessInstance result = subject.from(internalProcessInstance);

        //then
        assertValidProcessInstanceResult(result);
        assertThat(result.getStatus()).isEqualTo(ProcessInstanceStatus.SUSPENDED);
    }

    @Test
    public void shouldConvertFromInternalProcessInstanceWithCompletedStatus() {
        //given
        ExecutionEntity internalProcessInstance = anInternalProcessInstance(startTime);

        internalProcessInstance.setEnded(true);
        
        //when
        ProcessInstance result = subject.from(internalProcessInstance);

        //then
        assertValidProcessInstanceResult(result);
        assertThat(result.getStatus()).isEqualTo(ProcessInstanceStatus.COMPLETED);
    }

    private void assertValidProcessInstanceResult(ProcessInstance result) {
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(PROCESS_INSTANCE_ID);
        assertThat(result.getBusinessKey()).isEqualTo(BUSINESS_KEY);
        assertThat(result.getProcessDefinitionId()).isEqualTo(PROCESS_DEFINITION_ID);
        assertThat(result.getProcessDefinitionKey()).isEqualTo(PROCESS_DEFINITION_KEY);
        assertThat(result.getProcessDefinitionVersion()).isEqualTo(PROCESS_DEFINITION_VERSION);
        assertThat(result.getParentId()).isEqualTo(PARENT_PROCESS_INSTANCE_ID);
        assertThat(result.getName()).isEqualTo(NAME);
        assertThat(result.getDescription()).isEqualTo(DESCRIPTION);
        assertThat(result.getParentId()).isEqualTo(PARENT_PROCESS_INSTANCE_ID);
        assertThat(result.getInitiator()).isEqualTo(START_USER_ID);
        assertThat(result.getStartDate()).isEqualTo(startTime);
    }
    
    private ExecutionEntity anInternalProcessInstance(Date startTime) {
        ExecutionEntity internalProcessInstance = new ExecutionEntityImpl();        
        
        internalProcessInstance.setId(PROCESS_INSTANCE_ID);
        internalProcessInstance.setParentProcessInstanceId(PARENT_PROCESS_INSTANCE_ID);
        internalProcessInstance.setProcessDefinitionId(PROCESS_DEFINITION_ID);
        internalProcessInstance.setProcessDefinitionKey(PROCESS_DEFINITION_KEY);
        internalProcessInstance.setProcessDefinitionVersion(PROCESS_DEFINITION_VERSION);
        internalProcessInstance.setBusinessKey(BUSINESS_KEY);
        internalProcessInstance.setName(NAME);
        internalProcessInstance.setDescription(DESCRIPTION);
        internalProcessInstance.setStartUserId(START_USER_ID);
        internalProcessInstance.setStartTime(startTime);
        internalProcessInstance.setActive(true);
        
        return internalProcessInstance;
    }
    
}

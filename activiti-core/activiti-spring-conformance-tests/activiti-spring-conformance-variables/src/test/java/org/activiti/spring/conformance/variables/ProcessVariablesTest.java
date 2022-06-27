/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
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
package org.activiti.spring.conformance.variables;

import org.activiti.api.model.shared.event.RuntimeEvent;
import org.activiti.api.model.shared.event.VariableEvent;
import org.activiti.api.model.shared.model.VariableInstance;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.GetVariablesPayloadBuilder;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.model.builders.SetVariablesPayloadBuilder;
import org.activiti.api.process.model.events.BPMNActivityEvent;
import org.activiti.api.process.model.events.BPMNSequenceFlowTakenEvent;
import org.activiti.api.process.model.events.ProcessRuntimeEvent;
import org.activiti.api.process.runtime.ProcessAdminRuntime;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.events.TaskRuntimeEvent;
import org.activiti.spring.conformance.util.RuntimeTestConfiguration;
import org.activiti.spring.conformance.util.security.SecurityUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ProcessVariablesTest {

    private final String processKey = "usertaskas-b5300a4b-8950-4486-ba20-a8d775a3d75d";

    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private ProcessAdminRuntime processAdminRuntime;

    private String processInstanceId;

    private List<VariableInstance> variableInstanceList;

    @BeforeEach
    public void cleanUp() {
        clearEvents();
    }

    @Test
    public void shouldGetSameNamesAndValues() {

        securityUtil.logInAs("user1");

        startProcess();

        setVariables();

        assertThat(RuntimeTestConfiguration.collectedEvents)
                .extracting("eventType","entity.name","entity.value")
                .containsExactly(
                        tuple(  VariableEvent.VariableEvents.VARIABLE_CREATED,
                                variableInstanceList.get(0).getName(),
                                variableInstanceList.get(0).getValue()),
                        tuple(  VariableEvent.VariableEvents.VARIABLE_CREATED,
                                variableInstanceList.get(1).getName(),
                                variableInstanceList.get(1).getValue())
                );
    }

    @Test
    public void shouldGetProcessIdAndNotTaskId() {

        securityUtil.logInAs("user1");

        startProcess();

        setVariables();

        VariableInstance variableOneRuntime = variableInstanceList.get(0);
        assertThat(variableOneRuntime.getProcessInstanceId()).isEqualTo(processInstanceId);
        assertThat(variableOneRuntime.getTaskId()).isNull();

        assertThat(RuntimeTestConfiguration.collectedEvents)
                .extracting("eventType","entity.name","entity.value")
                .containsExactly(
                        tuple(  VariableEvent.VariableEvents.VARIABLE_CREATED,
                                variableInstanceList.get(0).getName(),
                                variableInstanceList.get(0).getValue()),
                        tuple(  VariableEvent.VariableEvents.VARIABLE_CREATED,
                                variableInstanceList.get(1).getName(),
                                variableInstanceList.get(1).getValue())
                );
    }

    @Test
    public void shouldNotBeTaskVariable() {
        securityUtil.logInAs("user1");

        startProcess();

        setVariables();

        VariableInstance variableOneRuntime = variableInstanceList.get(0);
        assertThat(variableOneRuntime.isTaskVariable()).isFalse();

        assertThat(RuntimeTestConfiguration.collectedEvents)
                .extracting("eventType","entity.name","entity.value")
                .containsExactly(
                        tuple(  VariableEvent.VariableEvents.VARIABLE_CREATED,
                                variableInstanceList.get(0).getName(),
                                variableInstanceList.get(0).getValue()),
                        tuple(  VariableEvent.VariableEvents.VARIABLE_CREATED,
                                variableInstanceList.get(1).getName(),
                                variableInstanceList.get(1).getValue())
                );
    }

    @Test
    public void shouldGetRightVariableType(){
        securityUtil.logInAs("user1");

        startProcess();

        setVariables();

        VariableInstance variableOneRuntime = variableInstanceList.get(0);
        VariableInstance variableTwoRuntime = variableInstanceList.get(1);
        assertThat(variableOneRuntime.getType()).isEqualTo("string");
        assertThat(variableTwoRuntime.getType()).isEqualTo("integer");

        assertThat(RuntimeTestConfiguration.collectedEvents)
                .extracting("eventType","entity.name","entity.value")
                .containsExactly(
                        tuple(  VariableEvent.VariableEvents.VARIABLE_CREATED,
                                variableInstanceList.get(0).getName(),
                                variableInstanceList.get(0).getValue()),
                        tuple(  VariableEvent.VariableEvents.VARIABLE_CREATED,
                                variableInstanceList.get(1).getName(),
                                variableInstanceList.get(1).getValue())
                );
    }

    @AfterEach
    public void cleanup() {
        securityUtil.logInAs("admin");
        Page<ProcessInstance> processInstancePage = processAdminRuntime.processInstances(Pageable.of(0, 50));
        for (ProcessInstance pi : processInstancePage.getContent()) {
            processAdminRuntime.delete(ProcessPayloadBuilder.delete(pi.getId()));
        }

        clearEvents();
    }

    private void startProcess(){
        processInstanceId = processRuntime.start(ProcessPayloadBuilder
                .start()
                .withProcessDefinitionKey(processKey)
                .withBusinessKey("my-business-key")
                .withName("my-process-instance-name")
                .build()).getId();

        assertThat(RuntimeTestConfiguration.collectedEvents)
                .extracting(RuntimeEvent::getEventType)
                .containsExactly(
                        ProcessRuntimeEvent.ProcessEvents.PROCESS_CREATED,
                        ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                        BPMNSequenceFlowTakenEvent.SequenceFlowEvents.SEQUENCE_FLOW_TAKEN,
                        BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                        TaskRuntimeEvent.TaskEvents.TASK_CREATED,
                        TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED
                );

        clearEvents();
    }

    private void setVariables(){

        Map<String, Object> variablesMap = new HashMap<>();
        variablesMap.put("one", "variableOne");
        variablesMap.put("two", 2);

        processRuntime.setVariables(new SetVariablesPayloadBuilder().withVariables(variablesMap).withProcessInstanceId(processInstanceId).build());

        variableInstanceList = processRuntime.variables(new GetVariablesPayloadBuilder().withProcessInstanceId(processInstanceId).build());
    }

    public void clearEvents() {
        RuntimeTestConfiguration.collectedEvents.clear();
    }

}

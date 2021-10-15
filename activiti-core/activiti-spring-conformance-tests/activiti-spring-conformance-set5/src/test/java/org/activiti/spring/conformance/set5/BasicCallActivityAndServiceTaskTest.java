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
package org.activiti.spring.conformance.set5;

import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.api.model.shared.event.RuntimeEvent;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.model.events.BPMNActivityEvent;
import org.activiti.api.process.model.events.BPMNSequenceFlowTakenEvent;
import org.activiti.api.process.model.events.ProcessRuntimeEvent;
import org.activiti.api.process.runtime.ProcessAdminRuntime;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.spring.conformance.util.RuntimeTestConfiguration;
import org.activiti.spring.conformance.util.security.SecurityUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class BasicCallActivityAndServiceTaskTest {

    private final String processKey =
        "basiccalla-bdf1f7af-7cc7-45c7-a206-8b5c46a8198f";

    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private ProcessAdminRuntime processAdminRuntime;

    @BeforeEach
    public void cleanUp() {
        clearEvents();
    }

    @Test
    public void shouldCheckCallActivityWaitingOnUserTask() {
        securityUtil.logInAs("user1");

        ProcessInstance processInstance = processRuntime.start(
            ProcessPayloadBuilder
                .start()
                .withProcessDefinitionKey(processKey)
                .withBusinessKey("my-business-key")
                .withName("my-process-instance-name")
                .build()
        );

        //then
        assertThat(processInstance).isNotNull();
        assertThat(processInstance.getStatus())
            .isEqualTo(ProcessInstance.ProcessInstanceStatus.COMPLETED);
        assertThat(processInstance.getBusinessKey())
            .isEqualTo("my-business-key");
        assertThat(processInstance.getName())
            .isEqualTo("my-process-instance-name");

        assertThat(RuntimeTestConfiguration.collectedEvents)
            .extracting(RuntimeEvent::getEventType)
            .containsExactly(
                ProcessRuntimeEvent.ProcessEvents.PROCESS_CREATED,
                ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED,
                BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                BPMNSequenceFlowTakenEvent.SequenceFlowEvents.SEQUENCE_FLOW_TAKEN,
                BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                ProcessRuntimeEvent.ProcessEvents.PROCESS_CREATED,
                ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED,
                BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                BPMNSequenceFlowTakenEvent.SequenceFlowEvents.SEQUENCE_FLOW_TAKEN,
                BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                BPMNSequenceFlowTakenEvent.SequenceFlowEvents.SEQUENCE_FLOW_TAKEN,
                BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED,
                BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                BPMNSequenceFlowTakenEvent.SequenceFlowEvents.SEQUENCE_FLOW_TAKEN,
                BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED,
                BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED,
                ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED
            );

        clearEvents();
    }

    @AfterEach
    public void cleanup() {
        securityUtil.logInAs("admin");
        Page<ProcessInstance> processInstancePage = processAdminRuntime.processInstances(
            Pageable.of(0, 50)
        );
        for (ProcessInstance pi : processInstancePage.getContent()) {
            // We want to delete root processes instances because sub processes will be deleted automatically when the root ones are deleted
            if (pi.getParentId() == null) {
                processAdminRuntime.delete(
                    ProcessPayloadBuilder.delete(pi.getId())
                );
            }
        }

        clearEvents();
    }

    public void clearEvents() {
        RuntimeTestConfiguration.collectedEvents.clear();
    }
}

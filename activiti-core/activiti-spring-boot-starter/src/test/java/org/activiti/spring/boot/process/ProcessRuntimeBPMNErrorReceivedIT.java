/*
 * Copyright 2019 Alfresco, Inc. and/or its affiliates.
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

package org.activiti.spring.boot.process;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.model.events.BPMNErrorReceivedEvent;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.GetTasksPayloadBuilder;
import org.activiti.api.task.model.payloads.GetTasksPayload;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.spring.boot.process.listener.DummyBPMNErrorReceivedListener;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.activiti.spring.boot.test.util.ProcessCleanUpUtil;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Import(DummyBPMNErrorReceivedListener.class)
public class ProcessRuntimeBPMNErrorReceivedIT {

    private static final String ERROR_BOUNDARY_EVENT_SUBPROCESS = "errorBoundaryEventSubProcess";
    private static final String ERROR_START_EVENT_SUBPROCESS = "errorStartEventSubProcess";
    private static final String ERROR_BOUNDARY_EVENT_CALLACTIVITY = "catchErrorOnCallActivity";

    @Autowired
    private ProcessRuntime processRuntime;

    @Autowired
    private TaskRuntime taskRuntime;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private ProcessCleanUpUtil processCleanUpUtil;

    @Autowired
    private DummyBPMNErrorReceivedListener listener;

    @BeforeEach
    public void init() {
        listener.clear();
    }

    @AfterEach
    public void cleanUp(){
        processCleanUpUtil.cleanUpWithAdmin();
        listener.clear();
    }

    @Test
    public void should_CatchSubProcessBoundaryErrorEvent_When_ErrorEndEvenThrown(){

        securityUtil.logInAs("user");

        ProcessInstance processInstance = processRuntime.start(
                ProcessPayloadBuilder
                        .start()
                        .withProcessDefinitionKey(ERROR_BOUNDARY_EVENT_SUBPROCESS)
                        .build());

        assertThat(processInstance).isNotNull();

        checkProcessAndTask(processInstance.getId(), "Task");

        assertThat(listener.getErrorReceivedEvents())
        .isNotEmpty()
        .extracting(BPMNErrorReceivedEvent::getEventType,
                    BPMNErrorReceivedEvent::getProcessDefinitionId,
                    BPMNErrorReceivedEvent::getProcessInstanceId,
                    event -> event.getEntity().getProcessDefinitionId(),
                    event -> event.getEntity().getProcessInstanceId(),
                    event -> event.getEntity().getElementId(),
                    event -> event.getEntity().getActivityName(),
                    event -> event.getEntity().getActivityType(),
                    event -> event.getEntity().getErrorId(),
                    event -> event.getEntity().getErrorCode()
        )
        .contains(Tuple.tuple(BPMNErrorReceivedEvent.ErrorEvents.ERROR_RECEIVED,
                              processInstance.getProcessDefinitionId(),
                              processInstance.getId(),
                              processInstance.getProcessDefinitionId(),
                              processInstance.getId(),
                              "catchError",
                              null,
                              null,
                              "errorId",
                              "123"
        ));

    }

    @Test
    public void should_StartSubProcess_When_ErrorEndEvenThrown(){

        securityUtil.logInAs("user");

        ProcessInstance processInstance = processRuntime.start(
                ProcessPayloadBuilder
                        .start()
                        .withProcessDefinitionKey(ERROR_START_EVENT_SUBPROCESS)
                        .build());

        assertThat(processInstance).isNotNull();

        checkProcessAndTask(processInstance.getId(), "Task");

        assertThat(listener.getErrorReceivedEvents())
        .isNotEmpty()
        .extracting(BPMNErrorReceivedEvent::getEventType,
                    BPMNErrorReceivedEvent::getProcessDefinitionId,
                    BPMNErrorReceivedEvent::getProcessInstanceId,
                    event -> event.getEntity().getProcessDefinitionId(),
                    event -> event.getEntity().getProcessInstanceId(),
                    event -> event.getEntity().getElementId(),
                    event -> event.getEntity().getActivityName(),
                    event -> event.getEntity().getActivityType(),
                    event -> event.getEntity().getErrorId(),
                    event -> event.getEntity().getErrorCode()
        )
        .contains(Tuple.tuple(BPMNErrorReceivedEvent.ErrorEvents.ERROR_RECEIVED,
                              processInstance.getProcessDefinitionId(),
                              processInstance.getId(),
                              processInstance.getProcessDefinitionId(),
                              processInstance.getId(),
                              "subStart1",
                              null,
                              null,
                              "errorId",
                              "123"
        ));

    }

    @Test
    public void should_CatchCallActivityBoundaryErrorEvent_When_ErrorEndEvenThrown(){

        securityUtil.logInAs("user");

        ProcessInstance processInstance = processRuntime.start(
                ProcessPayloadBuilder
                        .start()
                        .withProcessDefinitionKey(ERROR_BOUNDARY_EVENT_CALLACTIVITY)
                        .build());

        assertThat(processInstance).isNotNull();

        checkProcessAndTask(processInstance.getId(), "Task");

        assertThat(listener.getErrorReceivedEvents())
        .isNotEmpty()
        .extracting(BPMNErrorReceivedEvent::getEventType,
                    BPMNErrorReceivedEvent::getProcessDefinitionId,
                    BPMNErrorReceivedEvent::getProcessInstanceId,
                    event -> event.getEntity().getProcessDefinitionId(),
                    event -> event.getEntity().getProcessInstanceId(),
                    event -> event.getEntity().getElementId(),
                    event -> event.getEntity().getActivityName(),
                    event -> event.getEntity().getActivityType(),
                    event -> event.getEntity().getErrorId(),
                    event -> event.getEntity().getErrorCode()
        )
        .contains(Tuple.tuple(BPMNErrorReceivedEvent.ErrorEvents.ERROR_RECEIVED,
                              processInstance.getProcessDefinitionId(),
                              processInstance.getId(),
                              processInstance.getProcessDefinitionId(),
                              processInstance.getId(),
                              "catchError",
                              null,
                              null,
                              "errorId",
                              "123"
        ));
    }

    private void checkProcessAndTask(String processInstanceId, String taskName) {

        ProcessInstance processInstance = processRuntime.processInstance(processInstanceId);
        assertThat(processInstance).isNotNull();

        checkTask(processInstanceId, taskName);
    }

    private void checkTask(String processInstanceId, String taskName) {

        GetTasksPayload getTasksPayload = new GetTasksPayloadBuilder()
                                                .withProcessInstanceId(processInstanceId)
                                                .build();

        Page<Task> tasks = taskRuntime.tasks(Pageable.of(0,
                                                         50),
                                             getTasksPayload);

        assertThat(tasks.getContent()).hasSize(1);
        assertThat(tasks.getContent().get(0).getName()).isEqualTo(taskName);
    }

}

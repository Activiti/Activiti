package org.activiti.spring.boot.process;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.api.process.runtime.events.ProcessCancelledEvent;
import org.activiti.api.task.model.Task;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.activiti.spring.boot.tasks.TaskBaseRuntime;
import org.activiti.spring.boot.tasks.TaskRuntimeEventListeners;
import org.activiti.spring.boot.test.util.TaskCleanUpUtil;
import org.activiti.test.LocalEventSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration
public class ProcessRuntimeTerminatedEndEventIT {

    private static final String PROCESS_TERMINATE_EVENT = "Process_KzwZAEl-";
    public static final String LOGGED_USER = "user";

    @Autowired
    private TaskBaseRuntime taskBaseRuntime;

    @Autowired
    private ProcessRuntime processRuntime;
    @Autowired
    private TaskCleanUpUtil taskCleanUpUtil;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private LocalEventSource localEventSource;

    @Autowired
    private TaskRuntimeEventListeners taskRuntimeEventListeners;

    @BeforeEach
    public void setUp() {
        localEventSource.clearEvents();
        securityUtil.logInAs(LOGGED_USER);
    }

    @AfterEach
    public void tearDown() {
        taskCleanUpUtil.cleanUpWithAdmin();
        localEventSource.clearEvents();
        taskRuntimeEventListeners.clearEvents();
    }

    @Test
    public void should_CancelledProcessesByTerminateEndEventsHaveCancellationReasonSet() {
        //given
        ProcessInstance processInstance = processRuntime.start(ProcessPayloadBuilder.start()
            .withProcessDefinitionKey(PROCESS_TERMINATE_EVENT)
            .withName("to be terminated")
            .withBusinessKey("My business key")
            .build()
        );

        List<Task> tasks = taskBaseRuntime.getTasks(processInstance);
        assertThat(tasks).hasSize(2);
        Task task2 = tasks.get(1);

        //when
        taskBaseRuntime.completeTask(task2.getId());

        //then
        List<Task> tasksAfterCompletion = taskBaseRuntime.getTasks(processInstance);
        assertThat(tasksAfterCompletion).hasSize(0);

        List<ProcessCancelledEvent> processCancelledEvents =
            localEventSource.getEvents(ProcessCancelledEvent.class);

        assertThat(processCancelledEvents).hasSize(1);
        ProcessCancelledEvent processCancelledEvent = processCancelledEvents.get(0);
        assertThat(processCancelledEvent.getCause()).contains("Terminated by end event");
        assertThat(processCancelledEvent.getEntity().getId()).isEqualTo(processInstance.getId());
        assertThat(processCancelledEvent.getEntity().getProcessDefinitionId())
            .isEqualTo(processInstance.getProcessDefinitionId());
        assertThat(processCancelledEvent.getEntity().getName())
            .isEqualTo(processInstance.getName());
        assertThat(processCancelledEvent.getEntity().getBusinessKey())
            .isEqualTo(processInstance.getBusinessKey());
        assertThat(processCancelledEvent.getEntity().getStartDate())
            .isEqualTo(processInstance.getStartDate());
        assertThat(processCancelledEvent.getEntity().getInitiator()).isEqualTo(LOGGED_USER);
    }

}

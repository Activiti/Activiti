package org.activiti.spring.boot.process;

import java.util.List;

import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.runtime.events.ProcessCancelledEvent;
import org.activiti.api.task.model.Task;
import org.activiti.spring.boot.security.util.SecurityUtil;
import org.activiti.spring.boot.tasks.TaskBaseRuntime;
import org.activiti.spring.boot.tasks.TaskRuntimeEventListeners;
import org.activiti.spring.boot.test.util.TaskCleanUpUtil;
import org.activiti.test.LocalEventSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration
public class ProcessRuntimeTerminatedEndEventTest {

    private static final String PROCESS_TERMINATE_EVENT = "Process_KzwZAEl-";

    @Autowired
    private TaskBaseRuntime taskBaseRuntime;
    @Autowired
    private ProcessBaseRuntime processBaseRuntime;
    @Autowired
    private TaskCleanUpUtil taskCleanUpUtil;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private LocalEventSource localEventSource;

    @Autowired
    private TaskRuntimeEventListeners taskRuntimeEventListeners;

    @Before
    public void setUp(){
        localEventSource.clearEvents();

    }

    @After
    public void tearDown(){
        taskCleanUpUtil.cleanUpWithAdmin();
        localEventSource.clearEvents();
        taskRuntimeEventListeners.clearEvents();
    }

    @Test
    public void should_CancelledProcessesByTerminateEndEventsHaveCancellationReasonSet(){

        securityUtil.logInAs("user");

        ProcessInstance processInstance = processBaseRuntime.startProcessWithProcessDefinitionKey(PROCESS_TERMINATE_EVENT);
        assertThat(processInstance).isNotNull();

        List<Task> tasks = taskBaseRuntime.getTasks(processInstance);
        assertThat(tasks).hasSize(2);
        Task task2 = tasks.get(1);

        taskBaseRuntime.completeTask(task2.getId());

        List<Task> tasksAfterCompletion = taskBaseRuntime.getTasks(processInstance);
        assertThat(tasksAfterCompletion).hasSize(0);

        List<ProcessCancelledEvent> processCancelledEvents =
            localEventSource.getEvents(ProcessCancelledEvent.class);

        assertThat(processCancelledEvents).hasSize(1);
        assertThat(processCancelledEvents.get(0).getCause()).contains("Terminated by end event");

    }

}

package org.activiti.services.core.commands;

import java.util.HashMap;
import java.util.Map;

import org.activiti.services.core.ProcessEngineWrapper;
import org.activiti.services.core.model.commands.CompleteTaskCmd;
import org.activiti.services.core.model.commands.results.CompleteTaskResults;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class CompleteTaskCmdExecutorTest {

    @InjectMocks
    private CompleteTaskCmdExecutor completeTaskCmdExecutor;

    @Mock
    private ProcessEngineWrapper processEngine;

    @Mock
    private MessageChannel commandResults;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void completeTaskCmdExecutorTest() {
        Map<String, Object> variables = new HashMap<>();
        CompleteTaskCmd completeTaskCmd = new CompleteTaskCmd("taskId",
                                                              variables);

        assertThat(completeTaskCmdExecutor.getHandledType()).isEqualTo(CompleteTaskCmd.class);

        completeTaskCmdExecutor.execute(completeTaskCmd);

        verify(processEngine).completeTask(completeTaskCmd);

        verify(commandResults).send(ArgumentMatchers.<Message<CompleteTaskResults>>any());
    }
}
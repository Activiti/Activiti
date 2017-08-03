package org.activiti.services.core.commands;

import org.activiti.services.core.ProcessEngineWrapper;
import org.activiti.services.core.model.commands.ClaimTaskCmd;
import org.activiti.services.core.model.commands.results.ClaimTaskResults;
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

public class ClaimTaskCmdExecutorTest {

    @InjectMocks
    private ClaimTaskCmdExecutor claimTaskCmdExecutor;

    @Mock
    private ProcessEngineWrapper processEngine;

    @Mock
    private MessageChannel commandResults;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void claimTaskCmdExecutorTest() {
        ClaimTaskCmd claimTaskCmd = new ClaimTaskCmd("taskId",
                                                     "assignee");

        assertThat(claimTaskCmdExecutor.getHandledType()).isEqualTo(ClaimTaskCmd.class);

        claimTaskCmdExecutor.execute(claimTaskCmd);

        verify(processEngine).claimTask(claimTaskCmd);

        verify(commandResults).send(ArgumentMatchers.<Message<ClaimTaskResults>>any());
    }
}
package org.activiti.services.core.commands;

import org.activiti.services.core.ProcessEngineWrapper;
import org.activiti.services.core.model.commands.ReleaseTaskCmd;
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

public class ReleaseTaskCmdExecutorTest {

    @InjectMocks
    private ReleaseTaskCmdExecutor releaseTaskCmdExecutor;

    @Mock
    private ProcessEngineWrapper processEngine;

    @Mock
    private MessageChannel commandResults;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void releaseTaskCmdExecutorTest() {

        ReleaseTaskCmd releaseTaskCmd = new ReleaseTaskCmd("taskId");

        assertThat(releaseTaskCmdExecutor.getHandledType()).isEqualTo(ReleaseTaskCmd.class);

        releaseTaskCmdExecutor.execute(releaseTaskCmd);

        verify(processEngine).releaseTask(releaseTaskCmd);

        verify(commandResults).send(ArgumentMatchers.<Message<CompleteTaskResults>>any());
    }
}
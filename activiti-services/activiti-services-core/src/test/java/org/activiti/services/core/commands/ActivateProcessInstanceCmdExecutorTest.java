package org.activiti.services.core.commands;

import org.activiti.services.core.ProcessEngineWrapper;
import org.activiti.services.core.model.commands.ActivateProcessInstanceCmd;
import org.activiti.services.core.model.commands.results.ActivateProcessInstanceResults;
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

public class ActivateProcessInstanceCmdExecutorTest {

    @InjectMocks
    private ActivateProcessInstanceCmdExecutor activateProcessInstanceCmdExecutor;

    @Mock
    private ProcessEngineWrapper processEngine;

    @Mock
    private MessageChannel commandResults;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void activateProcessInstanceCmdExecutorTest() {
        ActivateProcessInstanceCmd activateProcessInstanceCmd = new ActivateProcessInstanceCmd("x");

        assertThat(activateProcessInstanceCmdExecutor.getHandledType()).isEqualTo(ActivateProcessInstanceCmd.class);

        activateProcessInstanceCmdExecutor.execute(activateProcessInstanceCmd);

        verify(processEngine).activate(activateProcessInstanceCmd);

        verify(commandResults).send(ArgumentMatchers.<Message<ActivateProcessInstanceResults>>any());
    }
}
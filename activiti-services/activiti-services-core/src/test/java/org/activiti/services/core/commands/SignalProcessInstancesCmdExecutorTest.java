package org.activiti.services.core.commands;

import org.activiti.services.core.ProcessEngineWrapper;
import org.activiti.services.core.model.commands.SignalProcessInstancesCmd;
import org.activiti.services.core.model.commands.results.SignalProcessInstancesResults;
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

public class SignalProcessInstancesCmdExecutorTest {

    @InjectMocks
    private SignalProcessInstancesCmdExecutor signalProcessInstancesCmdExecutor;

    @Mock
    private ProcessEngineWrapper processEngine;

    @Mock
    private MessageChannel commandResults;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void signalProcessInstancesCmdExecutorTest() {
        SignalProcessInstancesCmd signalProcessInstancesCmd = new SignalProcessInstancesCmd("x");

        assertThat(signalProcessInstancesCmdExecutor.getHandledType()).isEqualTo(SignalProcessInstancesCmd.class);

        signalProcessInstancesCmdExecutor.execute(signalProcessInstancesCmd);

        verify(processEngine).signal(signalProcessInstancesCmd);

        verify(commandResults).send(ArgumentMatchers.<Message<SignalProcessInstancesResults>>any());
    }
}
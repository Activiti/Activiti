package org.activiti.services.core.commands;

import java.util.Date;

import org.activiti.services.core.ProcessEngineWrapper;
import org.activiti.services.core.model.ProcessInstance;
import org.activiti.services.core.model.commands.StartProcessInstanceCmd;
import org.activiti.services.core.model.commands.results.StartProcessInstanceResults;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class StartProcessInstanceCmdExecutorTest {

    @InjectMocks
    private StartProcessInstanceCmdExecutor startProcessInstanceCmdExecutor;

    @Mock
    private ProcessEngineWrapper processEngine;

    @Mock
    private MessageChannel commandResults;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void startProcessInstanceCmdExecutorTest() {
        StartProcessInstanceCmd startProcessInstanceCmd = new StartProcessInstanceCmd("x");

        ProcessInstance fakeProcessInstance = new ProcessInstance("fakeId",
                                                                  "name",
                                                                  "description",
                                                                  "processDefinitionId",
                                                                  "initiator",
                                                                  new Date(),
                                                                  "businessKey",
                                                                  "status");

        given(processEngine.startProcess(any())).willReturn(fakeProcessInstance);

        assertThat(startProcessInstanceCmdExecutor.getHandledType()).isEqualTo(StartProcessInstanceCmd.class);

        startProcessInstanceCmdExecutor.execute(startProcessInstanceCmd);

        verify(processEngine).startProcess(startProcessInstanceCmd);

        verify(commandResults).send(ArgumentMatchers.<Message<StartProcessInstanceResults>>any());
    }
}
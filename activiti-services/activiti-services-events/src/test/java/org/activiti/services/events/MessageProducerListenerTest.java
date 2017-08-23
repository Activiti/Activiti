package org.activiti.services.events;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.services.api.events.ProcessEngineEvent;
import org.activiti.services.events.tests.util.MockMessageChannel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MessageProducerListenerTest.ContextConfig.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
        classes = {MessageProducerListenerTest.class, MessageProducerActivitiEventListener.class, MessageProducerCommandContextCloseListener.class})
public class MessageProducerListenerTest {

    @Autowired
    private MessageProducerActivitiEventListener eventListener;

    @Configuration
    @ComponentScan({"org.activiti.services.events.tests.util", "org.activiti.services.events.converter", "org.activiti.services.core.model.converter"})
    public class ContextConfig {
    }

    @Test
    public void executeListener() throws Exception {
        ProcessEngine processEngine = ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration().buildProcessEngine();
        deploy("SimpleProcess", processEngine);
        deploy("RollbackProcess", processEngine);
        deploy("AsyncErrorProcess", processEngine);
        processEngine.getRuntimeService().addEventListener(eventListener);
        processEngine.getRuntimeService().startProcessInstanceByKey("simpleProcess");

        ProcessEngineEvent[] events = (ProcessEngineEvent[]) MockMessageChannel.messageResult.getPayload();
        assertThat(events.length).isEqualTo(7);
        assertThat(events[0].getClass()).isEqualTo(ProcessStartedEventImpl.class);
        assertThat(events[1].getClass()).isEqualTo(ActivityStartedEventImpl.class);
        assertThat(events[2].getClass()).isEqualTo(ActivityCompletedEventImpl.class);
        assertThat(events[3].getClass()).isEqualTo(SequenceFlowTakenEventImpl.class);
        assertThat(events[4].getClass()).isEqualTo(ActivityStartedEventImpl.class);
        assertThat(events[5].getClass()).isEqualTo(ActivityCompletedEventImpl.class);
        assertThat(events[6].getClass()).isEqualTo(ProcessCompletedEventImpl.class);

        MockMessageChannel.messageResult = null;
        try {
            processEngine.getRuntimeService().startProcessInstanceByKey("rollbackProcess");
        } catch (Exception e) {
        }
        assertThat(MockMessageChannel.messageResult).isEqualTo(null);

        MockMessageChannel.messageResult = null;
        try {
            processEngine.getRuntimeService().startProcessInstanceByKey("asyncErrorProcess");
        } catch (Exception e) {
        }
        assertThat(MockMessageChannel.messageResult).isNotNull();
        events = (ProcessEngineEvent[]) MockMessageChannel.messageResult.getPayload();
        assertThat(events.length).isEqualTo(4);
        assertThat(events[0].getClass()).isEqualTo(ProcessStartedEventImpl.class);
        assertThat(events[1].getClass()).isEqualTo(ActivityStartedEventImpl.class);
        assertThat(events[2].getClass()).isEqualTo(ActivityCompletedEventImpl.class);
        assertThat(events[3].getClass()).isEqualTo(SequenceFlowTakenEventImpl.class);
    }

    public static void deploy(final String processDefinitionKey, ProcessEngine processEngine) throws IOException {
        try (InputStream is = ClassLoader.getSystemResourceAsStream("processes/" + processDefinitionKey + ".bpmn")) {
            processEngine.getRepositoryService()
                         .createDeployment()
                         .addInputStream(processDefinitionKey + ".bpmn", is)
                         .deploy();
        }
    }
}
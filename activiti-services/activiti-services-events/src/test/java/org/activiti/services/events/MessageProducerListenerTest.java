package org.activiti.services.events;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.StartEvent;
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
    MessageProducerActivitiEventListener eventListener;

    @Configuration
    @ComponentScan({"org.activiti.services.events.tests.util", "org.activiti.services.events.converter", "org.activiti.services.core.model.converter"})
    public class ContextConfig {
    }

    @Test
    public void executeListener() {
        ProcessEngine processEngine = ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration().buildProcessEngine();
        processEngine.getRepositoryService().createDeployment().addBpmnModel("simpleTestProcess.bpmn",
                                                                             getSimpleTestProcess()).deploy();
        processEngine.getRuntimeService().addEventListener(eventListener);
        processEngine.getRuntimeService().startProcessInstanceByKey("simpleTestProcess");

        ProcessEngineEvent[] events = (ProcessEngineEvent[]) MockMessageChannel.messageResult.getPayload();
        assertThat(events.length).isEqualTo(7);
        assertThat(events[0].getClass()).isEqualTo(ProcessStartedEventImpl.class);
        assertThat(events[1].getClass()).isEqualTo(ActivityStartedEventImpl.class);
        assertThat(events[2].getClass()).isEqualTo(ActivityCompletedEventImpl.class);
        assertThat(events[3].getClass()).isEqualTo(SequenceFlowTakenEventImpl.class);
        assertThat(events[4].getClass()).isEqualTo(ActivityStartedEventImpl.class);
        assertThat(events[5].getClass()).isEqualTo(ActivityCompletedEventImpl.class);
        assertThat(events[6].getClass()).isEqualTo(ProcessCompletedEventImpl.class);
    }

    private BpmnModel getSimpleTestProcess() {
        Process p = new Process();
        p.setId("simpleTestProcess");
        p.setName("Simple Test Process");

        StartEvent startEvent = new StartEvent();
        startEvent.setName("start");
        startEvent.setId("start");

        EndEvent endEvent = new EndEvent();
        endEvent.setName("end");
        endEvent.setId("end");

        SequenceFlow flow = new SequenceFlow();
        flow.setId("flow1");
        flow.setSourceRef("start");
        flow.setTargetRef("end");
        p.addFlowElement(startEvent);
        p.addFlowElement(endEvent);
        p.addFlowElement(flow);

        List<SequenceFlow> startEventoutgoingFlows = new ArrayList<>();
        startEventoutgoingFlows.add(flow);
        startEvent.setOutgoingFlows(startEventoutgoingFlows);

        BpmnModel model = new BpmnModel();
        model.addProcess(p);
        return model;
    }
}

package org.activiti.audit.tests;

import org.activiti.services.audit.producer.app.AuditProducerChannels;
import org.junit.Test;
import org.junit.runner.RunWith;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.annotation.EnableBinding;

import org.springframework.cloud.stream.messaging.Processor;

import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.integration.annotation.Transformer;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuditProducerChannelTest {


    @Autowired
    private MyProducerApp producer;

    @Autowired
    private MessageCollector messageCollector;

    @Test
    @SuppressWarnings("unchecked")
    public void testWiring() {
        Message<String> message = new GenericMessage<>("hello");
        producer.send(message);
        Message<String> received = (Message<String>) messageCollector.forChannel(producer.channels.auditProducer()).poll();
        assertThat(received.getPayload(), equalTo("hello world"));
    }


    @SpringBootApplication
    @EnableBinding(AuditProducerChannels.class)
    public static class MyProducerApp {

        @Autowired
        private AuditProducerChannels channels;


        public void send(Message<String> message){
            channels.auditProducer().send(new GenericMessage<String>(message.getPayload()+" world"));
        }

    }
}
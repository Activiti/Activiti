package org.activiti.spring.integration;

import org.activiti.engine.ProcessEngine;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

/**
 * Spring Integration Java Configuration DSL integration for Activiti.
 *
 * @author Josh Long
 */
public class Activiti {

    /**
     * This is the component that you'll use in your Spring Integration
     * {@link org.springframework.integration.dsl.IntegrationFlow}.
     */
    public static ActivitiInboundGateway inboundGateway(ProcessEngine processEngine, String... varsToPreserve) {
        return new ActivitiInboundGateway(processEngine, varsToPreserve);
    }

    /**
     * This is the bean to expose and then reference
     * from your Activiti BPMN flow in an expression.
     */
    public static IntegrationActivityBehavior inboundGatewayActivityBehavior(ActivitiInboundGateway gateway) {
        return new IntegrationActivityBehavior(gateway);
    }

    /**
     * Any message that enters this {@link org.springframework.messaging.MessageHandler}
     * containing a {@code executionId} parameter will trigger a
     * {@link org.activiti.engine.RuntimeService#signal(String)}.
     */
    public static MessageHandler signallingMessageHandler(final ProcessEngine processEngine) {
        return new MessageHandler() {
            @Override
            public void handleMessage(Message<?> message) throws MessagingException {
                String executionId = message.getHeaders().containsKey("executionId") ?
                        (String) message.getHeaders().get("executionId") : (String) null;

                if (null != executionId)
                    processEngine.getRuntimeService().signal(executionId);
            }
        };
    }
}

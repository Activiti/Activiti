/* * Copyright 2012-2014 the original author or authors. * * Licensed under the Apache License, Version 2.0 (the "License"); * you may not use this file except in compliance with the License. * You may obtain a copy of the License at * * http://www.apache.org/licenses/LICENSE-2.0 * * Unless required by applicable law or agreed to in writing, software * distributed under the License is distributed on an "AS IS" BASIS, * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. * See the License for the specific language governing permissions and * limitations under the License. */
package org.activiti.spring.boot;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.bpmn.behavior.ReceiveTaskActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.gateway.MessagingGatewaySupport;
import org.springframework.integration.mapping.HeaderMapper;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Auto-configuration and starter for Spring Integration adapter * support for Activiti.
 * https://github.com/Activiti/Activiti/blob/master/modules/activiti-engine/src/main/java/org/activiti/engine/impl/bpmn/behavior/ReceiveTaskActivityBehavior.java
 * https://github.com/spring-projects/spring-integration-extensions/blob/master/spring-integration-java-dsl/src/test/java/org/springframework/integration/dsl/test/flows/IntegrationFlowTests.java#L1899
 * https://github.com/spring-projects/spring-integration-extensions/wiki/Spring-Integration-Java-DSL-Reference#adapters
 * https://github.com/joshlong/spring-integration-activiti/blob/master/src/main/java/org/springframework/integration/activiti/gateway/AbstractActivityBehaviorMessagingGateway.java
 * https://github.com/joshlong/spring-integration-activiti/tree/master/src/main/java/org/springframework/integration/activiti/gateway
 */
@Configuration
@AutoConfigureAfter(BasicProcessEngineAutoConfiguration.class)
public class ProcessEngineMessagingAutoConfiguration {
}

class ExposeActiviti extends ReceiveTaskActivityBehavior {

    @Override
    public void leave(ActivityExecution execution) {
        super.leave(execution);
    }

    private final ExposeSpringIntegration gateway;

    public ExposeActiviti(ExposeSpringIntegration gateway) {
        this.gateway = gateway;
    }

    @Override
    public void execute(ActivityExecution execution) throws Exception {
        gateway.execute(this, execution);
    }

    @Override
    public void signal(ActivityExecution execution, String signalName, Object data) throws Exception {
        gateway.signal(this, execution, signalName, data);
    }
}

class ExposeSpringIntegration extends MessagingGatewaySupport {

    private final ProcessEngine processEngine;
    private final ProcessVariableHeaderMapper headerMapper;

    public ExposeSpringIntegration(ProcessEngine processEngine) {
        this.processEngine = processEngine;
        this.headerMapper = new ProcessVariableHeaderMapper(processEngine);
    }

    public void execute(ExposeActiviti receiveTaskActivityBehavior,
                        ActivityExecution execution) throws Exception {
        MessageBuilder<?> mb = doBasicOutboundMessageConstruction(execution);
        Message<?> reply = sendAndReceiveMessage(mb.build());
        Map<String, Object> vars = new HashMap<String, Object>();
        headerMapper.fromHeaders(reply.getHeaders(), vars);
     /*   for (String key : vars.keySet()) {
            callback.setProcessVariable(processEngine, activityExecution, key, vars.get(key));
        }
        callback.signal(processEngine, activityExecution);*/

        receiveTaskActivityBehavior.leave(execution);

    }

    private MessageBuilder<?> doBasicOutboundMessageConstruction(ActivityExecution execution) {
        Map<String, ?> headers = headerMapper.toHeaders(execution.getVariables());
        return MessageBuilder.withPayload(execution).copyHeadersIfAbsent(new HashMap<String, Object>()).copyHeaders(headers);
    }

    public void signal(ExposeActiviti exposeActiviti, ActivityExecution execution, String signalName, Object data) {
        exposeActiviti.leave(execution);
    }
}

class ProcessVariableHeaderMapper implements HeaderMapper<Map<String, Object>> {
    private final ProcessEngine processEngine;

    public ProcessVariableHeaderMapper(ProcessEngine processEngine) {
        this.processEngine = processEngine;
    }

    @Override
    public void fromHeaders(MessageHeaders headers, Map<String, Object> target) {
        // inbound SI msg. take the headers and convert it to
    }

    @Override
    public Map<String, Object> toHeaders(Map<String, Object> source) {
        return source;
    }
}
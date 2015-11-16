/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.spring.integration;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.springframework.integration.gateway.MessagingGatewaySupport;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * As a process enters a wait-state, this forwards the
 * flow into Spring Integration. Spring Integration flows
 * may ultimately return a reply message and that will signal the
 * execution.
 *
 * @author Josh Long
 */
public class ActivitiInboundGateway extends MessagingGatewaySupport {

    private String executionId = "executionId";
    private String processInstanceId = "processInstanceId";
    private String processDefinitionId = "processDefinitionId";

    private final ProcessVariableHeaderMapper headerMapper;
    private ProcessEngine processEngine;

    private Set<String> sync = new ConcurrentSkipListSet<String>();

    public ActivitiInboundGateway(ProcessEngine processEngine, String... pvsOrHeadersToPreserve) {
        Collections.addAll(this.sync, pvsOrHeadersToPreserve);
        this.processEngine = processEngine;
        this.headerMapper = new ProcessVariableHeaderMapper(sync);
        this.initializeDefaultPreservedHeaders();
    }

    protected void initializeDefaultPreservedHeaders() {
        this.sync.add(executionId);
        this.sync.add(processDefinitionId);
        this.sync.add(processInstanceId);
    }

    public void execute(IntegrationActivityBehavior receiveTaskActivityBehavior,
                        ActivityExecution execution) throws Exception {
        Map<String, Object> stringObjectMap = new HashMap<String, Object>();
        stringObjectMap.put(executionId, execution.getId());

        stringObjectMap.put(processInstanceId, execution.getProcessInstanceId());
        stringObjectMap.put(processDefinitionId, execution.getProcessDefinitionId());
        stringObjectMap.putAll(headerMapper.toHeaders(execution.getVariables()));
        MessageBuilder<?> mb = MessageBuilder.withPayload(execution).copyHeaders(stringObjectMap);
        Message<?> reply = sendAndReceiveMessage(mb.build());
        if (null != reply) {
            Map<String, Object> vars = new HashMap<String, Object>();
            headerMapper.fromHeaders(reply.getHeaders(), vars);

            for (String k : vars.keySet()) {
                processEngine.getRuntimeService().setVariable(execution.getId(), k, vars.get(k));
            }
            receiveTaskActivityBehavior.leave(execution);
        }
    }

    public void signal(IntegrationActivityBehavior receiveTaskActivityBehavior, ActivityExecution execution, String signalName, Object data) {
        receiveTaskActivityBehavior.leave(execution);
    }


}

package org.activiti.spring.integration;

import org.activiti.engine.impl.bpmn.behavior.ReceiveTaskActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;

public class IntegrationActivityBehavior extends ReceiveTaskActivityBehavior {

    private final ActivitiInboundGateway gateway;

    public IntegrationActivityBehavior(ActivitiInboundGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    public void execute(ActivityExecution execution) throws Exception {
        gateway.execute(this, execution);
    }

    @Override
    public void leave(ActivityExecution execution) {
        super.leave(execution);
    }

    @Override
    public void signal(ActivityExecution execution, String signalName, Object data) {
        gateway.signal(this, execution, signalName, data);
    }
}

package org.activiti.spring.integration;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.bpmn.behavior.ReceiveTaskActivityBehavior;

public class IntegrationActivityBehavior extends ReceiveTaskActivityBehavior {

    private final ActivitiInboundGateway gateway;

    public IntegrationActivityBehavior(ActivitiInboundGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    public void execute(DelegateExecution execution) {
        gateway.execute(this, execution);
    }

    @Override
    public void leave(DelegateExecution execution) {
        super.leave(execution);
    }

    @Override
    public void trigger(DelegateExecution execution, String signalName, Object data) {
        gateway.signal(this, execution, signalName, data);
    }
}

package org.activiti.services.core.model.commands;

public class SuspendProcessInstanceCmd implements Command {
    private String processInstanceId;

    public SuspendProcessInstanceCmd() {
    }

    public SuspendProcessInstanceCmd(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }
}

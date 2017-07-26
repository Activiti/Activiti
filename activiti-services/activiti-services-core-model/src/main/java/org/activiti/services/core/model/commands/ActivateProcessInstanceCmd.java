package org.activiti.services.core.model.commands;

public class ActivateProcessInstanceCmd implements Command {
    private String processInstanceId;

    public ActivateProcessInstanceCmd() {
    }

    public ActivateProcessInstanceCmd(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }
}

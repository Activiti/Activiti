package org.activiti.services.core.model.commands;

import java.util.UUID;

public class SuspendProcessInstanceCmd implements Command {

    private String id;
    private String processInstanceId;

    public SuspendProcessInstanceCmd() {
        this.id = UUID.randomUUID().toString();
    }

    public SuspendProcessInstanceCmd(String processInstanceId) {
        this();
        this.processInstanceId = processInstanceId;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }
}

package org.activiti.services.core.model.commands;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ActivateProcessInstanceCmd extends AbstractCommand {

    private String processInstanceId;

    @JsonCreator
    public ActivateProcessInstanceCmd(@JsonProperty("processInstanceId") String processInstanceId) {
        super();
        this.processInstanceId = processInstanceId;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }
}

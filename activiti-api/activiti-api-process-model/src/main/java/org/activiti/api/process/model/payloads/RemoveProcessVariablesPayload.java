package org.activiti.api.process.model.payloads;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.activiti.api.model.shared.Payload;

public class RemoveProcessVariablesPayload implements Payload {

    private String id;
    private String processInstanceId;
    private List<String> variableNames = new ArrayList<>();

    public RemoveProcessVariablesPayload() {
        this.id = UUID.randomUUID().toString();
    }

    public RemoveProcessVariablesPayload(String processInstanceId,
                                         List<String> variableNames) {
        this();
        this.processInstanceId = processInstanceId;
        this.variableNames = variableNames;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public List<String> getVariableNames() {
        return variableNames;
    }

    public void setVariableNames(List<String> variableNames) {
        this.variableNames = variableNames;
    }
}

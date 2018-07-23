package org.activiti.runtime.api.model.payloads;

import java.util.Map;

public class StartProcessPayload {
    private String processDefinitionId;
    private String processDefinitionKey;
    private String businessKey;
    private Map<String, Object> variables;

    public StartProcessPayload(String processDefinitionId,
                               String processDefinitionKey,
                               String businessKey,
                               Map<String, Object> variables) {
        this.processDefinitionId = processDefinitionId;
        this.processDefinitionKey = processDefinitionKey;
        this.businessKey = businessKey;
        this.variables = variables;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }
}

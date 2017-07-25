package org.activiti.cmdendpoint.cmds;

import java.util.Map;

public class StartProcessInstanceCmd implements org.activiti.cmdendpoint.cmds.Command {

    private String processDefinitionId;
    private Map<String, String> variables;

    public StartProcessInstanceCmd() {
    }

    public StartProcessInstanceCmd(String processDefinitionId,
                                   Map<String, String> variables) {
        this.processDefinitionId = processDefinitionId;
        this.variables = variables;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

}

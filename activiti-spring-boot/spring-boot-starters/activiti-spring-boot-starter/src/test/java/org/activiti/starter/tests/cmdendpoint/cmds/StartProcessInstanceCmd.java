package org.activiti.starter.tests.cmdendpoint.cmds;

import java.util.Map;

public class StartProcessInstanceCmd implements org.activiti.starter.tests.cmdendpoint.cmds.Command {

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

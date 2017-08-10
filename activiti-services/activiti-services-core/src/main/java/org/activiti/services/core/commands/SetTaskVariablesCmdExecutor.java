package org.activiti.services.core.commands;

import org.activiti.services.core.ProcessEngineWrapper;
import org.activiti.services.core.model.commands.SetTaskVariablesCmd;
import org.activiti.services.core.model.commands.results.SetTaskVariablesResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class SetTaskVariablesCmdExecutor implements CommandExecutor<SetTaskVariablesCmd> {

    private ProcessEngineWrapper processEngine;
    private MessageChannel commandResults;

    @Autowired
    public SetTaskVariablesCmdExecutor(ProcessEngineWrapper processEngine,
                                       MessageChannel commandResults) {
        this.processEngine = processEngine;
        this.commandResults = commandResults;
    }

    @Override
    public Class getHandledType() {
        return SetTaskVariablesCmd.class;
    }

    @Override
    public void execute(SetTaskVariablesCmd cmd) {
        processEngine.setTaskVariables(cmd);
        SetTaskVariablesResults cmdResult = new SetTaskVariablesResults(cmd.getId());
        commandResults.send(MessageBuilder.withPayload(cmdResult).build());
    }
}

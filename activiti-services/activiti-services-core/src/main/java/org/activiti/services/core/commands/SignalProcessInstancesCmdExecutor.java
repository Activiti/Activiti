package org.activiti.services.core.commands;

import org.activiti.services.core.ProcessEngineWrapper;
import org.activiti.services.core.model.commands.SignalProcessInstancesCmd;
import org.activiti.services.core.model.commands.results.SignalProcessInstancesResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class SignalProcessInstancesCmdExecutor implements CommandExecutor<SignalProcessInstancesCmd> {

    private ProcessEngineWrapper processEngine;
    private MessageChannel commandResults;

    @Autowired
    public SignalProcessInstancesCmdExecutor(ProcessEngineWrapper processEngine,
                                             MessageChannel commandResults) {
        this.processEngine = processEngine;
        this.commandResults = commandResults;
    }

    @Override
    public Class getHandledType() {
        return SignalProcessInstancesCmd.class;
    }

    @Override
    public void execute(SignalProcessInstancesCmd cmd) {
        processEngine.signal(cmd);
        SignalProcessInstancesResults cmdResult = new SignalProcessInstancesResults(cmd.getId());
        commandResults.send(MessageBuilder.withPayload(cmdResult).build());
    }
}

package org.activiti.services.core.commands;

import org.activiti.services.core.ProcessEngineWrapper;
import org.activiti.services.core.model.commands.ActivateProcessInstanceCmd;
import org.activiti.services.core.model.commands.results.ActivateProcessInstanceResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class ActivateProcessInstanceCmdExecutor implements CommandExecutor<ActivateProcessInstanceCmd> {

    private ProcessEngineWrapper processEngine;
    private MessageChannel commandResults;

    @Autowired
    public ActivateProcessInstanceCmdExecutor(ProcessEngineWrapper processEngine,
                                              MessageChannel commandResults) {
        this.processEngine = processEngine;
        this.commandResults = commandResults;
    }

    @Override
    public Class getHandledType() {
        return ActivateProcessInstanceCmd.class;
    }

    @Override
    public void execute(ActivateProcessInstanceCmd cmd) {
        processEngine.activate(cmd);
        ActivateProcessInstanceResults cmdResult = new ActivateProcessInstanceResults(cmd.getId());
        commandResults.send(MessageBuilder.withPayload(cmdResult).build());
    }
}

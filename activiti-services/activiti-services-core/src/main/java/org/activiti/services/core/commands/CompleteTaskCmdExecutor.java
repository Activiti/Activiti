package org.activiti.services.core.commands;

import org.activiti.services.core.ProcessEngineWrapper;
import org.activiti.services.core.model.commands.Command;
import org.activiti.services.core.model.commands.CompleteTaskCmd;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;

@Component
public class CompleteTaskCmdExecutor implements CommandExecutor<CompleteTaskCmd> {

    private ProcessEngineWrapper processEngine;
    private MessageChannel commandResults;

    @Autowired
    public CompleteTaskCmdExecutor(ProcessEngineWrapper processEngine,
                                   MessageChannel commandResults) {
        this.processEngine = processEngine;
        this.commandResults = commandResults;
    }

    @Override
    public Class getHandledType() {
        return CompleteTaskCmd.class;
    }

    @Override
    public void execute(String cmdId, CompleteTaskCmd cmd) {
        processEngine.completeTask(cmd);
    }
}

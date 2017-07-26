package org.activiti.services.core.commands;

import org.activiti.services.core.ProcessEngineWrapper;
import org.activiti.services.core.model.commands.Command;
import org.activiti.services.core.model.commands.SignalProcessInstancesCmd;
import org.activiti.services.core.model.commands.StartProcessInstanceCmd;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;

@Component
public class SignalProcessInstancesCmdExecutor implements CommandExecutor {

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
        return StartProcessInstanceCmd.class;
    }

    @Override
    public void execute(Command cmd) {
        processEngine.signal((SignalProcessInstancesCmd) cmd);
    }
}

package org.activiti.services.core.commands;

import org.activiti.services.core.ProcessEngineWrapper;
import org.activiti.services.core.model.commands.Command;
import org.activiti.services.core.model.commands.SuspendProcessInstanceCmd;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;

@Component
public class SuspendProcessInstanceCmdExecutor implements CommandExecutor<SuspendProcessInstanceCmd> {

    private ProcessEngineWrapper processEngine;
    private MessageChannel commandResults;

    @Autowired
    public SuspendProcessInstanceCmdExecutor(ProcessEngineWrapper processEngine,
                                             MessageChannel commandResults) {
        this.processEngine = processEngine;
        this.commandResults = commandResults;
    }

    @Override
    public Class getHandledType() {
        return SuspendProcessInstanceCmd.class;
    }

    @Override
    public void execute(String cmdId, SuspendProcessInstanceCmd cmd) {
        processEngine.suspend(cmd);
    }
}

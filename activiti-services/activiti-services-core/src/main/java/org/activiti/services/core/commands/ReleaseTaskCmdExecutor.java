package org.activiti.services.core.commands;

import org.activiti.services.core.ProcessEngineWrapper;
import org.activiti.services.core.model.commands.Command;
import org.activiti.services.core.model.commands.ReleaseTaskCmd;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;

@Component
public class ReleaseTaskCmdExecutor implements CommandExecutor {

    private ProcessEngineWrapper processEngine;
    private MessageChannel commandResults;

    @Autowired
    public ReleaseTaskCmdExecutor(ProcessEngineWrapper processEngine,
                                  MessageChannel commandResults) {
        this.processEngine = processEngine;
        this.commandResults = commandResults;
    }

    @Override
    public Class getHandledType() {
        return ReleaseTaskCmd.class;
    }

    @Override
    public void execute(Command cmd) {
        processEngine.releaseTask((ReleaseTaskCmd) cmd);
    }
}

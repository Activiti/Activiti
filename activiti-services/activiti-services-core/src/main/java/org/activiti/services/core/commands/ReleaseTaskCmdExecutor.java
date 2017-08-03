package org.activiti.services.core.commands;

import org.activiti.services.core.ProcessEngineWrapper;
import org.activiti.services.core.model.commands.ReleaseTaskCmd;
import org.activiti.services.core.model.commands.results.ReleaseTaskResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class ReleaseTaskCmdExecutor implements CommandExecutor<ReleaseTaskCmd> {

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
    public void execute(ReleaseTaskCmd cmd) {
        processEngine.releaseTask(cmd);
        ReleaseTaskResults cmdResult = new ReleaseTaskResults(cmd.getId());
        commandResults.send(MessageBuilder.withPayload(cmdResult).build());
    }
}

package org.activiti.services.core.commands;

import org.activiti.services.core.ProcessEngineWrapper;
import org.activiti.services.core.model.ProcessInstance;
import org.activiti.services.core.model.commands.StartProcessInstanceCmd;
import org.activiti.services.core.model.commands.results.StartProcessInstanceResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class StartProcessInstanceCmdExecutor implements CommandExecutor<StartProcessInstanceCmd> {

    private ProcessEngineWrapper processEngine;
    private MessageChannel commandResults;

    @Autowired
    public StartProcessInstanceCmdExecutor(ProcessEngineWrapper processEngine,
                                           MessageChannel commandResults) {
        this.processEngine = processEngine;
        this.commandResults = commandResults;
    }

    @Override
    public Class getHandledType() {
        return StartProcessInstanceCmd.class;
    }

    @Override
    public void execute(String cmdId,
                        StartProcessInstanceCmd cmd) {
        ProcessInstance processInstance = processEngine.startProcess(cmd);
        StartProcessInstanceResults cmdResult = new StartProcessInstanceResults(cmdId,
                                                                                processInstance.getId());
        commandResults.send(MessageBuilder.withPayload(cmdResult).setHeader("cmdId",
                                                                            cmdId).build());
    }
}

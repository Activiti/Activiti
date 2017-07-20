package org.activiti.services.core.commands;

import org.activiti.services.core.ProcessEngineWrapper;
import org.activiti.services.core.model.ProcessInstance;
import org.activiti.services.core.model.commands.Command;
import org.activiti.services.core.model.commands.StartProcessInstanceCmd;
import org.activiti.services.events.ProcessEngineChannels;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Component;

@Component
public class CommandEndpoint {

    private ProcessEngineWrapper processEngine;

    @StreamListener(ProcessEngineChannels.COMMAND_CONSUMER)
    public void consumeCommand(Command cmd) {
        if (cmd instanceof StartProcessInstanceCmd) {
            ProcessInstance processInstance = processEngine.startProcess((StartProcessInstanceCmd) cmd);
            System.out.println(processInstance.getId() + " -  " + processInstance);
        }
    }
}

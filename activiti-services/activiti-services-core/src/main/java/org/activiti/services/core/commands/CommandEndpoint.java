package org.activiti.services.core.commands;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.activiti.services.core.model.commands.Command;
import org.activiti.services.events.ProcessEngineChannels;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class CommandEndpoint {

    private Map<Class, CommandExecutor> commandExecutors;

    @Autowired
    public CommandEndpoint(Set<CommandExecutor> cmdExecutors) {
        this.commandExecutors = cmdExecutors.stream().collect(Collectors.toMap(CommandExecutor::getHandledType,
                                                                               Function.identity()));
    }

    @StreamListener(ProcessEngineChannels.COMMAND_CONSUMER)
    public void consumeCommand(Command cmd, @Header("cmdId") String cmdId) {

        CommandExecutor cmdExecutor = commandExecutors.get(cmd.getClass());
        if (cmdExecutor != null) {
            cmdExecutor.execute(cmdId, cmd);
            return;
        }

        System.out.println(">>> No Command Found for type: " + cmd.getClass());
    }
}

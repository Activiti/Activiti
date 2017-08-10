package org.activiti.services.core.commands;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.activiti.services.core.model.commands.AbstractCommand;
import org.activiti.services.events.ProcessEngineChannels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Component;

@Component
public class CommandEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandEndpoint.class);
    private Map<Class, CommandExecutor> commandExecutors;

    @Autowired
    public CommandEndpoint(Set<CommandExecutor> cmdExecutors) {
        this.commandExecutors = cmdExecutors.stream().collect(Collectors.toMap(CommandExecutor::getHandledType,
                                                                               Function.identity()));
    }

    @StreamListener(ProcessEngineChannels.COMMAND_CONSUMER)
    public void consumeCommand(AbstractCommand cmd) {

        CommandExecutor cmdExecutor = commandExecutors.get(cmd.getClass());
        if (cmdExecutor != null) {
            cmdExecutor.execute(cmd);
            return;
        }

        LOGGER.debug(">>> No Command Found for type: " + cmd.getClass());
    }
}

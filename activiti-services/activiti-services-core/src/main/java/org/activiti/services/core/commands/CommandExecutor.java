package org.activiti.services.core.commands;

import org.activiti.services.api.commands.Command;

public interface CommandExecutor<T extends Command> {

    Class getHandledType();

    void execute(T cmd);
}

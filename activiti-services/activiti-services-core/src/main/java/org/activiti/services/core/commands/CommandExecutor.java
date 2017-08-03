package org.activiti.services.core.commands;

import org.activiti.services.core.model.commands.Command;

public interface CommandExecutor<T extends Command> {

    Class getHandledType();

    void execute(String cmdId, T cmd);
}

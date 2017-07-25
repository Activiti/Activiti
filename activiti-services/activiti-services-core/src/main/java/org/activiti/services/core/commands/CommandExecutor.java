package org.activiti.services.core.commands;

import org.activiti.services.core.model.commands.Command;

public interface CommandExecutor {

    Class getHandledType();

    void execute(Command cmd);
}

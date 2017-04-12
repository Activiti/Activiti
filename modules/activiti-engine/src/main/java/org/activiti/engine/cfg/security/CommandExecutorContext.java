package org.activiti.engine.cfg.security;

public class CommandExecutorContext {

    private static CommandExecutorFactory shellCommandExecutorFactory;

    public static void setShellExecutorContextFactory(CommandExecutorFactory shellCommandExecutorFactory) {
        CommandExecutorContext.shellCommandExecutorFactory = shellCommandExecutorFactory;
    }

    public static CommandExecutorFactory getShellCommandExecutorFactory() {
        return shellCommandExecutorFactory;
    }
}

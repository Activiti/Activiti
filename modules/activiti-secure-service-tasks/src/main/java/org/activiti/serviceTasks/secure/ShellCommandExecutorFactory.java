package org.activiti.serviceTasks.secure;

import java.util.HashSet;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.cfg.security.ExecutorContext;
import org.activiti.engine.cfg.security.CommandExecutorFactory;
import org.activiti.engine.impl.util.ShellCommandExecutor;
import org.activiti.engine.impl.util.ShellExecutorContext;

public class ShellCommandExecutorFactory implements CommandExecutorFactory {
    private HashSet<String> whiteListedCommands;

    public void setWhiteListedCommands(HashSet<String> whiteListedCommands) {
        this.whiteListedCommands = whiteListedCommands;
    }

    public HashSet<String> getWhiteListedCommands() {
        return whiteListedCommands;
    }

    @Override
    public ShellCommandExecutor createExecutor(ExecutorContext context) {
        if (context instanceof ShellExecutorContext) {
            return new SecureShellCommandExecutor((ShellExecutorContext) context, whiteListedCommands);
        } else{
            throw new ActivitiException("Invalid context: ExecutorContext !");
        }
    }
}

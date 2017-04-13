package org.activiti.serviceTasks.secure;

import java.util.Set;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.cfg.security.CommandExecutorFactory;
import org.activiti.engine.cfg.security.ExecutorContext;
import org.activiti.engine.impl.util.ShellCommandExecutor;
import org.activiti.engine.impl.util.ShellExecutorContext;

/**
 * @author Vasile Dirla
 */
public class ShellCommandExecutorFactory implements CommandExecutorFactory {
    private Set<String> whiteListedCommands;

    public void setWhiteListedCommands(Set<String> whiteListedCommands) {
        this.whiteListedCommands = whiteListedCommands;
    }

    public Set<String> getWhiteListedCommands() {
        return whiteListedCommands;
    }

    @Override
    public ShellCommandExecutor createExecutor(ExecutorContext context) {
        if (context instanceof ShellExecutorContext) {
            return new SecureShellCommandExecutor((ShellExecutorContext) context, whiteListedCommands);
        } else {
            throw new ActivitiException("Invalid context: ExecutorContext !");
        }
    }
}

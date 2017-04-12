package org.activiti.engine.impl.util;

import org.activiti.engine.impl.pvm.delegate.ActivityExecution;

/**
 * @author Vasile Dirla
 */
public interface CommandExecutor {
    void executeCommand(ActivityExecution execution) throws Exception;
}

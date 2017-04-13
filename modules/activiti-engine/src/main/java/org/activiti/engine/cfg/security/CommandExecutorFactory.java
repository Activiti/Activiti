package org.activiti.engine.cfg.security;

import org.activiti.engine.impl.util.CommandExecutor;

/**
 * @author Vasile Dirla
 */
public interface CommandExecutorFactory {

    CommandExecutor createExecutor(ExecutorContext executorContext);

}

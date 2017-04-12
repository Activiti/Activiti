package org.activiti.engine.cfg.security;

import org.activiti.engine.impl.util.CommandExecutor;

public interface CommandExecutorFactory {

    CommandExecutor createExecutor(ExecutorContext executorContext);

}

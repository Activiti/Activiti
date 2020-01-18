package org.activiti.engine.test.profiler;

import org.activiti.engine.impl.interceptor.AbstractCommandInterceptor;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandConfig;

/**

 */
public class TotalExecutionTimeCommandInterceptor extends AbstractCommandInterceptor {

    protected ActivitiProfiler activitiProfiler;

    public TotalExecutionTimeCommandInterceptor() {
        this.activitiProfiler = ActivitiProfiler.getInstance();
    }

    public <T> T execute(CommandConfig config, Command<T> command) {
        ProfileSession currentProfileSession = activitiProfiler.getCurrentProfileSession();

        if (currentProfileSession != null) {

            String className = command.getClass().getName();
            CommandExecutionResult commandExecutionResult = new CommandExecutionResult();
            currentProfileSession.setCurrentCommandExecution(commandExecutionResult);

            commandExecutionResult.setCommandFqn(className);

            long start = System.currentTimeMillis();
            T result = next.execute(config, command);
            long end = System.currentTimeMillis();
            long totalTime = end - start;
            commandExecutionResult.setTotalTimeInMs(totalTime);

            currentProfileSession.addCommandExecution(className, commandExecutionResult);
            currentProfileSession.clearCurrentCommandExecution();

            return result;

        } else {
            return next.execute(config, command);
        }

    }

}

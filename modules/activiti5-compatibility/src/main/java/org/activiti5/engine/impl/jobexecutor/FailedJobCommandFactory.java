package org.activiti5.engine.impl.jobexecutor;

import org.activiti5.engine.impl.interceptor.Command;

public interface FailedJobCommandFactory {
	
	public Command<Object> getCommand(String jobId, Throwable exception);

}

package org.activiti.camel.util;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

/**
 * ServiceTask to start with.
 * 
 * @author stefan.schulze@accelsis.biz
 *
 */
public class DummyJavaDelegate implements JavaDelegate {
	
	@Override
	public void execute(DelegateExecution execution) throws Exception {
		// dummy
	}

}

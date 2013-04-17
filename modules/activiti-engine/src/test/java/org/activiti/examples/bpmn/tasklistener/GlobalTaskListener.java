package org.activiti.examples.bpmn.tasklistener;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

public class GlobalTaskListener implements TaskListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int initExecuteCount = 0 ; 

	@Override
	public void notify(DelegateTask delegateTask) {
		Object count = delegateTask.getVariable("executeCount") ;
		if( count != null ) {
			initExecuteCount = (Integer)count ;
			initExecuteCount ++ ;
		}
		delegateTask.setVariable("executeCount", initExecuteCount) ;
	}

}

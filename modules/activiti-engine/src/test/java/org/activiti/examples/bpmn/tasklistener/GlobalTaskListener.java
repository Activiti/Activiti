package org.activiti.examples.bpmn.tasklistener;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

public class GlobalTaskListener implements TaskListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void notify(DelegateTask delegateTask) {
		System.out.println( "this is global listener : " + delegateTask.getName() +" , "+ delegateTask.getEventName() );
	}

}

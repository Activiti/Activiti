package org.activiti.engine.test.jobexecutor;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

public class RetryFailingDelegate implements JavaDelegate {

  public static final String EXCEPTION_MESSAGE = "Expected exception.";
  
	public static boolean shallThrow = false;
	public static List<Long> times;
	 
	static public void resetTimeList() {
	  times = new ArrayList<Long>();
	}

	@Override
	public void execute(DelegateExecution execution) throws Exception {
	    	
	  times.add(System.currentTimeMillis());

	  if (shallThrow) {
	    throw new ActivitiException(EXCEPTION_MESSAGE);
	  }
	}
}
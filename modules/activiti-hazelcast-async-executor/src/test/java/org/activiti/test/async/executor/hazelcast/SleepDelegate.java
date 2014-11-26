package org.activiti.test.async.executor.hazelcast;


import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;

public class SleepDelegate implements JavaDelegate {
	
	public static AtomicInteger nrOfExecutions = new AtomicInteger(0);

  protected Expression sleepTime;

  public void execute(DelegateExecution execution) throws Exception {
  	long startTime = System.currentTimeMillis();
  	long var = 0;
  	while (System.currentTimeMillis() - startTime < 200) {
  		var += startTime; // Doing something to keep the JVM busy
  		Thread.sleep(50L); // Doing some sleeps to mimic I/O
  	}
  	nrOfExecutions.incrementAndGet();
//  	System.out.println(new Date() + "Job Done!");
  }

  public void setSleepTime(Expression sleepTime) {
    this.sleepTime = sleepTime;
  }
}

package org.activiti.camel.util;

/**
 * Simulates some real work, to delay the message on the Camel route.
 * 
 * @author stefan.schulze@accelsis.biz
 *
 */
public class TimeConsumingService {

	/**
	 * Spend some time.
	 * 
	 * @throws InterruptedException
	 */
	public void doWork() throws InterruptedException {
		Thread.sleep(100);
	}
	
}

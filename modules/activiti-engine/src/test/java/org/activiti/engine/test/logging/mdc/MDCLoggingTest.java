package org.activiti.engine.test.logging.mdc;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.test.Deployment;
import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class MDCLoggingTest extends PluggableActivitiTestCase {

	MemoryLogAppender console = new MemoryLogAppender();
	List<Appender> appenders = null;

	private void setCustomLogger() {
		String PATTERN = "Modified Log *** ProcessDefinitionId=%X{mdcProcessDefinitionID} executionId=%X{mdcExecutionId} mdcProcessInstanceID=%X{mdcProcessInstanceID} mdcBusinessKey=%X{mdcBusinessKey} mdcTaskId=%X{mdcTaskId}  %m%n";
		console.setLayout(new PatternLayout(PATTERN));
		console.setThreshold(Level.DEBUG);
		console.activateOptions();
		console.setName("MemoryAppender");

		appenders = new ArrayList<Appender>();
		Enumeration<?> appendersEnum = Logger.getRootLogger().getAllAppenders();

		while (appendersEnum.hasMoreElements()) {
			Appender object = (Appender) appendersEnum.nextElement();
			appenders.add(object);
		}

		removeAppenders();

		Logger.getRootLogger().addAppender(console);
	}

	private void removeAppenders() {
		Enumeration<?> appendersEnum = Logger.getRootLogger().getAllAppenders();
		while (appendersEnum.hasMoreElements()) {
			Appender object = (Appender) appendersEnum.nextElement();
			Logger.getRootLogger().removeAppender(object);
		}
	}

	private void restoreLoggers() {
		removeAppenders();

		for (Appender appender : appenders) {
			Logger.getRootLogger().addAppender(appender);
		}
	}

	@Deployment
	public void testLogger() {
		setCustomLogger();

		try {
			runtimeService.startProcessInstanceByKey("testLoggerProcess");
			fail("Expected exception");
		} catch (Exception e) {
			// expected exception
		}
		String messages = console.toString();
		
		assertTrue(messages.contains("ProcessDefinitionId="
				+ TestService.processDefinitionId));
		assertTrue(messages.contains("executionId=" + TestService.executionId));
		assertTrue(messages.contains("mdcProcessInstanceID="
				+ TestService.processInstanceId));
		assertTrue(messages.contains("mdcBusinessKey="
				+ (TestService.businessKey == null ? ""
						: TestService.businessKey)));
		console.clear();
		restoreLoggers();

		try {
			runtimeService.startProcessInstanceByKey("testLoggerProcess");
			fail("Expected exception");
		} catch (Exception e) {
			// expected exception
		}
		assertFalse(console.toString().contains(
				"ProcessDefinitionId=" + TestService.processDefinitionId));
	}
}

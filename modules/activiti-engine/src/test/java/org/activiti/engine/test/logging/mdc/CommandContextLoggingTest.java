package org.activiti.engine.test.logging.mdc;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.ActivitiOptimisticLockingException;
import org.activiti.engine.ActivitiTaskAlreadyClaimedException;
import org.activiti.engine.JobNotFoundException;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class CommandContextLoggingTest extends PluggableActivitiTestCase {

	MemoryLogAppender console = new MemoryLogAppender();
	List<Appender> appenders = null;

	private void setCustomLogger(Level logLevel) {
		console.setLayout(new PatternLayout());
		console.setThreshold(logLevel);
		console.activateOptions();
		console.setName("MemoryAppender");

		appenders = new ArrayList<Appender>();
		Enumeration<?> appendersEnum = Logger.getRootLogger().getAllAppenders();

		while (appendersEnum.hasMoreElements()) {
			Appender object = (Appender) appendersEnum.nextElement();
			appenders.add(object);
			Logger.getRootLogger().removeAppender(object);
		}

		Logger.getRootLogger().addAppender(console);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		
		for (Appender appender : appenders) {
			Logger.getRootLogger().addAppender(appender);
		}
	}
	
	@Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
	public void testCloseInfoLogging(){
		setCustomLogger(Level.INFO);

		causeJobNotFoundException();
		causeActivitiOptimisticLockingException();
		causeActivitiTaskAlreadyClaimedException();
		causeException();

		String messages = console.toString();
		assertTrue(messages.contains("JobNotFoundException"));
		assertTrue(messages.contains("ActivitiTaskAlreadyClaimedException"));
		assertTrue(!messages.contains("ActivitiOptimisticLockingException"));
		assertTrue(!messages.contains("ActivitiObjectNotFoundException"));

		console.clear();
	}

	@Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
	public void testCloseDebugLogging(){
		setCustomLogger(Level.DEBUG);

		causeJobNotFoundException();
		causeActivitiOptimisticLockingException();
		causeActivitiTaskAlreadyClaimedException();
		causeException();

		String messages = console.toString();
		assertTrue(messages.contains("JobNotFoundException"));
		assertTrue(messages.contains("ActivitiTaskAlreadyClaimedException"));
		assertTrue(messages.contains("ActivitiOptimisticLockingException"));
		assertTrue(messages.contains("ActivitiObjectNotFoundException"));

		console.clear();
	}

	@Deployment(resources = { "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml" })
	public void testCloseErrorLogging(){
		setCustomLogger(Level.ERROR);
		
		causeJobNotFoundException();
		causeActivitiOptimisticLockingException();
		causeActivitiTaskAlreadyClaimedException();
		causeException();
		
		String messages = console.toString();
		assertTrue(!messages.contains("JobNotFoundException"));
		assertTrue(!messages.contains("ActivitiTaskAlreadyClaimedException"));
		assertTrue(!messages.contains("ActivitiOptimisticLockingException"));
		assertTrue(!messages.contains("ActivitiObjectNotFoundException"));

		console.clear();
	}

	// JobNotFoundException -> Level.INFO
	private void causeJobNotFoundException() {
		try{
			managementService.executeJob("unexistingjob");
		} catch (JobNotFoundException jnfe) {
		}
	}
	// ActivitiTaskAlreadyClaimedException -> Level.INFO
	private void causeActivitiTaskAlreadyClaimedException() {
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
		Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
		
		User user = identityService.newUser("user");
		identityService.saveUser(user);
		User secondUser = identityService.newUser("seconduser");
		identityService.saveUser(secondUser);

		// Claim task the first time
		taskService.claim(task.getId(), user.getId());

		try {
			taskService.claim(task.getId(), secondUser.getId());
		} catch (ActivitiTaskAlreadyClaimedException ae) {
		}

		identityService.deleteUser(user.getId());
		identityService.deleteUser(secondUser.getId());
	}

	// ActivitiOptimisticLockingException -> Level.DEBUG
	private void causeActivitiOptimisticLockingException() {
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
		Task task1 = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
		Task task2 = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();

		task1.setDescription("test description one");
		taskService.saveTask(task1);

		try {
			task2.setDescription("test description two");
			taskService.saveTask(task2);
		} catch(ActivitiOptimisticLockingException e) {
		}
	}

	// Other Exception -> Level.DEBUG
	private void causeException() {
		try {
			runtimeService.getActiveActivityIds("not found");
		} catch(ActivitiObjectNotFoundException e){
		}
	}
}
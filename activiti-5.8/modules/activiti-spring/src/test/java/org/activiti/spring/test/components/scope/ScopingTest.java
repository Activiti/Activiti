package org.activiti.spring.test.components.scope;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.spring.test.components.ProcessInitiatingPojo;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * tests the scoped beans
 *
 * @author Josh Long
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:org/activiti/spring/test/components/ScopingTests-context.xml")
public class ScopingTest {

	@Autowired
	private ProcessInitiatingPojo processInitiatingPojo;

	private Logger logger = Logger.getLogger(getClass().getName());

	@Autowired
	private ProcessEngine processEngine;

	private RepositoryService repositoryService;
	private TaskService taskService;

	@Before
	public void before() throws Throwable {
	  this.repositoryService = this.processEngine.getRepositoryService();
		this.taskService = this.processEngine.getTaskService();
		
		repositoryService.createDeployment()
		  .addClasspathResource("org/activiti/spring/test/autodeployment/autodeploy.b.bpmn20.xml")
		  .addClasspathResource("org/activiti/spring/test/components/waiter.bpmn20.xml")
		  .addClasspathResource("org/activiti/spring/test/components/spring-component-waiter.bpmn20.xml")
		  .deploy();
	}
	
	@After
	public void after() {
	  for (Deployment deployment : repositoryService.createDeploymentQuery().list()) {
	    repositoryService.deleteDeployment(deployment.getId(), true);
	  }
	}
	
	static public long CUSTOMER_ID_PROC_VAR_VALUE = 343;

	static public String customerIdProcVarName = "customerId";

	/**
	 * this code instantiates a business process that in turn delegates to a few Spring beans that in turn inject a process scoped object, {@link StatefulObject}.
	 *
	 * @return the StatefulObject that was injected across different components, that all share the same state.
	 * @throws Throwable if anythign goes wrong
	 */
	private StatefulObject run() throws Throwable {
		logger.info("----------------------------------------------");
		Map<String, Object> vars = new HashMap<String, Object>();
		vars.put(customerIdProcVarName, CUSTOMER_ID_PROC_VAR_VALUE);
		ProcessInstance processInstance = processEngine.getRuntimeService().startProcessInstanceByKey("component-waiter", vars);
		StatefulObject scopedObject = (StatefulObject) processEngine.getRuntimeService().getVariable(processInstance.getId(), "scopedTarget.c1");
		Assert.assertNotNull("the scopedObject can't be null", scopedObject);
		Assert.assertTrue("the 'name' property can't be null.", StringUtils.hasText(scopedObject.getName()));
		Assert.assertEquals(scopedObject.getVisitedCount(), 2);

		// the process has paused
		String procId = processInstance.getProcessInstanceId();

		List<Task> tasks = taskService.createTaskQuery().executionId(procId).list();

		Assert.assertEquals("there should be 1 (one) task enqueued at this point.", tasks.size(), 1);

		Task t = tasks.iterator().next();

		this.taskService.claim(t.getId(), "me");

		logger.info("sleeping for 10 seconds while a user performs his task. " +
				"The first transaction has committed. A new one will start in 10 seconds");

		Thread.sleep(1000 * 5);

		this.taskService.complete(t.getId());

		scopedObject = (StatefulObject) processEngine.getRuntimeService().getVariable(processInstance.getId(), "scopedTarget.c1");
		Assert.assertEquals(scopedObject.getVisitedCount(), 3);

		Assert.assertEquals( "the customerId injected should " +
					"be what was given as a processVariable parameter." ,
				ScopingTest.CUSTOMER_ID_PROC_VAR_VALUE, scopedObject.getCustomerId()) ;
		return scopedObject;
	}

	@Test
	public void testUsingAnInjectedScopedProxy() throws Throwable {
		logger.info("Running 'component-waiter' process instance with scoped beans.");
		StatefulObject one = run();
		StatefulObject two = run();
		Assert.assertNotSame(one.getName(), two.getName());
		Assert.assertEquals(one.getVisitedCount(), two.getVisitedCount());
	}

	@Test
	public void testStartingAProcessWithScopedBeans() throws Throwable {
		this.processInitiatingPojo.startScopedProcess(3243);
	}
	

}

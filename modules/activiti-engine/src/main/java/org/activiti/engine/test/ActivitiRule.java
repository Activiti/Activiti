/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.test.TestHelper;
import org.activiti.engine.test.mock.ActivitiMockSupport;
import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;

/**
 * Convenience for ProcessEngine and services initialization in the form of a
 * JUnit rule.
 * 
 * <p>
 * Usage:
 * </p>
 * 
 * <pre>
 * public class YourTest {
 * 
 *   &#64;Rule
 *   public ActivitiRule activitiRule = new ActivitiRule();
 *   
 *   ...
 * }
 * </pre>
 * 
 * <p>
 * The ProcessEngine and the services will be made available to the test class
 * through the getters of the activitiRule. The processEngine will be
 * initialized by default with the activiti.cfg.xml resource on the classpath.
 * To specify a different configuration file, pass the resource location in
 * {@link #ActivitiRule(String) the appropriate constructor}. Process engines
 * will be cached statically. Right before the first time the setUp is called
 * for a given configuration resource, the process engine will be constructed.
 * </p>
 * 
 * <p>
 * You can declare a deployment with the {@link Deployment} annotation. This
 * base class will make sure that this deployment gets deployed before the setUp
 * and {@link RepositoryService#deleteDeployment(String, boolean) cascade
 * deleted} after the tearDown.
 * </p>
 * 
 * <p>
 * The activitiRule also lets you {@link ActivitiRule#setCurrentTime(Date) set
 * the current time used by the process engine}. This can be handy to control the
 * exact time that is used by the engine in order to verify e.g. e.g. due dates
 * of timers. Or start, end and duration times in the history service. In the
 * tearDown, the internal clock will automatically be reset to use the current
 * system time rather then the time that was set during a test method.
 * </p>
 * 
 * @author Tom Baeyens
 */
public class ActivitiRule implements TestRule {

	protected String configurationResource = "activiti.cfg.xml";
	protected String deploymentId = null;

  protected ProcessEngineConfiguration processEngineConfiguration;
	protected ProcessEngine processEngine;
	protected RepositoryService repositoryService;
	protected RuntimeService runtimeService;
	protected TaskService taskService;
	protected HistoryService historyService;
	protected IdentityService identityService;
	protected ManagementService managementService;
	protected FormService formService;

	protected ActivitiMockSupport mockSupport;

  public ActivitiRule() {
	}

	public ActivitiRule(String configurationResource) {
		this.configurationResource = configurationResource;
	}

	public ActivitiRule(ProcessEngine processEngine) {
	  setProcessEngine(processEngine);
	}

	/**
	 * Implementation based on {@link TestWatcher}.
	 */
	@Override
	public Statement apply(final Statement base, final Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				List<Throwable> errors = new ArrayList<Throwable>();

				startingQuietly(description, errors);
				try {
					base.evaluate();
					succeededQuietly(description, errors);
				} catch (AssumptionViolatedException e) {
					errors.add(e);
					skippedQuietly(e, description, errors);
				} catch (Throwable t) {
					errors.add(t);
					failedQuietly(t, description, errors);
				} finally {
					finishedQuietly(description, errors);
				}

				MultipleFailureException.assertEmpty(errors);
			}
		};
	}

	private void succeededQuietly(Description description, List<Throwable> errors) {
		try {
			succeeded(description);
		} catch (Throwable t) {
			errors.add(t);
		}
	}

	private void failedQuietly(Throwable t, Description description,
	    List<Throwable> errors) {
		try {
			failed(t, description);
		} catch (Throwable t1) {
			errors.add(t1);
		}
	}

	private void skippedQuietly(AssumptionViolatedException e,
	    Description description, List<Throwable> errors) {
		try {
			skipped(e, description);
		} catch (Throwable t) {
			errors.add(t);
		}
	}

	private void startingQuietly(Description description, List<Throwable> errors) {
		try {
			starting(description);
		} catch (Throwable t) {
			errors.add(t);
		}
	}

	private void finishedQuietly(Description description, List<Throwable> errors) {
		try {
			finished(description);
		} catch (Throwable t) {
			errors.add(t);
		}
	}

	/**
	 * Invoked when a test succeeds
	 */
	protected void succeeded(Description description) {
	}

	/**
	 * Invoked when a test fails
	 */
	protected void failed(Throwable e, Description description) {
	}

	/**
	 * Invoked when a test is skipped due to a failed assumption.
	 */
	protected void skipped(AssumptionViolatedException e, Description description) {
	}

	protected void starting(Description description) {
		if (processEngine == null) {
			initializeProcessEngine();
    }
		
		if (processEngineConfiguration == null) {
		  initializeServices();
		}

		if (mockSupport == null) {
			initializeMockSupport();
		}
		
		// Allow for mock configuration
		configureProcessEngine();
		
		// Allow for annotations
		try {
	    TestHelper.annotationMockSupportSetup(Class.forName(description.getClassName()), description.getMethodName(), mockSupport);
    } catch (ClassNotFoundException e) {
    	throw new ActivitiException("Programmatic error: could not instantiate "
			    + description.getClassName(), e);
    }

		try {
			deploymentId = TestHelper.annotationDeploymentSetUp(processEngine,
			    Class.forName(description.getClassName()), description.getMethodName());
		} catch (ClassNotFoundException e) {
			throw new ActivitiException("Programmatic error: could not instantiate "
			    + description.getClassName(), e);
		}
	}

	protected void initializeProcessEngine() {
		processEngine = TestHelper.getProcessEngine(configurationResource);
	}

	protected void initializeServices() {
    processEngineConfiguration = processEngine.getProcessEngineConfiguration();
		repositoryService = processEngine.getRepositoryService();
		runtimeService = processEngine.getRuntimeService();
		taskService = processEngine.getTaskService();
		historyService = processEngine.getHistoryService();
		identityService = processEngine.getIdentityService();
		managementService = processEngine.getManagementService();
		formService = processEngine.getFormService();
	}

	protected void initializeMockSupport() {
		if (ActivitiMockSupport.isMockSupportPossible(processEngine)) {
			this.mockSupport = new ActivitiMockSupport(processEngine);
		}
	}
	
	protected void configureProcessEngine() {
		/** meant to be overridden */
	}

	protected void finished(Description description) {

		// Remove the test deployment
		try {
			TestHelper.annotationDeploymentTearDown(processEngine, deploymentId,
			    Class.forName(description.getClassName()), description.getMethodName());
		} catch (ClassNotFoundException e) {
			throw new ActivitiException("Programmatic error: could not instantiate "
			    + description.getClassName(), e);
		}

		// Reset internal clock
		processEngineConfiguration.getClock().reset();

		// Rest mocks
		if (mockSupport != null) {
			TestHelper.annotationMockSupportTeardown(mockSupport);
		}
	}

	public void setCurrentTime(Date currentTime) {
		processEngineConfiguration.getClock().setCurrentTime(currentTime);
	}

	public String getConfigurationResource() {
		return configurationResource;
	}

	public void setConfigurationResource(String configurationResource) {
		this.configurationResource = configurationResource;
	}

	public ProcessEngine getProcessEngine() {
		return processEngine;
	}

	public void setProcessEngine(ProcessEngine processEngine) {
		this.processEngine = processEngine;
		initializeServices();
	}

	public RepositoryService getRepositoryService() {
		return repositoryService;
	}

	public void setRepositoryService(RepositoryService repositoryService) {
		this.repositoryService = repositoryService;
	}

	public RuntimeService getRuntimeService() {
		return runtimeService;
	}

	public void setRuntimeService(RuntimeService runtimeService) {
		this.runtimeService = runtimeService;
	}

	public TaskService getTaskService() {
		return taskService;
	}

	public void setTaskService(TaskService taskService) {
		this.taskService = taskService;
	}

	public HistoryService getHistoryService() {
		return historyService;
	}

	public void setHistoricDataService(HistoryService historicDataService) {
		this.historyService = historicDataService;
	}

	public IdentityService getIdentityService() {
		return identityService;
	}

	public void setIdentityService(IdentityService identityService) {
		this.identityService = identityService;
	}

	public ManagementService getManagementService() {
		return managementService;
	}

	public FormService getFormService() {
		return formService;
	}

	public void setManagementService(ManagementService managementService) {
		this.managementService = managementService;
	}

  public void setProcessEngineConfiguration(ProcessEngineConfigurationImpl processEngineConfiguration) {
    this.processEngineConfiguration = processEngineConfiguration;
  }

  public ActivitiMockSupport getMockSupport() {
		return mockSupport;
	}

	public ActivitiMockSupport mockSupport() {
		return mockSupport;
	}

}

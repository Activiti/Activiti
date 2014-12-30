package org.activiti6;

import java.sql.SQLException;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.ProcessEngineLifecycleListener;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.test.ActivitiRule;
import org.h2.tools.Server;
import org.junit.Before;
import org.junit.Rule;


/**
 * Parent class for internal Activiti tests.
 * 
 * Boots up a process engine and caches it. 
 * 
 * When using H2 and the default schema name, it will also boot the H2 webapp 
 * (reachable with browser on http://localhost:8082/)
 * 
 * @author Joram Barrez
 */
public class AbstractActvitiTest {
	
	public static String H2_TEST_JDBC_URL = "jdbc:h2:mem:activiti;DB_CLOSE_DELAY=1000";
	
	@Rule
	public ActivitiRule activitiRule = new ActivitiRule();
	
	protected ProcessEngine cachedProcessEngine;
	protected RepositoryService repositoryService; 
	protected RuntimeService runtimeService;
	protected TaskService taskService;
	protected FormService formService;
	protected HistoryService historyService;
	protected ManagementService managementService;
	
	@Before
	public void initProcessEngine() {
		if (cachedProcessEngine == null) {
			this.cachedProcessEngine = activitiRule.getProcessEngine();
			this.repositoryService = cachedProcessEngine.getRepositoryService();
			this.runtimeService = cachedProcessEngine.getRuntimeService();
			this.taskService = cachedProcessEngine.getTaskService();
			this.formService = cachedProcessEngine.getFormService();
			this.historyService = cachedProcessEngine.getHistoryService();
			this.managementService = cachedProcessEngine.getManagementService();
	
			if (cachedProcessEngine instanceof ProcessEngineImpl) {
				if (((ProcessEngineImpl) cachedProcessEngine).getProcessEngineConfiguration().getJdbcUrl().equals(H2_TEST_JDBC_URL)) {
					initializeH2WebApp(cachedProcessEngine);
				}
			}
		}
	}
	
	protected void initializeH2WebApp(ProcessEngine processEngine) {
		try {
			final Server server = Server.createWebServer("-web");

			// Shutdown hook
			final ProcessEngineConfiguration processEngineConfiguration = 
					((ProcessEngineImpl) processEngine).getProcessEngineConfiguration();
			final ProcessEngineLifecycleListener originalLifecycleListener = 
					processEngineConfiguration.getProcessEngineLifecycleListener();
			processEngineConfiguration.setProcessEngineLifecycleListener(new ProcessEngineLifecycleListener() {

				        @Override
				        public void onProcessEngineClosed(ProcessEngine processEngine) {
					        server.stop();
					        originalLifecycleListener.onProcessEngineClosed(processEngine);
				        }

				        @Override
				        public void onProcessEngineBuilt(ProcessEngine processEngine) {
					        originalLifecycleListener.onProcessEngineBuilt(processEngine);
				        }

			        });

			// Actually start the web server
			server.start();

		} catch (SQLException e) {
			throw new ActivitiException("Could not start H2 web app", e);
		}
	}

}

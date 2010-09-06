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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.AssertionFailedError;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineBuilder;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.bpmn.deployer.BpmnDeployer;
import org.activiti.engine.impl.cfg.ProcessEngineConfiguration;
import org.activiti.engine.impl.jobexecutor.JobExecutor;
import org.activiti.engine.impl.util.ClockUtil;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.pvm.impl.util.ClassNameUtil;
import org.activiti.pvm.impl.util.LogUtil.ThreadLogMode;
import org.activiti.pvm.test.PvmTestCase;
import org.junit.Assert;


/** JUnit 3 style base class that only exposes the public API services. 
 * 
 * @author Tom Baeyens
 */
public class ProcessEngineTestCase extends PvmTestCase {

  private static Logger log = Logger.getLogger(ProcessEngineTestCase.class.getName());

  private static final List<String> TABLENAMES_EXCLUDED_FROM_DB_CLEAN_CHECK = Arrays.asList(
    "ACT_GE_PROPERTY"
  );

  static final String DEFAULT_CONFIGURATION_RESOURCE = "activiti.properties";
  static Map<String, ProcessEngine> processEngines = new HashMap<String, ProcessEngine>(); 
  
  protected ThreadLogMode threadRenderingMode = DEFAULT_THREAD_LOG_MODE;
  protected String configurationResource = DEFAULT_CONFIGURATION_RESOURCE;
  protected List<String> deploymentsToDeleteAfterTestMethod = new ArrayList<String>();
  protected Throwable exception;

  protected ProcessEngineConfiguration processEngineConfiguration;
  protected ProcessEngine processEngine;
  protected RepositoryService repositoryService;
  protected RuntimeService runtimeService;
  protected TaskService taskService;
  protected HistoryService historicDataService;
  protected IdentityService identityService;
  protected ManagementService managementService;

  public ProcessEngineTestCase() {
  }
  
  public ProcessEngineTestCase(String configurationResource) {
    this.configurationResource = configurationResource;
  }
  
  public ProcessEngineTestCase(String configurationResource, ThreadLogMode threadRenderingMode) {
    super(threadRenderingMode);
    this.configurationResource = configurationResource;
    this.isEmptyLinesEnabled = false;
  }
  
  public void assertProcessEnded(final String processInstanceId) {
    ProcessInstance processInstance = processEngine
      .getRuntimeService()
      .createProcessInstanceQuery()
      .processInstanceId(processInstanceId)
      .singleResult();
    
    if (processInstance!=null) {
      throw new AssertionFailedError("expected finished process instance '"+processInstanceId+"' but it was still in the db"); 
    }
  }

  
  @Override
  public void runBare() throws Throwable {
    if (processEngine==null) {
      processEngine = processEngines.get(configurationResource);
      if (processEngine==null) {
        initializeProcessEngine();
        processEngines.put(configurationResource, processEngine);
      }
      initializeServices();
    }

    log.severe(EMPTY_LINE);

    try {
      
      annotationDeploymentBefore();
      
      super.runBare();

    }  catch (AssertionFailedError e) {
      log.severe(EMPTY_LINE);
      log.log(Level.SEVERE, "ASSERTION FAILED: "+e, e);
      exception = e;
      throw e;
      
    } catch (Throwable e) {
      log.severe(EMPTY_LINE);
      log.log(Level.SEVERE, "EXCEPTION: "+e, e);
      exception = e;
      throw e;
      
    } finally {
      annotationDeploymentAfter();
      assertAndEnsureCleanDb();
      ClockUtil.reset();
    }
  }

  protected void initializeProcessEngine() {
    log.fine("==== BUILDING PROCESS ENGINE ========================================================================");
    processEngine = new ProcessEngineBuilder()
      .configureFromPropertiesResource(configurationResource)
      .buildProcessEngine();
    log.fine("==== PROCESS ENGINE CREATED =========================================================================");
  }
  
  /** Each test is assumed to clean up all DB content it entered.
   * After a test method executed, this method scans all tables to see if the DB is completely clean. 
   * It throws AssertionFailed in case the DB is not clean.
   * If the DB is not clean, it is cleaned by performing a create a drop. */
  protected void assertAndEnsureCleanDb() throws Throwable {
    log.fine("verifying that db is clean after test");
    Map<String, Long> tableCounts = processEngine.getManagementService().getTableCount();
    StringBuilder outputMessage = new StringBuilder();
    for (String tableName : tableCounts.keySet()) {
      if (!TABLENAMES_EXCLUDED_FROM_DB_CLEAN_CHECK.contains(tableName)) {
        Long count = tableCounts.get(tableName);
        if (count!=0L) {
          outputMessage.append("  "+tableName + ": " + count + " record(s) ");
        }
      }
    }
    if (outputMessage.length() > 0) {
      outputMessage.insert(0, "DB NOT CLEAN: \n");
      log.severe(EMPTY_LINE);
      log.severe(outputMessage.toString());
      
      log.info("dropping and recreating db");
      
      processEngine.close();
      processEngine = null;
      processEngines.remove(configurationResource);

      if (exception!=null) {
        throw exception;
      } else {
        Assert.fail(outputMessage.toString());
      }
    }
  }

  private void annotationDeploymentBefore() {
    Method method = null;
    try {
      method = getClass().getDeclaredMethod(getName(), (Class<?>[])null);
    } catch (Exception e) {
      throw new ActivitiException("can't get method by reflection", e);
    }
    Deployment deploymentAnnotation = method.getAnnotation(Deployment.class);
    if (deploymentAnnotation != null) {
      log.fine("annotation @Deployment creates deployment for "+ClassNameUtil.getClassNameWithoutPackage(this)+"."+getName());
      String[] resources = deploymentAnnotation.resources();
      if (resources.length == 0) {
        String name = method.getName();
        String resource = getBpmnProcessDefinitionResource(getClass(), name);
        resources = new String[]{resource};
      }
      
      DeploymentBuilder deploymentBuilder = repositoryService
        .createDeployment()
        .name(ClassNameUtil.getClassNameWithoutPackage(this)+"."+getName());
      
      for (String resource: resources) {
        deploymentBuilder.addClasspathResource(resource);
      }
      
      String deploymentId = deploymentBuilder.deploy().getId();
      deploymentsToDeleteAfterTestMethod.add(deploymentId);
    }
  }
  
  /**
   * get a resource location by convention based on a class (type) and a
   * relative resource name. The return value will be the full classpath
   * location of the type, plus a suffix built from the name parameter:
   * <code>.&lt;name&gt;.bpmn20.xml</code>.
   */
  public static String getBpmnProcessDefinitionResource(Class< ? > type, String name) {
    return type.getName().replace('.', '/') + "." + name + "." + BpmnDeployer.BPMN_RESOURCE_SUFFIX;
  }

  private void annotationDeploymentAfter() {
    for (String deploymentId: deploymentsToDeleteAfterTestMethod) {
      log.fine("annotation @Deployment deletes deployment for "+ClassNameUtil.getClassNameWithoutPackage(this)+"."+getName());
      repositoryService.deleteDeploymentCascade(deploymentId);
    }
  }


  private void initializeServices() {
    processEngineConfiguration = ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration();
    repositoryService = processEngine.getRepositoryService();
    runtimeService = processEngine.getRuntimeService();
    taskService = processEngine.getTaskService();
    historicDataService = processEngine.getHistoryService();
    identityService = processEngine.getIdentityService();
    managementService = processEngine.getManagementService();
  }
  
  public void waitForJobExecutorToProcessAllJobs(long maxMillisToWait, long intervalMillis) {
    JobExecutor jobExecutor = ((ProcessEngineImpl)processEngine).getJobExecutor();
    jobExecutor.start();

    try {
      Timer timer = new Timer();
      InteruptTask task = new InteruptTask(Thread.currentThread());
      timer.schedule(task, maxMillisToWait);
      boolean areJobsAvailable = true;
      try {
        while (areJobsAvailable && !task.isTimeLimitExceeded()) {
          Thread.sleep(intervalMillis);
          areJobsAvailable = areJobsAvailable();
        }
      } catch (InterruptedException e) {
      } finally {
        timer.cancel();
      }
      if (areJobsAvailable) {
        throw new ActivitiException("time limit of " + maxMillisToWait + " was exceeded");
      }

    } finally {
      jobExecutor.shutdown();
    }
  }

  public boolean areJobsAvailable() {
    return !managementService
      .createJobQuery()
      .executable()
      .list()
      .isEmpty();
  }

  private static class InteruptTask extends TimerTask {
    protected boolean timeLimitExceeded = false;
    protected Thread thread;
    public InteruptTask(Thread thread) {
      this.thread = thread;
    }
    public boolean isTimeLimitExceeded() {
      return timeLimitExceeded;
    }
    public void run() {
      timeLimitExceeded = true;
      thread.interrupt();
    }
  }

  public static void closeProcessEngines() {
    for (ProcessEngine processEngine: processEngines.values()) {
      processEngine.close();
    }
    processEngines.clear();
  }
}

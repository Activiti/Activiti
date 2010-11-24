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

package org.activiti.engine.impl.test;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.AssertionFailedError;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.jobexecutor.JobExecutor;
import org.activiti.engine.impl.util.ClockUtil;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.engine.impl.util.ReflectUtil;
import org.activiti.engine.impl.util.LogUtil.ThreadLogMode;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.Assert;


/** Base class for the activiti test cases.
 * 
 * The main reason not to use our own test support classes is that we need to 
 * run our test suite with various configurations, e.g. with and without spring,
 * standalone or on a server etc.  Those requirements create some complications 
 * so we think it's best to use a separate base class.  That way it is much easier 
 * for us to maintain our own codebase and at the same time provide stability 
 * on the test support classes that we offer as part of our api (in org.activiti.engine.test).
 * 
 * @author Tom Baeyens
 */
public class ActivitiInternalTestCase extends PvmTestCase {

  private static Logger log = Logger.getLogger(ActivitiInternalTestCase.class.getName());
  
  private static final ProcessEngineInitializer processEngineInitializer = getProcessEngineInitializer();

  private static final List<String> TABLENAMES_EXCLUDED_FROM_DB_CLEAN_CHECK = Arrays.asList(
    "ACT_GE_PROPERTY"
  );

  protected static ProcessEngine processEngine; 
  
  protected ThreadLogMode threadRenderingMode = DEFAULT_THREAD_LOG_MODE;
  protected String deploymentId;
  protected Throwable exception;

  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected RepositoryService repositoryService;
  protected RuntimeService runtimeService;
  protected TaskService taskService;
  protected FormService formService;
  protected HistoryService historyService;
  protected IdentityService identityService;
  protected ManagementService managementService;
  
  private static ProcessEngineInitializer getProcessEngineInitializer() {
    String processEngineInitializerClassName = null;
    InputStream initializersInputStream = ReflectUtil.getResourceAsStream("activiti.initializer.properties");
    if (initializersInputStream!=null) {
      Properties properties = new Properties();
      try {
        properties.load(initializersInputStream);
        processEngineInitializerClassName = properties.getProperty("process.engine.initializer");
        if (processEngineInitializerClassName!=null) {
          return (ProcessEngineInitializer) ReflectUtil.instantiate(processEngineInitializerClassName);
        }
    
      } catch (Exception e) {
        throw new RuntimeException("couldn't instantiate process engine initializer "+properties+": "+e, e);
      } finally {
        IoUtil.closeSilently(initializersInputStream);
      }
    }
    return new DefaultProcessEngineInitializer();
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
      initializeProcessEngine();
    }
    if (repositoryService==null) {
      initializeServices();
    }

    log.severe(EMPTY_LINE);

    try {
      
      deploymentId = TestHelper.annotationDeploymentSetUp(processEngine, getClass(), getName());
      
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
      TestHelper.annotationDeploymentTearDown(processEngine, deploymentId, getClass(), getName());
      assertAndEnsureCleanDb();
      ClockUtil.reset();
    }
  }

  protected void initializeProcessEngine() {
    processEngine = processEngineInitializer.getProcessEngine();
  }
  
  /** Each test is assumed to clean up all DB content it entered.
   * After a test method executed, this method scans all tables to see if the DB is completely clean. 
   * It throws AssertionFailed in case the DB is not clean.
   * If the DB is not clean, it is cleaned by performing a create a drop. */
  protected void assertAndEnsureCleanDb() throws Throwable {
    log.fine("verifying that db is clean after test");
    Map<String, Long> tableCounts = managementService.getTableCount();
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

      if (exception!=null) {
        throw exception;
      } else {
        Assert.fail(outputMessage.toString());
      }
    } else {
      log.info("database was clean");
    }
  }


  private void initializeServices() {
    processEngineConfiguration = ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration();
    repositoryService = processEngine.getRepositoryService();
    runtimeService = processEngine.getRuntimeService();
    taskService = processEngine.getTaskService();
    formService = processEngine.getFormService();
    historyService = processEngine.getHistoryService();
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

  public static void closeProcessEngine() {
    if (processEngine!=null) {
      processEngine.close();
      processEngine = null;
    }
  }
}

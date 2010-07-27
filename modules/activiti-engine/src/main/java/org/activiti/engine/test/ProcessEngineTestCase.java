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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.activiti.DbProcessEngineBuilder;
import org.activiti.HistoricDataService;
import org.activiti.IdentityService;
import org.activiti.ManagementService;
import org.activiti.ProcessEngine;
import org.activiti.ProcessService;
import org.activiti.TaskService;
import org.activiti.impl.time.Clock;
import org.activiti.impl.util.LogUtil;
import org.activiti.impl.util.LogUtil.ThreadLogMode;


/** JUnit 3 style base class that only exposes the public API services. 
 * 
 * @author Tom Baeyens
 */
public class ProcessEngineTestCase extends TestCase {

  private static final String EMPTY_LINE = "                                                                                           ";

  static {
    LogUtil.readJavaUtilLoggingConfigFromClasspath();
  }
  
  private static Logger log = Logger.getLogger(ProcessEngineTestCase.class.getName());

  private static final ThreadLogMode DEFAULT_THREAD_LOG_MODE = ThreadLogMode.INDENT;
  private static final String DEFAULT_CONFIGURATION_RESOURCE = "activiti.properties";
  private static Map<String, ProcessEngine> processEngines = new HashMap<String, ProcessEngine>(); 
  
  protected ThreadLogMode threadRenderingMode;
  protected String configurationResource;
  protected ProcessEngine processEngine;
  protected ProcessService processService;
  protected TaskService taskService;
  protected HistoricDataService historicDataService;
  protected IdentityService identityService;
  protected ManagementService managementService;
  
  public ProcessEngineTestCase() {
    this(DEFAULT_CONFIGURATION_RESOURCE, DEFAULT_THREAD_LOG_MODE);
  }
  
  public ProcessEngineTestCase(String configurationResource) {
    this(configurationResource, DEFAULT_THREAD_LOG_MODE);
  }
  
  public ProcessEngineTestCase(ThreadLogMode threadRenderingMode) {
    this(DEFAULT_CONFIGURATION_RESOURCE, threadRenderingMode);
  }
  
  public ProcessEngineTestCase(String configurationResource, ThreadLogMode threadRenderingMode) {
    this.configurationResource = configurationResource;
    this.threadRenderingMode = threadRenderingMode;
  }
  
  @Override
  protected void runTest() throws Throwable {
    LogUtil.resetThreadIndents();
    ThreadLogMode oldThreadRenderingMode = LogUtil.setThreadLogMode(threadRenderingMode);
    
    if (processEngine==null) {
      processEngine = processEngines.get(configurationResource);
      if (processEngine==null) {
        log.fine("==== BUILDING PROCESS ENGINE ========================================================================");
        processEngine = new DbProcessEngineBuilder()
          .configureFromPropertiesResource(configurationResource)
          .buildProcessEngine();
        log.fine("==== PROCESS ENGINE CREATED =========================================================================");
      }
      initializeServices();
    }

    log.fine(EMPTY_LINE);
    log.fine("#### START "+getClass().getName()+"."+getName()+" ###########################################################");

    try {
      
      super.runTest();

    }  catch (AssertionFailedError e) {
      log.severe(EMPTY_LINE);
      log.log(Level.SEVERE, "ASSERTION FAILED: "+e, e);
      throw e;
    } catch (Throwable e) {
      log.severe(EMPTY_LINE);
      log.log(Level.SEVERE, "EXCEPTION: "+e, e);
      throw e;
    } finally {
      Clock.reset();
      log.fine("#### END "+getClass().getName()+"."+getName()+" #############################################################");
      LogUtil.setThreadLogMode(oldThreadRenderingMode);
    }
  }

  void initializeServices() {
    processService = processEngine.getProcessService();
    taskService = processEngine.getTaskService();
    historicDataService = processEngine.getHistoricDataService();
    identityService = processEngine.getIdentityService();
    managementService = processEngine.getManagementService();
  }
}

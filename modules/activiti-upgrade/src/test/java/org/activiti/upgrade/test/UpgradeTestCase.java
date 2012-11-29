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
package org.activiti.upgrade.test;

import java.util.logging.Logger;

import junit.framework.TestCase;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.util.LogUtil;
import org.activiti.upgrade.UpgradeUtil;
import org.junit.Ignore;


@Ignore
/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public abstract class UpgradeTestCase extends TestCase {
  
  static {
    LogUtil.readJavaUtilLoggingConfigFromClasspath();
  }
  
  static Logger log = Logger.getLogger(UpgradeTestCase.class.getName());

  protected static ProcessEngine processEngine; 
  protected static RuntimeService runtimeService;
  protected static TaskService taskService;
  protected static HistoryService historyService;
  protected static ManagementService managementService;

//  protected static void runBeforeAndAfterInDevelopmentMode(UpgradeTestCase upgradeTest) {
//    setProcessEngine(ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration().setDatabaseSchemaUpdate("true").buildProcessEngine());
//
//    upgradeTest.runInTheOldVersion();
//    
//    // 'Reboot' the process engine, keep the data
//    processEngine.close();
//    setProcessEngine(ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration().setDatabaseSchemaUpdate("true").buildProcessEngine());
//    
//    Result result = JUnitCore.runClasses(upgradeTest.getClass());
//    System.err.println();
//    System.err.println("Tests run: "+result.getRunCount());
//    System.err.println("Failures : "+result.getFailureCount());
//    System.err.println();
//    for (Failure failure: result.getFailures()) {
//      System.err.println(failure.getDescription());
//      Throwable e = failure.getException();
//      if (e!=null) {
//        e.printStackTrace();
//      }
//    }
//  }
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    
    if (processEngine==null) {
      String database = System.getProperty("database");
      log.fine("Configuration properties...");
      log.fine("database.....:"+database);
      setProcessEngine(UpgradeUtil.createProcessEngineConfiguration(database).buildProcessEngine());
    }
  }


  public static void setProcessEngine(ProcessEngine processEngine) {
    UpgradeTestCase.processEngine = processEngine;
    runtimeService = processEngine.getRuntimeService();
    taskService = processEngine.getTaskService();
    historyService = processEngine.getHistoryService();
    managementService = processEngine.getManagementService();
  }

}

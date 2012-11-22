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
package org.activiti.upgrade;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import junit.framework.TestCase;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.util.LogUtil;
import org.junit.Ignore;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;


@Ignore
public abstract class UpgradeTestCase extends TestCase {
  
  static {
    LogUtil.readJavaUtilLoggingConfigFromClasspath();
  }

  protected static ProcessEngine processEngine; 
  protected static RuntimeService runtimeService;
  protected static TaskService taskService;
  protected static HistoryService historyService;
  protected static ManagementService managementService;

  protected static void runBeforeAndAfterInDevelopmentMode(UpgradeTestCase upgradeTest) {
    setProcessEngine(ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration().buildProcessEngine());

    upgradeTest.runInTheOldVersion();
    Result result = JUnitCore.runClasses(upgradeTest.getClass());
    System.err.println();
    System.err.println("Tests run: "+result.getRunCount());
    System.err.println("Failures : "+result.getFailureCount());
    System.err.println();
    for (Failure failure: result.getFailures()) {
      System.err.println(failure.getDescription());
      Throwable e = failure.getException();
      if (e!=null) {
        e.printStackTrace();
      }
    }
  }
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    
    if (processEngine==null) {
      String database = System.getProperty("database");
      UpgradeDataGenerator.log.fine("Configuration properties...");
      UpgradeDataGenerator.log.fine("database.....:"+database);
      setProcessEngine(createProcessEngineConfiguration(database).buildProcessEngine());
    }
  }


  public static void setProcessEngine(ProcessEngine processEngine) {
    UpgradeTestCase.processEngine = processEngine;
    runtimeService = processEngine.getRuntimeService();
    taskService = processEngine.getTaskService();
    historyService = processEngine.getHistoryService();
    managementService = processEngine.getManagementService();
  }

  public abstract void runInTheOldVersion();

  public static ProcessEngineConfigurationImpl createProcessEngineConfiguration(String database) throws Exception {
    ProcessEngineConfigurationImpl processEngineConfiguration;
    processEngineConfiguration = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration
            .createStandaloneProcessEngineConfiguration()
            .setDatabaseSchemaUpdate("true")
            .setHistory("full")
            .setJobExecutorActivate(false);
  
    // loading properties
    String propertiesFileName = System.getProperty("user.home")+System.getProperty("file.separator")+".activiti"+System.getProperty("file.separator")+"upgrade"+System.getProperty("file.separator")+"build."+database+".properties";
    Properties properties = new Properties();
    properties.load(new FileInputStream(propertiesFileName));
  
    // configure the jdbc parameters in the process engine configuration
    processEngineConfiguration.setJdbcDriver(properties.getProperty("jdbc.driver"));
    processEngineConfiguration.setJdbcUrl(properties.getProperty("jdbc.url"));
    processEngineConfiguration.setJdbcUsername(properties.getProperty("jdbc.username"));
    processEngineConfiguration.setJdbcPassword(properties.getProperty("jdbc.password"));

    UpgradeDataGenerator.log.fine("jdbc url.....: "+processEngineConfiguration.getJdbcUrl());
    UpgradeDataGenerator.log.fine("jdbc username: "+processEngineConfiguration.getJdbcUsername());

    return processEngineConfiguration;
  }
}

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
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.util.ClassNameUtil;
import org.activiti.engine.impl.util.LogUtil;
import org.activiti.upgrade.test.UpgradeTaskOneTest;
import org.activiti.upgrade.test.UpgradeTaskTwoTest;

/**
 * @author Tom Baeyens
 */
public class UpgradeDataGenerator {
  
  private static Logger log = Logger.getLogger(UpgradeTestCase.class.getName());
  
  static UpgradeTestCase[] upgradeTestCases = new UpgradeTestCase[]{
    new UpgradeTaskOneTest(),
    new UpgradeTaskTwoTest()
  };

  public static void main(String[] args) {
    try {

      LogUtil.readJavaUtilLoggingConfigFromClasspath();
      
      if (args==null || args.length!=1) {
        throw new RuntimeException("exactly argument expected: database");
      }
      
      String database = args[0];
      log.fine("database: "+database);
  
      ProcessEngineConfiguration processEngineConfiguration = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration
              .createStandaloneProcessEngineConfiguration()
              .setHistory("full")
              .setJobExecutorActivate(false);

      // loading properties
      log.fine("loading properties...");
      String propertiesFileName = System.getProperty("user.home")+System.getProperty("file.separator")+".activiti"+System.getProperty("file.separator")+"jdbc"+System.getProperty("file.separator")+"build."+database+".properties";
      Properties properties = new Properties();
      properties.load(new FileInputStream(propertiesFileName));
      log.fine("jdbc url.....: "+processEngineConfiguration.getJdbcUrl());
      log.fine("jdbc username: "+processEngineConfiguration.getJdbcUsername());
      
      // install the jdbc proxy driver
      log.fine("installing jdbc proxy driver...");
      ProxyDriver.setUrl(properties.getProperty("jdbc.url"));
      DriverManager.registerDriver(new ProxyDriver());

      // configure the jdbc parameters in the process engine configuration
      processEngineConfiguration.setJdbcDriver(properties.getProperty("jdbc.driver"));
      processEngineConfiguration.setJdbcUrl("proxy");
      processEngineConfiguration.setJdbcUsername(properties.getProperty("jdbc.username"));
      processEngineConfiguration.setJdbcPassword(properties.getProperty("jdbc.password"));

      log.fine("building the process engine...");
      ProcessEngine processEngine = processEngineConfiguration.buildProcessEngine();
      
      log.fine("deploy processes and start process instances");
      UpgradeTestCase.setProcessEngine(processEngine);
      
      for (UpgradeTestCase upgradeTestCase: upgradeTestCases) {
        log.fine("### Running test "+ClassNameUtil.getClassNameWithoutPackage(upgradeTestCase.getClass())+" in the old version");
        upgradeTestCase.runInTheOldVersion();
      }

      log.fine("### Captured SQL");

      System.err.println();
      System.err.println();
      for (String statement: ProxyDriver.statements) {
        System.err.println(statement);
        System.err.println();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

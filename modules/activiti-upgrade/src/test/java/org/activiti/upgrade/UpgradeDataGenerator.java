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

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.DriverManager;
import java.util.logging.Logger;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.util.ClassNameUtil;
import org.activiti.engine.impl.util.LogUtil;
import org.activiti.upgrade.test.UpgradeTaskOneTest;
import org.activiti.upgrade.test.UpgradeTaskTwoTest;

/**
 * @author Tom Baeyens
 */
public class UpgradeDataGenerator {
  
  static Logger log = Logger.getLogger(UpgradeTestCase.class.getName());
  
  static UpgradeTestCase[] upgradeTestCases = new UpgradeTestCase[]{
    new UpgradeTaskOneTest(),
    new UpgradeTaskTwoTest()
  };

  public static void main(String[] args) {
    
    ProcessEngineConfigurationImpl processEngineConfiguration = null;
    
    try {

      LogUtil.readJavaUtilLoggingConfigFromClasspath();
      
      if (args==null || args.length!=2) {
        throw new RuntimeException("exactly 2 arguments expected: database and releaseVersion");
      }
      
      String database = args[0];
      String releaseVersion = args[1];
      log.fine("database: "+database);
      log.fine("releaseVersion: "+releaseVersion);
  
      processEngineConfiguration = UpgradeTestCase.createProcessEngineConfiguration(database);

      // install the jdbc proxy driver
      log.fine("installing jdbc proxy driver...");
      ProxyDriver.setUrl(processEngineConfiguration.getJdbcUrl());
      processEngineConfiguration.setJdbcUrl("proxy");
      DriverManager.registerDriver(new ProxyDriver());

      log.fine("building the process engine...");
      ProcessEngine processEngine = processEngineConfiguration.buildProcessEngine();

      log.fine("deploy processes and start process instances");
      UpgradeTestCase.setProcessEngine(processEngine);
      
      for (UpgradeTestCase upgradeTestCase: upgradeTestCases) {
        log.fine("### Running test "+ClassNameUtil.getClassNameWithoutPackage(upgradeTestCase.getClass())+" in the old version");
        upgradeTestCase.runInTheOldVersion();
      }

      log.fine("### Captured SQL");
      PrintWriter file = new PrintWriter("src/test/resources/org/activiti/db/"+releaseVersion+"/data/"+database+".data.sql");
      System.err.println();
      System.err.println();
      for (String statement: ProxyDriver.statements) {
        System.err.println(statement);
        System.err.println();
        file.println(statement);
        file.println();
      }
      file.close();
      
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    dbSchemaDrop(processEngineConfiguration);
  }

  private static void dbSchemaDrop(ProcessEngineConfigurationImpl processEngineConfiguration) {
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    commandExecutor.execute(new Command<Object> (){
      public Object execute(CommandContext commandContext) {
        commandContext
          .getSession(DbSqlSession.class)
          .dbSchemaDrop();
        return null;
      }
    });
  }
}

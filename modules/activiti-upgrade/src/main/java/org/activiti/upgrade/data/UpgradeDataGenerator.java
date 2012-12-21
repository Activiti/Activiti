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
package org.activiti.upgrade.data;

import java.io.PrintWriter;
import java.sql.DriverManager;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.upgrade.CleanPostgres;
import org.activiti.upgrade.ProxyDriver;
import org.activiti.upgrade.UpgradeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class UpgradeDataGenerator {
  
  static Logger log = LoggerFactory.getLogger(UpgradeDataGenerator.class);
  
  public static void main(String[] args) {
    
    ProcessEngineConfigurationImpl processEngineConfiguration = null;
    
    try {
      if (args==null || args.length!=2) {
        throw new RuntimeException("exactly 2 arguments expected: database and releaseVersion");
      }
      
      String database = args[0];
      String releaseVersion = args[1];
      log.debug("database: {}", database);
      log.debug("releaseVersion: {}", releaseVersion);
  
      processEngineConfiguration = UpgradeUtil.createProcessEngineConfiguration(database);

      // install the jdbc proxy driver
      log.debug("installing jdbc proxy driver delegating to {}", processEngineConfiguration.getJdbcUrl());
      ProxyDriver.setUrl(processEngineConfiguration.getJdbcUrl());
      processEngineConfiguration.setJdbcUrl("proxy");
      DriverManager.registerDriver(new ProxyDriver());

      log.debug("building the process engine...");
      ProcessEngine processEngine = processEngineConfiguration.buildProcessEngine();

      
      log.debug("### Running data generator {} in the old version", CommonDataGenerator.class.getSimpleName());
      CommonDataGenerator commonDataGenerator = new CommonDataGenerator();
      commonDataGenerator.setProcessEngine(processEngine);
      commonDataGenerator.run();
      
      // < 5.11 upgrade tests (ie the following data only needs to be generated when the engine is lower then or equals 5.10)
      if (UpgradeUtil.getProcessEngineVersion(processEngine) < 11) {
        Activiti_5_10_DataGenerator activiti_5_10_DataGenerator = new Activiti_5_10_DataGenerator();
        activiti_5_10_DataGenerator.setProcessEngine(processEngine);
        activiti_5_10_DataGenerator.run();
      }

      log.debug("### Captured SQL");
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
    if (processEngineConfiguration.getDatabaseType().equals("postgres")) {
      
      CleanPostgres cleanPostgres = new CleanPostgres();
      cleanPostgres.execute();
      
    } else {
    
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
}

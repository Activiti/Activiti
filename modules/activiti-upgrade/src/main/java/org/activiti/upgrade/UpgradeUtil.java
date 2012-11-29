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
import java.util.Properties;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;

/**
 * @author Joram Barrez
 */
public class UpgradeUtil {
  
  /**
   * Returns the number after the 5. of the process engine version. Ie 5.11 -> 11
   */
  public static int getProcessEngineVersion(ProcessEngine processEngine) {
    return Integer.parseInt(processEngine.VERSION.toLowerCase()
            .replace("-snapshot", "")
            .replace("5.", ""));
  }
  
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

    return processEngineConfiguration;
  }
  
}

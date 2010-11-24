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

package org.activiti.engine.impl.cfg;

import java.io.InputStream;
import java.net.URL;

import org.activiti.engine.impl.util.xml.Element;
import org.activiti.engine.impl.util.xml.Parse;
import org.activiti.engine.impl.util.xml.Parser;


/**
 * Specific parsing of one XML configuration file, created by the {@link ConfigurationParser}.
 * 
 * @author Joram Barrez
 */
public class ConfigurationParse extends Parse {
  
  // Process engine
  protected String processEngineName;
  
  // Database config
  protected boolean isJdbcConfigured;
  protected String databaseType;
  protected String databaseSchemaStrategy;
  protected String jdbcUrl;
  protected String jdbcDriver;
  protected String jdbcUsername;
  protected String jdbcPassword;
  protected Integer maxActiveConnections;
  protected Integer maxIdleConnections;
  protected Integer maxCheckoutTime;
  protected Integer maxWaitTime;
  
  // Jobexecutor
  protected Boolean jobExecutorActivate;
  
  // Mail
  protected String mailServerHost;
  protected Integer mailServerPort;
  protected String mailServerUsername;
  protected String mailServerPassword;
  protected String mailDefaultFrom;
  
  // History
  protected Integer historyLevel;

  /**
   * Note package modifier: only {@link ConfigurationParser} is allowed to create instances.
   */
  ConfigurationParse(Parser parser) {
    super(parser);
  }
  
  @Override
  public ConfigurationParse execute() {
    super.execute();
    
    parseRootElement();
    parseDatabaseCfg();
    parseJobExecutorCfg();
    parseMailServerCfg();
    parseHistoryCfg();

    if (hasWarnings()) {
      logWarnings();
    }
    if (hasErrors()) {
      throwActivitiExceptionForErrors();
    }
    
    return this;
  }
  
  protected void parseRootElement() {
    if (!"activiti-cfg".equals(rootElement.getTagName())) {
      addError("Invalid root element: " + rootElement.getTagName(), rootElement);
    }
    this.processEngineName = rootElement.attribute("process-engine-name");
  }
  
  protected void parseDatabaseCfg() {
    Element databaseElement = rootElement.element("database");
    if (databaseElement != null) {
      this.databaseType = databaseElement.attribute("type");
      this.databaseSchemaStrategy = databaseElement.attribute("schema-strategy");
      
      // Jdbc
      Element jdbcElement = databaseElement.element("jdbc");
      if (jdbcElement != null) {
        this.isJdbcConfigured = true;
        this.jdbcUrl = jdbcElement.attribute("url");
        this.jdbcDriver = jdbcElement.attribute("driver");
        this.jdbcUsername = jdbcElement.attribute("username");
        this.jdbcPassword = jdbcElement.attribute("password");
        
        if (jdbcUrl == null || jdbcDriver == null || jdbcUsername == null || jdbcPassword == null) {
          addError("Invalid jdbc configuration: need to provide url, driver, username and password", jdbcElement);
        }
        
        this.maxActiveConnections = stringToInteger(jdbcElement, "max-active", jdbcElement.attribute("max-active"));
        this.maxIdleConnections = stringToInteger(jdbcElement, "max-idle", jdbcElement.attribute("max-idle"));
        this.maxCheckoutTime = stringToInteger(jdbcElement, "max-checkout", jdbcElement.attribute("max-checkout"));
        this.maxWaitTime = stringToInteger(jdbcElement, "max-wait", jdbcElement.attribute("max-wait"));
      }
      
      // Datasource through jndi: TODO
      
    } else {
      addError("Could not find required element 'database'", rootElement);
    }

  }
  
  protected void parseJobExecutorCfg() {
    Element jobExecutorElement = rootElement.element("job-executor");
    if (jobExecutorElement != null) {
      String activateString = jobExecutorElement.attribute("activate");
      
      if (activateString != null) {
        
        if (activateString.equals("off")
              || activateString.equals("disabled")
              || activateString.equals("false")) {
          this.jobExecutorActivate = false;
          
        } else if (activateString.equals("on")
                || activateString.equals("enabled")
                || activateString.equals("true")) {
          this.jobExecutorActivate = true;
          
        } else {
          addError("Invalid value for 'auto-activate', current values are supported:" 
                  +	"on/off enabled/disabled true/false", jobExecutorElement);
        }
      }
      
    }
    
  }
  
  protected void parseMailServerCfg() {
    Element mailElement = rootElement.element("mail");
    if (mailElement != null) {
      this.mailServerHost = mailElement.attribute("server");
      
      if(mailServerHost == null) {
        addError("server is a required attribute when configuring e-mail", mailElement);
      }
      
      this.mailServerUsername = mailElement.attribute("username");
      this.mailServerPassword = mailElement.attribute("password");
      this.mailDefaultFrom = mailElement.attribute("default-from");
      this.mailServerPort = stringToInteger(mailElement, "mail port", mailElement.attribute("port"));
    }
  }
  
  protected void parseHistoryCfg() {
    Element historyElement = rootElement.element("history");
    if (historyElement != null) {
      String historyLevelString = historyElement.attribute("level");
      if (historyLevelString != null) {
        this.historyLevel = ProcessEngineConfigurationImpl.parseHistoryLevel(historyLevelString);
      }
    }
  }
  
  protected Integer stringToInteger(Element element, String attributeName, String value) {
    if (value != null) {
      try {
        return Integer.parseInt(value);
      } catch (NumberFormatException e) {
        addError("Invalid: value for " + attributeName + " is not numerical", element);
      }
    }
    return null;
  }
  
  // Source definition operations //////////////////////////////////////////////////
  
  @Override
  public ConfigurationParse sourceInputStream(InputStream inputStream) {
    super.sourceInputStream(inputStream);
    return this;
  }
  
  @Override
  public ConfigurationParse sourceResource(String resource) {
    super.sourceResource(resource);
    return this;
  }
  
  @Override
  public ConfigurationParse sourceResource(String resource, ClassLoader classLoader) {
    super.sourceResource(resource, classLoader);
    return this;
  }
  
  @Override
  public ConfigurationParse sourceString(String string) {
    super.sourceString(string);
    return this;
  }
  
  @Override
  public ConfigurationParse sourceUrl(String url) {
    super.sourceUrl(url);
    return this;
  }
  
  @Override
  public ConfigurationParse sourceUrl(URL url) {
    super.sourceUrl(url);
    return this;
  }
  
  // Getters (available after calling execute() ) ////////////////////////////////////
  
  public String getProcessEngineName() {
    return processEngineName;
  }
  public boolean isJdbcConfigured() {
    return isJdbcConfigured;
  }
  public String getDatabaseType() {
    return databaseType;
  }
  public String getDatabaseSchemaStrategy() {
    return databaseSchemaStrategy;
  }
  public String getJdbcUrl() {
    return jdbcUrl;
  }
  public String getJdbcDriver() {
    return jdbcDriver;
  }
  public String getJdbcUsername() {
    return jdbcUsername;
  }
  public String getJdbcPassword() {
    return jdbcPassword;
  }
  public Boolean getJobExecutorActivate() {
    return jobExecutorActivate;
  }
  public String getMailServerHost() {
    return mailServerHost;
  }
  public Integer getMailServerPort() {
    return mailServerPort;
  }
  public String getMailServerUsername() {
    return mailServerUsername;
  }
  public String getMailServerPassword() {
    return mailServerPassword;
  }
  public String getMailDefaultFrom() {
    return mailDefaultFrom;
  }
  public Integer getHistoryLevel() {
    return historyLevel;
  }
  public Integer getMaxActiveConnections() {
    return maxActiveConnections;
  }
  public Integer getMaxIdleConnections() {
    return maxIdleConnections;
  }
  public Integer getMaxCheckoutTime() {
    return maxCheckoutTime;
  }
  public Integer getMaxWaitTime() {
    return maxWaitTime;
  }

}

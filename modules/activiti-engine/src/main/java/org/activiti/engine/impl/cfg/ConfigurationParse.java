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

import org.activiti.engine.impl.util.ReflectUtil;
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
  protected String databaseName;
  protected String databaseSchemaStrategy;
  protected String jdbcUrl;
  protected String jdbcDriver;
  protected String jdbcUsername;
  protected String jdbcPassword;
  
  // Jobexecutor
  protected Boolean isJobExecutorAutoActivate;
  
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
    setSchemaResource(ReflectUtil.getClassLoader().getResource(ConfigurationParser.SCHEMA_RESOURCE_5_0).toString());
  }
  
  @Override
  public ConfigurationParse execute() {
    super.execute();
    
    parseRootElementAttributes();
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
  
  protected void parseRootElementAttributes() {
    this.processEngineName = rootElement.attribute("process-engine-name");
  }
  
  protected void parseDatabaseCfg() {
    Element databaseElement = rootElement.element("database");
    if (databaseElement != null) {
      this.databaseName = databaseElement.attribute("name");
      this.databaseSchemaStrategy = databaseElement.attribute("schema-strategy");
      
      // Jdbc
      Element jdbcElement = databaseElement.element("jdbc");
      if (jdbcElement != null) {
        this.isJdbcConfigured = true;
        this.jdbcUrl = jdbcElement.attribute("url");
        this.jdbcDriver = jdbcElement.attribute("driver");
        this.jdbcUsername = jdbcElement.attribute("username");
        this.jdbcPassword = jdbcElement.attribute("password");
      }
      
      // Datasource through jndi: TODO
      
    } else {
      addError("Could not find required element 'database'", rootElement);
    }

  }
  
  protected void parseJobExecutorCfg() {
    Element jobExecutorElement = rootElement.element("job-executor");
    if (jobExecutorElement != null) {
      String autoActivateString = jobExecutorElement.attribute("auto-activate");
      
      if (autoActivateString != null) {
        
        if (autoActivateString.equals("off")
              || autoActivateString.equals("disabled")
              || autoActivateString.equals("false")) {
          this.isJobExecutorAutoActivate = false;
          
        } else if (autoActivateString.equals("on")
                || autoActivateString.equals("enabled")
                || autoActivateString.equals("true")) {
          this.isJobExecutorAutoActivate = true;
          
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
      this.mailServerHost = mailElement.attribute("host");
      this.mailServerUsername = mailElement.attribute("username");
      this.mailServerPassword = mailElement.attribute("password");
      this.mailDefaultFrom = mailElement.attribute("default-from");
      try {
        this.mailServerPort = Integer.parseInt(mailElement.attribute("port"));
      } catch (NumberFormatException e) {
        addError("Invalid port for mail service", mailElement);
      }
    }
  }
  
  protected void parseHistoryCfg() {
    Element historyElement = rootElement.element("history");
    if (historyElement != null) {
      String historyLevelString = historyElement.attribute("level");
      if (historyLevelString != null) {
        this.historyLevel = ProcessEngineConfiguration.parseHistoryLevel(historyLevelString);
      }
    }
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
  public String getDatabaseName() {
    return databaseName;
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
  public Boolean getIsJobExecutorAutoActivate() {
    return isJobExecutorAutoActivate;
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
  

}

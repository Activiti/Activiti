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
package org.activiti.engine;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.activiti.engine.impl.cfg.ProcessEngineConfiguration;

/**
 * Builds a process engine based on a couple of simple properties.
 * 
 * To build a ProcessEngine that's using a h2 database over a TCP connection:
 * 
 * <pre>
 * ProcessEngine processEngine = ProcessEngineBuilder
 *   .setDatabaseName(&quot;h2&quot;)
 *   .setJdbcDriver(&quot;org.h2.Driver&quot;)
 *   .setJdbcUrl(&quot;jdbc:h2:tcp://localhost/activiti&quot;)
 *   .setJdbcUsername(&quot;sa&quot;)
 *   .setJdbcPassword(&quot;&quot;)
 *   .setDbSchemaStrategy(DbSchemaStrategy.CHECK_VERSION)
 *   .buildProcessEngine();
 * </pre>
 * 
 * To build a ProcessEngine that's using a h2 in memory database:
 * 
 * <pre>
 * ProcessEngine processEngine = ProcessEngineBuilder
 *   .setDatabaseName(&quot;h2&quot;)
 *   .setJdbcDriver(&quot;org.h2.Driver&quot;)
 *   .setJdbcUrl(&quot;jdbc:h2:mem:activiti&quot;)
 *   .setJdbcUsername(&quot;sa&quot;)
 *   .setJdbcPassword(&quot;&quot;)
 *   .setDbSchemaStrategy(DbSchemaStrategy.CREATE_DROP)
 *   .buildProcessEngine();
 * </pre>
 * 
 * @see ProcessEngines
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class ProcessEngineBuilder {

  protected String processEngineName = ProcessEngines.NAME_DEFAULT;
  protected String databaseName = ProcessEngineConfiguration.DEFAULT_DATABASE_NAME;
  protected String jdbcDriver = ProcessEngineConfiguration.DEFAULT_JDBC_DRIVER;
  protected String jdbcUrl = ProcessEngineConfiguration.DEFAULT_JDBC_URL;
  protected String jdbcUsername = ProcessEngineConfiguration.DEFAULT_JDBC_USERNAME;
  protected String jdbcPassword = ProcessEngineConfiguration.DEFAULT_JDBC_PASSWORD;
  protected String wsSyncFactoryClassName = ProcessEngineConfiguration.DEFAULT_WS_SYNC_FACTORY;
  protected DbSchemaStrategy dbSchemaStrategy = DbSchemaStrategy.CHECK_VERSION;
  protected boolean jobExecutorAutoActivate = true;
  protected boolean localTransactions = true;
  
  protected String mailServerSmtpHost;
  protected String mailServerSmtpUserName;
  protected String mailServerSmtpPassword;
  protected int mailServerSmtpPort = ProcessEngineConfiguration.DEFAULT_MAIL_SERVER_SMTP_PORT;
  protected String mailServerDefaultFrom = ProcessEngineConfiguration.DEFAULT_FROM_EMAIL_ADDRESS;

  public ProcessEngineBuilder setProcessEngineName(String processEngineName) {
    this.processEngineName = processEngineName;
    return this;
  }

  public ProcessEngineBuilder setDatabaseName(String databaseName) {
    this.databaseName = databaseName;
    return this;
  }

  public ProcessEngineBuilder setJdbcDriver(String jdbcDriver) {
    this.jdbcDriver = jdbcDriver;
    return this;
  }

  public ProcessEngineBuilder setLocalTransactions(boolean localTransactions) {
    this.localTransactions = localTransactions;
    return this;
  }

  public ProcessEngineBuilder setJdbcUrl(String jdbcUrl) {
    this.jdbcUrl = jdbcUrl;
    return this;
  }

  public ProcessEngineBuilder setJdbcUsername(String jdbcUsername) {
    this.jdbcUsername = jdbcUsername;
    return this;
  }

  public ProcessEngineBuilder setJdbcPassword(String jdbcPassword) {
    this.jdbcPassword = jdbcPassword;
    return this;
  }

  public ProcessEngineBuilder setDbSchemaStrategy(DbSchemaStrategy dbSchemaStrategy) {
    this.dbSchemaStrategy = dbSchemaStrategy;
    return this;
  }
  
  public ProcessEngineBuilder setMailServerSmtpHost(String mailServerSmtpHost) {
    this.mailServerSmtpHost = mailServerSmtpHost;
    return this;
  }
  
  public ProcessEngineBuilder setMailServerSmtpUserName(String mailServerSmtpUserName) {
    this.mailServerSmtpUserName = mailServerSmtpUserName;
    return this;
  }
  
  public ProcessEngineBuilder setMailServerSmtpPassword(String mailServerSmtpPassword) {
    this.mailServerSmtpPassword = mailServerSmtpPassword;
    return this;
  }
  
  public ProcessEngineBuilder setMailServerSmtpPort(int mailServerSmtpPort) {
    this.mailServerSmtpPort = mailServerSmtpPort;
    return this;
  }
  
  public ProcessEngineBuilder setMailServerDefaultFrom(String mailServerDefaultFrom) {
    this.mailServerDefaultFrom = mailServerDefaultFrom;
    return this;
  }

  public ProcessEngineBuilder configureFromProperties(Properties configurationProperties) {
    if (configurationProperties == null) {
      throw new ActivitiException("configurationProperties is null");
    }

    String processEngineName = configurationProperties.getProperty("process.engine.name");
    if (processEngineName != null) {
      this.processEngineName = processEngineName;
    }
    
    // DATABASE

    String databaseName = configurationProperties.getProperty("database");
    if (databaseName != null) {
      this.databaseName = databaseName;
    }

    String jdbcDriver = configurationProperties.getProperty("jdbc.driver");
    if (jdbcDriver != null) {
      this.jdbcDriver = jdbcDriver;
    }

    String jdbcUrl = configurationProperties.getProperty("jdbc.url");
    if (jdbcUrl != null) {
      this.jdbcUrl = jdbcUrl;
    }

    String jdbcUsername = configurationProperties.getProperty("jdbc.username");
    if (jdbcUsername != null) {
      this.jdbcUsername = jdbcUsername;
    }

    String jdbcPassword = configurationProperties.getProperty("jdbc.password");
    if (jdbcPassword != null) {
      this.jdbcPassword = jdbcPassword;
    }

    String dbSchemaStrategy = configurationProperties.getProperty("db.schema.strategy");
    if (dbSchemaStrategy != null) {
      String strategy = dbSchemaStrategy.toUpperCase().replace("-", "_");
      this.dbSchemaStrategy = DbSchemaStrategy.valueOf(strategy);
    }
    
    // JOBEXECUTOR

    String jobExecutorAutoActivate = configurationProperties.getProperty("job.executor.auto.activate");
    if ((jobExecutorAutoActivate != null)
            && (("false".equals(jobExecutorAutoActivate)) || ("disabled".equals(jobExecutorAutoActivate)) || ("off".equals(jobExecutorAutoActivate)))) {
      this.jobExecutorAutoActivate = false;
    }
    
    // WEBSERVICE
    
    String wsSyncFactory = configurationProperties.getProperty("ws.sync.factory");
    if (wsSyncFactory != null) {
      this.wsSyncFactoryClassName = wsSyncFactory;
    }

    // EMAIL
    
    String mailServerSmtpHost = configurationProperties.getProperty("mail.smtp.host");
    if (mailServerSmtpHost != null) {
      this.mailServerSmtpHost = mailServerSmtpHost;
    }
    
    String mailServerSmtpPort= configurationProperties.getProperty("mail.smtp.port");
    if (mailServerSmtpPort != null) {
      try {
        this.mailServerSmtpPort = Integer.parseInt(mailServerSmtpPort);
      } catch (NumberFormatException e) {
        throw new ActivitiException("Invalid port number: " + mailServerSmtpPort, e);
      }
    }
    
    String mailServerSmtpUserName = configurationProperties.getProperty("mail.smtp.user");
    if (mailServerSmtpUserName != null) {
      this.mailServerSmtpUserName = mailServerSmtpUserName;
    }
    
    String mailServerSmtpPassword= configurationProperties.getProperty("mail.smtp.password");
    if (mailServerSmtpPassword != null) {
      this.mailServerSmtpPassword = mailServerSmtpPassword;
    }
    
    String mailServerDefaultFrom= configurationProperties.getProperty("mail.default.from");
    if (mailServerDefaultFrom != null) {
      this.mailServerDefaultFrom = mailServerDefaultFrom;
    }
    
    return this;
  }

  public ProcessEngineBuilder configureFromPropertiesInputStream(InputStream inputStream) {
    if (inputStream == null) {
      throw new ActivitiException("inputStream is null");
    }
    Properties properties = new Properties();
    try {
      properties.load(inputStream);
    } catch (IOException e) {
      throw new ActivitiException("problem while reading activiti configuration properties " + e.getMessage(), e);
    }
    configureFromProperties(properties);
    return this;
  }

  public ProcessEngineBuilder configureFromPropertiesResource(String propertiesResource) {
    InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(propertiesResource);
    if (inputStream == null) {
      throw new ActivitiException("configuration properties resource '" + propertiesResource + "' is unavailable on classpath "
              + System.getProperty("java.class.path"));
    }
    configureFromPropertiesInputStream(inputStream);
    return this;
  }

  public ProcessEngineBuilder setJobExecutorAutoActivation(boolean jobExecutorAutoActivate) {
    this.jobExecutorAutoActivate = jobExecutorAutoActivate;
    return this;
  }

  public ProcessEngine buildProcessEngine() {
    if (databaseName == null) {
      throw new ActivitiException("no database name specified (used to look up queries and scripts)");
    }

    ProcessEngineConfiguration processEngineConfiguration = new ProcessEngineConfiguration();
    processEngineConfiguration.setProcessEngineName(processEngineName);
    
    // JOBEXECUTOR
    processEngineConfiguration.setJobExecutorAutoActivate(jobExecutorAutoActivate);

    // DATABASE
    processEngineConfiguration.setDbSchemaStrategy(dbSchemaStrategy);
    processEngineConfiguration.setJdbcDriver(jdbcDriver);
    processEngineConfiguration.setJdbcUrl(jdbcUrl);
    processEngineConfiguration.setJdbcUsername(jdbcUsername);
    processEngineConfiguration.setJdbcPassword(jdbcPassword);
    processEngineConfiguration.setDatabaseName(databaseName);
    processEngineConfiguration.setLocalTransactions(localTransactions);
    
    // WEBSERVICE
    processEngineConfiguration.setWsSyncFactoryClassName(wsSyncFactoryClassName);
    
    // EMAIL
    processEngineConfiguration.setMailServerSmtpHost(mailServerSmtpHost);
    processEngineConfiguration.setMailServerSmtpPort(mailServerSmtpPort);
    processEngineConfiguration.setMailServerSmtpUserName(mailServerSmtpUserName);
    processEngineConfiguration.setMailServerSmtpPassword(mailServerSmtpPassword);
    processEngineConfiguration.setMailServerDefaultFrom(mailServerDefaultFrom);
      
    return processEngineConfiguration.buildProcessEngine();
  }
}

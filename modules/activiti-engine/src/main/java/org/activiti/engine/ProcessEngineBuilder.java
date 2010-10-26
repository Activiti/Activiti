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

import org.activiti.engine.impl.cfg.ProcessEngineConfiguration;
import org.activiti.engine.impl.cfg.ConfigurationParse;
import org.activiti.engine.impl.cfg.ConfigurationParser;
import org.activiti.engine.impl.util.ReflectUtil;
import org.activiti.engine.impl.util.IoUtil;

/**
 * Builds a process engine based on an XML configuration resource:
 * 
 * <pre>
 * ProcessEngine processEngine = ProcessEngineBuilder
 *   .configureFromResource("activiti.cfg.xml")
 *   .buildProcessEngine();
 * </pre>
 * 
 * To build programmatically a ProcessEngine that for example uses a h2 database over a TCP connection:
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

  protected ProcessEngineConfiguration processEngineConfiguration = new ProcessEngineConfiguration();
  
  public ProcessEngineBuilder setProcessEngineName(String processEngineName) {
    processEngineConfiguration.setProcessEngineName(processEngineName);
    return this;
  }

  public ProcessEngineBuilder setDatabaseName(String databaseName) {
    processEngineConfiguration.setDatabaseName(databaseName);
    return this;
  }

  public ProcessEngineBuilder setJdbcDriver(String jdbcDriver) {
    processEngineConfiguration.setJdbcDriver(jdbcDriver);
    return this;
  }

  public ProcessEngineBuilder setTransactionsExternallyManaged(boolean transactionsExternallyManaged) {
    processEngineConfiguration.setTransactionsExternallyManaged(transactionsExternallyManaged);
    return this;
  }

  public ProcessEngineBuilder setJdbcUrl(String jdbcUrl) {
    processEngineConfiguration.setJdbcUrl(jdbcUrl);
    return this;
  }

  public ProcessEngineBuilder setJdbcUsername(String jdbcUsername) {
    processEngineConfiguration.setJdbcUsername(jdbcUsername);
    return this;
  }

  public ProcessEngineBuilder setJdbcPassword(String jdbcPassword) {
    processEngineConfiguration.setJdbcPassword(jdbcPassword);
    return this;
  }

  public ProcessEngineBuilder setDbSchemaStrategy(String dbSchemaStrategy) {
    processEngineConfiguration.setDbSchemaStrategy(dbSchemaStrategy);
    return this;
  }
  
  public ProcessEngineBuilder setMailServerHost(String mailServerHost) {
    processEngineConfiguration.setMailServerHost(mailServerHost);
    return this;
  }
  
  public ProcessEngineBuilder setMailServerUsername(String mailServerUsername) {
    processEngineConfiguration.setMailServerUsername(mailServerUsername);
    return this;
  }
  
  public ProcessEngineBuilder setMailServerPassword(String mailServerPassword) {
    processEngineConfiguration.setMailServerPassword(mailServerPassword);
    return this;
  }
  
  public ProcessEngineBuilder setMailServerPort(int mailServerPort) {
    processEngineConfiguration.setMailServerPort(mailServerPort);
    return this;
  }
  
  public ProcessEngineBuilder setMailServerDefaultFrom(String mailServerDefaultFrom) {
    processEngineConfiguration.setMailServerDefaultFrom(mailServerDefaultFrom);
    return this;
  }

  public ProcessEngineBuilder setJobExecutorAutoActivation(boolean jobExecutorAutoActivate) {
    processEngineConfiguration.setJobExecutorAutoActivate(jobExecutorAutoActivate);
    return this;
  }
  
  public ProcessEngineBuilder enableJPA(Object entityManagerFactory, boolean handleTransaction, boolean closeEntityManager) {
    processEngineConfiguration.enableJPA(entityManagerFactory, handleTransaction, closeEntityManager);
    return this;
  }
  
  public ProcessEngineBuilder enableJPA(Object entityManagerFactory) {
    return enableJPA(entityManagerFactory, true, true);
  }
  
  /**
   * Configures a {@link ProcessEngine} based on an XML configuration provided by the inputstream.
   * 
   * Calling methods are responsible for closing the provided inputStream.
   */
  public ProcessEngineBuilder configureFromInputStream(InputStream inputStream) {
    if (inputStream == null) {
      throw new ActivitiException("inputStream is null");
    }
    
    ConfigurationParser cfgParser = new ConfigurationParser();
    ConfigurationParse cfgParse = cfgParser.createParse()
      .sourceInputStream(inputStream)
      .execute();
    
    // Process engine
    String processEngineName = cfgParse.getProcessEngineName();
    if (processEngineName != null) {
      processEngineConfiguration.setProcessEngineName(processEngineName);
    }
    
    // Database
    String databaseName = cfgParse.getDatabaseName();
    if (databaseName != null) {
      processEngineConfiguration.setDatabaseName(databaseName);
    }
    String databaseSchemaStrategy = cfgParse.getDatabaseSchemaStrategy();
    if (databaseSchemaStrategy != null) {
      processEngineConfiguration.setDbSchemaStrategy(databaseSchemaStrategy);
    }
    if (cfgParse.isJdbcConfigured()) {
      String databaseUrl = cfgParse.getJdbcUrl();
      if (databaseUrl != null) {
        processEngineConfiguration.setJdbcUrl(databaseUrl);
      }
      String databaseDriver = cfgParse.getJdbcDriver();
      if (databaseDriver != null) {
        processEngineConfiguration.setJdbcDriver(databaseDriver);
      }
      String databaseUsername = cfgParse.getJdbcUsername();
      if (databaseUsername != null) {
        processEngineConfiguration.setJdbcUsername(databaseUsername);
      }
      String databasePassword = cfgParse.getJdbcPassword();
      if (databasePassword != null) {
        processEngineConfiguration.setJdbcPassword(databasePassword);
      }
    }
    
    // Mail
    String mailServerHost = cfgParse.getMailServerHost();
    if (mailServerHost != null) {
      processEngineConfiguration.setMailServerHost(mailServerHost);
    }
    Integer mailServerPort = cfgParse.getMailServerPort();
    if (mailServerPort != null) {
      processEngineConfiguration.setMailServerPort(mailServerPort);
    }
    String mailServerUsername = cfgParse.getMailServerUsername();
    if (mailServerUsername != null) {
      processEngineConfiguration.setMailServerUsername(mailServerUsername);
    }
    String mailServerPassword = cfgParse.getMailServerPassword();
    if (mailServerPassword != null) {
      processEngineConfiguration.setMailServerPassword(mailServerPassword);
    }
    String mailDefaultFrom = cfgParse.getMailDefaultFrom();
    if (mailDefaultFrom != null) {
      processEngineConfiguration.setMailServerDefaultFrom(mailDefaultFrom);
    }
    
    // Job executor
    Boolean jobExecutorAutoActivate = cfgParse.getIsJobExecutorAutoActivate();
    if (jobExecutorAutoActivate != null) {
      processEngineConfiguration.setJobExecutorAutoActivate(jobExecutorAutoActivate);
    }
    
    // History
    Integer historyLevel = cfgParse.getHistoryLevel();
    if (historyLevel != null) {
      processEngineConfiguration.setHistoryLevel(historyLevel);
    }

    return this;
  }
  
  /**
   * Configures a {@link ProcessEngine} based on an XML configuration file.
   */
  public ProcessEngineBuilder configureFromResource(String resource) {
    InputStream inputStream = ReflectUtil.getClassLoader().getResourceAsStream(resource);
    if (inputStream == null) {
      throw new ActivitiException("configuration resource '" + resource 
              + "' is unavailable on classpath " + System.getProperty("java.class.path"));
    }
    try {
      configureFromInputStream(inputStream);
      inputStream.close();
    } catch (IOException e) {
        throw new ActivitiException("Exception while closing inputstream", e);
    } finally {
      IoUtil.closeSilently(inputStream);
    }
    return this;
  }

  public ProcessEngine buildProcessEngine() {
    return processEngineConfiguration.buildProcessEngine();
  }
}

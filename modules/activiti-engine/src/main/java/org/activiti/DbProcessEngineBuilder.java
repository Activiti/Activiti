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
package org.activiti;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.sql.DataSource;

import org.activiti.impl.cfg.ProcessEngineFactory;
import org.apache.ibatis.datasource.pooled.PooledDataSource;

/**
 * builds a process engine based on a couple of simple properties.
 * 
 * To build a ProcessEngine that's using a h2 database over a TCP connection:
 * 
 * <pre>
 * 
 * 
 * 
 * 
 * 
 * ProcessEngine processEngine = DbProcessEngineBuilder.setDatabaseName(&quot;h2&quot;).setJdbcDriver(&quot;org.h2.Driver&quot;).setJdbcUrl(&quot;jdbc:h2:tcp://localhost/activiti&quot;)
 *         .setJdbcUsername(&quot;sa&quot;).setJdbcPassword(&quot;&quot;).setDbSchemaStrategy(DbSchemaStrategy.CHECK_VERSION).buildProcessEngine();
 * </pre>
 * 
 * To build a ProcessEngine that's using a h2 in memory database:
 * 
 * <pre>
 * 
 * 
 * 
 * 
 * 
 * ProcessEngine processEngine = DbProcessEngineBuilder.setDatabaseName(&quot;h2&quot;).setJdbcDriver(&quot;org.h2.Driver&quot;).setJdbcUrl(&quot;jdbc:h2:mem:activiti&quot;).setJdbcUsername(
 *         &quot;sa&quot;).setJdbcPassword(&quot;&quot;).setDbSchemaStrategy(DbSchemaStrategy.CREATE_DROP).buildProcessEngine();
 * </pre>
 * 
 * @see ProcessEngines
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class DbProcessEngineBuilder {

  private String processEngineName = ProcessEngines.NAME_DEFAULT;
  private String dataBaseName;
  private String jdbcDriver;
  private String jdbcUrl;
  private String jdbcUsername;
  private String jdbcPassword;
  private DbSchemaStrategy dbSchemaStrategy = DbSchemaStrategy.CHECK_VERSION;
  private boolean jobExecutorAutoActivate = true;
  private boolean localTransactions = true;

  public DbProcessEngineBuilder setProcessEngineName(String processEngineName) {
    this.processEngineName = processEngineName;
    return this;
  }

  public DbProcessEngineBuilder setDatabaseName(String databaseName) {
    this.dataBaseName = databaseName;
    return this;
  }

  public DbProcessEngineBuilder setJdbcDriver(String jdbcDriver) {
    this.jdbcDriver = jdbcDriver;
    return this;
  }

  public DbProcessEngineBuilder setLocalTransactions(boolean localTransactions) {
    this.localTransactions = localTransactions;
    return this;
  }

  public DbProcessEngineBuilder setJdbcUrl(String jdbcUrl) {
    this.jdbcUrl = jdbcUrl;
    return this;
  }

  public DbProcessEngineBuilder setJdbcUsername(String jdbcUsername) {
    this.jdbcUsername = jdbcUsername;
    return this;
  }

  public DbProcessEngineBuilder setJdbcPassword(String jdbcPassword) {
    this.jdbcPassword = jdbcPassword;
    return this;
  }

  public DbProcessEngineBuilder setDbSchemaStrategy(DbSchemaStrategy dbSchemaStrategy) {
    this.dbSchemaStrategy = dbSchemaStrategy;
    return this;
  }

  public DbProcessEngineBuilder configureFromProperties(Properties configurationProperties) {
    if (configurationProperties == null) {
      throw new ActivitiException("configurationProperties is null");
    }

    String processEngineName = configurationProperties.getProperty("process.engine.name");
    if (processEngineName != null) {
      this.processEngineName = processEngineName;
    }

    String databaseName = configurationProperties.getProperty("database");
    if (databaseName != null) {
      this.dataBaseName = databaseName;
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

    String jobExecutorAutoActivate = configurationProperties.getProperty("job.executor.auto.activate");
    if ((jobExecutorAutoActivate != null)
            && (("false".equals(jobExecutorAutoActivate)) || ("disabled".equals(jobExecutorAutoActivate)) || ("off".equals(jobExecutorAutoActivate)))) {
      this.jobExecutorAutoActivate = false;
    }

    return this;
  }

  public DbProcessEngineBuilder configureFromPropertiesInputStream(InputStream inputStream) {
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

  public DbProcessEngineBuilder configureFromPropertiesResource(String propertiesResource) {
    InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(propertiesResource);
    if (inputStream == null) {
      throw new ActivitiException("configuration properties resource '" + propertiesResource + "' is unavailable on classpath "
              + System.getProperty("java.class.path"));
    }
    configureFromPropertiesInputStream(inputStream);
    return this;
  }

  public DbProcessEngineBuilder setJobExecutorAutoActivation(boolean jobExecutorAutoActivate) {
    this.jobExecutorAutoActivate = jobExecutorAutoActivate;
    return this;
  }

  public ProcessEngine buildProcessEngine() {
    if (dataBaseName == null) {
      throw new ActivitiException("no database name specified (used to look up queries and scripts)");
    }

    ProcessEngineFactory factory = new ProcessEngineFactory();
    factory.setProcessEngineName(processEngineName);
    factory.setDbSchemaStrategy(dbSchemaStrategy);
    factory.setJobExecutorAutoActivate(jobExecutorAutoActivate);

    if (jdbcDriver == null) {
      throw new ActivitiException("no jdbc driver specified");
    }
    if (jdbcUrl == null) {
      throw new ActivitiException("no jdbc url specified");
    }
    if (jdbcUsername == null) {
      throw new ActivitiException("no jdbc username specified");
    }
    if (jdbcPassword == null) {
      throw new ActivitiException("no jdbc password specified");
    }

    DataSource dataSource = new PooledDataSource(Thread.currentThread().getContextClassLoader(), jdbcDriver, jdbcUrl, jdbcUsername, jdbcPassword);
    factory.setDataSource(dataSource);
    factory.setDatabaseName(dataBaseName);
    factory.setLocalTransactions(localTransactions);

    return factory.createProcessEngine();
  }
}

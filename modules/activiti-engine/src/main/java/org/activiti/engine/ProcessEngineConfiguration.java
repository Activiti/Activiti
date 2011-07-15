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

import java.io.InputStream;

import javax.sql.DataSource;

import org.activiti.engine.impl.cfg.BeansConfigurationHelper;
import org.activiti.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.activiti.engine.impl.cfg.StandaloneProcessEngineConfiguration;


/** Configuration information from which a process engine can be build.
 * 
 * <p>Most common is to create a process engine based on the default configuration file:
 * <pre>ProcessEngine processEngine = ProcessEngineConfiguration
 *   .createProcessEngineConfigurationFromResourceDefault()
 *   .buildProcessEngine();
 * </pre>
 * </p>
 * 
 * <p>To create a process engine programatic, without a configuration file, 
 * the first option is {@link #createStandaloneProcessEngineConfiguration()}
 * <pre>ProcessEngine processEngine = ProcessEngineConfiguration
 *   .createStandaloneProcessEngineConfiguration()
 *   .buildProcessEngine();
 * </pre>
 * This creates a new process engine with all the defaults to connect to 
 * a remote h2 database (jdbc:h2:tcp://localhost/activiti) in standalone 
 * mode.  Standalone mode means that Activiti will manage the transactions 
 * on the JDBC connections that it creates.  One transaction per 
 * service method.
 * For a description of how to write the configuration files, see the 
 * userguide.
 * </p>
 * 
 * <p>The second option is great for testing: {@link #createStandalonInMemeProcessEngineConfiguration()}
 * <pre>ProcessEngine processEngine = ProcessEngineConfiguration
 *   .createStandaloneInMemProcessEngineConfiguration()
 *   .buildProcessEngine();
 * </pre>
 * This creates a new process engine with all the defaults to connect to 
 * an memory h2 database (jdbc:h2:tcp://localhost/activiti) in standalone 
 * mode.  The DB schema strategy default is in this case <code>create-drop</code>.  
 * Standalone mode means that Activiti will manage the transactions 
 * on the JDBC connections that it creates.  One transaction per 
 * service method.
 * </p>
 * 
 * <p>On all forms of creating a process engine, you can first customize the configuration 
 * before calling the {@link #buildProcessEngine()} method by calling any of the 
 * setters like this:
 * <pre>ProcessEngine processEngine = ProcessEngineConfiguration
 *   .createProcessEngineConfigurationFromResourceDefault()
 *   .setMailServerHost("gmail.com")
 *   .setJdbcUsername("mickey")
 *   .setJdbcPassword("mouse")
 *   .buildProcessEngine();
 * </pre>
 * </p>
 * 
 * @see ProcessEngines 
 * @author Tom Baeyens
 */
public abstract class ProcessEngineConfiguration {
  
  /** Checks the version of the DB schema against the library when 
   * the process engine is being created and throws an exception
   * if the versions don't match. */
  public static final String DB_SCHEMA_UPDATE_FALSE = "false";
  
  /** Creates the schema when the process engine is being created and 
   * drops the schema when the process engine is being closed. */
  public static final String DB_SCHEMA_UPDATE_CREATE_DROP = "create-drop";

  /** Upon building of the process engine, a check is performed and 
   * an update of the schema is performed if it is necessary. */
  public static final String DB_SCHEMA_UPDATE_TRUE = "true";

  /** Value for {@link #setHistory(String)} to ensure that no history is being recorded. */
  public static final String HISTORY_NONE = "none";
  /** Value for {@link #setHistory(String)} to ensure that only historic process instances and 
   * historic activity instances are being recorded. 
   * This means no details for those entities. */
  public static final String HISTORY_ACTIVITY = "activity";
  /** Value for {@link #setHistory(String)} to ensure that only historic process instances, 
   * historic activity instances and submitted form property values are being recorded. */ 
  public static final String HISTORY_AUDIT = "audit";
  /** Value for {@link #setHistory(String)} to ensure that all historic information is 
   * being recorded, including the variable updates. */ 
  public static final String HISTORY_FULL = "full";
  
  protected String processEngineName = ProcessEngines.NAME_DEFAULT;
  protected int idBlockSize = 100;
  protected String history = HISTORY_AUDIT;
  protected boolean jobExecutorActivate;

  protected String mailServerHost = "localhost";
  protected String mailServerUsername; // by default no name and password are provided, which 
  protected String mailServerPassword; // means no authentication for mail server
  protected int mailServerPort = 25;
  protected String mailServerDefaultFrom = "activiti@localhost";

  protected String databaseType;
  protected String databaseSchemaUpdate = DB_SCHEMA_UPDATE_FALSE;
  protected String jdbcDriver = "org.h2.Driver";
  protected String jdbcUrl = "jdbc:h2:tcp://localhost/activiti";
  protected String jdbcUsername = "sa";
  protected String jdbcPassword = "";
  protected String dataSourceJndiName = null;
  protected int jdbcMaxActiveConnections;
  protected int jdbcMaxIdleConnections;
  protected int jdbcMaxCheckoutTime;
  protected int jdbcMaxWaitTime;
  protected boolean jdbcPingEnabled = false;
  protected String jdbcPingQuery = null;
  protected int jdbcPingConnectionNotUsedFor;
  protected DataSource dataSource;
  protected boolean transactionsExternallyManaged = false;
  
  protected String jpaPersistenceUnitName;
  protected Object jpaEntityManagerFactory;
  protected boolean jpaHandleTransaction;
  protected boolean jpaCloseEntityManager;
  
  protected ClassLoader classLoader;

  /** use one of the static createXxxx methods instead */
  protected ProcessEngineConfiguration() {
  }

  public abstract ProcessEngine buildProcessEngine();
  
  public static ProcessEngineConfiguration createProcessEngineConfigurationFromResourceDefault() {
    return createProcessEngineConfigurationFromResource("activiti.cfg.xml", "processEngineConfiguration");
  }

  public static ProcessEngineConfiguration createProcessEngineConfigurationFromResource(String resource) {
    return createProcessEngineConfigurationFromResource(resource, "processEngineConfiguration");
  }

  public static ProcessEngineConfiguration createProcessEngineConfigurationFromResource(String resource, String beanName) {
    return BeansConfigurationHelper.parseProcessEngineConfigurationFromResource(resource, beanName);
  }
  
  public static ProcessEngineConfiguration createProcessEngineConfigurationFromInputStream(InputStream inputStream) {
    return createProcessEngineConfigurationFromInputStream(inputStream, "processEngineConfiguration");
  }

  public static ProcessEngineConfiguration createProcessEngineConfigurationFromInputStream(InputStream inputStream, String beanName) {
    return BeansConfigurationHelper.parseProcessEngineConfigurationFromInputStream(inputStream, beanName);
  }

  public static ProcessEngineConfiguration createStandaloneProcessEngineConfiguration() {
    return new StandaloneProcessEngineConfiguration();
  }

  public static ProcessEngineConfiguration createStandaloneInMemProcessEngineConfiguration() {
    return new StandaloneInMemProcessEngineConfiguration();
  }

// TODO add later when we have test coverage for this
//  public static ProcessEngineConfiguration createJtaProcessEngineConfiguration() {
//    return new JtaProcessEngineConfiguration();
//  }
  

  // getters and setters //////////////////////////////////////////////////////
  
  public String getProcessEngineName() {
    return processEngineName;
  }

  public ProcessEngineConfiguration setProcessEngineName(String processEngineName) {
    this.processEngineName = processEngineName;
    return this;
  }

  
  public int getIdBlockSize() {
    return idBlockSize;
  }

  
  public ProcessEngineConfiguration setIdBlockSize(int idBlockSize) {
    this.idBlockSize = idBlockSize;
    return this;
  }

  
  public String getHistory() {
    return history;
  }

  
  public ProcessEngineConfiguration setHistory(String history) {
    this.history = history;
    return this;
  }

  
  public String getMailServerHost() {
    return mailServerHost;
  }

  
  public ProcessEngineConfiguration setMailServerHost(String mailServerHost) {
    this.mailServerHost = mailServerHost;
    return this;
  }

  
  public String getMailServerUsername() {
    return mailServerUsername;
  }

  
  public ProcessEngineConfiguration setMailServerUsername(String mailServerUsername) {
    this.mailServerUsername = mailServerUsername;
    return this;
  }

  
  public String getMailServerPassword() {
    return mailServerPassword;
  }

  
  public ProcessEngineConfiguration setMailServerPassword(String mailServerPassword) {
    this.mailServerPassword = mailServerPassword;
    return this;
  }

  
  public int getMailServerPort() {
    return mailServerPort;
  }

  
  public ProcessEngineConfiguration setMailServerPort(int mailServerPort) {
    this.mailServerPort = mailServerPort;
    return this;
  }

  
  public String getMailServerDefaultFrom() {
    return mailServerDefaultFrom;
  }

  
  public ProcessEngineConfiguration setMailServerDefaultFrom(String mailServerDefaultFrom) {
    this.mailServerDefaultFrom = mailServerDefaultFrom;
    return this;
  }

  
  public String getDatabaseType() {
    return databaseType;
  }

  
  public ProcessEngineConfiguration setDatabaseType(String databaseType) {
    this.databaseType = databaseType;
    return this;
  }

  
  public String getDatabaseSchemaUpdate() {
    return databaseSchemaUpdate;
  }

  
  public ProcessEngineConfiguration setDatabaseSchemaUpdate(String databaseSchemaUpdate) {
    this.databaseSchemaUpdate = databaseSchemaUpdate;
    return this;
  }

  
  public DataSource getDataSource() {
    return dataSource;
  }

  
  public ProcessEngineConfiguration setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
    return this;
  }

  
  public String getJdbcDriver() {
    return jdbcDriver;
  }

  
  public ProcessEngineConfiguration setJdbcDriver(String jdbcDriver) {
    this.jdbcDriver = jdbcDriver;
    return this;
  }

  
  public String getJdbcUrl() {
    return jdbcUrl;
  }

  
  public ProcessEngineConfiguration setJdbcUrl(String jdbcUrl) {
    this.jdbcUrl = jdbcUrl;
    return this;
  }

  
  public String getJdbcUsername() {
    return jdbcUsername;
  }

  
  public ProcessEngineConfiguration setJdbcUsername(String jdbcUsername) {
    this.jdbcUsername = jdbcUsername;
    return this;
  }

  
  public String getJdbcPassword() {
    return jdbcPassword;
  }

  
  public ProcessEngineConfiguration setJdbcPassword(String jdbcPassword) {
    this.jdbcPassword = jdbcPassword;
    return this;
  }

  
  public boolean isTransactionsExternallyManaged() {
    return transactionsExternallyManaged;
  }

  
  public ProcessEngineConfiguration setTransactionsExternallyManaged(boolean transactionsExternallyManaged) {
    this.transactionsExternallyManaged = transactionsExternallyManaged;
    return this;
  }

  
  public int getJdbcMaxActiveConnections() {
    return jdbcMaxActiveConnections;
  }

  
  public ProcessEngineConfiguration setJdbcMaxActiveConnections(int jdbcMaxActiveConnections) {
    this.jdbcMaxActiveConnections = jdbcMaxActiveConnections;
    return this;
  }

  
  public int getJdbcMaxIdleConnections() {
    return jdbcMaxIdleConnections;
  }

  
  public ProcessEngineConfiguration setJdbcMaxIdleConnections(int jdbcMaxIdleConnections) {
    this.jdbcMaxIdleConnections = jdbcMaxIdleConnections;
    return this;
  }

  
  public int getJdbcMaxCheckoutTime() {
    return jdbcMaxCheckoutTime;
  }

  
  public ProcessEngineConfiguration setJdbcMaxCheckoutTime(int jdbcMaxCheckoutTime) {
    this.jdbcMaxCheckoutTime = jdbcMaxCheckoutTime;
    return this;
  }

  
  public int getJdbcMaxWaitTime() {
    return jdbcMaxWaitTime;
  }
  
  public ProcessEngineConfiguration setJdbcMaxWaitTime(int jdbcMaxWaitTime) {
    this.jdbcMaxWaitTime = jdbcMaxWaitTime;
    return this;
  }
  
  public boolean isJdbcPingEnabled() {
    return jdbcPingEnabled;
  }

  public ProcessEngineConfiguration setJdbcPingEnabled(boolean jdbcPingEnabled) {
    this.jdbcPingEnabled = jdbcPingEnabled;
    return this;
  }

  public String getJdbcPingQuery() {
      return jdbcPingQuery;
  }

  public ProcessEngineConfiguration setJdbcPingQuery(String jdbcPingQuery) {
    this.jdbcPingQuery = jdbcPingQuery;
    return this;
  }

  public int getJdbcPingConnectionNotUsedFor() {
      return jdbcPingConnectionNotUsedFor;
  }

  public ProcessEngineConfiguration setJdbcPingConnectionNotUsedFor(int jdbcPingNotUsedFor) {
    this.jdbcPingConnectionNotUsedFor = jdbcPingNotUsedFor;
    return this;
  }

  public boolean isJobExecutorActivate() {
    return jobExecutorActivate;
  }

  
  public ProcessEngineConfiguration setJobExecutorActivate(boolean jobExecutorActivate) {
    this.jobExecutorActivate = jobExecutorActivate;
    return this;
  }
  
  public ClassLoader getClassLoader() {
    return classLoader;
  }
  
  public ProcessEngineConfiguration setClassLoader(ClassLoader classLoader) {
    this.classLoader = classLoader;
    return this;
  }

  
  public Object getJpaEntityManagerFactory() {
    return jpaEntityManagerFactory;
  }

  
  public ProcessEngineConfiguration setJpaEntityManagerFactory(Object jpaEntityManagerFactory) {
    this.jpaEntityManagerFactory = jpaEntityManagerFactory;
    return this;
  }

  
  public boolean isJpaHandleTransaction() {
    return jpaHandleTransaction;
  }

  
  public ProcessEngineConfiguration setJpaHandleTransaction(boolean jpaHandleTransaction) {
    this.jpaHandleTransaction = jpaHandleTransaction;
    return this;
  }

  
  public boolean isJpaCloseEntityManager() {
    return jpaCloseEntityManager;
  }

  
  public ProcessEngineConfiguration setJpaCloseEntityManager(boolean jpaCloseEntityManager) {
    this.jpaCloseEntityManager = jpaCloseEntityManager;
    return this;
  }

  public String getJpaPersistenceUnitName() {
    return jpaPersistenceUnitName;
  }

  public void setJpaPersistenceUnitName(String jpaPersistenceUnitName) {
    this.jpaPersistenceUnitName = jpaPersistenceUnitName;
  }

  public String getDataSourceJndiName() {
    return dataSourceJndiName;
  }

  public void setDataSourceJndiName(String dataSourceJndiName) {
    this.dataSourceJndiName = dataSourceJndiName;
  }
}

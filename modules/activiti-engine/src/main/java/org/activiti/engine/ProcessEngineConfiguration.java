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

import org.activiti.engine.impl.cfg.JtaProcessEngineConfiguration;
import org.activiti.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;


/**
 * @author Tom Baeyens
 */
public abstract class ProcessEngineConfiguration {
  
  /** checks the version of the DB schema against the library when 
   * the process engine is being created and throws an exception
   * if the versions don't match.
   */
  public static final String DB_SCHEMA_STRATEGY_CHECK_VERSION = "check-version";
  
  /** creates the schema when the process engine is being created and 
   * drops the schema when the process engine is being closed.
   */
  public static final String DB_SCHEMA_STRATEGY_CREATE_DROP = "create-drop";

  public static final int HISTORYLEVEL_NONE = 0;
  public static final int HISTORYLEVEL_ACTIVITY = 1;
  public static final int HISTORYLEVEL_AUDIT = 2;
  public static final int HISTORYLEVEL_FULL = 3;
  
  protected String processEngineName = ProcessEngines.NAME_DEFAULT;
  protected int idBlockSize = 100;
  protected int historyLevel = HISTORYLEVEL_AUDIT;
  protected boolean jobExecutorActivate;

  protected String mailServerHost = "localhost";
  protected String mailServerUsername = "activiti";
  protected String mailServerPassword = "activiti";
  protected int mailServerPort = 25;
  protected String mailServerDefaultFrom = "activiti@localhost";

  protected String databaseType = "h2";
  protected String dbSchemaStrategy = DB_SCHEMA_STRATEGY_CHECK_VERSION;
  protected String jdbcDriver = "org.h2.Driver";
  protected String jdbcUrl = "jdbc:h2:tcp://localhost/activiti";
  protected String jdbcUsername = "sa";
  protected String jdbcPassword = "";
  protected int jdbcMaxActiveConnections;
  protected int jdbcMaxIdleConnections;
  protected int jdbcMaxCheckoutTime;
  protected int jdbcMaxWaitTime;
  protected DataSource dataSource;
  protected boolean transactionsExternallyManaged = false;
  
  protected String jpaPersistenceUnitName;
  protected Object jpaEntityManagerFactory;
  protected boolean jpaHandleTransaction;
  protected boolean jpaCloseEntityManager;
  
  protected ClassLoader classLoader;

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
    Resource springResource = new ClassPathResource(resource);
    return parseProcessEngineConfiguration(springResource, beanName);
  }
  
  public static ProcessEngineConfiguration createProcessEngineConfigurationFromInputStream(InputStream inputStream) {
    return createProcessEngineConfigurationFromInputStream(inputStream, "processEngineConfiguration");
  }

  public static ProcessEngineConfiguration createProcessEngineConfigurationFromInputStream(InputStream inputStream, String beanName) {
    Resource springResource = new InputStreamResource(inputStream);
    return parseProcessEngineConfiguration(springResource, beanName);
  }

  protected static ProcessEngineConfiguration parseProcessEngineConfiguration(Resource springResource, String beanName) {
    DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
    XmlBeanDefinitionReader xmlBeanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);
    xmlBeanDefinitionReader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_XSD);
    xmlBeanDefinitionReader.loadBeanDefinitions(springResource);
    return (ProcessEngineConfiguration) beanFactory.getBean(beanName);
  }
  
  public static ProcessEngineConfiguration createStandaloneProcessEngineConfiguration() {
    return new StandaloneProcessEngineConfiguration();
  }

  public static ProcessEngineConfiguration createJtaProcessEngineConfiguration() {
    return new JtaProcessEngineConfiguration();
  }

  

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

  
  public int getHistoryLevel() {
    return historyLevel;
  }

  
  public ProcessEngineConfiguration setHistoryLevel(int historyLevel) {
    this.historyLevel = historyLevel;
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

  
  public String getDbSchemaStrategy() {
    return dbSchemaStrategy;
  }

  
  public ProcessEngineConfiguration setDbSchemaStrategy(String dbSchemaStrategy) {
    this.dbSchemaStrategy = dbSchemaStrategy;
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
}

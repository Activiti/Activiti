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
package org.activiti.form.engine;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.activiti.editor.form.converter.FormJsonConverter;
import org.activiti.form.api.FormRepositoryService;
import org.activiti.form.api.FormService;
import org.activiti.form.engine.impl.FormEngineImpl;
import org.activiti.form.engine.impl.FormRepositoryServiceImpl;
import org.activiti.form.engine.impl.FormServiceImpl;
import org.activiti.form.engine.impl.ServiceImpl;
import org.activiti.form.engine.impl.cfg.CommandExecutorImpl;
import org.activiti.form.engine.impl.cfg.IdGenerator;
import org.activiti.form.engine.impl.cfg.StandaloneFormEngineConfiguration;
import org.activiti.form.engine.impl.cfg.StandaloneInMemFormEngineConfiguration;
import org.activiti.form.engine.impl.cfg.TransactionContextFactory;
import org.activiti.form.engine.impl.cfg.standalone.StandaloneMybatisTransactionContextFactory;
import org.activiti.form.engine.impl.db.DbSqlSessionFactory;
import org.activiti.form.engine.impl.deployer.CachingAndArtifactsManager;
import org.activiti.form.engine.impl.deployer.FormDeployer;
import org.activiti.form.engine.impl.deployer.FormDeploymentHelper;
import org.activiti.form.engine.impl.deployer.ParsedDeploymentBuilderFactory;
import org.activiti.form.engine.impl.el.ExpressionManager;
import org.activiti.form.engine.impl.interceptor.CommandConfig;
import org.activiti.form.engine.impl.interceptor.CommandContextFactory;
import org.activiti.form.engine.impl.interceptor.CommandContextInterceptor;
import org.activiti.form.engine.impl.interceptor.CommandExecutor;
import org.activiti.form.engine.impl.interceptor.CommandInterceptor;
import org.activiti.form.engine.impl.interceptor.CommandInvoker;
import org.activiti.form.engine.impl.interceptor.LogInterceptor;
import org.activiti.form.engine.impl.interceptor.SessionFactory;
import org.activiti.form.engine.impl.parser.FormParseFactory;
import org.activiti.form.engine.impl.persistence.StrongUuidGenerator;
import org.activiti.form.engine.impl.persistence.deploy.DefaultDeploymentCache;
import org.activiti.form.engine.impl.persistence.deploy.Deployer;
import org.activiti.form.engine.impl.persistence.deploy.DeploymentCache;
import org.activiti.form.engine.impl.persistence.deploy.DeploymentManager;
import org.activiti.form.engine.impl.persistence.deploy.FormCacheEntry;
import org.activiti.form.engine.impl.persistence.entity.FormDeploymentEntityManager;
import org.activiti.form.engine.impl.persistence.entity.FormDeploymentEntityManagerImpl;
import org.activiti.form.engine.impl.persistence.entity.FormEntityManager;
import org.activiti.form.engine.impl.persistence.entity.FormEntityManagerImpl;
import org.activiti.form.engine.impl.persistence.entity.ResourceEntityManager;
import org.activiti.form.engine.impl.persistence.entity.ResourceEntityManagerImpl;
import org.activiti.form.engine.impl.persistence.entity.SubmittedFormEntityManager;
import org.activiti.form.engine.impl.persistence.entity.SubmittedFormEntityManagerImpl;
import org.activiti.form.engine.impl.persistence.entity.data.FormDataManager;
import org.activiti.form.engine.impl.persistence.entity.data.FormDeploymentDataManager;
import org.activiti.form.engine.impl.persistence.entity.data.ResourceDataManager;
import org.activiti.form.engine.impl.persistence.entity.data.SubmittedFormDataManager;
import org.activiti.form.engine.impl.persistence.entity.data.impl.MybatisFormDataManager;
import org.activiti.form.engine.impl.persistence.entity.data.impl.MybatisFormDeploymentDataManager;
import org.activiti.form.engine.impl.persistence.entity.data.impl.MybatisResourceDataManager;
import org.activiti.form.engine.impl.persistence.entity.data.impl.MybatisSubmittedFormDataManager;
import org.activiti.form.engine.impl.util.DefaultClockImpl;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.ibatis.transaction.managed.ManagedTransactionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

import com.fasterxml.jackson.databind.ObjectMapper;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

public class FormEngineConfiguration {

  protected static final Logger logger = LoggerFactory.getLogger(FormEngineConfiguration.class);

  /** The tenant id indicating 'no tenant' */
  public static final String NO_TENANT_ID = "";

  public static final String DEFAULT_MYBATIS_MAPPING_FILE = "org/activiti/form/db/mapping/mappings.xml";
  
  public static final String LIQUIBASE_CHANGELOG_PREFIX = "ACT_FO_";

  /**
   * Checks the version of the DB schema against the library when the form engine is being created and throws an exception if the versions don't match.
   */
  public static final String DB_SCHEMA_UPDATE_FALSE = "false";

  /**
   * Creates the schema when the form engine is being created and drops the schema when the form engine is being closed.
   */
  public static final String DB_SCHEMA_UPDATE_DROP_CREATE = "create-drop";

  /**
   * Upon building of the process engine, a check is performed and an update of the schema is performed if it is necessary.
   */
  public static final String DB_SCHEMA_UPDATE_TRUE = "true";

  protected String formEngineName = FormEngines.NAME_DEFAULT;

  protected String databaseType;
  protected String jdbcDriver = "org.h2.Driver";
  protected String jdbcUrl = "jdbc:h2:tcp://localhost/~/activitiform";
  protected String jdbcUsername = "sa";
  protected String jdbcPassword = "";
  protected String dataSourceJndiName;
  protected int jdbcMaxActiveConnections;
  protected int jdbcMaxIdleConnections;
  protected int jdbcMaxCheckoutTime;
  protected int jdbcMaxWaitTime;
  protected boolean jdbcPingEnabled;
  protected String jdbcPingQuery;
  protected int jdbcPingConnectionNotUsedFor;
  protected int jdbcDefaultTransactionIsolationLevel;
  protected DataSource dataSource;
  
  protected String databaseSchemaUpdate = DB_SCHEMA_UPDATE_TRUE;

  protected String xmlEncoding = "UTF-8";

  protected BeanFactory beanFactory;

  // COMMAND EXECUTORS ///////////////////////////////////////////////

  protected CommandConfig defaultCommandConfig;
  protected CommandConfig schemaCommandConfig;

  protected CommandInterceptor commandInvoker;

  /**
   * the configurable list which will be {@link #initInterceptorChain(java.util.List) processed} to build the {@link #commandExecutor}
   */
  protected List<CommandInterceptor> customPreCommandInterceptors;
  protected List<CommandInterceptor> customPostCommandInterceptors;

  protected List<CommandInterceptor> commandInterceptors;

  /** this will be initialized during the configurationComplete() */
  protected CommandExecutor commandExecutor;

  // SERVICES
  // /////////////////////////////////////////////////////////////////

  protected FormRepositoryService repositoryService = new FormRepositoryServiceImpl();
  protected FormService formService = new FormServiceImpl();

  // DATA MANAGERS ///////////////////////////////////////////////////

  protected FormDeploymentDataManager deploymentDataManager;
  protected FormDataManager formDataManager;
  protected ResourceDataManager resourceDataManager;
  protected SubmittedFormDataManager submittedFormDataManager;

  // ENTITY MANAGERS /////////////////////////////////////////////////
  protected FormDeploymentEntityManager deploymentEntityManager;
  protected FormEntityManager formEntityManager;
  protected ResourceEntityManager resourceEntityManager;
  protected SubmittedFormEntityManager submittedFormEntityManager;

  protected CommandContextFactory commandContextFactory;
  protected TransactionContextFactory transactionContextFactory;
  
  protected ExpressionManager expressionManager;
  
  protected FormJsonConverter formJsonConverter = new FormJsonConverter();

  // MYBATIS SQL SESSION FACTORY /////////////////////////////////////

  protected SqlSessionFactory sqlSessionFactory;
  protected TransactionFactory transactionFactory;

  protected Set<Class<?>> customMybatisMappers;
  protected Set<String> customMybatisXMLMappers;

  // SESSION FACTORIES ///////////////////////////////////////////////
  protected List<SessionFactory> customSessionFactories;
  protected DbSqlSessionFactory dbSqlSessionFactory;
  protected Map<Class<?>, SessionFactory> sessionFactories;
  
  protected ObjectMapper objectMapper = new ObjectMapper();

  protected boolean transactionsExternallyManaged;

  /**
   * Flag that can be set to configure or nota relational database is used. This is useful for custom implementations that do not use relational databases at all.
   * 
   * If true (default), the {@link ProcessEngineConfiguration#getDatabaseSchemaUpdate()} value will be used to determine what needs to happen wrt the database schema.
   * 
   * If false, no validation or schema creation will be done. That means that the database schema must have been created 'manually' before but the engine does not validate whether the schema is
   * correct. The {@link ProcessEngineConfiguration#getDatabaseSchemaUpdate()} value will not be used.
   */
  protected boolean usingRelationalDatabase = true;

  /**
   * Allows configuring a database table prefix which is used for all runtime operations of the process engine. For example, if you specify a prefix named 'PRE1.', activiti will query for executions
   * in a table named 'PRE1.ACT_RU_EXECUTION_'.
   * 
   * <p />
   * <strong>NOTE: the prefix is not respected by automatic database schema management. If you use {@link ProcessEngineConfiguration#DB_SCHEMA_UPDATE_CREATE_DROP} or
   * {@link ProcessEngineConfiguration#DB_SCHEMA_UPDATE_TRUE}, activiti will create the database tables using the default names, regardless of the prefix configured here.</strong>
   */
  protected String databaseTablePrefix = "";

  /**
   * database catalog to use
   */
  protected String databaseCatalog = "";

  /**
   * In some situations you want to set the schema to use for table checks / generation if the database metadata doesn't return that correctly, see https://jira.codehaus.org/browse/ACT-1220,
   * https://jira.codehaus.org/browse/ACT-1062
   */
  protected String databaseSchema;

  /**
   * Set to true in case the defined databaseTablePrefix is a schema-name, instead of an actual table name prefix. This is relevant for checking if Activiti-tables exist, the databaseTablePrefix will
   * not be used here - since the schema is taken into account already, adding a prefix for the table-check will result in wrong table-names.
   */
  protected boolean tablePrefixIsSchema;

  protected static Properties databaseTypeMappings = getDefaultDatabaseTypeMappings();

  public static final String DATABASE_TYPE_H2 = "h2";
  public static final String DATABASE_TYPE_HSQL = "hsql";
  public static final String DATABASE_TYPE_MYSQL = "mysql";
  public static final String DATABASE_TYPE_ORACLE = "oracle";
  public static final String DATABASE_TYPE_POSTGRES = "postgres";
  public static final String DATABASE_TYPE_MSSQL = "mssql";
  public static final String DATABASE_TYPE_DB2 = "db2";

  public static Properties getDefaultDatabaseTypeMappings() {
    Properties databaseTypeMappings = new Properties();
    databaseTypeMappings.setProperty("H2", DATABASE_TYPE_H2);
    databaseTypeMappings.setProperty("HSQL Database Engine", DATABASE_TYPE_HSQL);
    databaseTypeMappings.setProperty("MySQL", DATABASE_TYPE_MYSQL);
    databaseTypeMappings.setProperty("Oracle", DATABASE_TYPE_ORACLE);
    databaseTypeMappings.setProperty("PostgreSQL", DATABASE_TYPE_POSTGRES);
    databaseTypeMappings.setProperty("Microsoft SQL Server", DATABASE_TYPE_MSSQL);
    databaseTypeMappings.setProperty(DATABASE_TYPE_DB2, DATABASE_TYPE_DB2);
    databaseTypeMappings.setProperty("DB2", DATABASE_TYPE_DB2);
    databaseTypeMappings.setProperty("DB2/NT", DATABASE_TYPE_DB2);
    databaseTypeMappings.setProperty("DB2/NT64", DATABASE_TYPE_DB2);
    databaseTypeMappings.setProperty("DB2 UDP", DATABASE_TYPE_DB2);
    databaseTypeMappings.setProperty("DB2/LINUX", DATABASE_TYPE_DB2);
    databaseTypeMappings.setProperty("DB2/LINUX390", DATABASE_TYPE_DB2);
    databaseTypeMappings.setProperty("DB2/LINUXX8664", DATABASE_TYPE_DB2);
    databaseTypeMappings.setProperty("DB2/LINUXZ64", DATABASE_TYPE_DB2);
    databaseTypeMappings.setProperty("DB2/LINUXPPC64", DATABASE_TYPE_DB2);
    databaseTypeMappings.setProperty("DB2/400 SQL", DATABASE_TYPE_DB2);
    databaseTypeMappings.setProperty("DB2/6000", DATABASE_TYPE_DB2);
    databaseTypeMappings.setProperty("DB2 UDB iSeries", DATABASE_TYPE_DB2);
    databaseTypeMappings.setProperty("DB2/AIX64", DATABASE_TYPE_DB2);
    databaseTypeMappings.setProperty("DB2/HPUX", DATABASE_TYPE_DB2);
    databaseTypeMappings.setProperty("DB2/HP64", DATABASE_TYPE_DB2);
    databaseTypeMappings.setProperty("DB2/SUN", DATABASE_TYPE_DB2);
    databaseTypeMappings.setProperty("DB2/SUN64", DATABASE_TYPE_DB2);
    databaseTypeMappings.setProperty("DB2/PTX", DATABASE_TYPE_DB2);
    databaseTypeMappings.setProperty("DB2/2", DATABASE_TYPE_DB2);
    databaseTypeMappings.setProperty("DB2 UDB AS400", DATABASE_TYPE_DB2);
    return databaseTypeMappings;
  }

  // DEPLOYERS
  // ////////////////////////////////////////////////////////////////

  protected FormDeployer formDeployer;
  protected FormParseFactory formParseFactory;
  protected ParsedDeploymentBuilderFactory parsedDeploymentBuilderFactory;
  protected FormDeploymentHelper formDeploymentHelper;
  protected CachingAndArtifactsManager cachingAndArtifactsManager;
  protected List<Deployer> customPreDeployers;
  protected List<Deployer> customPostDeployers;
  protected List<Deployer> deployers;
  protected DeploymentManager deploymentManager;

  protected int formCacheLimit = -1; // By default, no limit
  protected DeploymentCache<FormCacheEntry> formCache;

  protected IdGenerator idGenerator;

  protected Clock clock;

  public static FormEngineConfiguration createFormEngineConfigurationFromResourceDefault() {
    return createFormEngineConfigurationFromResource("activiti.form.cfg.xml", "formEngineConfiguration");
  }

  public static FormEngineConfiguration createFormEngineConfigurationFromResource(String resource) {
    return createFormEngineConfigurationFromResource(resource, "formEngineConfiguration");
  }

  public static FormEngineConfiguration createFormEngineConfigurationFromResource(String resource, String beanName) {
    return parseFormEngineConfigurationFromResource(resource, beanName);
  }

  public static FormEngineConfiguration createFormEngineConfigurationFromInputStream(InputStream inputStream) {
    return createFormEngineConfigurationFromInputStream(inputStream, "formEngineConfiguration");
  }

  public static FormEngineConfiguration createFormEngineConfigurationFromInputStream(InputStream inputStream, String beanName) {
    return parseFormEngineConfigurationFromInputStream(inputStream, beanName);
  }

  public static FormEngineConfiguration createStandaloneFormEngineConfiguration() {
    return new StandaloneFormEngineConfiguration();
  }

  public static FormEngineConfiguration createStandaloneInMemFormEngineConfiguration() {
    return new StandaloneInMemFormEngineConfiguration();
  }

  public static FormEngineConfiguration parseFormEngineConfiguration(Resource springResource, String beanName) {
    DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
    XmlBeanDefinitionReader xmlBeanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);
    xmlBeanDefinitionReader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_XSD);
    xmlBeanDefinitionReader.loadBeanDefinitions(springResource);
    FormEngineConfiguration formEngineConfiguration = (FormEngineConfiguration) beanFactory.getBean(beanName);
    formEngineConfiguration.setBeanFactory(beanFactory);
    return formEngineConfiguration;
  }

  public static FormEngineConfiguration parseFormEngineConfigurationFromInputStream(InputStream inputStream, String beanName) {
    Resource springResource = new InputStreamResource(inputStream);
    return parseFormEngineConfiguration(springResource, beanName);
  }

  public static FormEngineConfiguration parseFormEngineConfigurationFromResource(String resource, String beanName) {
    Resource springResource = new ClassPathResource(resource);
    return parseFormEngineConfiguration(springResource, beanName);
  }

  // buildProcessEngine
  // ///////////////////////////////////////////////////////

  public FormEngine buildFormEngine() {
    init();
    return new FormEngineImpl(this);
  }

  // init
  // /////////////////////////////////////////////////////////////////////

  protected void init() {
    initExpressionManager();
    initCommandContextFactory();
    initTransactionContextFactory();
    initCommandExecutors();
    initIdGenerator();
    
    if (usingRelationalDatabase) {
      initDataSource();
      initDbSchema();
    }
    
    initTransactionFactory();
    initSqlSessionFactory();
    initSessionFactories();
    initServices();
    initDataManagers();
    initEntityManagers();
    initDeployers();
    initClock();
  }

  // services
  // /////////////////////////////////////////////////////////////////

  protected void initServices() {
    initService(repositoryService);
    initService(formService);
  }

  protected void initService(Object service) {
    if (service instanceof ServiceImpl) {
      ((ServiceImpl) service).setCommandExecutor(commandExecutor);
    }
  }
  
  public void initExpressionManager() {
    if (expressionManager == null) {
      expressionManager = new ExpressionManager();
    }
  }

  // Data managers
  ///////////////////////////////////////////////////////////

  public void initDataManagers() {
    if (deploymentDataManager == null) {
      deploymentDataManager = new MybatisFormDeploymentDataManager(this);
    }
    if (formDataManager == null) {
      formDataManager = new MybatisFormDataManager(this);
    }
    if (resourceDataManager == null) {
      resourceDataManager = new MybatisResourceDataManager(this);
    }
    if (submittedFormDataManager == null) {
      submittedFormDataManager = new MybatisSubmittedFormDataManager(this);
    }
  }

  public void initEntityManagers() {
    if (deploymentEntityManager == null) {
      deploymentEntityManager = new FormDeploymentEntityManagerImpl(this, deploymentDataManager);
    }
    if (formEntityManager == null) {
      formEntityManager = new FormEntityManagerImpl(this, formDataManager);
    }
    if (resourceEntityManager == null) {
      resourceEntityManager = new ResourceEntityManagerImpl(this, resourceDataManager);
    }
    if (submittedFormEntityManager == null) {
      submittedFormEntityManager = new SubmittedFormEntityManagerImpl(this, submittedFormDataManager);
    }
  }

  // DataSource
  // ///////////////////////////////////////////////////////////////

  protected void initDataSource() {
    if (dataSource == null) {
      if (dataSourceJndiName != null) {
        try {
          dataSource = (DataSource) new InitialContext().lookup(dataSourceJndiName);
        } catch (Exception e) {
          throw new ActivitiFormException("couldn't lookup datasource from " + dataSourceJndiName + ": " + e.getMessage(), e);
        }

      } else if (jdbcUrl != null) {
        if ((jdbcDriver == null) || (jdbcUsername == null)) {
          throw new ActivitiFormException("DataSource or JDBC properties have to be specified in a process engine configuration");
        }

        logger.debug("initializing datasource to db: {}", jdbcUrl);

        if (logger.isInfoEnabled()) {
          logger.info("Configuring Datasource with following properties (omitted password for security)");
          logger.info("datasource driver: " + jdbcDriver);
          logger.info("datasource url : " + jdbcUrl);
          logger.info("datasource user name : " + jdbcUsername);
        }

        PooledDataSource pooledDataSource = new PooledDataSource(this.getClass().getClassLoader(), jdbcDriver, jdbcUrl, jdbcUsername, jdbcPassword);

        if (jdbcMaxActiveConnections > 0) {
          pooledDataSource.setPoolMaximumActiveConnections(jdbcMaxActiveConnections);
        }
        if (jdbcMaxIdleConnections > 0) {
          pooledDataSource.setPoolMaximumIdleConnections(jdbcMaxIdleConnections);
        }
        if (jdbcMaxCheckoutTime > 0) {
          pooledDataSource.setPoolMaximumCheckoutTime(jdbcMaxCheckoutTime);
        }
        if (jdbcMaxWaitTime > 0) {
          pooledDataSource.setPoolTimeToWait(jdbcMaxWaitTime);
        }
        if (jdbcPingEnabled == true) {
          pooledDataSource.setPoolPingEnabled(true);
          if (jdbcPingQuery != null) {
            pooledDataSource.setPoolPingQuery(jdbcPingQuery);
          }
          pooledDataSource.setPoolPingConnectionsNotUsedFor(jdbcPingConnectionNotUsedFor);
        }
        if (jdbcDefaultTransactionIsolationLevel > 0) {
          pooledDataSource.setDefaultTransactionIsolationLevel(jdbcDefaultTransactionIsolationLevel);
        }
        dataSource = pooledDataSource;
      }
      
      if (dataSource instanceof PooledDataSource) {
        // ACT-233: connection pool of Ibatis is not properly
        // initialized if this is not called!
        ((PooledDataSource) dataSource).forceCloseAll();
      }
    }
    
    if (databaseType == null) {
      initDatabaseType();
    }
  }
  
  public void initDatabaseType() {
    Connection connection = null;
    try {
      connection = dataSource.getConnection();
      DatabaseMetaData databaseMetaData = connection.getMetaData();
      String databaseProductName = databaseMetaData.getDatabaseProductName();
      logger.debug("database product name: '{}'", databaseProductName);
      databaseType = databaseTypeMappings.getProperty(databaseProductName);
      if (databaseType == null) {
        throw new ActivitiFormException("couldn't deduct database type from database product name '" + databaseProductName + "'");
      }
      logger.debug("using database type: {}", databaseType);

    } catch (SQLException e) {
      logger.error("Exception while initializing Database connection", e);
    } finally {
      try {
        if (connection != null) {
          connection.close();
        }
      } catch (SQLException e) {
        logger.error("Exception while closing the Database connection", e);
      }
    }
  }
  
  // data model ///////////////////////////////////////////////////////////////

  public void initDbSchema() {
    try {
      DatabaseConnection connection = new JdbcConnection(dataSource.getConnection());
      Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);
      database.setDatabaseChangeLogTableName(LIQUIBASE_CHANGELOG_PREFIX+database.getDatabaseChangeLogTableName());
      database.setDatabaseChangeLogLockTableName(LIQUIBASE_CHANGELOG_PREFIX+database.getDatabaseChangeLogLockTableName());
      
      if (StringUtils.isNotEmpty(databaseSchema)) {
        database.setDefaultSchemaName(databaseSchema);
        database.setLiquibaseSchemaName(databaseSchema);
      }
      
      if (StringUtils.isNotEmpty(databaseCatalog)) {
        database.setDefaultCatalogName(databaseCatalog);
        database.setLiquibaseCatalogName(databaseCatalog);
      }

      Liquibase liquibase = new Liquibase("org/activiti/form/db/liquibase/activiti-form-db-changelog.xml", new ClassLoaderResourceAccessor(), database);

      if (DB_SCHEMA_UPDATE_DROP_CREATE.equals(databaseSchemaUpdate)) {
        logger.debug("Dropping and creating schema FORM");
        liquibase.dropAll();
        liquibase.update("form");
      } else if (DB_SCHEMA_UPDATE_TRUE.equals(databaseSchemaUpdate)) {
        logger.debug("Updating schema FORM");
        liquibase.update("form");
      } else if (DB_SCHEMA_UPDATE_FALSE.equals(databaseSchemaUpdate)) {
        logger.debug("Validating schema FORM");
        liquibase.validate();
      }
    } catch (Exception e) {
      throw new ActivitiFormException("Error initialising form data schema", e);
    }
  }

  // session factories ////////////////////////////////////////////////////////

  public void initSessionFactories() {
    if (sessionFactories == null) {
      sessionFactories = new HashMap<Class<?>, SessionFactory>();

      if (usingRelationalDatabase) {
        initDbSqlSessionFactory();
      }
    }

    if (customSessionFactories != null) {
      for (SessionFactory sessionFactory : customSessionFactories) {
        addSessionFactory(sessionFactory);
      }
    }
  }

  public void initDbSqlSessionFactory() {
    if (dbSqlSessionFactory == null) {
      dbSqlSessionFactory = createDbSqlSessionFactory();
    }
    dbSqlSessionFactory.setDatabaseType(databaseType);
    dbSqlSessionFactory.setSqlSessionFactory(sqlSessionFactory);
    dbSqlSessionFactory.setIdGenerator(idGenerator);
    dbSqlSessionFactory.setDatabaseTablePrefix(databaseTablePrefix);
    dbSqlSessionFactory.setTablePrefixIsSchema(tablePrefixIsSchema);
    dbSqlSessionFactory.setDatabaseCatalog(databaseCatalog);
    dbSqlSessionFactory.setDatabaseSchema(databaseSchema);
    addSessionFactory(dbSqlSessionFactory);
  }

  public DbSqlSessionFactory createDbSqlSessionFactory() {
    return new DbSqlSessionFactory();
  }

  public void addSessionFactory(SessionFactory sessionFactory) {
    sessionFactories.put(sessionFactory.getSessionType(), sessionFactory);
  }

  // command executors
  // ////////////////////////////////////////////////////////

  public void initCommandExecutors() {
    initDefaultCommandConfig();
    initSchemaCommandConfig();
    initCommandInvoker();
    initCommandInterceptors();
    initCommandExecutor();
  }

  public void initDefaultCommandConfig() {
    if (defaultCommandConfig == null) {
      defaultCommandConfig = new CommandConfig();
    }
  }

  public void initSchemaCommandConfig() {
    if (schemaCommandConfig == null) {
      schemaCommandConfig = new CommandConfig().transactionNotSupported();
    }
  }

  public void initCommandInvoker() {
    if (commandInvoker == null) {
      commandInvoker = new CommandInvoker();
    }
  }

  public void initCommandInterceptors() {
    if (commandInterceptors == null) {
      commandInterceptors = new ArrayList<CommandInterceptor>();
      if (customPreCommandInterceptors != null) {
        commandInterceptors.addAll(customPreCommandInterceptors);
      }
      commandInterceptors.addAll(getDefaultCommandInterceptors());
      if (customPostCommandInterceptors != null) {
        commandInterceptors.addAll(customPostCommandInterceptors);
      }
      commandInterceptors.add(commandInvoker);
    }
  }

  public Collection<? extends CommandInterceptor> getDefaultCommandInterceptors() {
    List<CommandInterceptor> interceptors = new ArrayList<CommandInterceptor>();
    interceptors.add(new LogInterceptor());

    CommandInterceptor transactionInterceptor = createTransactionInterceptor();
    if (transactionInterceptor != null) {
      interceptors.add(transactionInterceptor);
    }

    interceptors.add(new CommandContextInterceptor(commandContextFactory, this));
    return interceptors;
  }

  public void initCommandExecutor() {
    if (commandExecutor == null) {
      CommandInterceptor first = initInterceptorChain(commandInterceptors);
      commandExecutor = new CommandExecutorImpl(getDefaultCommandConfig(), first);
    }
  }

  public CommandInterceptor initInterceptorChain(List<CommandInterceptor> chain) {
    if (chain == null || chain.isEmpty()) {
      throw new ActivitiFormException("invalid command interceptor chain configuration: " + chain);
    }
    for (int i = 0; i < chain.size() - 1; i++) {
      chain.get(i).setNext(chain.get(i + 1));
    }
    return chain.get(0);
  }

  public CommandInterceptor createTransactionInterceptor() {
    return null;
  }

  // deployers
  // ////////////////////////////////////////////////////////////////

  protected void initDeployers() {
    if (formParseFactory == null) {
      formParseFactory = new FormParseFactory();
    }

    if (this.formDeployer == null) {
      this.deployers = new ArrayList<Deployer>();
      if (customPreDeployers != null) {
        this.deployers.addAll(customPreDeployers);
      }
      this.deployers.addAll(getDefaultDeployers());
      if (customPostDeployers != null) {
        this.deployers.addAll(customPostDeployers);
      }
    }

    // Decision cache
    if (formCache == null) {
      if (formCacheLimit <= 0) {
        formCache = new DefaultDeploymentCache<FormCacheEntry>();
      } else {
        formCache = new DefaultDeploymentCache<FormCacheEntry>(formCacheLimit);
      }
    }

    deploymentManager = new DeploymentManager(formCache, this);
    deploymentManager.setDeployers(deployers);
    deploymentManager.setDeploymentEntityManager(deploymentEntityManager);
    deploymentManager.setFormEntityManager(formEntityManager);
  }

  public Collection<? extends Deployer> getDefaultDeployers() {
    List<Deployer> defaultDeployers = new ArrayList<Deployer>();

    if (formDeployer == null) {
      formDeployer = new FormDeployer();
    }

    initDmnDeployerDependencies();

    formDeployer.setIdGenerator(idGenerator);
    formDeployer.setParsedDeploymentBuilderFactory(parsedDeploymentBuilderFactory);
    formDeployer.setFormDeploymentHelper(formDeploymentHelper);
    formDeployer.setCachingAndArtifactsManager(cachingAndArtifactsManager);

    defaultDeployers.add(formDeployer);
    return defaultDeployers;
  }

  public void initDmnDeployerDependencies() {
    if (parsedDeploymentBuilderFactory == null) {
      parsedDeploymentBuilderFactory = new ParsedDeploymentBuilderFactory();
    }
    if (parsedDeploymentBuilderFactory.getFormParseFactory() == null) {
      parsedDeploymentBuilderFactory.setFormParseFactory(formParseFactory);
    }

    if (formDeploymentHelper == null) {
      formDeploymentHelper = new FormDeploymentHelper();
    }

    if (cachingAndArtifactsManager == null) {
      cachingAndArtifactsManager = new CachingAndArtifactsManager();
    }
  }

  // id generator
  // /////////////////////////////////////////////////////////////

  public void initIdGenerator() {
    if (idGenerator == null) {
      idGenerator = new StrongUuidGenerator();
    }
  }

  // OTHER
  // ////////////////////////////////////////////////////////////////////

  public void initCommandContextFactory() {
    if (commandContextFactory == null) {
      commandContextFactory = new CommandContextFactory();
    }
    commandContextFactory.setDmnEngineConfiguration(this);
  }

  public void initTransactionContextFactory() {
    if (transactionContextFactory == null) {
      transactionContextFactory = new StandaloneMybatisTransactionContextFactory();
    }
  }

  public void initClock() {
    if (clock == null) {
      clock = new DefaultClockImpl();
    }
  }

  // myBatis SqlSessionFactory
  // ////////////////////////////////////////////////

  public void initTransactionFactory() {
    if (transactionFactory == null) {
      if (transactionsExternallyManaged) {
        transactionFactory = new ManagedTransactionFactory();
      } else {
        transactionFactory = new JdbcTransactionFactory();
      }
    }
  }

  public void initSqlSessionFactory() {
    if (sqlSessionFactory == null) {
      InputStream inputStream = null;
      try {
        inputStream = getMyBatisXmlConfigurationStream();

        Environment environment = new Environment("default", transactionFactory, dataSource);
        Reader reader = new InputStreamReader(inputStream);
        Properties properties = new Properties();
        properties.put("prefix", databaseTablePrefix);
        // set default properties
        properties.put("limitBefore", "");
        properties.put("limitAfter", "");
        properties.put("limitBetween", "");
        properties.put("limitOuterJoinBetween", "");
        properties.put("limitBeforeNativeQuery", "");
        properties.put("orderBy", "order by ${orderByColumns}");
        properties.put("blobType", "BLOB");
        properties.put("boolValue", "TRUE");

        if (databaseType != null) {
          properties.load(getResourceAsStream("org/activiti/form/db/properties/" + databaseType + ".properties"));
        }

        Configuration configuration = initMybatisConfiguration(environment, reader, properties);
        sqlSessionFactory = new DefaultSqlSessionFactory(configuration);

      } catch (Exception e) {
        throw new ActivitiFormException("Error while building ibatis SqlSessionFactory: " + e.getMessage(), e);
      } finally {
        IOUtils.closeQuietly(inputStream);
      }
    }
  }

  public Configuration initMybatisConfiguration(Environment environment, Reader reader, Properties properties) {
    XMLConfigBuilder parser = new XMLConfigBuilder(reader, "", properties);
    Configuration configuration = parser.getConfiguration();

    if (databaseType != null) {
      configuration.setDatabaseId(databaseType);
    }

    configuration.setEnvironment(environment);

    initCustomMybatisMappers(configuration);

    configuration = parseMybatisConfiguration(configuration, parser);
    return configuration;
  }

  public void initCustomMybatisMappers(Configuration configuration) {
    if (getCustomMybatisMappers() != null) {
      for (Class<?> clazz : getCustomMybatisMappers()) {
        configuration.addMapper(clazz);
      }
    }
  }

  public Configuration parseMybatisConfiguration(Configuration configuration, XMLConfigBuilder parser) {
    return parseCustomMybatisXMLMappers(parser.parse());
  }

  public Configuration parseCustomMybatisXMLMappers(Configuration configuration) {
    if (getCustomMybatisXMLMappers() != null)
      // see XMLConfigBuilder.mapperElement()
      for (String resource : getCustomMybatisXMLMappers()) {
      XMLMapperBuilder mapperParser = new XMLMapperBuilder(getResourceAsStream(resource), configuration, resource, configuration.getSqlFragments());
      mapperParser.parse();
      }
    return configuration;
  }

  protected InputStream getResourceAsStream(String resource) {
    return this.getClass().getClassLoader().getResourceAsStream(resource);
  }

  public InputStream getMyBatisXmlConfigurationStream() {
    return getResourceAsStream(DEFAULT_MYBATIS_MAPPING_FILE);
  }

  // getters and setters
  // //////////////////////////////////////////////////////

  public String getFormEngineName() {
    return formEngineName;
  }

  public FormEngineConfiguration setFormEngineName(String formEngineName) {
    this.formEngineName = formEngineName;
    return this;
  }

  public String getDatabaseType() {
    return databaseType;
  }

  public FormEngineConfiguration setDatabaseType(String databaseType) {
    this.databaseType = databaseType;
    return this;
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  public FormEngineConfiguration setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
    return this;
  }

  public String getJdbcDriver() {
    return jdbcDriver;
  }

  public FormEngineConfiguration setJdbcDriver(String jdbcDriver) {
    this.jdbcDriver = jdbcDriver;
    return this;
  }

  public String getJdbcUrl() {
    return jdbcUrl;
  }

  public FormEngineConfiguration setJdbcUrl(String jdbcUrl) {
    this.jdbcUrl = jdbcUrl;
    return this;
  }

  public String getJdbcUsername() {
    return jdbcUsername;
  }

  public FormEngineConfiguration setJdbcUsername(String jdbcUsername) {
    this.jdbcUsername = jdbcUsername;
    return this;
  }

  public String getJdbcPassword() {
    return jdbcPassword;
  }

  public FormEngineConfiguration setJdbcPassword(String jdbcPassword) {
    this.jdbcPassword = jdbcPassword;
    return this;
  }

  public int getJdbcMaxActiveConnections() {
    return jdbcMaxActiveConnections;
  }

  public FormEngineConfiguration setJdbcMaxActiveConnections(int jdbcMaxActiveConnections) {
    this.jdbcMaxActiveConnections = jdbcMaxActiveConnections;
    return this;
  }

  public int getJdbcMaxIdleConnections() {
    return jdbcMaxIdleConnections;
  }

  public FormEngineConfiguration setJdbcMaxIdleConnections(int jdbcMaxIdleConnections) {
    this.jdbcMaxIdleConnections = jdbcMaxIdleConnections;
    return this;
  }

  public int getJdbcMaxCheckoutTime() {
    return jdbcMaxCheckoutTime;
  }

  public FormEngineConfiguration setJdbcMaxCheckoutTime(int jdbcMaxCheckoutTime) {
    this.jdbcMaxCheckoutTime = jdbcMaxCheckoutTime;
    return this;
  }

  public int getJdbcMaxWaitTime() {
    return jdbcMaxWaitTime;
  }

  public FormEngineConfiguration setJdbcMaxWaitTime(int jdbcMaxWaitTime) {
    this.jdbcMaxWaitTime = jdbcMaxWaitTime;
    return this;
  }

  public boolean isJdbcPingEnabled() {
    return jdbcPingEnabled;
  }

  public FormEngineConfiguration setJdbcPingEnabled(boolean jdbcPingEnabled) {
    this.jdbcPingEnabled = jdbcPingEnabled;
    return this;
  }

  public int getJdbcPingConnectionNotUsedFor() {
    return jdbcPingConnectionNotUsedFor;
  }

  public FormEngineConfiguration setJdbcPingConnectionNotUsedFor(int jdbcPingConnectionNotUsedFor) {
    this.jdbcPingConnectionNotUsedFor = jdbcPingConnectionNotUsedFor;
    return this;
  }

  public int getJdbcDefaultTransactionIsolationLevel() {
    return jdbcDefaultTransactionIsolationLevel;
  }

  public FormEngineConfiguration setJdbcDefaultTransactionIsolationLevel(int jdbcDefaultTransactionIsolationLevel) {
    this.jdbcDefaultTransactionIsolationLevel = jdbcDefaultTransactionIsolationLevel;
    return this;
  }

  public String getJdbcPingQuery() {
    return jdbcPingQuery;
  }

  public FormEngineConfiguration setJdbcPingQuery(String jdbcPingQuery) {
    this.jdbcPingQuery = jdbcPingQuery;
    return this;
  }

  public String getDataSourceJndiName() {
    return dataSourceJndiName;
  }

  public FormEngineConfiguration setDataSourceJndiName(String dataSourceJndiName) {
    this.dataSourceJndiName = dataSourceJndiName;
    return this;
  }

  public String getXmlEncoding() {
    return xmlEncoding;
  }

  public FormEngineConfiguration setXmlEncoding(String xmlEncoding) {
    this.xmlEncoding = xmlEncoding;
    return this;
  }

  public BeanFactory getBeanFactory() {
    return beanFactory;
  }

  public FormEngineConfiguration setBeanFactory(BeanFactory beanFactory) {
    this.beanFactory = beanFactory;
    return this;
  }

  public CommandConfig getDefaultCommandConfig() {
    return defaultCommandConfig;
  }

  public FormEngineConfiguration setDefaultCommandConfig(CommandConfig defaultCommandConfig) {
    this.defaultCommandConfig = defaultCommandConfig;
    return this;
  }

  public CommandInterceptor getCommandInvoker() {
    return commandInvoker;
  }

  public FormEngineConfiguration setCommandInvoker(CommandInterceptor commandInvoker) {
    this.commandInvoker = commandInvoker;
    return this;
  }

  public List<CommandInterceptor> getCustomPreCommandInterceptors() {
    return customPreCommandInterceptors;
  }

  public FormEngineConfiguration setCustomPreCommandInterceptors(List<CommandInterceptor> customPreCommandInterceptors) {
    this.customPreCommandInterceptors = customPreCommandInterceptors;
    return this;
  }

  public List<CommandInterceptor> getCustomPostCommandInterceptors() {
    return customPostCommandInterceptors;
  }

  public FormEngineConfiguration setCustomPostCommandInterceptors(List<CommandInterceptor> customPostCommandInterceptors) {
    this.customPostCommandInterceptors = customPostCommandInterceptors;
    return this;
  }

  public List<CommandInterceptor> getCommandInterceptors() {
    return commandInterceptors;
  }

  public FormEngineConfiguration setCommandInterceptors(List<CommandInterceptor> commandInterceptors) {
    this.commandInterceptors = commandInterceptors;
    return this;
  }

  public CommandExecutor getCommandExecutor() {
    return commandExecutor;
  }

  public FormEngineConfiguration setCommandExecutor(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
    return this;
  }

  public FormRepositoryService getFormRepositoryService() {
    return repositoryService;
  }

  public FormService getFormService() {
    return formService;
  }

  public DeploymentManager getDeploymentManager() {
    return deploymentManager;
  }

  public FormEngineConfiguration getFormEngineConfiguration() {
    return this;
  }

  public FormDeployer getFormDeployer() {
    return formDeployer;
  }

  public FormEngineConfiguration setFormDeployer(FormDeployer formDeployer) {
    this.formDeployer = formDeployer;
    return this;
  }

  public FormParseFactory getFormParseFactory() {
    return formParseFactory;
  }

  public FormEngineConfiguration setFormParseFactory(FormParseFactory formParseFactory) {
    this.formParseFactory = formParseFactory;
    return this;
  }

  public int getFormCacheLimit() {
    return formCacheLimit;
  }

  public FormEngineConfiguration setFormCacheLimit(int formCacheLimit) {
    this.formCacheLimit = formCacheLimit;
    return this;
  }

  public DeploymentCache<FormCacheEntry> getFormCache() {
    return formCache;
  }

  public FormEngineConfiguration setFormCache(DeploymentCache<FormCacheEntry> formCache) {
    this.formCache = formCache;
    return this;
  }

  public FormDeploymentDataManager getDeploymentDataManager() {
    return deploymentDataManager;
  }

  public FormEngineConfiguration setDeploymentDataManager(FormDeploymentDataManager deploymentDataManager) {
    this.deploymentDataManager = deploymentDataManager;
    return this;
  }

  public FormDataManager getFormDataManager() {
    return formDataManager;
  }

  public FormEngineConfiguration setFormDataManager(FormDataManager formDataManager) {
    this.formDataManager = formDataManager;
    return this;
  }

  public ResourceDataManager getResourceDataManager() {
    return resourceDataManager;
  }

  public FormEngineConfiguration setResourceDataManager(ResourceDataManager resourceDataManager) {
    this.resourceDataManager = resourceDataManager;
    return this;
  }

  public SubmittedFormDataManager getSubmittedFormDataManager() {
    return submittedFormDataManager;
  }

  public FormEngineConfiguration setSubmittedFormDataManager(SubmittedFormDataManager submittedFormDataManager) {
    this.submittedFormDataManager = submittedFormDataManager;
    return this;
  }

  public FormDeploymentEntityManager getDeploymentEntityManager() {
    return deploymentEntityManager;
  }

  public FormEngineConfiguration setDeploymentEntityManager(FormDeploymentEntityManager deploymentEntityManager) {
    this.deploymentEntityManager = deploymentEntityManager;
    return this;
  }

  public FormEntityManager getFormEntityManager() {
    return formEntityManager;
  }

  public FormEngineConfiguration setFormEntityManager(FormEntityManager formEntityManager) {
    this.formEntityManager = formEntityManager;
    return this;
  }

  public ResourceEntityManager getResourceEntityManager() {
    return resourceEntityManager;
  }

  public FormEngineConfiguration setResourceEntityManager(ResourceEntityManager resourceEntityManager) {
    this.resourceEntityManager = resourceEntityManager;
    return this;
  }
  
  public SubmittedFormEntityManager getSubmittedFormEntityManager() {
    return submittedFormEntityManager;
  }

  public FormEngineConfiguration setSubmittedFormEntityManager(SubmittedFormEntityManager submittedFormEntityManager) {
    this.submittedFormEntityManager = submittedFormEntityManager;
    return this;
  }

  public CommandContextFactory getCommandContextFactory() {
    return commandContextFactory;
  }

  public FormEngineConfiguration setCommandContextFactory(CommandContextFactory commandContextFactory) {
    this.commandContextFactory = commandContextFactory;
    return this;
  }

  public SqlSessionFactory getSqlSessionFactory() {
    return sqlSessionFactory;
  }

  public FormEngineConfiguration setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
    this.sqlSessionFactory = sqlSessionFactory;
    return this;
  }

  public TransactionFactory getTransactionFactory() {
    return transactionFactory;
  }

  public FormEngineConfiguration setTransactionFactory(TransactionFactory transactionFactory) {
    this.transactionFactory = transactionFactory;
    return this;
  }
  
  public ExpressionManager getExpressionManager() {
    return expressionManager;
  }

  public FormEngineConfiguration setExpressionManager(ExpressionManager expressionManager) {
    this.expressionManager = expressionManager;
    return this;
  }

  public FormJsonConverter getFormJsonConverter() {
    return formJsonConverter;
  }

  public FormEngineConfiguration setFormJsonConverter(FormJsonConverter formJsonConverter) {
    this.formJsonConverter = formJsonConverter;
    return this;
  }

  public Set<Class<?>> getCustomMybatisMappers() {
    return customMybatisMappers;
  }

  public FormEngineConfiguration setCustomMybatisMappers(Set<Class<?>> customMybatisMappers) {
    this.customMybatisMappers = customMybatisMappers;
    return this;
  }

  public Set<String> getCustomMybatisXMLMappers() {
    return customMybatisXMLMappers;
  }

  public FormEngineConfiguration setCustomMybatisXMLMappers(Set<String> customMybatisXMLMappers) {
    this.customMybatisXMLMappers = customMybatisXMLMappers;
    return this;
  }

  public List<SessionFactory> getCustomSessionFactories() {
    return customSessionFactories;
  }

  public FormEngineConfiguration setCustomSessionFactories(List<SessionFactory> customSessionFactories) {
    this.customSessionFactories = customSessionFactories;
    return this;
  }

  public DbSqlSessionFactory getDbSqlSessionFactory() {
    return dbSqlSessionFactory;
  }

  public FormEngineConfiguration setDbSqlSessionFactory(DbSqlSessionFactory dbSqlSessionFactory) {
    this.dbSqlSessionFactory = dbSqlSessionFactory;
    return this;
  }

  public boolean isUsingRelationalDatabase() {
    return usingRelationalDatabase;
  }

  public FormEngineConfiguration setUsingRelationalDatabase(boolean usingRelationalDatabase) {
    this.usingRelationalDatabase = usingRelationalDatabase;
    return this;
  }

  public String getDatabaseTablePrefix() {
    return databaseTablePrefix;
  }

  public FormEngineConfiguration setDatabaseTablePrefix(String databaseTablePrefix) {
    this.databaseTablePrefix = databaseTablePrefix;
    return this;
  }

  public String getDatabaseCatalog() {
    return databaseCatalog;
  }

  public FormEngineConfiguration setDatabaseCatalog(String databaseCatalog) {
    this.databaseCatalog = databaseCatalog;
    return this;
  }

  public String getDatabaseSchema() {
    return databaseSchema;
  }

  public FormEngineConfiguration setDatabaseSchema(String databaseSchema) {
    this.databaseSchema = databaseSchema;
    return this;
  }

  public boolean isTablePrefixIsSchema() {
    return tablePrefixIsSchema;
  }

  public FormEngineConfiguration setTablePrefixIsSchema(boolean tablePrefixIsSchema) {
    this.tablePrefixIsSchema = tablePrefixIsSchema;
    return this;
  }

  public Map<Class<?>, SessionFactory> getSessionFactories() {
    return sessionFactories;
  }

  public FormEngineConfiguration setSessionFactories(Map<Class<?>, SessionFactory> sessionFactories) {
    this.sessionFactories = sessionFactories;
    return this;
  }

  public TransactionContextFactory getTransactionContextFactory() {
    return transactionContextFactory;
  }

  public FormEngineConfiguration setTransactionContextFactory(TransactionContextFactory transactionContextFactory) {
    this.transactionContextFactory = transactionContextFactory;
    return this;
  }
  
  public FormEngineConfiguration setDatabaseSchemaUpdate(String databaseSchemaUpdate) {
    this.databaseSchemaUpdate = databaseSchemaUpdate;
    return this;
  }

  public Clock getClock() {
    return clock;
  }

  public FormEngineConfiguration setClock(Clock clock) {
    this.clock = clock;
    return this;
  }
  
  public ObjectMapper getObjectMapper() {
    return objectMapper;
  }
  
  public FormEngineConfiguration setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    return this;
  }
}

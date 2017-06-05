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
package org.activiti.dmn.engine;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Method;
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

import org.activiti.dmn.api.DmnRepositoryService;
import org.activiti.dmn.api.DmnRuleService;
import org.activiti.dmn.engine.impl.DmnEngineImpl;
import org.activiti.dmn.engine.impl.DmnRepositoryServiceImpl;
import org.activiti.dmn.engine.impl.DmnRuleServiceImpl;
import org.activiti.dmn.engine.impl.RuleEngineExecutorImpl;
import org.activiti.dmn.engine.impl.ServiceImpl;
import org.activiti.dmn.engine.impl.cfg.CommandExecutorImpl;
import org.activiti.dmn.engine.impl.cfg.IdGenerator;
import org.activiti.dmn.engine.impl.cfg.StandaloneDmnEngineConfiguration;
import org.activiti.dmn.engine.impl.cfg.StandaloneInMemDmnEngineConfiguration;
import org.activiti.dmn.engine.impl.cfg.TransactionContextFactory;
import org.activiti.dmn.engine.impl.cfg.standalone.StandaloneMybatisTransactionContextFactory;
import org.activiti.dmn.engine.impl.db.DbSqlSessionFactory;
import org.activiti.dmn.engine.impl.deployer.CachingAndArtifactsManager;
import org.activiti.dmn.engine.impl.deployer.DmnDeployer;
import org.activiti.dmn.engine.impl.deployer.DmnDeploymentHelper;
import org.activiti.dmn.engine.impl.deployer.ParsedDeploymentBuilderFactory;
import org.activiti.dmn.engine.impl.interceptor.CommandConfig;
import org.activiti.dmn.engine.impl.interceptor.CommandContextFactory;
import org.activiti.dmn.engine.impl.interceptor.CommandContextInterceptor;
import org.activiti.dmn.engine.impl.interceptor.CommandExecutor;
import org.activiti.dmn.engine.impl.interceptor.CommandInterceptor;
import org.activiti.dmn.engine.impl.interceptor.CommandInvoker;
import org.activiti.dmn.engine.impl.interceptor.LogInterceptor;
import org.activiti.dmn.engine.impl.interceptor.SessionFactory;
import org.activiti.dmn.engine.impl.mvel.config.DefaultCustomExpressionFunctionRegistry;
import org.activiti.dmn.engine.impl.parser.DmnParseFactory;
import org.activiti.dmn.engine.impl.persistence.StrongUuidGenerator;
import org.activiti.dmn.engine.impl.persistence.deploy.DecisionTableCacheEntry;
import org.activiti.dmn.engine.impl.persistence.deploy.DefaultDeploymentCache;
import org.activiti.dmn.engine.impl.persistence.deploy.Deployer;
import org.activiti.dmn.engine.impl.persistence.deploy.DeploymentCache;
import org.activiti.dmn.engine.impl.persistence.deploy.DeploymentManager;
import org.activiti.dmn.engine.impl.persistence.entity.DecisionTableEntityManager;
import org.activiti.dmn.engine.impl.persistence.entity.DecisionTableEntityManagerImpl;
import org.activiti.dmn.engine.impl.persistence.entity.DmnDeploymentEntityManager;
import org.activiti.dmn.engine.impl.persistence.entity.DmnDeploymentEntityManagerImpl;
import org.activiti.dmn.engine.impl.persistence.entity.ResourceEntityManager;
import org.activiti.dmn.engine.impl.persistence.entity.ResourceEntityManagerImpl;
import org.activiti.dmn.engine.impl.persistence.entity.data.DecisionTableDataManager;
import org.activiti.dmn.engine.impl.persistence.entity.data.DmnDeploymentDataManager;
import org.activiti.dmn.engine.impl.persistence.entity.data.ResourceDataManager;
import org.activiti.dmn.engine.impl.persistence.entity.data.impl.MybatisDecisionTableDataManager;
import org.activiti.dmn.engine.impl.persistence.entity.data.impl.MybatisDmnDeploymentDataManager;
import org.activiti.dmn.engine.impl.persistence.entity.data.impl.MybatisResourceDataManager;
import org.activiti.dmn.engine.impl.util.DefaultClockImpl;
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
import org.mvel2.integration.PropertyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

public class DmnEngineConfiguration {

  protected static final Logger logger = LoggerFactory.getLogger(DmnEngineConfiguration.class);

  /** The tenant id indicating 'no tenant' */
  public static final String NO_TENANT_ID = "";

  public static final String DEFAULT_MYBATIS_MAPPING_FILE = "org/activiti/dmn/db/mapping/mappings.xml";

  public static final String LIQUIBASE_CHANGELOG_PREFIX = "ACT_DMN_";

  /**
   * Checks the version of the DB schema against the library when the process engine is being created and throws an exception if the versions don't match.
   */
  public static final String DB_SCHEMA_UPDATE_FALSE = "false";

  /**
   * Creates the schema when the process engine is being created and drops the schema when the process engine is being closed.
   */
  public static final String DB_SCHEMA_UPDATE_DROP_CREATE = "create-drop";

  /**
   * Upon building of the process engine, a check is performed and an update of the schema is performed if it is necessary.
   */
  public static final String DB_SCHEMA_UPDATE_TRUE = "true";

  protected String dmnEngineName = DmnEngines.NAME_DEFAULT;

  protected String databaseType;
  protected String jdbcDriver = "org.h2.Driver";
  protected String jdbcUrl = "jdbc:h2:tcp://localhost/~/activitidmn";
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

  protected DmnRepositoryService repositoryService = new DmnRepositoryServiceImpl();
  protected DmnRuleService ruleService = new DmnRuleServiceImpl();
  protected RuleEngineExecutor ruleEngineExecutor = new RuleEngineExecutorImpl();

  // DATA MANAGERS ///////////////////////////////////////////////////

  protected DmnDeploymentDataManager deploymentDataManager;
  protected DecisionTableDataManager decisionTableDataManager;
  protected ResourceDataManager resourceDataManager;

  // ENTITY MANAGERS /////////////////////////////////////////////////
  protected DmnDeploymentEntityManager deploymentEntityManager;
  protected DecisionTableEntityManager decisionTableEntityManager;
  protected ResourceEntityManager resourceEntityManager;

  protected CommandContextFactory commandContextFactory;
  protected TransactionContextFactory transactionContextFactory;

  // MYBATIS SQL SESSION FACTORY /////////////////////////////////////

  protected SqlSessionFactory sqlSessionFactory;
  protected TransactionFactory transactionFactory;

  protected Set<Class<?>> customMybatisMappers;
  protected Set<String> customMybatisXMLMappers;

  // SESSION FACTORIES ///////////////////////////////////////////////
  protected List<SessionFactory> customSessionFactories;
  protected DbSqlSessionFactory dbSqlSessionFactory;
  protected Map<Class<?>, SessionFactory> sessionFactories;

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

  public void initDatabaseType() {
    Connection connection = null;
    try {
      connection = dataSource.getConnection();
      DatabaseMetaData databaseMetaData = connection.getMetaData();
      String databaseProductName = databaseMetaData.getDatabaseProductName();
      logger.debug("database product name: '{}'", databaseProductName);
      databaseType = databaseTypeMappings.getProperty(databaseProductName);
      if (databaseType == null) {
        throw new ActivitiDmnException("couldn't deduct database type from database product name '" + databaseProductName + "'");
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

  // DEPLOYERS
  // ////////////////////////////////////////////////////////////////

  protected DmnDeployer dmnDeployer;
  protected DmnParseFactory dmnParseFactory;
  protected ParsedDeploymentBuilderFactory parsedDeploymentBuilderFactory;
  protected DmnDeploymentHelper dmnDeploymentHelper;
  protected CachingAndArtifactsManager cachingAndArtifactsManager;
  protected List<Deployer> customPreDeployers;
  protected List<Deployer> customPostDeployers;
  protected List<Deployer> deployers;
  protected DeploymentManager deploymentManager;

  protected int decisionCacheLimit = -1; // By default, no limit
  protected DeploymentCache<DecisionTableCacheEntry> decisionCache;

  protected IdGenerator idGenerator;

  protected Clock clock;

  // CUSTOM EXPRESSION FUNCTIONS
  // ////////////////////////////////////////////////////////////////
  protected CustomExpressionFunctionRegistry customExpressionFunctionRegistry;
  protected CustomExpressionFunctionRegistry postCustomExpressionFunctionRegistry;
  protected Map<String, Method> customExpressionFunctions = new HashMap<String, Method>();
  protected Map<Class<?>, PropertyHandler> customPropertyHandlers = new HashMap<Class<?>, PropertyHandler>();

  /**
   * Set this to true if you want to have extra checks on the BPMN xml that is parsed.
   * 
   * Unfortunately, this feature is not available on some platforms (JDK 6, JBoss), hence the reason why it is disabled by default. If your platform allows the use of StaxSource during XML parsing, do
   * enable it.
   */
  protected boolean enableSafeDmnXml;

  public static DmnEngineConfiguration createDmnEngineConfigurationFromResourceDefault() {
    return createDmnEngineConfigurationFromResource("activiti.dmn.cfg.xml", "dmnEngineConfiguration");
  }

  public static DmnEngineConfiguration createDmnEngineConfigurationFromResource(String resource) {
    return createDmnEngineConfigurationFromResource(resource, "dmnEngineConfiguration");
  }

  public static DmnEngineConfiguration createDmnEngineConfigurationFromResource(String resource, String beanName) {
    return parseProcessEngineConfigurationFromResource(resource, beanName);
  }

  public static DmnEngineConfiguration createDmnEngineConfigurationFromInputStream(InputStream inputStream) {
    return createDmnEngineConfigurationFromInputStream(inputStream, "dmnEngineConfiguration");
  }

  public static DmnEngineConfiguration createDmnEngineConfigurationFromInputStream(InputStream inputStream, String beanName) {
    return parseProcessEngineConfigurationFromInputStream(inputStream, beanName);
  }

  public static DmnEngineConfiguration createStandaloneDmnEngineConfiguration() {
    return new StandaloneDmnEngineConfiguration();
  }

  public static DmnEngineConfiguration createStandaloneInMemDmnEngineConfiguration() {
    return new StandaloneInMemDmnEngineConfiguration();
  }

  public static DmnEngineConfiguration parseDmnEngineConfiguration(Resource springResource, String beanName) {
    DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
    XmlBeanDefinitionReader xmlBeanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);
    xmlBeanDefinitionReader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_XSD);
    xmlBeanDefinitionReader.loadBeanDefinitions(springResource);
    DmnEngineConfiguration processEngineConfiguration = (DmnEngineConfiguration) beanFactory.getBean(beanName);
    processEngineConfiguration.setBeanFactory(beanFactory);
    return processEngineConfiguration;
  }

  public static DmnEngineConfiguration parseProcessEngineConfigurationFromInputStream(InputStream inputStream, String beanName) {
    Resource springResource = new InputStreamResource(inputStream);
    return parseDmnEngineConfiguration(springResource, beanName);
  }

  public static DmnEngineConfiguration parseProcessEngineConfigurationFromResource(String resource, String beanName) {
    Resource springResource = new ClassPathResource(resource);
    return parseDmnEngineConfiguration(springResource, beanName);
  }

  // buildProcessEngine
  // ///////////////////////////////////////////////////////

  public DmnEngine buildDmnEngine() {
    init();
    return new DmnEngineImpl(this);
  }

  // init
  // /////////////////////////////////////////////////////////////////////

  protected void init() {
    initCommandContextFactory();
    initTransactionContextFactory();
    initCommandExecutors();
    initIdGenerator();
    initDataSource();
    initDbSchema();
    initTransactionFactory();
    initSqlSessionFactory();
    initSessionFactories();
    initServices();
    initDataManagers();
    initEntityManagers();
    initDeployers();
    initClock();
    initCustomExpressionFunctions();
  }

  // services
  // /////////////////////////////////////////////////////////////////

  protected void initServices() {
    initService(repositoryService);
    initService(ruleService);
  }

  protected void initService(Object service) {
    if (service instanceof ServiceImpl) {
      ((ServiceImpl) service).setCommandExecutor(commandExecutor);
    }
  }

  // Data managers
  ///////////////////////////////////////////////////////////

  public void initDataManagers() {
    if (deploymentDataManager == null) {
      deploymentDataManager = new MybatisDmnDeploymentDataManager(this);
    }
    if (decisionTableDataManager == null) {
      decisionTableDataManager = new MybatisDecisionTableDataManager(this);
    }
    if (resourceDataManager == null) {
      resourceDataManager = new MybatisResourceDataManager(this);
    }
  }

  public void initEntityManagers() {
    if (deploymentEntityManager == null) {
      deploymentEntityManager = new DmnDeploymentEntityManagerImpl(this, deploymentDataManager);
    }
    if (decisionTableEntityManager == null) {
      decisionTableEntityManager = new DecisionTableEntityManagerImpl(this, decisionTableDataManager);
    }
    if (resourceEntityManager == null) {
      resourceEntityManager = new ResourceEntityManagerImpl(this, resourceDataManager);
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
          throw new ActivitiDmnException("couldn't lookup datasource from " + dataSourceJndiName + ": " + e.getMessage(), e);
        }

      } else if (jdbcUrl != null) {
        if ((jdbcDriver == null) || (jdbcUsername == null)) {
          throw new ActivitiDmnException("DataSource or JDBC properties have to be specified in a process engine configuration");
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

  // data model
  // ///////////////////////////////////////////////////////////////

  public void initDbSchema() {
    try {
      DatabaseConnection connection = new JdbcConnection(dataSource.getConnection());
      Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);
      database.setDatabaseChangeLogTableName(LIQUIBASE_CHANGELOG_PREFIX + database.getDatabaseChangeLogTableName());
      database.setDatabaseChangeLogLockTableName(LIQUIBASE_CHANGELOG_PREFIX + database.getDatabaseChangeLogLockTableName());
      
      if (StringUtils.isNotEmpty(databaseSchema)) {
        database.setDefaultSchemaName(databaseSchema);
        database.setLiquibaseSchemaName(databaseSchema);
      }
      
      if (StringUtils.isNotEmpty(databaseCatalog)) {
        database.setDefaultCatalogName(databaseCatalog);
        database.setLiquibaseCatalogName(databaseCatalog);
      }

      Liquibase liquibase = new Liquibase("org/activiti/dmn/db/liquibase/activiti-dmn-db-changelog.xml", new ClassLoaderResourceAccessor(), database);

      if (DB_SCHEMA_UPDATE_DROP_CREATE.equals(databaseSchemaUpdate)) {
        logger.debug("Dropping and creating schema DMN");
        liquibase.dropAll();
        liquibase.update("dmn");
      } else if (DB_SCHEMA_UPDATE_TRUE.equals(databaseSchemaUpdate)) {
        logger.debug("Updating schema DMN");
        liquibase.update("dmn");
      } else if (DB_SCHEMA_UPDATE_FALSE.equals(databaseSchemaUpdate)) {
        logger.debug("Validating schema DMN");
        liquibase.validate();
      }
    } catch (Exception e) {
      throw new ActivitiDmnException("Error initialising dmn data model");
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
      throw new ActivitiDmnException("invalid command interceptor chain configuration: " + chain);
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
    if (dmnParseFactory == null) {
      dmnParseFactory = new DmnParseFactory();
    }

    if (this.dmnDeployer == null) {
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
    if (decisionCache == null) {
      if (decisionCacheLimit <= 0) {
        decisionCache = new DefaultDeploymentCache<DecisionTableCacheEntry>();
      } else {
        decisionCache = new DefaultDeploymentCache<DecisionTableCacheEntry>(decisionCacheLimit);
      }
    }

    deploymentManager = new DeploymentManager(decisionCache, this);
    deploymentManager.setDeployers(deployers);
    deploymentManager.setDeploymentEntityManager(deploymentEntityManager);
    deploymentManager.setDecisionTableEntityManager(decisionTableEntityManager);
  }

  public Collection<? extends Deployer> getDefaultDeployers() {
    List<Deployer> defaultDeployers = new ArrayList<Deployer>();

    if (dmnDeployer == null) {
      dmnDeployer = new DmnDeployer();
    }

    initDmnDeployerDependencies();

    dmnDeployer.setIdGenerator(idGenerator);
    dmnDeployer.setParsedDeploymentBuilderFactory(parsedDeploymentBuilderFactory);
    dmnDeployer.setDmnDeploymentHelper(dmnDeploymentHelper);
    dmnDeployer.setCachingAndArtifactsManager(cachingAndArtifactsManager);

    defaultDeployers.add(dmnDeployer);
    return defaultDeployers;
  }

  public void initDmnDeployerDependencies() {
    if (parsedDeploymentBuilderFactory == null) {
      parsedDeploymentBuilderFactory = new ParsedDeploymentBuilderFactory();
    }
    if (parsedDeploymentBuilderFactory.getDmnParseFactory() == null) {
      parsedDeploymentBuilderFactory.setDmnParseFactory(dmnParseFactory);
    }

    if (dmnDeploymentHelper == null) {
      dmnDeploymentHelper = new DmnDeploymentHelper();
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

  // custom expression functions
  // ////////////////////////////////////////////////////////////////
  protected void initCustomExpressionFunctions() {
    if (customExpressionFunctionRegistry == null) {
      customExpressionFunctions.putAll(new DefaultCustomExpressionFunctionRegistry().getCustomExpressionMethods());
    } else {
      customExpressionFunctions.putAll(customExpressionFunctionRegistry.getCustomExpressionMethods());
    }

    if (postCustomExpressionFunctionRegistry != null) {
      customExpressionFunctions.putAll(postCustomExpressionFunctionRegistry.getCustomExpressionMethods());
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
          properties.load(getResourceAsStream("org/activiti/dmn/db/properties/" + databaseType + ".properties"));
        }

        Configuration configuration = initMybatisConfiguration(environment, reader, properties);
        sqlSessionFactory = new DefaultSqlSessionFactory(configuration);

      } catch (Exception e) {
        throw new ActivitiDmnException("Error while building ibatis SqlSessionFactory: " + e.getMessage(), e);
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

  public String getDmnEngineName() {
    return dmnEngineName;
  }

  public DmnEngineConfiguration setDmnEngineName(String dmnEngineName) {
    this.dmnEngineName = dmnEngineName;
    return this;
  }

  public String getDatabaseType() {
    return databaseType;
  }

  public DmnEngineConfiguration setDatabaseType(String databaseType) {
    this.databaseType = databaseType;
    return this;
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  public DmnEngineConfiguration setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
    return this;
  }

  public String getJdbcDriver() {
    return jdbcDriver;
  }

  public DmnEngineConfiguration setJdbcDriver(String jdbcDriver) {
    this.jdbcDriver = jdbcDriver;
    return this;
  }

  public String getJdbcUrl() {
    return jdbcUrl;
  }

  public DmnEngineConfiguration setJdbcUrl(String jdbcUrl) {
    this.jdbcUrl = jdbcUrl;
    return this;
  }

  public String getJdbcUsername() {
    return jdbcUsername;
  }

  public DmnEngineConfiguration setJdbcUsername(String jdbcUsername) {
    this.jdbcUsername = jdbcUsername;
    return this;
  }

  public String getJdbcPassword() {
    return jdbcPassword;
  }

  public DmnEngineConfiguration setJdbcPassword(String jdbcPassword) {
    this.jdbcPassword = jdbcPassword;
    return this;
  }

  public int getJdbcMaxActiveConnections() {
    return jdbcMaxActiveConnections;
  }

  public DmnEngineConfiguration setJdbcMaxActiveConnections(int jdbcMaxActiveConnections) {
    this.jdbcMaxActiveConnections = jdbcMaxActiveConnections;
    return this;
  }

  public int getJdbcMaxIdleConnections() {
    return jdbcMaxIdleConnections;
  }

  public DmnEngineConfiguration setJdbcMaxIdleConnections(int jdbcMaxIdleConnections) {
    this.jdbcMaxIdleConnections = jdbcMaxIdleConnections;
    return this;
  }

  public int getJdbcMaxCheckoutTime() {
    return jdbcMaxCheckoutTime;
  }

  public DmnEngineConfiguration setJdbcMaxCheckoutTime(int jdbcMaxCheckoutTime) {
    this.jdbcMaxCheckoutTime = jdbcMaxCheckoutTime;
    return this;
  }

  public int getJdbcMaxWaitTime() {
    return jdbcMaxWaitTime;
  }

  public DmnEngineConfiguration setJdbcMaxWaitTime(int jdbcMaxWaitTime) {
    this.jdbcMaxWaitTime = jdbcMaxWaitTime;
    return this;
  }

  public boolean isJdbcPingEnabled() {
    return jdbcPingEnabled;
  }

  public DmnEngineConfiguration setJdbcPingEnabled(boolean jdbcPingEnabled) {
    this.jdbcPingEnabled = jdbcPingEnabled;
    return this;
  }

  public int getJdbcPingConnectionNotUsedFor() {
    return jdbcPingConnectionNotUsedFor;
  }

  public DmnEngineConfiguration setJdbcPingConnectionNotUsedFor(int jdbcPingConnectionNotUsedFor) {
    this.jdbcPingConnectionNotUsedFor = jdbcPingConnectionNotUsedFor;
    return this;
  }

  public int getJdbcDefaultTransactionIsolationLevel() {
    return jdbcDefaultTransactionIsolationLevel;
  }

  public DmnEngineConfiguration setJdbcDefaultTransactionIsolationLevel(int jdbcDefaultTransactionIsolationLevel) {
    this.jdbcDefaultTransactionIsolationLevel = jdbcDefaultTransactionIsolationLevel;
    return this;
  }

  public String getJdbcPingQuery() {
    return jdbcPingQuery;
  }

  public DmnEngineConfiguration setJdbcPingQuery(String jdbcPingQuery) {
    this.jdbcPingQuery = jdbcPingQuery;
    return this;
  }

  public String getDataSourceJndiName() {
    return dataSourceJndiName;
  }

  public DmnEngineConfiguration setDataSourceJndiName(String dataSourceJndiName) {
    this.dataSourceJndiName = dataSourceJndiName;
    return this;
  }

  public String getXmlEncoding() {
    return xmlEncoding;
  }

  public DmnEngineConfiguration setXmlEncoding(String xmlEncoding) {
    this.xmlEncoding = xmlEncoding;
    return this;
  }

  public BeanFactory getBeanFactory() {
    return beanFactory;
  }

  public DmnEngineConfiguration setBeanFactory(BeanFactory beanFactory) {
    this.beanFactory = beanFactory;
    return this;
  }

  public CommandConfig getDefaultCommandConfig() {
    return defaultCommandConfig;
  }

  public DmnEngineConfiguration setDefaultCommandConfig(CommandConfig defaultCommandConfig) {
    this.defaultCommandConfig = defaultCommandConfig;
    return this;
  }

  public CommandInterceptor getCommandInvoker() {
    return commandInvoker;
  }

  public DmnEngineConfiguration setCommandInvoker(CommandInterceptor commandInvoker) {
    this.commandInvoker = commandInvoker;
    return this;
  }

  public List<CommandInterceptor> getCustomPreCommandInterceptors() {
    return customPreCommandInterceptors;
  }

  public DmnEngineConfiguration setCustomPreCommandInterceptors(List<CommandInterceptor> customPreCommandInterceptors) {
    this.customPreCommandInterceptors = customPreCommandInterceptors;
    return this;
  }

  public List<CommandInterceptor> getCustomPostCommandInterceptors() {
    return customPostCommandInterceptors;
  }

  public DmnEngineConfiguration setCustomPostCommandInterceptors(List<CommandInterceptor> customPostCommandInterceptors) {
    this.customPostCommandInterceptors = customPostCommandInterceptors;
    return this;
  }

  public List<CommandInterceptor> getCommandInterceptors() {
    return commandInterceptors;
  }

  public DmnEngineConfiguration setCommandInterceptors(List<CommandInterceptor> commandInterceptors) {
    this.commandInterceptors = commandInterceptors;
    return this;
  }

  public CommandExecutor getCommandExecutor() {
    return commandExecutor;
  }

  public DmnEngineConfiguration setCommandExecutor(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
    return this;
  }

  public DmnRepositoryService getDmnRepositoryService() {
    return repositoryService;
  }

  public DmnRuleService getDmnRuleService() {
    return ruleService;
  }
  
  public RuleEngineExecutor getRuleEngineExecutor() {
    return ruleEngineExecutor;
  }

  public DeploymentManager getDeploymentManager() {
    return deploymentManager;
  }

  public DmnEngineConfiguration getDmnEngineConfiguration() {
    return this;
  }

  public DmnDeployer getDmnDeployer() {
    return dmnDeployer;
  }

  public DmnEngineConfiguration setDmnDeployer(DmnDeployer dmnDeployer) {
    this.dmnDeployer = dmnDeployer;
    return this;
  }

  public DmnParseFactory getDmnParseFactory() {
    return dmnParseFactory;
  }

  public DmnEngineConfiguration setDmnParseFactory(DmnParseFactory dmnParseFactory) {
    this.dmnParseFactory = dmnParseFactory;
    return this;
  }

  public int getDecisionCacheLimit() {
    return decisionCacheLimit;
  }

  public DmnEngineConfiguration setDecisionCacheLimit(int decisionCacheLimit) {
    this.decisionCacheLimit = decisionCacheLimit;
    return this;
  }

  public DeploymentCache<DecisionTableCacheEntry> getDecisionCache() {
    return decisionCache;
  }

  public DmnEngineConfiguration setDecisionCache(DeploymentCache<DecisionTableCacheEntry> decisionCache) {
    this.decisionCache = decisionCache;
    return this;
  }

  public DmnDeploymentDataManager getDeploymentDataManager() {
    return deploymentDataManager;
  }

  public void setDeploymentDataManager(DmnDeploymentDataManager deploymentDataManager) {
    this.deploymentDataManager = deploymentDataManager;
  }

  public DecisionTableDataManager getDecisionTableDataManager() {
    return decisionTableDataManager;
  }

  public void setDecisionTableDataManager(DecisionTableDataManager decisionTableDataManager) {
    this.decisionTableDataManager = decisionTableDataManager;
  }

  public ResourceDataManager getResourceDataManager() {
    return resourceDataManager;
  }

  public void setResourceDataManager(ResourceDataManager resourceDataManager) {
    this.resourceDataManager = resourceDataManager;
  }

  public DmnDeploymentEntityManager getDeploymentEntityManager() {
    return deploymentEntityManager;
  }

  public void setDeploymentEntityManager(DmnDeploymentEntityManager deploymentEntityManager) {
    this.deploymentEntityManager = deploymentEntityManager;
  }

  public DecisionTableEntityManager getDecisionTableEntityManager() {
    return decisionTableEntityManager;
  }

  public void setDecisionTableEntityManager(DecisionTableEntityManager decisionTableEntityManager) {
    this.decisionTableEntityManager = decisionTableEntityManager;
  }

  public ResourceEntityManager getResourceEntityManager() {
    return resourceEntityManager;
  }

  public void setResourceEntityManager(ResourceEntityManager resourceEntityManager) {
    this.resourceEntityManager = resourceEntityManager;
  }

  public CommandContextFactory getCommandContextFactory() {
    return commandContextFactory;
  }

  public void setCommandContextFactory(CommandContextFactory commandContextFactory) {
    this.commandContextFactory = commandContextFactory;
  }

  public SqlSessionFactory getSqlSessionFactory() {
    return sqlSessionFactory;
  }

  public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
    this.sqlSessionFactory = sqlSessionFactory;
  }

  public TransactionFactory getTransactionFactory() {
    return transactionFactory;
  }

  public void setTransactionFactory(TransactionFactory transactionFactory) {
    this.transactionFactory = transactionFactory;
  }

  public Set<Class<?>> getCustomMybatisMappers() {
    return customMybatisMappers;
  }

  public void setCustomMybatisMappers(Set<Class<?>> customMybatisMappers) {
    this.customMybatisMappers = customMybatisMappers;
  }

  public Set<String> getCustomMybatisXMLMappers() {
    return customMybatisXMLMappers;
  }

  public void setCustomMybatisXMLMappers(Set<String> customMybatisXMLMappers) {
    this.customMybatisXMLMappers = customMybatisXMLMappers;
  }

  public List<SessionFactory> getCustomSessionFactories() {
    return customSessionFactories;
  }

  public void setCustomSessionFactories(List<SessionFactory> customSessionFactories) {
    this.customSessionFactories = customSessionFactories;
  }

  public DbSqlSessionFactory getDbSqlSessionFactory() {
    return dbSqlSessionFactory;
  }

  public void setDbSqlSessionFactory(DbSqlSessionFactory dbSqlSessionFactory) {
    this.dbSqlSessionFactory = dbSqlSessionFactory;
  }

  public boolean isUsingRelationalDatabase() {
    return usingRelationalDatabase;
  }

  public void setUsingRelationalDatabase(boolean usingRelationalDatabase) {
    this.usingRelationalDatabase = usingRelationalDatabase;
  }

  public String getDatabaseTablePrefix() {
    return databaseTablePrefix;
  }

  public void setDatabaseTablePrefix(String databaseTablePrefix) {
    this.databaseTablePrefix = databaseTablePrefix;
  }

  public String getDatabaseCatalog() {
    return databaseCatalog;
  }

  public void setDatabaseCatalog(String databaseCatalog) {
    this.databaseCatalog = databaseCatalog;
  }

  public String getDatabaseSchema() {
    return databaseSchema;
  }

  public void setDatabaseSchema(String databaseSchema) {
    this.databaseSchema = databaseSchema;
  }

  public boolean isTablePrefixIsSchema() {
    return tablePrefixIsSchema;
  }

  public void setTablePrefixIsSchema(boolean tablePrefixIsSchema) {
    this.tablePrefixIsSchema = tablePrefixIsSchema;
  }

  public Map<Class<?>, SessionFactory> getSessionFactories() {
    return sessionFactories;
  }

  public DmnEngineConfiguration setSessionFactories(Map<Class<?>, SessionFactory> sessionFactories) {
    this.sessionFactories = sessionFactories;
    return this;
  }

  public TransactionContextFactory getTransactionContextFactory() {
    return transactionContextFactory;
  }

  public DmnEngineConfiguration setTransactionContextFactory(TransactionContextFactory transactionContextFactory) {
    this.transactionContextFactory = transactionContextFactory;
    return this;
  }

  public boolean isEnableSafeDmnXml() {
    return enableSafeDmnXml;
  }

  public DmnEngineConfiguration setEnableSafeDmnXml(boolean enableSafeDmnXml) {
    this.enableSafeDmnXml = enableSafeDmnXml;
    return this;
  }

  public Clock getClock() {
    return clock;
  }

  public DmnEngineConfiguration setClock(Clock clock) {
    this.clock = clock;
    return this;
  }

  public CustomExpressionFunctionRegistry getCustomExpressionFunctionRegistry() {
    return customExpressionFunctionRegistry;
  }

  public void setCustomExpressionFunctionRegistry(CustomExpressionFunctionRegistry customExpressionFunctionRegistry) {
    this.customExpressionFunctionRegistry = customExpressionFunctionRegistry;
  }

  public CustomExpressionFunctionRegistry getPostCustomExpressionFunctionRegistry() {
    return postCustomExpressionFunctionRegistry;
  }

  public void setPostCustomExpressionFunctionRegistry(CustomExpressionFunctionRegistry postCustomExpressionFunctionRegistry) {
    this.postCustomExpressionFunctionRegistry = postCustomExpressionFunctionRegistry;
  }

  public Map<String, Method> getCustomExpressionFunctions() {
    return customExpressionFunctions;
  }

  public void setCustomExpressionFunctions(Map<String, Method> customExpressionFunctions) {
    this.customExpressionFunctions = customExpressionFunctions;
  }

  public Map<Class<?>, PropertyHandler> getCustomPropertyHandlers() {
    return customPropertyHandlers;
  }

  public void setCustomPropertyHandlers(Map<Class<?>, PropertyHandler> customPropertyHandlers) {
    this.customPropertyHandlers = customPropertyHandlers;
  }

  public DmnEngineConfiguration setDatabaseSchemaUpdate(String databaseSchemaUpdate) {
    this.databaseSchemaUpdate = databaseSchemaUpdate;
    return this;
  }
}

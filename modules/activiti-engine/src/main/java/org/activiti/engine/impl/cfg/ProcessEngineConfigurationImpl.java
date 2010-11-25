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
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.form.FormType;
import org.activiti.engine.impl.FormServiceImpl;
import org.activiti.engine.impl.HistoryServiceImpl;
import org.activiti.engine.impl.IdentityServiceImpl;
import org.activiti.engine.impl.ManagementServiceImpl;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.RuntimeServiceImpl;
import org.activiti.engine.impl.ServiceImpl;
import org.activiti.engine.impl.TaskServiceImpl;
import org.activiti.engine.impl.bpmn.deployer.BpmnDeployer;
import org.activiti.engine.impl.bpmn.parser.BpmnParser;
import org.activiti.engine.impl.calendar.BusinessCalendarManager;
import org.activiti.engine.impl.calendar.DurationBusinessCalendar;
import org.activiti.engine.impl.calendar.MapBusinessCalendarManager;
import org.activiti.engine.impl.cfg.standalone.StandaloneIbatisTransactionContextFactory;
import org.activiti.engine.impl.db.DbHistorySessionFactory;
import org.activiti.engine.impl.db.DbIdGenerator;
import org.activiti.engine.impl.db.DbIdentitySessionFactory;
import org.activiti.engine.impl.db.DbManagementSessionFactory;
import org.activiti.engine.impl.db.DbRepositorySessionFactory;
import org.activiti.engine.impl.db.DbRuntimeSessionFactory;
import org.activiti.engine.impl.db.DbSqlSessionFactory;
import org.activiti.engine.impl.db.DbTaskSessionFactory;
import org.activiti.engine.impl.db.IbatisVariableTypeHandler;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.form.AbstractFormType;
import org.activiti.engine.impl.form.DateFormType;
import org.activiti.engine.impl.form.FormEngine;
import org.activiti.engine.impl.form.FormTypes;
import org.activiti.engine.impl.form.JuelFormEngine;
import org.activiti.engine.impl.form.LongFormType;
import org.activiti.engine.impl.form.StringFormType;
import org.activiti.engine.impl.history.handler.HistoryParseListener;
import org.activiti.engine.impl.interceptor.CommandContextFactory;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.interceptor.CommandInterceptor;
import org.activiti.engine.impl.interceptor.SessionFactory;
import org.activiti.engine.impl.jobexecutor.JobExecutor;
import org.activiti.engine.impl.jobexecutor.JobExecutorMessageSessionFactory;
import org.activiti.engine.impl.jobexecutor.JobExecutorTimerSessionFactory;
import org.activiti.engine.impl.jobexecutor.JobHandler;
import org.activiti.engine.impl.jobexecutor.TimerExecuteNestedActivityJobHandler;
import org.activiti.engine.impl.repository.Deployer;
import org.activiti.engine.impl.scripting.ScriptingEngines;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.engine.impl.util.ReflectUtil;
import org.activiti.engine.impl.variable.DefaultVariableTypes;
import org.activiti.engine.impl.variable.EntityManagerSession;
import org.activiti.engine.impl.variable.EntityManagerSessionFactory;
import org.activiti.engine.impl.variable.JPAEntityVariableType;
import org.activiti.engine.impl.variable.SerializableType;
import org.activiti.engine.impl.variable.VariableType;
import org.activiti.engine.impl.variable.VariableTypes;
import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.ibatis.transaction.managed.ManagedTransactionFactory;
import org.apache.ibatis.type.JdbcType;


/**
 * @author Tom Baeyens
 */
public abstract class ProcessEngineConfigurationImpl extends ProcessEngineConfiguration {
  
  public static final String DB_SCHEMA_UPDATE_CREATE = "create";
  public static final String DB_SCHEMA_UPDATE_DROP_CREATE = "drop-create";

  public static final String DEFAULT_WS_SYNC_FACTORY = "org.activiti.engine.impl.webservice.CxfWebServiceClientFactory";

  // SERVICES /////////////////////////////////////////////////////////////////

  protected RepositoryService repositoryService = new RepositoryServiceImpl();
  protected RuntimeService runtimeService = new RuntimeServiceImpl();
  protected HistoryService historyService = new HistoryServiceImpl();
  protected IdentityService identityService = new IdentityServiceImpl();
  protected TaskService taskService = new TaskServiceImpl();
  protected FormService formService = new FormServiceImpl();
  protected ManagementService managementService = new ManagementServiceImpl();
  
  // COMMAND EXECUTORS ////////////////////////////////////////////////////////
  
  // Command executor and interceptor stack
  /** the configurable list which will be {@link #initializeInterceptorChain(List) processed} to build the {@link #commandExecutorTxRequired} */
  protected List<Class<?>> customPreCommandInterceptorsTxRequiredClasses;
  protected List<Class<?>> customPostCommandInterceptorsTxRequiredClasses;

  protected List<CommandInterceptor> customPreCommandInterceptorsTxRequired;
  protected List<CommandInterceptor> customPostCommandInterceptorsTxRequired;
  
  protected List<CommandInterceptor> commandInterceptorsTxRequired;

  /** this will be initialized during the configurationComplete() */
  protected CommandExecutor commandExecutorTxRequired;
  
  /** the configurable list which will be {@link #initializeInterceptorChain(List) processed} to build the {@link #commandExecutorTxRequiresNew} */
  protected List<CommandInterceptor> customPreCommandInterceptorsTxRequiresNew;
  protected List<CommandInterceptor> customPostCommandInterceptorsTxRequiresNew;

  protected List<CommandInterceptor> commandInterceptorsTxRequiresNew;

  /** this will be initialized during the configurationComplete() */
  protected CommandExecutor commandExecutorTxRequiresNew;

  // SESSIOB FACTORIES ////////////////////////////////////////////////////////

  protected List<Class<?>> customSessionFactoryClasses;
  protected DbSqlSessionFactory dbSqlSessionFactory;
  protected Map<Class<?>, SessionFactory> sessionFactories;
  
  // DEPLOYERS ////////////////////////////////////////////////////////////////

  protected List<Class<?>> customPreDeployerClasses;
  protected List<Class<?>> customPostDeployerClasses;

  protected List<Deployer> customPreDeployers;
  protected List<Deployer> customPostDeployers;

  protected List<Deployer> deployers;

  // JOB EXECUTOR /////////////////////////////////////////////////////////////
  
  protected List<Class<?>> customJobHandlerClasses;
  protected Map<String, JobHandler> jobHandlers;
  protected JobExecutor jobExecutor;

  // MYBATIS SQL SESSION FACTORY //////////////////////////////////////////////
  
  protected SqlSessionFactory sqlSessionFactory;
  protected TransactionFactory transactionFactory;


  // ID GENERATOR /////////////////////////////////////////////////////////////
  protected IdGenerator idGenerator;
  
  // OTHER ////////////////////////////////////////////////////////////////////
  protected List<Class<?>> customFormEngineClasses;
  protected Map<String, FormEngine> formEngines;

  protected List<Class<?>> customFormTypeClasses;
  protected FormTypes formTypes;

  protected List<Class<?>> customScriptingEngineClasses;
  protected ScriptingEngines scriptingEngines;
  
  protected List<Class<?>> customVariableTypeClasses;
  protected VariableTypes variableTypes;
  
  protected ExpressionManager expressionManager;
  protected BusinessCalendarManager businessCalendarManager;

  protected String wsSyncFactoryClassName = DEFAULT_WS_SYNC_FACTORY;

  protected CommandContextFactory commandContextFactory;
  protected TransactionContextFactory transactionContextFactory;
  
  // buildProcessEngine ///////////////////////////////////////////////////////
  
  public ProcessEngine buildProcessEngine() {
    init();
    return new ProcessEngineImpl(this);
  }
  
  // init /////////////////////////////////////////////////////////////////////
  
  protected void init() {
    initExpressionManager();
    initVariableTypes();
    initFormEngines();
    initFormTypes();
    initScriptingEngines();
    initBusinessCalendarManager();
    initCommandContextFactory();
    initTransactionContextFactory();
    initCommandExecutors();
    initServices();
    initDeployers();
    initJobExecutor();
    initDataSource();
    initIdGenerator();
    initTransactionFactory();
    initSqlSessionFactory();
    initSessionFactories();
    initJpa();
  }

  // command executors ////////////////////////////////////////////////////////
  
  protected abstract Collection< ? extends CommandInterceptor> getDefaultCommandInterceptorsTxRequired();
  protected abstract Collection< ? extends CommandInterceptor> getDefaultCommandInterceptorsTxRequiresNew();
  
  protected void initCommandExecutors() {
    initCommandInterceptorsTxRequired();
    initCommandExecutorTxRequired();
    initCommandInterceptorsTxRequiresNew();
    initCommandExecutorTxRequiresNew();
  }

  protected void initCommandInterceptorsTxRequired() {
    if (commandInterceptorsTxRequired==null) {
      if (customPreCommandInterceptorsTxRequired!=null) {
        commandInterceptorsTxRequired = new ArrayList<CommandInterceptor>(customPreCommandInterceptorsTxRequired);
      } else {
        commandInterceptorsTxRequired = new ArrayList<CommandInterceptor>();
      }
      commandInterceptorsTxRequired.addAll(getDefaultCommandInterceptorsTxRequired());
      if (customPostCommandInterceptorsTxRequired!=null) {
        commandInterceptorsTxRequired.addAll(customPostCommandInterceptorsTxRequired);
      }
    }
  }

  protected void initCommandInterceptorsTxRequiresNew() {
    if (commandInterceptorsTxRequiresNew==null) {
      if (customPreCommandInterceptorsTxRequiresNew!=null) {
        commandInterceptorsTxRequiresNew = new ArrayList<CommandInterceptor>(customPreCommandInterceptorsTxRequiresNew);
      } else {
        commandInterceptorsTxRequiresNew = new ArrayList<CommandInterceptor>();
      }
      commandInterceptorsTxRequiresNew.addAll(getDefaultCommandInterceptorsTxRequiresNew());
      if (customPostCommandInterceptorsTxRequiresNew!=null) {
        commandInterceptorsTxRequiresNew.addAll(customPostCommandInterceptorsTxRequiresNew);
      }
    }
  }

  protected void initCommandExecutorTxRequired() {
    if (commandExecutorTxRequired==null) {
      commandExecutorTxRequired = initInterceptorChain(commandInterceptorsTxRequired);
    }
  }

  protected void initCommandExecutorTxRequiresNew() {
    if (commandExecutorTxRequiresNew==null) {
      commandExecutorTxRequiresNew = initInterceptorChain(commandInterceptorsTxRequiresNew);
    }
  }

  protected CommandInterceptor initInterceptorChain(List<CommandInterceptor> chain) {
    if (chain==null || chain.isEmpty()) {
      throw new ActivitiException("invalid command interceptor chain configuration: "+chain);
    }
    for (int i = 0; i < chain.size()-1; i++) {
      chain.get(i).setNext( chain.get(i+1) );
    }
    return chain.get(0);
  }
  
  // services /////////////////////////////////////////////////////////////////
  
  protected void initServices() {
    initService(repositoryService);
    initService(runtimeService);
    initService(historyService);
    initService(identityService);
    initService(taskService);
    initService(formService);
    initService(managementService);
  }

  protected void initService(Object service) {
    if (service instanceof ServiceImpl) {
      ((ServiceImpl)service).setCommandExecutor(commandExecutorTxRequired);
    }
  }
  
  // DataSource ///////////////////////////////////////////////////////////////
  
  protected void initDataSource() {
    if (dataSource==null && jdbcUrl!=null) {
      if ( (jdbcDriver==null) || (jdbcUrl==null) || (jdbcUsername==null) ) {
        throw new ActivitiException("DataSource or JDBC properties have to be specified in a process engine configuration");
      }
      
      PooledDataSource pooledDataSource = 
        new PooledDataSource(ReflectUtil.getClassLoader(), jdbcDriver, jdbcUrl, jdbcUsername, jdbcPassword );
      
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
      
      dataSource = pooledDataSource;
    }
    
    if (dataSource instanceof PooledDataSource) {
      // ACT-233: connection pool of Ibatis is not properely initialized if this is not called!
      ((PooledDataSource)dataSource).forceCloseAll();
    }
  }
  
  // myBatis SqlSessionFactory ////////////////////////////////////////////////
  
  protected void initTransactionFactory() {
    if (transactionFactory==null) {
      if (transactionsExternallyManaged) {
        transactionFactory = new ManagedTransactionFactory();
      } else {
        transactionFactory = new JdbcTransactionFactory();
      }
    }
  }

  protected void initSqlSessionFactory() {
    if (sqlSessionFactory==null) {
      InputStream inputStream = null;
      try {
        inputStream = ReflectUtil.getResourceAsStream("org/activiti/db/ibatis/activiti.ibatis.mem.conf.xml");

        // update the jdbc parameters to the configured ones...
        Environment environment = new Environment("default", transactionFactory, dataSource);
        Reader reader = new InputStreamReader(inputStream);
        XMLConfigBuilder parser = new XMLConfigBuilder(reader);
        Configuration configuration = parser.getConfiguration();
        configuration.setEnvironment(environment);
        configuration.getTypeHandlerRegistry().register(VariableType.class, JdbcType.VARCHAR, new IbatisVariableTypeHandler());
        configuration = parser.parse();

        sqlSessionFactory = new DefaultSqlSessionFactory(configuration);

      } catch (Exception e) {
        throw new ActivitiException("Error while building ibatis SqlSessionFactory: " + e.getMessage(), e);
      } finally {
        IoUtil.closeSilently(inputStream);
      }
    }
  }
  
  // session factories ////////////////////////////////////////////////////////
  
  protected void initSessionFactories() {
    if (sessionFactories==null) {
      sessionFactories = new HashMap<Class<?>, SessionFactory>();

      addSessionFactory(new DbRuntimeSessionFactory());
      addSessionFactory(new DbTaskSessionFactory());
      addSessionFactory(new DbIdentitySessionFactory());
      addSessionFactory(new DbManagementSessionFactory());
      addSessionFactory(new JobExecutorTimerSessionFactory());
      addSessionFactory(new DbHistorySessionFactory());
      
      DbRepositorySessionFactory dbRepositorySessionFactory = new DbRepositorySessionFactory();
      dbRepositorySessionFactory.setDeployers(deployers);
      addSessionFactory(dbRepositorySessionFactory);

      JobExecutorMessageSessionFactory jobExecutorMessageSessionFactory = new JobExecutorMessageSessionFactory();
      addSessionFactory(jobExecutorMessageSessionFactory);
      
      dbSqlSessionFactory = new DbSqlSessionFactory();
      dbSqlSessionFactory.setDatabaseType(databaseType);
      dbSqlSessionFactory.setIdGenerator(idGenerator);
      dbSqlSessionFactory.setSqlSessionFactory(sqlSessionFactory);
      addSessionFactory(dbSqlSessionFactory);
    }
    if (customSessionFactoryClasses!=null) {
      for (Class<?> customSessionFactoryClass: customSessionFactoryClasses) {
        try {
          SessionFactory sessionFactory = (SessionFactory) customSessionFactoryClass.newInstance();
          addSessionFactory(sessionFactory);
        } catch (Exception e) {
          throw new ActivitiException("problem instantiating new custom session factory for class+ "+customSessionFactoryClass+": "+e.getMessage(), e);
        }
      }
    }
  }
  
  protected void addSessionFactory(SessionFactory sessionFactory) {
    sessionFactories.put(sessionFactory.getSessionType(), sessionFactory);
  }
  
  // deployers ////////////////////////////////////////////////////////////////
  
  protected void initDeployers() {
    if (deployers==null) {
      initCustomPreDeployers();
      initCustomPostDeployers();

      deployers = new ArrayList<Deployer>();
      if (customPreDeployers!=null) {
        deployers.addAll(customPreDeployers);
      }
      deployers.addAll(getDefaultDeployers());
      if (customPreDeployers!=null) {
        deployers.addAll(customPreDeployers);
      }
    }
  }

  protected Collection< ? extends Deployer> getDefaultDeployers() {
    List<Deployer> defaultDeployers = new ArrayList<Deployer>();

    BpmnDeployer bpmnDeployer = new BpmnDeployer();
    bpmnDeployer.setExpressionManager(expressionManager);
    BpmnParser bpmnParser = new BpmnParser(expressionManager);
    if (historyLevel>=ProcessEngineConfiguration.HISTORYLEVEL_ACTIVITY) {
      bpmnParser.getParseListeners().add(new HistoryParseListener(historyLevel));
    }
    bpmnDeployer.setBpmnParser(bpmnParser);
    
    defaultDeployers.add(bpmnDeployer);
    return defaultDeployers;
  }

  protected void initCustomPreDeployers() {
    if (customPreDeployers==null && customPreDeployerClasses!=null) {
      customPreDeployers = new ArrayList<Deployer>();
      for (Class<?> customPreDeployerClass: customPreDeployerClasses) {
        try {
          customPreDeployers.add((Deployer) customPreDeployerClass.newInstance());
        } catch (Exception e) {
          new ActivitiException("problem instantiating custom deployer "+customPreDeployerClass+": "+e.getMessage(), e);
        }
      }
    }
  }
  
  protected void initCustomPostDeployers() {
    if (customPostDeployers==null && customPostDeployerClasses!=null) {
      customPostDeployers = new ArrayList<Deployer>();
      for (Class<?> customPostDeployerClass: customPostDeployerClasses) {
        try {
          customPostDeployers.add((Deployer) customPostDeployerClass.newInstance());
        } catch (Exception e) {
          new ActivitiException("problem instantiating custom deployer "+customPostDeployerClass+": "+e.getMessage(), e);
        }
      }
    }
  }
  
  // job executor /////////////////////////////////////////////////////////////
  
  protected void initJobExecutor() {
    if (jobExecutor==null) {
      jobExecutor = new JobExecutor();
    }

    jobHandlers = new HashMap<String, JobHandler>();
    TimerExecuteNestedActivityJobHandler timerExecuteNestedActivityJobHandler = new TimerExecuteNestedActivityJobHandler();
    jobHandlers.put(timerExecuteNestedActivityJobHandler.getType(), timerExecuteNestedActivityJobHandler);
    
    jobExecutor.setCommandExecutor(commandExecutorTxRequired);
    jobExecutor.setAutoActivate(jobExecutorActivate);
  }
  
  // history //////////////////////////////////////////////////////////////////
  
  public static Integer parseHistoryLevel(String historyLevelText) {
    if ("none".equalsIgnoreCase(historyLevelText)) {
      return HISTORYLEVEL_NONE;
    }
    if ("activity".equalsIgnoreCase(historyLevelText)) {
      return HISTORYLEVEL_ACTIVITY;
    }
    if ("audit".equalsIgnoreCase(historyLevelText)) {
      return HISTORYLEVEL_AUDIT;
    }
    if ("full".equalsIgnoreCase(historyLevelText)) {
      return HISTORYLEVEL_FULL;
    }
    throw new ActivitiException("invalid history level: "+historyLevelText);
  }
  
  // id generator /////////////////////////////////////////////////////////////
  
  protected void initIdGenerator() {
    if (idGenerator==null) {
      DbIdGenerator dbIdGenerator = new DbIdGenerator();
      dbIdGenerator.setIdBlockSize(idBlockSize);
      dbIdGenerator.setCommandExecutor(commandExecutorTxRequiresNew);
      idGenerator = dbIdGenerator;
    }
  }
  
  // OTHER ////////////////////////////////////////////////////////////////////
  
  protected void initCommandContextFactory() {
    if (commandContextFactory==null) {
      commandContextFactory = new CommandContextFactory();
      commandContextFactory.setProcessEngineConfiguration(this);
    }
  }

  protected void initTransactionContextFactory() {
    if (transactionContextFactory==null) {
      transactionContextFactory = new StandaloneIbatisTransactionContextFactory();
    }
  }

  protected void initVariableTypes() {
    if (variableTypes==null) {
      variableTypes = new DefaultVariableTypes();
    }
    if (customVariableTypeClasses!=null) {
      for (Class<?> customVariableTypeClass: customVariableTypeClasses) {
        try {
          variableTypes.addType((VariableType) customVariableTypeClass.newInstance());
        } catch (Exception e) {
          new ActivitiException("problem instantiating custom variable type "+customVariableTypeClass+": "+e.getMessage(), e);
        }
      }
    }
  }

  protected void initFormEngines() {
    if (formEngines==null) {
      formEngines = new HashMap<String, FormEngine>();
      FormEngine defaultFormEngine = new JuelFormEngine();
      formEngines.put(null, defaultFormEngine); // default form engine is looked up with null
      formEngines.put(defaultFormEngine.getName(), defaultFormEngine);
    }
    if (customFormEngineClasses!=null) {
      for (Class<?> customFormEngineClass: customFormEngineClasses) {
        try {
          FormEngine formEngine = (FormEngine) customFormEngineClass.newInstance();
          formEngines.put(formEngine.getName(), formEngine);
        } catch (Exception e) {
          new ActivitiException("problem instantiating custom form engine "+customFormEngineClass+": "+e.getMessage(), e);
        }
      }
    }
  }

  protected void initFormTypes() {
    if (formTypes==null) {
      formTypes = new FormTypes();
      formTypes.addFormType(new StringFormType());
      formTypes.addFormType(new LongFormType());
      formTypes.addFormType(new DateFormType("dd/MM/yyyy"));
    }
    if (customFormTypeClasses!=null) {
      for (Class<?> customFormTypeClass: customFormTypeClasses) {
        try {
          FormType formType = (FormType) customFormTypeClass.newInstance();
          formTypes.addFormType((AbstractFormType) formType);
        } catch (Exception e) {
          new ActivitiException("problem instantiating custom form type "+customFormTypeClass+": "+e.getMessage(), e);
        }
      }
    }
  }

  protected void initScriptingEngines() {
    if (scriptingEngines==null) {
      scriptingEngines = new ScriptingEngines();
    }
  }

  protected void initExpressionManager() {
    if (expressionManager==null) {
      expressionManager = new ExpressionManager();
    }
  }

  protected void initBusinessCalendarManager() {
    if (businessCalendarManager==null) {
      MapBusinessCalendarManager mapBusinessCalendarManager = new MapBusinessCalendarManager();
      mapBusinessCalendarManager.addBusinessCalendar(DurationBusinessCalendar.NAME, new DurationBusinessCalendar());
      businessCalendarManager = mapBusinessCalendarManager;
    }
  }
  
  // JPA //////////////////////////////////////////////////////////////////////
  
  protected void initJpa() {
    if(jpaPersistenceUnitName!=null) {
      jpaEntityManagerFactory = JpaHelper.createEntityManagerFactory(jpaPersistenceUnitName);
    }
    if(jpaEntityManagerFactory!=null) {
      sessionFactories.put(EntityManagerSession.class, new EntityManagerSessionFactory(jpaEntityManagerFactory, jpaHandleTransaction, jpaCloseEntityManager));
      VariableType jpaType = variableTypes.getVariableType(JPAEntityVariableType.TYPE_NAME);
      // Add JPA-type
      if(jpaType == null) {
        // We try adding the variable right before SerializableType, if available
        int serializableIndex = variableTypes.getTypeIndex(SerializableType.TYPE_NAME);
        if(serializableIndex > -1) {
          variableTypes.addType(new JPAEntityVariableType(), serializableIndex);
        } else {
          variableTypes.addType(new JPAEntityVariableType());
        }        
      }
    }
  }

  // getters and setters //////////////////////////////////////////////////////
  
  public String getProcessEngineName() {
    return processEngineName;
  }
  
  public ProcessEngineConfigurationImpl setProcessEngineName(String processEngineName) {
    this.processEngineName = processEngineName;
    return this;
  }
  
  public List<CommandInterceptor> getCustomPreCommandInterceptorsTxRequired() {
    return customPreCommandInterceptorsTxRequired;
  }
  
  public ProcessEngineConfigurationImpl setCustomPreCommandInterceptorsTxRequired(List<CommandInterceptor> customPreCommandInterceptorsTxRequired) {
    this.customPreCommandInterceptorsTxRequired = customPreCommandInterceptorsTxRequired;
    return this;
  }
  
  public List<CommandInterceptor> getCustomPostCommandInterceptorsTxRequired() {
    return customPostCommandInterceptorsTxRequired;
  }
  
  public ProcessEngineConfigurationImpl setCustomPostCommandInterceptorsTxRequired(List<CommandInterceptor> customPostCommandInterceptorsTxRequired) {
    this.customPostCommandInterceptorsTxRequired = customPostCommandInterceptorsTxRequired;
    return this;
  }
  
  public List<CommandInterceptor> getCommandInterceptorsTxRequired() {
    return commandInterceptorsTxRequired;
  }
  
  public ProcessEngineConfigurationImpl setCommandInterceptorsTxRequired(List<CommandInterceptor> commandInterceptorsTxRequired) {
    this.commandInterceptorsTxRequired = commandInterceptorsTxRequired;
    return this;
  }
  
  public CommandExecutor getCommandExecutorTxRequired() {
    return commandExecutorTxRequired;
  }
  
  public ProcessEngineConfigurationImpl setCommandExecutorTxRequired(CommandExecutor commandExecutorTxRequired) {
    this.commandExecutorTxRequired = commandExecutorTxRequired;
    return this;
  }
  
  public List<CommandInterceptor> getCustomPreCommandInterceptorsTxRequiresNew() {
    return customPreCommandInterceptorsTxRequiresNew;
  }
  
  public ProcessEngineConfigurationImpl setCustomPreCommandInterceptorsTxRequiresNew(List<CommandInterceptor> customPreCommandInterceptorsTxRequiresNew) {
    this.customPreCommandInterceptorsTxRequiresNew = customPreCommandInterceptorsTxRequiresNew;
    return this;
  }
  
  public List<CommandInterceptor> getCustomPostCommandInterceptorsTxRequiresNew() {
    return customPostCommandInterceptorsTxRequiresNew;
  }
  
  public ProcessEngineConfigurationImpl setCustomPostCommandInterceptorsTxRequiresNew(List<CommandInterceptor> customPostCommandInterceptorsTxRequiresNew) {
    this.customPostCommandInterceptorsTxRequiresNew = customPostCommandInterceptorsTxRequiresNew;
    return this;
  }
  
  public List<CommandInterceptor> getCommandInterceptorsTxRequiresNew() {
    return commandInterceptorsTxRequiresNew;
  }
  
  public ProcessEngineConfigurationImpl setCommandInterceptorsTxRequiresNew(List<CommandInterceptor> commandInterceptorsTxRequiresNew) {
    this.commandInterceptorsTxRequiresNew = commandInterceptorsTxRequiresNew;
    return this;
  }
  
  public CommandExecutor getCommandExecutorTxRequiresNew() {
    return commandExecutorTxRequiresNew;
  }
  
  public ProcessEngineConfigurationImpl setCommandExecutorTxRequiresNew(CommandExecutor commandExecutorTxRequiresNew) {
    this.commandExecutorTxRequiresNew = commandExecutorTxRequiresNew;
    return this;
  }
  
  public RepositoryService getRepositoryService() {
    return repositoryService;
  }
  
  public ProcessEngineConfigurationImpl setRepositoryService(RepositoryService repositoryService) {
    this.repositoryService = repositoryService;
    return this;
  }
  
  public RuntimeService getRuntimeService() {
    return runtimeService;
  }
  
  public ProcessEngineConfigurationImpl setRuntimeService(RuntimeService runtimeService) {
    this.runtimeService = runtimeService;
    return this;
  }
  
  public HistoryService getHistoryService() {
    return historyService;
  }
  
  public ProcessEngineConfigurationImpl setHistoryService(HistoryService historyService) {
    this.historyService = historyService;
    return this;
  }
  
  public IdentityService getIdentityService() {
    return identityService;
  }
  
  public ProcessEngineConfigurationImpl setIdentityService(IdentityService identityService) {
    this.identityService = identityService;
    return this;
  }
  
  public TaskService getTaskService() {
    return taskService;
  }
  
  public ProcessEngineConfigurationImpl setTaskService(TaskService taskService) {
    this.taskService = taskService;
    return this;
  }
  
  public FormService getFormService() {
    return formService;
  }
  
  public ProcessEngineConfigurationImpl setFormService(FormService formService) {
    this.formService = formService;
    return this;
  }
  
  public ManagementService getManagementService() {
    return managementService;
  }
  
  public ProcessEngineConfigurationImpl setManagementService(ManagementService managementService) {
    this.managementService = managementService;
    return this;
  }
  
  public Map<Class< ? >, SessionFactory> getSessionFactories() {
    return sessionFactories;
  }
  
  public ProcessEngineConfigurationImpl setSessionFactories(Map<Class< ? >, SessionFactory> sessionFactories) {
    this.sessionFactories = sessionFactories;
    return this;
  }
  
  public List<Deployer> getDeployers() {
    return deployers;
  }
  
  public ProcessEngineConfigurationImpl setDeployers(List<Deployer> deployers) {
    this.deployers = deployers;
    return this;
  }
  
  public JobExecutor getJobExecutor() {
    return jobExecutor;
  }
  
  public ProcessEngineConfigurationImpl setJobExecutor(JobExecutor jobExecutor) {
    this.jobExecutor = jobExecutor;
    return this;
  }
  
  public IdGenerator getIdGenerator() {
    return idGenerator;
  }
  
  public ProcessEngineConfigurationImpl setIdGenerator(IdGenerator idGenerator) {
    this.idGenerator = idGenerator;
    return this;
  }
  
  public String getWsSyncFactoryClassName() {
    return wsSyncFactoryClassName;
  }
  
  public ProcessEngineConfigurationImpl setWsSyncFactoryClassName(String wsSyncFactoryClassName) {
    this.wsSyncFactoryClassName = wsSyncFactoryClassName;
    return this;
  }
  
  public Map<String, FormEngine> getFormEngines() {
    return formEngines;
  }
  
  public ProcessEngineConfigurationImpl setFormEngines(Map<String, FormEngine> formEngines) {
    this.formEngines = formEngines;
    return this;
  }
  
  public FormTypes getFormTypes() {
    return formTypes;
  }
  
  public ProcessEngineConfigurationImpl setFormTypes(FormTypes formTypes) {
    this.formTypes = formTypes;
    return this;
  }
  
  public ScriptingEngines getScriptingEngines() {
    return scriptingEngines;
  }
  
  public ProcessEngineConfigurationImpl setScriptingEngines(ScriptingEngines scriptingEngines) {
    this.scriptingEngines = scriptingEngines;
    return this;
  }
  
  public VariableTypes getVariableTypes() {
    return variableTypes;
  }
  
  public ProcessEngineConfigurationImpl setVariableTypes(VariableTypes variableTypes) {
    this.variableTypes = variableTypes;
    return this;
  }
  
  public ExpressionManager getExpressionManager() {
    return expressionManager;
  }
  
  public ProcessEngineConfigurationImpl setExpressionManager(ExpressionManager expressionManager) {
    this.expressionManager = expressionManager;
    return this;
  }
  
  public BusinessCalendarManager getBusinessCalendarManager() {
    return businessCalendarManager;
  }
  
  public ProcessEngineConfigurationImpl setBusinessCalendarManager(BusinessCalendarManager businessCalendarManager) {
    this.businessCalendarManager = businessCalendarManager;
    return this;
  }
  
  public CommandContextFactory getCommandContextFactory() {
    return commandContextFactory;
  }
  
  public ProcessEngineConfigurationImpl setCommandContextFactory(CommandContextFactory commandContextFactory) {
    this.commandContextFactory = commandContextFactory;
    return this;
  }
  
  public TransactionContextFactory getTransactionContextFactory() {
    return transactionContextFactory;
  }
  
  public ProcessEngineConfigurationImpl setTransactionContextFactory(TransactionContextFactory transactionContextFactory) {
    this.transactionContextFactory = transactionContextFactory;
    return this;
  }

  
  public List<Class< ? >> getCustomPreCommandInterceptorsTxRequiredClasses() {
    return customPreCommandInterceptorsTxRequiredClasses;
  }

  
  public ProcessEngineConfigurationImpl setCustomPreCommandInterceptorsTxRequiredClasses(List<Class< ? >> customPreCommandInterceptorsTxRequiredClasses) {
    this.customPreCommandInterceptorsTxRequiredClasses = customPreCommandInterceptorsTxRequiredClasses;
    return this;
  }

  
  public List<Class< ? >> getCustomPostCommandInterceptorsTxRequiredClasses() {
    return customPostCommandInterceptorsTxRequiredClasses;
  }

  
  public ProcessEngineConfigurationImpl setCustomPostCommandInterceptorsTxRequiredClasses(List<Class< ? >> customPostCommandInterceptorsTxRequiredClasses) {
    this.customPostCommandInterceptorsTxRequiredClasses = customPostCommandInterceptorsTxRequiredClasses;
    return this;
  }

  
  public List<Class< ? >> getCustomSessionFactoryClasses() {
    return customSessionFactoryClasses;
  }

  
  public ProcessEngineConfigurationImpl setCustomSessionFactoryClasses(List<Class< ? >> customSessionFactoryClasses) {
    this.customSessionFactoryClasses = customSessionFactoryClasses;
    return this;
  }

  
  public List<Class< ? >> getCustomPreDeployerClasses() {
    return customPreDeployerClasses;
  }

  
  public ProcessEngineConfigurationImpl setCustomPreDeployerClasses(List<Class< ? >> customPreDeployerClasses) {
    this.customPreDeployerClasses = customPreDeployerClasses;
    return this;
  }

  
  public List<Class< ? >> getCustomPostDeployerClasses() {
    return customPostDeployerClasses;
  }

  
  public ProcessEngineConfigurationImpl setCustomPostDeployerClasses(List<Class< ? >> customPostDeployerClasses) {
    this.customPostDeployerClasses = customPostDeployerClasses;
    return this;
  }

  
  public List<Deployer> getCustomPreDeployers() {
    return customPreDeployers;
  }

  
  public ProcessEngineConfigurationImpl setCustomPreDeployers(List<Deployer> customPreDeployers) {
    this.customPreDeployers = customPreDeployers;
    return this;
  }

  
  public List<Deployer> getCustomPostDeployers() {
    return customPostDeployers;
  }

  
  public ProcessEngineConfigurationImpl setCustomPostDeployers(List<Deployer> customPostDeployers) {
    this.customPostDeployers = customPostDeployers;
    return this;
  }

  
  public List<Class< ? >> getCustomJobHandlerClasses() {
    return customJobHandlerClasses;
  }

  
  public ProcessEngineConfigurationImpl setCustomJobHandlerClasses(List<Class< ? >> customJobHandlerClasses) {
    this.customJobHandlerClasses = customJobHandlerClasses;
    return this;
  }

  
  public Map<String, JobHandler> getJobHandlers() {
    return jobHandlers;
  }

  
  public ProcessEngineConfigurationImpl setJobHandlers(Map<String, JobHandler> jobHandlers) {
    this.jobHandlers = jobHandlers;
    return this;
  }

  
  public SqlSessionFactory getSqlSessionFactory() {
    return sqlSessionFactory;
  }

  
  public ProcessEngineConfigurationImpl setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
    this.sqlSessionFactory = sqlSessionFactory;
    return this;
  }

  
  public List<Class< ? >> getCustomFormEngineClasses() {
    return customFormEngineClasses;
  }

  
  public ProcessEngineConfigurationImpl setCustomFormEngineClasses(List<Class< ? >> customFormEngineClasses) {
    this.customFormEngineClasses = customFormEngineClasses;
    return this;
  }

  
  public List<Class< ? >> getCustomFormTypeClasses() {
    return customFormTypeClasses;
  }

  
  public ProcessEngineConfigurationImpl setCustomFormTypeClasses(List<Class< ? >> customFormTypeClasses) {
    this.customFormTypeClasses = customFormTypeClasses;
    return this;
  }

  
  public List<Class< ? >> getCustomScriptingEngineClasses() {
    return customScriptingEngineClasses;
  }

  
  public ProcessEngineConfigurationImpl setCustomScriptingEngineClasses(List<Class< ? >> customScriptingEngineClasses) {
    this.customScriptingEngineClasses = customScriptingEngineClasses;
    return this;
  }

  
  public List<Class< ? >> getCustomVariableTypeClasses() {
    return customVariableTypeClasses;
  }

  
  public ProcessEngineConfigurationImpl setCustomVariableTypeClasses(List<Class< ? >> customVariableTypeClasses) {
    this.customVariableTypeClasses = customVariableTypeClasses;
    return this;
  }

  public DbSqlSessionFactory getDbSqlSessionFactory() {
    return dbSqlSessionFactory;
  }

  public ProcessEngineConfigurationImpl setDbSqlSessionFactory(DbSqlSessionFactory dbSqlSessionFactory) {
    this.dbSqlSessionFactory = dbSqlSessionFactory;
    return this;
  }
  
  public TransactionFactory getTransactionFactory() {
    return transactionFactory;
  }

  public ProcessEngineConfigurationImpl setTransactionFactory(TransactionFactory transactionFactory) {
    this.transactionFactory = transactionFactory;
    return this;
  }


  @Override
  public ProcessEngineConfigurationImpl setClassLoader(ClassLoader classLoader) {
    super.setClassLoader(classLoader);
    return this;
  }

  @Override
  public ProcessEngineConfigurationImpl setDatabaseType(String databaseType) {
    super.setDatabaseType(databaseType);
    return this;
  }

  @Override
  public ProcessEngineConfigurationImpl setDataSource(DataSource dataSource) {
    super.setDataSource(dataSource);
    return this;
  }

  @Override
  public ProcessEngineConfigurationImpl setDatabaseSchemaUpdate(String databaseSchemaUpdate) {
    super.setDatabaseSchemaUpdate(databaseSchemaUpdate);
    return this;
  }

  @Override
  public ProcessEngineConfigurationImpl setHistoryLevel(int historyLevel) {
    super.setHistoryLevel(historyLevel);
    return this;
  }

  @Override
  public ProcessEngineConfigurationImpl setIdBlockSize(int idBlockSize) {
    super.setIdBlockSize(idBlockSize);
    return this;
  }

  @Override
  public ProcessEngineConfigurationImpl setJdbcDriver(String jdbcDriver) {
    super.setJdbcDriver(jdbcDriver);
    return this;
  }

  @Override
  public ProcessEngineConfigurationImpl setJdbcPassword(String jdbcPassword) {
    super.setJdbcPassword(jdbcPassword);
    return this;
  }

  @Override
  public ProcessEngineConfigurationImpl setJdbcUrl(String jdbcUrl) {
    super.setJdbcUrl(jdbcUrl);
    return this;
  }

  @Override
  public ProcessEngineConfigurationImpl setJdbcUsername(String jdbcUsername) {
    super.setJdbcUsername(jdbcUsername);
    return this;
  }

  @Override
  public ProcessEngineConfigurationImpl setJobExecutorActivate(boolean jobExecutorActivate) {
    super.setJobExecutorActivate(jobExecutorActivate);
    return this;
  }

  @Override
  public ProcessEngineConfigurationImpl setMailServerDefaultFrom(String mailServerDefaultFrom) {
    super.setMailServerDefaultFrom(mailServerDefaultFrom);
    return this;
  }

  @Override
  public ProcessEngineConfigurationImpl setMailServerHost(String mailServerHost) {
    super.setMailServerHost(mailServerHost);
    return this;
  }

  @Override
  public ProcessEngineConfigurationImpl setMailServerPassword(String mailServerPassword) {
    super.setMailServerPassword(mailServerPassword);
    return this;
  }

  @Override
  public ProcessEngineConfigurationImpl setMailServerPort(int mailServerPort) {
    super.setMailServerPort(mailServerPort);
    return this;
  }

  @Override
  public ProcessEngineConfigurationImpl setMailServerUsername(String mailServerUsername) {
    super.setMailServerUsername(mailServerUsername);
    return this;
  }

  @Override
  public ProcessEngineConfigurationImpl setJdbcMaxActiveConnections(int jdbcMaxActiveConnections) {
    super.setJdbcMaxActiveConnections(jdbcMaxActiveConnections);
    return this;
  }

  @Override
  public ProcessEngineConfigurationImpl setJdbcMaxCheckoutTime(int jdbcMaxCheckoutTime) {
    super.setJdbcMaxCheckoutTime(jdbcMaxCheckoutTime);
    return this;
  }

  @Override
  public ProcessEngineConfigurationImpl setJdbcMaxIdleConnections(int jdbcMaxIdleConnections) {
    super.setJdbcMaxIdleConnections(jdbcMaxIdleConnections);
    return this;
  }

  @Override
  public ProcessEngineConfigurationImpl setJdbcMaxWaitTime(int jdbcMaxWaitTime) {
    super.setJdbcMaxWaitTime(jdbcMaxWaitTime);
    return this;
  }

  @Override
  public ProcessEngineConfigurationImpl setTransactionsExternallyManaged(boolean transactionsExternallyManaged) {
    super.setTransactionsExternallyManaged(transactionsExternallyManaged);
    return this;
  }

  @Override
  public ProcessEngineConfigurationImpl setJpaEntityManagerFactory(Object jpaEntityManagerFactory) {
    this.jpaEntityManagerFactory = jpaEntityManagerFactory;
    return this;
  }

  @Override
  public ProcessEngineConfigurationImpl setJpaHandleTransaction(boolean jpaHandleTransaction) {
    this.jpaHandleTransaction = jpaHandleTransaction;
    return this;
  }
  
  @Override
  public ProcessEngineConfigurationImpl setJpaCloseEntityManager(boolean jpaCloseEntityManager) {
    this.jpaCloseEntityManager = jpaCloseEntityManager;
    return this;
  }
}
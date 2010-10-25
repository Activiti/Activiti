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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.DbSchemaStrategy;
import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.FormServiceImpl;
import org.activiti.engine.impl.HistoryServiceImpl;
import org.activiti.engine.impl.IdentityServiceImpl;
import org.activiti.engine.impl.ManagementServiceImpl;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.RuntimeServiceImpl;
import org.activiti.engine.impl.TaskServiceImpl;
import org.activiti.engine.impl.bpmn.deployer.BpmnDeployer;
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
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.db.DbSqlSessionFactory;
import org.activiti.engine.impl.db.DbTaskSessionFactory;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.form.DateFormType;
import org.activiti.engine.impl.form.FormEngine;
import org.activiti.engine.impl.form.FormTypes;
import org.activiti.engine.impl.form.JuelFormEngine;
import org.activiti.engine.impl.form.LongFormType;
import org.activiti.engine.impl.form.StringFormType;
import org.activiti.engine.impl.history.handler.HistoryTaskAssignmentHandler;
import org.activiti.engine.impl.interceptor.CommandContextFactory;
import org.activiti.engine.impl.interceptor.CommandContextInterceptor;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.interceptor.CommandExecutorImpl;
import org.activiti.engine.impl.interceptor.CommandInterceptor;
import org.activiti.engine.impl.interceptor.LogInterceptor;
import org.activiti.engine.impl.interceptor.SessionFactory;
import org.activiti.engine.impl.jobexecutor.JobExecutor;
import org.activiti.engine.impl.jobexecutor.JobExecutorMessageSessionFactory;
import org.activiti.engine.impl.jobexecutor.JobExecutorTimerSessionFactory;
import org.activiti.engine.impl.jobexecutor.JobHandlers;
import org.activiti.engine.impl.jobexecutor.TimerExecuteNestedActivityJobHandler;
import org.activiti.engine.impl.repository.Deployer;
import org.activiti.engine.impl.scripting.ScriptingEngines;
import org.activiti.engine.impl.task.TaskListener;
import org.activiti.engine.impl.variable.DefaultVariableTypes;
import org.activiti.engine.impl.variable.EntityManagerSession;
import org.activiti.engine.impl.variable.EntityManagerSessionFactory;
import org.activiti.engine.impl.variable.JPAEntityVariableType;
import org.activiti.engine.impl.variable.SerializableType;
import org.activiti.engine.impl.variable.VariableType;
import org.activiti.engine.impl.variable.VariableTypes;

/**
 * @author Tom Baeyens
 */
public class ProcessEngineConfiguration {

  public static final String DEFAULT_DATABASE_NAME = "h2";
  public static final String DEFAULT_JDBC_DRIVER = "org.h2.Driver";
  public static final String DEFAULT_JDBC_URL = "jdbc:h2:mem:activiti";
  public static final String DEFAULT_JDBC_USERNAME = "sa";
  public static final String DEFAULT_JDBC_PASSWORD = "";
  
  public static final String DEFAULT_WS_SYNC_FACTORY = "org.activiti.engine.impl.webservice.CxfWebServiceClientFactory";
  
  public static final String DEFAULT_FROM_EMAIL_ADDRESS = "noreply@activiti.org";
  public static final int DEFAULT_MAIL_SERVER_SMTP_PORT = 25;
  
  public static final String DBSCHEMASTRATEGY_CREATE = "create";
  public static final String DBSCHEMASTRATEGY_CREATE_IF_NECESSARY = "create-if-necessary";
  public static final String DBSCHEMASTRATEGY_DROP_CREATE = "drop-create";

  public static final int HISTORYLEVEL_NONE = 0;
  public static final int HISTORYLEVEL_ACTIVITY = 1;
  public static final int HISTORYLEVEL_AUDIT = 2;
  public static final int HISTORYLEVEL_FULL = 3;
  
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


  protected String processEngineName;

  /** the configurable list which will be {@link #initializeInterceptorChain(List) processed} to build the {@link #commandExecutorTxRequired} */
  protected List<CommandInterceptor> commandInterceptorsTxRequired;
  /** this will be initialized during the configurationComplete() */
  protected CommandExecutor commandExecutorTxRequired;
  /** the configurable list which will be {@link #initializeInterceptorChain(List) processed} to build the {@link #commandExecutorTxRequiresNew} */
  protected List<CommandInterceptor> commandInterceptorsTxRequiresNew;
  /** this will be initialized during the configurationComplete() */
  protected CommandExecutor commandExecutorTxRequiresNew;

  protected CommandContextFactory commandContextFactory;
  protected TransactionContextFactory transactionContextFactory;

  protected RepositoryService repositoryService;
  protected RuntimeService runtimeService;
  protected HistoryService historyService;
  protected IdentityService identityService;
  protected TaskService taskService;
  protected FormService formService;
  protected ManagementService managementService;
  
  protected Map<Class<?>, SessionFactory> sessionFactories;
  protected List<Deployer> deployers;

  protected JobExecutor jobExecutor;
  protected JobHandlers jobHandlers;
  protected boolean jobExecutorAutoActivate;
  
  protected String databaseName;
  protected String dbSchemaStrategy;
  protected IdGenerator idGenerator;
  protected long idBlockSize;
  protected DataSource dataSource;
  protected boolean localTransactions;
  protected String jdbcDriver;
  protected String jdbcUrl;
  protected String jdbcUsername;
  protected String jdbcPassword;

  protected ScriptingEngines scriptingEngines;
  protected VariableTypes variableTypes;
  protected ExpressionManager expressionManager;
  protected BusinessCalendarManager businessCalendarManager;
  
  protected int historyLevel = HISTORYLEVEL_AUDIT;
  protected Map<String, List<TaskListener>> taskListeners;
  
  protected boolean isConfigurationCompleted = false;
  
  protected String wsSyncFactoryClassName;
  
  protected String mailServerSmtpHost;
  protected String mailServerSmtpUserName;
  protected String mailServerSmtpPassword;
  protected int mailServerSmtpPort;
  protected String mailServerDefaultFrom;
  
  protected Map<String, FormEngine> formEngines;
  protected FormTypes formTypes;

  protected static ThreadLocal<ClassLoader> currentClassLoaderParameter = new ThreadLocal<ClassLoader>();
  protected ClassLoader classLoader;
  

  public ProcessEngineConfiguration() {
    processEngineName = ProcessEngines.NAME_DEFAULT;

    commandInterceptorsTxRequired = new ArrayList<CommandInterceptor>();
    commandInterceptorsTxRequired.add(new LogInterceptor());
    commandInterceptorsTxRequired.add(new CommandContextInterceptor());
    commandInterceptorsTxRequired.add(new CommandExecutorImpl());
    commandInterceptorsTxRequiresNew = commandInterceptorsTxRequired;
    
    commandContextFactory = new CommandContextFactory(this);
    transactionContextFactory = new StandaloneIbatisTransactionContextFactory();

    repositoryService = new RepositoryServiceImpl();
    runtimeService = new RuntimeServiceImpl();
    taskService = new TaskServiceImpl();
    formService = new FormServiceImpl();
    managementService = new ManagementServiceImpl();
    identityService = new IdentityServiceImpl();
    historyService = new HistoryServiceImpl();

    sessionFactories = new HashMap<Class<?>, SessionFactory>();
    sessionFactories.put(RepositorySession.class, new DbRepositorySessionFactory());
    sessionFactories.put(RuntimeSession.class, new DbRuntimeSessionFactory());
    sessionFactories.put(TaskSession.class, new DbTaskSessionFactory());
    sessionFactories.put(IdentitySession.class, new DbIdentitySessionFactory());
    sessionFactories.put(ManagementSession.class, new DbManagementSessionFactory());
    sessionFactories.put(MessageSession.class, new JobExecutorMessageSessionFactory());
    sessionFactories.put(TimerSession.class, new JobExecutorTimerSessionFactory());
    sessionFactories.put(DbSqlSession.class, new DbSqlSessionFactory());
    sessionFactories.put(HistorySession.class, new DbHistorySessionFactory());
    
    deployers = new ArrayList<Deployer>();
    deployers.add(new BpmnDeployer());

    jobHandlers = new JobHandlers();
    jobHandlers.addJobHandler(new TimerExecuteNestedActivityJobHandler());
    jobExecutor = new JobExecutor();
    jobExecutorAutoActivate = false;
    
    databaseName = DEFAULT_DATABASE_NAME;
    dbSchemaStrategy = DbSchemaStrategy.CREATE_DROP;
    idGenerator = new DbIdGenerator();
    idBlockSize = 100;
    dataSource = null;
    localTransactions = true;
    jdbcDriver = DEFAULT_JDBC_DRIVER;
    jdbcUrl = DEFAULT_JDBC_URL;
    jdbcUsername = DEFAULT_JDBC_USERNAME;
    jdbcPassword = DEFAULT_JDBC_PASSWORD;

    scriptingEngines = new ScriptingEngines();
    variableTypes = new DefaultVariableTypes();
    expressionManager = new ExpressionManager();
    MapBusinessCalendarManager mapBusinessCalendarManager = new MapBusinessCalendarManager();
    mapBusinessCalendarManager.addBusinessCalendar(DurationBusinessCalendar.NAME, new DurationBusinessCalendar());
    businessCalendarManager = mapBusinessCalendarManager;
    
    mailServerDefaultFrom = DEFAULT_FROM_EMAIL_ADDRESS;
    mailServerSmtpPort = DEFAULT_MAIL_SERVER_SMTP_PORT;

    formEngines = new HashMap<String, FormEngine>();
    FormEngine defaultFormEngine = new JuelFormEngine();
    formEngines.put(null, defaultFormEngine); // default form engine is looked up with null
    formEngines.put("juel", defaultFormEngine);
    
    formTypes = new FormTypes();
    formTypes.addFormType(new StringFormType());
    formTypes.addFormType(new LongFormType());
    formTypes.addFormType(new DateFormType("dd/MM/yyyy"));
  }
  
  public ProcessEngine buildProcessEngine() {
    configurationComplete();
    classLoader = currentClassLoaderParameter.get();
    
    return new ProcessEngineImpl(this);
  }

  public static void setCurrentClassLoaderParameter(ClassLoader currentClassLoader) {
    currentClassLoaderParameter.set(currentClassLoader);
  }
  
  protected void configurationComplete() {
    if (!isConfigurationCompleted) {
      commandExecutorTxRequired = initializeInterceptorChain(commandInterceptorsTxRequired);
      if (commandInterceptorsTxRequiresNew!=commandInterceptorsTxRequired) {
        commandExecutorTxRequiresNew = initializeInterceptorChain(commandInterceptorsTxRequiresNew);
      } else {
        commandExecutorTxRequiresNew = commandExecutorTxRequired;
      }
      
      notifyConfigurationComplete(commandContextFactory);
      notifyConfigurationComplete(transactionContextFactory);
      notifyConfigurationComplete(repositoryService);
      notifyConfigurationComplete(runtimeService);
      notifyConfigurationComplete(taskService);
      notifyConfigurationComplete(formService);
      notifyConfigurationComplete(managementService);
      notifyConfigurationComplete(identityService);
      notifyConfigurationComplete(historyService);
      
      for (SessionFactory sessionFactory : sessionFactories.values()) {
        notifyConfigurationComplete(sessionFactory);
      }
      
      for (Deployer deployer : deployers) {
        notifyConfigurationComplete(deployer);
      }
      
      notifyConfigurationComplete(jobHandlers);
      notifyConfigurationComplete(jobExecutor);
      notifyConfigurationComplete(idGenerator);
      notifyConfigurationComplete(dataSource);
      notifyConfigurationComplete(scriptingEngines);
      notifyConfigurationComplete(variableTypes);
      notifyConfigurationComplete(expressionManager);
      notifyConfigurationComplete(businessCalendarManager);

      if (historyLevel>=HISTORYLEVEL_ACTIVITY) {
        addTaskListener(TaskListener.EVENTNAME_ASSIGNMENT, new HistoryTaskAssignmentHandler());
      }
      
      isConfigurationCompleted = true;
    }
  }
  
  protected CommandInterceptor initializeInterceptorChain(List<CommandInterceptor> chain) {
    if (chain==null || chain.isEmpty()) {
      throw new ActivitiException("invalid command interceptor chain configuration: "+chain);
    }
    for (int i = 0; i < chain.size()-1; i++) {
      chain.get(i).setNext( chain.get(i+1) );
    }
    for (int i = 0; i < chain.size(); i++) {
      CommandInterceptor commandExecutor = chain.get(i);
      if (commandExecutor instanceof ProcessEngineConfigurationAware) {
        ((ProcessEngineConfigurationAware)commandExecutor).configurationCompleted(this);
      }
    }
    return chain.get(0);
  }
  
  public void addTaskListener(String taskEventName, TaskListener taskListener) {
    if (taskListeners==null) {
      taskListeners = new HashMap<String, List<TaskListener>>();
    }
    List<TaskListener> taskEventListeners = taskListeners.get(taskEventName);
    if (taskEventListeners==null) {
      taskEventListeners = new ArrayList<TaskListener>();
      taskListeners.put(taskEventName, taskEventListeners);
    }
    taskEventListeners.add(taskListener);
  }

  protected void notifyConfigurationComplete(Object object) {
    if (object instanceof ProcessEngineConfigurationAware) {
      ((ProcessEngineConfigurationAware)object).configurationCompleted(this);
    }
  }

  public void dbSchemaCreate() {
    configurationComplete();
    getDbSqlSessionFactory().dbSchemaCreate();
  }

  public void dbSchemaDrop() {
    configurationComplete();
    getDbSqlSessionFactory().dbSchemaDrop();
  }
  
  public DbSqlSessionFactory getDbSqlSessionFactory() {
    return (DbSqlSessionFactory) sessionFactories.get(DbSqlSession.class);
  }
  
  // configuration update methods /////////////////////////////////////////////

  public void addCommandInterceptorsTxRequired(CommandInterceptor commandInterceptor) {
    commandInterceptorsTxRequired.add(commandInterceptor);
  }

  public void addCommandInterceptorsTxRequiresNew(CommandInterceptor commandInterceptor) {
    commandInterceptorsTxRequiresNew.add(commandInterceptor);
  }
  
  public void enableJPA(Object entityManagerFactory, boolean handleTransaction, boolean closeEntityManager) {
    if(entityManagerFactory ==  null) {
      throw new ActivitiException("entityManagerFactory is null, JPA cannot be enabled");
    }
    if(!sessionFactories.containsKey(EntityManagerSession.class)) {
      sessionFactories.put(EntityManagerSession.class, new EntityManagerSessionFactory(entityManagerFactory, true, true));
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
    } else {
      throw new ActivitiException("JPA is already enabled");
    }
  }

  // getters and setters //////////////////////////////////////////////////////

  public String getProcessEngineName() {
    return processEngineName;
  }

  public void setProcessEngineName(String processEngineName) {
    this.processEngineName = processEngineName;
  }

  public JobExecutor getJobExecutor() {
    return jobExecutor;
  }

  public String getDbSchemaStrategy() {
    return dbSchemaStrategy;
  }

  public void setDbSchemaStrategy(String dbSchemaStrategy) {
    this.dbSchemaStrategy = dbSchemaStrategy;
  }

  public RuntimeService getProcessService() {
    return runtimeService;
  }

  public void setHistoryService(HistoryService historyService) {
    this.historyService = historyService;
  }

  public HistoryService getHistoryService() {
    return historyService;
  }

  public IdentityService getIdentityService() {
    return identityService;
  }

  public TaskService getTaskService() {
    return taskService;
  }

  public ManagementService getManagementService() {
    return managementService;
  }

  public CommandContextFactory getCommandContextFactory() {
    return commandContextFactory;
  }

  public VariableTypes getVariableTypes() {
    return variableTypes;
  }

  public IdGenerator getIdGenerator() {
    return idGenerator;
  }

  public CommandExecutor getCommandExecutorTxRequired() {
    return commandExecutorTxRequired;
  }

  public JobHandlers getJobHandlers() {
    return jobHandlers;
  }

  public void setProcessService(RuntimeService runtimeService) {
    this.runtimeService = runtimeService;
  }

  public void setIdentityService(IdentityService identityService) {
    this.identityService = identityService;
  }
  
  public void setTaskService(TaskService taskService) {
    this.taskService = taskService;
  }
  
  public void setManagementService(ManagementService managementService) {
    this.managementService = managementService;
  }
  
  public void setVariableTypes(VariableTypes variableTypes) {
    this.variableTypes = variableTypes;
  }
  
  public void setJobExecutor(JobExecutor jobExecutor) {
    this.jobExecutor = jobExecutor;
  }
 
  public void setJobHandlers(JobHandlers jobHandlers) {
    this.jobHandlers = jobHandlers;
  }
 
  public void setIdGenerator(IdGenerator idGenerator) {
    this.idGenerator = idGenerator;
  }
  
  public void setCommandContextFactory(CommandContextFactory commandContextFactory) {
    this.commandContextFactory = commandContextFactory;
  }

  public Map<Class< ? >, SessionFactory> getSessionFactories() {
    return sessionFactories;
  }

  public void setSessionFactories(Map<Class< ? >, SessionFactory> sessionFactories) {
    this.sessionFactories = sessionFactories;
  }

  public RepositoryService getRepositoryService() {
    return repositoryService;
  }
  
  public void setRepositoryService(RepositoryService repositoryService) {
    this.repositoryService = repositoryService;
  }
  
  public ScriptingEngines getScriptingEngines() {
    return scriptingEngines;
  }
  
  public void setScriptingEngines(ScriptingEngines scriptingEngines) {
    this.scriptingEngines = scriptingEngines;
  }

  public ExpressionManager getExpressionManager() {
    return expressionManager;
  }

  public void setExpressionManager(ExpressionManager expressionManager) {
    this.expressionManager = expressionManager;
  }

  public BusinessCalendarManager getBusinessCalendarManager() {
    return businessCalendarManager;
  }
  
  public void setBusinessCalendarManager(BusinessCalendarManager businessCalendarManager) {
    this.businessCalendarManager = businessCalendarManager;
  }

  public TransactionContextFactory getTransactionContextFactory() {
    return transactionContextFactory;
  }

  public void setTransactionContextFactory(TransactionContextFactory transactionContextFactory) {
    this.transactionContextFactory = transactionContextFactory;
  }

  public String getDatabaseName() {
    return databaseName;
  }

  public void setDatabaseName(String databaseName) {
    this.databaseName = databaseName;
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  public void setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public String getJdbcDriver() {
    return jdbcDriver;
  }

  public void setJdbcDriver(String jdbcDriver) {
    this.jdbcDriver = jdbcDriver;
  }

  public String getJdbcUrl() {
    return jdbcUrl;
  }

  public void setJdbcUrl(String jdbcUrl) {
    this.jdbcUrl = jdbcUrl;
  }

  public String getJdbcUsername() {
    return jdbcUsername;
  }

  public void setJdbcUsername(String jdbcUsername) {
    this.jdbcUsername = jdbcUsername;
  }

  public String getJdbcPassword() {
    return jdbcPassword;
  }

  public void setJdbcPassword(String jdbcPassword) {
    this.jdbcPassword = jdbcPassword;
  }

  public boolean isJobExecutorAutoActivate() {
    return jobExecutorAutoActivate;
  }

  public void setJobExecutorAutoActivate(boolean jobExecutorAutoActivate) {
    this.jobExecutorAutoActivate = jobExecutorAutoActivate;
  }

  public boolean isLocalTransactions() {
    return localTransactions;
  }

  public void setLocalTransactions(boolean localTransactions) {
    this.localTransactions = localTransactions;
  }
  
  public List<Deployer> getDeployers() {
    return deployers;
  }
  
  public void setDeployers(List<Deployer> deployers) {
    this.deployers = deployers;
  }

  public long getIdBlockSize() {
    return idBlockSize;
  }

  public void setIdBlockSize(long idBlockSize) {
    this.idBlockSize = idBlockSize;
  }

  public String getWsSyncFactoryClassName() {
    return wsSyncFactoryClassName;
  }

  public void setWsSyncFactoryClassName(String wsSyncFactoryClassName) {
    this.wsSyncFactoryClassName = wsSyncFactoryClassName;
  }
  
  public String getMailServerSmtpHost() {
    return mailServerSmtpHost;
  }
  
  public void setMailServerSmtpHost(String mailServerSmtpHost) {
    this.mailServerSmtpHost = mailServerSmtpHost;
  }
  
  public String getMailServerSmtpUserName() {
    return mailServerSmtpUserName;
  }
  
  public void setMailServerSmtpUserName(String mailServerSmtpUserName) {
    this.mailServerSmtpUserName = mailServerSmtpUserName;
  }
  
  public String getMailServerSmtpPassword() {
    return mailServerSmtpPassword;
  }

  public void setMailServerSmtpPassword(String mailServerSmtpPassword) {
    this.mailServerSmtpPassword = mailServerSmtpPassword;
  }
  
  public int getMailServerSmtpPort() {
    return mailServerSmtpPort;
  }
  
  public void setMailServerSmtpPort(int mailServerSmtpPort) {
    this.mailServerSmtpPort = mailServerSmtpPort;
  }
  
  public String getMailServerDefaultFrom() {
    return mailServerDefaultFrom;
  }

  
  public void setMailServerDefaultFrom(String mailServerDefaultFrom) {
    this.mailServerDefaultFrom = mailServerDefaultFrom;
  }

  public Map<String, List<TaskListener>> getTaskListeners() {
    return taskListeners;
  }
  
  public void setTaskListeners(Map<String, List<TaskListener>> taskListeners) {
    this.taskListeners = taskListeners;
  }

  
  public List<CommandInterceptor> getCommandInterceptorsTxRequired() {
    return commandInterceptorsTxRequired;
  }

  
  public void setCommandInterceptorsTxRequired(List<CommandInterceptor> commandInterceptorsTxRequired) {
    this.commandInterceptorsTxRequired = commandInterceptorsTxRequired;
  }

  public List<CommandInterceptor> getCommandInterceptorsTxRequiresNew() {
    return commandInterceptorsTxRequiresNew;
  }

  public void setCommandInterceptorsTxRequiresNew(List<CommandInterceptor> commandInterceptorsTxRequiresNew) {
    this.commandInterceptorsTxRequiresNew = commandInterceptorsTxRequiresNew;
  }

  public CommandExecutor getCommandExecutorTxRequiresNew() {
    return commandExecutorTxRequiresNew;
  }
  
  public int getHistoryLevel() {
    return historyLevel;
  }
  
  public void setHistoryLevel(int historyLevel) {
    this.historyLevel = historyLevel;
  }
  
  public Map<String, FormEngine> getFormEngines() {
    return formEngines;
  }

  public void setFormEngines(Map<String, FormEngine> formEngines) {
    this.formEngines = formEngines;
  }

  public FormService getFormService() {
    return formService;
  }

  public void setFormService(FormService formService) {
    this.formService = formService;
  }
  
  public FormTypes getFormTypes() {
    return formTypes;
  }
  
  public void setFormTypes(FormTypes formTypes) {
    this.formTypes = formTypes;
  }

  public ClassLoader getClassLoader() {
    return classLoader;
  }
}

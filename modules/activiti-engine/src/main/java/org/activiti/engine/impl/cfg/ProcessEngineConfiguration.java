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

import org.activiti.engine.DbSchemaStrategy;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.HistoricDataServiceImpl;
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
import org.activiti.engine.impl.db.DbIdGenerator;
import org.activiti.engine.impl.db.DbIdentitySessionFactory;
import org.activiti.engine.impl.db.DbManagementSessionFactory;
import org.activiti.engine.impl.db.DbRepositorySessionFactory;
import org.activiti.engine.impl.db.DbRuntimeSessionFactory;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.db.DbSqlSessionFactory;
import org.activiti.engine.impl.db.DbTaskSessionFactory;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.interceptor.CommandContextFactory;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.interceptor.DefaultCommandExecutor;
import org.activiti.engine.impl.interceptor.SessionFactory;
import org.activiti.engine.impl.jobexecutor.JobExecutor;
import org.activiti.engine.impl.jobexecutor.JobExecutorMessageSessionFactory;
import org.activiti.engine.impl.jobexecutor.JobExecutorTimerSessionFactory;
import org.activiti.engine.impl.jobexecutor.JobHandlers;
import org.activiti.engine.impl.jobexecutor.TimerExecuteNestedActivityJobHandler;
import org.activiti.engine.impl.repository.Deployer;
import org.activiti.engine.impl.scripting.ScriptingEngines;
import org.activiti.engine.impl.variable.DefaultVariableTypes;
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

  protected String processEngineName;

  protected CommandExecutor commandExecutor;
  protected CommandContextFactory commandContextFactory;
  protected TransactionContextFactory transactionContextFactory;

  protected RepositoryService repositoryService;
  protected RuntimeService runtimeService;
  protected HistoryService historicDataService;
  protected IdentityService identityService;
  protected TaskService taskService;
  protected ManagementService managementService;
  
  protected Map<Class<?>, SessionFactory> sessionFactories;
  protected List<Deployer> deployers;

  protected JobExecutor jobExecutor;
  protected JobHandlers jobHandlers;
  protected boolean jobExecutorAutoActivate;
  
  protected String databaseName;
  protected DbSchemaStrategy dbSchemaStrategy;
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
  
  protected boolean isConfigurationCompleted = false;

  public ProcessEngineConfiguration() {
    processEngineName = ProcessEngines.NAME_DEFAULT;

    commandExecutor = new DefaultCommandExecutor();
    commandContextFactory = new CommandContextFactory(this);
    transactionContextFactory = new StandaloneIbatisTransactionContextFactory();

    repositoryService = new RepositoryServiceImpl();
    runtimeService = new RuntimeServiceImpl();
    taskService = new TaskServiceImpl();
    managementService = new ManagementServiceImpl();
    identityService = new IdentityServiceImpl();
    historicDataService = new HistoricDataServiceImpl();

    sessionFactories = new HashMap<Class<?>, SessionFactory>();
    sessionFactories.put(RepositorySession.class, new DbRepositorySessionFactory());
    sessionFactories.put(RuntimeSession.class, new DbRuntimeSessionFactory());
    sessionFactories.put(TaskSession.class, new DbTaskSessionFactory());
    sessionFactories.put(IdentitySession.class, new DbIdentitySessionFactory());
    sessionFactories.put(ManagementSession.class, new DbManagementSessionFactory());
    sessionFactories.put(MessageSession.class, new JobExecutorMessageSessionFactory());
    sessionFactories.put(TimerSession.class, new JobExecutorTimerSessionFactory());
    sessionFactories.put(DbSqlSession.class, new DbSqlSessionFactory());
    
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
  }
  
  public ProcessEngine buildProcessEngine() {
    configurationComplete();

    return new ProcessEngineImpl(this);
  }

  protected void configurationComplete() {
    if (!isConfigurationCompleted) {
      notifyConfigurationComplete(commandExecutor);
      notifyConfigurationComplete(commandContextFactory);
      notifyConfigurationComplete(transactionContextFactory);
      notifyConfigurationComplete(repositoryService);
      notifyConfigurationComplete(runtimeService);
      notifyConfigurationComplete(taskService);
      notifyConfigurationComplete(managementService);
      notifyConfigurationComplete(identityService);
      notifyConfigurationComplete(historicDataService);
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
      
      isConfigurationCompleted = true;
    }
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

  public DbSchemaStrategy getDbSchemaStrategy() {
    return dbSchemaStrategy;
  }

  public void setDbSchemaStrategy(DbSchemaStrategy dbSchemaStrategy) {
    this.dbSchemaStrategy = dbSchemaStrategy;
  }

  public RuntimeService getProcessService() {
    return runtimeService;
  }

  public void setHistoricDataService(HistoryService historicDataService) {
    this.historicDataService = historicDataService;
  }

  public HistoryService getHistoricDataService() {
    return historicDataService;
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

  public CommandExecutor getCommandExecutor() {
    return commandExecutor;
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
  
  public void setCommandExecutor(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
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
}

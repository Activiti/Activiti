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
package org.activiti.impl.cfg;

import java.util.HashMap;
import java.util.Map;

import javax.el.ELResolver;
import javax.sql.DataSource;

import org.activiti.DbSchemaStrategy;
import org.activiti.IdentityService;
import org.activiti.engine.impl.persistence.RepositorySession;
import org.activiti.engine.impl.persistence.db.DbRepositorySessionFactory;
import org.activiti.engine.impl.persistence.db.DbSqlSession;
import org.activiti.engine.impl.persistence.db.DbSqlSessionFactory;
import org.activiti.impl.IdentityServiceImpl;
import org.activiti.impl.ManagementServiceImpl;
import org.activiti.impl.ProcessEngineImpl;
import org.activiti.impl.ProcessServiceImpl;
import org.activiti.impl.TaskServiceImpl;
import org.activiti.impl.bpmn.BpmnDeployer;
import org.activiti.impl.calendar.BusinessCalendarManager;
import org.activiti.impl.calendar.DurationBusinessCalendar;
import org.activiti.impl.calendar.MapBusinessCalendarManager;
import org.activiti.impl.db.IdGenerator;
import org.activiti.impl.el.ExpressionManager;
import org.activiti.impl.event.DefaultProcessEventBus;
import org.activiti.HistoricDataService;
import org.activiti.impl.history.HistoricDataServiceImpl;
import org.activiti.impl.interceptor.CommandContextFactory;
import org.activiti.impl.interceptor.CommandExecutor;
import org.activiti.impl.interceptor.DefaultCommandExecutor;
import org.activiti.impl.interceptor.SessionFactory;
import org.activiti.impl.job.JobHandlers;
import org.activiti.impl.job.TimerExecuteNestedActivityJobHandler;
import org.activiti.impl.jobexecutor.JobExecutor;
import org.activiti.impl.msg.JobExecutorMessageSessionFactory;
import org.activiti.impl.msg.MessageSession;
import org.activiti.impl.msg.MessageSessionFactory;
import org.activiti.impl.persistence.CachingPersistenceSessionFactory;
import org.activiti.impl.persistence.IbatisPersistenceSessionFactory;
import org.activiti.impl.persistence.PersistenceSession;
import org.activiti.impl.persistence.PersistenceSessionFactory;
import org.activiti.impl.repository.DeployerManager;
import org.activiti.impl.scripting.ScriptingEngines;
import org.activiti.impl.timer.JobExecutorTimerSessionFactory;
import org.activiti.impl.timer.TimerSession;
import org.activiti.impl.tx.StandaloneTransactionContextFactory;
import org.activiti.impl.tx.TransactionContextFactory;
import org.activiti.impl.variable.DefaultVariableTypes;
import org.activiti.impl.variable.VariableTypes;
import org.activiti.pvm.event.ProcessEventBus;

/**
 * @author Dave Syer
 * @author Christian Stettler
 */
public class ProcessEngineFactory {

  private String processEngineName;
  private ProcessEventBus processEventBus;

  private HistoricDataService historicDataService;
  private IdentityService identityService;

  private VariableTypes variableTypes;
  private JobExecutor jobExecutor;
  private IdGenerator idGenerator;
  private CommandExecutor commandExecutor;

  private boolean jobExecutorAutoActivate = true;
  private DbSchemaStrategy dbSchemaStrategy;
  private DataSource dataSource;
  private String databaseName = "h2";
  private boolean localTransactions = true;

  private final CommandContextFactory commandContextFactory;

  private boolean initialized = false;
  private Object lock = new Object();

  private final ProcessEngineConfiguration configuration = new ProcessEngineConfiguration();

  private ELResolver elResolver;

  public ProcessEngineFactory() {
    this.commandContextFactory = createDefaultCommandContextFactory();
  }

  public void init() {

    if (initialized) {
      return;
    }

    synchronized (lock) {

      if (!initialized) {

        ExpressionManager expressionManager = createDefaultExpressionManager();
        if (elResolver != null) {
          expressionManager.setElResolver(elResolver);
        }
        ScriptingEngines scriptingEngines = createDefaultScriptingEngines();
        BusinessCalendarManager businessCalendarManager = createDefaultBusinessCalendarManager();

        variableTypes = variableTypes == null ? createDefaultVariableTypes() : variableTypes;
        dbSchemaStrategy = dbSchemaStrategy == null ? createDefaultDbSchemaStrategy() : dbSchemaStrategy;
        JobHandlers jobHandlers = createDefaultJobHandlers();

        DeployerManager deployerManager = createDefaultDeployerManager(expressionManager, scriptingEngines, businessCalendarManager);

        commandExecutor = createDefaultCmdExecutor(this.commandContextFactory);

        processEventBus = processEventBus == null ? createDefaultProcessEventBus() : processEventBus;
        jobExecutor = jobExecutor == null ? createDefaultJobExecutor(commandExecutor, jobHandlers, jobExecutorAutoActivate) : jobExecutor;
        historicDataService = historicDataService == null ? createDefaultHistoricDataService(commandExecutor, processEventBus) : historicDataService;
        idGenerator = idGenerator == null ? createDefaultIdGenerator(commandExecutor) : idGenerator;
        identityService = identityService == null ? createDefaultIdentityService(commandExecutor) : identityService;

        ProcessServiceImpl processService = createDefaultProcessService(commandExecutor, deployerManager, scriptingEngines);
        TaskServiceImpl taskService = createDefaultTaskService(commandExecutor, scriptingEngines);
        ManagementServiceImpl managementService = createDefaultManagementService(commandExecutor);

        IbatisPersistenceSessionFactory ibatisPersistenceSessionFactory = new IbatisPersistenceSessionFactory(variableTypes, idGenerator, databaseName, dataSource, localTransactions);
        CachingPersistenceSessionFactory cachingPersistenceSessionFactory = new CachingPersistenceSessionFactory(ibatisPersistenceSessionFactory, deployerManager, Thread.currentThread().getContextClassLoader());

        commandContextFactory.setTransactionContextFactory(createDefaultTransactionContextFactory());
        
        Map<Class<?>, SessionFactory> sessionFactories = new HashMap<Class<?>, SessionFactory>();
        sessionFactories.put(MessageSession.class, new JobExecutorMessageSessionFactory());
        sessionFactories.put(TimerSession.class, new JobExecutorTimerSessionFactory());
        sessionFactories.put(PersistenceSession.class, cachingPersistenceSessionFactory);
        sessionFactories.put(RepositorySession.class, new DbRepositorySessionFactory());
        sessionFactories.put(DbSqlSession.class, new DbSqlSessionFactory(ibatisPersistenceSessionFactory.getSqlSessionFactory(), idGenerator, databaseName));
        
        configuration.setCommandContextFactory(commandContextFactory);
        configuration.setCommandExecutor(commandExecutor);
        configuration.setDbSchemaStrategy(dbSchemaStrategy);
        configuration.setDeployerManager(deployerManager);
        configuration.setHistoricDataService(historicDataService);
        configuration.setIdentityService(identityService);
        configuration.setIdGenerator(idGenerator);
        configuration.setJobExecutor(jobExecutor);
        configuration.setJobHandlers(jobHandlers);
        configuration.setManagementService(managementService);
        configuration.setPersistenceSessionFactory(cachingPersistenceSessionFactory);
        configuration.setProcessEngineName(processEngineName);
        configuration.setProcessEventBus(processEventBus);
        configuration.setProcessService(processService);
        configuration.setTaskService(taskService);
        configuration.setVariableTypes(variableTypes);
        configuration.setSessionFactories(sessionFactories);

        commandContextFactory.setProcessEngineConfiguration(configuration);

        initialized = true;

      }
    }
  }

  public ProcessEngineImpl createProcessEngine() {
    init();
    return new ProcessEngineImpl(this.configuration);
  }

  public CommandContextFactory getCommandContextFactory() {
    return commandContextFactory;
  }

  protected BusinessCalendarManager createDefaultBusinessCalendarManager() {
    MapBusinessCalendarManager defaultBusinessCalendarManager = new MapBusinessCalendarManager();
    defaultBusinessCalendarManager.addBusinessCalendar(DurationBusinessCalendar.NAME, new DurationBusinessCalendar());
    return defaultBusinessCalendarManager;
  }

  protected TransactionContextFactory createDefaultTransactionContextFactory() {
    return new StandaloneTransactionContextFactory();
  }

  protected JobHandlers createDefaultJobHandlers() {
    JobHandlers jobHandlers = new JobHandlers();
    jobHandlers.addJobHandler(new TimerExecuteNestedActivityJobHandler());
    return jobHandlers;
  }

  protected CommandContextFactory createDefaultCommandContextFactory() {
    return new CommandContextFactory();
  }

  protected ProcessEventBus createDefaultProcessEventBus() {
    return new DefaultProcessEventBus();
  }

  protected HistoricDataService createDefaultHistoricDataService(CommandExecutor commandExecutor, ProcessEventBus processEventBus) {
    HistoricDataServiceImpl historicDataService = new HistoricDataServiceImpl(commandExecutor);
    historicDataService.registerEventConsumers(processEventBus);    
    return historicDataService;
  }

  protected JobExecutor createDefaultJobExecutor(CommandExecutor commandExecutor, JobHandlers jobHandlers, boolean jobExecutorAutoActivate) {
    JobExecutor jobExecutor = new JobExecutor(commandExecutor, jobHandlers);
    jobExecutor.setAutoActivate(jobExecutorAutoActivate);
    return jobExecutor;
  }

  protected ManagementServiceImpl createDefaultManagementService(CommandExecutor commandExecutor) {
    return new ManagementServiceImpl(commandExecutor);
  }

  protected TaskServiceImpl createDefaultTaskService(CommandExecutor commandExecutor, ScriptingEngines scriptingEngines) {
    return new TaskServiceImpl(commandExecutor, scriptingEngines);
  }

  protected IdentityServiceImpl createDefaultIdentityService(CommandExecutor commandExecutor) {
    return new IdentityServiceImpl(commandExecutor);
  }

  protected ProcessServiceImpl createDefaultProcessService(CommandExecutor commandExecutor, DeployerManager deployerManager, ScriptingEngines scriptingEngines) {
    return new ProcessServiceImpl(commandExecutor, deployerManager, scriptingEngines);
  }

  protected DbSchemaStrategy createDefaultDbSchemaStrategy() {
    return DbSchemaStrategy.CREATE_DROP;
  }

  protected MessageSessionFactory createDefaultMessageSessionFactory(JobExecutor jobExecutor) {
    return new JobExecutorMessageSessionFactory(jobExecutor);
  }

  protected PersistenceSessionFactory createDefaultPersistenceSessionFactory(DeployerManager deployerManager, VariableTypes variableTypes,
          IdGenerator idGenerator, String databaseName, DataSource dataSource, boolean localTransactions) {
    PersistenceSessionFactory persistenceSessionFactory = new IbatisPersistenceSessionFactory(variableTypes, idGenerator, databaseName, dataSource,
            localTransactions);
    persistenceSessionFactory = new CachingPersistenceSessionFactory(persistenceSessionFactory, deployerManager, Thread.currentThread().getContextClassLoader());
    return persistenceSessionFactory;
  }

  protected CommandExecutor createDefaultCmdExecutor(CommandContextFactory commandContextFactory) {
    return new DefaultCommandExecutor(commandContextFactory);
  }

  protected ScriptingEngines createDefaultScriptingEngines() {
    return new ScriptingEngines();
  }

  protected VariableTypes createDefaultVariableTypes() {
    return new DefaultVariableTypes();
  }

  protected IdGenerator createDefaultIdGenerator(CommandExecutor commandExecutor) {
    return new IdGenerator(commandExecutor);
  }

  protected DeployerManager createDefaultDeployerManager(ExpressionManager expressionManager, ScriptingEngines scriptingEngines,
          BusinessCalendarManager businessCalendarManager) {
    return new DeployerManager().addDeployer(new BpmnDeployer(expressionManager, scriptingEngines, businessCalendarManager));
  }

  protected ExpressionManager createDefaultExpressionManager() {
    return new ExpressionManager();
  }

  // setters
  // //////////////////////////////////////////////////////

  public void setProcessEngineName(String processEngineName) {
    this.processEngineName = processEngineName;
  }

  public void setProcessEventBus(ProcessEventBus processEventBus) {
    this.processEventBus = processEventBus;
  }

  public void setHistoricDataService(HistoricDataService historicDataService) {
    this.historicDataService = historicDataService;
  }

  public void setIdentityService(IdentityService identityService) {
    this.identityService = identityService;
  }

  public void setElResolver(ELResolver elResolver) {
    this.elResolver = elResolver;
  }

  public void setVariableTypes(VariableTypes variableTypes) {
    this.variableTypes = variableTypes;
  }

  public void setJobExecutor(JobExecutor jobExecutor) {
    this.jobExecutor = jobExecutor;
  }

  public void setIdGenerator(IdGenerator idGenerator) {
    this.idGenerator = idGenerator;
  }

  public void setDbSchemaStrategy(DbSchemaStrategy dbSchemaStrategy) {
    this.dbSchemaStrategy = dbSchemaStrategy;
  }

  public void setJobExecutorAutoActivate(boolean jobExecutorAutoActivate) {
    this.jobExecutorAutoActivate = jobExecutorAutoActivate;
  }

  public void setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public void setDatabaseName(String dataBaseName) {
    this.databaseName = dataBaseName;
  }

  public void setLocalTransactions(boolean localTransactions) {
    this.localTransactions = localTransactions;
  }

  public void setCommandExecutor(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
  }
}

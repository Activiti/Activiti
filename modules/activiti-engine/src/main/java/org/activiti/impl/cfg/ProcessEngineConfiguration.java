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

import org.activiti.DbSchemaStrategy;
import org.activiti.IdentityService;
import org.activiti.ManagementService;
import org.activiti.ProcessService;
import org.activiti.TaskService;
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
import org.activiti.impl.identity.IdentitySession;
import org.activiti.impl.interceptor.CommandContextFactory;
import org.activiti.impl.interceptor.CommandExecutor;
import org.activiti.impl.interceptor.DefaultCommandExecutor;
import org.activiti.impl.job.JobHandlers;
import org.activiti.impl.job.TimerExecuteNestedActivityJobHandler;
import org.activiti.impl.jobexecutor.JobExecutor;
import org.activiti.impl.msg.JobExecutorMessageSessionFactory;
import org.activiti.impl.msg.MessageSessionFactory;
import org.activiti.impl.persistence.CachingPersistenceSessionFactory;
import org.activiti.impl.persistence.IbatisIdentitySessionFactory;
import org.activiti.impl.persistence.IbatisPersistenceSessionFactory;
import org.activiti.impl.persistence.PersistenceSessionFactory;
import org.activiti.impl.repository.DeployerManager;
import org.activiti.impl.scripting.ScriptingEngines;
import org.activiti.impl.timer.JobExecutorTimerSessionFactory;
import org.activiti.impl.timer.TimerSessionFactory;
import org.activiti.impl.tx.StandaloneTransactionContextFactory;
import org.activiti.impl.tx.TransactionContextFactory;
import org.activiti.impl.variable.DefaultVariableTypes;
import org.activiti.impl.variable.VariableTypes;

/**
 * @author Tom Baeyens
 */
public class ProcessEngineConfiguration {

  private String processEngineName;

  private ProcessService processService;
  private IdentityService identityService;
  private TaskService taskService;
  private ManagementService managementService;

  private DeployerManager deployerManager;
  private VariableTypes variableTypes;
  private ScriptingEngines scriptingEngines;
  private JobExecutor jobExecutor;
  private boolean jobExecutorAutoActivate;
  private IdGenerator idGenerator;
  private CommandExecutor commandExecutor;
  private DbSchemaStrategy dbSchemaStrategy;
  private ExpressionManager expressionManager;
  private JobHandlers jobHandlers;
  private BusinessCalendarManager businessCalendarManager;

  private CommandContextFactory commandContextFactory;
  private PersistenceSessionFactory persistenceSessionFactory;
  private MessageSessionFactory messageSessionFactory;
  private TimerSessionFactory timerSessionFactory;
  private TransactionContextFactory transactionContextFactory;

  public ProcessEngineConfiguration() {
    this(null);
  }

  public ProcessEngineConfiguration(CommandContextFactory commandContextFactory) {

    this.commandContextFactory = commandContextFactory == null ? createDefaultCommandContextFactory() : commandContextFactory;
    commandExecutor = createDefaultCmdExecutor(this.commandContextFactory);

    expressionManager = createDefaultExpressionManager();
    scriptingEngines = createDefaultScriptingEngines();
    businessCalendarManager = createDefaultBusinessCalendarManager();
    deployerManager = createDefaultDeployerManager(expressionManager, scriptingEngines, businessCalendarManager);
    variableTypes = createDefaultVariableTypes();
    dbSchemaStrategy = createDefaultDbSchemaStrategy();
    jobExecutorAutoActivate = createDefaultJobExecutorAutoActivate();
    jobHandlers = createDefaultJobHandlers();

    jobExecutor = createDefaultJobExecutor(commandExecutor, jobHandlers);
    idGenerator = createDefaultIdGenerator(commandExecutor);

    processService = createDefaultProcessService(commandExecutor, deployerManager, scriptingEngines);
    identityService = createDefaultIdentityService(commandExecutor);
    taskService = createDefaultTaskService(commandExecutor, scriptingEngines);
    managementService = createDefaultManagementService(commandExecutor);

    persistenceSessionFactory = createDefaultPersistenceSessionFactory(deployerManager, variableTypes, idGenerator);
    messageSessionFactory = createDefaultMessageSessionFactory(jobExecutor);
    timerSessionFactory = createDefaultTimerSessionFactory(jobExecutor);
    transactionContextFactory = createDefaultTransactionContextFactory();
  }

  public ProcessEngineImpl buildProcessEngine() {
    return new ProcessEngineImpl(this);
  }

  protected BusinessCalendarManager createDefaultBusinessCalendarManager() {
    MapBusinessCalendarManager defaultBusinessCalendarManager = new MapBusinessCalendarManager();
    defaultBusinessCalendarManager.addBusinessCalendar(DurationBusinessCalendar.NAME, new DurationBusinessCalendar());
    return defaultBusinessCalendarManager;
  }

  protected TimerSessionFactory createDefaultTimerSessionFactory(JobExecutor jobExecutor) {
    return new JobExecutorTimerSessionFactory(jobExecutor);
  }

  protected TransactionContextFactory createDefaultTransactionContextFactory() {
    return new StandaloneTransactionContextFactory();
  }

  protected JobHandlers createDefaultJobHandlers() {
    JobHandlers jobHandlers = new JobHandlers();
    jobHandlers.addJobHandler(new TimerExecuteNestedActivityJobHandler());
    return jobHandlers;
  }

  protected boolean createDefaultJobExecutorAutoActivate() {
    return true;
  }

  protected CommandContextFactory createDefaultCommandContextFactory() {
    CommandContextFactory commandContextFactory = new CommandContextFactory(this);
    commandContextFactory.addSessionFactory(IdentitySession.class, new IbatisIdentitySessionFactory());
    return commandContextFactory;
  }

  protected JobExecutor createDefaultJobExecutor(CommandExecutor commandExecutor, JobHandlers jobHandlers) {
    JobExecutor jobExecutor = new JobExecutor(commandExecutor, jobHandlers);
    return jobExecutor;
  }

  protected ManagementServiceImpl createDefaultManagementService(CommandExecutor commandExecutor) {
    ManagementServiceImpl managementService = new ManagementServiceImpl(commandExecutor);
    return managementService;
  }

  protected TaskServiceImpl createDefaultTaskService(CommandExecutor commandExecutor, ScriptingEngines scriptingEngines) {
    TaskServiceImpl taskService = new TaskServiceImpl(commandExecutor, scriptingEngines);
    return taskService;
  }

  protected IdentityServiceImpl createDefaultIdentityService(CommandExecutor commandExecutor) {
    IdentityServiceImpl identityService = new IdentityServiceImpl(commandExecutor);
    return identityService;
  }

  protected ProcessServiceImpl createDefaultProcessService(CommandExecutor commandExecutor, DeployerManager deployerManager, ScriptingEngines scriptingEngines) {
    ProcessServiceImpl processService = new ProcessServiceImpl(commandExecutor, deployerManager, scriptingEngines);
    return processService;
  }

  protected DbSchemaStrategy createDefaultDbSchemaStrategy() {
    return DbSchemaStrategy.CREATE_DROP;
  }

  protected MessageSessionFactory createDefaultMessageSessionFactory(JobExecutor jobExecutor) {
    return new JobExecutorMessageSessionFactory(jobExecutor);
  }

  protected PersistenceSessionFactory createDefaultPersistenceSessionFactory(DeployerManager deployerManager, VariableTypes variableTypes, IdGenerator idGenerator) {
    PersistenceSessionFactory persistenceSessionFactory = new IbatisPersistenceSessionFactory(variableTypes, idGenerator, "h2", "org.h2.Driver", "jdbc:h2:mem:activiti", "sa", "");
    persistenceSessionFactory = new CachingPersistenceSessionFactory(persistenceSessionFactory, deployerManager, Thread.currentThread().getContextClassLoader());
    return persistenceSessionFactory;
  }

  protected CommandExecutor createDefaultCmdExecutor(CommandContextFactory commandContextFactory) {
    DefaultCommandExecutor commandExecutor = new DefaultCommandExecutor(commandContextFactory);
    return commandExecutor;
  }

  protected ScriptingEngines createDefaultScriptingEngines() {
    return new ScriptingEngines();
  }

  protected VariableTypes createDefaultVariableTypes() {
    return new DefaultVariableTypes();
  }

  protected IdGenerator createDefaultIdGenerator(CommandExecutor commandExecutor) {
    IdGenerator idGenerator = new IdGenerator(commandExecutor);
    return idGenerator;
  }

  protected DeployerManager createDefaultDeployerManager(ExpressionManager expressionManager, ScriptingEngines scriptingEngines, BusinessCalendarManager businessCalendarManager) {
    return new DeployerManager().addDeployer(new BpmnDeployer(expressionManager, scriptingEngines, businessCalendarManager));
  }

  protected ExpressionManager createDefaultExpressionManager() {
    return new ExpressionManager();
  }

  // getters and setters
  // //////////////////////////////////////////////////////

  public DeployerManager getDeployerManager() {
    return deployerManager;
  }

  public void setDeployerManager(DeployerManager deployerManager) {
    this.deployerManager = deployerManager;
  }

  public ScriptingEngines getScriptingEngines() {
    return scriptingEngines;
  }

  public void setScriptingEngines(ScriptingEngines scriptingEngines) {
    this.scriptingEngines = scriptingEngines;
  }

  public PersistenceSessionFactory getPersistenceSessionFactory() {
    return persistenceSessionFactory;
  }

  public void setPersistenceSessionFactory(PersistenceSessionFactory persistenceSessionFactory) {
    this.persistenceSessionFactory = persistenceSessionFactory;
  }

  public String getProcessEngineName() {
    return processEngineName;
  }

  public void setProcessEngineName(String processEngineName) {
    this.processEngineName = processEngineName;
  }

  public JobExecutor getJobExecutor() {
    return jobExecutor;
  }

  public void setJobExecutor(JobExecutor jobExecutor) {
    this.jobExecutor = jobExecutor;
  }

  public DbSchemaStrategy getDbSchemaStrategy() {
    return dbSchemaStrategy;
  }

  public void setDbSchemaStrategy(DbSchemaStrategy dbSchemaStrategy) {
    this.dbSchemaStrategy = dbSchemaStrategy;
  }

  public ProcessService getProcessService() {
    return processService;
  }

  public void setProcessService(ProcessService processService) {
    this.processService = processService;
  }

  public IdentityService getIdentityService() {
    return identityService;
  }

  public void setIdentityService(IdentityService identityService) {
    this.identityService = identityService;
  }

  public TaskService getTaskService() {
    return taskService;
  }

  public void setTaskService(TaskService taskService) {
    this.taskService = taskService;
  }

  public ManagementService getManagementService() {
    return managementService;
  }

  public void setManagementService(ManagementService managementService) {
    this.managementService = managementService;
  }

  public boolean isJobExecutorAutoActivate() {
    return jobExecutorAutoActivate;
  }

  public void setJobExecutorAutoActivate(boolean jobExecutorAutoActivate) {
    this.jobExecutorAutoActivate = jobExecutorAutoActivate;
  }

  public void setManagementService(ManagementServiceImpl managementService) {
    this.managementService = managementService;
  }

  public CommandContextFactory getCommandContextFactory() {
    return commandContextFactory;
  }

  public void setCommandContextFactory(CommandContextFactory commandContextFactory) {
    this.commandContextFactory = commandContextFactory;
  }

  public ExpressionManager getExpressionManager() {
    return expressionManager;
  }

  public void setExpressionManager(ExpressionManager expressionManager) {
    this.expressionManager = expressionManager;
  }

  public VariableTypes getVariableTypes() {
    return variableTypes;
  }

  public void setVariableTypes(VariableTypes variableTypes) {
    this.variableTypes = variableTypes;
  }

  public IdGenerator getIdGenerator() {
    return idGenerator;
  }

  public void setIdGenerator(IdGenerator idGenerator) {
    this.idGenerator = idGenerator;
  }

  public CommandExecutor getCommandExecutor() {
    return commandExecutor;
  }

  public MessageSessionFactory getMessageSessionFactory() {
    return messageSessionFactory;
  }

  public void setMessageSessionFactory(MessageSessionFactory messageSessionFactory) {
    this.messageSessionFactory = messageSessionFactory;
  }

  public JobHandlers getJobHandlers() {
    return jobHandlers;
  }

  public void setJobHandlers(JobHandlers jobHandlers) {
    this.jobHandlers = jobHandlers;
  }

  public TimerSessionFactory getTimerSessionFactory() {
    return timerSessionFactory;
  }

  public void setTimerSessionFactory(TimerSessionFactory timerSessionFactory) {
    this.timerSessionFactory = timerSessionFactory;
  }

  public TransactionContextFactory getTransactionContextFactory() {
    return transactionContextFactory;
  }

  public void setTransactionContextFactory(TransactionContextFactory transactionContextFactory) {
    this.transactionContextFactory = transactionContextFactory;
  }

  public BusinessCalendarManager getBusinessCalendarManager() {
    return businessCalendarManager;
  }

  public void setBusinessCalendarManager(BusinessCalendarManager businessCalendarManager) {
    this.businessCalendarManager = businessCalendarManager;
  }
}

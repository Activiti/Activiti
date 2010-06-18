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
import org.activiti.impl.interceptor.CommandContextFactory;
import org.activiti.impl.interceptor.CommandContextInterceptor;
import org.activiti.impl.interceptor.CommandExecutor;
import org.activiti.impl.interceptor.CommandExecutorImpl;
import org.activiti.impl.interceptor.InterceptorChainBuilder;
import org.activiti.impl.job.JobHandlers;
import org.activiti.impl.job.TimerExecuteNestedActivityJobHandler;
import org.activiti.impl.jobexecutor.JobExecutor;
import org.activiti.impl.msg.JobExecutorMessageSessionFactory;
import org.activiti.impl.msg.MessageSessionFactory;
import org.activiti.impl.persistence.IbatisPersistenceSessionFactory;
import org.activiti.impl.persistence.PersistenceSessionFactory;
import org.activiti.impl.repository.DeployerManager;
import org.activiti.impl.repository.ProcessCache;
import org.activiti.impl.scripting.ScriptingEngines;
import org.activiti.impl.timer.JobExecutorTimerSessionFactory;
import org.activiti.impl.timer.TimerSessionFactory;
import org.activiti.impl.tx.StandaloneTransactionContextFactory;
import org.activiti.impl.tx.TransactionContextFactory;
import org.activiti.impl.variable.ByteArrayType;
import org.activiti.impl.variable.DateType;
import org.activiti.impl.variable.IntegerType;
import org.activiti.impl.variable.LongType;
import org.activiti.impl.variable.NullType;
import org.activiti.impl.variable.SerializableType;
import org.activiti.impl.variable.ShortType;
import org.activiti.impl.variable.StringType;
import org.activiti.impl.variable.VariableTypes;

import com.sun.script.juel.JuelScriptEngineFactory;


/**
 * @author Tom Baeyens
 */
public class ProcessEngineConfiguration {

  String processEngineName;
  DeployerManager deployerManager;
  VariableTypes variableTypes;
  ScriptingEngines scriptingEngines;
  JobExecutor jobExecutor;
  boolean jobExecutorAutoActivate;
  IdGenerator idGenerator;
  ProcessCache processCache;
  CommandContextFactory commandContextFactory;
  CommandExecutor commandExecutor;
  DbSchemaStrategy dbSchemaStrategy;
  ProcessServiceImpl processService;
  IdentityServiceImpl identityService;
  TaskServiceImpl taskService;
  ManagementServiceImpl managementService;
  ExpressionManager expressionManager;
  JobHandlers jobHandlers;
  BusinessCalendarManager businessCalendarManager;

  PersistenceSessionFactory persistenceSessionFactory;
  MessageSessionFactory messageSessionFactory;
  TimerSessionFactory timerSessionFactory;
  TransactionContextFactory transactionContextFactory;

  public ProcessEngineConfiguration() {
    deployerManager = createDefaultDeployerManager();
    variableTypes = createDefaultVariableTypes();
    scriptingEngines = createDefaultScriptingEngines();
    jobExecutor = createDefaultJobExecutor();
    jobExecutorAutoActivate = createDefaultJobExecutorAutoActivate();
    idGenerator = createDefaultIdGenerator();
    processCache = createDefaultProcessCache();
    commandContextFactory = createDefaultCommandContextFactory();
    commandExecutor = createDefaultCmdExecutor();
    dbSchemaStrategy = createDefaultDbSchemaStrategy();
    processService = createDefaultProcessService();
    identityService = createDefaultIdentityService();
    taskService = createDefaultTaskService();
    managementService = createDefaultManagementService();
    expressionManager = createDefaultExpressionManager();
    jobHandlers = createDefaultJobHandlers();
    businessCalendarManager = createDefaultBusinessCalendarManager();

    persistenceSessionFactory = createDefaultPersistenceSessionFactory();
    messageSessionFactory = createDefaultMessageSessionFactory();
    timerSessionFactory = createDefaultTimerSessionFactory();
    transactionContextFactory = createDefaultTransactionContextFactory();
  }

  public ProcessEngineImpl buildProcessEngine() {
    // wiring the configurable objects together
    this.processService.setCmdExecutor(commandExecutor);
    this.identityService.setCmdExecutor(commandExecutor);
    this.taskService.setCmdExecutor(commandExecutor);
    this.managementService.setCmdExecutor(commandExecutor);
    this.idGenerator.setCmdExecutor(commandExecutor);
    this.processCache.setDeployerManager(deployerManager);
    this.jobExecutor.setCommandExecutor(commandExecutor);
    this.persistenceSessionFactory.setDbidGenerator(idGenerator);
    this.commandExecutor.setProcessEngineConfiguration(this);
    this.commandContextFactory.setProcessEngineConfiguration(this);
 
    return new ProcessEngineImpl(this);
  }
  
  protected BusinessCalendarManager createDefaultBusinessCalendarManager() {
    MapBusinessCalendarManager defaultBusinessCalendarManager = new MapBusinessCalendarManager();
    defaultBusinessCalendarManager.addBusinessCalendar(DurationBusinessCalendar.NAME, new DurationBusinessCalendar());
    return defaultBusinessCalendarManager;
  }

  protected TimerSessionFactory createDefaultTimerSessionFactory() {
    return new JobExecutorTimerSessionFactory();
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
    return new CommandContextFactory();
  }

  protected JobExecutor createDefaultJobExecutor() {
    return new JobExecutor();
  }

  protected ManagementServiceImpl createDefaultManagementService() {
    return new ManagementServiceImpl();
  }

  protected TaskServiceImpl createDefaultTaskService() {
    return new TaskServiceImpl();
  }

  protected IdentityServiceImpl createDefaultIdentityService() {
    return new IdentityServiceImpl();
  }

  protected ProcessServiceImpl createDefaultProcessService() {
    return new ProcessServiceImpl();
  }

  protected DbSchemaStrategy createDefaultDbSchemaStrategy() {
    return DbSchemaStrategy.CREATE_DROP;
  }

  protected MessageSessionFactory createDefaultMessageSessionFactory() {
    return new JobExecutorMessageSessionFactory();
  }

  protected PersistenceSessionFactory createDefaultPersistenceSessionFactory() {
    return new IbatisPersistenceSessionFactory(
      "h2",
      "org.h2.Driver",
      "jdbc:h2:mem:activiti",
      "sa",
      ""
    );
  }

  protected CommandExecutor createDefaultCmdExecutor() {
    return new InterceptorChainBuilder()
      .addInterceptor(new CommandContextInterceptor())
      .addInterceptor(new CommandExecutorImpl())
      .getFirst();
  }

  protected ScriptingEngines createDefaultScriptingEngines() {
    return new ScriptingEngines()
      .addScriptEngineFactory(new JuelScriptEngineFactory());
  }

  protected VariableTypes createDefaultVariableTypes() {
    return new VariableTypes()
      .addType(new NullType())
      .addType(new StringType())
      .addType(new ShortType())
      .addType(new IntegerType())
      .addType(new LongType())
      .addType(new DateType())
      .addType(new ByteArrayType())
      .addType(new SerializableType());
  }

  protected IdGenerator createDefaultIdGenerator() {
    return new IdGenerator();
  }

  protected ProcessCache createDefaultProcessCache() {
    return new ProcessCache();
  }

  protected DeployerManager createDefaultDeployerManager() {
    return new DeployerManager()
      .addDeployer(new BpmnDeployer());
  }
  
  protected ExpressionManager createDefaultExpressionManager() {
    return new ExpressionManager();
  }




  // getters and setters //////////////////////////////////////////////////////
  
  public DeployerManager getDeployerManager() {
    return deployerManager;
  }

  
  public void setDeployerManager(DeployerManager deployerManager) {
    this.deployerManager = deployerManager;
  }

  
  public ProcessCache getProcessCache() {
    return processCache;
  }

  
  public void setProcessCache(ProcessCache processCache) {
    this.processCache = processCache;
  }

  
  public IdGenerator getDbidGenerator() {
    return idGenerator;
  }

  
  public void setDbidGenerator(IdGenerator idGenerator) {
    this.idGenerator = idGenerator;
  }

  
  public VariableTypes getTypes() {
    return variableTypes;
  }

  
  public void setTypes(VariableTypes variableTypes) {
    this.variableTypes = variableTypes;
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


  
  public ProcessServiceImpl getProcessService() {
    return processService;
  }


  
  public void setProcessService(ProcessServiceImpl processService) {
    this.processService = processService;
  }


  
  public IdentityServiceImpl getIdentityService() {
    return identityService;
  }


  
  public void setIdentityService(IdentityServiceImpl identityService) {
    this.identityService = identityService;
  }


  
  public TaskServiceImpl getTaskService() {
    return taskService;
  }


  
  public void setTaskService(TaskServiceImpl taskService) {
    this.taskService = taskService;
  }


  
  public ManagementServiceImpl getManagementService() {
    return managementService;
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

  public void setCommandExecutor(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
  }
  
  public JobHandlers getJobCommands() {
    return jobHandlers;
  }
  
  public void setJobCommands(JobHandlers jobHandlers) {
    this.jobHandlers = jobHandlers;
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

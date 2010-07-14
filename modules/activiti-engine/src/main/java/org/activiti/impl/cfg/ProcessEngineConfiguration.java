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
import org.activiti.impl.interceptor.DefaultCommandContextFactory;
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

  private final ProcessService processService;
  private final IdentityService identityService;
  private final TaskService taskService;
  private final ManagementService managementService;

  private final DeployerManager deployerManager;
  private final VariableTypes variableTypes;
  private final JobExecutor jobExecutor;
  private final JobHandlers jobHandlers;
  private final IdGenerator idGenerator;
  private final CommandExecutor commandExecutor;

  private boolean jobExecutorAutoActivate;
  private DbSchemaStrategy dbSchemaStrategy;

  private DefaultCommandContextFactory commandContextFactory;
  private PersistenceSessionFactory persistenceSessionFactory;

  public ProcessEngineConfiguration() {

    this.commandContextFactory = createDefaultCommandContextFactory();

    ExpressionManager expressionManager = createDefaultExpressionManager();
    ScriptingEngines scriptingEngines = createDefaultScriptingEngines();
    BusinessCalendarManager businessCalendarManager = createDefaultBusinessCalendarManager();

    variableTypes = createDefaultVariableTypes();
    dbSchemaStrategy = createDefaultDbSchemaStrategy();
    jobExecutorAutoActivate = createDefaultJobExecutorAutoActivate();
    jobHandlers = createDefaultJobHandlers();

    deployerManager = createDefaultDeployerManager(expressionManager, scriptingEngines, businessCalendarManager);

    commandExecutor = createDefaultCmdExecutor(this.commandContextFactory);

    jobExecutor = createDefaultJobExecutor(commandExecutor, jobHandlers);
    idGenerator = createDefaultIdGenerator(commandExecutor);

    processService = createDefaultProcessService(commandExecutor, deployerManager, scriptingEngines);
    identityService = createDefaultIdentityService(commandExecutor);
    taskService = createDefaultTaskService(commandExecutor, scriptingEngines);
    managementService = createDefaultManagementService(commandExecutor);

    commandContextFactory.setMessageSessionFactory(createDefaultMessageSessionFactory(jobExecutor));
    commandContextFactory.setPersistenceSessionFactory(createDefaultPersistenceSessionFactory(deployerManager, variableTypes, idGenerator));
    commandContextFactory.setTimerSessionFactory(createDefaultTimerSessionFactory(jobExecutor));
    commandContextFactory.setTransactionContextFactory(createDefaultTransactionContextFactory());
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

  protected DefaultCommandContextFactory createDefaultCommandContextFactory() {
    DefaultCommandContextFactory commandContextFactory = new DefaultCommandContextFactory();
    commandContextFactory.addSessionFactory(IdentitySession.class, new IbatisIdentitySessionFactory());
    return commandContextFactory;
  }

  protected JobExecutor createDefaultJobExecutor(CommandExecutor commandExecutor, JobHandlers jobHandlers) {
    return new JobExecutor(commandExecutor, jobHandlers);
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

  protected PersistenceSessionFactory createDefaultPersistenceSessionFactory(DeployerManager deployerManager, VariableTypes variableTypes, IdGenerator idGenerator) {
    PersistenceSessionFactory persistenceSessionFactory = new IbatisPersistenceSessionFactory(variableTypes, idGenerator, "h2", "org.h2.Driver", "jdbc:h2:mem:activiti", "sa", "");
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

  public DbSchemaStrategy getDbSchemaStrategy() {
    return dbSchemaStrategy;
  }

  public void setDbSchemaStrategy(DbSchemaStrategy dbSchemaStrategy) {
    this.dbSchemaStrategy = dbSchemaStrategy;
  }

  public ProcessService getProcessService() {
    return processService;
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

  public boolean isJobExecutorAutoActivate() {
    return jobExecutorAutoActivate;
  }

  public void setJobExecutorAutoActivate(boolean jobExecutorAutoActivate) {
    this.jobExecutorAutoActivate = jobExecutorAutoActivate;
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
}

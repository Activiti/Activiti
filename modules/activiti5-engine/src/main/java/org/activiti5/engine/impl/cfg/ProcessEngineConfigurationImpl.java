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

package org.activiti5.engine.impl.cfg;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.Objects;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.xml.namespace.QName;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.compatibility.Activiti5CompatibilityHandler;
import org.activiti.engine.delegate.event.ActivitiEventDispatcher;
import org.activiti.engine.form.AbstractFormType;
import org.activiti.engine.impl.asyncexecutor.DefaultAsyncJobExecutor;
import org.activiti.engine.impl.bpmn.data.ItemInstance;
import org.activiti.engine.impl.bpmn.webservice.MessageInstance;
import org.activiti.engine.impl.calendar.BusinessCalendarManager;
import org.activiti.engine.impl.calendar.CycleBusinessCalendar;
import org.activiti.engine.impl.calendar.DueDateBusinessCalendar;
import org.activiti.engine.impl.calendar.DurationBusinessCalendar;
import org.activiti.engine.impl.calendar.MapBusinessCalendarManager;
import org.activiti.engine.impl.cfg.DelegateExpressionFieldInjectionMode;
import org.activiti.engine.impl.persistence.deploy.DefaultDeploymentCache;
import org.activiti.engine.impl.persistence.deploy.DeploymentCache;
import org.activiti.engine.impl.persistence.deploy.ProcessDefinitionCacheEntry;
import org.activiti.engine.impl.util.DefaultClockImpl;
import org.activiti.engine.impl.variable.VariableType;
import org.activiti.engine.impl.variable.VariableTypes;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.activiti.validation.ProcessValidator;
import org.activiti.validation.ProcessValidatorFactory;
import org.activiti5.engine.ActivitiException;
import org.activiti5.engine.DynamicBpmnService;
import org.activiti5.engine.FormService;
import org.activiti5.engine.HistoryService;
import org.activiti5.engine.IdentityService;
import org.activiti5.engine.ManagementService;
import org.activiti5.engine.ProcessEngine;
import org.activiti5.engine.ProcessEngineConfiguration;
import org.activiti5.engine.RepositoryService;
import org.activiti5.engine.RuntimeService;
import org.activiti5.engine.TaskService;
import org.activiti5.engine.cfg.ProcessEngineConfigurator;
import org.activiti5.engine.impl.DynamicBpmnServiceImpl;
import org.activiti5.engine.impl.FormServiceImpl;
import org.activiti5.engine.impl.HistoryServiceImpl;
import org.activiti5.engine.impl.IdentityServiceImpl;
import org.activiti5.engine.impl.ManagementServiceImpl;
import org.activiti5.engine.impl.ProcessEngineImpl;
import org.activiti5.engine.impl.RepositoryServiceImpl;
import org.activiti5.engine.impl.RuntimeServiceImpl;
import org.activiti5.engine.impl.ServiceImpl;
import org.activiti5.engine.impl.TaskServiceImpl;
import org.activiti5.engine.impl.bpmn.deployer.BpmnDeployer;
import org.activiti5.engine.impl.bpmn.parser.BpmnParseHandlers;
import org.activiti5.engine.impl.bpmn.parser.BpmnParser;
import org.activiti5.engine.impl.bpmn.parser.factory.AbstractBehaviorFactory;
import org.activiti5.engine.impl.bpmn.parser.factory.ActivityBehaviorFactory;
import org.activiti5.engine.impl.bpmn.parser.factory.DefaultActivityBehaviorFactory;
import org.activiti5.engine.impl.bpmn.parser.factory.DefaultListenerFactory;
import org.activiti5.engine.impl.bpmn.parser.factory.ListenerFactory;
import org.activiti5.engine.impl.bpmn.parser.handler.BoundaryEventParseHandler;
import org.activiti5.engine.impl.bpmn.parser.handler.BusinessRuleParseHandler;
import org.activiti5.engine.impl.bpmn.parser.handler.CallActivityParseHandler;
import org.activiti5.engine.impl.bpmn.parser.handler.CancelEventDefinitionParseHandler;
import org.activiti5.engine.impl.bpmn.parser.handler.CompensateEventDefinitionParseHandler;
import org.activiti5.engine.impl.bpmn.parser.handler.EndEventParseHandler;
import org.activiti5.engine.impl.bpmn.parser.handler.ErrorEventDefinitionParseHandler;
import org.activiti5.engine.impl.bpmn.parser.handler.EventBasedGatewayParseHandler;
import org.activiti5.engine.impl.bpmn.parser.handler.EventSubProcessParseHandler;
import org.activiti5.engine.impl.bpmn.parser.handler.ExclusiveGatewayParseHandler;
import org.activiti5.engine.impl.bpmn.parser.handler.InclusiveGatewayParseHandler;
import org.activiti5.engine.impl.bpmn.parser.handler.IntermediateCatchEventParseHandler;
import org.activiti5.engine.impl.bpmn.parser.handler.IntermediateThrowEventParseHandler;
import org.activiti5.engine.impl.bpmn.parser.handler.ManualTaskParseHandler;
import org.activiti5.engine.impl.bpmn.parser.handler.MessageEventDefinitionParseHandler;
import org.activiti5.engine.impl.bpmn.parser.handler.ParallelGatewayParseHandler;
import org.activiti5.engine.impl.bpmn.parser.handler.ProcessParseHandler;
import org.activiti5.engine.impl.bpmn.parser.handler.ReceiveTaskParseHandler;
import org.activiti5.engine.impl.bpmn.parser.handler.ScriptTaskParseHandler;
import org.activiti5.engine.impl.bpmn.parser.handler.SendTaskParseHandler;
import org.activiti5.engine.impl.bpmn.parser.handler.SequenceFlowParseHandler;
import org.activiti5.engine.impl.bpmn.parser.handler.ServiceTaskParseHandler;
import org.activiti5.engine.impl.bpmn.parser.handler.SignalEventDefinitionParseHandler;
import org.activiti5.engine.impl.bpmn.parser.handler.StartEventParseHandler;
import org.activiti5.engine.impl.bpmn.parser.handler.SubProcessParseHandler;
import org.activiti5.engine.impl.bpmn.parser.handler.TaskParseHandler;
import org.activiti5.engine.impl.bpmn.parser.handler.TimerEventDefinitionParseHandler;
import org.activiti5.engine.impl.bpmn.parser.handler.TransactionParseHandler;
import org.activiti5.engine.impl.bpmn.parser.handler.UserTaskParseHandler;
import org.activiti5.engine.impl.cfg.standalone.StandaloneMybatisTransactionContextFactory;
import org.activiti5.engine.impl.db.DbIdGenerator;
import org.activiti5.engine.impl.db.DbSqlSessionFactory;
import org.activiti5.engine.impl.db.IbatisVariableTypeHandler;
import org.activiti5.engine.impl.delegate.DefaultDelegateInterceptor;
import org.activiti5.engine.impl.el.ExpressionManager;
import org.activiti5.engine.impl.event.CompensationEventHandler;
import org.activiti5.engine.impl.event.EventHandler;
import org.activiti5.engine.impl.event.MessageEventHandler;
import org.activiti5.engine.impl.event.SignalEventHandler;
import org.activiti5.engine.impl.event.logger.EventLogger;
import org.activiti5.engine.impl.form.BooleanFormType;
import org.activiti5.engine.impl.form.DateFormType;
import org.activiti5.engine.impl.form.DoubleFormType;
import org.activiti5.engine.impl.form.FormEngine;
import org.activiti5.engine.impl.form.FormTypes;
import org.activiti5.engine.impl.form.JuelFormEngine;
import org.activiti5.engine.impl.form.LongFormType;
import org.activiti5.engine.impl.form.StringFormType;
import org.activiti5.engine.impl.history.HistoryLevel;
import org.activiti5.engine.impl.history.parse.FlowNodeHistoryParseHandler;
import org.activiti5.engine.impl.history.parse.ProcessHistoryParseHandler;
import org.activiti5.engine.impl.history.parse.StartEventHistoryParseHandler;
import org.activiti5.engine.impl.history.parse.UserTaskHistoryParseHandler;
import org.activiti5.engine.impl.interceptor.CommandConfig;
import org.activiti5.engine.impl.interceptor.CommandContextFactory;
import org.activiti5.engine.impl.interceptor.CommandContextInterceptor;
import org.activiti5.engine.impl.interceptor.CommandExecutor;
import org.activiti5.engine.impl.interceptor.CommandInterceptor;
import org.activiti5.engine.impl.interceptor.CommandInvoker;
import org.activiti5.engine.impl.interceptor.DelegateInterceptor;
import org.activiti5.engine.impl.interceptor.LogInterceptor;
import org.activiti5.engine.impl.interceptor.SessionFactory;
import org.activiti5.engine.impl.jobexecutor.AsyncContinuationJobHandler;
import org.activiti5.engine.impl.jobexecutor.DefaultFailedJobCommandFactory;
import org.activiti5.engine.impl.jobexecutor.FailedJobCommandFactory;
import org.activiti5.engine.impl.jobexecutor.JobHandler;
import org.activiti5.engine.impl.jobexecutor.ProcessEventJobHandler;
import org.activiti5.engine.impl.jobexecutor.TimerActivateProcessDefinitionHandler;
import org.activiti5.engine.impl.jobexecutor.TimerCatchIntermediateEventJobHandler;
import org.activiti5.engine.impl.jobexecutor.TimerExecuteNestedActivityJobHandler;
import org.activiti5.engine.impl.jobexecutor.TimerStartEventJobHandler;
import org.activiti5.engine.impl.jobexecutor.TimerSuspendProcessDefinitionHandler;
import org.activiti5.engine.impl.persistence.DefaultHistoryManagerSessionFactory;
import org.activiti5.engine.impl.persistence.GenericManagerFactory;
import org.activiti5.engine.impl.persistence.GroupEntityManagerFactory;
import org.activiti5.engine.impl.persistence.MembershipEntityManagerFactory;
import org.activiti5.engine.impl.persistence.UserEntityManagerFactory;
import org.activiti5.engine.impl.persistence.deploy.Deployer;
import org.activiti5.engine.impl.persistence.deploy.DeploymentManager;
import org.activiti5.engine.impl.persistence.deploy.ProcessDefinitionInfoCache;
import org.activiti5.engine.impl.persistence.entity.AttachmentEntityManager;
import org.activiti5.engine.impl.persistence.entity.ByteArrayEntityManager;
import org.activiti5.engine.impl.persistence.entity.CommentEntityManager;
import org.activiti5.engine.impl.persistence.entity.DeadLetterJobEntityManager;
import org.activiti5.engine.impl.persistence.entity.DeploymentEntityManager;
import org.activiti5.engine.impl.persistence.entity.EventLogEntryEntityManager;
import org.activiti5.engine.impl.persistence.entity.EventSubscriptionEntityManager;
import org.activiti5.engine.impl.persistence.entity.ExecutionEntityManager;
import org.activiti5.engine.impl.persistence.entity.HistoricActivityInstanceEntityManager;
import org.activiti5.engine.impl.persistence.entity.HistoricDetailEntityManager;
import org.activiti5.engine.impl.persistence.entity.HistoricIdentityLinkEntityManager;
import org.activiti5.engine.impl.persistence.entity.HistoricProcessInstanceEntityManager;
import org.activiti5.engine.impl.persistence.entity.HistoricTaskInstanceEntityManager;
import org.activiti5.engine.impl.persistence.entity.HistoricVariableInstanceEntityManager;
import org.activiti5.engine.impl.persistence.entity.IdentityInfoEntityManager;
import org.activiti5.engine.impl.persistence.entity.IdentityLinkEntityManager;
import org.activiti5.engine.impl.persistence.entity.JobEntityManager;
import org.activiti5.engine.impl.persistence.entity.ModelEntityManager;
import org.activiti5.engine.impl.persistence.entity.ProcessDefinitionEntityManager;
import org.activiti5.engine.impl.persistence.entity.ProcessDefinitionInfoEntityManager;
import org.activiti5.engine.impl.persistence.entity.PropertyEntityManager;
import org.activiti5.engine.impl.persistence.entity.ResourceEntityManager;
import org.activiti5.engine.impl.persistence.entity.SuspendedJobEntityManager;
import org.activiti5.engine.impl.persistence.entity.TableDataManager;
import org.activiti5.engine.impl.persistence.entity.TaskEntityManager;
import org.activiti5.engine.impl.persistence.entity.TimerJobEntityManager;
import org.activiti5.engine.impl.persistence.entity.VariableInstanceEntityManager;
import org.activiti5.engine.impl.scripting.BeansResolverFactory;
import org.activiti5.engine.impl.scripting.ResolverFactory;
import org.activiti5.engine.impl.scripting.ScriptBindingsFactory;
import org.activiti5.engine.impl.scripting.ScriptingEngines;
import org.activiti5.engine.impl.scripting.VariableScopeResolverFactory;
import org.activiti5.engine.impl.util.IoUtil;
import org.activiti5.engine.impl.util.ReflectUtil;
import org.activiti5.engine.impl.variable.BooleanType;
import org.activiti5.engine.impl.variable.ByteArrayType;
import org.activiti5.engine.impl.variable.CustomObjectType;
import org.activiti5.engine.impl.variable.DateType;
import org.activiti5.engine.impl.variable.DefaultVariableTypes;
import org.activiti5.engine.impl.variable.DoubleType;
import org.activiti5.engine.impl.variable.EntityManagerSession;
import org.activiti5.engine.impl.variable.EntityManagerSessionFactory;
import org.activiti5.engine.impl.variable.IntegerType;
import org.activiti5.engine.impl.variable.JPAEntityListVariableType;
import org.activiti5.engine.impl.variable.JPAEntityVariableType;
import org.activiti5.engine.impl.variable.JsonType;
import org.activiti5.engine.impl.variable.LongJsonType;
import org.activiti5.engine.impl.variable.LongStringType;
import org.activiti5.engine.impl.variable.LongType;
import org.activiti5.engine.impl.variable.NullType;
import org.activiti5.engine.impl.variable.SerializableType;
import org.activiti5.engine.impl.variable.ShortType;
import org.activiti5.engine.impl.variable.StringType;
import org.activiti5.engine.impl.variable.UUIDType;
import org.activiti5.engine.parse.BpmnParseHandler;
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
import org.apache.ibatis.type.JdbcType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public abstract class ProcessEngineConfigurationImpl extends ProcessEngineConfiguration {  

  private static Logger log = LoggerFactory.getLogger(ProcessEngineConfigurationImpl.class);
  
  public static final String DB_SCHEMA_UPDATE_CREATE = "create";
  public static final String DB_SCHEMA_UPDATE_DROP_CREATE = "drop-create";

  public static final String DEFAULT_WS_SYNC_FACTORY = "org.activiti5.engine.impl.webservice.CxfWebServiceClientFactory";
  
  public static final String DEFAULT_MYBATIS_MAPPING_FILE = "org/activiti5/db/mapping/mappings.xml";

  // SERVICES /////////////////////////////////////////////////////////////////

  protected RepositoryService repositoryService = new RepositoryServiceImpl();
  protected RuntimeService runtimeService = new RuntimeServiceImpl();
  protected HistoryService historyService = new HistoryServiceImpl(this);
  protected IdentityService identityService = new IdentityServiceImpl();
  protected TaskService taskService = new TaskServiceImpl(this);
  protected FormService formService = new FormServiceImpl();
  protected ManagementService managementService = new ManagementServiceImpl();
  protected DynamicBpmnService dynamicBpmnService = new DynamicBpmnServiceImpl(this);
  
  // COMMAND EXECUTORS ////////////////////////////////////////////////////////
  
  protected CommandConfig defaultCommandConfig;
  protected CommandConfig schemaCommandConfig;
  
  protected CommandInterceptor commandInvoker;
  
  /** the configurable list which will be {@link #initInterceptorChain(java.util.List) processed} to build the {@link #commandExecutor} */
  protected List<CommandInterceptor> customPreCommandInterceptors;
  protected List<CommandInterceptor> customPostCommandInterceptors;
  
  protected List<CommandInterceptor> commandInterceptors;

  /** this will be initialized during the configurationComplete() */
  protected CommandExecutor commandExecutor;
  
  // SESSION FACTORIES ////////////////////////////////////////////////////////

  protected List<SessionFactory> customSessionFactories;
  protected DbSqlSessionFactory dbSqlSessionFactory;
  protected Map<Class<?>, SessionFactory> sessionFactories;
  
  // Configurators ////////////////////////////////////////////////////////////
  
  protected boolean enableConfiguratorServiceLoader = true; // Enabled by default. In certain environments this should be set to false (eg osgi)
  protected List<ProcessEngineConfigurator> configurators; // The injected configurators
  protected List<ProcessEngineConfigurator> allConfigurators; // Including auto-discovered configurators
  
  // DEPLOYERS ////////////////////////////////////////////////////////////////

  protected BpmnDeployer bpmnDeployer;
  protected BpmnParser bpmnParser;
  protected List<Deployer> customPreDeployers;
  protected List<Deployer> customPostDeployers;
  protected List<Deployer> deployers;
  protected DeploymentManager deploymentManager;
  
  protected int processDefinitionCacheLimit = -1; // By default, no limit
  protected DeploymentCache<ProcessDefinitionCacheEntry> processDefinitionCache;
  protected int bpmnModelCacheLimit = -1; // By default, no limit
  protected DeploymentCache<BpmnModel> bpmnModelCache;
  protected int processDefinitionInfoCacheLimit = -1; // By default, no limit
  protected ProcessDefinitionInfoCache processDefinitionInfoCache;
  
  protected int knowledgeBaseCacheLimit = -1;
  protected DeploymentCache<Object> knowledgeBaseCache;

  // JOB EXECUTOR /////////////////////////////////////////////////////////////
  
  protected List<JobHandler> customJobHandlers;
  protected Map<String, JobHandler> jobHandlers;
  
  // ASYNC EXECUTOR ///////////////////////////////////////////////////////////

  /**
   * The time (in milliseconds) a thread used for job execution must be kept
   * alive before it is destroyed. Default setting is 5 seconds. Having a
   * setting > 0 takes resources, but in the case of many job executions it
   * avoids creating new threads all the time. If 0, threads will be destroyed
   * after they've been used for job execution.
   * 
   * (This property is only applicable when using the
   * {@link DefaultAsyncJobExecutor}).
   */
  protected long asyncExecutorThreadKeepAliveTime = 5000L;

  /**
   * The size of the queue on which jobs to be executed are placed, before they
   * are actually executed. Default value = 100. (This property is only
   * applicable when using the {@link DefaultAsyncJobExecutor}).
   */
  protected int asyncExecutorThreadPoolQueueSize = 100;

  /**
   * The queue onto which jobs will be placed before they are actually executed.
   * Threads form the async executor threadpool will take work from this queue.
   * 
   * By default null. If null, an {@link ArrayBlockingQueue} will be created of
   * size {@link #asyncExecutorThreadPoolQueueSize}.
   * 
   * When the queue is full, the job will be executed by the calling thread
   * (ThreadPoolExecutor.CallerRunsPolicy())
   * 
   * (This property is only applicable when using the
   * {@link DefaultAsyncJobExecutor}).
   */
  protected BlockingQueue<Runnable> asyncExecutorThreadPoolQueue;

  /**
   * The time (in seconds) that is waited to gracefully shut down the threadpool
   * used for job execution when the a shutdown on the executor (or process
   * engine) is requested. Default value = 60.
   * 
   * (This property is only applicable when using the
   * {@link DefaultAsyncJobExecutor}).
   */
  protected long asyncExecutorSecondsToWaitOnShutdown = 60L;

  /**
   * The number of timer jobs that are acquired during one query (before a job
   * is executed, an acquirement thread fetches jobs from the database and puts
   * them on the queue).
   * 
   * Default value = 1, as this lowers the potential on optimistic locking
   * exceptions. Change this value if you know what you are doing.
   * 
   * (This property is only applicable when using the
   * {@link DefaultAsyncJobExecutor}).
   */
  protected int asyncExecutorMaxTimerJobsPerAcquisition = 1;

  /**
   * The number of async jobs that are acquired during one query (before a job
   * is executed, an acquirement thread fetches jobs from the database and puts
   * them on the queue).
   * 
   * Default value = 1, as this lowers the potential on optimistic locking
   * exceptions. Change this value if you know what you are doing.
   * 
   * (This property is only applicable when using the
   * {@link DefaultAsyncJobExecutor}).
   */
  protected int asyncExecutorMaxAsyncJobsDuePerAcquisition = 1;

  /**
   * The time (in milliseconds) the timer acquisition thread will wait to
   * execute the next acquirement query. This happens when no new timer jobs
   * were found or when less timer jobs have been fetched than set in
   * {@link #asyncExecutorMaxTimerJobsPerAcquisition}. Default value = 10
   * seconds.
   * 
   * (This property is only applicable when using the
   * {@link DefaultAsyncJobExecutor}).
   */
  protected int asyncExecutorDefaultTimerJobAcquireWaitTime = 10 * 1000;

  /**
   * The time (in milliseconds) the async job acquisition thread will wait to
   * execute the next acquirement query. This happens when no new async jobs
   * were found or when less async jobs have been fetched than set in
   * {@link #asyncExecutorMaxAsyncJobsDuePerAcquisition}. Default value = 10
   * seconds.
   * 
   * (This property is only applicable when using the
   * {@link DefaultAsyncJobExecutor}).
   */
  protected int asyncExecutorDefaultAsyncJobAcquireWaitTime = 10 * 1000;
  
  /**
   * The time (in milliseconds) the async job (both timer and async continuations) acquisition thread will 
   * wait when the queueu is full to execute the next query. By default set to 0 (for backwards compatibility)
   */
  protected int asyncExecutorDefaultQueueSizeFullWaitTime = 0;

  /**
   * When a job is acquired, it is locked so other async executors can't lock
   * and execute it. While doing this, the 'name' of the lock owner is written
   * into a column of the job.
   * 
   * By default, a random UUID will be generated when the executor is created.
   * 
   * It is important that each async executor instance in a cluster of Activiti
   * engines has a different name!
   * 
   * (This property is only applicable when using the
   * {@link DefaultAsyncJobExecutor}).
   */
  protected String asyncExecutorLockOwner;

  /**
   * The amount of time (in milliseconds) a timer job is locked when acquired by
   * the async executor. During this period of time, no other async executor
   * will try to acquire and lock this job.
   * 
   * Default value = 5 minutes;
   * 
   * (This property is only applicable when using the
   * {@link DefaultAsyncJobExecutor}).
   */
  protected int asyncExecutorTimerLockTimeInMillis = 5 * 60 * 1000;

  /**
   * The amount of time (in milliseconds) an async job is locked when acquired
   * by the async executor. During this period of time, no other async executor
   * will try to acquire and lock this job.
   * 
   * Default value = 5 minutes;
   * 
   * (This property is only applicable when using the
   * {@link DefaultAsyncJobExecutor}).
   */
  protected int asyncExecutorAsyncJobLockTimeInMillis = 5 * 60 * 1000;

  /**
   * The amount of time (in milliseconds) that is waited before trying locking
   * again, when an exclusive job is tried to be locked, but fails and the
   * locking.
   * 
   * Default value = 500. If 0, this would stress database traffic a lot in case
   * when a retry is needed, as exclusive jobs would be constantly tried to be
   * locked.
   * 
   * (This property is only applicable when using the
   * {@link DefaultAsyncJobExecutor}).
   */
  protected int asyncExecutorLockRetryWaitTimeInMillis = 500;

  // MYBATIS SQL SESSION FACTORY //////////////////////////////////////////////
  
  protected SqlSessionFactory sqlSessionFactory;
  protected TransactionFactory transactionFactory;
  
  protected Set<Class<?>> customMybatisMappers;
  protected Set<String> customMybatisXMLMappers;

  // ID GENERATOR /////////////////////////////////////////////////////////////
  
  protected IdGenerator idGenerator;
  protected DataSource idGeneratorDataSource;
  protected String idGeneratorDataSourceJndiName;
  
  // BPMN PARSER //////////////////////////////////////////////////////////////
  
  protected List<BpmnParseHandler> preBpmnParseHandlers;
  protected List<BpmnParseHandler> postBpmnParseHandlers;
  protected List<BpmnParseHandler> customDefaultBpmnParseHandlers;
  protected ActivityBehaviorFactory activityBehaviorFactory;
  protected ListenerFactory listenerFactory;
  protected BpmnParseFactory bpmnParseFactory;
  
  // PROCESS VALIDATION 
  
  protected ProcessValidator processValidator;

  // OTHER ////////////////////////////////////////////////////////////////////
  
  protected List<FormEngine> customFormEngines;
  protected Map<String, FormEngine> formEngines;

  protected List<AbstractFormType> customFormTypes;
  protected FormTypes formTypes;

  protected List<VariableType> customPreVariableTypes;
  protected List<VariableType> customPostVariableTypes;
  protected VariableTypes variableTypes;
  
  protected ExpressionManager expressionManager;
  protected List<String> customScriptingEngineClasses;
  protected ScriptingEngines scriptingEngines;
  protected List<ResolverFactory> resolverFactories;
  
  protected BusinessCalendarManager businessCalendarManager;
  
  protected int executionQueryLimit = 20000;
  protected int taskQueryLimit = 20000;
  protected int historicTaskQueryLimit = 20000;
  protected int historicProcessInstancesQueryLimit = 20000;

  protected String wsSyncFactoryClassName = DEFAULT_WS_SYNC_FACTORY;
  protected ConcurrentMap<QName, URL> wsOverridenEndpointAddresses = new ConcurrentHashMap<QName, URL>();

  protected CommandContextFactory commandContextFactory;
  protected TransactionContextFactory transactionContextFactory;
  
  protected Map<Object, Object> beans;
  
  protected DelegateInterceptor delegateInterceptor;
  
  protected Map<String, EventHandler> eventHandlers;
  protected List<EventHandler> customEventHandlers;

  protected FailedJobCommandFactory failedJobCommandFactory;
  
  /**
   * Set this to true if you want to have extra checks on the BPMN xml that is parsed.
   * 
   * Unfortunately, this feature is not available on some platforms (JDK 6, JBoss),
   * hence the reason why it is disabled by default. If your platform allows 
   * the use of StaxSource during XML parsing, do enable it.
   */
  protected boolean enableSafeBpmnXml = false;
  
  /**
   * The following settings will determine the amount of entities loaded at once when the engine 
   * needs to load multiple entities (eg. when suspending a process definition with all its process instances).
   * 
   * The default setting is quite low, as not to surprise anyone with sudden memory spikes.
   * Change it to something higher if the environment Activiti runs in allows it.
   */
  protected int batchSizeProcessInstances = 25;
  protected int batchSizeTasks = 25;
  
  /**
   * If set to true, enables bulk insert (grouping sql inserts together).
   * Default true. For some databases (eg DB2 on Zos: https://activiti.atlassian.net/browse/ACT-4042) needs to be set to false
   */
  protected boolean isBulkInsertEnabled = true;
  
  /**
  * Some databases have a limit of how many parameters one sql insert can have (eg SQL Server, 2000 params (!= insert statements) ).
  * Tweak this parameter in case of exceptions indicating too much is being put into one bulk insert,
  * or make it higher if your database can cope with it and there are inserts with a huge amount of data.
  * 
  * By default: 100.
  */
  protected int maxNrOfStatementsInBulkInsert = 100;
  
  protected ObjectMapper objectMapper = new ObjectMapper();
  
  protected boolean enableEventDispatcher = true;
  protected ActivitiEventDispatcher eventDispatcher;
  
  // Event logging to database
  protected boolean enableDatabaseEventLogging = false;
  
  /**
   * Using field injection together with a delegate expression for a service
   * task / execution listener / task listener is not thread-sade , see user
   * guide section 'Field Injection' for more information.
   * 
   * Set this flag to false to throw an exception at runtime when a field is
   * injected and a delegateExpression is used. Default is true for backwards compatibility.
   * 
   * @since 5.21
   */
  protected DelegateExpressionFieldInjectionMode delegateExpressionFieldInjectionMode = DelegateExpressionFieldInjectionMode.COMPATIBILITY;
  
  /**
   *  Define a max length for storing String variable types in the database.
   *  Mainly used for the Oracle NVARCHAR2 limit of 2000 characters
   */
  protected int maxLengthStringVariableType = 4000;
  
  protected boolean enableProcessDefinitionInfoCache = false;
  
  // Activiti 5 backwards compatibility handler
  protected Activiti5CompatibilityHandler activiti5CompatibilityHandler;
  
  // buildProcessEngine ///////////////////////////////////////////////////////
  
  public ProcessEngine buildProcessEngine() {
    init();
    return new ProcessEngineImpl(this);
  }
  
  // init /////////////////////////////////////////////////////////////////////
  
  protected void init() {
  	initConfigurators();
  	configuratorsBeforeInit();
    initProcessDiagramGenerator();
    initHistoryLevel();
    initExpressionManager();
    initVariableTypes();
    initBeans();
    initFormEngines();
    initFormTypes();
    initScriptingEngines();
    initClock();
    initBusinessCalendarManager();
    initCommandContextFactory();
    initTransactionContextFactory();
    initCommandExecutors();
    initServices();
    initIdGenerator();
    initDeployers();
    initJobHandlers();
    initDataSource();
    initTransactionFactory();
    initSqlSessionFactory();
    initSessionFactories();
    initJpa();
    initDelegateInterceptor();
    initEventHandlers();
    initFailedJobCommandFactory();
    initProcessValidator();
    initDatabaseEventLogging();
    configuratorsAfterInit();
  }

  // failedJobCommandFactory ////////////////////////////////////////////////////////
  
  protected void initFailedJobCommandFactory() {
    if (failedJobCommandFactory == null) {
      failedJobCommandFactory = new DefaultFailedJobCommandFactory();
    }
  }

  // command executors ////////////////////////////////////////////////////////
  
  protected void initCommandExecutors() {
    initDefaultCommandConfig();
    initSchemaCommandConfig();
    initCommandInvoker();
    initCommandInterceptors();
    initCommandExecutor();
  }

  protected void initDefaultCommandConfig() {
    if (defaultCommandConfig==null) {
      defaultCommandConfig = new CommandConfig();
    }
  }

  private void initSchemaCommandConfig() {
    if (schemaCommandConfig==null) {
      schemaCommandConfig = new CommandConfig().transactionNotSupported();
    }
  }

  protected void initCommandInvoker() {
    if (commandInvoker==null) {
      commandInvoker = new CommandInvoker();
    }
  }
  
  protected void initCommandInterceptors() {
    if (commandInterceptors==null) {
      commandInterceptors = new ArrayList<CommandInterceptor>();
      if (customPreCommandInterceptors!=null) {
        commandInterceptors.addAll(customPreCommandInterceptors);
      }
      commandInterceptors.addAll(getDefaultCommandInterceptors());
      if (customPostCommandInterceptors!=null) {
        commandInterceptors.addAll(customPostCommandInterceptors);
      }
      commandInterceptors.add(commandInvoker);
    }
  }

  protected Collection< ? extends CommandInterceptor> getDefaultCommandInterceptors() {
    List<CommandInterceptor> interceptors = new ArrayList<CommandInterceptor>();
    interceptors.add(new LogInterceptor());
    
    CommandInterceptor transactionInterceptor = createTransactionInterceptor();
    if (transactionInterceptor != null) {
      interceptors.add(transactionInterceptor);
    }
    
    interceptors.add(new CommandContextInterceptor(commandContextFactory, this));
    return interceptors;
  }

  protected void initCommandExecutor() {
    if (commandExecutor==null) {
      CommandInterceptor first = initInterceptorChain(commandInterceptors);
      commandExecutor = new CommandExecutorImpl(getDefaultCommandConfig(), first);
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
  
  protected abstract CommandInterceptor createTransactionInterceptor();
  
  // services /////////////////////////////////////////////////////////////////
  
  protected void initServices() {
    initService(repositoryService);
    initService(runtimeService);
    initService(historyService);
    initService(identityService);
    initService(taskService);
    initService(formService);
    initService(managementService);
    initService(dynamicBpmnService);
  }

  protected void initService(Object service) {
    if (service instanceof ServiceImpl) {
      ((ServiceImpl)service).setCommandExecutor(commandExecutor);
    }
  }
  
  // DataSource ///////////////////////////////////////////////////////////////
  
  protected void initDataSource() {
    if (dataSource==null) {
      if (dataSourceJndiName!=null) {
        try {
          dataSource = (DataSource) new InitialContext().lookup(dataSourceJndiName);
        } catch (Exception e) {
          throw new ActivitiException("couldn't lookup datasource from "+dataSourceJndiName+": "+e.getMessage(), e);
        }
        
      } else if (jdbcUrl!=null) {
        if ( (jdbcDriver==null) || (jdbcUrl==null) || (jdbcUsername==null) ) {
          throw new ActivitiException("DataSource or JDBC properties have to be specified in a process engine configuration");
        }
        
        log.debug("initializing datasource to db: {}", jdbcUrl);
        
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
        // ACT-233: connection pool of Ibatis is not properely initialized if this is not called!
        ((PooledDataSource)dataSource).forceCloseAll();
      }
    }

    if (databaseType == null) {
      initDatabaseType();
    }
  }
  
  protected static Properties databaseTypeMappings = getDefaultDatabaseTypeMappings();
  
  public static final String DATABASE_TYPE_H2 = "h2";
  public static final String DATABASE_TYPE_HSQL = "hsql";
  public static final String DATABASE_TYPE_MYSQL = "mysql";
  public static final String DATABASE_TYPE_ORACLE = "oracle";
  public static final String DATABASE_TYPE_POSTGRES = "postgres";
  public static final String DATABASE_TYPE_MSSQL = "mssql";
  public static final String DATABASE_TYPE_DB2 = "db2";

  protected static Properties getDefaultDatabaseTypeMappings() {
    Properties databaseTypeMappings = new Properties();
    databaseTypeMappings.setProperty("H2", DATABASE_TYPE_H2);
    databaseTypeMappings.setProperty("HSQL Database Engine", DATABASE_TYPE_HSQL);
    databaseTypeMappings.setProperty("MySQL", DATABASE_TYPE_MYSQL);
    databaseTypeMappings.setProperty("Oracle", DATABASE_TYPE_ORACLE);
    databaseTypeMappings.setProperty("PostgreSQL", DATABASE_TYPE_POSTGRES);
    databaseTypeMappings.setProperty("Microsoft SQL Server", DATABASE_TYPE_MSSQL);
    databaseTypeMappings.setProperty(DATABASE_TYPE_DB2,DATABASE_TYPE_DB2);
    databaseTypeMappings.setProperty("DB2",DATABASE_TYPE_DB2);
    databaseTypeMappings.setProperty("DB2/NT",DATABASE_TYPE_DB2);
    databaseTypeMappings.setProperty("DB2/NT64",DATABASE_TYPE_DB2);
    databaseTypeMappings.setProperty("DB2 UDP",DATABASE_TYPE_DB2);
    databaseTypeMappings.setProperty("DB2/LINUX",DATABASE_TYPE_DB2);
    databaseTypeMappings.setProperty("DB2/LINUX390",DATABASE_TYPE_DB2);
    databaseTypeMappings.setProperty("DB2/LINUXX8664",DATABASE_TYPE_DB2);
    databaseTypeMappings.setProperty("DB2/LINUXZ64",DATABASE_TYPE_DB2);
    databaseTypeMappings.setProperty("DB2/LINUXPPC64",DATABASE_TYPE_DB2);
    databaseTypeMappings.setProperty("DB2/LINUXPPC64LE",DATABASE_TYPE_DB2);
    databaseTypeMappings.setProperty("DB2/400 SQL",DATABASE_TYPE_DB2);
    databaseTypeMappings.setProperty("DB2/6000",DATABASE_TYPE_DB2);
    databaseTypeMappings.setProperty("DB2 UDB iSeries",DATABASE_TYPE_DB2);
    databaseTypeMappings.setProperty("DB2/AIX64",DATABASE_TYPE_DB2);
    databaseTypeMappings.setProperty("DB2/HPUX",DATABASE_TYPE_DB2);
    databaseTypeMappings.setProperty("DB2/HP64",DATABASE_TYPE_DB2);
    databaseTypeMappings.setProperty("DB2/SUN",DATABASE_TYPE_DB2);
    databaseTypeMappings.setProperty("DB2/SUN64",DATABASE_TYPE_DB2);
    databaseTypeMappings.setProperty("DB2/PTX",DATABASE_TYPE_DB2);
    databaseTypeMappings.setProperty("DB2/2",DATABASE_TYPE_DB2);
    databaseTypeMappings.setProperty("DB2 UDB AS400", DATABASE_TYPE_DB2);
    return databaseTypeMappings;
  }

  public void initDatabaseType() {
    Connection connection = null;
    try {
      connection = dataSource.getConnection();
      DatabaseMetaData databaseMetaData = connection.getMetaData();
      String databaseProductName = databaseMetaData.getDatabaseProductName();
      log.debug("database product name: '{}'", databaseProductName);
      databaseType = databaseTypeMappings.getProperty(databaseProductName);
      if (databaseType==null) {
        throw new ActivitiException("couldn't deduct database type from database product name '"+databaseProductName+"'");
      }
      log.debug("using database type: {}", databaseType);

    } catch (SQLException e) {
      log.error("Exception while initializing Database connection", e);
    } finally {
      try {
        if (connection!=null) {
          connection.close();
        }
      } catch (SQLException e) {
          log.error("Exception while closing the Database connection", e);
      }
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
        inputStream = getMyBatisXmlConfigurationSteam();

        // update the jdbc parameters to the configured ones...
        Environment environment = new Environment("default", transactionFactory, dataSource);
        Reader reader = new InputStreamReader(inputStream);
        Properties properties = new Properties();
        properties.put("prefix", databaseTablePrefix);
        String wildcardEscapeClause = "";
        if ((databaseWildcardEscapeCharacter != null) && (databaseWildcardEscapeCharacter.length() != 0)) {
          wildcardEscapeClause = " escape '" + databaseWildcardEscapeCharacter + "'";
        }
        properties.put("wildcardEscapeClause", wildcardEscapeClause);
        if(databaseType != null) {
          properties.put("limitBefore" , DbSqlSessionFactory.databaseSpecificLimitBeforeStatements.get(databaseType));
          properties.put("limitAfter" , DbSqlSessionFactory.databaseSpecificLimitAfterStatements.get(databaseType));
          properties.put("limitBetween" , DbSqlSessionFactory.databaseSpecificLimitBetweenStatements.get(databaseType));
          properties.put("limitOuterJoinBetween" , DbSqlSessionFactory.databaseOuterJoinLimitBetweenStatements.get(databaseType));
          properties.put("orderBy" , DbSqlSessionFactory.databaseSpecificOrderByStatements.get(databaseType));
          properties.put("limitBeforeNativeQuery" , Objects.toString(DbSqlSessionFactory.databaseSpecificLimitBeforeNativeQueryStatements.get(databaseType), ""));
        }
        
        Configuration configuration = initMybatisConfiguration(environment, reader, properties);
        sqlSessionFactory = new DefaultSqlSessionFactory(configuration);

      } catch (Exception e) {
        throw new ActivitiException("Error while building ibatis SqlSessionFactory: " + e.getMessage(), e);
      } finally {
        IoUtil.closeSilently(inputStream);
      }
    }
  }

	protected Configuration initMybatisConfiguration(Environment environment, Reader reader, Properties properties) {
	  XMLConfigBuilder parser = new XMLConfigBuilder(reader,"", properties);
	  Configuration configuration = parser.getConfiguration();
	  configuration.setEnvironment(environment);
	  
	  initMybatisTypeHandlers(configuration);
	  initCustomMybatisMappers(configuration);
	  
	  configuration = parseMybatisConfiguration(configuration, parser);
	  return configuration;
  }

	protected void initMybatisTypeHandlers(Configuration configuration) {
	  configuration.getTypeHandlerRegistry().register(VariableType.class, JdbcType.VARCHAR, new IbatisVariableTypeHandler());
  }

	protected void initCustomMybatisMappers(Configuration configuration) {
	  if (getCustomMybatisMappers() != null) {
	  	for (Class<?> clazz : getCustomMybatisMappers()) {
	  		configuration.addMapper(clazz);
	  	}
	  }
  }
	
	protected Configuration parseMybatisConfiguration(Configuration configuration, XMLConfigBuilder parser) {
	  return parseCustomMybatisXMLMappers(parser.parse());
  }
	
	protected Configuration parseCustomMybatisXMLMappers(Configuration configuration) {
	  if (getCustomMybatisXMLMappers() != null)
    // see XMLConfigBuilder.mapperElement()
    for(String resource: getCustomMybatisXMLMappers()){
      XMLMapperBuilder mapperParser = new XMLMapperBuilder(getResourceAsStream(resource), 
          configuration, resource, configuration.getSqlFragments());
      mapperParser.parse();
    }
    return configuration;
  }
  
	protected InputStream getResourceAsStream(String resource) {
    return ReflectUtil.getResourceAsStream(resource);
  }
	
  protected InputStream getMyBatisXmlConfigurationSteam() {
    return getResourceAsStream(DEFAULT_MYBATIS_MAPPING_FILE);
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
  
  // session factories ////////////////////////////////////////////////////////
  

  protected void initSessionFactories() {
    if (sessionFactories==null) {
      sessionFactories = new HashMap<Class<?>, SessionFactory>();

      if (dbSqlSessionFactory == null) {
        dbSqlSessionFactory = new DbSqlSessionFactory();
      }
      dbSqlSessionFactory.setDatabaseType(databaseType);
      dbSqlSessionFactory.setIdGenerator(idGenerator);
      dbSqlSessionFactory.setSqlSessionFactory(sqlSessionFactory);
      dbSqlSessionFactory.setDbIdentityUsed(isDbIdentityUsed);
      dbSqlSessionFactory.setDbHistoryUsed(isDbHistoryUsed);
      dbSqlSessionFactory.setDatabaseTablePrefix(databaseTablePrefix);
      dbSqlSessionFactory.setTablePrefixIsSchema(tablePrefixIsSchema);
      dbSqlSessionFactory.setDatabaseCatalog(databaseCatalog);
      dbSqlSessionFactory.setDatabaseSchema(databaseSchema);
      dbSqlSessionFactory.setBulkInsertEnabled(isBulkInsertEnabled, databaseType);
      dbSqlSessionFactory.setMaxNrOfStatementsInBulkInsert(maxNrOfStatementsInBulkInsert);
      addSessionFactory(dbSqlSessionFactory);
      
      addSessionFactory(new GenericManagerFactory(AttachmentEntityManager.class));
      addSessionFactory(new GenericManagerFactory(CommentEntityManager.class));
      addSessionFactory(new GenericManagerFactory(DeadLetterJobEntityManager.class));
      addSessionFactory(new GenericManagerFactory(DeploymentEntityManager.class));
      addSessionFactory(new GenericManagerFactory(ModelEntityManager.class));
      addSessionFactory(new GenericManagerFactory(ExecutionEntityManager.class));
      addSessionFactory(new GenericManagerFactory(HistoricActivityInstanceEntityManager.class));
      addSessionFactory(new GenericManagerFactory(HistoricDetailEntityManager.class));
      addSessionFactory(new GenericManagerFactory(HistoricProcessInstanceEntityManager.class));
      addSessionFactory(new GenericManagerFactory(HistoricVariableInstanceEntityManager.class));
      addSessionFactory(new GenericManagerFactory(HistoricTaskInstanceEntityManager.class));
      addSessionFactory(new GenericManagerFactory(HistoricIdentityLinkEntityManager.class));
      addSessionFactory(new GenericManagerFactory(IdentityInfoEntityManager.class));
      addSessionFactory(new GenericManagerFactory(IdentityLinkEntityManager.class));
      addSessionFactory(new GenericManagerFactory(JobEntityManager.class));
      addSessionFactory(new GenericManagerFactory(ProcessDefinitionEntityManager.class));
      addSessionFactory(new GenericManagerFactory(ProcessDefinitionInfoEntityManager.class));
      addSessionFactory(new GenericManagerFactory(PropertyEntityManager.class));
      addSessionFactory(new GenericManagerFactory(ResourceEntityManager.class));
      addSessionFactory(new GenericManagerFactory(ByteArrayEntityManager.class));
      addSessionFactory(new GenericManagerFactory(SuspendedJobEntityManager.class));
      addSessionFactory(new GenericManagerFactory(TableDataManager.class));
      addSessionFactory(new GenericManagerFactory(TaskEntityManager.class));
      addSessionFactory(new GenericManagerFactory(TimerJobEntityManager.class));
      addSessionFactory(new GenericManagerFactory(VariableInstanceEntityManager.class));
      addSessionFactory(new GenericManagerFactory(EventSubscriptionEntityManager.class));
      addSessionFactory(new GenericManagerFactory(EventLogEntryEntityManager.class));
      
      addSessionFactory(new DefaultHistoryManagerSessionFactory());
      
      addSessionFactory(new UserEntityManagerFactory());
      addSessionFactory(new GroupEntityManagerFactory());
      addSessionFactory(new MembershipEntityManagerFactory());
    }
    
    if (customSessionFactories!=null) {
      for (SessionFactory sessionFactory: customSessionFactories) {
        addSessionFactory(sessionFactory);
      }
    }
  }
  
  protected void addSessionFactory(SessionFactory sessionFactory) {
    sessionFactories.put(sessionFactory.getSessionType(), sessionFactory);
  }
  
  protected void initConfigurators() {
  	
  	allConfigurators = new ArrayList<ProcessEngineConfigurator>();
  	
  	// Configurators that are explicitely added to the config
    if (configurators != null) {
      for (ProcessEngineConfigurator configurator : configurators) {
        allConfigurators.add(configurator);
      }
    }
    
    // Auto discovery through ServiceLoader
    if (enableConfiguratorServiceLoader) {
    	ClassLoader classLoader = getClassLoader();
    	if (classLoader == null) {
    		classLoader = ReflectUtil.getClassLoader();
    	}
    	
    	ServiceLoader<ProcessEngineConfigurator> configuratorServiceLoader
    			= ServiceLoader.load(ProcessEngineConfigurator.class, classLoader);
    	int nrOfServiceLoadedConfigurators = 0;
    	for (ProcessEngineConfigurator configurator : configuratorServiceLoader) {
    		allConfigurators.add(configurator);
    		nrOfServiceLoadedConfigurators++;
    	}
    	
    	if (nrOfServiceLoadedConfigurators > 0) {
    		log.info("Found {} auto-discoverable Process Engine Configurator{}", nrOfServiceLoadedConfigurators++, nrOfServiceLoadedConfigurators > 1 ? "s" : "");
    	}
    	
    	if (!allConfigurators.isEmpty()) {
    		
    		// Order them according to the priorities (usefule for dependent configurator)
	    	Collections.sort(allConfigurators, new Comparator<ProcessEngineConfigurator>() {
	    		@Override
	    		public int compare(ProcessEngineConfigurator configurator1, ProcessEngineConfigurator configurator2) {
	    			int priority1 = configurator1.getPriority();
	    			int priority2 = configurator2.getPriority();
	    			
	    			if (priority1 < priority2) {
	    				return -1;
	    			} else if (priority1 > priority2) {
	    				return 1;
	    			} 
	    			return 0;
	    		}
				});
	    	
	    	// Execute the configurators
	    	log.info("Found {} Process Engine Configurators in total:", allConfigurators.size());
	    	for (ProcessEngineConfigurator configurator : allConfigurators) {
	    		log.info("{} (priority:{})", configurator.getClass(), configurator.getPriority());
	    	}
	    	
    	}
    	
    }
  }
  
  protected void configuratorsBeforeInit() {
  	for (ProcessEngineConfigurator configurator : allConfigurators) {
  		log.info("Executing beforeInit() of {} (priority:{})", configurator.getClass(), configurator.getPriority());
  		configurator.beforeInit(this);
  	}
  }
  
  protected void configuratorsAfterInit() {
  	for (ProcessEngineConfigurator configurator : allConfigurators) {
  		log.info("Executing configure() of {} (priority:{})", configurator.getClass(), configurator.getPriority());
  		configurator.configure(this);
  	}
  }
  
  // deployers ////////////////////////////////////////////////////////////////
  
  protected void initDeployers() {
    if (this.deployers==null) {
      this.deployers = new ArrayList<Deployer>();
      if (customPreDeployers!=null) {
        this.deployers.addAll(customPreDeployers);
      }
      this.deployers.addAll(getDefaultDeployers());
      if (customPostDeployers!=null) {
        this.deployers.addAll(customPostDeployers);
      }
    }
    if (deploymentManager==null) {
      deploymentManager = new DeploymentManager();
      deploymentManager.setDeployers(deployers);
      
      // BpmnModel cache
      if (bpmnModelCache == null) {
        if (bpmnModelCacheLimit <= 0) {
          bpmnModelCache = new DefaultDeploymentCache<BpmnModel>();
        } else {
          bpmnModelCache = new DefaultDeploymentCache<BpmnModel>(bpmnModelCacheLimit);
        }
      }
      
      if (processDefinitionInfoCache == null) {
        if (processDefinitionInfoCacheLimit <= 0) {
          processDefinitionInfoCache = new ProcessDefinitionInfoCache(commandExecutor);
        } else {
          processDefinitionInfoCache = new ProcessDefinitionInfoCache(commandExecutor, processDefinitionInfoCacheLimit);
        }
      }
      
      // Knowledge base cache (used for Drools business task)
      if (knowledgeBaseCache == null) {
        if (knowledgeBaseCacheLimit <= 0) {
          knowledgeBaseCache = new DefaultDeploymentCache<Object>();
        } else {
          knowledgeBaseCache = new DefaultDeploymentCache<Object>(knowledgeBaseCacheLimit);
        }
      }
      
      deploymentManager.setProcessDefinitionCache(processDefinitionCache);
      deploymentManager.setBpmnModelCache(bpmnModelCache);
      deploymentManager.setProcessDefinitionInfoCache(processDefinitionInfoCache);
      deploymentManager.setKnowledgeBaseCache(knowledgeBaseCache);
    }
  }

  protected Collection< ? extends Deployer> getDefaultDeployers() {
    List<Deployer> defaultDeployers = new ArrayList<Deployer>();

    if (bpmnDeployer == null) {
      bpmnDeployer = new BpmnDeployer();
    }
      
    bpmnDeployer.setExpressionManager(expressionManager);
    bpmnDeployer.setIdGenerator(idGenerator);
    
    if (bpmnParseFactory == null) {
      bpmnParseFactory = new DefaultBpmnParseFactory();
    }
    
	if (activityBehaviorFactory == null) {
	  DefaultActivityBehaviorFactory defaultActivityBehaviorFactory = new DefaultActivityBehaviorFactory();
	  defaultActivityBehaviorFactory.setExpressionManager(expressionManager);
	  activityBehaviorFactory = defaultActivityBehaviorFactory;
	} else if ((activityBehaviorFactory instanceof AbstractBehaviorFactory)
			&& ((AbstractBehaviorFactory) activityBehaviorFactory).getExpressionManager() == null) {
		((AbstractBehaviorFactory) activityBehaviorFactory).setExpressionManager(expressionManager);
	}

	if (listenerFactory == null) {
	  DefaultListenerFactory defaultListenerFactory = new DefaultListenerFactory();
	  defaultListenerFactory.setExpressionManager(expressionManager);
	  listenerFactory = defaultListenerFactory;
	} else if ((listenerFactory instanceof AbstractBehaviorFactory)
			&& ((AbstractBehaviorFactory) listenerFactory).getExpressionManager() == null) {
		((AbstractBehaviorFactory) listenerFactory).setExpressionManager(expressionManager);
	}
    
    if (bpmnParser == null) {
      bpmnParser = new BpmnParser();
    }
    
    bpmnParser.setExpressionManager(expressionManager);
    bpmnParser.setBpmnParseFactory(bpmnParseFactory);
    bpmnParser.setActivityBehaviorFactory(activityBehaviorFactory);
    bpmnParser.setListenerFactory(listenerFactory);
    
    List<BpmnParseHandler> parseHandlers = new ArrayList<BpmnParseHandler>();
    if(getPreBpmnParseHandlers() != null) {
      parseHandlers.addAll(getPreBpmnParseHandlers());
    }
    parseHandlers.addAll(getDefaultBpmnParseHandlers());
    if(getPostBpmnParseHandlers() != null) {
      parseHandlers.addAll(getPostBpmnParseHandlers());
    }
    
    BpmnParseHandlers bpmnParseHandlers = new BpmnParseHandlers();
    bpmnParseHandlers.addHandlers(parseHandlers);
    bpmnParser.setBpmnParserHandlers(bpmnParseHandlers);
    
    bpmnDeployer.setBpmnParser(bpmnParser);
    
    defaultDeployers.add(bpmnDeployer);
    return defaultDeployers;
  }
  
  protected List<BpmnParseHandler> getDefaultBpmnParseHandlers() {
    
    // Alpabetic list of default parse handler classes
    List<BpmnParseHandler> bpmnParserHandlers = new ArrayList<BpmnParseHandler>();
    bpmnParserHandlers.add(new BoundaryEventParseHandler());
    bpmnParserHandlers.add(new BusinessRuleParseHandler());
    bpmnParserHandlers.add(new CallActivityParseHandler());
    bpmnParserHandlers.add(new CancelEventDefinitionParseHandler());
    bpmnParserHandlers.add(new CompensateEventDefinitionParseHandler());
    bpmnParserHandlers.add(new EndEventParseHandler());
    bpmnParserHandlers.add(new ErrorEventDefinitionParseHandler());
    bpmnParserHandlers.add(new EventBasedGatewayParseHandler());
    bpmnParserHandlers.add(new ExclusiveGatewayParseHandler());
    bpmnParserHandlers.add(new InclusiveGatewayParseHandler());
    bpmnParserHandlers.add(new IntermediateCatchEventParseHandler());
    bpmnParserHandlers.add(new IntermediateThrowEventParseHandler());
    bpmnParserHandlers.add(new ManualTaskParseHandler());
    bpmnParserHandlers.add(new MessageEventDefinitionParseHandler());
    bpmnParserHandlers.add(new ParallelGatewayParseHandler());
    bpmnParserHandlers.add(new ProcessParseHandler());
    bpmnParserHandlers.add(new ReceiveTaskParseHandler());
    bpmnParserHandlers.add(new ScriptTaskParseHandler());
    bpmnParserHandlers.add(new SendTaskParseHandler());
    bpmnParserHandlers.add(new SequenceFlowParseHandler());
    bpmnParserHandlers.add(new ServiceTaskParseHandler());
    bpmnParserHandlers.add(new SignalEventDefinitionParseHandler());
    bpmnParserHandlers.add(new StartEventParseHandler());
    bpmnParserHandlers.add(new SubProcessParseHandler());
    bpmnParserHandlers.add(new EventSubProcessParseHandler());
    bpmnParserHandlers.add(new TaskParseHandler());
    bpmnParserHandlers.add(new TimerEventDefinitionParseHandler());
    bpmnParserHandlers.add(new TransactionParseHandler());
    bpmnParserHandlers.add(new UserTaskParseHandler());
    
    // Replace any default handler if the user wants to replace them
    if (customDefaultBpmnParseHandlers != null) {
      
      Map<Class<?>, BpmnParseHandler> customParseHandlerMap = new HashMap<Class<?>, BpmnParseHandler>();
      for (BpmnParseHandler bpmnParseHandler : customDefaultBpmnParseHandlers) {
        for (Class<?> handledType : bpmnParseHandler.getHandledTypes()) {
          customParseHandlerMap.put(handledType, bpmnParseHandler);
        }
      }
      
      for (int i=0; i<bpmnParserHandlers.size(); i++) {
        // All the default handlers support only one type
        BpmnParseHandler defaultBpmnParseHandler = bpmnParserHandlers.get(i);
        if (defaultBpmnParseHandler.getHandledTypes().size() != 1) {
          StringBuilder supportedTypes = new StringBuilder();
          for (Class<?> type : defaultBpmnParseHandler.getHandledTypes()) {
            supportedTypes.append(" ").append(type.getCanonicalName()).append(" ");
          }
          throw new ActivitiException("The default BPMN parse handlers should only support one type, but " + defaultBpmnParseHandler.getClass() 
                  + " supports " + supportedTypes.toString() + ". This is likely a programmatic error");
        } else {
          Class<?> handledType = defaultBpmnParseHandler.getHandledTypes().iterator().next();
          if (customParseHandlerMap.containsKey(handledType)) {
            BpmnParseHandler newBpmnParseHandler = customParseHandlerMap.get(handledType);
            log.info("Replacing default BpmnParseHandler " + defaultBpmnParseHandler.getClass().getName() + " with " + newBpmnParseHandler.getClass().getName());
            bpmnParserHandlers.set(i, newBpmnParseHandler);
          }
        }
      }
    }
    
    // History
    for (BpmnParseHandler handler : getDefaultHistoryParseHandlers()) {
      bpmnParserHandlers.add(handler);
    }
    
    return bpmnParserHandlers;
  }
  
  protected List<BpmnParseHandler> getDefaultHistoryParseHandlers() {
    List<BpmnParseHandler> parseHandlers = new ArrayList<BpmnParseHandler>();
    parseHandlers.add(new FlowNodeHistoryParseHandler());
    parseHandlers.add(new ProcessHistoryParseHandler());
    parseHandlers.add(new StartEventHistoryParseHandler());
    parseHandlers.add(new UserTaskHistoryParseHandler());
    return parseHandlers;
  }

  private void initClock() {
    if (clock == null) {
      clock = new DefaultClockImpl();
    }
  }

  protected void initProcessDiagramGenerator() {
    if (processDiagramGenerator == null) {
      processDiagramGenerator = new DefaultProcessDiagramGenerator();
    }
  }
  
  protected void initJobHandlers() {
    jobHandlers = new HashMap<String, JobHandler>();
    TimerExecuteNestedActivityJobHandler timerExecuteNestedActivityJobHandler = new TimerExecuteNestedActivityJobHandler();
    jobHandlers.put(timerExecuteNestedActivityJobHandler.getType(), timerExecuteNestedActivityJobHandler);

    TimerCatchIntermediateEventJobHandler timerCatchIntermediateEvent = new TimerCatchIntermediateEventJobHandler();
    jobHandlers.put(timerCatchIntermediateEvent.getType(), timerCatchIntermediateEvent);

    TimerStartEventJobHandler timerStartEvent = new TimerStartEventJobHandler();
    jobHandlers.put(timerStartEvent.getType(), timerStartEvent);
    
    AsyncContinuationJobHandler asyncContinuationJobHandler = new AsyncContinuationJobHandler();
    jobHandlers.put(asyncContinuationJobHandler.getType(), asyncContinuationJobHandler);
    
    ProcessEventJobHandler processEventJobHandler = new ProcessEventJobHandler();
    jobHandlers.put(processEventJobHandler.getType(), processEventJobHandler);
    
    TimerSuspendProcessDefinitionHandler suspendProcessDefinitionHandler = new TimerSuspendProcessDefinitionHandler();
    jobHandlers.put(suspendProcessDefinitionHandler.getType(), suspendProcessDefinitionHandler);
    
    TimerActivateProcessDefinitionHandler activateProcessDefinitionHandler = new TimerActivateProcessDefinitionHandler();
    jobHandlers.put(activateProcessDefinitionHandler.getType(), activateProcessDefinitionHandler);
    
    // if we have custom job handlers, register them
    if (getCustomJobHandlers()!=null) {
      for (JobHandler customJobHandler : getCustomJobHandlers()) {
        jobHandlers.put(customJobHandler.getType(), customJobHandler);      
      }
    }
  }
  
  // history //////////////////////////////////////////////////////////////////
  
  public void initHistoryLevel() {
  	if (historyLevel == null) {
  		historyLevel = HistoryLevel.getHistoryLevelForKey(getHistory());
  	}
  }
  
  // id generator /////////////////////////////////////////////////////////////
  
  protected void initIdGenerator() {
    if (idGenerator==null) {
      CommandExecutor idGeneratorCommandExecutor = null;
      if (idGeneratorDataSource!=null) {
        ProcessEngineConfigurationImpl processEngineConfiguration = new StandaloneProcessEngineConfiguration();
        processEngineConfiguration.setDataSource(idGeneratorDataSource);
        processEngineConfiguration.setDatabaseSchemaUpdate(DB_SCHEMA_UPDATE_FALSE);
        processEngineConfiguration.init();
        idGeneratorCommandExecutor = processEngineConfiguration.getCommandExecutor();
      } else if (idGeneratorDataSourceJndiName!=null) {
        ProcessEngineConfigurationImpl processEngineConfiguration = new StandaloneProcessEngineConfiguration();
        processEngineConfiguration.setDataSourceJndiName(idGeneratorDataSourceJndiName);
        processEngineConfiguration.setDatabaseSchemaUpdate(DB_SCHEMA_UPDATE_FALSE);
        processEngineConfiguration.init();
        idGeneratorCommandExecutor = processEngineConfiguration.getCommandExecutor();
      } else {
        idGeneratorCommandExecutor = getCommandExecutor();
      }
      
      DbIdGenerator dbIdGenerator = new DbIdGenerator();
      dbIdGenerator.setIdBlockSize(idBlockSize);
      dbIdGenerator.setCommandExecutor(idGeneratorCommandExecutor);
      dbIdGenerator.setCommandConfig(getDefaultCommandConfig().transactionRequiresNew());
      idGenerator = dbIdGenerator;
    }
  }

  // OTHER ////////////////////////////////////////////////////////////////////
  
  protected void initCommandContextFactory() {
    if (commandContextFactory==null) {
      commandContextFactory = new CommandContextFactory();
    }
    commandContextFactory.setProcessEngineConfiguration(this);
  }

  protected void initTransactionContextFactory() {
    if (transactionContextFactory==null) {
      transactionContextFactory = new StandaloneMybatisTransactionContextFactory();
    }
  }

  protected void initVariableTypes() {
    if (variableTypes==null) {
      variableTypes = new DefaultVariableTypes();
      if (customPreVariableTypes!=null) {
        for (VariableType customVariableType: customPreVariableTypes) {
          variableTypes.addType(customVariableType);
        }
      }
      variableTypes.addType(new NullType());
      variableTypes.addType(new StringType(maxLengthStringVariableType));
      variableTypes.addType(new LongStringType(maxLengthStringVariableType + 1));
      variableTypes.addType(new BooleanType());
      variableTypes.addType(new ShortType());
      variableTypes.addType(new IntegerType());
      variableTypes.addType(new LongType());
      variableTypes.addType(new DateType());
      variableTypes.addType(new DoubleType());
      variableTypes.addType(new UUIDType());
      variableTypes.addType(new JsonType(maxLengthStringVariableType, objectMapper));
      variableTypes.addType(new LongJsonType(maxLengthStringVariableType + 1, objectMapper));
      variableTypes.addType(new ByteArrayType());
      variableTypes.addType(new SerializableType());
      variableTypes.addType(new CustomObjectType("item", ItemInstance.class));
      variableTypes.addType(new CustomObjectType("message", MessageInstance.class));
      if (customPostVariableTypes != null) {
        for (VariableType customVariableType: customPostVariableTypes) {
          variableTypes.addType(customVariableType);
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
    if (customFormEngines!=null) {
      for (FormEngine formEngine: customFormEngines) {
        formEngines.put(formEngine.getName(), formEngine);
      }
    }
  }

  protected void initFormTypes() {
    if (formTypes==null) {
      formTypes = new FormTypes();
      formTypes.addFormType(new StringFormType());
      formTypes.addFormType(new LongFormType());
      formTypes.addFormType(new DateFormType("dd/MM/yyyy"));
      formTypes.addFormType(new BooleanFormType());
      formTypes.addFormType(new DoubleFormType());
    }
    if (customFormTypes!=null) {
      for (AbstractFormType customFormType: customFormTypes) {
        formTypes.addFormType(customFormType);
      }
    }
  }

  protected void initScriptingEngines() {
    if (resolverFactories==null) {
      resolverFactories = new ArrayList<ResolverFactory>();
      resolverFactories.add(new VariableScopeResolverFactory());
      resolverFactories.add(new BeansResolverFactory());
    }
    if (scriptingEngines==null) {
      scriptingEngines = new ScriptingEngines(new ScriptBindingsFactory(resolverFactories));
    }
  }

  protected void initExpressionManager() {
    if (expressionManager==null) {
      expressionManager = new ExpressionManager(beans);
    }
  }

  protected void initBusinessCalendarManager() {
    if (businessCalendarManager==null) {
      MapBusinessCalendarManager mapBusinessCalendarManager = new MapBusinessCalendarManager();
      mapBusinessCalendarManager.addBusinessCalendar(DurationBusinessCalendar.NAME, new DurationBusinessCalendar(this.clock));
      mapBusinessCalendarManager.addBusinessCalendar(DueDateBusinessCalendar.NAME, new DueDateBusinessCalendar(this.clock));
      mapBusinessCalendarManager.addBusinessCalendar(CycleBusinessCalendar.NAME, new CycleBusinessCalendar(this.clock));

      businessCalendarManager = mapBusinessCalendarManager;
    }
  }
  
  protected void initDelegateInterceptor() {
    if(delegateInterceptor == null) {
      delegateInterceptor = new DefaultDelegateInterceptor();
    }
  }
  
  protected void initEventHandlers() {
    if(eventHandlers == null) {
      eventHandlers = new HashMap<String, EventHandler>();
      
      SignalEventHandler signalEventHander = new SignalEventHandler();
      eventHandlers.put(signalEventHander.getEventHandlerType(), signalEventHander);
      
      CompensationEventHandler compensationEventHandler = new CompensationEventHandler();
      eventHandlers.put(compensationEventHandler.getEventHandlerType(), compensationEventHandler);
      
      MessageEventHandler messageEventHandler = new MessageEventHandler();
      eventHandlers.put(messageEventHandler.getEventHandlerType(), messageEventHandler);
      
    }
    if(customEventHandlers != null) {
      for (EventHandler eventHandler : customEventHandlers) {
        eventHandlers.put(eventHandler.getEventHandlerType(), eventHandler);        
      }
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
        
      jpaType = variableTypes.getVariableType(JPAEntityListVariableType.TYPE_NAME);
      
      // Add JPA-list type after regular JPA type if not already present
      if(jpaType == null) {
        variableTypes.addType(new JPAEntityListVariableType(), variableTypes.getTypeIndex(JPAEntityVariableType.TYPE_NAME));
      }        
    }
  }
  
  protected void initBeans() {
    if (beans == null) {
      beans = new HashMap<Object, Object>();
    }
  }
  
  protected void initProcessValidator() {
  	if (this.processValidator == null) {
  		this.processValidator = new ProcessValidatorFactory().createDefaultProcessValidator();
  	}
  }
  
  protected void initDatabaseEventLogging() {
  	if (enableDatabaseEventLogging) {
  		// Database event logging uses the default logging mechanism and adds
  		// a specific event listener to the list of event listeners
  		getEventDispatcher().addEventListener(new EventLogger(clock, objectMapper));
  	}
  }

  // getters and setters //////////////////////////////////////////////////////
  
  public CommandConfig getDefaultCommandConfig() {
    return defaultCommandConfig;
  }
  
  public void setDefaultCommandConfig(CommandConfig defaultCommandConfig) {
    this.defaultCommandConfig = defaultCommandConfig;
  }
  
  public CommandConfig getSchemaCommandConfig() {
    return schemaCommandConfig;
  }
  
  public void setSchemaCommandConfig(CommandConfig schemaCommandConfig) {
    this.schemaCommandConfig = schemaCommandConfig;
  }

  public CommandInterceptor getCommandInvoker() {
    return commandInvoker;
  }
  
  public void setCommandInvoker(CommandInterceptor commandInvoker) {
    this.commandInvoker = commandInvoker;
  }

  public List<CommandInterceptor> getCustomPreCommandInterceptors() {
    return customPreCommandInterceptors;
  }
  
  public ProcessEngineConfigurationImpl setCustomPreCommandInterceptors(List<CommandInterceptor> customPreCommandInterceptors) {
    this.customPreCommandInterceptors = customPreCommandInterceptors;
    return this;
  }
  
  public List<CommandInterceptor> getCustomPostCommandInterceptors() {
    return customPostCommandInterceptors;
  }
  
  public ProcessEngineConfigurationImpl setCustomPostCommandInterceptors(List<CommandInterceptor> customPostCommandInterceptors) {
    this.customPostCommandInterceptors = customPostCommandInterceptors;
    return this;
  }
  
  public List<CommandInterceptor> getCommandInterceptors() {
    return commandInterceptors;
  }
  
  public ProcessEngineConfigurationImpl setCommandInterceptors(List<CommandInterceptor> commandInterceptors) {
    this.commandInterceptors = commandInterceptors;
    return this;
  }
  
  public CommandExecutor getCommandExecutor() {
    return commandExecutor;
  }
  
  public ProcessEngineConfigurationImpl setCommandExecutor(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
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
  
  public DynamicBpmnService getDynamicBpmnService() {
    return dynamicBpmnService;
  }
  
  public ProcessEngineConfigurationImpl setManagementService(ManagementService managementService) {
    this.managementService = managementService;
    return this;
  }
  
  public ProcessEngineConfiguration getProcessEngineConfiguration() {
    return this;
  }
  
  public Map<Class< ? >, SessionFactory> getSessionFactories() {
    return sessionFactories;
  }
  
  public ProcessEngineConfigurationImpl setSessionFactories(Map<Class< ? >, SessionFactory> sessionFactories) {
    this.sessionFactories = sessionFactories;
    return this;
  }
  
  public List<ProcessEngineConfigurator> getConfigurators() {
    return configurators;
  }

  public ProcessEngineConfigurationImpl addConfigurator(ProcessEngineConfigurator configurator) {
    if(this.configurators == null) {
      this.configurators = new ArrayList<ProcessEngineConfigurator>();
    }
    this.configurators.add(configurator);
    return this;
  }
  
  public ProcessEngineConfigurationImpl setConfigurators(List<ProcessEngineConfigurator> configurators) {
    this.configurators = configurators;
    return this;
  }

  public void setEnableConfiguratorServiceLoader(boolean enableConfiguratorServiceLoader) {
  	this.enableConfiguratorServiceLoader = enableConfiguratorServiceLoader;
  }

  public List<ProcessEngineConfigurator> getAllConfigurators() {
		return allConfigurators;
  }

	public BpmnDeployer getBpmnDeployer() {
    return bpmnDeployer;
  }

  public ProcessEngineConfigurationImpl setBpmnDeployer(BpmnDeployer bpmnDeployer) {
    this.bpmnDeployer = bpmnDeployer;
    return this;
  }
  
  public BpmnParser getBpmnParser() {
    return bpmnParser;
  }
  
  public ProcessEngineConfigurationImpl setBpmnParser(BpmnParser bpmnParser) {
    this.bpmnParser = bpmnParser;
    return this;
  }

  public List<Deployer> getDeployers() {
    return deployers;
  }
  
  public ProcessEngineConfigurationImpl setDeployers(List<Deployer> deployers) {
    this.deployers = deployers;
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
  
  /**
   * Add or replace the address of the given web-service endpoint with the given value
   * @param endpointName The endpoint name for which a new address must be set
   * @param address The new address of the endpoint
   */
  public ProcessEngineConfiguration addWsEndpointAddress(QName endpointName, URL address) {
      this.wsOverridenEndpointAddresses.put(endpointName, address);
      return this;
  }
  
  /**
   * Remove the address definition of the given web-service endpoint
   * @param endpointName The endpoint name for which the address definition must be removed
   */
  public ProcessEngineConfiguration removeWsEndpointAddress(QName endpointName) {
      this.wsOverridenEndpointAddresses.remove(endpointName);
      return this;
  }
  
  public ConcurrentMap<QName, URL> getWsOverridenEndpointAddresses() {
      return this.wsOverridenEndpointAddresses;
  }
  
  public ProcessEngineConfiguration setWsOverridenEndpointAddresses(final ConcurrentMap<QName, URL> wsOverridenEndpointAdress) {
    this.wsOverridenEndpointAddresses.putAll(wsOverridenEndpointAdress);
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
  
  public int getExecutionQueryLimit() {
    return executionQueryLimit;
  }

  public ProcessEngineConfigurationImpl setExecutionQueryLimit(int executionQueryLimit) {
    this.executionQueryLimit = executionQueryLimit;
    return this;
  }

  public int getTaskQueryLimit() {
    return taskQueryLimit;
  }

  public ProcessEngineConfigurationImpl setTaskQueryLimit(int taskQueryLimit) {
    this.taskQueryLimit = taskQueryLimit;
    return this;
  }

  public int getHistoricTaskQueryLimit() {
    return historicTaskQueryLimit;
  }

  public ProcessEngineConfigurationImpl setHistoricTaskQueryLimit(int historicTaskQueryLimit) {
    this.historicTaskQueryLimit = historicTaskQueryLimit;
    return this;
  }

  public int getHistoricProcessInstancesQueryLimit() {
    return historicProcessInstancesQueryLimit;
  }

  public ProcessEngineConfigurationImpl setHistoricProcessInstancesQueryLimit(int historicProcessInstancesQueryLimit) {
    this.historicProcessInstancesQueryLimit = historicProcessInstancesQueryLimit;
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

  public List<SessionFactory> getCustomSessionFactories() {
    return customSessionFactories;
  }
  
  public ProcessEngineConfigurationImpl setCustomSessionFactories(List<SessionFactory> customSessionFactories) {
    this.customSessionFactories = customSessionFactories;
    return this;
  }
  
  public List<JobHandler> getCustomJobHandlers() {
    return customJobHandlers;
  }
  
  public ProcessEngineConfigurationImpl setCustomJobHandlers(List<JobHandler> customJobHandlers) {
    this.customJobHandlers = customJobHandlers;
    return this;
  }
  
  public List<FormEngine> getCustomFormEngines() {
    return customFormEngines;
  }
  
  public ProcessEngineConfigurationImpl setCustomFormEngines(List<FormEngine> customFormEngines) {
    this.customFormEngines = customFormEngines;
    return this;
  }

  public List<AbstractFormType> getCustomFormTypes() {
    return customFormTypes;
  }

  public ProcessEngineConfigurationImpl setCustomFormTypes(List<AbstractFormType> customFormTypes) {
    this.customFormTypes = customFormTypes;
    return this;
  }

  public List<String> getCustomScriptingEngineClasses() {
    return customScriptingEngineClasses;
  }
  
  public ProcessEngineConfigurationImpl setCustomScriptingEngineClasses(List<String> customScriptingEngineClasses) {
    this.customScriptingEngineClasses = customScriptingEngineClasses;
    return this;
  }

  public List<VariableType> getCustomPreVariableTypes() {
    return customPreVariableTypes;
  }

  public ProcessEngineConfigurationImpl setCustomPreVariableTypes(List<VariableType> customPreVariableTypes) {
    this.customPreVariableTypes = customPreVariableTypes;
    return this;
  }
  
  public List<VariableType> getCustomPostVariableTypes() {
    return customPostVariableTypes;
  }

  public ProcessEngineConfigurationImpl setCustomPostVariableTypes(List<VariableType> customPostVariableTypes) {
    this.customPostVariableTypes = customPostVariableTypes;
    return this;
  }

  public List<BpmnParseHandler> getPreBpmnParseHandlers() {
    return preBpmnParseHandlers;
  }
  
  public ProcessEngineConfigurationImpl setPreBpmnParseHandlers(List<BpmnParseHandler> preBpmnParseHandlers) {
    this.preBpmnParseHandlers = preBpmnParseHandlers;
    return this;
  }
  
  public List<BpmnParseHandler> getCustomDefaultBpmnParseHandlers() {
    return customDefaultBpmnParseHandlers;
  }
  
  public ProcessEngineConfigurationImpl setCustomDefaultBpmnParseHandlers(List<BpmnParseHandler> customDefaultBpmnParseHandlers) {
    this.customDefaultBpmnParseHandlers = customDefaultBpmnParseHandlers;
    return this;
  }

  public List<BpmnParseHandler> getPostBpmnParseHandlers() {
    return postBpmnParseHandlers;
  }

  public ProcessEngineConfigurationImpl setPostBpmnParseHandlers(List<BpmnParseHandler> postBpmnParseHandlers) {
    this.postBpmnParseHandlers = postBpmnParseHandlers;
    return this;
  }

  public ActivityBehaviorFactory getActivityBehaviorFactory() {
    return activityBehaviorFactory;
  }
  
  public ProcessEngineConfigurationImpl setActivityBehaviorFactory(ActivityBehaviorFactory activityBehaviorFactory) {
    this.activityBehaviorFactory = activityBehaviorFactory;
    return this;
  }
  
  public ListenerFactory getListenerFactory() {
    return listenerFactory;
  }

  public ProcessEngineConfigurationImpl setListenerFactory(ListenerFactory listenerFactory) {
    this.listenerFactory = listenerFactory;
    return this;
  }
  
  public BpmnParseFactory getBpmnParseFactory() {
    return bpmnParseFactory;
  }
  
  public ProcessEngineConfigurationImpl setBpmnParseFactory(BpmnParseFactory bpmnParseFactory) {
    this.bpmnParseFactory = bpmnParseFactory;
    return this;
  }

  public Map<Object, Object> getBeans() {
    return beans;
  }

  public ProcessEngineConfigurationImpl setBeans(Map<Object, Object> beans) {
    this.beans = beans;
    return this;
  }
  
  public List<ResolverFactory> getResolverFactories() {
    return resolverFactories;
  }
  
  public ProcessEngineConfigurationImpl setResolverFactories(List<ResolverFactory> resolverFactories) {
    this.resolverFactories = resolverFactories;
    return this;
  }

  public DeploymentManager getDeploymentManager() {
    return deploymentManager;
  }
  
  public ProcessEngineConfigurationImpl setDeploymentManager(DeploymentManager deploymentManager) {
    this.deploymentManager = deploymentManager;
    return this;
  }
    
  public ProcessEngineConfigurationImpl setDelegateInterceptor(DelegateInterceptor delegateInterceptor) {
    this.delegateInterceptor = delegateInterceptor;
    return this;
  }
    
  public DelegateInterceptor getDelegateInterceptor() {
    return delegateInterceptor;
  }
    
  public EventHandler getEventHandler(String eventType) {
    return eventHandlers.get(eventType);
  }
  
  public ProcessEngineConfigurationImpl setEventHandlers(Map<String, EventHandler> eventHandlers) {
    this.eventHandlers = eventHandlers;
    return this;
  }
    
  public Map<String, EventHandler> getEventHandlers() {
    return eventHandlers;
  }
    
  public List<EventHandler> getCustomEventHandlers() {
    return customEventHandlers;
  }
    
  public ProcessEngineConfigurationImpl setCustomEventHandlers(List<EventHandler> customEventHandlers) {
    this.customEventHandlers = customEventHandlers;
    return this;
  }
  
  public FailedJobCommandFactory getFailedJobCommandFactory() {
    return failedJobCommandFactory;
  }
  
  public ProcessEngineConfigurationImpl setFailedJobCommandFactory(FailedJobCommandFactory failedJobCommandFactory) {
    this.failedJobCommandFactory = failedJobCommandFactory;
    return this;
  }

  public DataSource getIdGeneratorDataSource() {
    return idGeneratorDataSource;
  }
  
  public ProcessEngineConfigurationImpl setIdGeneratorDataSource(DataSource idGeneratorDataSource) {
    this.idGeneratorDataSource = idGeneratorDataSource;
    return this;
  }
  
  public String getIdGeneratorDataSourceJndiName() {
    return idGeneratorDataSourceJndiName;
  }

  public ProcessEngineConfigurationImpl setIdGeneratorDataSourceJndiName(String idGeneratorDataSourceJndiName) {
    this.idGeneratorDataSourceJndiName = idGeneratorDataSourceJndiName;
    return this;
  }

  public int getBatchSizeProcessInstances() {
    return batchSizeProcessInstances;
  }

  public ProcessEngineConfigurationImpl setBatchSizeProcessInstances(int batchSizeProcessInstances) {
    this.batchSizeProcessInstances = batchSizeProcessInstances;
    return this;
  }
  
  public int getBatchSizeTasks() {
    return batchSizeTasks;
  }
  
  public ProcessEngineConfigurationImpl setBatchSizeTasks(int batchSizeTasks) {
    this.batchSizeTasks = batchSizeTasks;
    return this;
  }
  
  public int getProcessDefinitionCacheLimit() {
    return processDefinitionCacheLimit;
  }

  public ProcessEngineConfigurationImpl setProcessDefinitionCacheLimit(int processDefinitionCacheLimit) {
    this.processDefinitionCacheLimit = processDefinitionCacheLimit;
    return this;
  }
  
  public DeploymentCache<ProcessDefinitionCacheEntry> getProcessDefinitionCache() {
    return processDefinitionCache;
  }
  
  public ProcessEngineConfigurationImpl setProcessDefinitionCache(DeploymentCache<ProcessDefinitionCacheEntry> processDefinitionCache) {
    this.processDefinitionCache = processDefinitionCache;
    return this;
  }

  public int getKnowledgeBaseCacheLimit() {
    return knowledgeBaseCacheLimit;
  }

  public ProcessEngineConfigurationImpl setKnowledgeBaseCacheLimit(int knowledgeBaseCacheLimit) {
    this.knowledgeBaseCacheLimit = knowledgeBaseCacheLimit;
    return this;
  }
  
  public DeploymentCache<Object> getKnowledgeBaseCache() {
    return knowledgeBaseCache;
  }
  
  public ProcessEngineConfigurationImpl setKnowledgeBaseCache(DeploymentCache<Object> knowledgeBaseCache) {
    this.knowledgeBaseCache = knowledgeBaseCache;
    return this;
  }

  public boolean isEnableSafeBpmnXml() {
    return enableSafeBpmnXml;
  }

  public ProcessEngineConfigurationImpl setEnableSafeBpmnXml(boolean enableSafeBpmnXml) {
    this.enableSafeBpmnXml = enableSafeBpmnXml;
    return this;
  }
  
  public ActivitiEventDispatcher getEventDispatcher() {
	  return eventDispatcher;
  }
  
  public void setEventDispatcher(ActivitiEventDispatcher eventDispatcher) {
	  this.eventDispatcher = eventDispatcher;
  }
  
  public void setEnableEventDispatcher(boolean enableEventDispatcher) {
	  this.enableEventDispatcher = enableEventDispatcher;
  }

	public ProcessValidator getProcessValidator() {
		return processValidator;
	}

	public void setProcessValidator(ProcessValidator processValidator) {
		this.processValidator = processValidator;
	}

	public boolean isEnableEventDispatcher() {
		return enableEventDispatcher;
	}

	public boolean isEnableDatabaseEventLogging() {
		return enableDatabaseEventLogging;
	}

	public ProcessEngineConfigurationImpl setEnableDatabaseEventLogging(boolean enableDatabaseEventLogging) {
		this.enableDatabaseEventLogging = enableDatabaseEventLogging;
    return this;
	}

  public int getMaxLengthStringVariableType() {
    return maxLengthStringVariableType;
  }

  public ProcessEngineConfigurationImpl setMaxLengthStringVariableType(int maxLengthStringVariableType) {
    this.maxLengthStringVariableType = maxLengthStringVariableType;
    return this;
  }
  
	public ProcessEngineConfigurationImpl setBulkInsertEnabled(boolean isBulkInsertEnabled) {
		this.isBulkInsertEnabled = isBulkInsertEnabled;
		return this;
	}

	public boolean isBulkInsertEnabled() {
		return isBulkInsertEnabled;
	}

	public int getMaxNrOfStatementsInBulkInsert() {
		return maxNrOfStatementsInBulkInsert;
	}

	public void setMaxNrOfStatementsInBulkInsert(int maxNrOfStatementsInBulkInsert) {
		this.maxNrOfStatementsInBulkInsert = maxNrOfStatementsInBulkInsert;
	}
	
	public DelegateExpressionFieldInjectionMode getDelegateExpressionFieldInjectionMode() {
    return delegateExpressionFieldInjectionMode;
  }

  public void setDelegateExpressionFieldInjectionMode(DelegateExpressionFieldInjectionMode delegateExpressionFieldInjectionMode) {
    this.delegateExpressionFieldInjectionMode = delegateExpressionFieldInjectionMode;
  }
	
	public ObjectMapper getObjectMapper() {
    return objectMapper;
  }
	
	public ProcessEngineConfigurationImpl setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    return this;
  }

  public Activiti5CompatibilityHandler getActiviti5CompatibilityHandler() {
    return activiti5CompatibilityHandler;
  }

  public ProcessEngineConfigurationImpl setActiviti5CompatibilityHandler(Activiti5CompatibilityHandler activiti5CompatibilityHandler) {
    this.activiti5CompatibilityHandler = activiti5CompatibilityHandler;
    return this;
  }

  public long getAsyncExecutorThreadKeepAliveTime() {
    return asyncExecutorThreadKeepAliveTime;
  }

  public ProcessEngineConfigurationImpl setAsyncExecutorThreadKeepAliveTime(long asyncExecutorThreadKeepAliveTime) {
    this.asyncExecutorThreadKeepAliveTime = asyncExecutorThreadKeepAliveTime;
    return this;
  }

  public int getAsyncExecutorThreadPoolQueueSize() {
    return asyncExecutorThreadPoolQueueSize;
  }

  public ProcessEngineConfigurationImpl setAsyncExecutorThreadPoolQueueSize(int asyncExecutorThreadPoolQueueSize) {
    this.asyncExecutorThreadPoolQueueSize = asyncExecutorThreadPoolQueueSize;
    return this;
  }

  public BlockingQueue<Runnable> getAsyncExecutorThreadPoolQueue() {
    return asyncExecutorThreadPoolQueue;
  }

  public ProcessEngineConfigurationImpl setAsyncExecutorThreadPoolQueue(BlockingQueue<Runnable> asyncExecutorThreadPoolQueue) {
    this.asyncExecutorThreadPoolQueue = asyncExecutorThreadPoolQueue;
    return this;
  }

  public long getAsyncExecutorSecondsToWaitOnShutdown() {
    return asyncExecutorSecondsToWaitOnShutdown;
  }

  public ProcessEngineConfigurationImpl setAsyncExecutorSecondsToWaitOnShutdown(long asyncExecutorSecondsToWaitOnShutdown) {
    this.asyncExecutorSecondsToWaitOnShutdown = asyncExecutorSecondsToWaitOnShutdown;
    return this;
  }

  public int getAsyncExecutorMaxTimerJobsPerAcquisition() {
    return asyncExecutorMaxTimerJobsPerAcquisition;
  }

  public ProcessEngineConfigurationImpl setAsyncExecutorMaxTimerJobsPerAcquisition(int asyncExecutorMaxTimerJobsPerAcquisition) {
    this.asyncExecutorMaxTimerJobsPerAcquisition = asyncExecutorMaxTimerJobsPerAcquisition;
    return this;
  }

  public int getAsyncExecutorMaxAsyncJobsDuePerAcquisition() {
    return asyncExecutorMaxAsyncJobsDuePerAcquisition;
  }

  public ProcessEngineConfigurationImpl setAsyncExecutorMaxAsyncJobsDuePerAcquisition(int asyncExecutorMaxAsyncJobsDuePerAcquisition) {
    this.asyncExecutorMaxAsyncJobsDuePerAcquisition = asyncExecutorMaxAsyncJobsDuePerAcquisition;
    return this;
  }

  public int getAsyncExecutorDefaultTimerJobAcquireWaitTime() {
    return asyncExecutorDefaultTimerJobAcquireWaitTime;
  }

  public ProcessEngineConfigurationImpl setAsyncExecutorDefaultTimerJobAcquireWaitTime(int asyncExecutorTimerJobAcquireWaitTime) {
    this.asyncExecutorDefaultTimerJobAcquireWaitTime = asyncExecutorTimerJobAcquireWaitTime;
    return this;
  }

  public int getAsyncExecutorDefaultAsyncJobAcquireWaitTime() {
    return asyncExecutorDefaultAsyncJobAcquireWaitTime;
  }

  public ProcessEngineConfigurationImpl setAsyncExecutorDefaultAsyncJobAcquireWaitTime(int asyncExecutorDefaultAsyncJobAcquireWaitTime) {
    this.asyncExecutorDefaultAsyncJobAcquireWaitTime = asyncExecutorDefaultAsyncJobAcquireWaitTime;
    return this;
  }

  public int getAsyncExecutorDefaultQueueSizeFullWaitTime() {
    return asyncExecutorDefaultQueueSizeFullWaitTime;
  }

  public ProcessEngineConfigurationImpl setAsyncExecutorDefaultQueueSizeFullWaitTime(int asyncExecutorDefaultQueueSizeFullWaitTime) {
    this.asyncExecutorDefaultQueueSizeFullWaitTime = asyncExecutorDefaultQueueSizeFullWaitTime;
    return this;
  }

  public String getAsyncExecutorLockOwner() {
    return asyncExecutorLockOwner;
  }

  public ProcessEngineConfigurationImpl setAsyncExecutorLockOwner(String asyncExecutorLockOwner) {
    this.asyncExecutorLockOwner = asyncExecutorLockOwner;
    return this;
  }

  public int getAsyncExecutorTimerLockTimeInMillis() {
    return asyncExecutorTimerLockTimeInMillis;
  }

  public ProcessEngineConfigurationImpl setAsyncExecutorTimerLockTimeInMillis(int asyncExecutorTimerLockTimeInMillis) {
    this.asyncExecutorTimerLockTimeInMillis = asyncExecutorTimerLockTimeInMillis;
    return this;
  }

  public int getAsyncExecutorAsyncJobLockTimeInMillis() {
    return asyncExecutorAsyncJobLockTimeInMillis;
  }

  public ProcessEngineConfigurationImpl setAsyncExecutorAsyncJobLockTimeInMillis(int asyncExecutorAsyncJobLockTimeInMillis) {
    this.asyncExecutorAsyncJobLockTimeInMillis = asyncExecutorAsyncJobLockTimeInMillis;
    return this;
  }

  public int getAsyncExecutorLockRetryWaitTimeInMillis() {
    return asyncExecutorLockRetryWaitTimeInMillis;
  }

  public ProcessEngineConfigurationImpl setAsyncExecutorLockRetryWaitTimeInMillis(int asyncExecutorLockRetryWaitTimeInMillis) {
    this.asyncExecutorLockRetryWaitTimeInMillis = asyncExecutorLockRetryWaitTimeInMillis;
    return this;
  }
  
}

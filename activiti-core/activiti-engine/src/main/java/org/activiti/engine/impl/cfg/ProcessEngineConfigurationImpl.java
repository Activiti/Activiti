/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import java.math.BigDecimal;
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
import java.util.Map.Entry;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.xml.namespace.QName;

import jakarta.el.ELResolver;
import org.activiti.api.runtime.shared.identity.UserGroupManager;
import org.activiti.core.el.CustomFunctionProvider;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.DynamicBpmnService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.cfg.ProcessEngineConfigurator;
import org.activiti.engine.delegate.event.ActivitiEventDispatcher;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventDispatcherImpl;
import org.activiti.engine.impl.DynamicBpmnServiceImpl;
import org.activiti.engine.impl.HistoryServiceImpl;
import org.activiti.engine.impl.ManagementServiceImpl;
import org.activiti.engine.impl.ProcessDefinitionHelper;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.RuntimeServiceImpl;
import org.activiti.engine.impl.ServiceImpl;
import org.activiti.engine.impl.TaskServiceImpl;
import org.activiti.engine.impl.agenda.DefaultActivitiEngineAgendaFactory;
import org.activiti.engine.impl.asyncexecutor.AsyncExecutor;
import org.activiti.engine.impl.asyncexecutor.DefaultAsyncJobExecutor;
import org.activiti.engine.impl.asyncexecutor.DefaultJobManager;
import org.activiti.engine.impl.asyncexecutor.ExecuteAsyncRunnableFactory;
import org.activiti.engine.impl.asyncexecutor.JobManager;
import org.activiti.engine.impl.bpmn.data.ItemInstance;
import org.activiti.engine.impl.bpmn.deployer.BpmnDeployer;
import org.activiti.engine.impl.bpmn.deployer.BpmnDeploymentHelper;
import org.activiti.engine.impl.bpmn.deployer.CachingAndArtifactsManager;
import org.activiti.engine.impl.bpmn.deployer.EventSubscriptionManager;
import org.activiti.engine.impl.bpmn.deployer.ParsedDeploymentBuilderFactory;
import org.activiti.engine.impl.bpmn.deployer.TimerManager;
import org.activiti.engine.impl.bpmn.listener.ListenerNotificationHelper;
import org.activiti.engine.impl.bpmn.parser.BpmnParseHandlers;
import org.activiti.engine.impl.bpmn.parser.BpmnParser;
import org.activiti.engine.impl.bpmn.parser.factory.AbstractBehaviorFactory;
import org.activiti.engine.impl.bpmn.parser.factory.ActivityBehaviorFactory;
import org.activiti.engine.impl.bpmn.parser.factory.DefaultActivityBehaviorFactory;
import org.activiti.engine.impl.bpmn.parser.factory.DefaultListenerFactory;
import org.activiti.engine.impl.bpmn.parser.factory.ListenerFactory;
import org.activiti.engine.impl.bpmn.parser.handler.AdhocSubProcessParseHandler;
import org.activiti.engine.impl.bpmn.parser.handler.BoundaryEventParseHandler;
import org.activiti.engine.impl.bpmn.parser.handler.BusinessRuleParseHandler;
import org.activiti.engine.impl.bpmn.parser.handler.CallActivityParseHandler;
import org.activiti.engine.impl.bpmn.parser.handler.CancelEventDefinitionParseHandler;
import org.activiti.engine.impl.bpmn.parser.handler.CompensateEventDefinitionParseHandler;
import org.activiti.engine.impl.bpmn.parser.handler.EndEventParseHandler;
import org.activiti.engine.impl.bpmn.parser.handler.ErrorEventDefinitionParseHandler;
import org.activiti.engine.impl.bpmn.parser.handler.EventBasedGatewayParseHandler;
import org.activiti.engine.impl.bpmn.parser.handler.EventSubProcessParseHandler;
import org.activiti.engine.impl.bpmn.parser.handler.ExclusiveGatewayParseHandler;
import org.activiti.engine.impl.bpmn.parser.handler.InclusiveGatewayParseHandler;
import org.activiti.engine.impl.bpmn.parser.handler.IntermediateCatchEventParseHandler;
import org.activiti.engine.impl.bpmn.parser.handler.IntermediateThrowEventParseHandler;
import org.activiti.engine.impl.bpmn.parser.handler.LinkEventDefinitionParseHandler;
import org.activiti.engine.impl.bpmn.parser.handler.ManualTaskParseHandler;
import org.activiti.engine.impl.bpmn.parser.handler.MessageEventDefinitionParseHandler;
import org.activiti.engine.impl.bpmn.parser.handler.ParallelGatewayParseHandler;
import org.activiti.engine.impl.bpmn.parser.handler.ProcessParseHandler;
import org.activiti.engine.impl.bpmn.parser.handler.ReceiveTaskParseHandler;
import org.activiti.engine.impl.bpmn.parser.handler.ScriptTaskParseHandler;
import org.activiti.engine.impl.bpmn.parser.handler.SendTaskParseHandler;
import org.activiti.engine.impl.bpmn.parser.handler.SequenceFlowParseHandler;
import org.activiti.engine.impl.bpmn.parser.handler.ServiceTaskParseHandler;
import org.activiti.engine.impl.bpmn.parser.handler.SignalEventDefinitionParseHandler;
import org.activiti.engine.impl.bpmn.parser.handler.StartEventParseHandler;
import org.activiti.engine.impl.bpmn.parser.handler.SubProcessParseHandler;
import org.activiti.engine.impl.bpmn.parser.handler.TaskParseHandler;
import org.activiti.engine.impl.bpmn.parser.handler.TimerEventDefinitionParseHandler;
import org.activiti.engine.impl.bpmn.parser.handler.TransactionParseHandler;
import org.activiti.engine.impl.bpmn.parser.handler.UserTaskParseHandler;
import org.activiti.engine.impl.bpmn.webservice.MessageInstance;
import org.activiti.engine.impl.calendar.BusinessCalendarManager;
import org.activiti.engine.impl.calendar.CycleBusinessCalendar;
import org.activiti.engine.impl.calendar.DueDateBusinessCalendar;
import org.activiti.engine.impl.calendar.DurationBusinessCalendar;
import org.activiti.engine.impl.calendar.MapBusinessCalendarManager;
import org.activiti.engine.impl.cfg.standalone.StandaloneMybatisTransactionContextFactory;
import org.activiti.engine.impl.cmd.ValidateExecutionRelatedEntityCountCfgCmd;
import org.activiti.engine.impl.db.DbIdGenerator;
import org.activiti.engine.impl.db.DbSqlSessionFactory;
import org.activiti.engine.impl.db.IbatisVariableTypeHandler;
import org.activiti.engine.impl.delegate.invocation.DefaultDelegateInterceptor;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.event.CompensationEventHandler;
import org.activiti.engine.impl.event.EventHandler;
import org.activiti.engine.impl.event.EventSubscriptionPayloadMappingProvider;
import org.activiti.engine.impl.event.MessageEventHandler;
import org.activiti.engine.impl.event.SignalEventHandler;
import org.activiti.engine.impl.event.logger.EventLogger;
import org.activiti.engine.impl.history.DefaultHistoryManager;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.history.HistoryManager;
import org.activiti.engine.impl.interceptor.CommandConfig;
import org.activiti.engine.impl.interceptor.CommandContextFactory;
import org.activiti.engine.impl.interceptor.CommandContextInterceptor;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.interceptor.CommandInterceptor;
import org.activiti.engine.impl.interceptor.CommandInvoker;
import org.activiti.engine.impl.interceptor.DebugCommandInvoker;
import org.activiti.engine.impl.interceptor.DelegateInterceptor;
import org.activiti.engine.impl.interceptor.LogInterceptor;
import org.activiti.engine.impl.interceptor.SessionFactory;
import org.activiti.engine.impl.interceptor.TransactionContextInterceptor;
import org.activiti.engine.impl.jobexecutor.AsyncContinuationJobHandler;
import org.activiti.engine.impl.jobexecutor.DefaultFailedJobCommandFactory;
import org.activiti.engine.impl.jobexecutor.FailedJobCommandFactory;
import org.activiti.engine.impl.jobexecutor.JobHandler;
import org.activiti.engine.impl.jobexecutor.ProcessEventJobHandler;
import org.activiti.engine.impl.jobexecutor.TimerActivateProcessDefinitionHandler;
import org.activiti.engine.impl.jobexecutor.TimerStartEventJobHandler;
import org.activiti.engine.impl.jobexecutor.TimerSuspendProcessDefinitionHandler;
import org.activiti.engine.impl.jobexecutor.TriggerTimerEventJobHandler;
import org.activiti.engine.impl.persistence.GenericManagerFactory;
import org.activiti.engine.impl.persistence.cache.EntityCache;
import org.activiti.engine.impl.persistence.cache.EntityCacheImpl;
import org.activiti.engine.impl.persistence.deploy.DefaultDeploymentCache;
import org.activiti.engine.impl.persistence.deploy.Deployer;
import org.activiti.engine.impl.persistence.deploy.DeploymentCache;
import org.activiti.engine.impl.persistence.deploy.DeploymentManager;
import org.activiti.engine.impl.persistence.deploy.ProcessDefinitionCacheEntry;
import org.activiti.engine.impl.persistence.deploy.ProcessDefinitionInfoCache;
import org.activiti.engine.impl.persistence.entity.AttachmentEntityManager;
import org.activiti.engine.impl.persistence.entity.AttachmentEntityManagerImpl;
import org.activiti.engine.impl.persistence.entity.ByteArrayEntityManager;
import org.activiti.engine.impl.persistence.entity.ByteArrayEntityManagerImpl;
import org.activiti.engine.impl.persistence.entity.CommentEntityManager;
import org.activiti.engine.impl.persistence.entity.CommentEntityManagerImpl;
import org.activiti.engine.impl.persistence.entity.DeadLetterJobEntityManager;
import org.activiti.engine.impl.persistence.entity.DeadLetterJobEntityManagerImpl;
import org.activiti.engine.impl.persistence.entity.DeploymentEntityManager;
import org.activiti.engine.impl.persistence.entity.DeploymentEntityManagerImpl;
import org.activiti.engine.impl.persistence.entity.EventLogEntryEntityManager;
import org.activiti.engine.impl.persistence.entity.EventLogEntryEntityManagerImpl;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntityManager;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntityManagerImpl;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManagerImpl;
import org.activiti.engine.impl.persistence.entity.HistoricActivityInstanceEntityManager;
import org.activiti.engine.impl.persistence.entity.HistoricActivityInstanceEntityManagerImpl;
import org.activiti.engine.impl.persistence.entity.HistoricDetailEntityManager;
import org.activiti.engine.impl.persistence.entity.HistoricDetailEntityManagerImpl;
import org.activiti.engine.impl.persistence.entity.HistoricIdentityLinkEntityManager;
import org.activiti.engine.impl.persistence.entity.HistoricIdentityLinkEntityManagerImpl;
import org.activiti.engine.impl.persistence.entity.HistoricProcessInstanceEntityManager;
import org.activiti.engine.impl.persistence.entity.HistoricProcessInstanceEntityManagerImpl;
import org.activiti.engine.impl.persistence.entity.HistoricTaskInstanceEntityManager;
import org.activiti.engine.impl.persistence.entity.HistoricTaskInstanceEntityManagerImpl;
import org.activiti.engine.impl.persistence.entity.HistoricVariableInstanceEntityManager;
import org.activiti.engine.impl.persistence.entity.HistoricVariableInstanceEntityManagerImpl;
import org.activiti.engine.impl.persistence.entity.IdentityLinkEntityManager;
import org.activiti.engine.impl.persistence.entity.IdentityLinkEntityManagerImpl;
import org.activiti.engine.impl.persistence.entity.JobEntityManager;
import org.activiti.engine.impl.persistence.entity.JobEntityManagerImpl;
import org.activiti.engine.impl.persistence.entity.ModelEntityManager;
import org.activiti.engine.impl.persistence.entity.ModelEntityManagerImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntityManager;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntityManagerImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionInfoEntityManager;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionInfoEntityManagerImpl;
import org.activiti.engine.impl.persistence.entity.PropertyEntityManager;
import org.activiti.engine.impl.persistence.entity.PropertyEntityManagerImpl;
import org.activiti.engine.impl.persistence.entity.ResourceEntityManager;
import org.activiti.engine.impl.persistence.entity.ResourceEntityManagerImpl;
import org.activiti.engine.impl.persistence.entity.SuspendedJobEntityManager;
import org.activiti.engine.impl.persistence.entity.SuspendedJobEntityManagerImpl;
import org.activiti.engine.impl.persistence.entity.TableDataManager;
import org.activiti.engine.impl.persistence.entity.TableDataManagerImpl;
import org.activiti.engine.impl.persistence.entity.TaskEntityManager;
import org.activiti.engine.impl.persistence.entity.TaskEntityManagerImpl;
import org.activiti.engine.impl.persistence.entity.TimerJobEntityManager;
import org.activiti.engine.impl.persistence.entity.TimerJobEntityManagerImpl;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntityManager;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntityManagerImpl;
import org.activiti.engine.impl.persistence.entity.data.AttachmentDataManager;
import org.activiti.engine.impl.persistence.entity.data.ByteArrayDataManager;
import org.activiti.engine.impl.persistence.entity.data.CommentDataManager;
import org.activiti.engine.impl.persistence.entity.data.DeadLetterJobDataManager;
import org.activiti.engine.impl.persistence.entity.data.DeploymentDataManager;
import org.activiti.engine.impl.persistence.entity.data.EventLogEntryDataManager;
import org.activiti.engine.impl.persistence.entity.data.EventSubscriptionDataManager;
import org.activiti.engine.impl.persistence.entity.data.ExecutionDataManager;
import org.activiti.engine.impl.persistence.entity.data.HistoricActivityInstanceDataManager;
import org.activiti.engine.impl.persistence.entity.data.HistoricDetailDataManager;
import org.activiti.engine.impl.persistence.entity.data.HistoricIdentityLinkDataManager;
import org.activiti.engine.impl.persistence.entity.data.HistoricProcessInstanceDataManager;
import org.activiti.engine.impl.persistence.entity.data.HistoricTaskInstanceDataManager;
import org.activiti.engine.impl.persistence.entity.data.HistoricVariableInstanceDataManager;
import org.activiti.engine.impl.persistence.entity.data.IdentityLinkDataManager;
import org.activiti.engine.impl.persistence.entity.data.JobDataManager;
import org.activiti.engine.impl.persistence.entity.data.ModelDataManager;
import org.activiti.engine.impl.persistence.entity.data.ProcessDefinitionDataManager;
import org.activiti.engine.impl.persistence.entity.data.ProcessDefinitionInfoDataManager;
import org.activiti.engine.impl.persistence.entity.data.PropertyDataManager;
import org.activiti.engine.impl.persistence.entity.data.ResourceDataManager;
import org.activiti.engine.impl.persistence.entity.data.SuspendedJobDataManager;
import org.activiti.engine.impl.persistence.entity.data.TaskDataManager;
import org.activiti.engine.impl.persistence.entity.data.TimerJobDataManager;
import org.activiti.engine.impl.persistence.entity.data.VariableInstanceDataManager;
import org.activiti.engine.impl.persistence.entity.data.impl.MybatisAttachmentDataManager;
import org.activiti.engine.impl.persistence.entity.data.impl.MybatisByteArrayDataManager;
import org.activiti.engine.impl.persistence.entity.data.impl.MybatisCommentDataManager;
import org.activiti.engine.impl.persistence.entity.data.impl.MybatisDeadLetterJobDataManager;
import org.activiti.engine.impl.persistence.entity.data.impl.MybatisDeploymentDataManager;
import org.activiti.engine.impl.persistence.entity.data.impl.MybatisEventLogEntryDataManager;
import org.activiti.engine.impl.persistence.entity.data.impl.MybatisEventSubscriptionDataManager;
import org.activiti.engine.impl.persistence.entity.data.impl.MybatisExecutionDataManager;
import org.activiti.engine.impl.persistence.entity.data.impl.MybatisHistoricActivityInstanceDataManager;
import org.activiti.engine.impl.persistence.entity.data.impl.MybatisHistoricDetailDataManager;
import org.activiti.engine.impl.persistence.entity.data.impl.MybatisHistoricIdentityLinkDataManager;
import org.activiti.engine.impl.persistence.entity.data.impl.MybatisHistoricProcessInstanceDataManager;
import org.activiti.engine.impl.persistence.entity.data.impl.MybatisHistoricTaskInstanceDataManager;
import org.activiti.engine.impl.persistence.entity.data.impl.MybatisHistoricVariableInstanceDataManager;
import org.activiti.engine.impl.persistence.entity.data.impl.MybatisIdentityLinkDataManager;
import org.activiti.engine.impl.persistence.entity.data.impl.MybatisJobDataManager;
import org.activiti.engine.impl.persistence.entity.data.impl.MybatisModelDataManager;
import org.activiti.engine.impl.persistence.entity.data.impl.MybatisProcessDefinitionDataManager;
import org.activiti.engine.impl.persistence.entity.data.impl.MybatisProcessDefinitionInfoDataManager;
import org.activiti.engine.impl.persistence.entity.data.impl.MybatisPropertyDataManager;
import org.activiti.engine.impl.persistence.entity.data.impl.MybatisResourceDataManager;
import org.activiti.engine.impl.persistence.entity.data.impl.MybatisSuspendedJobDataManager;
import org.activiti.engine.impl.persistence.entity.data.impl.MybatisTaskDataManager;
import org.activiti.engine.impl.persistence.entity.data.impl.MybatisTimerJobDataManager;
import org.activiti.engine.impl.persistence.entity.data.impl.MybatisVariableInstanceDataManager;
import org.activiti.engine.impl.persistence.entity.data.integration.IntegrationContextDataManager;
import org.activiti.engine.impl.persistence.entity.data.integration.MybatisIntegrationContextDataManager;
import org.activiti.engine.impl.persistence.entity.integration.IntegrationContextManager;
import org.activiti.engine.impl.persistence.entity.integration.IntegrationContextManagerImpl;
import org.activiti.engine.impl.scripting.BeansResolverFactory;
import org.activiti.engine.impl.scripting.ResolverFactory;
import org.activiti.engine.impl.scripting.ScriptBindingsFactory;
import org.activiti.engine.impl.scripting.ScriptingEngines;
import org.activiti.engine.impl.scripting.VariableScopeResolverFactory;
import org.activiti.engine.impl.util.DefaultClockImpl;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.engine.impl.util.ProcessInstanceHelper;
import org.activiti.engine.impl.util.ReflectUtil;
import org.activiti.engine.impl.variable.BigDecimalType;
import org.activiti.engine.impl.variable.BooleanType;
import org.activiti.engine.impl.variable.ByteArrayType;
import org.activiti.engine.impl.variable.CustomObjectType;
import org.activiti.engine.impl.variable.DateType;
import org.activiti.engine.impl.variable.DefaultVariableTypes;
import org.activiti.engine.impl.variable.DoubleType;
import org.activiti.engine.impl.variable.EntityManagerSession;
import org.activiti.engine.impl.variable.EntityManagerSessionFactory;
import org.activiti.engine.impl.variable.IntegerType;
import org.activiti.engine.impl.variable.JPAEntityListVariableType;
import org.activiti.engine.impl.variable.JPAEntityVariableType;
import org.activiti.engine.impl.variable.JodaDateTimeType;
import org.activiti.engine.impl.variable.JodaDateType;
import org.activiti.engine.impl.variable.JsonType;
import org.activiti.engine.impl.variable.JsonTypeConverter;
import org.activiti.engine.impl.variable.LocalDateTimeType;
import org.activiti.engine.impl.variable.LocalDateType;
import org.activiti.engine.impl.variable.LongJsonType;
import org.activiti.engine.impl.variable.LongStringType;
import org.activiti.engine.impl.variable.LongType;
import org.activiti.engine.impl.variable.NullType;
import org.activiti.engine.impl.variable.SerializableType;
import org.activiti.engine.impl.variable.ShortType;
import org.activiti.engine.impl.variable.StringType;
import org.activiti.engine.impl.variable.UUIDType;
import org.activiti.engine.impl.variable.VariableType;
import org.activiti.engine.impl.variable.VariableTypes;
import org.activiti.engine.integration.IntegrationContextService;
import org.activiti.engine.integration.IntegrationContextServiceImpl;
import org.activiti.engine.parse.BpmnParseHandler;
import org.activiti.engine.runtime.Clock;
import org.activiti.validation.ProcessValidator;
import org.activiti.validation.ProcessValidatorFactory;
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

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;


public abstract class ProcessEngineConfigurationImpl extends ProcessEngineConfiguration {

  private static Logger log = LoggerFactory.getLogger(ProcessEngineConfigurationImpl.class);

  public static final String DB_SCHEMA_UPDATE_CREATE = "create";
  public static final String DB_SCHEMA_UPDATE_DROP_CREATE = "drop-create";

  public static final String DEFAULT_WS_SYNC_FACTORY = "org.activiti.engine.impl.webservice.CxfWebServiceClientFactory";

  public static final String DEFAULT_MYBATIS_MAPPING_FILE = "org/activiti/db/mapping/mappings.xml";

  public static final int DEFAULT_GENERIC_MAX_LENGTH_STRING= 4000;
  public static final int DEFAULT_ORACLE_MAX_LENGTH_STRING= 2000;

  // SERVICES /////////////////////////////////////////////////////////////////

  protected RepositoryService repositoryService = new RepositoryServiceImpl();
  protected RuntimeService runtimeService = new RuntimeServiceImpl();
  protected HistoryService historyService = new HistoryServiceImpl(this);
  protected TaskService taskService = new TaskServiceImpl(this);
  protected ManagementService managementService = new ManagementServiceImpl();
  protected DynamicBpmnService dynamicBpmnService = new DynamicBpmnServiceImpl(this);
  protected UserGroupManager userGroupManager;
  private IntegrationContextService integrationContextService;

  // COMMAND EXECUTORS ////////////////////////////////////////////////////////

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

  // DATA MANAGERS /////////////////////////////////////////////////////////////

  protected AttachmentDataManager attachmentDataManager;
  protected ByteArrayDataManager byteArrayDataManager;
  protected CommentDataManager commentDataManager;
  protected DeploymentDataManager deploymentDataManager;
  protected EventLogEntryDataManager eventLogEntryDataManager;
  protected EventSubscriptionDataManager eventSubscriptionDataManager;
  protected ExecutionDataManager executionDataManager;
  protected HistoricActivityInstanceDataManager historicActivityInstanceDataManager;
  protected HistoricDetailDataManager historicDetailDataManager;
  protected HistoricIdentityLinkDataManager historicIdentityLinkDataManager;
  protected HistoricProcessInstanceDataManager historicProcessInstanceDataManager;
  protected HistoricTaskInstanceDataManager historicTaskInstanceDataManager;
  protected HistoricVariableInstanceDataManager historicVariableInstanceDataManager;
  protected IdentityLinkDataManager identityLinkDataManager;
  protected JobDataManager jobDataManager;
  protected TimerJobDataManager timerJobDataManager;
  protected SuspendedJobDataManager suspendedJobDataManager;
  protected DeadLetterJobDataManager deadLetterJobDataManager;
  protected ModelDataManager modelDataManager;
  protected ProcessDefinitionDataManager processDefinitionDataManager;
  protected ProcessDefinitionInfoDataManager processDefinitionInfoDataManager;
  protected PropertyDataManager propertyDataManager;
  protected ResourceDataManager resourceDataManager;
  protected TaskDataManager taskDataManager;
  protected VariableInstanceDataManager variableInstanceDataManager;
  private IntegrationContextDataManager integrationContextDataManager;


  // ENTITY MANAGERS ///////////////////////////////////////////////////////////

  protected AttachmentEntityManager attachmentEntityManager;
  protected ByteArrayEntityManager byteArrayEntityManager;
  protected CommentEntityManager commentEntityManager;
  protected DeploymentEntityManager deploymentEntityManager;
  protected EventLogEntryEntityManager eventLogEntryEntityManager;
  protected EventSubscriptionEntityManager eventSubscriptionEntityManager;
  protected ExecutionEntityManager executionEntityManager;
  protected HistoricActivityInstanceEntityManager historicActivityInstanceEntityManager;
  protected HistoricDetailEntityManager historicDetailEntityManager;
  protected HistoricIdentityLinkEntityManager historicIdentityLinkEntityManager;
  protected HistoricProcessInstanceEntityManager historicProcessInstanceEntityManager;
  protected HistoricTaskInstanceEntityManager historicTaskInstanceEntityManager;
  protected HistoricVariableInstanceEntityManager historicVariableInstanceEntityManager;
  protected IdentityLinkEntityManager identityLinkEntityManager;
  protected JobEntityManager jobEntityManager;
  protected TimerJobEntityManager timerJobEntityManager;
  protected SuspendedJobEntityManager suspendedJobEntityManager;
  protected DeadLetterJobEntityManager deadLetterJobEntityManager;
  protected ModelEntityManager modelEntityManager;
  protected ProcessDefinitionEntityManager processDefinitionEntityManager;
  protected ProcessDefinitionInfoEntityManager processDefinitionInfoEntityManager;
  protected PropertyEntityManager propertyEntityManager;
  protected ResourceEntityManager resourceEntityManager;
  protected TableDataManager tableDataManager;
  protected TaskEntityManager taskEntityManager;
  protected VariableInstanceEntityManager variableInstanceEntityManager;
  private IntegrationContextManager integrationContextManager;
  private EventSubscriptionPayloadMappingProvider eventSubscriptionPayloadMappingProvider =
                                                              new EventSubscriptionPayloadMappingProvider() {};
  // History Manager

  protected HistoryManager historyManager;

  // Job Manager

  protected JobManager jobManager;

  // SESSION FACTORIES /////////////////////////////////////////////////////////

  protected List<SessionFactory> customSessionFactories;
  protected DbSqlSessionFactory dbSqlSessionFactory;
  protected Map<Class<?>, SessionFactory> sessionFactories;

  // CONFIGURATORS ////////////////////////////////////////////////////////////

  protected boolean enableConfiguratorServiceLoader = true; // Enabled by default. In certain environments this should be set to false (eg osgi)
  protected List<ProcessEngineConfigurator> configurators; // The injected configurators
  protected List<ProcessEngineConfigurator> allConfigurators; // Including auto-discovered configurators

  // DEPLOYERS //////////////////////////////////////////////////////////////////

  protected BpmnDeployer bpmnDeployer;
  protected BpmnParser bpmnParser;
  protected ParsedDeploymentBuilderFactory parsedDeploymentBuilderFactory;
  protected TimerManager timerManager;
  protected EventSubscriptionManager eventSubscriptionManager;
  protected BpmnDeploymentHelper bpmnDeploymentHelper;
  protected CachingAndArtifactsManager cachingAndArtifactsManager;
  protected List<Deployer> customPreDeployers;
  protected List<Deployer> customPostDeployers;
  protected List<Deployer> deployers;
  protected DeploymentManager deploymentManager;

  protected int processDefinitionCacheLimit = -1; // By default, no limit
  protected DeploymentCache<ProcessDefinitionCacheEntry> processDefinitionCache;

  protected int processDefinitionInfoCacheLimit = -1; // By default, no limit
  protected ProcessDefinitionInfoCache processDefinitionInfoCache;

  protected int knowledgeBaseCacheLimit = -1;
  protected DeploymentCache<Object> knowledgeBaseCache;

  // JOB EXECUTOR /////////////////////////////////////////////////////////////

  protected List<JobHandler> customJobHandlers;
  protected Map<String, JobHandler> jobHandlers;

  // HELPERS //////////////////////////////////////////////////////////////////
  protected ProcessInstanceHelper processInstanceHelper;
  protected ListenerNotificationHelper listenerNotificationHelper;

  // ASYNC EXECUTOR ///////////////////////////////////////////////////////////

  /**
   * The number of retries for a job.
   */
  protected int asyncExecutorNumberOfRetries = 3;

  /**
   * The minimal number of threads that are kept alive in the threadpool for job
   * execution. Default value = 2. (This property is only applicable when using
   * the {@link DefaultAsyncJobExecutor}).
   */
  protected int asyncExecutorCorePoolSize = 2;

  /**
   * The maximum number of threads that are created in the threadpool for job
   * execution. Default value = 10. (This property is only applicable when using
   * the {@link DefaultAsyncJobExecutor}).
   */
  protected int asyncExecutorMaxPoolSize = 10;

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
   * The amount of time (in milliseconds) that is between two consecutive checks
   * of 'expired jobs'. Expired jobs are jobs that were locked (a lock owner + time
   * was written by some executor, but the job was never completed).
   *
   * During such a check, jobs that are expired are again made available,
   * meaning the lock owner and lock time will be removed. Other executors
   * will now be able to pick it up.
   *
   * A job is deemed expired if the lock time is before the current date.
   *
   * By default one minute.
   */
  protected int asyncExecutorResetExpiredJobsInterval = 60 * 1000;

  /**
   * The {@link AsyncExecutor} has a 'cleanup' thread that resets expired jobs
   * so they can be re-acquired by other executors. This setting defines the size
   * of the page being used when fetching these expired jobs.
   */
  protected int asyncExecutorResetExpiredJobsPageSize = 3;

  /**
   * Experimental!
   *
   * Set this to true when using the message queue based job executor.
   */
  protected boolean asyncExecutorMessageQueueMode;

 /**
  * Allows to define a custom factory for creating the {@link Runnable} that is executed by the async executor.
  *
  * (This property is only applicable when using the {@link DefaultAsyncJobExecutor}).
  */
  protected ExecuteAsyncRunnableFactory asyncExecutorExecuteAsyncRunnableFactory;

  // MYBATIS SQL SESSION FACTORY //////////////////////////////////////////////

  protected SqlSessionFactory sqlSessionFactory;
  protected TransactionFactory transactionFactory;

  protected Set<Class<?>> customMybatisMappers;
  protected Set<String> customMybatisXMLMappers;

  // ID GENERATOR ///////////////////////////////////////////////////////////////

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

  // PROCESS VALIDATION //////////////////////////////////////////////////////////////

  protected ProcessValidator processValidator;

  // OTHER //////////////////////////////////////////////////////////////////////

  protected List<VariableType> customPreVariableTypes;
  protected List<VariableType> customPostVariableTypes;
  protected VariableTypes variableTypes;

  /**
   * This flag determines whether variables of the type 'serializable' will be tracked.
   * This means that, when true, in a JavaDelegate you can write
   *
   * MySerializableVariable myVariable = (MySerializableVariable) execution.getVariable("myVariable");
   * myVariable.setNumber(123);
   *
   * And the changes to the java object will be reflected in the database.
   * Otherwise, a manual call to setVariable will be needed.
   *
   * By default true for backwards compatibility.
   */
  protected boolean serializableVariableTypeTrackDeserializedObjects = true;

  protected boolean serializePOJOsInVariablesToJson = false;
  protected String javaClassFieldForJackson = JsonTypeInfo.Id.CLASS.getDefaultPropertyName();

  protected ExpressionManager expressionManager;
  protected List<String> customScriptingEngineClasses;
  protected ScriptingEngines scriptingEngines;
  protected List<ResolverFactory> resolverFactories;
  protected List<CustomFunctionProvider> customFunctionProviders;
  private List<ELResolver> customELResolvers;

  protected BusinessCalendarManager businessCalendarManager;

  protected int executionQueryLimit = 20000;
  protected int taskQueryLimit = 20000;
  protected int historicTaskQueryLimit = 20000;
  protected int historicProcessInstancesQueryLimit = 20000;

  protected String wsSyncFactoryClassName = DEFAULT_WS_SYNC_FACTORY;
  protected ConcurrentMap<QName, URL> wsOverridenEndpointAddresses = new ConcurrentHashMap<QName, URL>();

  protected CommandContextFactory commandContextFactory;
  protected TransactionContextFactory transactionContextFactory;

  protected boolean isRollbackDeployment;

  protected Map<Object, Object> beans;

  protected DelegateInterceptor delegateInterceptor;

  protected Map<String, EventHandler> eventHandlers;
  protected List<EventHandler> customEventHandlers;

  protected FailedJobCommandFactory failedJobCommandFactory;

  /**
   * Set this to true if you want to have extra checks on the BPMN xml that is parsed. See http://www.jorambarrez.be/blog/2013/02/19/uploading-a-funny-xml -can-bring-down-your-server/
   *
   * Unfortunately, this feature is not available on some platforms (JDK 6, JBoss), hence the reason why it is disabled by default. If your platform allows the use of StaxSource during XML parsing, do
   * enable it.
   */
  protected boolean enableSafeBpmnXml;

  /**
   * The following settings will determine the amount of entities loaded at once when the engine needs to load multiple entities (eg. when suspending a process definition with all its process
   * instances).
   *
   * The default setting is quite low, as not to surprise anyone with sudden memory spikes. Change it to something higher if the environment Activiti runs in allows it.
   */
  protected int batchSizeProcessInstances = 25;
  protected int batchSizeTasks = 25;

  protected boolean enableEventDispatcher = true;
  protected ActivitiEventDispatcher eventDispatcher;
  protected List<ActivitiEventListener> eventListeners;
  protected Map<String, List<ActivitiEventListener>> typedEventListeners;

  // Event logging to database
  protected boolean enableDatabaseEventLogging;

  /**
   * Using field injection together with a delegate expression for a service
   * task / execution listener / task listener is not thread-sade , see user
   * guide section 'Field Injection' for more information.
   *
   * Set this flag to false to throw an exception at runtime when a field is
   * injected and a delegateExpression is used.
   *
   * @since 5.21
   */
  protected DelegateExpressionFieldInjectionMode delegateExpressionFieldInjectionMode = DelegateExpressionFieldInjectionMode.MIXED;

  /**
  *  Define a max length for storing String variable types in the database.
  *  Mainly used for the Oracle NVARCHAR2 limit of 2000 characters
  */
  protected int maxLengthStringVariableType = -1;

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
   * By default: 100 (75 for mssql server as it has a hard limit of 2000 parameters in a statement)
   */
  protected int maxNrOfStatementsInBulkInsert = 100;

  public int DEFAULT_MAX_NR_OF_STATEMENTS_BULK_INSERT_SQL_SERVER = 70; // currently Execution has most params (28). 2000 / 28 = 71.

  protected ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Flag that can be set to configure or nota relational database is used.
   * This is useful for custom implementations that do not use relational databases at all.
   *
   * If true (default), the {@link ProcessEngineConfiguration#getDatabaseSchemaUpdate()} value will be used to determine
   * what needs to happen wrt the database schema.
   *
   * If false, no validation or schema creation will be done. That means that the database schema must have been
   * created 'manually' before but the engine does not validate whether the schema is correct.
   * The {@link ProcessEngineConfiguration#getDatabaseSchemaUpdate()} value will not be used.
   */
  protected boolean usingRelationalDatabase = true;

  /**
   * Enabled a very verbose debug output of the execution tree whilst executing operations.
   * Most useful for core engine developers or people fiddling around with the execution tree.
   */
  protected boolean enableVerboseExecutionTreeLogging;

  protected PerformanceSettings performanceSettings = new PerformanceSettings();

  protected ProcessDefinitionHelper processDefinitionHelper;

  // buildProcessEngine
  // ///////////////////////////////////////////////////////

  @Override
  public ProcessEngine buildProcessEngine() {
    init();
    ProcessEngineImpl processEngine = new ProcessEngineImpl(this);
    postProcessEngineInitialisation();

    return processEngine;
  }

  // init
  // /////////////////////////////////////////////////////////////////////

  public void init() {
    initConfigurators();
    configuratorsBeforeInit();
    initHistoryLevel();
    initExpressionManager();

    if (usingRelationalDatabase) {
      initDataSource();
    }

    initAgendaFactory();
    initHelpers();
    initVariableTypes();
    initBeans();
    initScriptingEngines();
    initClock();
    initBusinessCalendarManager();
    initCommandContextFactory();
    initTransactionContextFactory();
    initCommandExecutors();
    initServices();
    initIdGenerator();
    initBehaviorFactory();
    initListenerFactory();
    initBpmnParser();
    initProcessDefinitionCache();
    initProcessDefinitionInfoCache();
    initKnowledgeBaseCache();
    initJobHandlers();
    initJobManager();
    initAsyncExecutor();

    initTransactionFactory();

    if (usingRelationalDatabase) {
      initSqlSessionFactory();
    }

    initSessionFactories();
    initDataManagers();
    initEntityManagers();
    initHistoryManager();
    initJpa();
    initDeployers();
    initDelegateInterceptor();
    initEventHandlers();
    initFailedJobCommandFactory();
    initEventDispatcher();
    initProcessValidator();
    initDatabaseEventLogging();
    configuratorsAfterInit();
  }

  // failedJobCommandFactory
  // ////////////////////////////////////////////////////////

  public void initFailedJobCommandFactory() {
    if (failedJobCommandFactory == null) {
      failedJobCommandFactory = new DefaultFailedJobCommandFactory();
    }
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
      if (enableVerboseExecutionTreeLogging) {
        commandInvoker = new DebugCommandInvoker();
      } else {
        commandInvoker = new CommandInvoker();
      }
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

    if (commandContextFactory != null) {
      interceptors.add(new CommandContextInterceptor(commandContextFactory, this));
    }

    if (transactionContextFactory != null) {
      interceptors.add(new TransactionContextInterceptor(transactionContextFactory));
    }

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
      throw new ActivitiException("invalid command interceptor chain configuration: " + chain);
    }
    for (int i = 0; i < chain.size() - 1; i++) {
      chain.get(i).setNext(chain.get(i + 1));
    }
    return chain.get(0);
  }

  public abstract CommandInterceptor createTransactionInterceptor();

  // services
  // /////////////////////////////////////////////////////////////////

  public void initServices() {
    initService(repositoryService);
    initService(runtimeService);
    initService(historyService);
    initService(taskService);
    initService(managementService);
    initService(dynamicBpmnService);
  }

  public void initService(Object service) {
    if (service instanceof ServiceImpl) {
      ((ServiceImpl) service).setCommandExecutor(commandExecutor);
    }
  }

  // DataSource
  // ///////////////////////////////////////////////////////////////

  public void initDataSource() {
    if (dataSource == null) {
      if (dataSourceJndiName != null) {
        try {
          dataSource = (DataSource) new InitialContext().lookup(dataSourceJndiName);
        } catch (Exception e) {
          throw new ActivitiException("couldn't lookup datasource from " + dataSourceJndiName + ": " + e.getMessage(), e);
        }

      } else if (jdbcUrl != null) {
        if ((jdbcDriver == null) || (jdbcUsername == null)) {
          throw new ActivitiException("DataSource or JDBC properties have to be specified in a process engine configuration");
        }

        log.debug("initializing datasource to db: {}", jdbcUrl);

        PooledDataSource pooledDataSource = new PooledDataSource(ReflectUtil.getClassLoader(), jdbcDriver, jdbcUrl, jdbcUsername, jdbcPassword);

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
        if (jdbcPingEnabled) {
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

  protected static Properties databaseVendorMappings = getDefaultDatabaseTypeMappings();

  public static final String DATABASE_TYPE_H2 = "h2";
  public static final String DATABASE_TYPE_HSQL = "hsql";
  public static final String DATABASE_TYPE_MYSQL = "mysql";
  public static final String DATABASE_TYPE_ORACLE = "oracle";
  public static final String DATABASE_TYPE_POSTGRES = "postgres";
  public static final String DATABASE_TYPE_MSSQL = "mssql";
  public static final String DATABASE_TYPE_MARIADB = "mariadb";
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
    databaseTypeMappings.setProperty("DB2",DATABASE_TYPE_DB2);
    databaseTypeMappings.setProperty("DB2/NT", DATABASE_TYPE_DB2);
    databaseTypeMappings.setProperty("DB2/NT64", DATABASE_TYPE_DB2);
    databaseTypeMappings.setProperty("DB2 UDP", DATABASE_TYPE_DB2);
    databaseTypeMappings.setProperty("DB2/LINUX", DATABASE_TYPE_DB2);
    databaseTypeMappings.setProperty("DB2/LINUX390", DATABASE_TYPE_DB2);
    databaseTypeMappings.setProperty("DB2/LINUXX8664", DATABASE_TYPE_DB2);
    databaseTypeMappings.setProperty("DB2/LINUXZ64", DATABASE_TYPE_DB2);
    databaseTypeMappings.setProperty("DB2/LINUXPPC64",DATABASE_TYPE_DB2);
    databaseTypeMappings.setProperty("DB2/LINUXPPC64LE",DATABASE_TYPE_DB2);
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
    databaseTypeMappings.setProperty("MariaDB", DATABASE_TYPE_MARIADB);
    return databaseTypeMappings;
  }

  public void initDatabaseType() {
    Connection connection = null;
    try {
      connection = dataSource.getConnection();
      DatabaseMetaData databaseMetaData = connection.getMetaData();
      String databaseProductName = databaseMetaData.getDatabaseProductName();
      log.debug("database product name: '{}'", databaseProductName);
      String databaseVendor = databaseVendorMappings.getProperty(databaseProductName);
      if (databaseVendor == null) {
        throw new ActivitiException("couldn't deduct database vendor from database product name '" + databaseProductName + "'");
      }

      databaseType = databaseVendor;

      switch (databaseVendor) {
          case DATABASE_TYPE_MYSQL:
              String databaseProductVersion = databaseMetaData.getDatabaseProductVersion();
              // MariaDB has performance penalty when using "withoutJoins" SQL scripts
              // example of MariaDB 10.5.4
              //     input: vendor=MySQL, version=5.5.5-10.5.24-MariaDB-1:10.5.24+maria~ubu2004
              //     output: databaseType=mariadb
              if (databaseProductVersion.toLowerCase().contains(DATABASE_TYPE_MARIADB)) {
                  databaseType = DATABASE_TYPE_MARIADB;
              }
              break;
          // Special care for MSSQL, as it has a hard limit of 2000 params per statement (incl bulk statement).
          // Especially with executions, with 100 as default, this limit is passed.
          case DATABASE_TYPE_MSSQL:
              maxNrOfStatementsInBulkInsert = DEFAULT_MAX_NR_OF_STATEMENTS_BULK_INSERT_SQL_SERVER;
              break;
      }
      log.debug("using database type: {}", databaseType);
    } catch (SQLException e) {
      log.error("Exception while initializing Database connection", e);
    } finally {
      try {
        if (connection != null) {
          connection.close();
        }
      } catch (SQLException e) {
        log.error("Exception while closing the Database connection", e);
      }
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
        String wildcardEscapeClause = "";
        if ((databaseWildcardEscapeCharacter != null) && (databaseWildcardEscapeCharacter.length() != 0)) {
          wildcardEscapeClause = " escape '" + databaseWildcardEscapeCharacter + "'";
        }
        properties.put("wildcardEscapeClause", wildcardEscapeClause);
        //set default properties
        properties.put("limitBefore" , "");
        properties.put("limitAfter" , "");
        properties.put("limitBetween" , "");
        properties.put("limitOuterJoinBetween" , "");
        properties.put("limitBeforeNativeQuery" , "");
        properties.put("orderBy" , "order by ${orderByColumns}");
        properties.put("blobType" , "BLOB");
        properties.put("boolValue" , "TRUE");

        if (databaseType != null) {
            properties.load(getResourceAsStream("org/activiti/db/properties/"+databaseType+".properties"));
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

  public Configuration initMybatisConfiguration(Environment environment, Reader reader, Properties properties) {
    XMLConfigBuilder parser = new XMLConfigBuilder(reader, "", properties);
    Configuration configuration = parser.getConfiguration();

    if(databaseType != null) {
        configuration.setDatabaseId(databaseType);
    }

    configuration.setEnvironment(environment);

    initMybatisTypeHandlers(configuration);
    initCustomMybatisMappers(configuration);

    configuration = parseMybatisConfiguration(configuration, parser);
    return configuration;
  }

  public void initMybatisTypeHandlers(Configuration configuration) {
    configuration.getTypeHandlerRegistry().register(VariableType.class, JdbcType.VARCHAR, new IbatisVariableTypeHandler());
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
    return ReflectUtil.getResourceAsStream(resource);
  }

  public InputStream getMyBatisXmlConfigurationStream() {
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

  // Data managers ///////////////////////////////////////////////////////////

  public void initDataManagers() {
    if (attachmentDataManager == null) {
      attachmentDataManager = new MybatisAttachmentDataManager(this);
    }
    if (byteArrayDataManager == null) {
      byteArrayDataManager = new MybatisByteArrayDataManager(this);
    }
    if (commentDataManager == null) {
      commentDataManager = new MybatisCommentDataManager(this);
    }
    if (deploymentDataManager == null) {
      deploymentDataManager = new MybatisDeploymentDataManager(this);
    }
    if (eventLogEntryDataManager == null) {
      eventLogEntryDataManager = new MybatisEventLogEntryDataManager(this);
    }
    if (eventSubscriptionDataManager == null) {
      eventSubscriptionDataManager = new MybatisEventSubscriptionDataManager(this);
    }
    if (executionDataManager == null) {
      executionDataManager = new MybatisExecutionDataManager(this);
    }
    if (historicActivityInstanceDataManager == null) {
      historicActivityInstanceDataManager = new MybatisHistoricActivityInstanceDataManager(this);
    }
    if (historicDetailDataManager == null) {
      historicDetailDataManager = new MybatisHistoricDetailDataManager(this);
    }
    if (historicIdentityLinkDataManager == null) {
      historicIdentityLinkDataManager = new MybatisHistoricIdentityLinkDataManager(this);
    }
    if (historicProcessInstanceDataManager == null) {
      historicProcessInstanceDataManager = new MybatisHistoricProcessInstanceDataManager(this);
    }
    if (historicTaskInstanceDataManager == null) {
      historicTaskInstanceDataManager = new MybatisHistoricTaskInstanceDataManager(this);
    }
    if (historicVariableInstanceDataManager == null) {
      historicVariableInstanceDataManager = new MybatisHistoricVariableInstanceDataManager(this);
    }
    if (identityLinkDataManager == null) {
      identityLinkDataManager = new MybatisIdentityLinkDataManager(this);
    }
    if (jobDataManager == null) {
      jobDataManager = new MybatisJobDataManager(this);
    }
    if (timerJobDataManager == null) {
      timerJobDataManager = new MybatisTimerJobDataManager(this);
    }
    if (suspendedJobDataManager == null) {
      suspendedJobDataManager = new MybatisSuspendedJobDataManager(this);
    }
    if (deadLetterJobDataManager == null) {
      deadLetterJobDataManager = new MybatisDeadLetterJobDataManager(this);
    }
    if (modelDataManager == null) {
      modelDataManager = new MybatisModelDataManager(this);
    }
    if (processDefinitionDataManager == null) {
      processDefinitionDataManager = new MybatisProcessDefinitionDataManager(this);
    }
    if (processDefinitionInfoDataManager == null) {
      processDefinitionInfoDataManager = new MybatisProcessDefinitionInfoDataManager(this);
    }
    if (propertyDataManager == null) {
      propertyDataManager = new MybatisPropertyDataManager(this);
    }
    if (resourceDataManager == null) {
      resourceDataManager = new MybatisResourceDataManager(this);
    }
    if (taskDataManager == null) {
      taskDataManager = new MybatisTaskDataManager(this);
    }
    if (variableInstanceDataManager == null) {
      variableInstanceDataManager = new MybatisVariableInstanceDataManager(this);
    }
  }

    // Entity managers //////////////////////////////////////////////////////////

  public void initEntityManagers() {
    if (attachmentEntityManager == null) {
      attachmentEntityManager = new AttachmentEntityManagerImpl(this, attachmentDataManager);
    }
    if (byteArrayEntityManager == null) {
      byteArrayEntityManager = new ByteArrayEntityManagerImpl(this, byteArrayDataManager);
    }
    if (commentEntityManager == null) {
      commentEntityManager = new CommentEntityManagerImpl(this, commentDataManager);
    }
    if (deploymentEntityManager == null) {
      deploymentEntityManager = new DeploymentEntityManagerImpl(this, deploymentDataManager);
    }
    if (eventLogEntryEntityManager == null) {
      eventLogEntryEntityManager = new EventLogEntryEntityManagerImpl(this, eventLogEntryDataManager);
    }
    if (eventSubscriptionEntityManager == null) {
      eventSubscriptionEntityManager = new EventSubscriptionEntityManagerImpl(this, eventSubscriptionDataManager);
    }
    if (executionEntityManager == null) {
      executionEntityManager = new ExecutionEntityManagerImpl(this, executionDataManager);
    }
    if (historicActivityInstanceEntityManager == null) {
      historicActivityInstanceEntityManager = new HistoricActivityInstanceEntityManagerImpl(this, historicActivityInstanceDataManager);
    }
    if (historicDetailEntityManager == null) {
      historicDetailEntityManager = new HistoricDetailEntityManagerImpl(this, historicDetailDataManager);
    }
    if (historicIdentityLinkEntityManager == null) {
      historicIdentityLinkEntityManager = new HistoricIdentityLinkEntityManagerImpl(this, historicIdentityLinkDataManager);
    }
    if (historicProcessInstanceEntityManager == null) {
      historicProcessInstanceEntityManager = new HistoricProcessInstanceEntityManagerImpl(this, historicProcessInstanceDataManager);
    }
    if (historicTaskInstanceEntityManager == null) {
      historicTaskInstanceEntityManager = new HistoricTaskInstanceEntityManagerImpl(this, historicTaskInstanceDataManager);
    }
    if (historicVariableInstanceEntityManager == null) {
      historicVariableInstanceEntityManager = new HistoricVariableInstanceEntityManagerImpl(this, historicVariableInstanceDataManager);
    }

    if (identityLinkEntityManager == null) {
      identityLinkEntityManager = new IdentityLinkEntityManagerImpl(this, identityLinkDataManager);
    }
    if (jobEntityManager == null) {
      jobEntityManager = new JobEntityManagerImpl(this, jobDataManager);
    }
    if (timerJobEntityManager == null) {
      timerJobEntityManager = new TimerJobEntityManagerImpl(this, timerJobDataManager);
    }
    if (suspendedJobEntityManager == null) {
      suspendedJobEntityManager = new SuspendedJobEntityManagerImpl(this, suspendedJobDataManager);
    }
    if (deadLetterJobEntityManager == null) {
      deadLetterJobEntityManager = new DeadLetterJobEntityManagerImpl(this, deadLetterJobDataManager);
    }
    if (modelEntityManager == null) {
      modelEntityManager = new ModelEntityManagerImpl(this, modelDataManager);
    }
    if (processDefinitionEntityManager == null) {
      processDefinitionEntityManager = new ProcessDefinitionEntityManagerImpl(this, processDefinitionDataManager);
    }
    if (processDefinitionInfoEntityManager == null) {
      processDefinitionInfoEntityManager = new ProcessDefinitionInfoEntityManagerImpl(this, processDefinitionInfoDataManager);
    }
    if (propertyEntityManager == null) {
      propertyEntityManager = new PropertyEntityManagerImpl(this, propertyDataManager);
    }
    if (resourceEntityManager == null) {
      resourceEntityManager = new ResourceEntityManagerImpl(this, resourceDataManager);
    }
    if (tableDataManager == null) {
      tableDataManager = new TableDataManagerImpl(this);
    }
    if (taskEntityManager == null) {
      taskEntityManager = new TaskEntityManagerImpl(this, taskDataManager);
    }
    if (variableInstanceEntityManager == null) {
      variableInstanceEntityManager = new VariableInstanceEntityManagerImpl(this, variableInstanceDataManager);
    }
  }

    // History manager ///////////////////////////////////////////////////////////

  public void initHistoryManager() {
    if (historyManager == null) {
      historyManager = new DefaultHistoryManager(this, historyLevel);
    }
  }

  // Job manager ///////////////////////////////////////////////////////////

   public void initJobManager() {
     if (jobManager == null) {
       jobManager = new DefaultJobManager(this);
     }

     jobManager.setProcessEngineConfiguration(this);
   }

  // session factories ////////////////////////////////////////////////////////

  public void initSessionFactories() {
    if (sessionFactories == null) {
      sessionFactories = new HashMap<Class<?>, SessionFactory>();

      if (usingRelationalDatabase) {
        initDbSqlSessionFactory();
      }

      addSessionFactory(new GenericManagerFactory(EntityCache.class, EntityCacheImpl.class));
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
    dbSqlSessionFactory.setIdGenerator(idGenerator);
    dbSqlSessionFactory.setSqlSessionFactory(sqlSessionFactory);
    dbSqlSessionFactory.setDbHistoryUsed(isDbHistoryUsed);
    dbSqlSessionFactory.setDatabaseTablePrefix(databaseTablePrefix);
    dbSqlSessionFactory.setTablePrefixIsSchema(tablePrefixIsSchema);
    dbSqlSessionFactory.setDatabaseCatalog(databaseCatalog);
    dbSqlSessionFactory.setDatabaseSchema(databaseSchema);
    dbSqlSessionFactory.setBulkInsertEnabled(isBulkInsertEnabled, databaseType);
    dbSqlSessionFactory.setMaxNrOfStatementsInBulkInsert(maxNrOfStatementsInBulkInsert);
    addSessionFactory(dbSqlSessionFactory);
  }

  public DbSqlSessionFactory createDbSqlSessionFactory() {
    return new DbSqlSessionFactory();
  }

  public void addSessionFactory(SessionFactory sessionFactory) {
    sessionFactories.put(sessionFactory.getSessionType(), sessionFactory);
  }

  public void initConfigurators() {

    allConfigurators = new ArrayList<ProcessEngineConfigurator>();

    // Configurators that are explicitly added to the config
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

      ServiceLoader<ProcessEngineConfigurator> configuratorServiceLoader = ServiceLoader.load(ProcessEngineConfigurator.class, classLoader);
      int nrOfServiceLoadedConfigurators = 0;
      for (ProcessEngineConfigurator configurator : configuratorServiceLoader) {
        allConfigurators.add(configurator);
        nrOfServiceLoadedConfigurators++;
      }

      if (nrOfServiceLoadedConfigurators > 0) {
        log.info("Found {} auto-discoverable Process Engine Configurator{}", nrOfServiceLoadedConfigurators++, nrOfServiceLoadedConfigurators > 1 ? "s" : "");
      }

      if (!allConfigurators.isEmpty()) {

        // Order them according to the priorities (useful for dependent
        // configurator)
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

  public void configuratorsBeforeInit() {
    for (ProcessEngineConfigurator configurator : allConfigurators) {
      log.info("Executing beforeInit() of {} (priority:{})", configurator.getClass(), configurator.getPriority());
      configurator.beforeInit(this);
    }
  }

  public void configuratorsAfterInit() {
    for (ProcessEngineConfigurator configurator : allConfigurators) {
      log.info("Executing configure() of {} (priority:{})", configurator.getClass(), configurator.getPriority());
      configurator.configure(this);
    }
  }

  // deployers
  // ////////////////////////////////////////////////////////////////

  public void initProcessDefinitionCache() {
    if (processDefinitionCache == null) {
      if (processDefinitionCacheLimit <= 0) {
        processDefinitionCache = new DefaultDeploymentCache<ProcessDefinitionCacheEntry>();
      } else {
        processDefinitionCache = new DefaultDeploymentCache<ProcessDefinitionCacheEntry>(processDefinitionCacheLimit);
      }
    }
  }

  public void initProcessDefinitionInfoCache() {
    if (processDefinitionInfoCache == null) {
      if (processDefinitionInfoCacheLimit <= 0) {
        processDefinitionInfoCache = new ProcessDefinitionInfoCache(commandExecutor);
      } else {
        processDefinitionInfoCache = new ProcessDefinitionInfoCache(commandExecutor, processDefinitionInfoCacheLimit);
      }
    }
  }

  public void initKnowledgeBaseCache() {
    if (knowledgeBaseCache == null) {
      if (knowledgeBaseCacheLimit <= 0) {
        knowledgeBaseCache = new DefaultDeploymentCache<Object>();
      } else {
        knowledgeBaseCache = new DefaultDeploymentCache<Object>(knowledgeBaseCacheLimit);
      }
    }
  }

  public void initDeployers() {
    if (this.deployers == null) {
      this.deployers = new ArrayList<Deployer>();
      if (customPreDeployers != null) {
        this.deployers.addAll(customPreDeployers);
      }
      this.deployers.addAll(getDefaultDeployers());
      if (customPostDeployers != null) {
        this.deployers.addAll(customPostDeployers);
      }
    }

    if (deploymentManager == null) {
      deploymentManager = new DeploymentManager();
      deploymentManager.setDeployers(deployers);

      deploymentManager.setProcessDefinitionCache(processDefinitionCache);
      deploymentManager.setProcessDefinitionInfoCache(processDefinitionInfoCache);
      deploymentManager.setKnowledgeBaseCache(knowledgeBaseCache);
      deploymentManager.setProcessEngineConfiguration(this);
      deploymentManager.setProcessDefinitionEntityManager(processDefinitionEntityManager);
      deploymentManager.setDeploymentEntityManager(deploymentEntityManager);
    }
  }

  public void initBpmnDeployerDependencies() {

    if (parsedDeploymentBuilderFactory == null) {
      parsedDeploymentBuilderFactory = new ParsedDeploymentBuilderFactory();
    }
    if (parsedDeploymentBuilderFactory.getBpmnParser() == null) {
      parsedDeploymentBuilderFactory.setBpmnParser(bpmnParser);
    }

    if (timerManager == null) {
      timerManager = new TimerManager();
    }

    if (eventSubscriptionManager == null) {
      eventSubscriptionManager = new EventSubscriptionManager();
    }

    if (bpmnDeploymentHelper == null) {
      bpmnDeploymentHelper = new BpmnDeploymentHelper();
    }
    if (bpmnDeploymentHelper.getTimerManager() == null) {
      bpmnDeploymentHelper.setTimerManager(timerManager);
    }
    if (bpmnDeploymentHelper.getEventSubscriptionManager() == null) {
      bpmnDeploymentHelper.setEventSubscriptionManager(eventSubscriptionManager);
    }

    if (cachingAndArtifactsManager == null) {
      cachingAndArtifactsManager = new CachingAndArtifactsManager();
    }

  }

  public Collection<? extends Deployer> getDefaultDeployers() {
    List<Deployer> defaultDeployers = new ArrayList<Deployer>();

    if (bpmnDeployer == null) {
      bpmnDeployer = new BpmnDeployer();
    }

    initBpmnDeployerDependencies();

    bpmnDeployer.setIdGenerator(idGenerator);
    bpmnDeployer.setParsedDeploymentBuilderFactory(parsedDeploymentBuilderFactory);
    bpmnDeployer.setBpmnDeploymentHelper(bpmnDeploymentHelper);
    bpmnDeployer.setCachingAndArtifactsManager(cachingAndArtifactsManager);
    bpmnDeployer.setDisableExistingStartEventSubscriptions(disableExistingStartEventSubscriptions);

    defaultDeployers.add(bpmnDeployer);
    return defaultDeployers;
  }

  public void initListenerFactory() {
    if (listenerFactory == null) {
      DefaultListenerFactory defaultListenerFactory = new DefaultListenerFactory();
      defaultListenerFactory.setExpressionManager(expressionManager);
      listenerFactory = defaultListenerFactory;
    } else if ((listenerFactory instanceof AbstractBehaviorFactory) && ((AbstractBehaviorFactory) listenerFactory).getExpressionManager() == null) {
      ((AbstractBehaviorFactory) listenerFactory).setExpressionManager(expressionManager);
    }
  }

  public void initBehaviorFactory() {
    if (activityBehaviorFactory == null) {
      DefaultActivityBehaviorFactory defaultActivityBehaviorFactory = new DefaultActivityBehaviorFactory();
      defaultActivityBehaviorFactory.setExpressionManager(expressionManager);
      activityBehaviorFactory = defaultActivityBehaviorFactory;
    } else if ((activityBehaviorFactory instanceof AbstractBehaviorFactory) && ((AbstractBehaviorFactory) activityBehaviorFactory).getExpressionManager() == null) {
      ((AbstractBehaviorFactory) activityBehaviorFactory).setExpressionManager(expressionManager);
    }
  }

  public void initBpmnParser() {
    if (bpmnParser == null) {
      bpmnParser = new BpmnParser();
    }

    if (bpmnParseFactory == null) {
      bpmnParseFactory = new DefaultBpmnParseFactory();
    }

    bpmnParser.setBpmnParseFactory(bpmnParseFactory);
    bpmnParser.setActivityBehaviorFactory(activityBehaviorFactory);
    bpmnParser.setListenerFactory(listenerFactory);

    List<BpmnParseHandler> parseHandlers = new ArrayList<BpmnParseHandler>();
    if (getPreBpmnParseHandlers() != null) {
      parseHandlers.addAll(getPreBpmnParseHandlers());
    }
    parseHandlers.addAll(getDefaultBpmnParseHandlers());
    if (getPostBpmnParseHandlers() != null) {
      parseHandlers.addAll(getPostBpmnParseHandlers());
    }

    BpmnParseHandlers bpmnParseHandlers = new BpmnParseHandlers();
    bpmnParseHandlers.addHandlers(parseHandlers);
    bpmnParser.setBpmnParserHandlers(bpmnParseHandlers);
  }

  public List<BpmnParseHandler> getDefaultBpmnParseHandlers() {

    // Alphabetic list of default parse handler classes
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
    bpmnParserHandlers.add(new LinkEventDefinitionParseHandler());
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
    bpmnParserHandlers.add(new AdhocSubProcessParseHandler());
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

      for (int i = 0; i < bpmnParserHandlers.size(); i++) {
        // All the default handlers support only one type
        BpmnParseHandler defaultBpmnParseHandler = bpmnParserHandlers.get(i);
        if (defaultBpmnParseHandler.getHandledTypes().size() != 1) {
          StringBuilder supportedTypes = new StringBuilder();
          for (Class<?> type : defaultBpmnParseHandler.getHandledTypes()) {
            supportedTypes.append(" ").append(type.getCanonicalName()).append(" ");
          }
          throw new ActivitiException("The default BPMN parse handlers should only support one type, but " + defaultBpmnParseHandler.getClass() + " supports " + supportedTypes.toString()
              + ". This is likely a programmatic error");
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

    return bpmnParserHandlers;
  }

  public void initClock() {
    if (clock == null) {
      clock = new DefaultClockImpl();
    }
  }


  public void initAgendaFactory() {
    if (this.engineAgendaFactory == null) {
      this.engineAgendaFactory = new DefaultActivitiEngineAgendaFactory();
    }
  }

  public void initJobHandlers() {
    jobHandlers = new HashMap<String, JobHandler>();

    AsyncContinuationJobHandler asyncContinuationJobHandler = new AsyncContinuationJobHandler();
    jobHandlers.put(asyncContinuationJobHandler.getType(), asyncContinuationJobHandler);

    TriggerTimerEventJobHandler triggerTimerEventJobHandler = new TriggerTimerEventJobHandler();
    jobHandlers.put(triggerTimerEventJobHandler.getType(), triggerTimerEventJobHandler);

    TimerStartEventJobHandler timerStartEvent = new TimerStartEventJobHandler();
    jobHandlers.put(timerStartEvent.getType(), timerStartEvent);

    TimerSuspendProcessDefinitionHandler suspendProcessDefinitionHandler = new TimerSuspendProcessDefinitionHandler();
    jobHandlers.put(suspendProcessDefinitionHandler.getType(), suspendProcessDefinitionHandler);

    TimerActivateProcessDefinitionHandler activateProcessDefinitionHandler = new TimerActivateProcessDefinitionHandler();
    jobHandlers.put(activateProcessDefinitionHandler.getType(), activateProcessDefinitionHandler);

    ProcessEventJobHandler processEventJobHandler = new ProcessEventJobHandler();
    jobHandlers.put(processEventJobHandler.getType(), processEventJobHandler);

    // if we have custom job handlers, register them
    if (getCustomJobHandlers() != null) {
      for (JobHandler customJobHandler : getCustomJobHandlers()) {
        jobHandlers.put(customJobHandler.getType(), customJobHandler);
      }
    }
  }

  // async executor
  // /////////////////////////////////////////////////////////////

  public void initAsyncExecutor() {
    if (asyncExecutor == null) {
      DefaultAsyncJobExecutor defaultAsyncExecutor = new DefaultAsyncJobExecutor();
      defaultAsyncExecutor.applyConfig(this);
      asyncExecutor = defaultAsyncExecutor;
    }

    asyncExecutor.setProcessEngineConfiguration(this);
    asyncExecutor.setAutoActivate(asyncExecutorActivate);
  }

  // history
  // //////////////////////////////////////////////////////////////////

  public void initHistoryLevel() {
    if (historyLevel == null) {
      historyLevel = HistoryLevel.getHistoryLevelForKey(getHistory());
    }
  }

  // id generator
  // /////////////////////////////////////////////////////////////

  public void initIdGenerator() {
    if (idGenerator == null) {
      CommandExecutor idGeneratorCommandExecutor = null;
      if (idGeneratorDataSource != null) {
        ProcessEngineConfigurationImpl processEngineConfiguration = new StandaloneProcessEngineConfiguration();
        processEngineConfiguration.setDataSource(idGeneratorDataSource);
        processEngineConfiguration.setDatabaseSchemaUpdate(DB_SCHEMA_UPDATE_FALSE);
        processEngineConfiguration.init();
        idGeneratorCommandExecutor = processEngineConfiguration.getCommandExecutor();
      } else if (idGeneratorDataSourceJndiName != null) {
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

  // OTHER
  // ////////////////////////////////////////////////////////////////////

  public void initCommandContextFactory() {
    if (commandContextFactory == null) {
      commandContextFactory = new CommandContextFactory();
    }
    commandContextFactory.setProcessEngineConfiguration(this);
  }

  public void initTransactionContextFactory() {
    if (transactionContextFactory == null) {
      transactionContextFactory = new StandaloneMybatisTransactionContextFactory();
    }
  }

  public void initHelpers() {
    if (processInstanceHelper == null) {
      processInstanceHelper = new ProcessInstanceHelper();
    }
    if (listenerNotificationHelper == null) {
      listenerNotificationHelper = new ListenerNotificationHelper();
    }
  }

  public void initVariableTypes() {
    if (variableTypes == null) {
      variableTypes = new DefaultVariableTypes();
      if (customPreVariableTypes != null) {
        for (VariableType customVariableType : customPreVariableTypes) {
          variableTypes.addType(customVariableType);
        }
      }
      variableTypes.addType(new NullType());
      variableTypes.addType(new StringType(getMaxLengthString()));
      variableTypes.addType(new LongStringType(getMaxLengthString() + 1));
      variableTypes.addType(new BooleanType());
      variableTypes.addType(new ShortType());
      variableTypes.addType(new IntegerType());
      variableTypes.addType(new LongType());
      variableTypes.addType(new DateType());
      variableTypes.addType(new LocalDateTimeType());
      variableTypes.addType(new LocalDateType());
      variableTypes.addType(new JodaDateType());
      variableTypes.addType(new JodaDateTimeType());
      variableTypes.addType(new DoubleType());
      variableTypes.addType(new UUIDType());
      variableTypes.addType(new BigDecimalType());

        objectMapper.configOverride(BigDecimal.class)
            .setFormat(JsonFormat.Value.forShape(JsonFormat.Shape.STRING));
      JsonTypeConverter jsonTypeConverter = new JsonTypeConverter(objectMapper,
            javaClassFieldForJackson);
      variableTypes.addType(new JsonType(getMaxLengthString(), objectMapper,serializePOJOsInVariablesToJson,
            jsonTypeConverter));
      variableTypes.addType(new LongJsonType(getMaxLengthString() + 1, objectMapper,serializePOJOsInVariablesToJson,
          jsonTypeConverter));

      //java serialization only supported OOTB if not defaulting to json
      //if java serliazation needed together with json defaulting then add to customPostVariableTypes
      if(!serializePOJOsInVariablesToJson) {
        variableTypes.addType(new ByteArrayType());
        variableTypes.addType(new SerializableType(serializableVariableTypeTrackDeserializedObjects));
        variableTypes.addType(new CustomObjectType("item", ItemInstance.class));
        variableTypes.addType(new CustomObjectType("message", MessageInstance.class));
      }
      if (customPostVariableTypes != null) {
        for (VariableType customVariableType : customPostVariableTypes) {
          variableTypes.addType(customVariableType);
        }
      }
    }
  }

  public int getMaxLengthString() {
    if (maxLengthStringVariableType == -1) {
      if ("oracle".equalsIgnoreCase(databaseType)) {
        return DEFAULT_ORACLE_MAX_LENGTH_STRING;
      } else {
        return DEFAULT_GENERIC_MAX_LENGTH_STRING;
      }
    } else {
      return maxLengthStringVariableType;
    }
  }

  public void initScriptingEngines() {
    if (resolverFactories == null) {
      resolverFactories = new ArrayList<ResolverFactory>();
      resolverFactories.add(new VariableScopeResolverFactory());
      resolverFactories.add(new BeansResolverFactory());
    }
    if (scriptingEngines == null) {
      scriptingEngines = new ScriptingEngines(new ScriptBindingsFactory(this, resolverFactories));
    }
  }

  public void initExpressionManager() {
      if (expressionManager == null) {
          expressionManager = new ExpressionManager(beans);
          if (customFunctionProviders != null) {
              expressionManager.setCustomFunctionProviders(customFunctionProviders);
          }
      }
  }

    public void setCustomELResolvers(List<ELResolver> customELResolvers){
     this.customELResolvers = customELResolvers;
  }

    public List<ELResolver> getCustomELResolvers(){
       return this.customELResolvers;
    }

  public void initBusinessCalendarManager() {
    if (businessCalendarManager == null) {
      MapBusinessCalendarManager mapBusinessCalendarManager = new MapBusinessCalendarManager();
      mapBusinessCalendarManager.addBusinessCalendar(DurationBusinessCalendar.NAME, new DurationBusinessCalendar(this.clock));
      mapBusinessCalendarManager.addBusinessCalendar(DueDateBusinessCalendar.NAME, new DueDateBusinessCalendar(this.clock));
      mapBusinessCalendarManager.addBusinessCalendar(CycleBusinessCalendar.NAME, new CycleBusinessCalendar(this.clock));

      businessCalendarManager = mapBusinessCalendarManager;
    }
  }

  public void initDelegateInterceptor() {
    if (delegateInterceptor == null) {
      delegateInterceptor = new DefaultDelegateInterceptor();
    }
  }

  public void initEventHandlers() {
    if (eventHandlers == null) {
      eventHandlers = new HashMap<String, EventHandler>();

      SignalEventHandler signalEventHandler = new SignalEventHandler();
      eventHandlers.put(signalEventHandler.getEventHandlerType(), signalEventHandler);

      CompensationEventHandler compensationEventHandler = new CompensationEventHandler();
      eventHandlers.put(compensationEventHandler.getEventHandlerType(), compensationEventHandler);

      MessageEventHandler messageEventHandler = new MessageEventHandler(eventSubscriptionPayloadMappingProvider);
      eventHandlers.put(messageEventHandler.getEventHandlerType(), messageEventHandler);

    }
    if (customEventHandlers != null) {
      for (EventHandler eventHandler : customEventHandlers) {
        eventHandlers.put(eventHandler.getEventHandlerType(), eventHandler);
      }
    }
  }

  // JPA
  // //////////////////////////////////////////////////////////////////////

  public void initJpa() {
    if (jpaPersistenceUnitName != null) {
      jpaEntityManagerFactory = JpaHelper.createEntityManagerFactory(jpaPersistenceUnitName);
    }
    if (jpaEntityManagerFactory != null) {
      sessionFactories.put(EntityManagerSession.class, new EntityManagerSessionFactory(jpaEntityManagerFactory, jpaHandleTransaction, jpaCloseEntityManager));
      VariableType jpaType = variableTypes.getVariableType(JPAEntityVariableType.TYPE_NAME);
      // Add JPA-type
      if (jpaType == null) {
        // We try adding the variable right before SerializableType, if
        // available
        int serializableIndex = variableTypes.getTypeIndex(SerializableType.TYPE_NAME);
        if (serializableIndex > -1) {
          variableTypes.addType(new JPAEntityVariableType(), serializableIndex);
        } else {
          variableTypes.addType(new JPAEntityVariableType());
        }
      }

      jpaType = variableTypes.getVariableType(JPAEntityListVariableType.TYPE_NAME);

      // Add JPA-list type after regular JPA type if not already present
      if (jpaType == null) {
        variableTypes.addType(new JPAEntityListVariableType(), variableTypes.getTypeIndex(JPAEntityVariableType.TYPE_NAME));
      }
    }
  }

  public void initBeans() {
    if (beans == null) {
      beans = new HashMap<Object, Object>();
    }
  }

  public void initEventDispatcher() {
    if (this.eventDispatcher == null) {
      this.eventDispatcher = new ActivitiEventDispatcherImpl();
    }

    this.eventDispatcher.setEnabled(enableEventDispatcher);

    if (eventListeners != null) {
      for (ActivitiEventListener listenerToAdd : eventListeners) {
        this.eventDispatcher.addEventListener(listenerToAdd);
      }
    }

    if (typedEventListeners != null) {
      for (Entry<String, List<ActivitiEventListener>> listenersToAdd : typedEventListeners.entrySet()) {
        // Extract types from the given string
        ActivitiEventType[] types = ActivitiEventType.getTypesFromString(listenersToAdd.getKey());

        for (ActivitiEventListener listenerToAdd : listenersToAdd.getValue()) {
          this.eventDispatcher.addEventListener(listenerToAdd, types);
        }
      }
    }

  }

  public void initProcessValidator() {
    if (this.processValidator == null) {
      this.processValidator = new ProcessValidatorFactory().createDefaultProcessValidator();
    }
  }

  public void initDatabaseEventLogging() {
    if (enableDatabaseEventLogging) {
      // Database event logging uses the default logging mechanism and adds
      // a specific event listener to the list of event listeners
      getEventDispatcher().addEventListener(new EventLogger(clock, objectMapper));
    }
  }

  /**
   * Called when the {@link ProcessEngine} is initialized, but before it is returned
   */
  protected void postProcessEngineInitialisation() {
    if (performanceSettings.isValidateExecutionRelationshipCountConfigOnBoot()) {
      commandExecutor.execute(new ValidateExecutionRelatedEntityCountCfgCmd());
    }
  }

  // getters and setters
  // //////////////////////////////////////////////////////

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

  @Override
public RepositoryService getRepositoryService() {
    return repositoryService;
  }

  public ProcessEngineConfigurationImpl setRepositoryService(RepositoryService repositoryService) {
    this.repositoryService = repositoryService;
    return this;
  }

  @Override
public RuntimeService getRuntimeService() {
    return runtimeService;
  }

  public ProcessEngineConfigurationImpl setRuntimeService(RuntimeService runtimeService) {
    this.runtimeService = runtimeService;
    return this;
  }

  @Override
public HistoryService getHistoryService() {
    return historyService;
  }

  public ProcessEngineConfigurationImpl setHistoryService(HistoryService historyService) {
    this.historyService = historyService;
    return this;
  }

  @Override
public TaskService getTaskService() {
    return taskService;
  }

  public ProcessEngineConfigurationImpl setTaskService(TaskService taskService) {
    this.taskService = taskService;
    return this;
  }

  @Override
public ManagementService getManagementService() {
    return managementService;
  }

  public ProcessEngineConfigurationImpl setManagementService(ManagementService managementService) {
    this.managementService = managementService;
    return this;
  }

  public DynamicBpmnService getDynamicBpmnService() {
    return dynamicBpmnService;
  }

  public ProcessEngineConfigurationImpl setDynamicBpmnService(DynamicBpmnService dynamicBpmnService) {
    this.dynamicBpmnService = dynamicBpmnService;
    return this;
  }

  public void setUserGroupManager(UserGroupManager userGroupManager) {
        this.userGroupManager = userGroupManager;
    }

    @Override
    public UserGroupManager getUserGroupManager() {
        return userGroupManager;
    }

    @Override
    public IntegrationContextManager getIntegrationContextManager() {
        if (integrationContextManager == null) {
            integrationContextManager = new IntegrationContextManagerImpl(this,
                                                                          getIntegrationContextDataManager());
        }
        return integrationContextManager;
    }

    private IntegrationContextDataManager getIntegrationContextDataManager() {
        if (integrationContextDataManager == null) {
            integrationContextDataManager = new MybatisIntegrationContextDataManager(this);
        }
        return integrationContextDataManager;
    }

    @Override
    public IntegrationContextService getIntegrationContextService() {
        if (integrationContextService == null) {
            integrationContextService = new IntegrationContextServiceImpl(commandExecutor);
        }
        return integrationContextService;
    }

  @Override
public ProcessEngineConfigurationImpl getProcessEngineConfiguration() {
    return this;
  }

  public Map<Class<?>, SessionFactory> getSessionFactories() {
    return sessionFactories;
  }

  public ProcessEngineConfigurationImpl setSessionFactories(Map<Class<?>, SessionFactory> sessionFactories) {
    this.sessionFactories = sessionFactories;
    return this;
  }

  public List<ProcessEngineConfigurator> getConfigurators() {
    return configurators;
  }

  public ProcessEngineConfigurationImpl addConfigurator(ProcessEngineConfigurator configurator) {
    if (this.configurators == null) {
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

  public ParsedDeploymentBuilderFactory getParsedDeploymentBuilderFactory() {
    return parsedDeploymentBuilderFactory;
  }

  public ProcessEngineConfigurationImpl setParsedDeploymentBuilderFactory(ParsedDeploymentBuilderFactory parsedDeploymentBuilderFactory) {
    this.parsedDeploymentBuilderFactory = parsedDeploymentBuilderFactory;
    return this;
  }

  public TimerManager getTimerManager() {
    return timerManager;
  }

  public void setTimerManager(TimerManager timerManager) {
    this.timerManager = timerManager;
  }

  public EventSubscriptionManager getEventSubscriptionManager() {
    return eventSubscriptionManager;
  }

  public void setEventSubscriptionManager(EventSubscriptionManager eventSubscriptionManager) {
    this.eventSubscriptionManager = eventSubscriptionManager;
  }

  public BpmnDeploymentHelper getBpmnDeploymentHelper() {
    return bpmnDeploymentHelper;
  }

  public ProcessEngineConfigurationImpl setBpmnDeploymentHelper(BpmnDeploymentHelper bpmnDeploymentHelper) {
    this.bpmnDeploymentHelper = bpmnDeploymentHelper;
    return this;
  }

  public CachingAndArtifactsManager getCachingAndArtifactsManager() {
    return cachingAndArtifactsManager;
  }

  public void setCachingAndArtifactsManager(CachingAndArtifactsManager cachingAndArtifactsManager) {
    this.cachingAndArtifactsManager = cachingAndArtifactsManager;
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

  public ProcessEngineConfiguration setWsOverridenEndpointAddresses(final ConcurrentMap<QName, URL> wsOverridenEndpointAddress) {
    this.wsOverridenEndpointAddresses.putAll(wsOverridenEndpointAddress);
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

  public boolean isSerializableVariableTypeTrackDeserializedObjects() {
    return serializableVariableTypeTrackDeserializedObjects;
  }

  public void setSerializableVariableTypeTrackDeserializedObjects(boolean serializableVariableTypeTrackDeserializedObjects) {
    this.serializableVariableTypeTrackDeserializedObjects = serializableVariableTypeTrackDeserializedObjects;
  }

  public boolean isSerializePOJOsInVariablesToJson() {
    return serializePOJOsInVariablesToJson;
  }

  public void setSerializePOJOsInVariablesToJson(boolean serializePOJOsInVariablesToJson) {
    this.serializePOJOsInVariablesToJson = serializePOJOsInVariablesToJson;
  }

  public String getJavaClassFieldForJackson() {
    return javaClassFieldForJackson;
  }

  public void setJavaClassFieldForJackson(String javaClassFieldForJackson) {
    this.javaClassFieldForJackson = javaClassFieldForJackson;
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

  public ProcessInstanceHelper getProcessInstanceHelper() {
    return processInstanceHelper;
  }

  public ProcessEngineConfigurationImpl setProcessInstanceHelper(ProcessInstanceHelper processInstanceHelper) {
    this.processInstanceHelper = processInstanceHelper;
    return this;
  }

  public ListenerNotificationHelper getListenerNotificationHelper() {
    return listenerNotificationHelper;
  }

  public ProcessEngineConfigurationImpl setListenerNotificationHelper(ListenerNotificationHelper listenerNotificationHelper) {
    this.listenerNotificationHelper = listenerNotificationHelper;
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

  public List<CustomFunctionProvider> getCustomFunctionProviders() {
    return customFunctionProviders;
  }

  public ProcessEngineConfigurationImpl setCustomFunctionProviders(List<CustomFunctionProvider> customFunctionProviders) {
    this.customFunctionProviders = customFunctionProviders;
    return this;
  }

  public ProcessEngineConfigurationImpl addCustomFunctionProvider(CustomFunctionProvider customFunctionProvider) {
    if (customFunctionProviders == null) {
      customFunctionProviders = new ArrayList<>();
    }
    customFunctionProviders.add(customFunctionProvider);
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

  public ProcessEngineConfigurationImpl setEventDispatcher(ActivitiEventDispatcher eventDispatcher) {
    this.eventDispatcher = eventDispatcher;
    return this;
  }

  public ProcessEngineConfigurationImpl setEnableEventDispatcher(boolean enableEventDispatcher) {
    this.enableEventDispatcher = enableEventDispatcher;
    return this;
  }

  public Map<String, List<ActivitiEventListener>> getTypedEventListeners() {
    return typedEventListeners;
  }

  public ProcessEngineConfigurationImpl setTypedEventListeners(Map<String, List<ActivitiEventListener>> typedListeners) {
    this.typedEventListeners = typedListeners;
    return this;
  }

  public List<ActivitiEventListener> getEventListeners() {
    return eventListeners;
  }

  public ProcessEngineConfigurationImpl setEventListeners(List<ActivitiEventListener> eventListeners) {
    this.eventListeners = eventListeners;
    return this;
  }

  public ProcessValidator getProcessValidator() {
    return processValidator;
  }

  public ProcessEngineConfigurationImpl setProcessValidator(ProcessValidator processValidator) {
    this.processValidator = processValidator;
    return this;
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

  public boolean isBulkInsertEnabled() {
    return isBulkInsertEnabled;
  }

  public ProcessEngineConfigurationImpl setBulkInsertEnabled(boolean isBulkInsertEnabled) {
    this.isBulkInsertEnabled = isBulkInsertEnabled;
    return this;
  }

  public int getMaxNrOfStatementsInBulkInsert() {
    return maxNrOfStatementsInBulkInsert;
  }

  public ProcessEngineConfigurationImpl setMaxNrOfStatementsInBulkInsert(int maxNrOfStatementsInBulkInsert) {
    this.maxNrOfStatementsInBulkInsert = maxNrOfStatementsInBulkInsert;
    return this;
  }

  public boolean isUsingRelationalDatabase() {
    return usingRelationalDatabase;
  }

  public ProcessEngineConfigurationImpl setUsingRelationalDatabase(boolean usingRelationalDatabase) {
    this.usingRelationalDatabase = usingRelationalDatabase;
    return this;
  }

  public boolean isEnableVerboseExecutionTreeLogging() {
    return enableVerboseExecutionTreeLogging;
  }

  public ProcessEngineConfigurationImpl setEnableVerboseExecutionTreeLogging(boolean enableVerboseExecutionTreeLogging) {
    this.enableVerboseExecutionTreeLogging = enableVerboseExecutionTreeLogging;
    return this;
  }

  public ProcessEngineConfigurationImpl setEnableEagerExecutionTreeFetching(boolean enableEagerExecutionTreeFetching) {
    this.performanceSettings.setEnableEagerExecutionTreeFetching(enableEagerExecutionTreeFetching);
    return this;
  }

  public ProcessEngineConfigurationImpl setEnableExecutionRelationshipCounts(boolean enableExecutionRelationshipCounts) {
    this.performanceSettings.setEnableExecutionRelationshipCounts(enableExecutionRelationshipCounts);
    return this;
  }

  public PerformanceSettings getPerformanceSettings() {
    return performanceSettings;
  }

  public void setPerformanceSettings(PerformanceSettings performanceSettings) {
    this.performanceSettings = performanceSettings;
  }

  public ProcessEngineConfigurationImpl setEnableLocalization(boolean enableLocalization) {
    this.performanceSettings.setEnableLocalization(enableLocalization);
    return this;
  }

  public AttachmentDataManager getAttachmentDataManager() {
    return attachmentDataManager;
  }

  public ProcessEngineConfigurationImpl setAttachmentDataManager(AttachmentDataManager attachmentDataManager) {
    this.attachmentDataManager = attachmentDataManager;
    return this;
  }

  public ByteArrayDataManager getByteArrayDataManager() {
    return byteArrayDataManager;
  }

  public ProcessEngineConfigurationImpl setByteArrayDataManager(ByteArrayDataManager byteArrayDataManager) {
    this.byteArrayDataManager = byteArrayDataManager;
    return this;
  }

  public CommentDataManager getCommentDataManager() {
    return commentDataManager;
  }

  public ProcessEngineConfigurationImpl setCommentDataManager(CommentDataManager commentDataManager) {
    this.commentDataManager = commentDataManager;
    return this;
  }

  public DeploymentDataManager getDeploymentDataManager() {
    return deploymentDataManager;
  }

  public ProcessEngineConfigurationImpl setDeploymentDataManager(DeploymentDataManager deploymentDataManager) {
    this.deploymentDataManager = deploymentDataManager;
    return this;
  }

  public EventLogEntryDataManager getEventLogEntryDataManager() {
    return eventLogEntryDataManager;
  }

  public ProcessEngineConfigurationImpl setEventLogEntryDataManager(EventLogEntryDataManager eventLogEntryDataManager) {
    this.eventLogEntryDataManager = eventLogEntryDataManager;
    return this;
  }

  public EventSubscriptionDataManager getEventSubscriptionDataManager() {
    return eventSubscriptionDataManager;
  }

  public ProcessEngineConfigurationImpl setEventSubscriptionDataManager(EventSubscriptionDataManager eventSubscriptionDataManager) {
    this.eventSubscriptionDataManager = eventSubscriptionDataManager;
    return this;
  }

  public ExecutionDataManager getExecutionDataManager() {
    return executionDataManager;
  }

  public ProcessEngineConfigurationImpl setExecutionDataManager(ExecutionDataManager executionDataManager) {
    this.executionDataManager = executionDataManager;
    return this;
  }

  public HistoricActivityInstanceDataManager getHistoricActivityInstanceDataManager() {
    return historicActivityInstanceDataManager;
  }

  public ProcessEngineConfigurationImpl setHistoricActivityInstanceDataManager(HistoricActivityInstanceDataManager historicActivityInstanceDataManager) {
    this.historicActivityInstanceDataManager = historicActivityInstanceDataManager;
    return this;
  }

  public HistoricDetailDataManager getHistoricDetailDataManager() {
    return historicDetailDataManager;
  }

  public ProcessEngineConfigurationImpl setHistoricDetailDataManager(HistoricDetailDataManager historicDetailDataManager) {
    this.historicDetailDataManager = historicDetailDataManager;
    return this;
  }

  public HistoricIdentityLinkDataManager getHistoricIdentityLinkDataManager() {
    return historicIdentityLinkDataManager;
  }

  public ProcessEngineConfigurationImpl setHistoricIdentityLinkDataManager(HistoricIdentityLinkDataManager historicIdentityLinkDataManager) {
    this.historicIdentityLinkDataManager = historicIdentityLinkDataManager;
    return this;
  }

  public HistoricProcessInstanceDataManager getHistoricProcessInstanceDataManager() {
    return historicProcessInstanceDataManager;
  }

  public ProcessEngineConfigurationImpl setHistoricProcessInstanceDataManager(HistoricProcessInstanceDataManager historicProcessInstanceDataManager) {
    this.historicProcessInstanceDataManager = historicProcessInstanceDataManager;
    return this;
  }

  public HistoricTaskInstanceDataManager getHistoricTaskInstanceDataManager() {
    return historicTaskInstanceDataManager;
  }

  public ProcessEngineConfigurationImpl setHistoricTaskInstanceDataManager(HistoricTaskInstanceDataManager historicTaskInstanceDataManager) {
    this.historicTaskInstanceDataManager = historicTaskInstanceDataManager;
    return this;
  }

  public HistoricVariableInstanceDataManager getHistoricVariableInstanceDataManager() {
    return historicVariableInstanceDataManager;
  }

  public ProcessEngineConfigurationImpl setHistoricVariableInstanceDataManager(HistoricVariableInstanceDataManager historicVariableInstanceDataManager) {
    this.historicVariableInstanceDataManager = historicVariableInstanceDataManager;
    return this;
  }

  public IdentityLinkDataManager getIdentityLinkDataManager() {
    return identityLinkDataManager;
  }

  public ProcessEngineConfigurationImpl setIdentityLinkDataManager(IdentityLinkDataManager identityLinkDataManager) {
    this.identityLinkDataManager = identityLinkDataManager;
    return this;
  }

  public JobDataManager getJobDataManager() {
    return jobDataManager;
  }

  public ProcessEngineConfigurationImpl setJobDataManager(JobDataManager jobDataManager) {
    this.jobDataManager = jobDataManager;
    return this;
  }

  public TimerJobDataManager getTimerJobDataManager() {
    return timerJobDataManager;
  }

  public ProcessEngineConfigurationImpl setTimerJobDataManager(TimerJobDataManager timerJobDataManager) {
    this.timerJobDataManager = timerJobDataManager;
    return this;
  }

  public SuspendedJobDataManager getSuspendedJobDataManager() {
    return suspendedJobDataManager;
  }

  public ProcessEngineConfigurationImpl setSuspendedJobDataManager(SuspendedJobDataManager suspendedJobDataManager) {
    this.suspendedJobDataManager = suspendedJobDataManager;
    return this;
  }

  public DeadLetterJobDataManager getDeadLetterJobDataManager() {
    return deadLetterJobDataManager;
  }

  public ProcessEngineConfigurationImpl setDeadLetterJobDataManager(DeadLetterJobDataManager deadLetterJobDataManager) {
    this.deadLetterJobDataManager = deadLetterJobDataManager;
    return this;
  }


  public ModelDataManager getModelDataManager() {
    return modelDataManager;
  }

  public ProcessEngineConfigurationImpl setModelDataManager(ModelDataManager modelDataManager) {
    this.modelDataManager = modelDataManager;
    return this;
  }

  public ProcessDefinitionDataManager getProcessDefinitionDataManager() {
    return processDefinitionDataManager;
  }

  public ProcessEngineConfigurationImpl setProcessDefinitionDataManager(ProcessDefinitionDataManager processDefinitionDataManager) {
    this.processDefinitionDataManager = processDefinitionDataManager;
    return this;
  }

  public ProcessDefinitionInfoDataManager getProcessDefinitionInfoDataManager() {
    return processDefinitionInfoDataManager;
  }

  public ProcessEngineConfigurationImpl setProcessDefinitionInfoDataManager(ProcessDefinitionInfoDataManager processDefinitionInfoDataManager) {
    this.processDefinitionInfoDataManager = processDefinitionInfoDataManager;
    return this;
  }

  public PropertyDataManager getPropertyDataManager() {
    return propertyDataManager;
  }

  public ProcessEngineConfigurationImpl setPropertyDataManager(PropertyDataManager propertyDataManager) {
    this.propertyDataManager = propertyDataManager;
    return this;
  }

  public ResourceDataManager getResourceDataManager() {
    return resourceDataManager;
  }

  public ProcessEngineConfigurationImpl setResourceDataManager(ResourceDataManager resourceDataManager) {
    this.resourceDataManager = resourceDataManager;
    return this;
  }

  public TaskDataManager getTaskDataManager() {
    return taskDataManager;
  }

  public ProcessEngineConfigurationImpl setTaskDataManager(TaskDataManager taskDataManager) {
    this.taskDataManager = taskDataManager;
    return this;
  }

  public VariableInstanceDataManager getVariableInstanceDataManager() {
    return variableInstanceDataManager;
  }

  public ProcessEngineConfigurationImpl setVariableInstanceDataManager(VariableInstanceDataManager variableInstanceDataManager) {
    this.variableInstanceDataManager = variableInstanceDataManager;
    return this;
  }

  public boolean isEnableConfiguratorServiceLoader() {
    return enableConfiguratorServiceLoader;
  }

  public AttachmentEntityManager getAttachmentEntityManager() {
    return attachmentEntityManager;
  }

  public ProcessEngineConfigurationImpl setAttachmentEntityManager(AttachmentEntityManager attachmentEntityManager) {
    this.attachmentEntityManager = attachmentEntityManager;
    return this;
  }

  public ByteArrayEntityManager getByteArrayEntityManager() {
    return byteArrayEntityManager;
  }

  public ProcessEngineConfigurationImpl setByteArrayEntityManager(ByteArrayEntityManager byteArrayEntityManager) {
    this.byteArrayEntityManager = byteArrayEntityManager;
    return this;
  }

  public CommentEntityManager getCommentEntityManager() {
    return commentEntityManager;
  }

  public ProcessEngineConfigurationImpl setCommentEntityManager(CommentEntityManager commentEntityManager) {
    this.commentEntityManager = commentEntityManager;
    return this;
  }

  public DeploymentEntityManager getDeploymentEntityManager() {
    return deploymentEntityManager;
  }

  public ProcessEngineConfigurationImpl setDeploymentEntityManager(DeploymentEntityManager deploymentEntityManager) {
    this.deploymentEntityManager = deploymentEntityManager;
    return this;
  }

  public EventLogEntryEntityManager getEventLogEntryEntityManager() {
    return eventLogEntryEntityManager;
  }

  public ProcessEngineConfigurationImpl setEventLogEntryEntityManager(EventLogEntryEntityManager eventLogEntryEntityManager) {
    this.eventLogEntryEntityManager = eventLogEntryEntityManager;
    return this;
  }

  public EventSubscriptionEntityManager getEventSubscriptionEntityManager() {
    return eventSubscriptionEntityManager;
  }

  public ProcessEngineConfigurationImpl setEventSubscriptionEntityManager(EventSubscriptionEntityManager eventSubscriptionEntityManager) {
    this.eventSubscriptionEntityManager = eventSubscriptionEntityManager;
    return this;
  }

  public ExecutionEntityManager getExecutionEntityManager() {
    return executionEntityManager;
  }

  public ProcessEngineConfigurationImpl setExecutionEntityManager(ExecutionEntityManager executionEntityManager) {
    this.executionEntityManager = executionEntityManager;
    return this;
  }

  public HistoricActivityInstanceEntityManager getHistoricActivityInstanceEntityManager() {
    return historicActivityInstanceEntityManager;
  }

  public ProcessEngineConfigurationImpl setHistoricActivityInstanceEntityManager(HistoricActivityInstanceEntityManager historicActivityInstanceEntityManager) {
    this.historicActivityInstanceEntityManager = historicActivityInstanceEntityManager;
    return this;
  }

  public HistoricDetailEntityManager getHistoricDetailEntityManager() {
    return historicDetailEntityManager;
  }

  public ProcessEngineConfigurationImpl setHistoricDetailEntityManager(HistoricDetailEntityManager historicDetailEntityManager) {
    this.historicDetailEntityManager = historicDetailEntityManager;
    return this;
  }

  public HistoricIdentityLinkEntityManager getHistoricIdentityLinkEntityManager() {
    return historicIdentityLinkEntityManager;
  }

  public ProcessEngineConfigurationImpl setHistoricIdentityLinkEntityManager(HistoricIdentityLinkEntityManager historicIdentityLinkEntityManager) {
    this.historicIdentityLinkEntityManager = historicIdentityLinkEntityManager;
    return this;
  }

  public HistoricProcessInstanceEntityManager getHistoricProcessInstanceEntityManager() {
    return historicProcessInstanceEntityManager;
  }

  public ProcessEngineConfigurationImpl setHistoricProcessInstanceEntityManager(HistoricProcessInstanceEntityManager historicProcessInstanceEntityManager) {
    this.historicProcessInstanceEntityManager = historicProcessInstanceEntityManager;
    return this;
  }

  public HistoricTaskInstanceEntityManager getHistoricTaskInstanceEntityManager() {
    return historicTaskInstanceEntityManager;
  }

  public ProcessEngineConfigurationImpl setHistoricTaskInstanceEntityManager(HistoricTaskInstanceEntityManager historicTaskInstanceEntityManager) {
    this.historicTaskInstanceEntityManager = historicTaskInstanceEntityManager;
    return this;
  }

  public HistoricVariableInstanceEntityManager getHistoricVariableInstanceEntityManager() {
    return historicVariableInstanceEntityManager;
  }

  public ProcessEngineConfigurationImpl setHistoricVariableInstanceEntityManager(HistoricVariableInstanceEntityManager historicVariableInstanceEntityManager) {
    this.historicVariableInstanceEntityManager = historicVariableInstanceEntityManager;
    return this;
  }

  public IdentityLinkEntityManager getIdentityLinkEntityManager() {
    return identityLinkEntityManager;
  }

  public ProcessEngineConfigurationImpl setIdentityLinkEntityManager(IdentityLinkEntityManager identityLinkEntityManager) {
    this.identityLinkEntityManager = identityLinkEntityManager;
    return this;
  }

  public JobEntityManager getJobEntityManager() {
    return jobEntityManager;
  }

  public ProcessEngineConfigurationImpl setJobEntityManager(JobEntityManager jobEntityManager) {
    this.jobEntityManager = jobEntityManager;
    return this;
  }

  public TimerJobEntityManager getTimerJobEntityManager() {
    return timerJobEntityManager;
  }

  public ProcessEngineConfigurationImpl setTimerJobEntityManager(TimerJobEntityManager timerJobEntityManager) {
    this.timerJobEntityManager = timerJobEntityManager;
    return this;
  }

  public SuspendedJobEntityManager getSuspendedJobEntityManager() {
    return suspendedJobEntityManager;
  }

  public ProcessEngineConfigurationImpl setSuspendedJobEntityManager(SuspendedJobEntityManager suspendedJobEntityManager) {
    this.suspendedJobEntityManager = suspendedJobEntityManager;
    return this;
  }

  public DeadLetterJobEntityManager getDeadLetterJobEntityManager() {
    return deadLetterJobEntityManager;
  }

  public ProcessEngineConfigurationImpl setDeadLetterJobEntityManager(DeadLetterJobEntityManager deadLetterJobEntityManager) {
    this.deadLetterJobEntityManager = deadLetterJobEntityManager;
    return this;
  }


  public ModelEntityManager getModelEntityManager() {
    return modelEntityManager;
  }

  public ProcessEngineConfigurationImpl setModelEntityManager(ModelEntityManager modelEntityManager) {
    this.modelEntityManager = modelEntityManager;
    return this;
  }

  public ProcessDefinitionEntityManager getProcessDefinitionEntityManager() {
    return processDefinitionEntityManager;
  }

  public ProcessEngineConfigurationImpl setProcessDefinitionEntityManager(ProcessDefinitionEntityManager processDefinitionEntityManager) {
    this.processDefinitionEntityManager = processDefinitionEntityManager;
    return this;
  }

  public ProcessDefinitionInfoEntityManager getProcessDefinitionInfoEntityManager() {
    return processDefinitionInfoEntityManager;
  }

  public ProcessEngineConfigurationImpl setProcessDefinitionInfoEntityManager(ProcessDefinitionInfoEntityManager processDefinitionInfoEntityManager) {
    this.processDefinitionInfoEntityManager = processDefinitionInfoEntityManager;
    return this;
  }

  public PropertyEntityManager getPropertyEntityManager() {
    return propertyEntityManager;
  }

  public ProcessEngineConfigurationImpl setPropertyEntityManager(PropertyEntityManager propertyEntityManager) {
    this.propertyEntityManager = propertyEntityManager;
    return this;
  }

  public ResourceEntityManager getResourceEntityManager() {
    return resourceEntityManager;
  }

  public ProcessEngineConfigurationImpl setResourceEntityManager(ResourceEntityManager resourceEntityManager) {
    this.resourceEntityManager = resourceEntityManager;
    return this;
  }

  public TaskEntityManager getTaskEntityManager() {
    return taskEntityManager;
  }

  public ProcessEngineConfigurationImpl setTaskEntityManager(TaskEntityManager taskEntityManager) {
    this.taskEntityManager = taskEntityManager;
    return this;
  }

  public VariableInstanceEntityManager getVariableInstanceEntityManager() {
    return variableInstanceEntityManager;
  }

  public ProcessEngineConfigurationImpl setVariableInstanceEntityManager(VariableInstanceEntityManager variableInstanceEntityManager) {
    this.variableInstanceEntityManager = variableInstanceEntityManager;
    return this;
  }

  public TableDataManager getTableDataManager() {
    return tableDataManager;
  }

  public ProcessEngineConfigurationImpl setTableDataManager(TableDataManager tableDataManager) {
    this.tableDataManager = tableDataManager;
    return this;
  }

  public HistoryManager getHistoryManager() {
    return historyManager;
  }

  public ProcessEngineConfigurationImpl setHistoryManager(HistoryManager historyManager) {
    this.historyManager = historyManager;
    return this;
  }

  public JobManager getJobManager() {
    return jobManager;
  }

  public ProcessEngineConfigurationImpl setJobManager(JobManager jobManager) {
    this.jobManager = jobManager;
    return this;
  }

  @Override
  public ProcessEngineConfigurationImpl setClock(Clock clock) {
    if (this.clock == null) {
      this.clock = clock;
    } else {
      this.clock.setCurrentCalendar(clock.getCurrentCalendar());
    }
    return this;
  }

  public void resetClock() {
    if (this.clock != null) {
      clock.reset();
    }
  }

  public DelegateExpressionFieldInjectionMode getDelegateExpressionFieldInjectionMode() {
    return delegateExpressionFieldInjectionMode;
  }

  public ProcessEngineConfigurationImpl setDelegateExpressionFieldInjectionMode(DelegateExpressionFieldInjectionMode delegateExpressionFieldInjectionMode) {
    this.delegateExpressionFieldInjectionMode = delegateExpressionFieldInjectionMode;
    return this;
  }

  public ObjectMapper getObjectMapper() {
    return objectMapper;
  }

  public ProcessEngineConfigurationImpl setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    return this;
  }

  public int getAsyncExecutorCorePoolSize() {
    return asyncExecutorCorePoolSize;
  }

  public ProcessEngineConfigurationImpl setAsyncExecutorCorePoolSize(int asyncExecutorCorePoolSize) {
    this.asyncExecutorCorePoolSize = asyncExecutorCorePoolSize;
    return this;
  }

  public int getAsyncExecutorNumberOfRetries() {
    return asyncExecutorNumberOfRetries;
  }

  public ProcessEngineConfigurationImpl setAsyncExecutorNumberOfRetries(int asyncExecutorNumberOfRetries) {
    this.asyncExecutorNumberOfRetries = asyncExecutorNumberOfRetries;
    return this;
  }

  public int getAsyncExecutorMaxPoolSize() {
    return asyncExecutorMaxPoolSize;
  }

  public ProcessEngineConfigurationImpl setAsyncExecutorMaxPoolSize(int asyncExecutorMaxPoolSize) {
    this.asyncExecutorMaxPoolSize = asyncExecutorMaxPoolSize;
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

  public ProcessEngineConfigurationImpl setAsyncExecutorDefaultTimerJobAcquireWaitTime(int asyncExecutorDefaultTimerJobAcquireWaitTime) {
    this.asyncExecutorDefaultTimerJobAcquireWaitTime = asyncExecutorDefaultTimerJobAcquireWaitTime;
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

  public int getAsyncExecutorResetExpiredJobsInterval() {
    return asyncExecutorResetExpiredJobsInterval;
  }

  public ProcessEngineConfigurationImpl setAsyncExecutorResetExpiredJobsInterval(int asyncExecutorResetExpiredJobsInterval) {
    this.asyncExecutorResetExpiredJobsInterval = asyncExecutorResetExpiredJobsInterval;
    return this;
  }

  public ExecuteAsyncRunnableFactory getAsyncExecutorExecuteAsyncRunnableFactory() {
    return asyncExecutorExecuteAsyncRunnableFactory;
  }

  public ProcessEngineConfigurationImpl setAsyncExecutorExecuteAsyncRunnableFactory(ExecuteAsyncRunnableFactory asyncExecutorExecuteAsyncRunnableFactory) {
    this.asyncExecutorExecuteAsyncRunnableFactory = asyncExecutorExecuteAsyncRunnableFactory;
    return this;
  }

  public int getAsyncExecutorResetExpiredJobsPageSize() {
    return asyncExecutorResetExpiredJobsPageSize;
  }

  public ProcessEngineConfigurationImpl setAsyncExecutorResetExpiredJobsPageSize(int asyncExecutorResetExpiredJobsPageSize) {
    this.asyncExecutorResetExpiredJobsPageSize = asyncExecutorResetExpiredJobsPageSize;
    return this;
  }

  public boolean isAsyncExecutorIsMessageQueueMode() {
    return asyncExecutorMessageQueueMode;
  }

  public ProcessEngineConfigurationImpl setAsyncExecutorMessageQueueMode(boolean asyncExecutorMessageQueueMode) {
    this.asyncExecutorMessageQueueMode = asyncExecutorMessageQueueMode;
    return this;
  }

  public boolean isRollbackDeployment() {
      return isRollbackDeployment;
  }

  public void setRollbackDeployment(boolean rollbackDeployment) {
      isRollbackDeployment = rollbackDeployment;
  }

    public EventSubscriptionPayloadMappingProvider getEventSubscriptionPayloadMappingProvider() {
    return eventSubscriptionPayloadMappingProvider;
  }

  public void setEventSubscriptionPayloadMappingProvider(EventSubscriptionPayloadMappingProvider eventSubscriptionPayloadMappingProvider) {
    this.eventSubscriptionPayloadMappingProvider = eventSubscriptionPayloadMappingProvider;
  }

    public ProcessDefinitionHelper getProcessDefinitionHelper() {
        return processDefinitionHelper;
    }

    public ProcessEngineConfigurationImpl setProcessDefinitionHelper(ProcessDefinitionHelper processDefinitionHelper) {
        this.processDefinitionHelper = processDefinitionHelper;
        return this;
    }
}

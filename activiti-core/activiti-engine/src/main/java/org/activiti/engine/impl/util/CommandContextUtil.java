package org.activiti.engine.impl.util;


import org.activiti.engine.ActivitiEngineAgenda;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.event.ActivitiEventDispatcher;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.history.HistoryManager;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.jobexecutor.FailedJobCommandFactory;
import org.activiti.engine.impl.persistence.cache.EntityCache;
import org.activiti.engine.impl.persistence.entity.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author LoveMyOrange
 */
public class CommandContextUtil {

    public static final String ATTRIBUTE_INVOLVED_EXECUTIONS = "ctx.attribute.involvedExecutions";

    public static ProcessEngineConfigurationImpl getProcessEngineConfiguration() {
        return getProcessEngineConfiguration(getCommandContext());
    }

    public static ProcessEngineConfigurationImpl getProcessEngineConfiguration(CommandContext commandContext) {
        if (commandContext != null) {
            return (ProcessEngineConfigurationImpl) commandContext.getProcessEngineConfiguration().getProcessEngineConfiguration();
        }
        return null;
    }


    public static TaskService getTaskService() {
        return getTaskService(getCommandContext());
    }

    public static TaskService getTaskService(CommandContext commandContext) {
        return getProcessEngineConfiguration(commandContext).getTaskService();
    }

    public static ActivitiEngineAgenda getAgenda() {
        return getAgenda(getCommandContext());
    }

    public static ActivitiEngineAgenda getAgenda(CommandContext commandContext) {
        return commandContext.getAgenda();
        //return commandContext.getSession(ActivitiEngineAgenda.class);
    }

    public static DbSqlSession getDbSqlSession() {
        return getDbSqlSession(getCommandContext());
    }

    public static DbSqlSession getDbSqlSession(CommandContext commandContext) {
        return commandContext.getSession(DbSqlSession.class);
    }

    public static EntityCache getEntityCache() {
        return getEntityCache(getCommandContext());
    }

    public static EntityCache getEntityCache(CommandContext commandContext) {
        return commandContext.getSession(EntityCache.class);
    }

    @SuppressWarnings("unchecked")
    public static void addInvolvedExecution(CommandContext commandContext, ExecutionEntity executionEntity) {
        if (executionEntity.getId() != null) {
            Map<String, ExecutionEntity> involvedExecutions = null;
            Object obj = commandContext.getAttribute(ATTRIBUTE_INVOLVED_EXECUTIONS);
            if (obj != null) {
                involvedExecutions = (Map<String, ExecutionEntity>) obj;
            } else {
                involvedExecutions = new HashMap<>();
                commandContext.addAttribute(ATTRIBUTE_INVOLVED_EXECUTIONS, involvedExecutions);
            }
            involvedExecutions.put(executionEntity.getId(), executionEntity);
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, ExecutionEntity> getInvolvedExecutions(CommandContext commandContext) {
        Object obj = commandContext.getAttribute(ATTRIBUTE_INVOLVED_EXECUTIONS);
        if (obj != null) {
            return (Map<String, ExecutionEntity>) obj;
        }
        return null;
    }

    public static boolean hasInvolvedExecutions(CommandContext commandContext) {
        return getInvolvedExecutions(commandContext) != null;
    }

    public static TableDataManager getTableDataManager() {
        return getTableDataManager(getCommandContext());
    }

    public static TableDataManager getTableDataManager(CommandContext commandContext) {
        return getProcessEngineConfiguration(commandContext).getTableDataManager();
    }

    public static ByteArrayEntityManager getByteArrayEntityManager() {
        return getByteArrayEntityManager(getCommandContext());
    }

    public static ByteArrayEntityManager getByteArrayEntityManager(CommandContext commandContext) {
        return getProcessEngineConfiguration(commandContext).getByteArrayEntityManager();
    }

    public static ResourceEntityManager getResourceEntityManager() {
        return getResourceEntityManager(getCommandContext());
    }

    public static ResourceEntityManager getResourceEntityManager(CommandContext commandContext) {
        return getProcessEngineConfiguration(commandContext).getResourceEntityManager();
    }

    public static DeploymentEntityManager getDeploymentEntityManager() {
        return getDeploymentEntityManager(getCommandContext());
    }

    public static DeploymentEntityManager getDeploymentEntityManager(CommandContext commandContext) {
        return getProcessEngineConfiguration(commandContext).getDeploymentEntityManager();
    }

    public static PropertyEntityManager getPropertyEntityManager() {
        return getPropertyEntityManager(getCommandContext());
    }

    public static PropertyEntityManager getPropertyEntityManager(CommandContext commandContext) {
        return getProcessEngineConfiguration(commandContext).getPropertyEntityManager();
    }

    public static ProcessDefinitionEntityManager getProcessDefinitionEntityManager() {
        return getProcessDefinitionEntityManager(getCommandContext());
    }

    public static ProcessDefinitionEntityManager getProcessDefinitionEntityManager(CommandContext commandContext) {
        return getProcessEngineConfiguration(commandContext).getProcessDefinitionEntityManager();
    }

    public static ProcessDefinitionInfoEntityManager getProcessDefinitionInfoEntityManager() {
        return getProcessDefinitionInfoEntityManager(getCommandContext());
    }

    public static ProcessDefinitionInfoEntityManager getProcessDefinitionInfoEntityManager(CommandContext commandContext) {
        return getProcessEngineConfiguration(commandContext).getProcessDefinitionInfoEntityManager();
    }

    public static ExecutionEntityManager getExecutionEntityManager() {
        return getExecutionEntityManager(getCommandContext());
    }

    public static ExecutionEntityManager getExecutionEntityManager(CommandContext commandContext) {
        return getProcessEngineConfiguration(commandContext).getExecutionEntityManager();
    }

    public static CommentEntityManager getCommentEntityManager() {
        return getCommentEntityManager(getCommandContext());
    }

    public static CommentEntityManager getCommentEntityManager(CommandContext commandContext) {
        return getProcessEngineConfiguration(commandContext).getCommentEntityManager();
    }

    public static ModelEntityManager getModelEntityManager() {
        return getModelEntityManager(getCommandContext());
    }

    public static ModelEntityManager getModelEntityManager(CommandContext commandContext) {
        return getProcessEngineConfiguration(commandContext).getModelEntityManager();
    }

    public static HistoryManager getHistoryManager() {
        return getHistoryManager(getCommandContext());
    }

    public static HistoricProcessInstanceEntityManager getHistoricProcessInstanceEntityManager() {
        return getHistoricProcessInstanceEntityManager(getCommandContext());
    }

    public static HistoricProcessInstanceEntityManager getHistoricProcessInstanceEntityManager(CommandContext commandContext) {
        return getProcessEngineConfiguration(commandContext).getHistoricProcessInstanceEntityManager();
    }

    public static HistoricActivityInstanceEntityManager getActivityInstanceEntityManager() {
        return getActivityInstanceEntityManager(getCommandContext());
    }

    public static HistoricActivityInstanceEntityManager getActivityInstanceEntityManager(CommandContext commandContext) {
        return getProcessEngineConfiguration(commandContext).getHistoricActivityInstanceEntityManager();
    }

    public static HistoricActivityInstanceEntityManager getHistoricActivityInstanceEntityManager() {
        return getHistoricActivityInstanceEntityManager(getCommandContext());
    }

    public static HistoricActivityInstanceEntityManager getHistoricActivityInstanceEntityManager(CommandContext commandContext) {
        return getProcessEngineConfiguration(commandContext).getHistoricActivityInstanceEntityManager();
    }

    public static HistoryManager getHistoryManager(CommandContext commandContext) {
        return getProcessEngineConfiguration(commandContext).getHistoryManager();
    }

    public static HistoricDetailEntityManager getHistoricDetailEntityManager() {
        return getHistoricDetailEntityManager(getCommandContext());
    }

    public static HistoricDetailEntityManager getHistoricDetailEntityManager(CommandContext commandContext) {
        return getProcessEngineConfiguration(commandContext).getHistoricDetailEntityManager();
    }

    public static AttachmentEntityManager getAttachmentEntityManager() {
        return getAttachmentEntityManager(getCommandContext());
    }

    public static AttachmentEntityManager getAttachmentEntityManager(CommandContext commandContext) {
        return getProcessEngineConfiguration(commandContext).getAttachmentEntityManager();
    }

    public static EventLogEntryEntityManager getEventLogEntryEntityManager() {
        return getEventLogEntryEntityManager(getCommandContext());
    }

    public static EventLogEntryEntityManager getEventLogEntryEntityManager(CommandContext commandContext) {
        return getProcessEngineConfiguration(commandContext).getEventLogEntryEntityManager();
    }

    public static ActivitiEventDispatcher getEventDispatcher() {
        return getEventDispatcher(getCommandContext());
    }

    public static ActivitiEventDispatcher getEventDispatcher(CommandContext commandContext) {
        return getProcessEngineConfiguration(commandContext).getEventDispatcher();
    }

    public static FailedJobCommandFactory getFailedJobCommandFactory() {
        return getFailedJobCommandFactory(getCommandContext());
    }

    public static FailedJobCommandFactory getFailedJobCommandFactory(CommandContext commandContext) {
        return getProcessEngineConfiguration(commandContext).getFailedJobCommandFactory();
    }

    public static ProcessInstanceHelper getProcessInstanceHelper() {
        return getProcessInstanceHelper(getCommandContext());
    }

    public static ProcessInstanceHelper getProcessInstanceHelper(CommandContext commandContext) {
        return getProcessEngineConfiguration(commandContext).getProcessInstanceHelper();
    }

    public static CommandContext getCommandContext() {
        return Context.getCommandContext();
    }
}


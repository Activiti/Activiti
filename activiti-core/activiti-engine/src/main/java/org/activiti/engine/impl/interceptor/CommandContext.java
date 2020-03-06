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
package org.activiti.engine.impl.interceptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiEngineAgenda;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiOptimisticLockingException;
import org.activiti.engine.ActivitiTaskAlreadyClaimedException;
import org.activiti.engine.JobNotFoundException;
import org.activiti.engine.delegate.event.ActivitiEventDispatcher;
import org.activiti.engine.impl.asyncexecutor.JobManager;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.history.HistoryManager;
import org.activiti.engine.impl.jobexecutor.FailedJobCommandFactory;
import org.activiti.engine.impl.persistence.cache.EntityCache;
import org.activiti.engine.impl.persistence.entity.AttachmentEntityManager;
import org.activiti.engine.impl.persistence.entity.ByteArrayEntityManager;
import org.activiti.engine.impl.persistence.entity.CommentEntityManager;
import org.activiti.engine.impl.persistence.entity.DeadLetterJobEntityManager;
import org.activiti.engine.impl.persistence.entity.DeploymentEntityManager;
import org.activiti.engine.impl.persistence.entity.EventLogEntryEntityManager;
import org.activiti.engine.impl.persistence.entity.EventSubscriptionEntityManager;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.activiti.engine.impl.persistence.entity.HistoricActivityInstanceEntityManager;
import org.activiti.engine.impl.persistence.entity.HistoricDetailEntityManager;
import org.activiti.engine.impl.persistence.entity.HistoricIdentityLinkEntityManager;
import org.activiti.engine.impl.persistence.entity.HistoricProcessInstanceEntityManager;
import org.activiti.engine.impl.persistence.entity.HistoricTaskInstanceEntityManager;
import org.activiti.engine.impl.persistence.entity.HistoricVariableInstanceEntityManager;
import org.activiti.engine.impl.persistence.entity.IdentityLinkEntityManager;
import org.activiti.engine.impl.persistence.entity.JobEntityManager;
import org.activiti.engine.impl.persistence.entity.ModelEntityManager;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntityManager;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionInfoEntityManager;
import org.activiti.engine.impl.persistence.entity.PropertyEntityManager;
import org.activiti.engine.impl.persistence.entity.ResourceEntityManager;
import org.activiti.engine.impl.persistence.entity.SuspendedJobEntityManager;
import org.activiti.engine.impl.persistence.entity.TableDataManager;
import org.activiti.engine.impl.persistence.entity.TaskEntityManager;
import org.activiti.engine.impl.persistence.entity.TimerJobEntityManager;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntityManager;
import org.activiti.engine.logging.LogMDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandContext {

    private static Logger log = LoggerFactory.getLogger(CommandContext.class);

    protected Command<?> command;
    protected Map<Class<?>, SessionFactory> sessionFactories;
    protected Map<Class<?>, Session> sessions = new HashMap<>();
    protected Throwable exception;
    protected ProcessEngineConfigurationImpl processEngineConfiguration;
    protected FailedJobCommandFactory failedJobCommandFactory;
    protected List<CommandContextCloseListener> closeListeners;
    protected Map<String, Object> attributes; // General-purpose storing of anything during the lifetime of a command context
    protected boolean reused;

    protected ActivitiEngineAgenda agenda;
    protected Map<String, ExecutionEntity> involvedExecutions = new HashMap<>(1); // The executions involved with the command
    protected LinkedList<Object> resultStack = new LinkedList<>(); // needs to be a stack, as JavaDelegates can do api calls again

    public CommandContext(Command<?> command,
                          ProcessEngineConfigurationImpl processEngineConfiguration) {
        this.command = command;
        this.processEngineConfiguration = processEngineConfiguration;
        this.failedJobCommandFactory = processEngineConfiguration.getFailedJobCommandFactory();
        this.sessionFactories = processEngineConfiguration.getSessionFactories();
        this.agenda = processEngineConfiguration.getEngineAgendaFactory().createAgenda(this);
    }

    public void close() {

        // The intention of this method is that all resources are closed properly, even if exceptions occur
        // in close or flush methods of the sessions or the transaction context.

        try {
            try {
                try {
                    executeCloseListenersClosing();
                    if (exception == null) {
                        flushSessions();
                    }
                } catch (Throwable exception) {
                    exception(exception);
                } finally {

                    try {
                        if (exception == null) {
                            executeCloseListenersAfterSessionFlushed();
                        }
                    } catch (Throwable exception) {
                        exception(exception);
                    }

                    if (exception != null) {
                        logException();
                        executeCloseListenersCloseFailure();
                    } else {
                        executeCloseListenersClosed();
                    }
                }
            } catch (Throwable exception) {
                // Catch exceptions during rollback
                exception(exception);
            } finally {
                // Sessions need to be closed, regardless of exceptions/commit/rollback
                closeSessions();
            }
        } catch (Throwable exception) {
            // Catch exceptions during session closing
            exception(exception);
        }

        if (exception != null) {
            rethrowExceptionIfNeeded();
        }
    }

    protected void logException() {
        if (exception instanceof JobNotFoundException || exception instanceof ActivitiTaskAlreadyClaimedException) {
            // reduce log level, because this may have been caused because of job deletion due to cancelActiviti="true"
            log.info("Error while closing command context",
                     exception);
        } else if (exception instanceof ActivitiOptimisticLockingException) {
            // reduce log level, as normally we're not interested in logging this exception
            log.debug("Optimistic locking exception : " + exception);
        } else {
            log.error("Error while closing command context",
                      exception);
        }
    }

    protected void rethrowExceptionIfNeeded() throws Error {
        if (exception instanceof Error) {
            throw (Error) exception;
        } else if (exception instanceof RuntimeException) {
            throw (RuntimeException) exception;
        } else {
            throw new ActivitiException("exception while executing command " + command,
                                        exception);
        }
    }

    public void addCloseListener(CommandContextCloseListener commandContextCloseListener) {
        if (closeListeners == null) {
            closeListeners = new ArrayList<>(1);
        }
        closeListeners.add(commandContextCloseListener);
    }

    public List<CommandContextCloseListener> getCloseListeners() {
        return closeListeners;
    }

    public boolean hasCloseListener(Class<?> type) {
        if (closeListeners != null && closeListeners.size() != 0) {
            for (CommandContextCloseListener listener : closeListeners) {
                if (type.isInstance(listener)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected void executeCloseListenersClosing() {
        if (closeListeners != null) {
            try {
                for (CommandContextCloseListener listener : closeListeners) {
                    listener.closing(this);
                }
            } catch (Throwable exception) {
                exception(exception);
            }
        }
    }

    protected void executeCloseListenersAfterSessionFlushed() {
        if (closeListeners != null) {
            try {
                for (CommandContextCloseListener listener : closeListeners) {
                    listener.afterSessionsFlush(this);
                }
            } catch (Throwable exception) {
                exception(exception);
            }
        }
    }

    protected void executeCloseListenersClosed() {
        if (closeListeners != null) {
            try {
                for (CommandContextCloseListener listener : closeListeners) {
                    listener.closed(this);
                }
            } catch (Throwable exception) {
                exception(exception);
            }
        }
    }

    protected void executeCloseListenersCloseFailure() {
        if (closeListeners != null) {
            try {
                for (CommandContextCloseListener listener : closeListeners) {
                    listener.closeFailure(this);
                }
            } catch (Throwable exception) {
                exception(exception);
            }
        }
    }

    protected void flushSessions() {
        for (Session session : sessions.values()) {
            session.flush();
        }
    }

    protected void closeSessions() {
        for (Session session : sessions.values()) {
            try {
                session.close();
            } catch (Throwable exception) {
                exception(exception);
            }
        }
    }

    /**
     * Stores the provided exception on this {@link CommandContext} instance.
     * That exception will be rethrown at the end of closing the {@link CommandContext} instance.
     * <p>
     * If there is already an exception being stored, a 'masked exception' message will be logged.
     */
    public void exception(Throwable exception) {
        if (this.exception == null) {
            this.exception = exception;
        } else {
            log.error("masked exception in command context. for root cause, see below as it will be rethrown later.",
                      exception);
            LogMDC.clear();
        }
    }

    public void addAttribute(String key,
                             Object value) {
        if (attributes == null) {
            attributes = new HashMap<>(1);
        }
        attributes.put(key,
                       value);
    }

    public Object getAttribute(String key) {
        if (attributes != null) {
            return attributes.get(key);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> T getGenericAttribute(String key) {
        if (attributes != null) {
            return (T) getAttribute(key);
        }
        return null;
    }

    @SuppressWarnings({"unchecked"})
    public <T> T getSession(Class<T> sessionClass) {
        Session session = sessions.get(sessionClass);
        if (session == null) {
            SessionFactory sessionFactory = sessionFactories.get(sessionClass);
            if (sessionFactory == null) {
                throw new ActivitiException("no session factory configured for " + sessionClass.getName());
            }
            session = sessionFactory.openSession(this);
            sessions.put(sessionClass,
                         session);
        }

        return (T) session;
    }

    public Map<Class<?>, SessionFactory> getSessionFactories() {
        return sessionFactories;
    }

    public DbSqlSession getDbSqlSession() {
        return getSession(DbSqlSession.class);
    }

    public EntityCache getEntityCache() {
        return getSession(EntityCache.class);
    }

    public DeploymentEntityManager getDeploymentEntityManager() {
        return processEngineConfiguration.getDeploymentEntityManager();
    }

    public ResourceEntityManager getResourceEntityManager() {
        return processEngineConfiguration.getResourceEntityManager();
    }

    public ByteArrayEntityManager getByteArrayEntityManager() {
        return processEngineConfiguration.getByteArrayEntityManager();
    }

    public ProcessDefinitionEntityManager getProcessDefinitionEntityManager() {
        return processEngineConfiguration.getProcessDefinitionEntityManager();
    }

    public ModelEntityManager getModelEntityManager() {
        return processEngineConfiguration.getModelEntityManager();
    }

    public ProcessDefinitionInfoEntityManager getProcessDefinitionInfoEntityManager() {
        return processEngineConfiguration.getProcessDefinitionInfoEntityManager();
    }

    public ExecutionEntityManager getExecutionEntityManager() {
        return processEngineConfiguration.getExecutionEntityManager();
    }

    public TaskEntityManager getTaskEntityManager() {
        return processEngineConfiguration.getTaskEntityManager();
    }

    public IdentityLinkEntityManager getIdentityLinkEntityManager() {
        return processEngineConfiguration.getIdentityLinkEntityManager();
    }

    public VariableInstanceEntityManager getVariableInstanceEntityManager() {
        return processEngineConfiguration.getVariableInstanceEntityManager();
    }

    public HistoricProcessInstanceEntityManager getHistoricProcessInstanceEntityManager() {
        return processEngineConfiguration.getHistoricProcessInstanceEntityManager();
    }

    public HistoricDetailEntityManager getHistoricDetailEntityManager() {
        return processEngineConfiguration.getHistoricDetailEntityManager();
    }

    public HistoricVariableInstanceEntityManager getHistoricVariableInstanceEntityManager() {
        return processEngineConfiguration.getHistoricVariableInstanceEntityManager();
    }

    public HistoricActivityInstanceEntityManager getHistoricActivityInstanceEntityManager() {
        return processEngineConfiguration.getHistoricActivityInstanceEntityManager();
    }

    public HistoricTaskInstanceEntityManager getHistoricTaskInstanceEntityManager() {
        return processEngineConfiguration.getHistoricTaskInstanceEntityManager();
    }

    public HistoricIdentityLinkEntityManager getHistoricIdentityLinkEntityManager() {
        return processEngineConfiguration.getHistoricIdentityLinkEntityManager();
    }

    public EventLogEntryEntityManager getEventLogEntryEntityManager() {
        return processEngineConfiguration.getEventLogEntryEntityManager();
    }

    public JobEntityManager getJobEntityManager() {
        return processEngineConfiguration.getJobEntityManager();
    }

    public TimerJobEntityManager getTimerJobEntityManager() {
        return processEngineConfiguration.getTimerJobEntityManager();
    }

    public SuspendedJobEntityManager getSuspendedJobEntityManager() {
        return processEngineConfiguration.getSuspendedJobEntityManager();
    }

    public DeadLetterJobEntityManager getDeadLetterJobEntityManager() {
        return processEngineConfiguration.getDeadLetterJobEntityManager();
    }

    public AttachmentEntityManager getAttachmentEntityManager() {
        return processEngineConfiguration.getAttachmentEntityManager();
    }

    public TableDataManager getTableDataManager() {
        return processEngineConfiguration.getTableDataManager();
    }

    public CommentEntityManager getCommentEntityManager() {
        return processEngineConfiguration.getCommentEntityManager();
    }

    public PropertyEntityManager getPropertyEntityManager() {
        return processEngineConfiguration.getPropertyEntityManager();
    }

    public EventSubscriptionEntityManager getEventSubscriptionEntityManager() {
        return processEngineConfiguration.getEventSubscriptionEntityManager();
    }

    public HistoryManager getHistoryManager() {
        return processEngineConfiguration.getHistoryManager();
    }

    public JobManager getJobManager() {
        return processEngineConfiguration.getJobManager();
    }

    // Involved executions ////////////////////////////////////////////////////////

    public void addInvolvedExecution(ExecutionEntity executionEntity) {
        if (executionEntity.getId() != null) {
            involvedExecutions.put(executionEntity.getId(),
                                   executionEntity);
        }
    }

    public boolean hasInvolvedExecutions() {
        return involvedExecutions.size() > 0;
    }

    public Collection<ExecutionEntity> getInvolvedExecutions() {
        return involvedExecutions.values();
    }

    // getters and setters
    // //////////////////////////////////////////////////////

    public Command<?> getCommand() {
        return command;
    }

    public Map<Class<?>, Session> getSessions() {
        return sessions;
    }

    public Throwable getException() {
        return exception;
    }

    public FailedJobCommandFactory getFailedJobCommandFactory() {
        return failedJobCommandFactory;
    }

    public ProcessEngineConfigurationImpl getProcessEngineConfiguration() {
        return processEngineConfiguration;
    }

    public ActivitiEventDispatcher getEventDispatcher() {
        return processEngineConfiguration.getEventDispatcher();
    }

    public ActivitiEngineAgenda getAgenda() {
        return agenda;
    }

    public Object getResult() {
        return resultStack.pollLast();
    }

    public void setResult(Object result) {
        resultStack.add(result);
    }

    public boolean isReused() {
        return reused;
    }

    public void setReused(boolean reused) {
        this.reused = reused;
    }
}

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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.cfg.HistorySession;
import org.activiti.engine.impl.cfg.ManagementSession;
import org.activiti.engine.impl.cfg.MessageSession;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cfg.TimerSession;
import org.activiti.engine.impl.cfg.TransactionContext;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.persistence.mgr.AttachmentManager;
import org.activiti.engine.impl.persistence.mgr.DeploymentManager;
import org.activiti.engine.impl.persistence.mgr.ExecutionManager;
import org.activiti.engine.impl.persistence.mgr.GroupManager;
import org.activiti.engine.impl.persistence.mgr.HistoricActivityInstanceManager;
import org.activiti.engine.impl.persistence.mgr.HistoricDetailManager;
import org.activiti.engine.impl.persistence.mgr.HistoricProcessInstanceManager;
import org.activiti.engine.impl.persistence.mgr.HistoricTaskInstanceManager;
import org.activiti.engine.impl.persistence.mgr.IdentityInfoManager;
import org.activiti.engine.impl.persistence.mgr.IdentityLinkManager;
import org.activiti.engine.impl.persistence.mgr.JobManager;
import org.activiti.engine.impl.persistence.mgr.MembershipManager;
import org.activiti.engine.impl.persistence.mgr.ProcessDefinitionManager;
import org.activiti.engine.impl.persistence.mgr.ResourceManager;
import org.activiti.engine.impl.persistence.mgr.TaskManager;
import org.activiti.engine.impl.persistence.mgr.UserManager;
import org.activiti.engine.impl.persistence.mgr.VariableInstanceManager;
import org.activiti.engine.impl.pvm.runtime.AtomicOperation;
import org.activiti.engine.impl.pvm.runtime.InterpretableExecution;

/**
 * @author Tom Baeyens
 * @author Agim Emruli
 */
public class CommandContext {

  private static Logger log = Logger.getLogger(CommandContext.class.getName());

  protected Command< ? > command;
  protected TransactionContext transactionContext;
  protected Map<Class< ? >, SessionFactory> sessionFactories;
  protected Map<Class< ? >, Session> sessions = new HashMap<Class< ? >, Session>();
  protected Throwable exception = null;
  protected LinkedList<AtomicOperation> nextOperations = new LinkedList<AtomicOperation>();

  
  public void performOperation(AtomicOperation executionOperation, InterpretableExecution execution) {
    nextOperations.add(executionOperation);
    if (nextOperations.size()==1) {
      try {
        Context.setExecutionContext(execution);
        while (!nextOperations.isEmpty()) {
          AtomicOperation currentOperation = nextOperations.removeFirst();
          if (log.isLoggable(Level.FINEST)) {
            log.finest("AtomicOperation: " + currentOperation + " on " + this);
          }
          currentOperation.execute(execution);
        }
      } finally {
        Context.removeExecutionContext();
      }
    }
  }

  public CommandContext(Command<?> command, ProcessEngineConfigurationImpl processEngineConfiguration) {
    this.command = command;
    sessionFactories = processEngineConfiguration.getSessionFactories();
    this.transactionContext = processEngineConfiguration
      .getTransactionContextFactory()
      .openTransactionContext(this);
  }

  public void close() {
    // the intention of this method is that all resources are closed properly,
    // even
    // if exceptions occur in close or flush methods of the sessions or the
    // transaction context.

    try {
      try {
        try {

          if (exception == null) {
            flushSessions();
          }

        } catch (Throwable exception) {
          exception(exception);
        } finally {

          try {
            if (exception == null) {
              transactionContext.commit();
            }
          } catch (Throwable exception) {
            exception(exception);
          }

          if (exception != null) {
            log.log(Level.SEVERE, "Error while closing command context", exception);
            transactionContext.rollback();
          }
        }
      } catch (Throwable exception) {
        exception(exception);
      } finally {
        closeSessions();

      }
    } catch (Throwable exception) {
      exception(exception);
    } 

    // rethrow the original exception if there was one
    if (exception != null) {
      if (exception instanceof Error) {
        throw (Error) exception;
      } else if (exception instanceof RuntimeException) {
        throw (RuntimeException) exception;
      } else {
        throw new ActivitiException("exception while executing command " + command, exception);
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

  public void exception(Throwable exception) {
    if (this.exception == null) {
      this.exception = exception;
    } else {
      log.log(Level.SEVERE, "masked exception in command context. for root cause, see below as it will be rethrown later.", exception);
    }
  }

  @SuppressWarnings({"unchecked"})
  public <T> T getSession(Class<T> sessionClass) {
    Session session = sessions.get(sessionClass);
    if (session == null) {
      SessionFactory sessionFactory = sessionFactories.get(sessionClass);
      if (sessionFactory==null) {
        throw new ActivitiException("no session factory configured for "+sessionClass.getName());
      }
      session = sessionFactory.openSession();
      sessions.put(sessionClass, session);
    }

    return (T) session;
  }
  
  public DbSqlSession getDbSqlSession() {
    return getSession(DbSqlSession.class);
  }
  
  public DeploymentManager getDeploymentManager() {
    return getSession(DeploymentManager.class);
  }

  public ResourceManager getResourceManager() {
    return getSession(ResourceManager.class);
  }
  
  public ProcessDefinitionManager getProcessDefinitionManager() {
    return getSession(ProcessDefinitionManager.class);
  }

  public ExecutionManager getExecutionManager() {
    return getSession(ExecutionManager.class);
  }

  public TaskManager getTaskManager() {
    return getSession(TaskManager.class);
  }

  public IdentityLinkManager getIdentityLinkManager() {
    return getSession(IdentityLinkManager.class);
  }

  public VariableInstanceManager getVariableInstanceManager() {
    return getSession(VariableInstanceManager.class);
  }

  public HistoricProcessInstanceManager getHistoricProcessInstanceManager() {
    return getSession(HistoricProcessInstanceManager.class);
  }

  public HistoricDetailManager getHistoricDetailManager() {
    return getSession(HistoricDetailManager.class);
  }

  public HistoricActivityInstanceManager getHistoricActivityInstanceManager() {
    return getSession(HistoricActivityInstanceManager.class);
  }
  
  public HistoricTaskInstanceManager getHistoricTaskInstanceManager() {
    return getSession(HistoricTaskInstanceManager.class);
  }
  
  public JobManager getJobManager() {
    return getSession(JobManager.class);
  }

  public UserManager getUserManager() {
    return getSession(UserManager.class);
  }

  public GroupManager getGroupManager() {
    return getSession(GroupManager.class);
  }

  public IdentityInfoManager getIdentityInfoManager() {
    return getSession(IdentityInfoManager.class);
  }

  public MembershipManager getMembershipManager() {
    return getSession(MembershipManager.class);
  }
  
  public AttachmentManager getAttachmentManager() {
    return getSession(AttachmentManager.class);
  }
  

  public MessageSession getMessageSession() {
    return getSession(MessageSession.class);
  }
  public TimerSession getTimerSession() {
    return getSession(TimerSession.class);
  }
  public HistorySession getHistorySession() {
    return getSession(HistorySession.class);
  }
  public ManagementSession getManagementSession() {
    return getSession(ManagementSession.class);
  }

  // getters and setters //////////////////////////////////////////////////////

  public TransactionContext getTransactionContext() {
    return transactionContext;
  }
  public Command< ? > getCommand() {
    return command;
  }
  public Map<Class< ? >, Session> getSessions() {
    return sessions;
  }
  public Throwable getException() {
    return exception;
  }
}

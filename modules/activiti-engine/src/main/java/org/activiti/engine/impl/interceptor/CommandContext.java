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
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.cfg.HistorySession;
import org.activiti.engine.impl.cfg.IdentitySession;
import org.activiti.engine.impl.cfg.ManagementSession;
import org.activiti.engine.impl.cfg.MessageSession;
import org.activiti.engine.impl.cfg.ProcessEngineConfiguration;
import org.activiti.engine.impl.cfg.RepositorySession;
import org.activiti.engine.impl.cfg.RuntimeSession;
import org.activiti.engine.impl.cfg.TaskSession;
import org.activiti.engine.impl.cfg.TimerSession;
import org.activiti.engine.impl.cfg.TransactionContext;
import org.activiti.engine.impl.persistence.db.DbSqlSession;

/**
 * @author Tom Baeyens
 * @author Agim Emruli
 */
public class CommandContext {

  private static Logger log = Logger.getLogger(CommandContext.class.getName());

  private static final ThreadLocal<Stack<CommandContext>> txContextStacks = new ThreadLocal<Stack<CommandContext>>();

  protected Command< ? > command;
  protected ProcessEngineConfiguration processEngineConfiguration;
  protected TransactionContext transactionContext;
  protected Map<Class< ? >, Session> sessions = new HashMap<Class< ? >, Session>();
  protected Throwable exception = null;


  public static void setCurrentCommandContext(CommandContext commandContext) {
    getContextStack(true).push(commandContext);
  }

  public static void removeCurrentCommandContext() {
    getContextStack(true).pop();
  }

  public static CommandContext getCurrent() {
    Stack<CommandContext> contextStack = getContextStack(false);
    if ((contextStack == null) || (contextStack.isEmpty())) {
      return null;
    }
    return contextStack.peek();
  }
  
  public static <T> T getCurrentSession(Class<T> sessionClass) {
    return (T) getCurrent().getSession(sessionClass);
  }

  private static Stack<CommandContext> getContextStack(boolean isInitializationRequired) {
    Stack<CommandContext> txContextStack = txContextStacks.get();
    if (txContextStack == null && isInitializationRequired) {
      txContextStack = new Stack<CommandContext>();
      txContextStacks.set(txContextStack);
    }
    return txContextStack;
  }

  public CommandContext(Command<?> command, ProcessEngineConfiguration processEngineConfiguration) {
    this.command = command;
    this.processEngineConfiguration = processEngineConfiguration;
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
            exception.printStackTrace();
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
      SessionFactory sessionFactory = processEngineConfiguration.getSessionFactories().get(sessionClass);
      if (sessionFactory==null) {
        throw new ActivitiException("no session factory configured for "+sessionClass.getName());
      }
      session = sessionFactory.openSession();
      sessions.put(sessionClass, session);
    }

    return (T) session;
  }

  public RepositorySession getRepositorySession() {
    return getSession(RepositorySession.class);
  }
  public RuntimeSession getRuntimeSession() {
    return getSession(RuntimeSession.class);
  }
  public IdentitySession getIdentitySession() {
    return getSession(IdentitySession.class);
  }
  public MessageSession getMessageSession() {
    return getSession(MessageSession.class);
  }
  public TimerSession getTimerSession() {
    return getSession(TimerSession.class);
  }
  public TaskSession getTaskSession() {
    return getSession(TaskSession.class);
  }
  public HistorySession getHistorySession() {
    return getSession(HistorySession.class);
  }
  public ManagementSession getManagementSession() {
    return getSession(ManagementSession.class);
  }
  public DbSqlSession getDbSqlSession() {
    return getSession(DbSqlSession.class);
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
  public ProcessEngineConfiguration getProcessEngineConfiguration() {
    return processEngineConfiguration;
  }
}

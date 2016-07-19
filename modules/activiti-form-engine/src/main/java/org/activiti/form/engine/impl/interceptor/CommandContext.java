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
package org.activiti.form.engine.impl.interceptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.form.engine.ActivitiFormException;
import org.activiti.form.engine.ActivitiFormOptimisticLockingException;
import org.activiti.form.engine.FormEngineConfiguration;
import org.activiti.form.engine.impl.cfg.TransactionContext;
import org.activiti.form.engine.impl.db.DbSqlSession;
import org.activiti.form.engine.impl.persistence.entity.FormDeploymentEntityManager;
import org.activiti.form.engine.impl.persistence.entity.FormEntityManager;
import org.activiti.form.engine.impl.persistence.entity.ResourceEntityManager;
import org.activiti.form.engine.impl.persistence.entity.SubmittedFormEntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tijs Rademakers
 * @author Joram Barrez
 */
public class CommandContext {

  private static Logger log = LoggerFactory.getLogger(CommandContext.class);

  protected Command<?> command;
  protected TransactionContext transactionContext;
  protected Map<Class<?>, SessionFactory> sessionFactories;
  protected Map<Class<?>, Session> sessions = new HashMap<Class<?>, Session>();
  protected Throwable exception;
  protected FormEngineConfiguration formEngineConfiguration;
  protected List<CommandContextCloseListener> closeListeners;
  protected Map<String, Object> attributes; // General-purpose storing of anything during the lifetime of a command context

  public CommandContext(Command<?> command, FormEngineConfiguration formEngineConfiguration) {
    this.command = command;
    this.formEngineConfiguration = formEngineConfiguration;
    sessionFactories = formEngineConfiguration.getSessionFactories();
    this.transactionContext = formEngineConfiguration.getTransactionContextFactory().openTransactionContext(this);
  }

  public void close() {
    // the intention of this method is that all resources are closed properly, even if exceptions occur
    // in close or flush methods of the sessions or the transaction context.

    try {
      try {
        try {

          if (closeListeners != null) {
            try {
              for (CommandContextCloseListener listener : closeListeners) {
                listener.closing(this);
              }
            } catch (Throwable exception) {
              exception(exception);
            }
          }

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

          if (closeListeners != null) {
            try {
              for (CommandContextCloseListener listener : closeListeners) {
                listener.closed(this);
              }
            } catch (Throwable exception) {
              exception(exception);
            }
          }

          if (exception != null) {
            if (exception instanceof ActivitiFormOptimisticLockingException) {
              // reduce log level, as normally we're not
              // interested in logging this exception
              log.debug("Optimistic locking exception : " + exception);
            } else {
              log.debug("Error while closing command context", exception);
            }

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
        throw new ActivitiFormException("exception while executing command " + command, exception);
      }
    }
  }

  public void addCloseListener(CommandContextCloseListener commandContextCloseListener) {
    if (closeListeners == null) {
      closeListeners = new ArrayList<CommandContextCloseListener>(1);
    }
    closeListeners.add(commandContextCloseListener);
  }

  public List<CommandContextCloseListener> getCloseListeners() {
    return closeListeners;
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
      log.error("masked exception in command context. for root cause, see below as it will be rethrown later.", exception);
    }
  }

  public void addAttribute(String key, Object value) {
    if (attributes == null) {
      attributes = new HashMap<String, Object>(1);
    }
    attributes.put(key, value);
  }

  public Object getAttribute(String key) {
    if (attributes != null) {
      return attributes.get(key);
    }
    return null;
  }

  @SuppressWarnings({ "unchecked" })
  public <T> T getSession(Class<T> sessionClass) {
    Session session = sessions.get(sessionClass);
    if (session == null) {
      SessionFactory sessionFactory = sessionFactories.get(sessionClass);
      if (sessionFactory == null) {
        throw new ActivitiFormException("no session factory configured for " + sessionClass.getName());
      }
      session = sessionFactory.openSession(this);
      sessions.put(sessionClass, session);
    }

    return (T) session;
  }

  public Map<Class<?>, SessionFactory> getSessionFactories() {
    return sessionFactories;
  }

  public DbSqlSession getDbSqlSession() {
    return getSession(DbSqlSession.class);
  }

  public FormDeploymentEntityManager getDeploymentEntityManager() {
    return formEngineConfiguration.getDeploymentEntityManager();
  }
  
  public FormEntityManager getFormEntityManager() {
    return formEngineConfiguration.getFormEntityManager();
  }

  public ResourceEntityManager getResourceEntityManager() {
    return formEngineConfiguration.getResourceEntityManager();
  }
  
  public SubmittedFormEntityManager getSubmittedFormEntityManager() {
    return formEngineConfiguration.getSubmittedFormEntityManager();
  }

  // getters and setters
  // //////////////////////////////////////////////////////
  
  public FormEngineConfiguration getFormEngineConfiguration() {
    return formEngineConfiguration;
  }

  public TransactionContext getTransactionContext() {
    return transactionContext;
  }

  public Command<?> getCommand() {
    return command;
  }

  public Map<Class<?>, Session> getSessions() {
    return sessions;
  }

  public Throwable getException() {
    return exception;
  }
}

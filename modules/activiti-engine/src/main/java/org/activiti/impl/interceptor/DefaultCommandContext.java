/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.impl.interceptor;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.ActivitiException;
import org.activiti.impl.msg.MessageSession;
import org.activiti.impl.msg.MessageSessionFactory;
import org.activiti.impl.persistence.PersistenceSession;
import org.activiti.impl.persistence.PersistenceSessionFactory;
import org.activiti.impl.timer.TimerSession;
import org.activiti.impl.timer.TimerSessionFactory;
import org.activiti.impl.tx.Session;
import org.activiti.impl.tx.TransactionContext;
import org.activiti.impl.tx.TransactionContextFactory;

/**
 * @author Tom Baeyens
 */
public class DefaultCommandContext implements CommandContext {

  private static Logger log = Logger.getLogger(DefaultCommandContext.class.getName());

  private final Command< ? > command;

  private final PersistenceSession persistenceSession;
  private final MessageSession messageSession;
  private final TimerSession timerSession;
  private final TransactionContext transactionContext;

  private final Map<Class< ? >, SessionFactory> sessionFactories;
  private final Map<Class< ? >, Session> sessions = new HashMap<Class< ? >, Session>();
  private Throwable exception = null;


  public DefaultCommandContext(Command<?> command, Map<Class<?>, SessionFactory> sessionFactories, TransactionContextFactory transactionContextFactory, PersistenceSessionFactory persistenceSessionFactory, MessageSessionFactory messageSessionFactory, TimerSessionFactory timerSessionFactory) {
    this.command = command;

    this.persistenceSession = persistenceSessionFactory.openPersistenceSession(this);
    this.messageSession = messageSessionFactory.openMessageSession(this);
    this.timerSession = timerSessionFactory.openTimerSession(this);
    this.transactionContext = transactionContextFactory.openTransactionContext(this);

    this.sessionFactories = sessionFactories;
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
    persistenceSession.flush();
    messageSession.flush();

    for (Session session : sessions.values()) {
      session.flush();
    }
  }

  protected void closeSessions() {
    messageSession.close();
    persistenceSession.close();

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
      log.log(Level.SEVERE, "exception in command context", exception);
    }
  }

  public PersistenceSession getPersistenceSession() {
    return persistenceSession;
  }
  public MessageSession getMessageSession() {
    return messageSession;
  }
  public TimerSession getTimerSession() {
    return timerSession;
  }

  @SuppressWarnings({"unchecked"})
  public <T> T getSession(Class<T> sessionClass) {
    Session session = sessions.get(sessionClass);
    if (session == null) {
      SessionFactory sessionFactory = sessionFactories.get(sessionClass);
      session = sessionFactory.openSession();
      sessions.put(sessionClass, session);
    }

    return (T) session;
  }

  public TransactionContext getTransactionContext() {
    return transactionContext;
  }

}

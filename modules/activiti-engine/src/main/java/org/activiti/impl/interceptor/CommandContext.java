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
package org.activiti.impl.interceptor;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.ActivitiException;
import org.activiti.impl.msg.MessageSession;
import org.activiti.impl.persistence.PersistenceSession;
import org.activiti.impl.timer.TimerSession;
import org.activiti.impl.tx.Session;
import org.activiti.impl.tx.TransactionContext;

/**
 * @author Tom Baeyens
 */
public class CommandContext {

  private static Logger log = Logger.getLogger(CommandContext.class.getName());

  private static ThreadLocal<Stack<CommandContext>> txContextStacks = new ThreadLocal<Stack<CommandContext>>();

  private CommandContextFactory commandContextFactory;
  private Command< ? > command;
  private Throwable exception = null;

  private PersistenceSession persistenceSession;
  private MessageSession messageSession;
  private TimerSession timerSession;
  private TransactionContext transactionContext;

  private Map<Class< ? >, SessionFactory> sessionFactories;
  private Map<Class< ? >, Session> sessions = new HashMap<Class< ? >, Session>();

  public CommandContext(Command< ? > command, CommandContextFactory commandContextFactory) {
    this.command = command;
    this.commandContextFactory = commandContextFactory;
    this.sessionFactories = commandContextFactory.getSessionFactories();

    this.transactionContext = commandContextFactory.getProcessEngineConfiguration().getTransactionContextFactory().openTransactionContext(this);
    this.persistenceSession = commandContextFactory.getProcessEngineConfiguration().getPersistenceSessionFactory().openPersistenceSession(this);
    this.messageSession = commandContextFactory.getProcessEngineConfiguration().getMessageSessionFactory().openMessageSession(this);
    this.timerSession = commandContextFactory.getProcessEngineConfiguration().getTimerSessionFactory().openTimerSession(this);

    getContextStack(true).push(this);

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
    } finally {

      getContextStack(true).pop();
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

  protected static Stack<CommandContext> getContextStack(boolean isInitializationRequired) {
    Stack<CommandContext> txContextStack = txContextStacks.get();
    if (txContextStack == null && isInitializationRequired) {
      txContextStack = new Stack<CommandContext>();
      txContextStacks.set(txContextStack);
    }
    return txContextStack;
  }

  public static CommandContext getCurrent() {
    Stack<CommandContext> contextStack = getContextStack(false);
    if ((contextStack == null) || (contextStack.isEmpty())) {
      return null;
    }
    return contextStack.peek();
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

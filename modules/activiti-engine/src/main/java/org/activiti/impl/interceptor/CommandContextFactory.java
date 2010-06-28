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

import org.activiti.impl.cfg.ProcessEngineConfiguration;
import org.activiti.impl.msg.MessageSessionFactory;
import org.activiti.impl.persistence.PersistenceSessionFactory;
import org.activiti.impl.timer.TimerSessionFactory;
import org.activiti.impl.tx.TransactionContextFactory;


/**
 * @author Tom Baeyens
 */
public class CommandContextFactory {
  
  protected final ProcessEngineConfiguration processEngineConfiguration;
  protected PersistenceSessionFactory persistenceSessionFactory;
  protected MessageSessionFactory messageSessionFactory;
  protected TimerSessionFactory timerSessionFactory;
  protected TransactionContextFactory transactionContextFactory;

  /** is this open ended set of session factories useful?
   * intended purpose is to allow for user defined session factories and sessions. */  
  protected Map<Class<?>, SessionFactory> sessionFactories = new HashMap<Class<?>, SessionFactory>();

  public CommandContextFactory(ProcessEngineConfiguration processEngineConfiguration) {
    this.processEngineConfiguration = processEngineConfiguration;
  }

  public CommandContext createCommandContext(Command<?> cmd) {
    return new CommandContext(cmd, this);
  }
  
  public void addSessionFactory(Class<?> sessionClass, SessionFactory sessionFactory) {
    sessionFactories.put(sessionClass, sessionFactory);
  }
  
  public SessionFactory getSessionFactory(Class<?> sessionClass) {
    return sessionFactories.get(sessionClass);
  }
  
  public void removeSessionFactory(Class<?> sessionClass) {
    sessionFactories.remove(sessionClass);
  }

  // getters and setters //////////////////////////////////////////////////////
  
  public ProcessEngineConfiguration getProcessEngineConfiguration() {
    return processEngineConfiguration;
  }
  public Map<Class< ? >, SessionFactory> getSessionFactories() {
    return sessionFactories;
  }
  public void setSessionFactories(Map<Class< ? >, SessionFactory> sessionFactories) {
    this.sessionFactories = sessionFactories;  }

  public TransactionContextFactory getTransactionContextFactory() {
    return transactionContextFactory;
  }
  public void setTransactionContextFactory(TransactionContextFactory transactionContextFactory) {
    this.transactionContextFactory = transactionContextFactory;
  }
  public PersistenceSessionFactory getPersistenceSessionFactory() {
    return persistenceSessionFactory;
  }
  public void setPersistenceSessionFactory(PersistenceSessionFactory persistenceSessionFactory) {
    this.persistenceSessionFactory = persistenceSessionFactory;
  }
  public MessageSessionFactory getMessageSessionFactory() {
    return messageSessionFactory;
  }
  public void setMessageSessionFactory(MessageSessionFactory messageSessionFactory) {
    this.messageSessionFactory = messageSessionFactory;
  }
  public TimerSessionFactory getTimerSessionFactory() {
    return timerSessionFactory;
  }
  public void setTimerSessionFactory(TimerSessionFactory timerSessionFactory) {
    this.timerSessionFactory = timerSessionFactory;
  }
}

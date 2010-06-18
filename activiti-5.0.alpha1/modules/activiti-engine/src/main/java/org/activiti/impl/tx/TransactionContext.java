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
package org.activiti.impl.tx;

import java.util.List;
import java.util.Stack;

import org.activiti.Configuration;
import org.activiti.impl.ProcessEngineImpl;


/**
 * @author Tom Baeyens
 */
public class TransactionContext {

  protected ProcessEngineImpl processEngine;
  protected TransactionalObjectFactory transactionalObjectFactory;
  protected static ThreadLocal<Stack<TransactionContext>> txContextStacks = new ThreadLocal<Stack<TransactionContext>>();

  public TransactionContext(ProcessEngineImpl processEngine) {
    this.processEngine = processEngine;
    List<Descriptor> descriptors = processEngine.getConfigurationObject(Configuration.NAME_TRANSACTIONALOBJECTDESCRIPTORS, List.class);
    this.transactionalObjectFactory = new TransactionalObjectFactory(descriptors);
   
    getContextStack(true).push(this);
  }
  
  protected static Stack<TransactionContext> getContextStack(boolean isInitializationRequired) {
    Stack<TransactionContext> txContextStack = txContextStacks.get();
    if (txContextStack==null && isInitializationRequired) {
      txContextStack = new Stack<TransactionContext>();
      txContextStacks.set(txContextStack);
    }
    return txContextStack;
  }
  
  public static TransactionContext getCurrent() {
    Stack<TransactionContext> contextStack = getContextStack(false);
    if (contextStack==null) {
      return null;
    }
    return contextStack.peek();
  }

  public void setConfiguredObject(Object configuredObject) {
    transactionalObjectFactory.addDescriptor(new ProvidedObjectDescriptor(configuredObject));
  }

  public <T> T getTransactionalObject(Class<T> type) {
    return transactionalObjectFactory.get(type, this);
  }
  
  public ProcessEngineImpl getProcessEngine() {
    return processEngine;
  }

  public void commit() {
    for (Session session: transactionalObjectFactory.getInstantiatedSessions()) {
      session.commit();
    }
  }

  public void rollback(RuntimeException e) {
    for (Session session: transactionalObjectFactory.getInstantiatedSessions()) {
      session.rollback();
    }
  }

  public void close() {
    for (Session session: transactionalObjectFactory.getInstantiatedSessions()) {
      session.close();
    }
    getContextStack(true).pop();
  }
}

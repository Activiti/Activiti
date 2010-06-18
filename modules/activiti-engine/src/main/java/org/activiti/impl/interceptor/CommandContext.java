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

import java.util.Map;
import java.util.Stack;

import org.activiti.impl.cfg.PersistenceSessionFactory;
import org.activiti.impl.el.ExpressionManager;
import org.activiti.impl.job.JobHandler;
import org.activiti.impl.persistence.PersistenceSession;
import org.activiti.impl.repository.DeployerManager;
import org.activiti.impl.repository.ProcessCache;
import org.activiti.impl.scripting.ScriptingEngines;
import org.activiti.impl.variable.VariableTypes;


/**
 * @author Tom Baeyens
 */
public class CommandContext {

  static ThreadLocal<Stack<CommandContext>> txContextStacks = new ThreadLocal<Stack<CommandContext>>();
  
  Command<?> command;
  Throwable exception = null;
  PersistenceSession persistenceSession;
  CommandContextFactory commandContextFactory;

  public CommandContext(Command<?> command, 
                        PersistenceSession persistenceSession, 
                        CommandContextFactory commandContextFactory) {
    this.command = command;
    this.persistenceSession = persistenceSession;
    this.commandContextFactory = commandContextFactory;
    getContextStack(true).push(this);
  }

  public void exception(Throwable exception) {
    this.exception = exception;
  }
  
  protected static Stack<CommandContext> getContextStack(boolean isInitializationRequired) {
    Stack<CommandContext> txContextStack = txContextStacks.get();
    if (txContextStack==null && isInitializationRequired) {
      txContextStack = new Stack<CommandContext>();
      txContextStacks.set(txContextStack);
    }
    return txContextStack;
  }
  
  public static CommandContext getCurrent() {
    Stack<CommandContext> contextStack = getContextStack(false);
    if ( (contextStack==null)
         || (contextStack.isEmpty())
       ) {
      return null;
    }
    return contextStack.peek();
  }

  public void close() {
    try {
      if (exception==null) {
        persistenceSession.commit();
      } else {
        persistenceSession.rollback();
      }
    } finally {
      persistenceSession.close();
    }
    
    getContextStack(true).pop();
  }

  public PersistenceSession getPersistenceSession() {
    return persistenceSession;
  }

  public DeployerManager getDeployerManager() {
    return commandContextFactory.getDeployerManager();
  }

  public ProcessCache getProcessCache() {
    return commandContextFactory.getProcessCache();
  }

  public ScriptingEngines getScriptingEngines() {
    return commandContextFactory.getScriptingEngines();
  }

  public VariableTypes getVariableTypes() {
    return commandContextFactory.getVariableTypes();
  }
  
  public PersistenceSessionFactory getPersistenceSessionFactory() {
    return commandContextFactory.getPersistenceSessionFactory();
  }
  
  public ExpressionManager getExpressionManager() {
    return commandContextFactory.getExpressionManager();
  }

  public Map<String, JobHandler> getJobCommands() {
    return commandContextFactory.getJobCommands();
  }
}

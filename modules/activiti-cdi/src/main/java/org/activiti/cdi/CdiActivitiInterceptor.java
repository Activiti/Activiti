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
package org.activiti.cdi;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.cdi.impl.context.BusinessProcessAssociationManager;
import org.activiti.cdi.impl.context.CachingBeanStore;
import org.activiti.cdi.impl.util.ProgrammaticBeanLookup;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandInterceptor;
import org.activiti.engine.impl.runtime.ExecutionEntity;

/**
 * CommandInterceptor for flushing the Beanstore before and after executing a
 * command.
 * 
 * TODO: open a context if not active. 
 * 
 * @author Daniel Meyer
 */
public class CdiActivitiInterceptor extends CommandInterceptor {
  
  Logger logger = Logger.getLogger(CdiActivitiInterceptor.class.getName());

  @Override
  public <T> T execute(Command<T> command) {
    // Under certain circumstances we might need to setup a context here. 
    // (I think this might be the case when Activiti calls clientcode, for example when executing a job.)
    // TODO: how can we open a context in a cdi-implementation agnostic way here?
    flushBeanStore(); 
    T result = next.execute(command);
    flushBeanStore();
    return result;
  }

  protected void flushBeanStore() {
    BusinessProcessAssociationManager associationManager = null;
    try {
      associationManager = ProgrammaticBeanLookup.lookup(BusinessProcessAssociationManager.class);
    } catch (Exception e) {
      // ignore silently -> CDI is not setup (yet/anymore)
      logger.finest("Not flushing the beanStore, could not lookup "+BusinessProcessAssociationManager.class.getName());
      return;
    }
    if (associationManager.getProcessInstanceId() != null) {
      ExecutionEntity processInstance = Context
        .getCommandContext()
        .getExecutionManager()
        .findExecutionById(associationManager.getProcessInstanceId());
      if (processInstance != null && !processInstance.isEnded()) {
        CachingBeanStore beanStore = associationManager.getBeanStore();
        logFlushSummary(beanStore);
        processInstance.setVariables(beanStore.getAll());
        beanStore.clear();
      }
    }

  }

  protected void logFlushSummary(CachingBeanStore beanStore) {
    if (logger.isLoggable(Level.FINE)) {
      if (beanStore.getVariableNames().size() == 0) {
        logger.finest("Cdi context flush summary: nothing to flush");
      } else {
        logger.fine("------------------ Cdi context flush summary:");
        for (String variable : beanStore.getVariableNames()) {
          logger.fine("   - " + variable + ": " + beanStore.getContextualInstance(variable));
        }
        logger.fine("-----------------------------------------------");
      }      
    }
  }

}

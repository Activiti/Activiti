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

import javax.enterprise.inject.spi.BeanManager;

import org.activiti.cdi.annotation.BusinessProcessScoped;
import org.activiti.cdi.impl.ActorReference;
import org.activiti.cdi.impl.context.BusinessProcessAssociationManager;
import org.activiti.cdi.impl.context.CachingBeanStore;
import org.activiti.cdi.impl.context.ThreadContext;
import org.activiti.cdi.impl.context.ThreadScoped;
import org.activiti.cdi.impl.util.ProgrammaticBeanLookup;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandInterceptor;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;

/**
 * CommandInterceptor for flushing process variables retrieved / created in the current unit of work.
 * 
 * NOTE: process variables are only flushed, if the unit of work is controlled using the methods exposed on
 * the {@link BusinessProcess} bean. 
 * I.e. in order for this interceptor to flush changes to {@link BusinessProcessScoped} beans or process variables 
 * set using {@link BusinessProcess#setProcessVariable(String, Object)} the unit of work needs to be completed using 
 * {@link BusinessProcess#completeTask()} or {@link BusinessProcess#signalExecution()}.
 * 
 * @author Daniel Meyer
 */
public class CdiActivitiInterceptor extends CommandInterceptor {
  
  private final static Logger log = Logger.getLogger(CdiActivitiInterceptor.class.getName());
  
  @Override
  public <T> T execute(Command<T> command) {
    if(isCdiActive()) {
      return executeInCdiEnv(command);
    } else {
      return next.execute(command);
    }
  }

  private boolean isCdiActive() {
    try {
      return getAssociationManager() != null;
    } catch (Exception e) {
      return false;
    }
  }

  public <T> T executeInCdiEnv(Command<T> command) {
    resetThreadContext();
    setActor();
    boolean flush = getAssociationManager().isFlushBeanStore();
    if (flush) {
      flushBeanStore();
    }
    T result = next.execute(command);
    if (flush) {
      flushBeanStore();
    }
    resetThreadContext();
    return result;
  }

  protected void resetThreadContext() {
    ThreadContext threadContext = (ThreadContext) getBeanInstance(BeanManager.class).getContext(ThreadScoped.class);
    threadContext.clear();
  }

  protected void flushBeanStore() {
    BusinessProcessAssociationManager associationManager = getAssociationManager();
    String executionId = associationManager.getExecutionId();

    if (executionId != null) {
      ExecutionEntity execution = Context
        .getCommandContext()
        .getExecutionManager()
        .findExecutionById(executionId);
      if (execution != null && !execution.isEnded()) {
        CachingBeanStore beanStore = associationManager.getBeanStore();
        if (log.isLoggable(Level.FINE)) {
          logFlushSummary(beanStore);
        }
        execution.setVariables(beanStore.getAll());
        beanStore.clear();
      }
    }
  }
  
  protected void setActor() {
    ActorReference actorReference = getBeanInstance(ActorReference.class);
    if(actorReference.isAvailable()) {
      Actor actor = actorReference.getActor();
      if(actor.getActorId() != null) {
        Authentication.setAuthenticatedUserId(actor.getActorId());
      }
    }
  }
  
  protected BusinessProcessAssociationManager getAssociationManager() {
    return getBeanInstance(BusinessProcessAssociationManager.class);
  }
  
  protected <T> T getBeanInstance(Class<T> clazz) {
    return ProgrammaticBeanLookup.lookup(clazz);           
  }

  protected void logFlushSummary(CachingBeanStore beanStore) {
    if (beanStore.getVariableNames().size() == 0) {
      log.finest("Cdi context flush summary: nothing to flush");
    } else {
      log.fine("------------------ Cdi context flush summary:");
      for (String variable : beanStore.getVariableNames()) {
        log.fine("   - " + variable + ": " + beanStore.getContextualInstance(variable));
      }
      log.fine("-----------------------------------------------");
    }
  }

}

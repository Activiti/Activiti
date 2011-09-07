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
package org.activiti.cdi.impl.context;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Scope;

import org.activiti.cdi.impl.util.ProgrammaticBeanLookup;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.task.Task;

/**
 * Default implementation of the business process association manager. Uses a
 * fallback-strategy to associate the process instance with the "broadest"
 * active scope, starting with the conversation.
 * <p />
 * Subclass in order to implement custom association schemes and association
 * with custom scopes.
 * 
 * @author Daniel Meyer
 */
@SuppressWarnings("serial")
public class DefaultBusinessProcessAssociationManager implements BusinessProcessAssociationManager, Serializable {
  
  private final static Logger log = Logger.getLogger(DefaultBusinessProcessAssociationManager.class.getName());
  
  protected static class ScopedAssociation { 
    protected String executionId;     
    protected Task task;
    protected CachingBeanStore beanStore = new CachingBeanStore();
    public void setExecutionId(String executionId) {
      this.executionId = executionId;
    }      
    public String getExecutionId() {
      return executionId;
    }    
    public CachingBeanStore getBeanStore() {
      return beanStore;
    }   

    public Task getTask() {
      return task;
    }        
    public void setTask(Task task) {
      this.task = task;
    }
  }
  
  @ConversationScoped protected static class ConversationScopedAssociation extends ScopedAssociation implements Serializable {}
  @RequestScoped protected static class RequestScopedAssociation extends ScopedAssociation implements Serializable {}
  @ThreadScoped protected static class ThreadScopedAssociation extends ScopedAssociation implements Serializable {}
  
  @Inject private BeanManager beanManager;

  protected Class< ? extends ScopedAssociation> getBroadestActiveContext() {
    for (Class< ? extends ScopedAssociation> scopeType : getAvailableScopedAssociationClasses()) {
      Annotation scopeAnnotation = scopeType.getAnnotations().length > 0 ? scopeType.getAnnotations()[0] : null;
      if (scopeAnnotation == null || !beanManager.isScope(scopeAnnotation.annotationType())) {
        throw new ActivitiException("ScopedAssociation must carry exactly one annotation and it must be a @Scope annotation");
      }
      try {
        beanManager.getContext(scopeAnnotation.annotationType());
        return scopeType;
      } catch (ContextNotActiveException e) {
        log.finest("Context " + scopeAnnotation.annotationType() + " not active.");            
      }
    }
    throw new ActivitiException("Could not determine an active context to associate the current process instance / task instance with.");
  }
  
  /**
   * Override to add different / additional contexts.
   * 
   * @returns a list of {@link Scope}-types, which are used in the given order
   *          to resolve the broadest active context (@link
   *          #getBroadestActiveContext()})
   */
  protected List<Class< ? extends ScopedAssociation>> getAvailableScopedAssociationClasses() {
    ArrayList<Class< ? extends ScopedAssociation>> scopeTypes = new ArrayList<Class< ? extends ScopedAssociation>>();
    scopeTypes.add(ConversationScopedAssociation.class);
    scopeTypes.add(RequestScopedAssociation.class);
    scopeTypes.add(ThreadScopedAssociation.class);
    return scopeTypes;
  }
  
  protected ScopedAssociation getScopedAssociation() {
    return ProgrammaticBeanLookup.lookup(getBroadestActiveContext());
  }

  @Override
  public void associate(String executionId) {
    ScopedAssociation scopedAssociation = getScopedAssociation();
    if (log.isLoggable(Level.FINE)) {
      log.fine("Associating Execution[" + executionId + "] (@" 
                + scopedAssociation.getClass().getAnnotations()[0].annotationType().getSimpleName() + ")");
    }
    scopedAssociation.setExecutionId(executionId);
  }

  @Override
  public void disAssociate() {
    ScopedAssociation scopedAssociation = getScopedAssociation();
    if (scopedAssociation.getExecutionId() == null) {
      throw new ActivitiException("Cannot dissasociate execution, no " 
                + scopedAssociation.getClass().getAnnotations()[0].annotationType().getSimpleName()
                + " execution associated. ");
    }
    if (log.isLoggable(Level.FINE)) {
      log.fine("Disassociating");
    }
    scopedAssociation.setExecutionId(null);
    scopedAssociation.setTask(null);
    scopedAssociation.getBeanStore().clear();
  }
  
  @Override
  public String getExecutionId() {
    return getScopedAssociation().getExecutionId();
  }

  @Override
  public CachingBeanStore getBeanStore() {
    return getScopedAssociation().getBeanStore();
  }
    
  @Override
  public Task getTask() {    
    return getScopedAssociation().getTask();
  }
  
  @Override
  public void setTask(Task task) {
    getScopedAssociation().setTask(task);
  }

}

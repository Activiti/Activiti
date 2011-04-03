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

import org.activiti.cdi.impl.util.BeanManagerLookup;
import org.activiti.cdi.impl.util.ProgrammaticBeanLookup;
import org.activiti.engine.ActivitiException;

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
  
  Logger log = Logger.getLogger(DefaultBusinessProcessAssociationManager.class.getName());
  
  protected static class ScopedAssociation { 
    protected String processInstanceId;
    protected String taskId;        
    @Inject CachingBeanStore beanStore;
    public void setProcessInstanceId(String processInstanceId) {
      this.processInstanceId = processInstanceId;
    }    
    public void setTaskId(String taskId) {
      this.taskId = taskId;
    }    
    public String getTaskId() {
      return taskId;
    }    
    public String getProcessInstanceId() {
      return processInstanceId;
    }    
    public CachingBeanStore getBeanStore() {
      return beanStore;
    }
  }
  
  @ConversationScoped protected static class ConversationScopedAssociation extends ScopedAssociation implements Serializable {}
  @RequestScoped protected static class RequestScopedAssociation extends ScopedAssociation implements Serializable {}
  

  protected Class< ? extends ScopedAssociation> getBroadestActiveContext() {
    BeanManager beanManager = BeanManagerLookup.getBeanManager();
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
    return scopeTypes;
  }
  
  protected ScopedAssociation getScopedAssociation() {
    return ProgrammaticBeanLookup.lookup(getBroadestActiveContext());
  }

  @Override
  public void associateProcessInstance(String processInstanceId) {
    ScopedAssociation scopedAssociation = getScopedAssociation();
    if (log.isLoggable(Level.FINE)) {
      log.fine("Assossating ProcessInstance[" + processInstanceId + "] (@" 
                + scopedAssociation.getClass().getAnnotations()[0].annotationType().getSimpleName() + ")");
    }
    scopedAssociation.setProcessInstanceId(processInstanceId);
  }

  @Override
  public void disAssociateProcessInstance() {
    ScopedAssociation scopedAssociation = getScopedAssociation();
    if (scopedAssociation.getProcessInstanceId() == null) {
      throw new ActivitiException("Cannot dissasociate process instance, no " 
                + scopedAssociation.getClass().getAnnotations()[0].annotationType().getSimpleName()
                + " processinstance associated. ");
    }
    if (log.isLoggable(Level.FINE)) {
      log.fine("Disassociating the current task");
    }
    getScopedAssociation().setProcessInstanceId(null);
    getScopedAssociation().setTaskId(null);
    getBeanStore().clear();
  }

  @Override
  public void associateTask(String taskId) {
    ScopedAssociation scopedAssociation = getScopedAssociation();
    if (log.isLoggable(Level.FINE)) {
      log.fine("Assossating Task[" + taskId + "] (@" 
                + scopedAssociation.getClass().getAnnotations()[0].annotationType().getSimpleName() + ")");
    }
    scopedAssociation.setTaskId(taskId);
  }

  @Override
  public void disAssociateTask() {
    ScopedAssociation scopedAssociation = getScopedAssociation();
    if (scopedAssociation.getTaskId() == null) {
      throw new ActivitiException("Cannot dissasociate task, no " 
                + scopedAssociation.getClass().getAnnotations()[0].annotationType().getSimpleName()
                + " task associated. ");
    }
    if (log.isLoggable(Level.FINE)) {
      log.fine("Disassociating the current task");
    }
    getScopedAssociation().setTaskId(null);
    getBeanStore().clear();
  }

  @Override
  public String getProcessInstanceId() {        
    return getScopedAssociation().getProcessInstanceId();
  }

  @Override
  public String getTaskId() {
    return getScopedAssociation().getTaskId();
  }

  @Override
  public CachingBeanStore getBeanStore() {
    return getScopedAssociation().getBeanStore();
  }

}

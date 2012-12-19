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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Scope;

import org.activiti.cdi.ActivitiCdiException;
import org.activiti.cdi.impl.util.ProgrammaticBeanLookup;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.context.ExecutionContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class DefaultContextAssociationManager implements ContextAssociationManager, Serializable {
  
  private final static Logger log = LoggerFactory.getLogger(DefaultContextAssociationManager.class);
  
  protected static class ScopedAssociation { 
    
    @Inject 
    private RuntimeService runtimeService;
    
    protected Map<String, Object> cachedVariables = new HashMap<String, Object>();
    protected Execution execution;    
    protected Task task;
    
    public Execution getExecution() {
      return execution;
    }
    
    public void setExecution(Execution execution) {
      this.execution = execution;
    }
    
    public Task getTask() {
      return task;
    }
    
    public void setTask(Task task) {
      this.task = task;
    }

    public <T> T getVariable(String variableName) {
      Object value = cachedVariables.get(variableName);
      if(value == null) {
        if(execution != null) {
          value = runtimeService.getVariable(execution.getId(), variableName);
          cachedVariables.put(variableName, value);
        }
      }
      return (T) value;
    }

    public void setVariable(String variableName, Object value) {
      cachedVariables.put(variableName, value);
    }

    public Map<String, Object> getCachedVariables() {
      return cachedVariables;
    }
   
  }
  
  @ConversationScoped protected static class ConversationScopedAssociation extends ScopedAssociation implements Serializable {}
  @RequestScoped protected static class RequestScopedAssociation extends ScopedAssociation implements Serializable {}
  
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
        log.trace("Context {} not active.", scopeAnnotation.annotationType());            
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
    return ProgrammaticBeanLookup.lookup(getBroadestActiveContext(), beanManager);
  }

  @Override
  public void setExecution(Execution execution) {
    if(execution == null) {
      throw new ActivitiCdiException("Cannot associate with execution: null");
    }
    
    if(Context.getCommandContext() != null) {
      throw new ActivitiCdiException("Cannot work with scoped associations inside command context.");
    }
    
    ScopedAssociation scopedAssociation = getScopedAssociation();
    Execution associatedExecution = scopedAssociation.getExecution();
    if(associatedExecution!=null && !associatedExecution.getId().equals(execution.getId())) {
      throw new ActivitiCdiException("Cannot associate "+execution+", already associated with "+associatedExecution+". Disassociate first!");
    }
    
    if (log.isTraceEnabled()) {
      log.trace("Associating {} (@{})", execution, 
                scopedAssociation.getClass().getAnnotations()[0].annotationType().getSimpleName());
    }
    scopedAssociation.setExecution(execution);
  }

  @Override
  public void disAssociate() {
    if(Context.getCommandContext() != null) {
      throw new ActivitiCdiException("Cannot work with scoped associations inside command context.");
    }
    ScopedAssociation scopedAssociation = getScopedAssociation();
    if (scopedAssociation.getExecution() == null) {
      throw new ActivitiException("Cannot dissasociate execution, no " 
                + scopedAssociation.getClass().getAnnotations()[0].annotationType().getSimpleName()
                + " execution associated. ");
    }
    if (log.isTraceEnabled()) {
      log.trace("Disassociating");
    }
    scopedAssociation.setExecution(null);
    scopedAssociation.setTask(null);
  }
  
  @Override
  public String getExecutionId() {
    Execution execution = getExecution();
    if (execution != null) {
      return execution.getId();
    } else {
      return null;
    }
  }
  
  @Override
  public Execution getExecution() {
    ExecutionEntity execution = getExecutionFromContext();
    if(execution != null) {
      return execution;
    } else {
      return getScopedAssociation().getExecution();     
    }
  }

  @Override
  public Object getVariable(String variableName) {
    ExecutionEntity execution = getExecutionFromContext();
    if(execution != null) {
      return execution.getVariable(variableName);
    } else {
      return getScopedAssociation().getVariable(variableName);  
    }
  }
  
  @Override
  public void setVariable(String variableName, Object value) {
    ExecutionEntity execution = getExecutionFromContext();
    if(execution != null) {
      execution.setVariable(variableName, value);
      execution.getVariable(variableName);
    } else {
      getScopedAssociation().setVariable(variableName, value);  
    }
  }
  
  protected ExecutionEntity getExecutionFromContext() {
    if(Context.getCommandContext() != null) {
      ExecutionContext executionContext = Context.getExecutionContext();
      if(executionContext != null) {
        return executionContext.getExecution();
      }
    }
    return null;
  }

  public Task getTask() {    
    if(Context.getCommandContext() != null) {
      throw new ActivitiCdiException("Cannot work with tasks in an activiti command.");
    }
    return getScopedAssociation().getTask();
  }
  
  public void setTask(Task task) {
    if(Context.getCommandContext() != null) {
      throw new ActivitiCdiException("Cannot work with tasks in an activiti command.");
    }
    getScopedAssociation().setTask(task);
  }

  @Override
  public Map<String, Object> getCachedVariables() {
    if(Context.getCommandContext() != null) {
      throw new ActivitiCdiException("Cannot work with cached variables in an activiti command.");
    }
    return getScopedAssociation().getCachedVariables();
  }

}

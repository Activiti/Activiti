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

package org.activiti.engine.impl.bpmn.helper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.ServiceTaskJavaDelegateActivityBehavior;
import org.activiti.engine.impl.bpmn.parser.FieldDeclaration;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.delegate.ExecutionListenerInvocation;
import org.activiti.engine.impl.delegate.TaskListenerInvocation;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.delegate.SignallableActivityBehavior;
import org.activiti.engine.impl.util.ReflectUtil;


/**
 * Helper class for bpmn constructs that allow class delegation.
 *
 * This class will lazily instantiate the referenced classes when needed at runtime.
 * 
 * @author Joram Barrez
 */
public class ClassDelegate extends AbstractBpmnActivityBehavior implements TaskListener, ExecutionListener {
  
  protected String className;
  protected List<FieldDeclaration> fieldDeclarations;
  protected ExecutionListener executionListenerInstance;
  protected TaskListener taskListenerInstance;
  protected ActivityBehavior activityBehaviorInstance;
  
  public ClassDelegate(String className, List<FieldDeclaration> fieldDeclarations) {
    this.className = className;
    this.fieldDeclarations = fieldDeclarations;
  }
  
  public ClassDelegate(Class<?> clazz, List<FieldDeclaration> fieldDeclarations) {
    this(clazz.getName(), fieldDeclarations);
  }

  // Execution listener
  public void notify(DelegateExecution execution) throws Exception {
    if (executionListenerInstance == null) {
      executionListenerInstance = getExecutionListenerInstance();
    }
    Context.getProcessEngineConfiguration()
      .getDelegateInterceptor()
      .handleInvocation(new ExecutionListenerInvocation(executionListenerInstance, execution));
  }

  protected ExecutionListener getExecutionListenerInstance() {
    Object delegateInstance = instantiateDelegate(className, fieldDeclarations);
    if (delegateInstance instanceof ExecutionListener) {
      return (ExecutionListener) delegateInstance; 
    } else if (delegateInstance instanceof JavaDelegate) {
      return new ServiceTaskJavaDelegateActivityBehavior((JavaDelegate) delegateInstance);
    } else {
      throw new ActivitiException(delegateInstance.getClass().getName()+" doesn't implement "+ExecutionListener.class+" nor "+JavaDelegate.class);
    }
  }
  
  // Task listener
  public void notify(DelegateTask delegateTask) {
    if (taskListenerInstance == null) {
      taskListenerInstance = getTaskListenerInstance();
    }
    try {
      Context.getProcessEngineConfiguration()
        .getDelegateInterceptor()
        .handleInvocation(new TaskListenerInvocation(taskListenerInstance, delegateTask));
    }catch (Exception e) {
      throw new ActivitiException("Exception while invoking TaskListener: "+e.getMessage(), e);
    }
  }
  
  protected TaskListener getTaskListenerInstance() {
    Object delegateInstance = instantiateDelegate(className, fieldDeclarations);
    if (delegateInstance instanceof TaskListener) {
      return (TaskListener) delegateInstance; 
    } else {
      throw new ActivitiException(delegateInstance.getClass().getName()+" doesn't implement "+TaskListener.class);
    }
  }

  // Activity Behavior
  public void execute(ActivityExecution execution) throws Exception {
    if (activityBehaviorInstance == null) {
      activityBehaviorInstance = getActivityBehaviorInstance(execution);
    }
    activityBehaviorInstance.execute(execution);
  }
  
  // Signallable activity behavior
  public void signal(ActivityExecution execution, String signalName, Object signalData) throws Exception {
    if (activityBehaviorInstance == null) {
      activityBehaviorInstance = getActivityBehaviorInstance(execution);
    }
    
    if (activityBehaviorInstance instanceof SignallableActivityBehavior) {
      ((SignallableActivityBehavior) activityBehaviorInstance).signal(execution, signalName, signalData);
    } else {
      throw new ActivitiException("signal() can only be called on a " + SignallableActivityBehavior.class.getName() + " instance");
    }
  }

  protected ActivityBehavior getActivityBehaviorInstance(ActivityExecution execution) {
    Object delegateInstance = instantiateDelegate(className, fieldDeclarations);
    
    if (delegateInstance instanceof ActivityBehavior) {
      return determineBehaviour((ActivityBehavior) delegateInstance, execution);
    } else if (delegateInstance instanceof JavaDelegate) {
      return determineBehaviour(new ServiceTaskJavaDelegateActivityBehavior((JavaDelegate) delegateInstance), execution);
    } else {
      throw new ActivitiException(delegateInstance.getClass().getName()+" doesn't implement "+JavaDelegate.class.getName()+" nor "+ActivityBehavior.class.getName());
    }
  }
  
  // Adds properties to the given delegation instance (eg multi instance) if needed
  protected ActivityBehavior determineBehaviour(ActivityBehavior delegateInstance, ActivityExecution execution) {
    if (hasMultiInstanceCharacteristics()) {
      multiInstanceActivityBehavior.setInnerActivityBehavior((AbstractBpmnActivityBehavior) delegateInstance);
      return multiInstanceActivityBehavior;
    }
    return delegateInstance;
  }
  
  // --HELPER METHODS (also usable by external classes) ----------------------------------------
  
  public static Object instantiateDelegate(Class<?> clazz, List<FieldDeclaration> fieldDeclarations) {
    return instantiateDelegate(clazz.getName(), fieldDeclarations);
  }
  
  public static Object instantiateDelegate(String className, List<FieldDeclaration> fieldDeclarations) {
    Object object = ReflectUtil.instantiate(className);
    if(fieldDeclarations != null) {
      for(FieldDeclaration declaration : fieldDeclarations) {
        applyFieldDeclaration(declaration, object);
      }
    }
    return object;
  }
  
  private static void applyFieldDeclaration(FieldDeclaration declaration, Object target) {
    Method setterMethod = ReflectUtil.getSetter(declaration.getName(), 
      target.getClass(), declaration.getValue().getClass());
    
    if(setterMethod != null) {
      try {
        setterMethod.invoke(target, declaration.getValue());
      } catch (IllegalArgumentException e) {
        throw new ActivitiException("Error while invoking '" + declaration.getName() + "' on class " + target.getClass().getName(), e);
      } catch (IllegalAccessException e) {
        throw new ActivitiException("Illegal acces when calling '" + declaration.getName() + "' on class " + target.getClass().getName(), e);
      } catch (InvocationTargetException e) {
        throw new ActivitiException("Exception while invoking '" + declaration.getName() + "' on class " + target.getClass().getName(), e);
      }
    } else {
      Field field = ReflectUtil.getField(declaration.getName(), target);
      if(field == null) {
        throw new ActivitiException("Field definition uses unexisting field '" + declaration.getName() + "' on class " + target.getClass().getName());
      }
      // Check if the delegate field's type is correct
     if(!fieldTypeCompatible(declaration, field)) {
       throw new ActivitiException("Incompatible type set on field declaration '" + declaration.getName() 
          + "' for class " + target.getClass().getName() 
          + ". Declared value has type " + declaration.getValue().getClass().getName() 
          + ", while expecting " + field.getType().getName());
     }
     ReflectUtil.setField(field, target, declaration.getValue());
    }
  }
  
  public static boolean fieldTypeCompatible(FieldDeclaration declaration, Field field) {
    if(declaration.getValue() != null) {
      return field.getType().isAssignableFrom(declaration.getValue().getClass());
    } else {      
      // Null can be set any field type
      return true;
    }
  }

}

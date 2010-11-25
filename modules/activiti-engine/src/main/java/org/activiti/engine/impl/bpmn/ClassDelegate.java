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

package org.activiti.engine.impl.bpmn;

import java.lang.reflect.Field;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.bpmn.parser.FieldDeclaration;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.delegate.ExecutionListener;
import org.activiti.engine.impl.pvm.delegate.ExecutionListenerExecution;
import org.activiti.engine.impl.pvm.delegate.TaskListener;
import org.activiti.engine.impl.util.ReflectUtil;


/**
 * Helper class for bpmn constructs that allow class delegation.
 *
 * This class will lazily instantiate the referenced classes when needed at runtime.
 * 
 * @author Joram Barrez
 */
public class ClassDelegate implements ActivityBehavior, TaskListener, ExecutionListener {
  
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
  public void notify(ExecutionListenerExecution execution) throws Exception {
    if (executionListenerInstance == null) {
      executionListenerInstance = getExecutionListenerInstance();
    } 
    executionListenerInstance.notify(execution);
  }

  protected ExecutionListener getExecutionListenerInstance() {
    Object delegateInstance = instantiateDelegate(className, fieldDeclarations);
    if (delegateInstance instanceof ExecutionListener) {
      return (ExecutionListener) delegateInstance; 
    } else if (delegateInstance instanceof JavaDelegate) {
      return new JavaDelegateDelegate((JavaDelegate) delegateInstance);
    } else {
      throw new ActivitiException(delegateInstance.getClass().getName()+" doesn't implement "+ExecutionListener.class+" nor "+JavaDelegate.class);
    }
  }
  
  // Task listener
  public void notify(DelegateTask delegateTask) {
    if (taskListenerInstance == null) {
      taskListenerInstance = getTaskListenerInstance();
    }
    taskListenerInstance.notify(delegateTask);
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
      activityBehaviorInstance = getActivityBehaviorInstance();
    }
    activityBehaviorInstance.execute(execution);
  }

  protected ActivityBehavior getActivityBehaviorInstance() {
    Object delegateInstance = instantiateDelegate(className, fieldDeclarations);
    if (delegateInstance instanceof ActivityBehavior) {
      return (ActivityBehavior) delegateInstance;
    } else if (delegateInstance instanceof JavaDelegate) {
      return new JavaDelegateDelegate((JavaDelegate) delegateInstance);
    } else {
      throw new ActivitiException(delegateInstance.getClass().getName()+" doesn't implement "+JavaDelegate.class.getName()+" nor "+ActivityBehavior.class.getName());
    }
  }
  
  // --HELPER METHODS (also usable by external classes) ----------------------------------------
  
  public static Object instantiateDelegate(Class<?> clazz, List<FieldDeclaration> fieldDeclarations) {
    return instantiateDelegate(clazz.getName(), fieldDeclarations);
  }
  
  public static Object instantiateDelegate(String className, List<FieldDeclaration> fieldDeclarations) {
    Object object = ReflectUtil.instantiate(className);
    if(fieldDeclarations != null) {
      for(FieldDeclaration declaration : fieldDeclarations) {
        Field field = ReflectUtil.getField(declaration.getName(), object);
        if(field == null) {
          throw new ActivitiException("Field definition uses unexisting field '" + declaration.getName() + "' on class " + className);
        }
        // Check if the delegate field's type is correct
       if(!fieldTypeCompatible(declaration, field)) {
         throw new ActivitiException("Incompatible type set on field declaration '" + declaration.getName() 
            + "' for class " + className 
            + ". Declared value has type " + declaration.getValue().getClass().getName() 
            + ", while expecting " + field.getType().getName());
       }
       ReflectUtil.setField(field, object, declaration.getValue());
      }
    }
    return object;
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

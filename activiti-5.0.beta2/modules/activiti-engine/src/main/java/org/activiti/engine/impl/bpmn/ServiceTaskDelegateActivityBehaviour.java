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
import org.activiti.engine.impl.bpmn.parser.FieldDeclaration;
import org.activiti.engine.impl.el.ActivitiValueExpression;
import org.activiti.engine.impl.util.ReflectUtil;
import org.activiti.pvm.activity.ActivityBehavior;
import org.activiti.pvm.activity.ActivityExecution;
import org.activiti.pvm.activity.SignallableActivityBehavior;

/**
 * @author dsyer
 * @author Josh Long
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class ServiceTaskDelegateActivityBehaviour implements SignallableActivityBehavior {

  protected ActivitiValueExpression expression;
  protected ActivityBehavior activityBehaviorImplementation;
  protected List<FieldDeclaration> fieldDeclarations;

  public ServiceTaskDelegateActivityBehaviour(ActivitiValueExpression expression, List<FieldDeclaration> fieldDeclarations) {
    this.expression = expression;
    this.fieldDeclarations = fieldDeclarations;
  }
  
  public ServiceTaskDelegateActivityBehaviour(ActivityBehavior activityBehaviorImplementation, List<FieldDeclaration> fieldDeclarations) {
    this.activityBehaviorImplementation = activityBehaviorImplementation;
    this.fieldDeclarations = fieldDeclarations;
  }

  public void execute(ActivityExecution execution) throws Exception {
    Object object = null;
    
    if (activityBehaviorImplementation != null) {
      object = activityBehaviorImplementation;
    } else {
      object = expression.getValue(execution);

      // Classname is provided as String
      if (object instanceof String) {
        String className = (String) object;
        if (className != null) {
          object = ReflectUtil.instantiate(className);
        }
      }
    }
    
    if (object instanceof ActivityBehavior) {
      ActivityBehavior activityBehavior = (ActivityBehavior) object;
      injectFields(execution, object);
      activityBehavior.execute(execution);
    } else {
      throw new ActivitiException("Service " + object + " is used in a serviceTask, but does not" + " implement the "
              + ActivityBehavior.class.getCanonicalName() + " interface");
    }

  }

  private void injectFields(ActivityExecution execution, Object object) {
    if (fieldDeclarations != null) {
      for (FieldDeclaration fieldDeclaration : fieldDeclarations) {
        Field field = ReflectUtil.getField(fieldDeclaration.getName(), object);
        
        if (field == null) {
          throw new ActivitiException("Invalid field declaration " + fieldDeclaration.getName() 
                  + " not found on" + object.getClass().getCanonicalName());
        }
        
        // Only String is supported now, so nothing special needs to happen (conversion etc.)
        ReflectUtil.setField(field, object, fieldDeclaration.getValueExpression().getValue(execution));
      }
    }
  }

  public void signal(ActivityExecution execution, String signalName, Object signalData) throws Exception {
    Object object = expression.getValue(execution);

    if (object instanceof String) {
      String className = (String) object;
      if (className != null) {
        object = ReflectUtil.instantiate(className);
      }
    }

    if (object instanceof SignallableActivityBehavior) {
      ((SignallableActivityBehavior) object).signal(execution, signalName, signalData);
    } else {
      throw new ActivitiException("Service " + object + " is used in a serviceTask, but does not" + " implement the "
              + SignallableActivityBehavior.class.getCanonicalName() + " interface");
    }
  }
}

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

import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.bpmn.parser.FieldDeclaration;
import org.activiti.pvm.activity.ActivityBehavior;
import org.activiti.pvm.activity.ActivityExecution;
import org.activiti.pvm.activity.SignallableActivityBehavior;

/**
 * @author dsyer
 * @author Josh Long
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class ServiceTaskDelegateActivityBehaviour extends FieldDeclarationDelegate implements SignallableActivityBehavior {

  public ServiceTaskDelegateActivityBehaviour(String className, List<FieldDeclaration> fieldDeclarations) {
   super(className, fieldDeclarations);
  }
  
  public void execute(ActivityExecution execution) throws Exception {
    Object delegate = getDelegateInstance();
    if (delegate instanceof ActivityBehavior) {
      ActivityBehavior activityBehavior = (ActivityBehavior) delegate;
      activityBehavior.execute(execution);
    } else {
      throw new ActivitiException("Service " + delegate + " is used in a serviceTask, but does not" + " implement the "
              + ActivityBehavior.class.getCanonicalName() + " interface");
    }
  }

  public void signal(ActivityExecution execution, String signalName, Object signalData) throws Exception {
    Object delegate = getDelegateInstance();
    if (delegate instanceof SignallableActivityBehavior) {
      ((SignallableActivityBehavior) delegate).signal(execution, signalName, signalData);
    } else {
      throw new ActivitiException("Service of type '" + getDelegateClassName() + "' is used in a serviceTask, but does not" + " implement the "
              + SignallableActivityBehavior.class.getCanonicalName() + " interface");
    }
  }
}

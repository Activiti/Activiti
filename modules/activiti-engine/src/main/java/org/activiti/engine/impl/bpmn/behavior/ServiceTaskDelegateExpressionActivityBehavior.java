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
package org.activiti.engine.impl.bpmn.behavior;

import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.BpmnError;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.bpmn.helper.ClassDelegate;
import org.activiti.engine.impl.bpmn.helper.ErrorPropagation;
import org.activiti.engine.impl.bpmn.parser.FieldDeclaration;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.delegate.ActivityBehaviorInvocation;
import org.activiti.engine.impl.delegate.JavaDelegateInvocation;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.delegate.SignallableActivityBehavior;


/**
 * {@link ActivityBehavior} used when 'delegateExpression' is used
 * for a serviceTask.
 * 
 * @author Joram Barrez
 * @author Josh Long
 * @author Slawomir Wojtasiak (Patch for ACT-1159)
 * @author Falko Menge
 */
public class ServiceTaskDelegateExpressionActivityBehavior extends TaskActivityBehavior {
  
  protected Expression expression;
  private final List<FieldDeclaration> fieldDeclarations;
  
  public ServiceTaskDelegateExpressionActivityBehavior(Expression expression, List<FieldDeclaration> fieldDeclarations) {
    this.expression = expression;
    this.fieldDeclarations = fieldDeclarations;
  }

  @Override
  public void signal(ActivityExecution execution, String signalName, Object signalData) throws Exception {
    Object delegate = expression.getValue(execution);
    if( delegate instanceof SignallableActivityBehavior){
      ((SignallableActivityBehavior) delegate).signal( execution , signalName , signalData);
    }
  }

	public void execute(ActivityExecution execution) throws Exception {

    try {

      // Note: we can't cache the result of the expression, because the
      // execution can change: eg. delegateExpression='${mySpringBeanFactory.randomSpringBean()}'
      Object delegate = expression.getValue(execution);
      ClassDelegate.applyFieldDeclaration(fieldDeclarations, delegate);

      if (delegate instanceof ActivityBehavior) {
        Context.getProcessEngineConfiguration()
          .getDelegateInterceptor()
          .handleInvocation(new ActivityBehaviorInvocation((ActivityBehavior) delegate, execution));

      } else if (delegate instanceof JavaDelegate) {
        Context.getProcessEngineConfiguration()
          .getDelegateInterceptor()
          .handleInvocation(new JavaDelegateInvocation((JavaDelegate) delegate, execution));
        leave(execution);

      } else {
        throw new ActivitiException("Delegate expression " + expression
                + " did neither resolve to an implementation of " + ActivityBehavior.class
                + " nor " + JavaDelegate.class);
      }
    } catch (Exception exc) {

      Throwable cause = exc;
      BpmnError error = null;
      while (cause != null) {
        if (cause instanceof BpmnError) {
          error = (BpmnError) cause;
          break;
        }
        cause = cause.getCause();
      }

      if (error != null) {
        ErrorPropagation.propagateError(error, execution);
      } else {
        throw exc;
      }

    }
  }

}

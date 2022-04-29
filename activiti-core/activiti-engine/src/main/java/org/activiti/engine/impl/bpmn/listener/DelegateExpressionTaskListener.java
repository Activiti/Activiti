/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.impl.bpmn.listener;

import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.bpmn.helper.DelegateExpressionUtil;
import org.activiti.engine.impl.bpmn.parser.FieldDeclaration;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.delegate.invocation.TaskListenerInvocation;


public class DelegateExpressionTaskListener implements TaskListener {

  protected Expression expression;
  private final List<FieldDeclaration> fieldDeclarations;

  public DelegateExpressionTaskListener(Expression expression, List<FieldDeclaration> fieldDeclarations) {
    this.expression = expression;
    this.fieldDeclarations = fieldDeclarations;
  }

  public void notify(DelegateTask delegateTask) {
    Object delegate = DelegateExpressionUtil.resolveDelegateExpression(expression, delegateTask, fieldDeclarations);
    if (delegate instanceof TaskListener) {
      try {
        Context.getProcessEngineConfiguration().getDelegateInterceptor().handleInvocation(new TaskListenerInvocation((TaskListener) delegate, delegateTask));
      } catch (Exception e) {
        throw new ActivitiException("Exception while invoking TaskListener: " + e.getMessage(), e);
      }
    } else {
      throw new ActivitiIllegalArgumentException("Delegate expression " + expression + " did not resolve to an implementation of " + TaskListener.class);
    }
  }

  /**
   * returns the expression text for this task listener. Comes in handy if you want to check which listeners you already have.
   */
  public String getExpressionText() {
    return expression.getExpressionText();
  }

}

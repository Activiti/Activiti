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

package org.activiti.engine.impl.bpmn.behavior;

import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.DynamicBpmnConstants;
import org.activiti.engine.delegate.BpmnError;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.bpmn.helper.DelegateExpressionUtil;
import org.activiti.engine.impl.bpmn.helper.ErrorPropagation;
import org.activiti.engine.impl.bpmn.helper.SkipExpressionUtil;
import org.activiti.engine.impl.bpmn.parser.FieldDeclaration;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.delegate.ActivityBehavior;
import org.activiti.engine.impl.delegate.ActivityBehaviorInvocation;
import org.activiti.engine.impl.delegate.TriggerableActivityBehavior;
import org.activiti.engine.impl.delegate.invocation.JavaDelegateInvocation;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * {@link ActivityBehavior} used when 'delegateExpression' is used for a serviceTask.
 *
 */
public class ServiceTaskDelegateExpressionActivityBehavior extends TaskActivityBehavior {

  private static final long serialVersionUID = 1L;

  protected String serviceTaskId;
  protected Expression expression;
  protected Expression skipExpression;
  private final List<FieldDeclaration> fieldDeclarations;

  public ServiceTaskDelegateExpressionActivityBehavior(String serviceTaskId, Expression expression, Expression skipExpression, List<FieldDeclaration> fieldDeclarations) {
    this.serviceTaskId = serviceTaskId;
    this.expression = expression;
    this.skipExpression = skipExpression;
    this.fieldDeclarations = fieldDeclarations;
  }

  @Override
  public void trigger(DelegateExecution execution, String signalName, Object signalData) {
    Object delegate = DelegateExpressionUtil.resolveDelegateExpression(expression, execution, fieldDeclarations);
    if (delegate instanceof TriggerableActivityBehavior) {
      ((TriggerableActivityBehavior) delegate).trigger(execution, signalName, signalData);
    }
  }

  public void execute(DelegateExecution execution) {

    try {
      boolean isSkipExpressionEnabled = SkipExpressionUtil.isSkipExpressionEnabled(execution, skipExpression);
      if (!isSkipExpressionEnabled || (isSkipExpressionEnabled && !SkipExpressionUtil.shouldSkipFlowElement(execution, skipExpression))) {

        if (Context.getProcessEngineConfiguration().isEnableProcessDefinitionInfoCache()) {
          ObjectNode taskElementProperties = Context.getBpmnOverrideElementProperties(serviceTaskId, execution.getProcessDefinitionId());
          if (taskElementProperties != null && taskElementProperties.has(DynamicBpmnConstants.SERVICE_TASK_DELEGATE_EXPRESSION)) {
            String overrideExpression = taskElementProperties.get(DynamicBpmnConstants.SERVICE_TASK_DELEGATE_EXPRESSION).asText();
            if (StringUtils.isNotEmpty(overrideExpression) && !overrideExpression.equals(expression.getExpressionText())) {
              expression = Context.getProcessEngineConfiguration().getExpressionManager().createExpression(overrideExpression);
            }
          }
        }

        Object delegate = DelegateExpressionUtil.resolveDelegateExpression(expression, execution, fieldDeclarations);
        if (delegate instanceof ActivityBehavior) {

          if (delegate instanceof AbstractBpmnActivityBehavior) {
            ((AbstractBpmnActivityBehavior) delegate).setMultiInstanceActivityBehavior(getMultiInstanceActivityBehavior());
          }

          Context.getProcessEngineConfiguration().getDelegateInterceptor().handleInvocation(new ActivityBehaviorInvocation((ActivityBehavior) delegate, execution));

        } else if (delegate instanceof JavaDelegate) {
          Context.getProcessEngineConfiguration().getDelegateInterceptor().handleInvocation(new JavaDelegateInvocation((JavaDelegate) delegate, execution));
          leave(execution);

        } else {
          throw new ActivitiIllegalArgumentException("Delegate expression " + expression + " did neither resolve to an implementation of " + ActivityBehavior.class + " nor " + JavaDelegate.class);
        }
      } else {
        leave(execution);
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
        throw new ActivitiException(exc.getMessage(), exc);
      }

    }
  }

}

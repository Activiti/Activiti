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

import org.activiti.engine.ActivitiException;
import org.activiti.engine.DynamicBpmnConstants;
import org.activiti.engine.delegate.BpmnError;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.bpmn.helper.ErrorPropagation;
import org.activiti.engine.impl.bpmn.helper.SkipExpressionUtil;
import org.activiti.engine.impl.context.Context;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * ActivityBehavior that evaluates an expression when executed. Optionally, it sets the result of the expression as a variable on the execution.
 *





 */
public class ServiceTaskExpressionActivityBehavior extends TaskActivityBehavior {

  private static final long serialVersionUID = 1L;

  protected String serviceTaskId;
  protected Expression expression;
  protected Expression skipExpression;
  protected String resultVariable;

  public ServiceTaskExpressionActivityBehavior(String serviceTaskId, Expression expression, Expression skipExpression, String resultVariable) {
    this.serviceTaskId = serviceTaskId;
    this.expression = expression;
    this.skipExpression = skipExpression;
    this.resultVariable = resultVariable;
  }

  public void execute(DelegateExecution execution) {
    Object value = null;
    try {
      boolean isSkipExpressionEnabled = SkipExpressionUtil.isSkipExpressionEnabled(execution, skipExpression);
      if (!isSkipExpressionEnabled || (isSkipExpressionEnabled && !SkipExpressionUtil.shouldSkipFlowElement(execution, skipExpression))) {

        if (Context.getProcessEngineConfiguration().isEnableProcessDefinitionInfoCache()) {
          ObjectNode taskElementProperties = Context.getBpmnOverrideElementProperties(serviceTaskId, execution.getProcessDefinitionId());
          if (taskElementProperties != null && taskElementProperties.has(DynamicBpmnConstants.SERVICE_TASK_EXPRESSION)) {
            String overrideExpression = taskElementProperties.get(DynamicBpmnConstants.SERVICE_TASK_EXPRESSION).asText();
            if (StringUtils.isNotEmpty(overrideExpression) && !overrideExpression.equals(expression.getExpressionText())) {
              expression = Context.getProcessEngineConfiguration().getExpressionManager().createExpression(overrideExpression);
            }
          }
        }

        value = expression.getValue(execution);
        if (resultVariable != null) {
          execution.setVariable(resultVariable, value);
        }
      }

      leave(execution);
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
        throw new ActivitiException("Could not execute service task expression", exc);
      }
    }
  }
}

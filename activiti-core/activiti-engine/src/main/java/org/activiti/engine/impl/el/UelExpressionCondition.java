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


package org.activiti.engine.impl.el;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.Condition;

/**
 * {@link Condition} that resolves an UEL expression at runtime.
 *


 */
public class UelExpressionCondition implements Condition {

  protected Expression expression;

  public UelExpressionCondition(Expression expression) {
    this.expression = expression;
  }

  public boolean evaluate(String sequenceFlowId, DelegateExecution execution) {
    Object result = expression.getValue(execution);

    if (result == null) {
      throw new ActivitiException("condition expression returns null (sequenceFlowId: " + sequenceFlowId + " execution: " + execution + ")");
    }
    if (!(result instanceof Boolean)) {
      throw new ActivitiException("condition expression returns non-Boolean (sequenceFlowId: " + sequenceFlowId + " execution: " + execution + "): " + result + " (" + result.getClass().getName() + ")");
    }
    return (Boolean) result;
  }

}

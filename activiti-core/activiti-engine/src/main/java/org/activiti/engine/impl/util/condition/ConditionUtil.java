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
package org.activiti.engine.impl.util.condition;

import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.DynamicBpmnConstants;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.Condition;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.el.UelExpressionCondition;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**


 */
public class ConditionUtil {

  public static boolean hasTrueCondition(SequenceFlow sequenceFlow, DelegateExecution execution) {
    String conditionExpression = null;
    if (Context.getProcessEngineConfiguration().isEnableProcessDefinitionInfoCache()) {
      ObjectNode elementProperties = Context.getBpmnOverrideElementProperties(sequenceFlow.getId(), execution.getProcessDefinitionId());
      conditionExpression = getActiveValue(sequenceFlow.getConditionExpression(), DynamicBpmnConstants.SEQUENCE_FLOW_CONDITION, elementProperties);
    } else {
      conditionExpression = sequenceFlow.getConditionExpression();
    }

    if (StringUtils.isNotEmpty(conditionExpression)) {

      Expression expression = Context.getProcessEngineConfiguration().getExpressionManager().createExpression(conditionExpression);
      Condition condition = new UelExpressionCondition(expression);
      if (condition.evaluate(sequenceFlow.getId(), execution)) {
        return true;
      }

      return false;

    } else {
      return true;
    }

  }

  protected static String getActiveValue(String originalValue, String propertyName, ObjectNode elementProperties) {
    String activeValue = originalValue;
    if (elementProperties != null) {
      JsonNode overrideValueNode = elementProperties.get(propertyName);
      if (overrideValueNode != null) {
        if (overrideValueNode.isNull()) {
          activeValue = null;
        } else {
          activeValue = overrideValueNode.asText();
        }
      }
    }
    return activeValue;
  }

}

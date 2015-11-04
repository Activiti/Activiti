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

package org.activiti.engine.impl.el;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.DynamicBpmnConstants;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.Condition;
import org.activiti.engine.impl.context.Context;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;


/**
 * {@link Condition} that resolves an UEL expression at runtime.  
 * 
 * @author Joram Barrez
 * @author Frederik Heremans
 */
public class UelExpressionCondition implements Condition {
  
  private static final long serialVersionUID = 1L;
  
  protected String initialConditionExpression;
  
  public UelExpressionCondition(String conditionExpression) {
    this.initialConditionExpression = conditionExpression;
  }

  public boolean evaluate(String sequenceFlowId, DelegateExecution execution) {
    String conditionExpression = null;
    if (Context.getProcessEngineConfiguration().isEnableProcessDefinitionInfoCache()) {
      ObjectNode elementProperties = Context.getBpmnOverrideElementProperties(sequenceFlowId, execution.getProcessDefinitionId());
      conditionExpression = getActiveValue(initialConditionExpression, DynamicBpmnConstants.SEQUENCE_FLOW_CONDITION, elementProperties);
    } else {
      conditionExpression = initialConditionExpression;
    }

    Expression expression = Context.getProcessEngineConfiguration().getExpressionManager().createExpression(conditionExpression);
    Object result = expression.getValue(execution);
    
    if (result==null) {
      throw new ActivitiException("condition expression returns null");
    }
    if (! (result instanceof Boolean)) {
      throw new ActivitiException("condition expression returns non-Boolean: "+result+" ("+result.getClass().getName()+")");
    }
    return (Boolean) result;
  }
  
  protected String getActiveValue(String originalValue, String propertyName, ObjectNode elementProperties) {
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

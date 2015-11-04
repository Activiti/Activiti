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
package org.activiti.engine.impl.scripting;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.DynamicBpmnConstants;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.Condition;
import org.activiti.engine.impl.context.Context;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author Tom Baeyens
 */
public class ScriptCondition implements Condition {
  
  private static final long serialVersionUID = 1L;

  private final String expression;
  private final String language;

  public ScriptCondition(String expression, String language) {
    this.expression = expression;
    this.language = language;
  }

  public boolean evaluate(String sequenceFlowId, DelegateExecution execution) {
    String conditionExpression = null;
    if (Context.getProcessEngineConfiguration().isEnableProcessDefinitionInfoCache()) {
      ObjectNode elementProperties = Context.getBpmnOverrideElementProperties(sequenceFlowId, execution.getProcessDefinitionId());
      conditionExpression = getActiveValue(expression, DynamicBpmnConstants.SEQUENCE_FLOW_CONDITION, elementProperties);
    } else {
      conditionExpression = expression;
    }
    
    ScriptingEngines scriptingEngines = Context
      .getProcessEngineConfiguration()
      .getScriptingEngines();
    
    Object result = scriptingEngines.evaluate(conditionExpression, language, execution);
    if (result == null) {
      throw new ActivitiException("condition script returns null: " + expression);
    }
    if (!(result instanceof Boolean)) {
      throw new ActivitiException("condition script returns non-Boolean: " + result + " (" + result.getClass().getName() + ")");
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

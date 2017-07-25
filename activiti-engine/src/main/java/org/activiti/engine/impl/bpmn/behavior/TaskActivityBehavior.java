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

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Parent class for all BPMN 2.0 task types such as ServiceTask, ScriptTask, UserTask, etc.
 * 
 * When used on its own, it behaves just as a pass-through activity.
 * 

 */
public class TaskActivityBehavior extends AbstractBpmnActivityBehavior {

  private static final long serialVersionUID = 1L;

  protected String getActiveValue(String originalValue, String propertyName, ObjectNode taskElementProperties) {
    String activeValue = originalValue;
    if (taskElementProperties != null) {
      JsonNode overrideValueNode = taskElementProperties.get(propertyName);
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
  
  protected List<String> getActiveValueList(List<String> originalValues, String propertyName, ObjectNode taskElementProperties) {
    List<String> activeValues = originalValues;
    if (taskElementProperties != null) {
      JsonNode overrideValuesNode = taskElementProperties.get(propertyName);
      if (overrideValuesNode != null) {
        if (overrideValuesNode.isNull() || !overrideValuesNode.isArray()  || overrideValuesNode.size() == 0) {
          activeValues = null;
        } else {
          activeValues = new ArrayList<String>();
          for (JsonNode valueNode : overrideValuesNode) {
            activeValues.add(valueNode.asText());
          }
        }
      }
    }
    return activeValues;
  }
}

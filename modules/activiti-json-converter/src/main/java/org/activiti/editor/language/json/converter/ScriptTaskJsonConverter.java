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
package org.activiti.editor.language.json.converter;

import java.util.Map;

import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.ScriptTask;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author Tijs Rademakers
 */
public class ScriptTaskJsonConverter extends BaseBpmnJsonConverter {

  public static void fillTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap,
      Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
    
    fillJsonTypes(convertersToBpmnMap);
    fillBpmnTypes(convertersToJsonMap);
  }
  
  public static void fillJsonTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap) {
    convertersToBpmnMap.put(STENCIL_TASK_SCRIPT, ScriptTaskJsonConverter.class);
  }
  
  public static void fillBpmnTypes(Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
    convertersToJsonMap.put(ScriptTask.class, ScriptTaskJsonConverter.class);
  }
  
  protected String getStencilId(BaseElement baseElement) {
    return STENCIL_TASK_SCRIPT;
  }
  
  protected void convertElementToJson(ObjectNode propertiesNode, BaseElement baseElement) {
  	ScriptTask scriptTask = (ScriptTask) baseElement;
  	propertiesNode.put(PROPERTY_SCRIPT_FORMAT, scriptTask.getScriptFormat());
  	propertiesNode.put(PROPERTY_SCRIPT_TEXT, scriptTask.getScript());
  }
  
  protected FlowElement convertJsonToElement(JsonNode elementNode, JsonNode modelNode, Map<String, JsonNode> shapeMap) {
    ScriptTask task = new ScriptTask();
    task.setScriptFormat(getPropertyValueAsString(PROPERTY_SCRIPT_FORMAT, elementNode));
    task.setScript(getPropertyValueAsString(PROPERTY_SCRIPT_TEXT, elementNode));
    return task;
  }
}

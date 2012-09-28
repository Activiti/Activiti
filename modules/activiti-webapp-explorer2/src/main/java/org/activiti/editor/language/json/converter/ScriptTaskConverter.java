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

import org.activiti.editor.language.bpmn.model.ScriptTask;
import org.codehaus.jackson.node.ObjectNode;

/**
 * @author Tijs Rademakers
 */
public class ScriptTaskConverter extends BaseBpmnElementToJsonConverter {

  protected String getActivityType() {
    return STENCIL_TASK_SCRIPT;
  }
  
  protected void convertElement(ObjectNode propertiesNode) {
  	ScriptTask scriptTask = (ScriptTask) flowElement;
  	propertiesNode.put(PROPERTY_SCRIPT_FORMAT, scriptTask.getScriptFormat());
  	propertiesNode.put(PROPERTY_SCRIPT_TEXT, scriptTask.getScript());
  }
}

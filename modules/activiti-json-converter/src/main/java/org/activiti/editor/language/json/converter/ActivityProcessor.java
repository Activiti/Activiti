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

import java.util.Collection;
import java.util.Map;

import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;

/**
 * @author Tijs Rademakers
 */
public interface ActivityProcessor {

  public void processFlowElements(Collection<FlowElement> flowElements, BpmnModel model, ArrayNode shapesArrayNode, 
      double subProcessX, double subProcessY);
  
  public void processJsonElements(JsonNode shapesArrayNode, JsonNode modelNode, BaseElement parentElement, Map<String, JsonNode> shapeMap);
}

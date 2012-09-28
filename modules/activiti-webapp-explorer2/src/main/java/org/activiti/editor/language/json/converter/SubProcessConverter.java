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

import org.activiti.editor.language.bpmn.parser.GraphicInfo;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

/**
 * @author Tijs Rademakers
 */
public class SubProcessConverter extends BaseBpmnElementToJsonConverter {
  
  protected String getActivityType() {
    return STENCIL_SUB_PROCESS;
  }

  protected void convertElement(ObjectNode propertiesNode) {
    propertiesNode.put("activitytype", "Sub-Process");
    propertiesNode.put("subprocesstype", "Embedded");
    ArrayNode subProcessshapesArrayNode = objectMapper.createArrayNode();
    GraphicInfo graphicInfo = model.getGraphicInfo(flowElement.getId());
    processor.processFlowElements(process, model, shapesArrayNode, 
        graphicInfo.x + subProcessX, graphicInfo.y + subProcessY);
    flowElementNode.put("childShapes", subProcessshapesArrayNode);
  }
}

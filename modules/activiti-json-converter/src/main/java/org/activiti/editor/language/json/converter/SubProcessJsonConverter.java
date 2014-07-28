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
import org.activiti.bpmn.model.GraphicInfo;
import org.activiti.bpmn.model.SubProcess;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author Tijs Rademakers
 */
public class SubProcessJsonConverter extends BaseBpmnJsonConverter {
  
  public static void fillTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap,
      Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
    
    fillJsonTypes(convertersToBpmnMap);
    fillBpmnTypes(convertersToJsonMap);
  }
  
  public static void fillJsonTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap) {
    convertersToBpmnMap.put(STENCIL_SUB_PROCESS, SubProcessJsonConverter.class);
  }
  
  public static void fillBpmnTypes(Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
    convertersToJsonMap.put(SubProcess.class, SubProcessJsonConverter.class);
  }
  
  protected String getStencilId(FlowElement flowElement) {
    return STENCIL_SUB_PROCESS;
  }

  protected void convertElementToJson(ObjectNode propertiesNode, FlowElement flowElement) {
    SubProcess subProcess = (SubProcess) flowElement;
    propertiesNode.put("activitytype", "Sub-Process");
    propertiesNode.put("subprocesstype", "Embedded");
    ArrayNode subProcessShapesArrayNode = objectMapper.createArrayNode();
    GraphicInfo graphicInfo = model.getGraphicInfo(flowElement.getId());
    processor.processFlowElements(subProcess.getFlowElements(), model, subProcessShapesArrayNode, 
    		graphicInfo.getX(), graphicInfo.getY());
    flowElementNode.put("childShapes", subProcessShapesArrayNode);
  }
  
  protected FlowElement convertJsonToElement(JsonNode elementNode, JsonNode modelNode, Map<String, JsonNode> shapeMap) {
    SubProcess subProcess = new SubProcess();
    JsonNode childShapesArray = elementNode.get(EDITOR_CHILD_SHAPES);
    processor.processJsonElements(childShapesArray, modelNode, subProcess, shapeMap);
    return subProcess;
  }
}

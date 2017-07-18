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
import org.activiti.bpmn.model.EventSubProcess;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.GraphicInfo;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.editor.language.json.model.ModelInfo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**

 */
public class EventSubProcessJsonConverter extends BaseBpmnJsonConverter implements FormAwareConverter, FormKeyAwareConverter, 
    DecisionTableAwareConverter, DecisionTableKeyAwareConverter {
  
  protected Map<String, String> formMap;
  protected Map<String, ModelInfo> formKeyMap;
  protected Map<String, String> decisionTableMap;
  protected Map<String, ModelInfo> decisionTableKeyMap;

  public static void fillTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap, Map<Class<? extends BaseElement>, 
      Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {

    fillJsonTypes(convertersToBpmnMap);
    fillBpmnTypes(convertersToJsonMap);
  }

  public static void fillJsonTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap) {
    convertersToBpmnMap.put(STENCIL_EVENT_SUB_PROCESS, EventSubProcessJsonConverter.class);
  }

  public static void fillBpmnTypes(Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
    convertersToJsonMap.put(EventSubProcess.class, EventSubProcessJsonConverter.class);
  }

  protected String getStencilId(BaseElement baseElement) {
    return STENCIL_EVENT_SUB_PROCESS;
  }

  protected void convertElementToJson(ObjectNode propertiesNode, BaseElement baseElement) {
    SubProcess subProcess = (SubProcess) baseElement;
    propertiesNode.put("activitytype", "Event-Sub-Process");
    propertiesNode.put("subprocesstype", "Embedded");
    ArrayNode subProcessShapesArrayNode = objectMapper.createArrayNode();
    GraphicInfo graphicInfo = model.getGraphicInfo(subProcess.getId());
    processor.processFlowElements(subProcess, model, subProcessShapesArrayNode, formKeyMap, 
        decisionTableKeyMap, graphicInfo.getX(), graphicInfo.getY());
    flowElementNode.set("childShapes", subProcessShapesArrayNode);
  }

  protected FlowElement convertJsonToElement(JsonNode elementNode, JsonNode modelNode, Map<String, JsonNode> shapeMap) {
    EventSubProcess subProcess = new EventSubProcess();
    JsonNode childShapesArray = elementNode.get(EDITOR_CHILD_SHAPES);
    processor.processJsonElements(childShapesArray, modelNode, subProcess, shapeMap, formMap, decisionTableMap, model);
    return subProcess;
  }
  
  @Override
  public void setFormMap(Map<String, String> formMap) {
    this.formMap = formMap;
  }
  
  @Override
  public void setFormKeyMap(Map<String, ModelInfo> formKeyMap) {
    this.formKeyMap = formKeyMap;
  }
  
  @Override
  public void setDecisionTableMap(Map<String, String> decisionTableMap) {
    this.decisionTableMap = decisionTableMap;
  }
  
  @Override
  public void setDecisionTableKeyMap(Map<String, ModelInfo> decisionTableKeyMap) {
    this.decisionTableKeyMap = decisionTableKeyMap;
  }
}

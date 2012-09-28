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

import org.activiti.editor.language.bpmn.model.FlowElement;
import org.activiti.editor.language.bpmn.model.Process;
import org.activiti.editor.language.bpmn.model.SequenceFlow;
import org.activiti.editor.language.bpmn.parser.BpmnModel;
import org.activiti.editor.language.bpmn.parser.GraphicInfo;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

/**
 * @author Tijs Rademakers
 */
public class SequenceFlowConverter extends BaseBpmnElementToJsonConverter {

  protected String getActivityType() {
    return STENCIL_SEQUENCE_FLOW;
  }
  
  public void convert(FlowElement flowElement, ActivityProcessor processor,
      Process process, BpmnModel model, ArrayNode shapesArrayNode,
      double subProcessX, double subProcessY) {
    
    SequenceFlow sequenceFlow = (SequenceFlow) flowElement;
    ObjectNode flowNode = BpmnJsonConverterUtil.createChildShape(sequenceFlow.getId(), STENCIL_SEQUENCE_FLOW, 172, 212, 128, 212);
    ArrayNode dockersArrayNode = objectMapper.createArrayNode();
    ObjectNode dockNode = objectMapper.createObjectNode();
    dockNode.put(EDITOR_BOUNDS_X, model.getGraphicInfo(sequenceFlow.getSourceRef()).width / 2.0);
    dockNode.put(EDITOR_BOUNDS_Y, model.getGraphicInfo(sequenceFlow.getSourceRef()).height / 2.0);
    dockersArrayNode.add(dockNode);
    
    if (model.getFlowLocationGraphicInfo(sequenceFlow.getId()).size() > 2) {
      for (int i = 1; i < model.getFlowLocationGraphicInfo(sequenceFlow.getId()).size() - 1; i++) {
        GraphicInfo graphicInfo =  model.getFlowLocationGraphicInfo(sequenceFlow.getId()).get(i);
        dockNode = objectMapper.createObjectNode();
        dockNode.put(EDITOR_BOUNDS_X, graphicInfo.x);
        dockNode.put(EDITOR_BOUNDS_Y, graphicInfo.y);
        dockersArrayNode.add(dockNode);
      }
    }
    
    dockNode = objectMapper.createObjectNode();
    dockNode.put(EDITOR_BOUNDS_X, model.getGraphicInfo(sequenceFlow.getTargetRef()).width / 2.0);
    dockNode.put(EDITOR_BOUNDS_Y, model.getGraphicInfo(sequenceFlow.getTargetRef()).height / 2.0);
    dockersArrayNode.add(dockNode);
    flowNode.put("dockers", dockersArrayNode);
    ArrayNode outgoingArrayNode = objectMapper.createArrayNode();
    outgoingArrayNode.add(BpmnJsonConverterUtil.createResourceNode(sequenceFlow.getTargetRef()));
    flowNode.put("outgoing", outgoingArrayNode);
    flowNode.put("target", BpmnJsonConverterUtil.createResourceNode(sequenceFlow.getTargetRef()));
    shapesArrayNode.add(flowNode);
  }
  
  protected void convertElement(ObjectNode propertiesNode) {
    // nothing to do
  }
}

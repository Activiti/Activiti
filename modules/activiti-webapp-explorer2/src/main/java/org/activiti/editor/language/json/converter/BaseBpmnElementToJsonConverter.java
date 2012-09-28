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

import java.util.List;
import java.util.logging.Logger;

import org.activiti.editor.json.constants.EditorJsonConstants;
import org.activiti.editor.language.bpmn.export.ActivitiNamespaceConstants;
import org.activiti.editor.language.bpmn.model.Activity;
import org.activiti.editor.language.bpmn.model.BoundaryEvent;
import org.activiti.editor.language.bpmn.model.ErrorEventDefinition;
import org.activiti.editor.language.bpmn.model.Event;
import org.activiti.editor.language.bpmn.model.EventDefinition;
import org.activiti.editor.language.bpmn.model.FlowElement;
import org.activiti.editor.language.bpmn.model.FormProperty;
import org.activiti.editor.language.bpmn.model.Process;
import org.activiti.editor.language.bpmn.model.SequenceFlow;
import org.activiti.editor.language.bpmn.model.SignalEventDefinition;
import org.activiti.editor.language.bpmn.model.TimerEventDefinition;
import org.activiti.editor.language.bpmn.parser.BpmnModel;
import org.activiti.editor.language.bpmn.parser.GraphicInfo;
import org.activiti.editor.stencilset.StencilConstants;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

/**
 * @author Tijs Rademakers
 */
public abstract class BaseBpmnElementToJsonConverter implements EditorJsonConstants, StencilConstants, ActivitiNamespaceConstants {
  
  protected static final Logger LOGGER = Logger.getLogger(BaseBpmnElementToJsonConverter.class.getName());
  
  protected ObjectMapper objectMapper = new ObjectMapper();
  protected ActivityProcessor processor;
  protected BpmnModel model;
  protected Process process;
  protected FlowElement flowElement;
  protected ObjectNode flowElementNode;
  protected double subProcessX;
  protected double subProcessY;
  protected ArrayNode shapesArrayNode;

  public void convert(FlowElement flowElement, ActivityProcessor processor, Process process, BpmnModel model,
      ArrayNode shapesArrayNode, double subProcessX, double subProcessY) {
    
    this.model = model;
    this.flowElement = flowElement;
    this.processor = processor;
    this.process = process;
    this.subProcessX = subProcessX;
    this.subProcessY = subProcessY;
    this.shapesArrayNode = shapesArrayNode;
    GraphicInfo graphicInfo = model.getGraphicInfo(flowElement.getId());
    flowElementNode = BpmnJsonConverterUtil.createChildShape(flowElement.getId(), getActivityType(), 
        graphicInfo.x - subProcessX + graphicInfo.width, 
        graphicInfo.y - subProcessY + graphicInfo.height, 
        graphicInfo.x - subProcessX, graphicInfo.y - subProcessY);
    shapesArrayNode.add(flowElementNode);
    ObjectNode propertiesNode = objectMapper.createObjectNode();
    if (StringUtils.isNotEmpty(flowElement.getName())) {
      propertiesNode.put(PROPERTY_NAME, flowElement.getName());
    }
    
    if (StringUtils.isNotEmpty(flowElement.getDocumentation())) {
      propertiesNode.put(PROPERTY_DOCUMENTATION, flowElement.getDocumentation());
    }
    
    convertElement(propertiesNode);
    
    flowElementNode.put(EDITOR_SHAPE_PROPERTIES, propertiesNode);
    ArrayNode outgoingArrayNode = objectMapper.createArrayNode();
    for (SequenceFlow sequenceFlow : flowElement.getOutgoingFlows()) {
      outgoingArrayNode.add(BpmnJsonConverterUtil.createResourceNode(sequenceFlow.getId()));
    }
    
    if (flowElement instanceof Activity) {
      
      Activity activity = (Activity) flowElement;
      for (BoundaryEvent boundaryEvent : activity.getBoundaryEvents()) {
        outgoingArrayNode.add(BpmnJsonConverterUtil.createResourceNode(boundaryEvent.getId()));
      }
    }
    
    flowElementNode.put("outgoing", outgoingArrayNode);
  }
  
  protected abstract void convertElement(ObjectNode propertiesNode);
  
  protected abstract String getActivityType();
  
  protected void addFormProperties(List<FormProperty> formProperties, ObjectNode propertiesNode) {
    ObjectNode formPropertiesNode = objectMapper.createObjectNode();
    ArrayNode itemsNode = objectMapper.createArrayNode();
    for (FormProperty property : formProperties) {
      ObjectNode propertyItemNode = objectMapper.createObjectNode();
      propertyItemNode.put("formproperty_id", property.getId());
      propertyItemNode.put("formproperty_name", property.getName());
      propertyItemNode.put("formproperty_type", property.getType());
      if (StringUtils.isNotEmpty(property.getExpression())) {
        propertyItemNode.put("formproperty_expression", property.getExpression());
      } else {
        propertyItemNode.putNull("formproperty_expression");
      }
      if (StringUtils.isNotEmpty(property.getVariable())) {
        propertyItemNode.put("formproperty_variable", property.getVariable());
      } else {
        propertyItemNode.putNull("formproperty_variable");
      }
      
      itemsNode.add(propertyItemNode);
    }
    
    formPropertiesNode.put("totalCount", itemsNode.size());
    formPropertiesNode.put(EDITOR_PROPERTIES_GENERAL_ITEMS, itemsNode);
    propertiesNode.put("formproperties", formPropertiesNode);
  }
  
  protected void addEventProperties(Event event, ObjectNode propertiesNode) {
    List<EventDefinition> eventDefinitions = event.getEventDefinitions();
    if (eventDefinitions.size() == 1) {
    
      EventDefinition eventDefinition = eventDefinitions.get(0);
      if (eventDefinition instanceof ErrorEventDefinition) {
        ErrorEventDefinition errorDefinition = (ErrorEventDefinition) eventDefinition;
        if (StringUtils.isNotEmpty(errorDefinition.getErrorCode())) {
          propertiesNode.put(PROPERTY_ERRORREF, errorDefinition.getErrorCode());
        }
        
      } else if (eventDefinition instanceof SignalEventDefinition) {
        SignalEventDefinition signalDefinition = (SignalEventDefinition) eventDefinition;
        if (StringUtils.isNotEmpty(signalDefinition.getSignalRef())) {
          propertiesNode.put(PROPERTY_SIGNALREF, signalDefinition.getSignalRef());
        }
        
      } else if (eventDefinition instanceof TimerEventDefinition) {
        TimerEventDefinition timerDefinition = (TimerEventDefinition) eventDefinition;
        if (StringUtils.isNotEmpty(timerDefinition.getTimeDuration())) {
          propertiesNode.put(PROPERTY_TIMER_DURATON, timerDefinition.getTimeDuration());
        }
        if (StringUtils.isNotEmpty(timerDefinition.getTimeCycle())) {
          propertiesNode.put(PROPERTY_TIMER_CYCLE, timerDefinition.getTimeCycle());
        }
        if (StringUtils.isNotEmpty(timerDefinition.getTimeDate())) {
          propertiesNode.put(PROPERTY_TIMER_DATE, timerDefinition.getTimeDate());
        }
      }
    }
  }
}

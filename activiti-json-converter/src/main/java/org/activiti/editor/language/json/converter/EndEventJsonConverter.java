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
import java.util.Map;

import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.CancelEventDefinition;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.ErrorEventDefinition;
import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.TerminateEventDefinition;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**

 */
public class EndEventJsonConverter extends BaseBpmnJsonConverter {

  public static void fillTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap,
      Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {

    fillJsonTypes(convertersToBpmnMap);
    fillBpmnTypes(convertersToJsonMap);
  }

  public static void fillJsonTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap) {
    convertersToBpmnMap.put(STENCIL_EVENT_END_NONE, EndEventJsonConverter.class);
    convertersToBpmnMap.put(STENCIL_EVENT_END_ERROR, EndEventJsonConverter.class);
    convertersToBpmnMap.put(STENCIL_EVENT_END_CANCEL, EndEventJsonConverter.class);
    convertersToBpmnMap.put(STENCIL_EVENT_END_TERMINATE, EndEventJsonConverter.class);
  }

  public static void fillBpmnTypes(Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
    convertersToJsonMap.put(EndEvent.class, EndEventJsonConverter.class);
  }

  protected String getStencilId(BaseElement baseElement) {
    EndEvent endEvent = (EndEvent) baseElement;
    List<EventDefinition> eventDefinitions = endEvent.getEventDefinitions();
    if (eventDefinitions.size() != 1) {
      return STENCIL_EVENT_END_NONE;
    }

    EventDefinition eventDefinition = eventDefinitions.get(0);
    if (eventDefinition instanceof ErrorEventDefinition) {
      return STENCIL_EVENT_END_ERROR;
    } else if (eventDefinition instanceof CancelEventDefinition) {
      return STENCIL_EVENT_END_CANCEL;
    } else if (eventDefinition instanceof TerminateEventDefinition) {
      return STENCIL_EVENT_END_TERMINATE;
    } else {
      return STENCIL_EVENT_END_NONE;
    }
  }

  protected void convertElementToJson(ObjectNode propertiesNode, BaseElement baseElement) {
    EndEvent endEvent = (EndEvent) baseElement;
    addEventProperties(endEvent, propertiesNode);
  }

  protected FlowElement convertJsonToElement(JsonNode elementNode, JsonNode modelNode, Map<String, JsonNode> shapeMap) {
    EndEvent endEvent = new EndEvent();
    String stencilId = BpmnJsonConverterUtil.getStencilId(elementNode);
    if (STENCIL_EVENT_END_ERROR.equals(stencilId)) {
      convertJsonToErrorDefinition(elementNode, endEvent);
    } else if (STENCIL_EVENT_END_CANCEL.equals(stencilId)) {
      CancelEventDefinition eventDefinition = new CancelEventDefinition();
      endEvent.getEventDefinitions().add(eventDefinition);
    } else if (STENCIL_EVENT_END_TERMINATE.equals(stencilId)) {
      TerminateEventDefinition eventDefinition = new TerminateEventDefinition();
      
      String terminateAllStringValue = getPropertyValueAsString(PROPERTY_TERMINATE_ALL, elementNode);
      if (StringUtils.isNotEmpty(terminateAllStringValue)) {
     		eventDefinition.setTerminateAll("true".equals(terminateAllStringValue));
      }
      
      String terminateMiStringValue = getPropertyValueAsString(PROPERTY_TERMINATE_MULTI_INSTANCE, elementNode);
      if (StringUtils.isNotEmpty(terminateMiStringValue)) {
        eventDefinition.setTerminateMultiInstance("true".equals(terminateMiStringValue));
      }
      
      endEvent.getEventDefinitions().add(eventDefinition);
    }
    return endEvent;
  }
}

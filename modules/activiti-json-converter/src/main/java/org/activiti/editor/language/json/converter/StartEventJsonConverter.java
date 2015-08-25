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
import org.activiti.bpmn.model.ErrorEventDefinition;
import org.activiti.bpmn.model.Event;
import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.ExtensionElement;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.TimerEventDefinition;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author Tijs Rademakers
 */
public class StartEventJsonConverter extends BaseBpmnJsonConverter {

  public static void fillTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap, Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {

    fillJsonTypes(convertersToBpmnMap);
    fillBpmnTypes(convertersToJsonMap);
  }

  public static void fillJsonTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap) {
    convertersToBpmnMap.put(STENCIL_EVENT_START_NONE, StartEventJsonConverter.class);
    convertersToBpmnMap.put(STENCIL_EVENT_START_TIMER, StartEventJsonConverter.class);
    convertersToBpmnMap.put(STENCIL_EVENT_START_ERROR, StartEventJsonConverter.class);
    convertersToBpmnMap.put(STENCIL_EVENT_START_MESSAGE, StartEventJsonConverter.class);
    convertersToBpmnMap.put(STENCIL_EVENT_START_SIGNAL, StartEventJsonConverter.class);
  }

  public static void fillBpmnTypes(Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
    convertersToJsonMap.put(StartEvent.class, StartEventJsonConverter.class);
  }

  protected String getStencilId(BaseElement baseElement) {
    Event event = (Event) baseElement;
    if (event.getEventDefinitions().size() > 0) {
      EventDefinition eventDefinition = event.getEventDefinitions().get(0);
      if (eventDefinition instanceof TimerEventDefinition) {
        return STENCIL_EVENT_START_TIMER;
      } else if (eventDefinition instanceof ErrorEventDefinition) {
        return STENCIL_EVENT_START_ERROR;
      } else if (eventDefinition instanceof MessageEventDefinition) {
        return STENCIL_EVENT_START_MESSAGE;
      } else if (eventDefinition instanceof SignalEventDefinition) {
        return STENCIL_EVENT_START_SIGNAL;
      }
    }
    return STENCIL_EVENT_START_NONE;
  }

  protected void convertElementToJson(ObjectNode propertiesNode, BaseElement baseElement) {
    StartEvent startEvent = (StartEvent) baseElement;
    if (StringUtils.isNotEmpty(startEvent.getInitiator())) {
      propertiesNode.put(PROPERTY_NONE_STARTEVENT_INITIATOR, startEvent.getInitiator());
    }
    
    if (StringUtils.isNotEmpty(startEvent.getFormKey())) {
      List<ExtensionElement> formIdExtensions = startEvent.getExtensionElements().get("form-reference-id");
      List<ExtensionElement> formNameExtensions = startEvent.getExtensionElements().get("form-reference-name");
      if (CollectionUtils.isNotEmpty(formIdExtensions) && CollectionUtils.isNotEmpty(formNameExtensions)) {
          ObjectNode formRefNode = objectMapper.createObjectNode();
          formRefNode.put("id", Long.valueOf(formIdExtensions.get(0).getElementText()));
          formRefNode.put("name", formNameExtensions.get(0).getElementText());
          propertiesNode.put(PROPERTY_FORM_REFERENCE, formRefNode);
          
      } else if (startEvent.getFormKey().startsWith("FORM_REFERENCE")) {
          String formReference = startEvent.getFormKey().replace("FORM_REFERENCE", "");
          if (formReference.contains("_")) {
              String formIdString = formReference.substring(0, formReference.indexOf("_"));
              String formNameString = formReference.substring(formReference.indexOf("_") + 1);

              ObjectNode formRefNode = objectMapper.createObjectNode();
              formRefNode.put("id", Long.valueOf(formIdString));
              formRefNode.put("name", formNameString);
              propertiesNode.put(PROPERTY_FORM_REFERENCE, formRefNode);
          }

      } else {
          setPropertyValue(PROPERTY_FORMKEY, startEvent.getFormKey(), propertiesNode);
      }
  }
    
    addFormProperties(startEvent.getFormProperties(), propertiesNode);
    addEventProperties(startEvent, propertiesNode);
  }

  protected FlowElement convertJsonToElement(JsonNode elementNode, JsonNode modelNode, Map<String, JsonNode> shapeMap) {
    StartEvent startEvent = new StartEvent();
    startEvent.setInitiator(getPropertyValueAsString(PROPERTY_NONE_STARTEVENT_INITIATOR, elementNode));
    String stencilId = BpmnJsonConverterUtil.getStencilId(elementNode);
    if (STENCIL_EVENT_START_NONE.equals(stencilId)) {
      String formKey = getPropertyValueAsString(PROPERTY_FORMKEY, elementNode);
      if (StringUtils.isNotEmpty(formKey)) {
          startEvent.setFormKey(formKey);
      } else {
          JsonNode formReferenceNode = getProperty(PROPERTY_FORM_REFERENCE, elementNode);
          if (formReferenceNode != null && formReferenceNode.get("id") != null && formReferenceNode.get("name") != null) {
              startEvent.setFormKey(formReferenceNode.get("id").asText());
              addExtensionElement("form-reference-id", formReferenceNode.get("id").asText(), startEvent);
              addExtensionElement("form-reference-name", formReferenceNode.get("name").asText(), startEvent);
          }
      }
      convertJsonToFormProperties(elementNode, startEvent);

    } else if (STENCIL_EVENT_START_TIMER.equals(stencilId)) {
      convertJsonToTimerDefinition(elementNode, startEvent);
    } else if (STENCIL_EVENT_START_ERROR.equals(stencilId)) {
      convertJsonToErrorDefinition(elementNode, startEvent);
    } else if (STENCIL_EVENT_START_MESSAGE.equals(stencilId)) {
      convertJsonToMessageDefinition(elementNode, startEvent);
    } else if (STENCIL_EVENT_START_SIGNAL.equals(stencilId)) {
      convertJsonToSignalDefinition(elementNode, startEvent);
    }
    return startEvent;
  }
  
  protected void addExtensionElement(String name, String elementText, Event event) {
    ExtensionElement extensionElement = new ExtensionElement();
    extensionElement.setNamespace(NAMESPACE);
    extensionElement.setNamespacePrefix("modeler");
    extensionElement.setName(name);
    extensionElement.setElementText(elementText);
    event.addExtensionElement(extensionElement);
}
}

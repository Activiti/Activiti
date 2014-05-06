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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.ErrorEventDefinition;
import org.activiti.bpmn.model.Event;
import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.FieldExtension;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.FormProperty;
import org.activiti.bpmn.model.FormValue;
import org.activiti.bpmn.model.GraphicInfo;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.Lane;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.bpmn.model.MultiInstanceLoopCharacteristics;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.TimerEventDefinition;
import org.activiti.bpmn.model.UserTask;
import org.activiti.editor.constants.EditorJsonConstants;
import org.activiti.editor.constants.StencilConstants;
import org.activiti.editor.language.json.converter.util.JsonConverterUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author Tijs Rademakers
 */
public abstract class BaseBpmnJsonConverter implements EditorJsonConstants, StencilConstants {
  
  protected static final Logger LOGGER = LoggerFactory.getLogger(BaseBpmnJsonConverter.class);
  
  protected ObjectMapper objectMapper = new ObjectMapper();
  protected ActivityProcessor processor;
  protected BpmnModel model;
  protected ObjectNode flowElementNode;
  protected double subProcessX;
  protected double subProcessY;
  protected ArrayNode shapesArrayNode;

  public void convertToJson(FlowElement flowElement, ActivityProcessor processor, BpmnModel model,
      ArrayNode shapesArrayNode, double subProcessX, double subProcessY) {
    
    this.model = model;
    this.processor = processor;
    this.subProcessX = subProcessX;
    this.subProcessY = subProcessY;
    this.shapesArrayNode = shapesArrayNode;
    GraphicInfo graphicInfo = model.getGraphicInfo(flowElement.getId());
    
    String stencilId = null;
    if (flowElement instanceof ServiceTask) {
      ServiceTask serviceTask = (ServiceTask) flowElement;
      if ("mail".equalsIgnoreCase(serviceTask.getType())) {
        stencilId = STENCIL_TASK_MAIL;
      } else {
        stencilId = getStencilId(flowElement);
      }
    } else {
      stencilId = getStencilId(flowElement);
    }
    
    flowElementNode = BpmnJsonConverterUtil.createChildShape(flowElement.getId(), stencilId, 
        graphicInfo.getX() - subProcessX + graphicInfo.getWidth(), 
        graphicInfo.getY() - subProcessY + graphicInfo.getHeight(), 
        graphicInfo.getX() - subProcessX, graphicInfo.getY() - subProcessY);
    shapesArrayNode.add(flowElementNode);
    ObjectNode propertiesNode = objectMapper.createObjectNode();
    propertiesNode.put(PROPERTY_OVERRIDE_ID, flowElement.getId());
    if (StringUtils.isNotEmpty(flowElement.getName())) {
      propertiesNode.put(PROPERTY_NAME, flowElement.getName());
    }
    
    if (StringUtils.isNotEmpty(flowElement.getDocumentation())) {
      propertiesNode.put(PROPERTY_DOCUMENTATION, flowElement.getDocumentation());
    }
    
    convertElementToJson(propertiesNode, flowElement);
    
    flowElementNode.put(EDITOR_SHAPE_PROPERTIES, propertiesNode);
    ArrayNode outgoingArrayNode = objectMapper.createArrayNode();
    
    if (flowElement instanceof FlowNode) {
      FlowNode flowNode = (FlowNode) flowElement;
      for (SequenceFlow sequenceFlow : flowNode.getOutgoingFlows()) {
        outgoingArrayNode.add(BpmnJsonConverterUtil.createResourceNode(sequenceFlow.getId()));
      }
    }
    
    if (flowElement instanceof Activity) {
      
      Activity activity = (Activity) flowElement;
      for (BoundaryEvent boundaryEvent : activity.getBoundaryEvents()) {
        outgoingArrayNode.add(BpmnJsonConverterUtil.createResourceNode(boundaryEvent.getId()));
      }
      
      if (activity.isAsynchronous()) {
        propertiesNode.put(PROPERTY_ASYNCHRONOUS, PROPERTY_VALUE_YES);
      }
      
      if (activity.isNotExclusive()) {
        propertiesNode.put(PROPERTY_EXCLUSIVE, PROPERTY_VALUE_NO);
      }
      
      if (activity.getLoopCharacteristics() != null) {
        MultiInstanceLoopCharacteristics loopDef = activity.getLoopCharacteristics();
        if (StringUtils.isNotEmpty(loopDef.getLoopCardinality()) || StringUtils.isNotEmpty(loopDef.getInputDataItem()) ||
            StringUtils.isNotEmpty(loopDef.getCompletionCondition())) {
          
          if (loopDef.isSequential() == false) {
            propertiesNode.put(PROPERTY_MULTIINSTANCE_SEQUENTIAL, PROPERTY_VALUE_NO);
          }
          if (StringUtils.isNotEmpty(loopDef.getLoopCardinality())) {
            propertiesNode.put(PROPERTY_MULTIINSTANCE_CARDINALITY, loopDef.getLoopCardinality());
          }
          if (StringUtils.isNotEmpty(loopDef.getInputDataItem())) {
            propertiesNode.put(PROPERTY_MULTIINSTANCE_COLLECTION, loopDef.getInputDataItem());
          }
          if (StringUtils.isNotEmpty(loopDef.getElementVariable())) {
            propertiesNode.put(PROPERTY_MULTIINSTANCE_VARIABLE, loopDef.getElementVariable());
          }
          if (StringUtils.isNotEmpty(loopDef.getCompletionCondition())) {
            propertiesNode.put(PROPERTY_MULTIINSTANCE_CONDITION, loopDef.getCompletionCondition());
          }
        }
      }
      
      if (activity instanceof UserTask) {
        addListeners(((UserTask) activity).getTaskListeners(), false,  propertiesNode);
      } else {
        addListeners(activity.getExecutionListeners(), true,  propertiesNode);
      }
    }
    
    flowElementNode.put("outgoing", outgoingArrayNode);
  }
  
  public void convertToBpmnModel(JsonNode elementNode, JsonNode modelNode, 
      ActivityProcessor processor, BaseElement parentElement, Map<String, JsonNode> shapeMap) {
    
    this.processor = processor;
    
    FlowElement flowElement = convertJsonToElement(elementNode, modelNode, shapeMap);
    flowElement.setId(BpmnJsonConverterUtil.getElementId(elementNode));
    flowElement.setName(getPropertyValueAsString(PROPERTY_NAME, elementNode));
    flowElement.setDocumentation(getPropertyValueAsString(PROPERTY_DOCUMENTATION, elementNode));
    
    convertJsonToListeners(elementNode, flowElement);
    
    if (flowElement instanceof Activity) {
      Activity activity = (Activity) flowElement;
      activity.setAsynchronous(getPropertyValueAsBoolean(PROPERTY_ASYNCHRONOUS, elementNode));
      activity.setNotExclusive(!getPropertyValueAsBoolean(PROPERTY_EXCLUSIVE, elementNode));
      
      String multiInstanceCardinality = getPropertyValueAsString(PROPERTY_MULTIINSTANCE_CARDINALITY, elementNode);
      String multiInstanceCollection = getPropertyValueAsString(PROPERTY_MULTIINSTANCE_COLLECTION, elementNode);
      String multiInstanceCondition = getPropertyValueAsString(PROPERTY_MULTIINSTANCE_CONDITION, elementNode);
      String multiInstanceLoopType = getPropertyValueAsString(PROPERTY_MULTIINSTANCE_LOOP_TYPE, elementNode);
      
      if (StringUtils.isNotEmpty(multiInstanceCardinality) || StringUtils.isNotEmpty(multiInstanceCollection) ||
          StringUtils.isNotEmpty(multiInstanceCondition) || StringUtils.isNotEmpty(multiInstanceLoopType)) {
        
        String multiInstanceVariable = getPropertyValueAsString(PROPERTY_MULTIINSTANCE_VARIABLE, elementNode);
        
        MultiInstanceLoopCharacteristics multiInstanceObject = new MultiInstanceLoopCharacteristics();
        multiInstanceObject.setSequential(getPropertyValueAsBoolean(PROPERTY_MULTIINSTANCE_SEQUENTIAL, elementNode));
        
        // There is another property used for multi-instance sequential control, that is used for rendering the correct
        // BPMN loop symbol on the task. In case this is set, also take that into account.
        
        if(multiInstanceLoopType != null) {
        	if(PROPERTY_MULTIINSTANCE_LOOP_TYPE_SEQUENTIAL.equals(multiInstanceLoopType)) {
        		multiInstanceObject.setSequential(true);
        	} else if(PROPERTY_MULTIINSTANCE_LOOP_TYPE_PARALLEL.equals(multiInstanceLoopType)) {
        		multiInstanceObject.setSequential(false);
        	}
        }
        
        
        multiInstanceObject.setLoopCardinality(multiInstanceCardinality);
        multiInstanceObject.setInputDataItem(multiInstanceCollection);
        multiInstanceObject.setElementVariable(multiInstanceVariable);
        multiInstanceObject.setCompletionCondition(multiInstanceCondition);
        activity.setLoopCharacteristics(multiInstanceObject);
      }
    }
    if (parentElement instanceof Process) {
      ((Process) parentElement).addFlowElement(flowElement);
    } else if (parentElement instanceof SubProcess) {
      ((SubProcess) parentElement).addFlowElement(flowElement);
    } else if (parentElement instanceof Lane) {
      Lane lane = (Lane) parentElement;
      lane.getFlowReferences().add(flowElement.getId());
      lane.getParentProcess().addFlowElement(flowElement);
    }
  }
  
  protected abstract void convertElementToJson(ObjectNode propertiesNode, FlowElement flowElement);
  
  protected abstract FlowElement convertJsonToElement(JsonNode elementNode, JsonNode modelNode, Map<String, JsonNode> shapeMap);
  
  protected abstract String getStencilId(FlowElement flowElement);
  
  protected void setPropertyValue(String name, String value, ObjectNode propertiesNode) {
    if (StringUtils.isNotEmpty(value)) {
    	propertiesNode.put(name, value);
    }
  }
  
  protected void addFormProperties(List<FormProperty> formProperties, ObjectNode propertiesNode) {
    ObjectNode formPropertiesNode = objectMapper.createObjectNode();
    ArrayNode itemsNode = objectMapper.createArrayNode();
    for (FormProperty property : formProperties) {
      ObjectNode propertyItemNode = objectMapper.createObjectNode();
      propertyItemNode.put(PROPERTY_FORM_ID, property.getId());
      propertyItemNode.put(PROPERTY_FORM_NAME, property.getName());
      propertyItemNode.put(PROPERTY_FORM_TYPE, property.getType());
      if (StringUtils.isNotEmpty(property.getExpression())) {
        propertyItemNode.put(PROPERTY_FORM_EXPRESSION, property.getExpression());
      } else {
        propertyItemNode.putNull(PROPERTY_FORM_EXPRESSION);
      }
      if (StringUtils.isNotEmpty(property.getVariable())) {
        propertyItemNode.put(PROPERTY_FORM_VARIABLE, property.getVariable());
      } else {
        propertyItemNode.putNull(PROPERTY_FORM_VARIABLE);
      }
      
      propertyItemNode.put(PROPERTY_FORM_REQUIRED, property.isRequired() ? PROPERTY_VALUE_YES : PROPERTY_VALUE_NO);
      propertyItemNode.put(PROPERTY_FORM_READABLE, property.isReadable() ? PROPERTY_VALUE_YES : PROPERTY_VALUE_NO);
      propertyItemNode.put(PROPERTY_FORM_WRITEABLE, property.isWriteable() ? PROPERTY_VALUE_YES : PROPERTY_VALUE_NO);
      
      ObjectNode formValueNode = objectMapper.createObjectNode();
      ArrayNode formValueItemNode = objectMapper.createArrayNode();
      
      for (FormValue formValue : property.getFormValues()) {
        ObjectNode propertyFormValueNode = objectMapper.createObjectNode();
        propertyFormValueNode.put(PROPERTY_FORM_FORM_VALUE_ID, formValue.getId());
        propertyFormValueNode.put(PROPERTY_FORM_FORM_VALUE_NAME, formValue.getName());
        formValueItemNode.add(propertyFormValueNode);
      }
      formValueNode.put("totalCount", formValueItemNode.size());
      formValueNode.put(EDITOR_PROPERTIES_GENERAL_ITEMS, formValueItemNode);
      propertyItemNode.put(PROPERTY_FORM_FORM_VALUES, formValueNode.toString());
      
      itemsNode.add(propertyItemNode);
    }
    
    formPropertiesNode.put("totalCount", itemsNode.size());
    formPropertiesNode.put(EDITOR_PROPERTIES_GENERAL_ITEMS, itemsNode);
    propertiesNode.put("formproperties", formPropertiesNode);
  }
  
  protected void addListeners(List<ActivitiListener> listeners, boolean isExecutionListener, ObjectNode propertiesNode) {
    
    String propertyName = null;
    String eventType = null;
    String listenerClass = null;
    String listenerExpression = null;
    String listenerDelegateExpression = null;
    
    if (isExecutionListener) {
      propertyName = PROPERTY_EXECUTION_LISTENERS;
      eventType = PROPERTY_EXECUTION_LISTENER_EVENT;
      listenerClass = PROPERTY_EXECUTION_LISTENER_CLASS;
      listenerExpression = PROPERTY_EXECUTION_LISTENER_EXPRESSION;
      listenerDelegateExpression = PROPERTY_EXECUTION_LISTENER_DELEGATEEXPRESSION;
      
    } else {
      propertyName = PROPERTY_TASK_LISTENERS;
      eventType = PROPERTY_TASK_LISTENER_EVENT;
      listenerClass = PROPERTY_TASK_LISTENER_CLASS;
      listenerExpression = PROPERTY_TASK_LISTENER_EXPRESSION;
      listenerDelegateExpression = PROPERTY_TASK_LISTENER_DELEGATEEXPRESSION;
    }
    
    ObjectNode listenersNode = objectMapper.createObjectNode();
    ArrayNode itemsNode = objectMapper.createArrayNode();
    for (ActivitiListener listener : listeners) {
      ObjectNode propertyItemNode = objectMapper.createObjectNode();
      
      propertyItemNode.put(eventType, listener.getEvent());
      
      if (ImplementationType.IMPLEMENTATION_TYPE_CLASS.equals(listener.getImplementationType())) {
        propertyItemNode.put(listenerClass, listener.getImplementation());
      } else if (ImplementationType.IMPLEMENTATION_TYPE_EXPRESSION.equals(listener.getImplementationType())) {
        propertyItemNode.put(listenerExpression, listener.getImplementation());
      } else if (ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION.equals(listener.getImplementationType())) {
        propertyItemNode.put(listenerDelegateExpression, listener.getImplementation());
      }
      
      itemsNode.add(propertyItemNode);
    }
    
    listenersNode.put("totalCount", itemsNode.size());
    listenersNode.put(EDITOR_PROPERTIES_GENERAL_ITEMS, itemsNode);
    propertiesNode.put(propertyName, listenersNode);
  }
  
  protected void addFieldExtensions(List<FieldExtension> extensions, ObjectNode propertiesNode) {
    ObjectNode fieldExtensionsNode = objectMapper.createObjectNode();
    ArrayNode itemsNode = objectMapper.createArrayNode();
    for (FieldExtension extension : extensions) {
      ObjectNode propertyItemNode = objectMapper.createObjectNode();
      propertyItemNode.put(PROPERTY_SERVICETASK_FIELD_NAME, extension.getFieldName());
      if (StringUtils.isNotEmpty(extension.getStringValue())) {
        propertyItemNode.put(PROPERTY_SERVICETASK_FIELD_VALUE, extension.getStringValue());
      }
      if (StringUtils.isNotEmpty(extension.getExpression())) {
        propertyItemNode.put(PROPERTY_SERVICETASK_FIELD_EXPRESSION, extension.getExpression());
      }
      itemsNode.add(propertyItemNode);
    }
    
    fieldExtensionsNode.put("totalCount", itemsNode.size());
    fieldExtensionsNode.put(EDITOR_PROPERTIES_GENERAL_ITEMS, itemsNode);
    propertiesNode.put(PROPERTY_SERVICETASK_FIELDS, fieldExtensionsNode);
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
        
      } else if (eventDefinition instanceof MessageEventDefinition) {
        MessageEventDefinition messageDefinition = (MessageEventDefinition) eventDefinition;
        if (StringUtils.isNotEmpty(messageDefinition.getMessageRef())) {
          String messageRef = messageDefinition.getMessageRef();
          // remove the namespace from the message id if set
          if (messageRef.startsWith(model.getTargetNamespace())) {
            messageRef = messageRef.replace(model.getTargetNamespace(), "");
            messageRef = messageRef.replaceFirst(":", "");
          } else {
            for (String prefix : model.getNamespaces().keySet()) {
              String namespace = model.getNamespace(prefix);
              if (messageRef.startsWith(namespace)) {
                messageRef = messageRef.replace(model.getTargetNamespace(), "");
                messageRef = prefix + messageRef;
              }
            }
          }
          propertiesNode.put(PROPERTY_MESSAGEREF, messageRef);
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
  
  protected void convertJsonToFormProperties(JsonNode objectNode, BaseElement element) {
    
    JsonNode formPropertiesNode = getProperty(PROPERTY_FORM_PROPERTIES, objectNode);
    if (formPropertiesNode != null) {
      if (formPropertiesNode.isValueNode() && StringUtils.isNotEmpty(formPropertiesNode.asText())) {
        try {
          formPropertiesNode = objectMapper.readTree(formPropertiesNode.asText());
        } catch (Exception e) {
          LOGGER.info("Form properties node can not be read", e);
        }
      }
      JsonNode itemsArrayNode = formPropertiesNode.get(EDITOR_PROPERTIES_GENERAL_ITEMS);
      String readWriteReqNode = null;
      JsonNode formValuesNode = null;
      JsonNode formValuesArrayNode = null;
      if (itemsArrayNode != null) {
        for (JsonNode formNode : itemsArrayNode) {
          JsonNode formIdNode = formNode.get(PROPERTY_FORM_ID);
          if (formIdNode != null && StringUtils.isNotEmpty(formIdNode.asText())) {
            
            FormProperty formProperty = new FormProperty();
            formProperty.setId(formIdNode.asText());
            formProperty.setName(getValueAsString(PROPERTY_FORM_NAME, formNode));
            formProperty.setType(getValueAsString(PROPERTY_FORM_TYPE, formNode));
            formProperty.setExpression(getValueAsString(PROPERTY_FORM_EXPRESSION, formNode));
            formProperty.setVariable(getValueAsString(PROPERTY_FORM_VARIABLE, formNode));
            readWriteReqNode = getValueAsString(PROPERTY_FORM_REQUIRED, formNode);
            if (PROPERTY_VALUE_YES.equalsIgnoreCase(readWriteReqNode))
              formProperty.setRequired(true);
            readWriteReqNode = getValueAsString(PROPERTY_FORM_READABLE, formNode);
            if (PROPERTY_VALUE_NO.equalsIgnoreCase(readWriteReqNode))
              formProperty.setReadable(false);
            readWriteReqNode = getValueAsString(PROPERTY_FORM_WRITEABLE, formNode);
            if (PROPERTY_VALUE_NO.equalsIgnoreCase(readWriteReqNode))
                formProperty.setWriteable(false);
            
            formValuesNode = formNode.get(PROPERTY_FORM_FORM_VALUES);
            if (formValuesNode != null && StringUtils.isNotEmpty(formValuesNode.asText()) && !("undefined".equals(formValuesNode.asText()))) {
              if (formValuesNode.isValueNode()) {
                try {
                  formValuesNode = objectMapper.readTree(formValuesNode.asText());
                } catch (Exception e) {
                  LOGGER.info("Form properties values node can not be read", e);
                }
              }
              formValuesArrayNode = formValuesNode.get(EDITOR_PROPERTIES_GENERAL_ITEMS);
              List<FormValue> formValues = new ArrayList<FormValue>();
              for (JsonNode valueNode : formValuesArrayNode) {
                JsonNode valueIdNode = valueNode.get(PROPERTY_FORM_FORM_VALUE_ID);
                if (valueIdNode != null && StringUtils.isNotEmpty(valueIdNode.asText())) {
                  FormValue formValue = new FormValue();
                  formValue.setId(valueIdNode.asText());
                  formValue.setName(getValueAsString(PROPERTY_FORM_FORM_VALUE_NAME, valueNode));
                  formValues.add(formValue);
                }
              }
              formProperty.setFormValues(formValues);
            }
            
            if (element instanceof StartEvent) {
              ((StartEvent) element).getFormProperties().add(formProperty);
            } else if (element instanceof UserTask) {
              ((UserTask) element).getFormProperties().add(formProperty);
            }
          }
        }
      }
    }
  }
  
  protected void convertJsonToListeners(JsonNode objectNode, BaseElement element) {
    JsonNode listenersNode = null;
    
    String propertyName = null;
    String eventType = null;
    String listenerClass = null;
    String listenerExpression = null;
    String listenerDelegateExpression = null;
    String listenerFields = null;
    String listenerFieldName = null;
    String listenerFieldValue = null;
    String listenerFieldExpression = null;
    
    JsonNode listenerFieldsNode = null;
    JsonNode listenerFieldsArrayNode = null;
    
    if (element instanceof UserTask) {
      propertyName = PROPERTY_TASK_LISTENERS;
      eventType = PROPERTY_TASK_LISTENER_EVENT;
      listenerClass = PROPERTY_TASK_LISTENER_CLASS;
      listenerExpression = PROPERTY_TASK_LISTENER_EXPRESSION;
      listenerDelegateExpression = PROPERTY_TASK_LISTENER_DELEGATEEXPRESSION;
      listenerFields = PROPERTY_TASK_LISTENER_FIELDS;
      listenerFieldName = PROPERTY_TASK_LISTENER_FIELD_NAME;
      listenerFieldValue = PROPERTY_TASK_LISTENER_FIELD_VALUE;
      listenerFieldExpression = PROPERTY_TASK_LISTENER_EXPRESSION;
      
    } else {
      propertyName = PROPERTY_EXECUTION_LISTENERS;
      eventType = PROPERTY_EXECUTION_LISTENER_EVENT;
      listenerClass = PROPERTY_EXECUTION_LISTENER_CLASS;
      listenerExpression = PROPERTY_EXECUTION_LISTENER_EXPRESSION;
      listenerDelegateExpression = PROPERTY_EXECUTION_LISTENER_DELEGATEEXPRESSION;
      listenerFields = PROPERTY_EXECUTION_LISTENER_FIELDS;
      listenerFieldName = PROPERTY_EXECUTION_LISTENER_FIELD_NAME;
      listenerFieldValue = PROPERTY_EXECUTION_LISTENER_FIELD_VALUE;
      listenerFieldExpression = PROPERTY_EXECUTION_LISTENER_EXPRESSION;
    }
    
    listenersNode = getProperty(propertyName, objectNode);
    
    if (listenersNode != null) {
    
      if (listenersNode.isValueNode() && StringUtils.isNotEmpty(listenersNode.asText())) {
        try {
          listenersNode = objectMapper.readTree(listenersNode.asText());
        } catch (Exception e) {
          LOGGER.info("Listeners node can not be read", e);
        }
      }
      
      JsonNode itemsArrayNode = listenersNode.get(EDITOR_PROPERTIES_GENERAL_ITEMS);
      if (itemsArrayNode != null) {
        for (JsonNode itemNode : itemsArrayNode) {
          JsonNode typeNode = itemNode.get(eventType);
          if (typeNode != null && StringUtils.isNotEmpty(typeNode.asText())) {
            
            ActivitiListener listener = new ActivitiListener();
            listener.setEvent(typeNode.asText());
            if (StringUtils.isNotEmpty(getValueAsString(listenerClass, itemNode))) {
              listener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
              listener.setImplementation(getValueAsString(listenerClass, itemNode));
            } else if (StringUtils.isNotEmpty(getValueAsString(listenerExpression, itemNode))) {
              listener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_EXPRESSION);
              listener.setImplementation(getValueAsString(listenerExpression, itemNode));
            } else if (StringUtils.isNotEmpty(getValueAsString(listenerDelegateExpression, itemNode))) {
              listener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION);
              listener.setImplementation(getValueAsString(listenerDelegateExpression, itemNode));
            }
            
            listenerFieldsNode = itemNode.get(listenerFields);
            if (listenerFieldsNode != null && StringUtils.isNotEmpty(listenerFieldsNode.asText()) && !("undefined".equals(listenerFieldsNode.asText()))){
              if(listenerFieldsNode.isValueNode()){
                try{
                  listenerFieldsNode = objectMapper.readTree(listenerFieldsNode.asText());
                } catch(Exception e){
                  LOGGER.info("Listener fields node can not be read", e);
                }
              }
            }
            if (listenerFieldsNode != null) {
              listenerFieldsArrayNode = listenerFieldsNode.get(EDITOR_PROPERTIES_GENERAL_ITEMS);
              List<FieldExtension> fields = new ArrayList<FieldExtension>();
              if (listenerFieldsArrayNode != null) {
                for (JsonNode fieldNode : listenerFieldsArrayNode){
                  JsonNode fieldNameNode = fieldNode.get(listenerFieldName);
                  if (fieldNameNode != null && StringUtils.isNotEmpty(fieldNameNode.asText())){
                    FieldExtension field = new FieldExtension();
                    field.setFieldName(fieldNameNode.asText());
                    field.setStringValue(getValueAsString(listenerFieldValue, fieldNode));
                    field.setExpression(getValueAsString(listenerFieldExpression, fieldNode));
                    fields.add(field);
                  }
                }
              }
              listener.setFieldExtensions(fields);
            }
            
            if (element instanceof Process) {
              ((Process) element).getExecutionListeners().add(listener);
            } else if (element instanceof SequenceFlow) {
              ((SequenceFlow) element).getExecutionListeners().add(listener);
            } else if (element instanceof UserTask) {
              ((UserTask) element).getTaskListeners().add(listener);
            } else if (element instanceof Activity) {
              ((Activity) element).getExecutionListeners().add(listener);
            }
          }
        }
      }
    }
  }
  
  protected void convertJsonToTimerDefinition(JsonNode objectNode, Event event) {
    
    String timeDate = getPropertyValueAsString(PROPERTY_TIMER_DATE, objectNode);
    String timeCycle = getPropertyValueAsString(PROPERTY_TIMER_CYCLE, objectNode);
    String timeDuration = getPropertyValueAsString(PROPERTY_TIMER_DURATON, objectNode);
    
    if (StringUtils.isNotEmpty(timeDate) || StringUtils.isNotEmpty(timeCycle) || StringUtils.isNotEmpty(timeDuration)) {
    
      TimerEventDefinition eventDefinition = new TimerEventDefinition();
      if (StringUtils.isNotEmpty(timeDate)) {
        eventDefinition.setTimeDate(timeDate);
        
      } else if (StringUtils.isNotEmpty(timeCycle)) {
        eventDefinition.setTimeCycle(timeCycle);
        
      } else if (StringUtils.isNotEmpty(timeDuration)) {
        eventDefinition.setTimeDuration(timeDuration);
      }
      
      event.getEventDefinitions().add(eventDefinition);
    }
  }
  
  protected void convertJsonToSignalDefinition(JsonNode objectNode, Event event) {
    String signalRef = getPropertyValueAsString(PROPERTY_SIGNALREF, objectNode);
    
    if (StringUtils.isNotEmpty(signalRef)) {
      SignalEventDefinition eventDefinition = new SignalEventDefinition();
      eventDefinition.setSignalRef(signalRef);
      event.getEventDefinitions().add(eventDefinition);
    }
  }
  
  protected void convertJsonToMessageDefinition(JsonNode objectNode, Event event) {
    String messageRef = getPropertyValueAsString(PROPERTY_MESSAGEREF, objectNode);
    
    if (StringUtils.isNotEmpty(messageRef)) {
      MessageEventDefinition eventDefinition = new MessageEventDefinition();
      eventDefinition.setMessageRef(messageRef);
      event.getEventDefinitions().add(eventDefinition);
    }
  }
  
  protected void convertJsonToErrorDefinition(JsonNode objectNode, Event event) {
    String errorRef = getPropertyValueAsString(PROPERTY_ERRORREF, objectNode);
    
    if (StringUtils.isNotEmpty(errorRef)) {
      ErrorEventDefinition eventDefinition = new ErrorEventDefinition();
      eventDefinition.setErrorCode(errorRef);
      event.getEventDefinitions().add(eventDefinition);
    }
  }
  
  protected String getValueAsString(String name, JsonNode objectNode) {
    String propertyValue = null;
    JsonNode propertyNode = objectNode.get(name);
    if (propertyNode != null && "null".equalsIgnoreCase(propertyNode.asText()) == false) {
      propertyValue = propertyNode.asText();
    }
    return propertyValue;
  }
  
  protected List<String> getValueAsList(String name, JsonNode objectNode) {
    List<String> resultList = new ArrayList<String>();
    String propertyValue = getValueAsString(name, objectNode);
    if (propertyValue != null) {
      String[] valueList = propertyValue.split(",");
      for (String value : valueList) {
        resultList.add(value.trim());
      }
    }
    return resultList;
  }
  
  protected String getPropertyValueAsString(String name, JsonNode objectNode) {
    return JsonConverterUtil.getPropertyValueAsString(name, objectNode);
  }
  
  protected boolean getPropertyValueAsBoolean(String name, JsonNode objectNode) {
    return JsonConverterUtil.getPropertyValueAsBoolean(name, objectNode);
  }
  
  protected boolean getPropertyValueAsBoolean(String name, JsonNode objectNode, boolean defaultValue) {
    return JsonConverterUtil.getPropertyValueAsBoolean(name, objectNode, defaultValue);
  }
  
  protected List<String> getPropertyValueAsList(String name, JsonNode objectNode) {
    return JsonConverterUtil.getPropertyValueAsList(name, objectNode);
  }
  
  protected JsonNode getProperty(String name, JsonNode objectNode) {
    return JsonConverterUtil.getProperty(name, objectNode);
  }
  
  protected String convertListToCommaSeparatedString(List<String> stringList) {
    String resultString = null;
    if (stringList  != null && stringList.size() > 0) {
      StringBuilder expressionBuilder = new StringBuilder();
      for (String singleItem : stringList) {
        if (expressionBuilder.length() > 0) {
          expressionBuilder.append(",");
        } 
        expressionBuilder.append(singleItem);
      }
      resultString = expressionBuilder.toString();
    }
    return resultString;
  }
}

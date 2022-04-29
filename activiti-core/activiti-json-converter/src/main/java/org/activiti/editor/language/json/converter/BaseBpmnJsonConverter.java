/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.Artifact;
import org.activiti.bpmn.model.Association;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.DataAssociation;
import org.activiti.bpmn.model.DataStoreReference;
import org.activiti.bpmn.model.ErrorEventDefinition;
import org.activiti.bpmn.model.Event;
import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.ExtensionElement;
import org.activiti.bpmn.model.FieldExtension;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowElementsContainer;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.FormProperty;
import org.activiti.bpmn.model.FormValue;
import org.activiti.bpmn.model.Gateway;
import org.activiti.bpmn.model.GraphicInfo;
import org.activiti.bpmn.model.Lane;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.bpmn.model.MessageFlow;
import org.activiti.bpmn.model.MultiInstanceLoopCharacteristics;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.TerminateEventDefinition;
import org.activiti.bpmn.model.TimerEventDefinition;
import org.activiti.bpmn.model.UserTask;
import org.activiti.editor.constants.EditorJsonConstants;
import org.activiti.editor.constants.StencilConstants;
import org.activiti.editor.language.json.converter.util.CollectionUtils;
import org.activiti.editor.language.json.converter.util.JsonConverterUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class BaseBpmnJsonConverter implements EditorJsonConstants,
                                                       StencilConstants {

    protected static final Logger LOGGER = LoggerFactory.getLogger(BaseBpmnJsonConverter.class);

    public static final String NAMESPACE = "http://activiti.com/modeler";

    protected ObjectMapper objectMapper = new ObjectMapper();
    protected ActivityProcessor processor;
    protected BpmnModel model;
    protected ObjectNode flowElementNode;
    protected double subProcessX;
    protected double subProcessY;
    protected ArrayNode shapesArrayNode;

    public void convertToJson(BaseElement baseElement,
                              ActivityProcessor processor,
                              BpmnModel model,
                              FlowElementsContainer container,
                              ArrayNode shapesArrayNode,
                              double subProcessX,
                              double subProcessY) {

        this.model = model;
        this.processor = processor;
        this.subProcessX = subProcessX;
        this.subProcessY = subProcessY;
        this.shapesArrayNode = shapesArrayNode;
        GraphicInfo graphicInfo = model.getGraphicInfo(baseElement.getId());

        String stencilId = null;
        if (baseElement instanceof ServiceTask) {
            ServiceTask serviceTask = (ServiceTask) baseElement;
            if ("mail".equalsIgnoreCase(serviceTask.getType())) {
                stencilId = STENCIL_TASK_MAIL;
            } else if ("camel".equalsIgnoreCase(serviceTask.getType())) {
                stencilId = STENCIL_TASK_CAMEL;
            } else if ("mule".equalsIgnoreCase(serviceTask.getType())) {
                stencilId = STENCIL_TASK_MULE;
            } else if ("dmn".equalsIgnoreCase(serviceTask.getType())) {
                stencilId = STENCIL_TASK_DECISION;
            } else {
                stencilId = getStencilId(baseElement);
            }
        } else {
            stencilId = getStencilId(baseElement);
        }

        flowElementNode = BpmnJsonConverterUtil.createChildShape(baseElement.getId(),
                                                                 stencilId,
                                                                 graphicInfo.getX() - subProcessX + graphicInfo.getWidth(),
                                                                 graphicInfo.getY() - subProcessY + graphicInfo.getHeight(),
                                                                 graphicInfo.getX() - subProcessX,
                                                                 graphicInfo.getY() - subProcessY);
        shapesArrayNode.add(flowElementNode);
        ObjectNode propertiesNode = objectMapper.createObjectNode();
        propertiesNode.put(PROPERTY_OVERRIDE_ID, baseElement.getId());

        if (baseElement instanceof FlowElement) {
            FlowElement flowElement = (FlowElement) baseElement;
            if (StringUtils.isNotEmpty(flowElement.getName())) {
                propertiesNode.put(PROPERTY_NAME, flowElement.getName());
            }

            if (StringUtils.isNotEmpty(flowElement.getDocumentation())) {
                propertiesNode.put(PROPERTY_DOCUMENTATION, flowElement.getDocumentation());
            }
        }

        convertElementToJson(propertiesNode, baseElement);

        flowElementNode.set(EDITOR_SHAPE_PROPERTIES, propertiesNode);
        ArrayNode outgoingArrayNode = objectMapper.createArrayNode();

        if (baseElement instanceof FlowNode) {
            FlowNode flowNode = (FlowNode) baseElement;
            for (SequenceFlow sequenceFlow : flowNode.getOutgoingFlows()) {
                outgoingArrayNode.add(BpmnJsonConverterUtil.createResourceNode(sequenceFlow.getId()));
            }

            for (MessageFlow messageFlow : model.getMessageFlows().values()) {
                if (messageFlow.getSourceRef().equals(flowNode.getId())) {
                    outgoingArrayNode.add(BpmnJsonConverterUtil.createResourceNode(messageFlow.getId()));
                }
            }
        }

        if (baseElement instanceof Activity) {

            Activity activity = (Activity) baseElement;
            for (BoundaryEvent boundaryEvent : activity.getBoundaryEvents()) {
                outgoingArrayNode.add(BpmnJsonConverterUtil.createResourceNode(boundaryEvent.getId()));
            }

            propertiesNode.put(PROPERTY_ASYNCHRONOUS,
                               activity.isAsynchronous());
            propertiesNode.put(PROPERTY_EXCLUSIVE,
                               !activity.isNotExclusive());

            if (activity.getLoopCharacteristics() != null) {
                MultiInstanceLoopCharacteristics loopDef = activity.getLoopCharacteristics();
                if (StringUtils.isNotEmpty(loopDef.getLoopCardinality()) || StringUtils.isNotEmpty(loopDef.getInputDataItem()) || StringUtils.isNotEmpty(loopDef.getCompletionCondition())) {

                    if (!loopDef.isSequential()) {
                        propertiesNode.put(PROPERTY_MULTIINSTANCE_TYPE,
                                           "Parallel");
                    } else {
                        propertiesNode.put(PROPERTY_MULTIINSTANCE_TYPE,
                                           "Sequential");
                    }

                    if (StringUtils.isNotEmpty(loopDef.getLoopCardinality())) {
                        propertiesNode.put(PROPERTY_MULTIINSTANCE_CARDINALITY,
                                           loopDef.getLoopCardinality());
                    }
                    if (StringUtils.isNotEmpty(loopDef.getInputDataItem())) {
                        propertiesNode.put(PROPERTY_MULTIINSTANCE_COLLECTION,
                                           loopDef.getInputDataItem());
                    }
                    if (StringUtils.isNotEmpty(loopDef.getElementVariable())) {
                        propertiesNode.put(PROPERTY_MULTIINSTANCE_VARIABLE,
                                           loopDef.getElementVariable());
                    }
                    if (StringUtils.isNotEmpty(loopDef.getCompletionCondition())) {
                        propertiesNode.put(PROPERTY_MULTIINSTANCE_CONDITION,
                                           loopDef.getCompletionCondition());
                    }
                }
            }

            if (activity instanceof UserTask) {
                BpmnJsonConverterUtil.convertListenersToJson(((UserTask) activity).getTaskListeners(),
                                                             false,
                                                             propertiesNode);
            }

            if (CollectionUtils.isNotEmpty(activity.getDataInputAssociations())) {
                for (DataAssociation dataAssociation : activity.getDataInputAssociations()) {
                    if (model.getFlowElement(dataAssociation.getSourceRef()) != null) {
                        createDataAssociation(dataAssociation,
                                              true,
                                              activity);
                    }
                }
            }

            if (CollectionUtils.isNotEmpty(activity.getDataOutputAssociations())) {
                for (DataAssociation dataAssociation : activity.getDataOutputAssociations()) {
                    if (model.getFlowElement(dataAssociation.getTargetRef()) != null) {
                        createDataAssociation(dataAssociation,
                                              false,
                                              activity);
                        outgoingArrayNode.add(BpmnJsonConverterUtil.createResourceNode(dataAssociation.getId()));
                    }
                }
            }
        }

        if (baseElement instanceof FlowElement) {
            BpmnJsonConverterUtil.convertListenersToJson(((FlowElement) baseElement).getExecutionListeners(),
                                                         true,
                                                         propertiesNode);
        }

        for (Artifact artifact : container.getArtifacts()) {
            if (artifact instanceof Association) {
                Association association = (Association) artifact;
                if (StringUtils.isNotEmpty(association.getSourceRef()) && association.getSourceRef().equals(baseElement.getId())) {
                    outgoingArrayNode.add(BpmnJsonConverterUtil.createResourceNode(association.getId()));
                }
            }
        }

        if (baseElement instanceof DataStoreReference) {
            for (Process process : model.getProcesses()) {
                processDataStoreReferences(process,
                                           baseElement.getId(),
                                           outgoingArrayNode);
            }
        }

        flowElementNode.set("outgoing",
                            outgoingArrayNode);
    }

    protected void processDataStoreReferences(FlowElementsContainer container,
                                              String dataStoreReferenceId,
                                              ArrayNode outgoingArrayNode) {
        for (FlowElement flowElement : container.getFlowElements()) {
            if (flowElement instanceof Activity) {
                Activity activity = (Activity) flowElement;

                if (CollectionUtils.isNotEmpty(activity.getDataInputAssociations())) {
                    for (DataAssociation dataAssociation : activity.getDataInputAssociations()) {
                        if (dataStoreReferenceId.equals(dataAssociation.getSourceRef())) {
                            outgoingArrayNode.add(BpmnJsonConverterUtil.createResourceNode(dataAssociation.getId()));
                        }
                    }
                }
            } else if (flowElement instanceof SubProcess) {
                processDataStoreReferences((SubProcess) flowElement,
                                           dataStoreReferenceId,
                                           outgoingArrayNode);
            }
        }
    }

    protected void createDataAssociation(DataAssociation dataAssociation,
                                         boolean incoming,
                                         Activity activity) {
        String sourceRef = null;
        String targetRef = null;
        if (incoming) {
            sourceRef = dataAssociation.getSourceRef();
            targetRef = activity.getId();
        } else {
            sourceRef = activity.getId();
            targetRef = dataAssociation.getTargetRef();
        }

        ObjectNode flowNode = BpmnJsonConverterUtil.createChildShape(dataAssociation.getId(),
                                                                     STENCIL_DATA_ASSOCIATION,
                                                                     172,
                                                                     212,
                                                                     128,
                                                                     212);
        ArrayNode dockersArrayNode = objectMapper.createArrayNode();
        ObjectNode dockNode = objectMapper.createObjectNode();

        dockNode.put(EDITOR_BOUNDS_X,
                     model.getGraphicInfo(sourceRef).getWidth() / 2.0);
        dockNode.put(EDITOR_BOUNDS_Y,
                     model.getGraphicInfo(sourceRef).getHeight() / 2.0);
        dockersArrayNode.add(dockNode);

        if (model.getFlowLocationGraphicInfo(dataAssociation.getId()).size() > 2) {
            for (int i = 1; i < model.getFlowLocationGraphicInfo(dataAssociation.getId()).size() - 1; i++) {
                GraphicInfo graphicInfo = model.getFlowLocationGraphicInfo(dataAssociation.getId()).get(i);
                dockNode = objectMapper.createObjectNode();
                dockNode.put(EDITOR_BOUNDS_X,
                             graphicInfo.getX());
                dockNode.put(EDITOR_BOUNDS_Y,
                             graphicInfo.getY());
                dockersArrayNode.add(dockNode);
            }
        }

        dockNode = objectMapper.createObjectNode();
        dockNode.put(EDITOR_BOUNDS_X,
                     model.getGraphicInfo(targetRef).getWidth() / 2.0);
        dockNode.put(EDITOR_BOUNDS_Y,
                     model.getGraphicInfo(targetRef).getHeight() / 2.0);
        dockersArrayNode.add(dockNode);
        flowNode.set("dockers",
                     dockersArrayNode);
        ArrayNode outgoingArrayNode = objectMapper.createArrayNode();
        outgoingArrayNode.add(BpmnJsonConverterUtil.createResourceNode(targetRef));
        flowNode.set("outgoing",
                     outgoingArrayNode);
        flowNode.set("target",
                     BpmnJsonConverterUtil.createResourceNode(targetRef));

        ObjectNode propertiesNode = objectMapper.createObjectNode();
        propertiesNode.put(PROPERTY_OVERRIDE_ID,
                           dataAssociation.getId());

        flowNode.set(EDITOR_SHAPE_PROPERTIES,
                     propertiesNode);
        shapesArrayNode.add(flowNode);
    }

    public void convertToBpmnModel(JsonNode elementNode,
                                   JsonNode modelNode,
                                   ActivityProcessor processor,
                                   BaseElement parentElement,
                                   Map<String, JsonNode> shapeMap,
                                   BpmnModel bpmnModel) {

        this.processor = processor;
        this.model = bpmnModel;

        BaseElement baseElement = convertJsonToElement(elementNode,
                                                       modelNode,
                                                       shapeMap);
        baseElement.setId(BpmnJsonConverterUtil.getElementId(elementNode));

        if (baseElement instanceof FlowElement) {
            FlowElement flowElement = (FlowElement) baseElement;
            flowElement.setName(getPropertyValueAsString(PROPERTY_NAME,
                                                         elementNode));
            flowElement.setDocumentation(getPropertyValueAsString(PROPERTY_DOCUMENTATION,
                                                                  elementNode));

            BpmnJsonConverterUtil.convertJsonToListeners(elementNode,
                                                         flowElement);

            if (baseElement instanceof Activity) {
                Activity activity = (Activity) baseElement;
                activity.setAsynchronous(getPropertyValueAsBoolean(PROPERTY_ASYNCHRONOUS,
                                                                   elementNode));
                activity.setNotExclusive(!getPropertyValueAsBoolean(PROPERTY_EXCLUSIVE,
                                                                    elementNode));

                String multiInstanceType = getPropertyValueAsString(PROPERTY_MULTIINSTANCE_TYPE,
                                                                    elementNode);
                String multiInstanceCardinality = getPropertyValueAsString(PROPERTY_MULTIINSTANCE_CARDINALITY,
                                                                           elementNode);
                String multiInstanceCollection = getPropertyValueAsString(PROPERTY_MULTIINSTANCE_COLLECTION,
                                                                          elementNode);
                String multiInstanceCondition = getPropertyValueAsString(PROPERTY_MULTIINSTANCE_CONDITION,
                                                                         elementNode);

                if (StringUtils.isNotEmpty(multiInstanceType) && !"none".equalsIgnoreCase(multiInstanceType)) {

                    String multiInstanceVariable = getPropertyValueAsString(PROPERTY_MULTIINSTANCE_VARIABLE,
                                                                            elementNode);

                    MultiInstanceLoopCharacteristics multiInstanceObject = new MultiInstanceLoopCharacteristics();
                    if ("sequential".equalsIgnoreCase(multiInstanceType)) {
                        multiInstanceObject.setSequential(true);
                    } else {
                        multiInstanceObject.setSequential(false);
                    }
                    multiInstanceObject.setLoopCardinality(multiInstanceCardinality);
                    multiInstanceObject.setInputDataItem(multiInstanceCollection);
                    multiInstanceObject.setElementVariable(multiInstanceVariable);
                    multiInstanceObject.setCompletionCondition(multiInstanceCondition);
                    activity.setLoopCharacteristics(multiInstanceObject);
                }
            } else if (baseElement instanceof Gateway) {
                JsonNode flowOrderNode = getProperty(PROPERTY_SEQUENCEFLOW_ORDER,
                                                     elementNode);
                if (flowOrderNode != null) {
                    flowOrderNode = BpmnJsonConverterUtil.validateIfNodeIsTextual(flowOrderNode);
                    JsonNode orderArray = flowOrderNode.get("sequenceFlowOrder");
                    if (orderArray != null && orderArray.size() > 0) {
                        for (JsonNode orderNode : orderArray) {
                            ExtensionElement orderElement = new ExtensionElement();
                            orderElement.setName("EDITOR_FLOW_ORDER");
                            orderElement.setElementText(orderNode.asText());
                            flowElement.addExtensionElement(orderElement);
                        }
                    }
                }
            }
        }

        if (baseElement instanceof FlowElement) {
            FlowElement flowElement = (FlowElement) baseElement;
            if (flowElement instanceof SequenceFlow) {
                ExtensionElement idExtensionElement = new ExtensionElement();
                idExtensionElement.setName("EDITOR_RESOURCEID");
                idExtensionElement.setElementText(elementNode.get(EDITOR_SHAPE_ID).asText());
                flowElement.addExtensionElement(idExtensionElement);
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
        } else if (baseElement instanceof Artifact) {
            Artifact artifact = (Artifact) baseElement;
            if (parentElement instanceof Process) {
                ((Process) parentElement).addArtifact(artifact);
            } else if (parentElement instanceof SubProcess) {
                ((SubProcess) parentElement).addArtifact(artifact);
            } else if (parentElement instanceof Lane) {
                Lane lane = (Lane) parentElement;
                lane.getFlowReferences().add(artifact.getId());
                lane.getParentProcess().addArtifact(artifact);
            }
        }
    }

    protected abstract void convertElementToJson(ObjectNode propertiesNode,
                                                 BaseElement baseElement);

    protected abstract BaseElement convertJsonToElement(JsonNode elementNode,
                                                        JsonNode modelNode,
                                                        Map<String, JsonNode> shapeMap);

    protected abstract String getStencilId(BaseElement baseElement);

    protected void setPropertyValue(String name,
                                    String value,
                                    ObjectNode propertiesNode) {
        if (StringUtils.isNotEmpty(value)) {
            propertiesNode.put(name,
                               value);
        }
    }

    protected void addFormProperties(List<FormProperty> formProperties,
                                     ObjectNode propertiesNode) {
        if (CollectionUtils.isEmpty(formProperties)) {
            return;
        }

        ObjectNode formPropertiesNode = objectMapper.createObjectNode();
        ArrayNode propertiesArrayNode = objectMapper.createArrayNode();
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
            if (StringUtils.isNotEmpty(property.getDatePattern())) {
                propertyItemNode.put(PROPERTY_FORM_DATE_PATTERN, property.getDatePattern());
            }
            if (CollectionUtils.isNotEmpty(property.getFormValues())) {
                ArrayNode valuesNode = objectMapper.createArrayNode();
                for (FormValue formValue : property.getFormValues()) {
                    ObjectNode valueNode = objectMapper.createObjectNode();
                    valueNode.put(PROPERTY_FORM_ENUM_VALUES_NAME, formValue.getName());
                    valueNode.put(PROPERTY_FORM_ENUM_VALUES_ID, formValue.getId());
                    valuesNode.add(valueNode);
                }
                propertyItemNode.set(PROPERTY_FORM_ENUM_VALUES, valuesNode);
            }
            propertyItemNode.put(PROPERTY_FORM_REQUIRED, property.isRequired());
            propertyItemNode.put(PROPERTY_FORM_READABLE, property.isReadable());
            propertyItemNode.put(PROPERTY_FORM_WRITABLE, property.isWriteable());

            propertiesArrayNode.add(propertyItemNode);
        }

        formPropertiesNode.set("formProperties", propertiesArrayNode);
        propertiesNode.set(PROPERTY_FORM_PROPERTIES, formPropertiesNode);
    }

    protected void addFieldExtensions(List<FieldExtension> extensions,
                                      ObjectNode propertiesNode) {
        ObjectNode fieldExtensionsNode = objectMapper.createObjectNode();
        ArrayNode itemsNode = objectMapper.createArrayNode();
        for (FieldExtension extension : extensions) {
            ObjectNode propertyItemNode = objectMapper.createObjectNode();
            propertyItemNode.put(PROPERTY_SERVICETASK_FIELD_NAME, extension.getFieldName());
            if (StringUtils.isNotEmpty(extension.getStringValue())) {
                propertyItemNode.put(PROPERTY_SERVICETASK_FIELD_STRING_VALUE, extension.getStringValue());
            }
            if (StringUtils.isNotEmpty(extension.getExpression())) {
                propertyItemNode.put(PROPERTY_SERVICETASK_FIELD_EXPRESSION, extension.getExpression());
            }
            itemsNode.add(propertyItemNode);
        }

        fieldExtensionsNode.set("fields", itemsNode);
        propertiesNode.set(PROPERTY_SERVICETASK_FIELDS, fieldExtensionsNode);
    }

    protected void addEventProperties(Event event,
                                      ObjectNode propertiesNode) {
        List<EventDefinition> eventDefinitions = event.getEventDefinitions();
        if (eventDefinitions.size() == 1) {

            EventDefinition eventDefinition = eventDefinitions.get(0);
            if (eventDefinition instanceof ErrorEventDefinition) {
                ErrorEventDefinition errorDefinition = (ErrorEventDefinition) eventDefinition;
                if (StringUtils.isNotEmpty(errorDefinition.getErrorRef())) {
                    propertiesNode.put(PROPERTY_ERRORREF,
                                       errorDefinition.getErrorRef());
                }
            } else if (eventDefinition instanceof SignalEventDefinition) {
                SignalEventDefinition signalDefinition = (SignalEventDefinition) eventDefinition;
                if (StringUtils.isNotEmpty(signalDefinition.getSignalRef())) {
                    propertiesNode.put(PROPERTY_SIGNALREF,
                                       signalDefinition.getSignalRef());
                }
            } else if (eventDefinition instanceof MessageEventDefinition) {
                MessageEventDefinition messageDefinition = (MessageEventDefinition) eventDefinition;
                if (StringUtils.isNotEmpty(messageDefinition.getMessageRef())) {
                    propertiesNode.put(PROPERTY_MESSAGEREF,
                                       messageDefinition.getMessageRef());
                }
            } else if (eventDefinition instanceof TimerEventDefinition) {
                TimerEventDefinition timerDefinition = (TimerEventDefinition) eventDefinition;
                if (StringUtils.isNotEmpty(timerDefinition.getTimeDuration())) {
                    propertiesNode.put(PROPERTY_TIMER_DURATON,
                                       timerDefinition.getTimeDuration());
                }
                if (StringUtils.isNotEmpty(timerDefinition.getTimeCycle())) {
                    propertiesNode.put(PROPERTY_TIMER_CYCLE,
                                       timerDefinition.getTimeCycle());
                }
                if (StringUtils.isNotEmpty(timerDefinition.getTimeDate())) {
                    propertiesNode.put(PROPERTY_TIMER_DATE,
                                       timerDefinition.getTimeDate());
                }
                if (StringUtils.isNotEmpty(timerDefinition.getEndDate())) {
                    propertiesNode.put(PROPERTY_TIMER_CYCLE_END_DATE,
                                       timerDefinition.getEndDate());
                }
            } else if (eventDefinition instanceof TerminateEventDefinition) {
                TerminateEventDefinition terminateEventDefinition = (TerminateEventDefinition) eventDefinition;
                propertiesNode.put(PROPERTY_TERMINATE_ALL, terminateEventDefinition.isTerminateAll());
                propertiesNode.put(PROPERTY_TERMINATE_MULTI_INSTANCE, terminateEventDefinition.isTerminateMultiInstance());
            }
        }
    }

    protected void convertJsonToFormProperties(JsonNode objectNode,
                                               BaseElement element) {

        JsonNode formPropertiesNode = getProperty(PROPERTY_FORM_PROPERTIES, objectNode);
        if (formPropertiesNode != null) {
            formPropertiesNode = BpmnJsonConverterUtil.validateIfNodeIsTextual(formPropertiesNode);
            JsonNode propertiesArray = formPropertiesNode.get("formProperties");
            if (propertiesArray != null) {
                for (JsonNode formNode : propertiesArray) {
                    JsonNode formIdNode = formNode.get(PROPERTY_FORM_ID);
                    if (formIdNode != null && StringUtils.isNotEmpty(formIdNode.asText())) {

                        FormProperty formProperty = new FormProperty();
                        formProperty.setId(formIdNode.asText());
                        formProperty.setName(getValueAsString(PROPERTY_FORM_NAME,
                                                              formNode));
                        formProperty.setType(getValueAsString(PROPERTY_FORM_TYPE,
                                                              formNode));
                        formProperty.setExpression(getValueAsString(PROPERTY_FORM_EXPRESSION,
                                                                    formNode));
                        formProperty.setVariable(getValueAsString(PROPERTY_FORM_VARIABLE,
                                                                  formNode));

                        if ("date".equalsIgnoreCase(formProperty.getType())) {
                            formProperty.setDatePattern(getValueAsString(PROPERTY_FORM_DATE_PATTERN,
                                                                         formNode));
                        } else if ("enum".equalsIgnoreCase(formProperty.getType())) {
                            JsonNode enumValuesNode = formNode.get(PROPERTY_FORM_ENUM_VALUES);
                            if (enumValuesNode != null) {
                                List<FormValue> formValueList = new ArrayList<FormValue>();
                                for (JsonNode enumNode : enumValuesNode) {
                                    if (enumNode.get(PROPERTY_FORM_ENUM_VALUES_ID) != null && !enumNode.get(PROPERTY_FORM_ENUM_VALUES_ID).isNull() && enumNode.get(PROPERTY_FORM_ENUM_VALUES_NAME) != null
                                            && !enumNode.get(PROPERTY_FORM_ENUM_VALUES_NAME).isNull()) {

                                        FormValue formValue = new FormValue();
                                        formValue.setId(enumNode.get(PROPERTY_FORM_ENUM_VALUES_ID).asText());
                                        formValue.setName(enumNode.get(PROPERTY_FORM_ENUM_VALUES_NAME).asText());
                                        formValueList.add(formValue);
                                    } else if (enumNode.get("value") != null && !enumNode.get("value").isNull()) {
                                        FormValue formValue = new FormValue();
                                        formValue.setId(enumNode.get("value").asText());
                                        formValue.setName(enumNode.get("value").asText());
                                        formValueList.add(formValue);
                                    }
                                }
                                formProperty.setFormValues(formValueList);
                            }
                        }

                        formProperty.setRequired(getValueAsBoolean(PROPERTY_FORM_REQUIRED,
                                                                   formNode));
                        formProperty.setReadable(getValueAsBoolean(PROPERTY_FORM_READABLE,
                                                                   formNode));
                        formProperty.setWriteable(getValueAsBoolean(PROPERTY_FORM_WRITABLE,
                                                                    formNode));

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

    protected void convertJsonToTimerDefinition(JsonNode objectNode,
                                                Event event) {

        String timeDate = getPropertyValueAsString(PROPERTY_TIMER_DATE,
                                                   objectNode);
        String timeCycle = getPropertyValueAsString(PROPERTY_TIMER_CYCLE,
                                                    objectNode);
        String timeDuration = getPropertyValueAsString(PROPERTY_TIMER_DURATON,
                                                       objectNode);
        String endDate = getPropertyValueAsString(PROPERTY_TIMER_CYCLE_END_DATE,
                                                  objectNode);

        TimerEventDefinition eventDefinition = new TimerEventDefinition();
        if (StringUtils.isNotEmpty(timeDate)) {
            eventDefinition.setTimeDate(timeDate);
        } else if (StringUtils.isNotEmpty(timeCycle)) {
            eventDefinition.setTimeCycle(timeCycle);
        } else if (StringUtils.isNotEmpty(timeDuration)) {
            eventDefinition.setTimeDuration(timeDuration);
        }

        if (StringUtils.isNotEmpty(endDate)) {
            eventDefinition.setEndDate(endDate);
        }

        event.getEventDefinitions().add(eventDefinition);
    }

    protected void convertJsonToSignalDefinition(JsonNode objectNode,
                                                 Event event) {
        String signalRef = getPropertyValueAsString(PROPERTY_SIGNALREF,
                                                    objectNode);
        SignalEventDefinition eventDefinition = new SignalEventDefinition();
        eventDefinition.setSignalRef(signalRef);
        event.getEventDefinitions().add(eventDefinition);
    }

    protected void convertJsonToMessageDefinition(JsonNode objectNode,
                                                  Event event) {
        String messageRef = getPropertyValueAsString(PROPERTY_MESSAGEREF,
                                                     objectNode);
        MessageEventDefinition eventDefinition = new MessageEventDefinition();
        eventDefinition.setMessageRef(messageRef);
        event.getEventDefinitions().add(eventDefinition);
    }

    protected void convertJsonToErrorDefinition(JsonNode objectNode,
                                                Event event) {
        String errorRef = getPropertyValueAsString(PROPERTY_ERRORREF,
                                                   objectNode);
        ErrorEventDefinition eventDefinition = new ErrorEventDefinition();
        eventDefinition.setErrorRef(errorRef);
        event.getEventDefinitions().add(eventDefinition);
    }

    protected String getValueAsString(String name,
                                      JsonNode objectNode) {
        String propertyValue = null;
        JsonNode propertyNode = objectNode.get(name);
        if (propertyNode != null && !propertyNode.isNull()) {
            propertyValue = propertyNode.asText();
        }
        return propertyValue;
    }

    protected boolean getValueAsBoolean(String name,
                                        JsonNode objectNode) {
        boolean propertyValue = false;
        JsonNode propertyNode = objectNode.get(name);
        if (propertyNode != null && !propertyNode.isNull()) {
            propertyValue = propertyNode.asBoolean();
        }
        return propertyValue;
    }

    protected List<String> getValueAsList(String name,
                                          JsonNode objectNode) {
        List<String> resultList = new ArrayList<String>();
        JsonNode valuesNode = objectNode.get(name);
        if (valuesNode != null) {
            for (JsonNode valueNode : valuesNode) {
                if (valueNode.get("value") != null && !valueNode.get("value").isNull()) {
                    resultList.add(valueNode.get("value").asText());
                }
            }
        }
        return resultList;
    }

    protected void addField(String name,
                            JsonNode elementNode,
                            ServiceTask task) {
        FieldExtension field = new FieldExtension();
        field.setFieldName(name.substring(8));
        String value = getPropertyValueAsString(name,
                                                elementNode);
        if (StringUtils.isNotEmpty(value)) {
            if ((value.contains("${") || value.contains("#{")) && value.contains("}")) {
                field.setExpression(value);
            } else {
                field.setStringValue(value);
            }
            task.getFieldExtensions().add(field);
        }
    }

    protected void addField(String name,
                            String propertyName,
                            JsonNode elementNode,
                            ServiceTask task) {
        FieldExtension field = new FieldExtension();
        field.setFieldName(name);
        String value = getPropertyValueAsString(propertyName,
                                                elementNode);
        if (StringUtils.isNotEmpty(value)) {
            if ((value.contains("${") || value.contains("#{")) && value.contains("}")) {
                field.setExpression(value);
            } else {
                field.setStringValue(value);
            }
            task.getFieldExtensions().add(field);
        }
    }

    protected String getPropertyValueAsString(String name,
                                              JsonNode objectNode) {
        return JsonConverterUtil.getPropertyValueAsString(name,
                                                          objectNode);
    }

    protected boolean getPropertyValueAsBoolean(String name,
                                                JsonNode objectNode) {
        return JsonConverterUtil.getPropertyValueAsBoolean(name,
                                                           objectNode);
    }

    protected List<String> getPropertyValueAsList(String name,
                                                  JsonNode objectNode) {
        return JsonConverterUtil.getPropertyValueAsList(name,
                                                        objectNode);
    }

    protected JsonNode getProperty(String name,
                                   JsonNode objectNode) {
        return JsonConverterUtil.getProperty(name,
                                             objectNode);
    }

    protected String convertListToCommaSeparatedString(List<String> stringList) {
        String resultString = null;
        if (stringList != null && stringList.size() > 0) {
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

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
package org.activiti.app.service.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.app.domain.editor.AbstractModel;
import org.activiti.app.service.editor.mapper.EventInfoMapper;
import org.activiti.app.service.editor.mapper.InfoMapper;
import org.activiti.app.service.editor.mapper.ReceiveTaskInfoMapper;
import org.activiti.app.service.editor.mapper.SequenceFlowInfoMapper;
import org.activiti.app.service.editor.mapper.ServiceTaskInfoMapper;
import org.activiti.app.service.editor.mapper.UserTaskInfoMapper;
import org.activiti.bpmn.model.Artifact;
import org.activiti.bpmn.model.Association;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.ErrorEventDefinition;
import org.activiti.bpmn.model.Event;
import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.GraphicInfo;
import org.activiti.bpmn.model.Lane;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.bpmn.model.Pool;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.TextAnnotation;
import org.activiti.bpmn.model.TimerEventDefinition;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.editor.language.json.converter.util.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
public class BpmnDisplayJsonConverter {
    
    private final Logger log = LoggerFactory.getLogger(BpmnDisplayJsonConverter.class);
    
    protected BpmnJsonConverter bpmnJsonConverter = new BpmnJsonConverter();
    
    protected ObjectMapper objectMapper = new ObjectMapper();
    protected List<String> eventElementTypes = new ArrayList<String>();
    protected Map<String, InfoMapper> propertyMappers = new HashMap<String, InfoMapper>();

    public BpmnDisplayJsonConverter() {
        eventElementTypes.add("StartEvent");
        eventElementTypes.add("EndEvent");
        eventElementTypes.add("BoundaryEvent");
        eventElementTypes.add("IntermediateCatchEvent");
        eventElementTypes.add("ThrowEvent");

        propertyMappers.put("BoundaryEvent", new EventInfoMapper());
        propertyMappers.put("EndEvent", new EventInfoMapper());
        propertyMappers.put("IntermediateCatchEvent", new EventInfoMapper());
        propertyMappers.put("ReceiveTask", new ReceiveTaskInfoMapper());
        propertyMappers.put("StartEvent", new EventInfoMapper());
        propertyMappers.put("SequenceFlow", new SequenceFlowInfoMapper());
        propertyMappers.put("ServiceTask", new ServiceTaskInfoMapper());
        propertyMappers.put("ThrowEvent", new EventInfoMapper());
        propertyMappers.put("UserTask", new UserTaskInfoMapper());
    }

    public void processProcessElements(AbstractModel processModel, ObjectNode displayNode, GraphicInfo diagramInfo) {
        BpmnModel pojoModel = null;
        if (!StringUtils.isEmpty(processModel.getModelEditorJson())) {
            try {
                JsonNode modelNode = objectMapper.readTree(processModel.getModelEditorJson());
                pojoModel = bpmnJsonConverter.convertToBpmnModel(modelNode);
            } catch (Exception e) {
                log.error("Error transforming json to pojo " + processModel.getId(), e);
            }
        }
        
        if (pojoModel == null || pojoModel.getLocationMap().isEmpty()) return;

        ArrayNode elementArray = objectMapper.createArrayNode();
        ArrayNode flowArray = objectMapper.createArrayNode();

        if (CollectionUtils.isNotEmpty(pojoModel.getPools())) {
            ArrayNode poolArray = objectMapper.createArrayNode();
            boolean firstElement = true;
            for (Pool pool : pojoModel.getPools()) {
                ObjectNode poolNode = objectMapper.createObjectNode();
                poolNode.put("id", pool.getId());
                poolNode.put("name", pool.getName());
                GraphicInfo poolInfo = pojoModel.getGraphicInfo(pool.getId());
                fillGraphicInfo(poolNode, poolInfo, true);
                org.activiti.bpmn.model.Process process = pojoModel.getProcess(pool.getId());
                if (process != null && CollectionUtils.isNotEmpty(process.getLanes())) {
                    ArrayNode laneArray = objectMapper.createArrayNode();
                    for (Lane lane : process.getLanes()) {
                        ObjectNode laneNode = objectMapper.createObjectNode();
                        laneNode.put("id", lane.getId());
                        laneNode.put("name", lane.getName());
                        fillGraphicInfo(laneNode, pojoModel.getGraphicInfo(lane.getId()), true);
                        laneArray.add(laneNode);
                    }
                    poolNode.put("lanes", laneArray);
                }
                poolArray.add(poolNode);

                double rightX = poolInfo.getX() + poolInfo.getWidth();
                double bottomY = poolInfo.getY() + poolInfo.getHeight();
                double middleX = poolInfo.getX() + (poolInfo.getWidth() / 2);
                if (firstElement || middleX < diagramInfo.getX()) {
                    diagramInfo.setX(middleX);
                }
                if (firstElement || poolInfo.getY() < diagramInfo.getY()) {
                    diagramInfo.setY(poolInfo.getY());
                }
                if (rightX > diagramInfo.getWidth()) {
                    diagramInfo.setWidth(rightX);
                }
                if (bottomY > diagramInfo.getHeight()) {
                    diagramInfo.setHeight(bottomY);
                }
                firstElement = false;
            }
            displayNode.put("pools", poolArray);
            
        } else {
            // in initialize with fake x and y to make sure the minimal
            // values are set
            diagramInfo.setX(9999);
            diagramInfo.setY(1000);
        }

        for (org.activiti.bpmn.model.Process process : pojoModel.getProcesses()) {
            processElements(process.getFlowElements(), pojoModel, elementArray, flowArray, diagramInfo);
            processArtifacts(process.getArtifacts(), pojoModel, elementArray, flowArray, diagramInfo);
        }

        displayNode.put("elements", elementArray);
        displayNode.put("flows", flowArray);
        
        displayNode.put("diagramBeginX", diagramInfo.getX());
        displayNode.put("diagramBeginY", diagramInfo.getY());
        displayNode.put("diagramWidth", diagramInfo.getWidth());
        displayNode.put("diagramHeight", diagramInfo.getHeight());
    }

    protected void processElements(Collection<FlowElement> elementList, BpmnModel model, ArrayNode elementArray, 
            ArrayNode flowArray, GraphicInfo diagramInfo) {

        for (FlowElement element : elementList) {
            if (element instanceof SequenceFlow) {
                ObjectNode elementNode = objectMapper.createObjectNode();
                SequenceFlow flow = (SequenceFlow) element;
                elementNode.put("id", flow.getId());
                elementNode.put("type", "sequenceFlow");
                elementNode.put("sourceRef", flow.getSourceRef());
                elementNode.put("targetRef", flow.getTargetRef());
                List<GraphicInfo> flowInfo = model.getFlowLocationGraphicInfo(flow.getId());
                if (CollectionUtils.isNotEmpty(flowInfo)) {
                    ArrayNode waypointArray = objectMapper.createArrayNode();
                    for (GraphicInfo graphicInfo : flowInfo) {
                        ObjectNode pointNode = objectMapper.createObjectNode();
                        fillGraphicInfo(pointNode, graphicInfo, false);
                        waypointArray.add(pointNode);
                        fillDiagramInfo(graphicInfo, diagramInfo);
                    }
                    elementNode.put("waypoints", waypointArray);
                    
                    String className = element.getClass().getSimpleName();
                    if (propertyMappers.containsKey(className)) {
                        elementNode.put("properties", propertyMappers.get(className).map(element));
                    }
                    
                    flowArray.add(elementNode);
                }

            } else {

                ObjectNode elementNode = objectMapper.createObjectNode();
                elementNode.put("id", element.getId());
                elementNode.put("name", element.getName());

                GraphicInfo graphicInfo = model.getGraphicInfo(element.getId());
                if (graphicInfo != null) {
                    fillGraphicInfo(elementNode, graphicInfo, true);
                    fillDiagramInfo(graphicInfo, diagramInfo);
                }

                String className = element.getClass().getSimpleName();
                elementNode.put("type", className);
                fillEventTypes(className, element, elementNode);

                if (element instanceof ServiceTask) {
                    ServiceTask serviceTask = (ServiceTask) element;
                    if (ServiceTask.MAIL_TASK.equals(serviceTask.getType())) {
                        elementNode.put("taskType", "mail");
                        
                    } else if ("camel".equals(serviceTask.getType())) {
                        elementNode.put("taskType", "camel");
                    
                    } else if ("mule".equals(serviceTask.getType())) {
                        elementNode.put("taskType", "mule");
                    }
                }

                if (propertyMappers.containsKey(className)) {
                    elementNode.put("properties", propertyMappers.get(className).map(element));
                }

                elementArray.add(elementNode);

                if (element instanceof SubProcess) {
                    SubProcess subProcess = (SubProcess) element;
                    processElements(subProcess.getFlowElements(), model, elementArray, flowArray, diagramInfo);
                    processArtifacts(subProcess.getArtifacts(), model, elementArray, flowArray, diagramInfo);
                }
            }
        }
    }
    
    protected void processArtifacts(Collection<Artifact> artifactList,
            BpmnModel model, ArrayNode elementArray, ArrayNode flowArray,
            GraphicInfo diagramInfo) {

        for (Artifact artifact : artifactList) {

            if (artifact instanceof Association) {
                ObjectNode elementNode = objectMapper.createObjectNode();
                Association flow = (Association) artifact;
                elementNode.put("id", flow.getId());
                elementNode.put("type", "association");
                elementNode.put("sourceRef", flow.getSourceRef());
                elementNode.put("targetRef", flow.getTargetRef());
                fillWaypoints(flow.getId(), model, elementNode, diagramInfo);
                flowArray.add(elementNode);

            } else {

                ObjectNode elementNode = objectMapper.createObjectNode();
                elementNode.put("id", artifact.getId());

                if (artifact instanceof TextAnnotation) {
                    TextAnnotation annotation = (TextAnnotation) artifact;
                    elementNode.put("text", annotation.getText());
                }

                GraphicInfo graphicInfo = model.getGraphicInfo(artifact.getId());
                if (graphicInfo != null) {
                    fillGraphicInfo(elementNode, graphicInfo, true);
                    fillDiagramInfo(graphicInfo, diagramInfo);
                }

                String className = artifact.getClass().getSimpleName();
                elementNode.put("type", className);

                elementArray.add(elementNode);
            }
        }
    }
    
    protected void fillWaypoints(String id, BpmnModel model, ObjectNode elementNode, GraphicInfo diagramInfo) {
        List<GraphicInfo> flowInfo = model.getFlowLocationGraphicInfo(id);
        ArrayNode waypointArray = objectMapper.createArrayNode();
        for (GraphicInfo graphicInfo : flowInfo) {
            ObjectNode pointNode = objectMapper.createObjectNode();
            fillGraphicInfo(pointNode, graphicInfo, false);
            waypointArray.add(pointNode);
            fillDiagramInfo(graphicInfo, diagramInfo);
        }
        elementNode.put("waypoints", waypointArray);
    }

    protected void fillEventTypes(String className, FlowElement element, ObjectNode elementNode) {
        if (eventElementTypes.contains(className)) {
            Event event = (Event) element;
            if (CollectionUtils.isNotEmpty(event.getEventDefinitions())) {
                EventDefinition eventDef = event.getEventDefinitions().get(0);
                ObjectNode eventNode = objectMapper.createObjectNode();
                if (eventDef instanceof TimerEventDefinition) {
                    TimerEventDefinition timerDef = (TimerEventDefinition) eventDef;
                    eventNode.put("type", "timer");
                    if (StringUtils.isNotEmpty(timerDef.getTimeCycle())) {
                        eventNode.put("timeCycle", timerDef.getTimeCycle());
                    }
                    if (StringUtils.isNotEmpty(timerDef.getTimeDate())) {
                        eventNode.put("timeDate", timerDef.getTimeDate());
                    }
                    if (StringUtils.isNotEmpty(timerDef.getTimeDuration())) {
                        eventNode.put("timeDuration", timerDef.getTimeDuration());
                    }

                } else if (eventDef instanceof ErrorEventDefinition) {
                    ErrorEventDefinition errorDef = (ErrorEventDefinition) eventDef;
                    eventNode.put("type", "error");
                    if (StringUtils.isNotEmpty(errorDef.getErrorCode())) {
                        eventNode.put("errorCode", errorDef.getErrorCode());
                    }

                } else if (eventDef instanceof SignalEventDefinition) {
                    SignalEventDefinition signalDef = (SignalEventDefinition) eventDef;
                    eventNode.put("type", "signal");
                    if (StringUtils.isNotEmpty(signalDef.getSignalRef())) {
                        eventNode.put("signalRef", signalDef.getSignalRef());
                    }

                } else if (eventDef instanceof MessageEventDefinition) {
                    MessageEventDefinition messageDef = (MessageEventDefinition) eventDef;
                    eventNode.put("type", "message");
                    if (StringUtils.isNotEmpty(messageDef.getMessageRef())) {
                        eventNode.put("messageRef", messageDef.getMessageRef());
                    }
                }
                elementNode.put("eventDefinition", eventNode);
            }
        }
    }

    protected void fillGraphicInfo(ObjectNode elementNode, GraphicInfo graphicInfo, boolean includeWidthAndHeight) {
        commonFillGraphicInfo(elementNode, graphicInfo.getX(),
                graphicInfo.getY(), graphicInfo.getWidth(),
                graphicInfo.getHeight(), includeWidthAndHeight);
    }

    protected void commonFillGraphicInfo(ObjectNode elementNode, double x,
            double y, double width, double height, boolean includeWidthAndHeight) {
        
        elementNode.put("x", x);
        elementNode.put("y", y);
        if (includeWidthAndHeight) {
            elementNode.put("width", width);
            elementNode.put("height", height);
        }
    }
    
    protected void fillDiagramInfo(GraphicInfo graphicInfo, GraphicInfo diagramInfo) {
        double rightX = graphicInfo.getX() + graphicInfo.getWidth();
        double bottomY = graphicInfo.getY() + graphicInfo.getHeight();
        double middleX = graphicInfo.getX() + (graphicInfo.getWidth() / 2);
        if (middleX < diagramInfo.getX()) {
            diagramInfo.setX(middleX);
        }
        if (graphicInfo.getY() < diagramInfo.getY()) {
            diagramInfo.setY(graphicInfo.getY());
        }
        if (rightX > diagramInfo.getWidth()) {
            diagramInfo.setWidth(rightX);
        }
        if (bottomY > diagramInfo.getHeight()) {
            diagramInfo.setHeight(bottomY);
        }
    }
}

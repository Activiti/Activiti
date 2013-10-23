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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.ActivitiListener;

import math.geom2d.Point2D;
import math.geom2d.conic.Circle2D;
import math.geom2d.line.Line2D;
import math.geom2d.polygon.Polyline2D;

import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.GraphicInfo;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.Lane;
import org.activiti.bpmn.model.Pool;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.editor.constants.EditorJsonConstants;
import org.activiti.editor.constants.StencilConstants;
import org.activiti.editor.language.json.converter.util.JsonConverterUtil;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tijs Rademakers
 */
public class BpmnJsonConverter implements EditorJsonConstants, StencilConstants, ActivityProcessor {
  
  protected static final Logger LOGGER = LoggerFactory.getLogger(BpmnJsonConverter.class);
  
  protected ObjectMapper objectMapper = new ObjectMapper();
  
  protected static Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap = 
      new HashMap<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>>();
  
  protected static Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap = 
      new HashMap<String, Class<? extends BaseBpmnJsonConverter>>();
  
  static {
    
    // start and end events
    StartEventJsonConverter.fillTypes(convertersToBpmnMap, convertersToJsonMap);
    EndEventJsonConverter.fillTypes(convertersToBpmnMap, convertersToJsonMap);
    
    // connectors
    SequenceFlowJsonConverter.fillTypes(convertersToBpmnMap, convertersToJsonMap);
    
    // task types
    BusinessRuleTaskJsonConverter.fillTypes(convertersToBpmnMap, convertersToJsonMap);
    MailTaskJsonConverter.fillTypes(convertersToBpmnMap, convertersToJsonMap);
    ManualTaskJsonConverter.fillTypes(convertersToBpmnMap, convertersToJsonMap);
    ReceiveTaskJsonConverter.fillTypes(convertersToBpmnMap, convertersToJsonMap);
    ScriptTaskJsonConverter.fillTypes(convertersToBpmnMap, convertersToJsonMap);
    ServiceTaskJsonConverter.fillTypes(convertersToBpmnMap, convertersToJsonMap);
    UserTaskJsonConverter.fillTypes(convertersToBpmnMap, convertersToJsonMap);
    CallActivityJsonConverter.fillTypes(convertersToBpmnMap, convertersToJsonMap);
    
    // gateways
    ExclusiveGatewayJsonConverter.fillTypes(convertersToBpmnMap, convertersToJsonMap);
    InclusiveGatewayJsonConverter.fillTypes(convertersToBpmnMap, convertersToJsonMap);
    ParallelGatewayJsonConverter.fillTypes(convertersToBpmnMap, convertersToJsonMap);
    EventGatewayJsonConverter.fillTypes(convertersToBpmnMap, convertersToJsonMap);
    
    // scope constructs
    SubProcessJsonConverter.fillTypes(convertersToBpmnMap, convertersToJsonMap);
    EventSubProcessJsonConverter.fillTypes(convertersToBpmnMap, convertersToJsonMap);
    
    // catch events
    CatchEventJsonConverter.fillTypes(convertersToBpmnMap, convertersToJsonMap);
    
    // throw events
    ThrowEventJsonConverter.fillTypes(convertersToBpmnMap, convertersToJsonMap);
    
    // boundary events
    BoundaryEventJsonConverter.fillTypes(convertersToBpmnMap, convertersToJsonMap);
  }
  
  private static final List<String> DI_CIRCLES = new ArrayList<String>();
  private static final List<String> DI_RECTANGLES = new ArrayList<String>();
  private static final List<String> DI_GATEWAY = new ArrayList<String>();
  
  static {
    DI_CIRCLES.add(STENCIL_EVENT_START_ERROR);
    DI_CIRCLES.add(STENCIL_EVENT_START_MESSAGE);
    DI_CIRCLES.add(STENCIL_EVENT_START_NONE);
    DI_CIRCLES.add(STENCIL_EVENT_START_TIMER);
    
    DI_CIRCLES.add(STENCIL_EVENT_BOUNDARY_ERROR);
    DI_CIRCLES.add(STENCIL_EVENT_BOUNDARY_SIGNAL);
    DI_CIRCLES.add(STENCIL_EVENT_BOUNDARY_TIMER);
    
    DI_CIRCLES.add(STENCIL_EVENT_CATCH_MESSAGE);
    DI_CIRCLES.add(STENCIL_EVENT_CATCH_SIGNAL);
    DI_CIRCLES.add(STENCIL_EVENT_CATCH_TIMER);
    
    DI_CIRCLES.add(STENCIL_EVENT_THROW_NONE);
    DI_CIRCLES.add(STENCIL_EVENT_THROW_SIGNAL);
    
    DI_CIRCLES.add(STENCIL_EVENT_END_NONE);
    DI_CIRCLES.add(STENCIL_EVENT_END_ERROR);
    
    DI_RECTANGLES.add(STENCIL_CALL_ACTIVITY);
    DI_RECTANGLES.add(STENCIL_SUB_PROCESS);
    DI_RECTANGLES.add(STENCIL_EVENT_SUB_PROCESS);
    DI_RECTANGLES.add(STENCIL_TASK_BUSINESS_RULE);
    DI_RECTANGLES.add(STENCIL_TASK_MAIL);
    DI_RECTANGLES.add(STENCIL_TASK_MANUAL);
    DI_RECTANGLES.add(STENCIL_TASK_RECEIVE);
    DI_RECTANGLES.add(STENCIL_TASK_SCRIPT);
    DI_RECTANGLES.add(STENCIL_TASK_SERVICE);
    DI_RECTANGLES.add(STENCIL_TASK_USER);
    
    DI_GATEWAY.add(STENCIL_GATEWAY_EVENT);
    DI_GATEWAY.add(STENCIL_GATEWAY_EXCLUSIVE);
    DI_GATEWAY.add(STENCIL_GATEWAY_INCLUSIVE);
    DI_GATEWAY.add(STENCIL_GATEWAY_PARALLEL);
  }

  public ObjectNode convertToJson(BpmnModel model) {
    ObjectNode modelNode = objectMapper.createObjectNode();
    modelNode.put("bounds", BpmnJsonConverterUtil.createBoundsNode(1485, 1050, 0, 0));
    modelNode.put("resourceId", "canvas");
    
    ObjectNode stencilNode = objectMapper.createObjectNode();
    stencilNode.put("id", "BPMNDiagram");
    modelNode.put("stencil", stencilNode);
    
    ObjectNode stencilsetNode = objectMapper.createObjectNode();
    stencilsetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
    stencilsetNode.put("url", "../editor/stencilsets/bpmn2.0/bpmn2.0.json");
    modelNode.put("stencilset", stencilsetNode);
    
    ArrayNode shapesArrayNode = objectMapper.createArrayNode();
    
    Process mainProcess = null;
    if (model.getPools().size() > 0) {
      mainProcess = model.getProcess(model.getPools().get(0).getId());
    } else {
      mainProcess = model.getMainProcess();
    }
      
    ObjectNode propertiesNode = objectMapper.createObjectNode();
    if (StringUtils.isNotEmpty(mainProcess.getId())) {
      propertiesNode.put(PROPERTY_PROCESS_ID, mainProcess.getId());
    }
    if (StringUtils.isNotEmpty(mainProcess.getName())) {
      propertiesNode.put(PROPERTY_NAME, mainProcess.getName());
    }
    if (mainProcess.isExecutable() == false) {
      propertiesNode.put(PROPERTY_PROCESS_EXECUTABLE, PROPERTY_VALUE_NO);
    }
    
    propertiesNode.put(PROPERTY_PROCESS_NAMESPACE, model.getTargetNamespace());
    
    if (StringUtils.isNotEmpty(mainProcess.getDocumentation())) {
      propertiesNode.put(PROPERTY_DOCUMENTATION, mainProcess.getDocumentation());
    }
    modelNode.put(EDITOR_SHAPE_PROPERTIES, propertiesNode);
    
    if (model.getPools().size() > 0) {
      for (Pool pool : model.getPools()) {
        GraphicInfo graphicInfo = model.getGraphicInfo(pool.getId());
        ObjectNode poolNode = BpmnJsonConverterUtil.createChildShape(pool.getId(), STENCIL_POOL, 
            graphicInfo.getX() + graphicInfo.getWidth(), graphicInfo.getY() + graphicInfo.getHeight(), graphicInfo.getX(), graphicInfo.getY());
        shapesArrayNode.add(poolNode);
        ObjectNode poolPropertiesNode = objectMapper.createObjectNode();
        poolPropertiesNode.put(PROPERTY_OVERRIDE_ID, pool.getId());
        if (StringUtils.isNotEmpty(pool.getName())) {
          poolPropertiesNode.put(PROPERTY_NAME, pool.getName());
        }
        poolNode.put(EDITOR_SHAPE_PROPERTIES, poolPropertiesNode);
        
        ArrayNode laneShapesArrayNode = objectMapper.createArrayNode();
        poolNode.put(EDITOR_CHILD_SHAPES, laneShapesArrayNode);
        
        Process process = model.getProcess(pool.getId());
        if (process != null) {
          for (Lane lane : process.getLanes()) {
            GraphicInfo laneGraphicInfo = model.getGraphicInfo(lane.getId());
            ObjectNode laneNode = BpmnJsonConverterUtil.createChildShape(lane.getId(), STENCIL_LANE, 
                laneGraphicInfo.getX() + laneGraphicInfo.getWidth(), laneGraphicInfo.getY() + laneGraphicInfo.getHeight(), 
                laneGraphicInfo.getX(), laneGraphicInfo.getY());
            laneShapesArrayNode.add(laneNode);
            ObjectNode lanePropertiesNode = objectMapper.createObjectNode();
            lanePropertiesNode.put(PROPERTY_OVERRIDE_ID, lane.getId());
            if (StringUtils.isNotEmpty(lane.getName())) {
              lanePropertiesNode.put(PROPERTY_NAME, lane.getName());
            }
            laneNode.put(EDITOR_SHAPE_PROPERTIES, lanePropertiesNode);
            
            ArrayNode elementShapesArrayNode = objectMapper.createArrayNode();
            laneNode.put(EDITOR_CHILD_SHAPES, elementShapesArrayNode);
            
            for (FlowElement flowElement : process.getFlowElements()) {
              if (lane.getFlowReferences().contains(flowElement.getId())) {
                Class<? extends BaseBpmnJsonConverter> converter = convertersToJsonMap.get(flowElement.getClass());
                if (converter != null) {
                  try {
                    converter.newInstance().convertToJson(flowElement, this, model, elementShapesArrayNode, 
                        laneGraphicInfo.getX(), laneGraphicInfo.getY());
                  } catch (Exception e) {
                    LOGGER.error("Error converting {}", flowElement, e);
                  }
                }
              }
            }
          }
        }
        
      }
    } else {
      processFlowElements(model.getMainProcess().getFlowElements(), model, shapesArrayNode, 0.0, 0.0);
    }
    
    modelNode.put(EDITOR_CHILD_SHAPES, shapesArrayNode);
    return modelNode;
  }
  
  public void processFlowElements(Collection<FlowElement> flowElements, BpmnModel model, ArrayNode shapesArrayNode, 
      double subProcessX, double subProcessY) {
    
    for (FlowElement flowElement : flowElements) {
      Class<? extends BaseBpmnJsonConverter> converter = convertersToJsonMap.get(flowElement.getClass());
      if (converter != null) {
        try {
          converter.newInstance().convertToJson(flowElement, this, model, shapesArrayNode, 
              subProcessX, subProcessY);
        } catch (Exception e) {
          LOGGER.error("Error converting {}", flowElement, e);
        }
      }
    }
  }
  
  public BpmnModel convertToBpmnModel(JsonNode modelNode) {
    BpmnModel bpmnModel = new BpmnModel();
    Map<String, JsonNode> shapeMap = new HashMap<String, JsonNode>();
    Map<String, JsonNode> sourceRefMap = new HashMap<String, JsonNode>();
    Map<String, JsonNode> edgeMap = new HashMap<String, JsonNode>();
    Map<String, List<JsonNode>> sourceAndTargetMap = new HashMap<String, List<JsonNode>>();
    readShapeDI(modelNode, 0, 0, shapeMap, sourceRefMap, bpmnModel);
    filterAllEdges(modelNode, edgeMap, sourceAndTargetMap, shapeMap, sourceRefMap);
    readEdgeDI(edgeMap, sourceAndTargetMap, bpmnModel);
    
    ArrayNode shapesArrayNode = (ArrayNode) modelNode.get(EDITOR_CHILD_SHAPES);
    
    boolean nonEmptyPoolFound = false;
    // first create the pool structure
    for (JsonNode shapeNode : shapesArrayNode) {
      String stencilId = BpmnJsonConverterUtil.getStencilId(shapeNode);
      if (STENCIL_POOL.equals(stencilId)) {
        Pool pool = new Pool();
        pool.setId(BpmnJsonConverterUtil.getElementId(shapeNode));
        pool.setName(JsonConverterUtil.getPropertyValueAsString(PROPERTY_NAME, shapeNode));
        bpmnModel.getPools().add(pool);
        
        Process process = new Process();
        process.setId("Process_" + pool.getId());
        bpmnModel.addProcess(process);
        pool.setProcessRef(process.getId());
        
        ArrayNode laneArrayNode = (ArrayNode) shapeNode.get(EDITOR_CHILD_SHAPES);
        for (JsonNode laneNode : laneArrayNode) {
          // should be a lane, but just check to be certain
          String laneStencilId = BpmnJsonConverterUtil.getStencilId(laneNode);
          if (STENCIL_LANE.equals(laneStencilId)) {
            nonEmptyPoolFound = true;
            Lane lane = new Lane();
            lane.setId(BpmnJsonConverterUtil.getElementId(laneNode));
            lane.setName(JsonConverterUtil.getPropertyValueAsString(PROPERTY_NAME, laneNode));
            lane.setParentProcess(process);
            process.getLanes().add(lane);
            
            processJsonElements(laneNode.get(EDITOR_CHILD_SHAPES), modelNode, lane, shapeMap);
          }
        }
      }
    }
    
    if (nonEmptyPoolFound == false) {
      
      JsonNode processIdNode = JsonConverterUtil.getProperty(PROPERTY_PROCESS_ID, modelNode);
      Process process = new Process();
      bpmnModel.getProcesses().add(process);
      if (processIdNode != null && StringUtils.isNotEmpty(processIdNode.asText())) {
        process.setId(processIdNode.asText());
      }
      
      JsonNode processNameNode = JsonConverterUtil.getProperty(PROPERTY_NAME, modelNode);
      if (processNameNode != null && StringUtils.isNotEmpty(processNameNode.asText())) {
        process.setName(processNameNode.asText());
      }
      
      JsonNode processExecutableNode = JsonConverterUtil.getProperty(PROPERTY_PROCESS_EXECUTABLE, modelNode);
      if (processExecutableNode != null && StringUtils.isNotEmpty(processExecutableNode.asText())) {
        process.setExecutable(JsonConverterUtil.getPropertyValueAsBoolean(PROPERTY_PROCESS_EXECUTABLE, modelNode));
      }
      
      JsonNode processTargetNamespace = JsonConverterUtil.getProperty(PROPERTY_PROCESS_NAMESPACE, modelNode);
      if (processTargetNamespace != null && StringUtils.isNotEmpty(processTargetNamespace.asText())) {
        bpmnModel.setTargetNamespace(processTargetNamespace.asText());
      }
      
      JsonNode processExecutionListenerNode = modelNode.get(EDITOR_SHAPE_PROPERTIES).get(PROPERTY_EXECUTION_LISTENERS);
      if (processExecutionListenerNode != null && StringUtils.isNotEmpty(processExecutionListenerNode.asText())){
         process.setExecutionListeners(convertJsonToListeners(processExecutionListenerNode));
      }
      
      processJsonElements(shapesArrayNode, modelNode, process, shapeMap);
    }
    
    // sequence flows are now all on root level
    Map<String, SubProcess> subShapesMap = new HashMap<String, SubProcess>();
    for (Process process : bpmnModel.getProcesses()) {
      for (FlowElement flowElement : process.findFlowElementsOfType(SubProcess.class)) {
        SubProcess subProcess = (SubProcess) flowElement;
        fillSubShapes(subShapesMap, subProcess);
      }
      
      if (subShapesMap.size() > 0) {
        List<String> removeSubFlowsList = new ArrayList<String>();
        for (FlowElement flowElement : process.findFlowElementsOfType(SequenceFlow.class)) {
          SequenceFlow sequenceFlow = (SequenceFlow) flowElement;
          if (subShapesMap.containsKey(sequenceFlow.getSourceRef())) {
            SubProcess subProcess = subShapesMap.get(sequenceFlow.getSourceRef());
            subProcess.addFlowElement(sequenceFlow);
            removeSubFlowsList.add(sequenceFlow.getId());
          }
        }
        for (String flowId : removeSubFlowsList) {
          process.removeFlowElement(flowId);
        }
      }
    }
    
    // Add flows to map for later processing
    Map<String, SequenceFlow> flowSourceMap = new HashMap<String, SequenceFlow>();
    Map<String, SequenceFlow> flowTargetMap = new HashMap<String, SequenceFlow>();
    for (Process process : bpmnModel.getProcesses()) {
      addAllSequenceFlows(process.getFlowElements(), flowSourceMap, flowTargetMap);
    }
    
    // boundary events only contain attached ref id
    for (Process process : bpmnModel.getProcesses()) {
      postProcessElements(process, process.getFlowElements(), flowSourceMap, flowTargetMap);
    }
    
    return bpmnModel;
  }
  
  public void processJsonElements(JsonNode shapesArrayNode, JsonNode modelNode, 
      BaseElement parentElement, Map<String, JsonNode> shapeMap) {
    
    for (JsonNode shapeNode : shapesArrayNode) {
      Class<? extends BaseBpmnJsonConverter> converter = convertersToBpmnMap.get(BpmnJsonConverterUtil.getStencilId(shapeNode));
      if (converter != null) {
        try {
          converter.newInstance().convertToBpmnModel(shapeNode, modelNode, this, parentElement, shapeMap);
        } catch (Exception e) {
          LOGGER.error("Error converting {}", BpmnJsonConverterUtil.getStencilId(shapeNode), e);
        }
      }
    }
  }
  
  private List<ActivitiListener> convertJsonToListeners(JsonNode listenersNode) {
    List<ActivitiListener> executionListeners = new ArrayList<ActivitiListener>();
    
    try {
      listenersNode = objectMapper.readTree(listenersNode.asText());
    } catch (Exception e) {
      LOGGER.info("Listeners node can not be read", e);
    }
      
    JsonNode itemsArrayNode = listenersNode.get(EDITOR_PROPERTIES_GENERAL_ITEMS);
    if (itemsArrayNode != null) {
      for (JsonNode itemNode : itemsArrayNode) {
        JsonNode typeNode = itemNode.get(PROPERTY_EXECUTION_LISTENER_EVENT);
        if (typeNode != null && StringUtils.isNotEmpty(typeNode.asText())) {

          ActivitiListener listener = new ActivitiListener();
          listener.setEvent(typeNode.asText());
          if (StringUtils.isNotEmpty(itemNode.get(PROPERTY_EXECUTION_LISTENER_CLASS).asText())) {
            listener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
            listener.setImplementation(itemNode.get(PROPERTY_EXECUTION_LISTENER_CLASS).asText());
          } else if (StringUtils.isNotEmpty(itemNode.get(PROPERTY_EXECUTION_LISTENER_EXPRESSION).asText())) {
            listener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_EXPRESSION);
            listener.setImplementation(itemNode.get(PROPERTY_EXECUTION_LISTENER_EXPRESSION).asText());
          } else if (StringUtils.isNotEmpty(itemNode.get(PROPERTY_EXECUTION_LISTENER_DELEGATEEXPRESSION).asText())) {
            listener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION);
            listener.setImplementation(itemNode.get(PROPERTY_EXECUTION_LISTENER_DELEGATEEXPRESSION).asText());
          }
          executionListeners.add(listener);
        }
      }
    }
    return executionListeners;
  }
  
  private void fillSubShapes(Map<String, SubProcess> subShapesMap, SubProcess subProcess) {
    for (FlowElement flowElement : subProcess.getFlowElements()) {
      if (flowElement instanceof SubProcess) {
        SubProcess childSubProcess = (SubProcess) flowElement;
        fillSubShapes(subShapesMap, childSubProcess);
      } else {
        subShapesMap.put(flowElement.getId(), subProcess);
      }
    }
  }
  
  private void addAllSequenceFlows(Collection<FlowElement> flowElementList,
      Map<String, SequenceFlow> flowSourceMap, Map<String, SequenceFlow> flowTargetMap) {
    
    for (FlowElement flowElement : flowElementList) {
      if (flowElement instanceof SequenceFlow) {
        SequenceFlow flow = (SequenceFlow) flowElement;
        flowSourceMap.put(flow.getSourceRef(), flow);
        flowTargetMap.put(flow.getTargetRef(), flow);
      } else if (flowElement instanceof SubProcess) {
        SubProcess subProcess = (SubProcess) flowElement;
        addAllSequenceFlows(subProcess.getFlowElements(), flowSourceMap, flowTargetMap);
      }
    }
  }
  
  private void postProcessElements(Process process, Collection<FlowElement> flowElementList,
      Map<String, SequenceFlow> flowSourceMap, Map<String, SequenceFlow> flowTargetMap) {
    
    for (FlowElement flowElement : flowElementList) {
      
      if (flowElement instanceof FlowNode) {
        if (flowSourceMap.containsKey(flowElement.getId())) {
          ((FlowNode) flowElement).getOutgoingFlows().add(flowSourceMap.get(flowElement.getId()));
        }
        if (flowTargetMap.containsKey(flowElement.getId())) {
          ((FlowNode) flowElement).getIncomingFlows().add(flowTargetMap.get(flowElement.getId()));
        }
      }
      
      if (flowElement instanceof BoundaryEvent) {
        BoundaryEvent boundaryEvent = (BoundaryEvent) flowElement;
        Activity activity = retrieveAttachedRefObject(boundaryEvent.getAttachedToRefId(), process.getFlowElements());
        
        if (activity == null) {
          LOGGER.warn("Boundary event " + boundaryEvent.getId() + " is not attached to any activity");
        } else {
          boundaryEvent.setAttachedToRef(activity);
          activity.getBoundaryEvents().add(boundaryEvent);
        }
      } else if (flowElement instanceof SubProcess) {
        SubProcess subProcess = (SubProcess) flowElement;
        postProcessElements(process, subProcess.getFlowElements(), flowSourceMap, flowTargetMap);
      }
    }
  }
   
  private Activity retrieveAttachedRefObject(String attachedToRefId, Collection<FlowElement> flowElementList) {
    for (FlowElement flowElement : flowElementList) {
      if (attachedToRefId.equals(flowElement.getId())) {
        return (Activity) flowElement;
      } else if (flowElement instanceof SubProcess) {
        SubProcess subProcess = (SubProcess) flowElement;
        Activity activity = retrieveAttachedRefObject(attachedToRefId, subProcess.getFlowElements());
        if (activity != null) {
          return activity;
        }
      }
    }
    return null;
  }
  
  private void readShapeDI(JsonNode objectNode, double parentX, double parentY, 
      Map<String, JsonNode> shapeMap, Map<String, JsonNode> sourceRefMap, BpmnModel bpmnModel) {
    
    if (objectNode.get(EDITOR_CHILD_SHAPES) != null) {
      for (JsonNode jsonChildNode : objectNode.get(EDITOR_CHILD_SHAPES)) {
        
        String stencilId = BpmnJsonConverterUtil.getStencilId(jsonChildNode);
        if (STENCIL_SEQUENCE_FLOW.equals(stencilId) == false) {
          
          GraphicInfo graphicInfo = new GraphicInfo();
          
          JsonNode boundsNode = jsonChildNode.get(EDITOR_BOUNDS);
          ObjectNode upperLeftNode = (ObjectNode) boundsNode.get(EDITOR_BOUNDS_UPPER_LEFT);
          graphicInfo.setX(upperLeftNode.get(EDITOR_BOUNDS_X).asDouble() + parentX);
          graphicInfo.setY(upperLeftNode.get(EDITOR_BOUNDS_Y).asDouble() + parentY);
          
          ObjectNode lowerRightNode = (ObjectNode) boundsNode.get(EDITOR_BOUNDS_LOWER_RIGHT);
          graphicInfo.setWidth(lowerRightNode.get(EDITOR_BOUNDS_X).asDouble() - graphicInfo.getX() + parentX);
          graphicInfo.setHeight(lowerRightNode.get(EDITOR_BOUNDS_Y).asDouble() - graphicInfo.getY() + parentY);
          
          String childShapeId = jsonChildNode.get(EDITOR_SHAPE_ID).asText();
          bpmnModel.addGraphicInfo(BpmnJsonConverterUtil.getElementId(jsonChildNode), graphicInfo);
          
          shapeMap.put(childShapeId, jsonChildNode);
          
          ArrayNode outgoingNode = (ArrayNode) jsonChildNode.get("outgoing");
          if (outgoingNode != null && outgoingNode.size() > 0) {
            for (JsonNode outgoingChildNode : outgoingNode) {
              JsonNode resourceNode = outgoingChildNode.get(EDITOR_SHAPE_ID);
              if (resourceNode != null) {
                sourceRefMap.put(resourceNode.asText(), jsonChildNode);
              }
            }
          }
          
          readShapeDI(jsonChildNode, graphicInfo.getX(), graphicInfo.getY(), shapeMap, sourceRefMap, bpmnModel);
        }
      }
    }
  }
  
  private void filterAllEdges(JsonNode objectNode, 
      Map<String, JsonNode> edgeMap, Map<String, List<JsonNode>> sourceAndTargetMap,
      Map<String, JsonNode> shapeMap, Map<String, JsonNode> sourceRefMap) {
    
    if (objectNode.get(EDITOR_CHILD_SHAPES) != null) {
      for (JsonNode jsonChildNode : objectNode.get(EDITOR_CHILD_SHAPES)) {
        
        ObjectNode childNode = (ObjectNode) jsonChildNode;
        String stencilId = BpmnJsonConverterUtil.getStencilId(childNode);
        if (STENCIL_SUB_PROCESS.equals(stencilId)) {
          filterAllEdges(childNode, edgeMap, sourceAndTargetMap, shapeMap, sourceRefMap);
          
        } else if (STENCIL_SEQUENCE_FLOW.equals(stencilId)) {
          
          String childEdgeId = BpmnJsonConverterUtil.getElementId(childNode);
          String targetRefId = childNode.get("target").get(EDITOR_SHAPE_ID).asText();
          List<JsonNode> sourceAndTargetList = new ArrayList<JsonNode>();
          sourceAndTargetList.add(sourceRefMap.get(childNode.get(EDITOR_SHAPE_ID).asText()));
          sourceAndTargetList.add(shapeMap.get(targetRefId));
          
          edgeMap.put(childEdgeId, childNode);
          sourceAndTargetMap.put(childEdgeId, sourceAndTargetList);
        }
      }
    }
  }
  
  private void readEdgeDI(Map<String, JsonNode> edgeMap, Map<String, List<JsonNode>> sourceAndTargetMap, BpmnModel bpmnModel) {
    for (String edgeId : edgeMap.keySet()) {
      
      JsonNode edgeNode = edgeMap.get(edgeId);
      List<JsonNode> sourceAndTargetList = sourceAndTargetMap.get(edgeId);
      
      JsonNode sourceRefNode = sourceAndTargetList.get(0);
      JsonNode targetRefNode = sourceAndTargetList.get(1);
      
      if (sourceRefNode == null) {
      	LOGGER.info("Skipping edge {} because source ref is null", edgeId);
      	continue;
      }
      
      if (targetRefNode == null) {
      	LOGGER.info("Skipping edge {} because target ref is null", edgeId);
      	continue;
      }
      
      JsonNode dockersNode = edgeNode.get(EDITOR_DOCKERS);
      double sourceDockersX = dockersNode.get(0).get(EDITOR_BOUNDS_X).getDoubleValue();
      double sourceDockersY = dockersNode.get(0).get(EDITOR_BOUNDS_Y).getDoubleValue();
      
      GraphicInfo sourceInfo = bpmnModel.getGraphicInfo(BpmnJsonConverterUtil.getElementId(sourceRefNode));
      GraphicInfo targetInfo = bpmnModel.getGraphicInfo(BpmnJsonConverterUtil.getElementId(targetRefNode));
      
      /*JsonNode sourceRefBoundsNode = sourceRefNode.get(EDITOR_BOUNDS);
      BoundsLocation sourceRefUpperLeftLocation = getLocation(EDITOR_BOUNDS_UPPER_LEFT, sourceRefBoundsNode);
      BoundsLocation sourceRefLowerRightLocation = getLocation(EDITOR_BOUNDS_LOWER_RIGHT, sourceRefBoundsNode);
      
      JsonNode targetRefBoundsNode = targetRefNode.get(EDITOR_BOUNDS);
      BoundsLocation targetRefUpperLeftLocation = getLocation(EDITOR_BOUNDS_UPPER_LEFT, targetRefBoundsNode);
      BoundsLocation targetRefLowerRightLocation = getLocation(EDITOR_BOUNDS_LOWER_RIGHT, targetRefBoundsNode);*/
      
      double sourceRefLineX = sourceInfo.getX() + sourceDockersX;
      double sourceRefLineY = sourceInfo.getY() + sourceDockersY;
      
      double nextPointInLineX;
      double nextPointInLineY;
      
      nextPointInLineX = dockersNode.get(1).get(EDITOR_BOUNDS_X).getDoubleValue();
      nextPointInLineY = dockersNode.get(1).get(EDITOR_BOUNDS_Y).getDoubleValue();
      if (dockersNode.size() == 2) {
        nextPointInLineX += targetInfo.getX();
        nextPointInLineY += targetInfo.getY();
      }
      
      Line2D firstLine = new Line2D(sourceRefLineX, sourceRefLineY, nextPointInLineX, nextPointInLineY);
      
      String sourceRefStencilId = BpmnJsonConverterUtil.getStencilId(sourceRefNode);
      String targetRefStencilId = BpmnJsonConverterUtil.getStencilId(targetRefNode);
      
      List<GraphicInfo> graphicInfoList = new ArrayList<GraphicInfo>();
      
      if (DI_CIRCLES.contains(sourceRefStencilId)) {
        Circle2D eventCircle = new Circle2D(sourceInfo.getX() + sourceDockersX, 
            sourceInfo.getY() + sourceDockersY, sourceDockersX);
        
        Collection<Point2D> intersections = eventCircle.intersections(firstLine);
        Point2D intersection = intersections.iterator().next();
        graphicInfoList.add(createGraphicInfo(intersection.getX(), intersection.getY()));
      
      } else if (DI_RECTANGLES.contains(sourceRefStencilId)) {
        Polyline2D rectangle = createRectangle(sourceInfo);
        
        Collection<Point2D> intersections = rectangle.intersections(firstLine);
        Point2D intersection = intersections.iterator().next();
        graphicInfoList.add(createGraphicInfo(intersection.getX(), intersection.getY()));
      
      } else if (DI_GATEWAY.contains(sourceRefStencilId)) {
        Polyline2D gatewayRectangle = createGateway(sourceInfo);
        
        Collection<Point2D> intersections = gatewayRectangle.intersections(firstLine);
        Point2D intersection = intersections.iterator().next();
        graphicInfoList.add(createGraphicInfo(intersection.getX(), intersection.getY()));
      }
      
      Line2D lastLine = null;
      
      if (dockersNode.size() > 2) {
        for(int i = 1; i < dockersNode.size() - 1; i++) {
          double x = dockersNode.get(i).get(EDITOR_BOUNDS_X).getDoubleValue();
          double y = dockersNode.get(i).get(EDITOR_BOUNDS_Y).getDoubleValue();
          graphicInfoList.add(createGraphicInfo(x, y));
        }
        
        double startLastLineX = dockersNode.get(dockersNode.size() - 2).get(EDITOR_BOUNDS_X).getDoubleValue();
        double startLastLineY = dockersNode.get(dockersNode.size() - 2).get(EDITOR_BOUNDS_Y).getDoubleValue();
        
        double endLastLineX = dockersNode.get(dockersNode.size() - 1).get(EDITOR_BOUNDS_X).getDoubleValue();
        double endLastLineY = dockersNode.get(dockersNode.size() - 1).get(EDITOR_BOUNDS_Y).getDoubleValue();
        
        endLastLineX += targetInfo.getX();
        endLastLineY += targetInfo.getY();
        
        lastLine = new Line2D(startLastLineX, startLastLineY, endLastLineX, endLastLineY);
        
      } else {
        lastLine = firstLine;
      }
      
      if (DI_RECTANGLES.contains(targetRefStencilId)) {
        Polyline2D rectangle = createRectangle(targetInfo);
        
        Collection<Point2D> intersections = rectangle.intersections(lastLine);
        Point2D intersection = intersections.iterator().next();
        graphicInfoList.add(createGraphicInfo(intersection.getX(), intersection.getY()));
        
      } else if (DI_CIRCLES.contains(targetRefStencilId)) {
        
        double targetDockersX = dockersNode.get(dockersNode.size() - 1).get(EDITOR_BOUNDS_X).getDoubleValue();
        double targetDockersY = dockersNode.get(dockersNode.size() - 1).get(EDITOR_BOUNDS_Y).getDoubleValue();
        
        Circle2D eventCircle = new Circle2D(targetInfo.getX() + targetDockersX, 
            targetInfo.getY() + targetDockersY, targetDockersX);
        
        Collection<Point2D> intersections = eventCircle.intersections(lastLine);
        Point2D intersection = intersections.iterator().next();
        graphicInfoList.add(createGraphicInfo(intersection.getX(), intersection.getY()));
        
      } else if (DI_GATEWAY.contains(targetRefStencilId)) {
        Polyline2D gatewayRectangle = createGateway(targetInfo);
        
        Collection<Point2D> intersections = gatewayRectangle.intersections(lastLine);
        Point2D intersection = intersections.iterator().next();
        graphicInfoList.add(createGraphicInfo(intersection.getX(), intersection.getY()));
      }
      
      bpmnModel.addFlowGraphicInfoList(edgeId, graphicInfoList);
    }
  }
  
  private Polyline2D createRectangle(GraphicInfo graphicInfo) {
    Polyline2D rectangle = new Polyline2D(new Point2D(graphicInfo.getX(), graphicInfo.getY()),
        new Point2D(graphicInfo.getX() + graphicInfo.getWidth(), graphicInfo.getY()),
        new Point2D(graphicInfo.getX() + graphicInfo.getWidth(), graphicInfo.getY() + graphicInfo.getHeight()),
        new Point2D(graphicInfo.getX(), graphicInfo.getY() + graphicInfo.getHeight()),
        new Point2D(graphicInfo.getX(), graphicInfo.getY()));
    return rectangle;
  }
  
  private Polyline2D createGateway(GraphicInfo graphicInfo) {
    
    double middleX = graphicInfo.getX() + (graphicInfo.getWidth() / 2);
    double middleY = graphicInfo.getY() + (graphicInfo.getHeight() / 2);
    
    Polyline2D gatewayRectangle = new Polyline2D(new Point2D(graphicInfo.getX(), middleY),
        new Point2D(middleX, graphicInfo.getY()),
        new Point2D(graphicInfo.getX() + graphicInfo.getWidth(), middleY),
        new Point2D(middleX, graphicInfo.getY() + graphicInfo.getHeight()),
        new Point2D(graphicInfo.getX(), middleY));
    
    return gatewayRectangle;
  }
  
  private GraphicInfo createGraphicInfo(double x, double y) {
    GraphicInfo graphicInfo = new GraphicInfo();
    graphicInfo.setX(x);
    graphicInfo.setY(y);
    return graphicInfo;
  }
}

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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import math.geom2d.Point2D;
import math.geom2d.conic.Circle2D;
import math.geom2d.curve.AbstractContinuousCurve2D;
import math.geom2d.line.Line2D;
import math.geom2d.polygon.Polyline2D;
import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.Artifact;
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Event;
import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.ExtensionElement;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowElementsContainer;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.Gateway;
import org.activiti.bpmn.model.GraphicInfo;
import org.activiti.bpmn.model.Lane;
import org.activiti.bpmn.model.Message;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.bpmn.model.MessageFlow;
import org.activiti.bpmn.model.Pool;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.Signal;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.ValuedDataObject;
import org.activiti.editor.constants.EditorJsonConstants;
import org.activiti.editor.constants.StencilConstants;
import org.activiti.editor.language.json.converter.util.CollectionUtils;
import org.activiti.editor.language.json.converter.util.JsonConverterUtil;
import org.activiti.editor.language.json.model.ModelInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**

 */
public class BpmnJsonConverter implements EditorJsonConstants,
                                          StencilConstants,
                                          ActivityProcessor {

    protected static final Logger LOGGER = LoggerFactory.getLogger(BpmnJsonConverter.class);

    protected ObjectMapper objectMapper = new ObjectMapper();

    protected static Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap = new HashMap<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>>();
    protected static Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap = new HashMap<String, Class<? extends BaseBpmnJsonConverter>>();

    public final static String MODELER_NAMESPACE = "http://activiti.com/modeler";
    protected final static DateFormat defaultFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    protected final static DateFormat entFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

    static {

        // start and end events
        StartEventJsonConverter.fillTypes(convertersToBpmnMap,
                                          convertersToJsonMap);
        EndEventJsonConverter.fillTypes(convertersToBpmnMap,
                                        convertersToJsonMap);

        // connectors
        SequenceFlowJsonConverter.fillTypes(convertersToBpmnMap,
                                            convertersToJsonMap);
        MessageFlowJsonConverter.fillTypes(convertersToBpmnMap,
                                           convertersToJsonMap);
        AssociationJsonConverter.fillTypes(convertersToBpmnMap,
                                           convertersToJsonMap);

        // task types
        BusinessRuleTaskJsonConverter.fillTypes(convertersToBpmnMap,
                                                convertersToJsonMap);
        MailTaskJsonConverter.fillTypes(convertersToBpmnMap,
                                        convertersToJsonMap);
        ManualTaskJsonConverter.fillTypes(convertersToBpmnMap,
                                          convertersToJsonMap);
        ReceiveTaskJsonConverter.fillTypes(convertersToBpmnMap,
                                           convertersToJsonMap);
        ScriptTaskJsonConverter.fillTypes(convertersToBpmnMap,
                                          convertersToJsonMap);
        ServiceTaskJsonConverter.fillTypes(convertersToBpmnMap,
                                           convertersToJsonMap);
        UserTaskJsonConverter.fillTypes(convertersToBpmnMap,
                                        convertersToJsonMap);
        CallActivityJsonConverter.fillTypes(convertersToBpmnMap,
                                            convertersToJsonMap);
        CamelTaskJsonConverter.fillTypes(convertersToBpmnMap,
                                         convertersToJsonMap);
        MuleTaskJsonConverter.fillTypes(convertersToBpmnMap,
                                        convertersToJsonMap);
        SendTaskJsonConverter.fillTypes(convertersToBpmnMap,
                                        convertersToJsonMap);
        DecisionTaskJsonConverter.fillTypes(convertersToBpmnMap,
                                            convertersToJsonMap);

        // gateways
        ExclusiveGatewayJsonConverter.fillTypes(convertersToBpmnMap,
                                                convertersToJsonMap);
        InclusiveGatewayJsonConverter.fillTypes(convertersToBpmnMap,
                                                convertersToJsonMap);
        ParallelGatewayJsonConverter.fillTypes(convertersToBpmnMap,
                                               convertersToJsonMap);
        EventGatewayJsonConverter.fillTypes(convertersToBpmnMap,
                                            convertersToJsonMap);

        // scope constructs
        SubProcessJsonConverter.fillTypes(convertersToBpmnMap,
                                          convertersToJsonMap);
        EventSubProcessJsonConverter.fillTypes(convertersToBpmnMap,
                                               convertersToJsonMap);

        // catch events
        CatchEventJsonConverter.fillTypes(convertersToBpmnMap,
                                          convertersToJsonMap);

        // throw events
        ThrowEventJsonConverter.fillTypes(convertersToBpmnMap,
                                          convertersToJsonMap);

        // boundary events
        BoundaryEventJsonConverter.fillTypes(convertersToBpmnMap,
                                             convertersToJsonMap);

        // artifacts
        TextAnnotationJsonConverter.fillTypes(convertersToBpmnMap,
                                              convertersToJsonMap);
        DataStoreJsonConverter.fillTypes(convertersToBpmnMap,
                                         convertersToJsonMap);
    }

    private static final List<String> DI_CIRCLES = new ArrayList<String>();
    private static final List<String> DI_RECTANGLES = new ArrayList<String>();
    private static final List<String> DI_GATEWAY = new ArrayList<String>();

    static {
        DI_CIRCLES.add(STENCIL_EVENT_START_ERROR);
        DI_CIRCLES.add(STENCIL_EVENT_START_MESSAGE);
        DI_CIRCLES.add(STENCIL_EVENT_START_NONE);
        DI_CIRCLES.add(STENCIL_EVENT_START_TIMER);
        DI_CIRCLES.add(STENCIL_EVENT_START_SIGNAL);

        DI_CIRCLES.add(STENCIL_EVENT_BOUNDARY_ERROR);
        DI_CIRCLES.add(STENCIL_EVENT_BOUNDARY_SIGNAL);
        DI_CIRCLES.add(STENCIL_EVENT_BOUNDARY_TIMER);
        DI_CIRCLES.add(STENCIL_EVENT_BOUNDARY_MESSAGE);
        DI_CIRCLES.add(STENCIL_EVENT_BOUNDARY_CANCEL);
        DI_CIRCLES.add(STENCIL_EVENT_BOUNDARY_COMPENSATION);

        DI_CIRCLES.add(STENCIL_EVENT_CATCH_MESSAGE);
        DI_CIRCLES.add(STENCIL_EVENT_CATCH_SIGNAL);
        DI_CIRCLES.add(STENCIL_EVENT_CATCH_TIMER);

        DI_CIRCLES.add(STENCIL_EVENT_THROW_NONE);
        DI_CIRCLES.add(STENCIL_EVENT_THROW_SIGNAL);

        DI_CIRCLES.add(STENCIL_EVENT_END_NONE);
        DI_CIRCLES.add(STENCIL_EVENT_END_ERROR);
        DI_CIRCLES.add(STENCIL_EVENT_END_CANCEL);
        DI_CIRCLES.add(STENCIL_EVENT_END_TERMINATE);

        DI_RECTANGLES.add(STENCIL_CALL_ACTIVITY);
        DI_RECTANGLES.add(STENCIL_SUB_PROCESS);
        DI_RECTANGLES.add(STENCIL_EVENT_SUB_PROCESS);
        DI_RECTANGLES.add(STENCIL_TASK_BUSINESS_RULE);
        DI_RECTANGLES.add(STENCIL_TASK_MAIL);
        DI_RECTANGLES.add(STENCIL_TASK_MANUAL);
        DI_RECTANGLES.add(STENCIL_TASK_RECEIVE);
        DI_RECTANGLES.add(STENCIL_TASK_SCRIPT);
        DI_RECTANGLES.add(STENCIL_TASK_SEND);
        DI_RECTANGLES.add(STENCIL_TASK_SERVICE);
        DI_RECTANGLES.add(STENCIL_TASK_USER);
        DI_RECTANGLES.add(STENCIL_TASK_CAMEL);
        DI_RECTANGLES.add(STENCIL_TASK_MULE);
        DI_RECTANGLES.add(STENCIL_TASK_DECISION);
        DI_RECTANGLES.add(STENCIL_TEXT_ANNOTATION);

        DI_GATEWAY.add(STENCIL_GATEWAY_EVENT);
        DI_GATEWAY.add(STENCIL_GATEWAY_EXCLUSIVE);
        DI_GATEWAY.add(STENCIL_GATEWAY_INCLUSIVE);
        DI_GATEWAY.add(STENCIL_GATEWAY_PARALLEL);
    }

    public ObjectNode convertToJson(BpmnModel model) {
        return convertToJson(model,
                             null,
                             null);
    }

    public ObjectNode convertToJson(BpmnModel model,
                                    Map<String, ModelInfo> formKeyMap,
                                    Map<String, ModelInfo> decisionTableKeyMap) {
        ObjectNode modelNode = objectMapper.createObjectNode();
        double maxX = 0.0;
        double maxY = 0.0;
        for (GraphicInfo flowInfo : model.getLocationMap().values()) {
            if ((flowInfo.getX() + flowInfo.getWidth()) > maxX) {
                maxX = flowInfo.getX() + flowInfo.getWidth();
            }

            if ((flowInfo.getY() + flowInfo.getHeight()) > maxY) {
                maxY = flowInfo.getY() + flowInfo.getHeight();
            }
        }
        maxX += 50;
        maxY += 50;

        if (maxX < 1485) {
            maxX = 1485;
        }

        if (maxY < 700) {
            maxY = 700;
        }

        modelNode.set("bounds",
                      BpmnJsonConverterUtil.createBoundsNode(maxX,
                                                             maxY,
                                                             0,
                                                             0));
        modelNode.put("resourceId",
                      "canvas");

        ObjectNode stencilNode = objectMapper.createObjectNode();
        stencilNode.put("id",
                        "BPMNDiagram");
        modelNode.set("stencil",
                      stencilNode);

        ObjectNode stencilsetNode = objectMapper.createObjectNode();
        stencilsetNode.put("namespace",
                           "http://b3mn.org/stencilset/bpmn2.0#");
        stencilsetNode.put("url",
                           "../editor/stencilsets/bpmn2.0/bpmn2.0.json");
        modelNode.set("stencilset",
                      stencilsetNode);

        ArrayNode shapesArrayNode = objectMapper.createArrayNode();

        Process mainProcess = null;
        if (model.getPools().size() > 0) {
            mainProcess = model.getProcess(model.getPools().get(0).getId());
        } else {
            mainProcess = model.getMainProcess();
        }

        ObjectNode propertiesNode = objectMapper.createObjectNode();
        if (StringUtils.isNotEmpty(mainProcess.getId())) {
            propertiesNode.put(PROPERTY_PROCESS_ID,
                               mainProcess.getId());
        }
        if (StringUtils.isNotEmpty(mainProcess.getName())) {
            propertiesNode.put(PROPERTY_NAME,
                               mainProcess.getName());
        }
        if (StringUtils.isNotEmpty(mainProcess.getDocumentation())) {
            propertiesNode.put(PROPERTY_DOCUMENTATION,
                               mainProcess.getDocumentation());
        }
        if (!mainProcess.isExecutable()) {
            propertiesNode.put(PROPERTY_PROCESS_EXECUTABLE,
                               "No");
        }
        if (StringUtils.isNoneEmpty(model.getTargetNamespace())) {
            propertiesNode.put(PROPERTY_PROCESS_NAMESPACE,
                               model.getTargetNamespace());
        }

        BpmnJsonConverterUtil.convertMessagesToJson(model.getMessages(),
                                                    propertiesNode);

        BpmnJsonConverterUtil.convertListenersToJson(mainProcess.getExecutionListeners(),
                                                     true,
                                                     propertiesNode);
        BpmnJsonConverterUtil.convertEventListenersToJson(mainProcess.getEventListeners(),
                                                          propertiesNode);
        BpmnJsonConverterUtil.convertSignalDefinitionsToJson(model,
                                                             propertiesNode);
        BpmnJsonConverterUtil.convertMessagesToJson(model,
                                                    propertiesNode);

        if (CollectionUtils.isNotEmpty(mainProcess.getDataObjects())) {
            BpmnJsonConverterUtil.convertDataPropertiesToJson(mainProcess.getDataObjects(),
                                                              propertiesNode);
        }

        modelNode.set(EDITOR_SHAPE_PROPERTIES,
                      propertiesNode);

        boolean poolHasDI = false;
        if (model.getPools().size() > 0) {
            for (Pool pool : model.getPools()) {
                GraphicInfo graphicInfo = model.getGraphicInfo(pool.getId());
                if (graphicInfo != null) {
                    poolHasDI = true;
                    break;
                }
            }
        }

        if (model.getPools().size() > 0 && poolHasDI) {
            for (Pool pool : model.getPools()) {
                GraphicInfo poolGraphicInfo = model.getGraphicInfo(pool.getId());
                if (poolGraphicInfo == null) {
                    continue;
                }
                ObjectNode poolNode = BpmnJsonConverterUtil.createChildShape(pool.getId(),
                                                                             STENCIL_POOL,
                                                                             poolGraphicInfo.getX() + poolGraphicInfo.getWidth(),
                                                                             poolGraphicInfo.getY() + poolGraphicInfo.getHeight(),
                                                                             poolGraphicInfo.getX(),
                                                                             poolGraphicInfo.getY());
                shapesArrayNode.add(poolNode);
                ObjectNode poolPropertiesNode = objectMapper.createObjectNode();
                poolPropertiesNode.put(PROPERTY_OVERRIDE_ID,
                                       pool.getId());
                poolPropertiesNode.put(PROPERTY_PROCESS_ID,
                                       pool.getProcessRef());
                if (!pool.isExecutable()) {
                    poolPropertiesNode.put(PROPERTY_PROCESS_EXECUTABLE,
                                           PROPERTY_VALUE_NO);
                }
                if (StringUtils.isNotEmpty(pool.getName())) {
                    poolPropertiesNode.put(PROPERTY_NAME,
                                           pool.getName());
                }
                poolNode.set(EDITOR_SHAPE_PROPERTIES,
                             poolPropertiesNode);

                ArrayNode laneShapesArrayNode = objectMapper.createArrayNode();
                poolNode.set(EDITOR_CHILD_SHAPES,
                             laneShapesArrayNode);

                ArrayNode outgoingArrayNode = objectMapper.createArrayNode();
                poolNode.set("outgoing",
                             outgoingArrayNode);

                Process process = model.getProcess(pool.getId());
                if (process != null) {
                    Map<String, ArrayNode> laneMap = new HashMap<String, ArrayNode>();
                    for (Lane lane : process.getLanes()) {
                        GraphicInfo laneGraphicInfo = model.getGraphicInfo(lane.getId());
                        if (laneGraphicInfo == null) {
                            continue;
                        }
                        ObjectNode laneNode = BpmnJsonConverterUtil.createChildShape(lane.getId(),
                                                                                     STENCIL_LANE,
                                                                                     laneGraphicInfo.getX() + laneGraphicInfo.getWidth() - poolGraphicInfo.getX(),
                                                                                     laneGraphicInfo.getY() + laneGraphicInfo.getHeight() - poolGraphicInfo.getY(),
                                                                                     laneGraphicInfo.getX() - poolGraphicInfo.getX(),
                                                                                     laneGraphicInfo.getY() - poolGraphicInfo.getY());
                        laneShapesArrayNode.add(laneNode);
                        ObjectNode lanePropertiesNode = objectMapper.createObjectNode();
                        lanePropertiesNode.put(PROPERTY_OVERRIDE_ID,
                                               lane.getId());
                        if (StringUtils.isNotEmpty(lane.getName())) {
                            lanePropertiesNode.put(PROPERTY_NAME,
                                                   lane.getName());
                        }
                        laneNode.set(EDITOR_SHAPE_PROPERTIES,
                                     lanePropertiesNode);

                        ArrayNode elementShapesArrayNode = objectMapper.createArrayNode();
                        laneNode.set(EDITOR_CHILD_SHAPES,
                                     elementShapesArrayNode);
                        laneNode.set("outgoing",
                                     objectMapper.createArrayNode());

                        laneMap.put(lane.getId(),
                                    elementShapesArrayNode);
                    }

                    for (FlowElement flowElement : process.getFlowElements()) {

                        Lane laneForElement = null;
                        GraphicInfo laneGraphicInfo = null;

                        FlowElement lookForElement = null;
                        if (flowElement instanceof SequenceFlow) {
                            SequenceFlow sequenceFlow = (SequenceFlow) flowElement;
                            lookForElement = model.getFlowElement(sequenceFlow.getSourceRef());
                        } else {
                            lookForElement = flowElement;
                        }

                        for (Lane lane : process.getLanes()) {
                            if (lane.getFlowReferences().contains(lookForElement.getId())) {
                                laneGraphicInfo = model.getGraphicInfo(lane.getId());
                                if (laneGraphicInfo != null) {
                                    laneForElement = lane;
                                }
                                break;
                            }
                        }

                        if (flowElement instanceof SequenceFlow || laneForElement != null) {
                            processFlowElement(flowElement,
                                               process,
                                               model,
                                               laneMap.get(laneForElement.getId()),
                                               formKeyMap,
                                               decisionTableKeyMap,
                                               laneGraphicInfo.getX(),
                                               laneGraphicInfo.getY());
                        }
                    }

                    processArtifacts(process,
                                     model,
                                     shapesArrayNode,
                                     0.0,
                                     0.0);
                }

                for (MessageFlow messageFlow : model.getMessageFlows().values()) {
                    if (messageFlow.getSourceRef().equals(pool.getId())) {
                        outgoingArrayNode.add(BpmnJsonConverterUtil.createResourceNode(messageFlow.getId()));
                    }
                }
            }

            processMessageFlows(model,
                                shapesArrayNode);
        } else {
            processFlowElements(model.getMainProcess(),
                                model,
                                shapesArrayNode,
                                formKeyMap,
                                decisionTableKeyMap,
                                0.0,
                                0.0);
            processMessageFlows(model,
                                shapesArrayNode);
        }

        modelNode.set(EDITOR_CHILD_SHAPES,
                      shapesArrayNode);
        return modelNode;
    }

    public void processFlowElements(FlowElementsContainer container,
                                    BpmnModel model,
                                    ArrayNode shapesArrayNode,
                                    Map<String, ModelInfo> formKeyMap,
                                    Map<String, ModelInfo> decisionTableKeyMap,
                                    double subProcessX,
                                    double subProcessY) {

        for (FlowElement flowElement : container.getFlowElements()) {
            processFlowElement(flowElement,
                               container,
                               model,
                               shapesArrayNode,
                               formKeyMap,
                               decisionTableKeyMap,
                               subProcessX,
                               subProcessY);
        }

        processArtifacts(container,
                         model,
                         shapesArrayNode,
                         subProcessX,
                         subProcessY);
    }

    protected void processFlowElement(FlowElement flowElement,
                                      FlowElementsContainer container,
                                      BpmnModel model,
                                      ArrayNode shapesArrayNode,
                                      Map<String, ModelInfo> formKeyMap,
                                      Map<String, ModelInfo> decisionTableKeyMap,
                                      double containerX,
                                      double containerY) {

        Class<? extends BaseBpmnJsonConverter> converter = convertersToJsonMap.get(flowElement.getClass());
        if (converter != null) {
            try {
                BaseBpmnJsonConverter converterInstance = converter.newInstance();
                if (converterInstance instanceof FormKeyAwareConverter) {
                    ((FormKeyAwareConverter) converterInstance).setFormKeyMap(formKeyMap);
                }
                if (converterInstance instanceof DecisionTableKeyAwareConverter) {
                    ((DecisionTableKeyAwareConverter) converterInstance).setDecisionTableKeyMap(decisionTableKeyMap);
                }

                converterInstance.convertToJson(flowElement,
                                                this,
                                                model,
                                                container,
                                                shapesArrayNode,
                                                containerX,
                                                containerY);
            } catch (Exception e) {
                LOGGER.error("Error converting {}",
                             flowElement,
                             e);
            }
        }
    }

    protected void processArtifacts(FlowElementsContainer container,
                                    BpmnModel model,
                                    ArrayNode shapesArrayNode,
                                    double containerX,
                                    double containerY) {

        for (Artifact artifact : container.getArtifacts()) {
            Class<? extends BaseBpmnJsonConverter> converter = convertersToJsonMap.get(artifact.getClass());
            if (converter != null) {
                try {
                    converter.newInstance().convertToJson(artifact,
                                                          this,
                                                          model,
                                                          container,
                                                          shapesArrayNode,
                                                          containerX,
                                                          containerY);
                } catch (Exception e) {
                    LOGGER.error("Error converting {}",
                                 artifact,
                                 e);
                }
            }
        }
    }

    protected void processMessageFlows(BpmnModel model,
                                       ArrayNode shapesArrayNode) {
        for (MessageFlow messageFlow : model.getMessageFlows().values()) {
            MessageFlowJsonConverter jsonConverter = new MessageFlowJsonConverter();
            jsonConverter.convertToJson(messageFlow,
                                        this,
                                        model,
                                        null,
                                        shapesArrayNode,
                                        0.0,
                                        0.0);
        }
    }

    public BpmnModel convertToBpmnModel(JsonNode modelNode) {
        return convertToBpmnModel(modelNode,
                                  null,
                                  null);
    }

    public BpmnModel convertToBpmnModel(JsonNode modelNode,
                                        Map<String, String> formKeyMap,
                                        Map<String, String> decisionTableKeyMap) {

        BpmnModel bpmnModel = new BpmnModel();

        bpmnModel.setTargetNamespace("http://activiti.org/test");
        Map<String, JsonNode> shapeMap = new HashMap<String, JsonNode>();
        Map<String, JsonNode> sourceRefMap = new HashMap<String, JsonNode>();
        Map<String, JsonNode> edgeMap = new HashMap<String, JsonNode>();
        Map<String, List<JsonNode>> sourceAndTargetMap = new HashMap<String, List<JsonNode>>();

        readShapeDI(modelNode,
                    0,
                    0,
                    shapeMap,
                    sourceRefMap,
                    bpmnModel);
        filterAllEdges(modelNode,
                       edgeMap,
                       sourceAndTargetMap,
                       shapeMap,
                       sourceRefMap);
        readEdgeDI(edgeMap,
                   sourceAndTargetMap,
                   bpmnModel);

        ArrayNode shapesArrayNode = (ArrayNode) modelNode.get(EDITOR_CHILD_SHAPES);

        if (shapesArrayNode == null || shapesArrayNode.size() == 0) {
            return bpmnModel;
        }

        boolean nonEmptyPoolFound = false;
        Map<String, Lane> elementInLaneMap = new HashMap<String, Lane>();
        // first create the pool structure
        for (JsonNode shapeNode : shapesArrayNode) {
            String stencilId = BpmnJsonConverterUtil.getStencilId(shapeNode);
            if (STENCIL_POOL.equals(stencilId)) {
                Pool pool = new Pool();
                pool.setId(BpmnJsonConverterUtil.getElementId(shapeNode));
                pool.setName(JsonConverterUtil.getPropertyValueAsString(PROPERTY_NAME,
                                                                        shapeNode));
                pool.setProcessRef(JsonConverterUtil.getPropertyValueAsString(PROPERTY_PROCESS_ID,
                                                                              shapeNode));
                pool.setExecutable(JsonConverterUtil.getPropertyValueAsBoolean(PROPERTY_PROCESS_EXECUTABLE,
                                                                               shapeNode,
                                                                               true));
                bpmnModel.getPools().add(pool);

                Process process = new Process();
                process.setId(pool.getProcessRef());
                process.setName(pool.getName());
                process.setExecutable(pool.isExecutable());
                bpmnModel.addProcess(process);

                ArrayNode laneArrayNode = (ArrayNode) shapeNode.get(EDITOR_CHILD_SHAPES);
                for (JsonNode laneNode : laneArrayNode) {
                    // should be a lane, but just check to be certain
                    String laneStencilId = BpmnJsonConverterUtil.getStencilId(laneNode);
                    if (STENCIL_LANE.equals(laneStencilId)) {
                        nonEmptyPoolFound = true;
                        Lane lane = new Lane();
                        lane.setId(BpmnJsonConverterUtil.getElementId(laneNode));
                        lane.setName(JsonConverterUtil.getPropertyValueAsString(PROPERTY_NAME,
                                                                                laneNode));
                        lane.setParentProcess(process);
                        process.getLanes().add(lane);

                        processJsonElements(laneNode.get(EDITOR_CHILD_SHAPES),
                                            modelNode,
                                            lane,
                                            shapeMap,
                                            formKeyMap,
                                            decisionTableKeyMap,
                                            bpmnModel);
                        if (CollectionUtils.isNotEmpty(lane.getFlowReferences())) {
                            for (String elementRef : lane.getFlowReferences()) {
                                elementInLaneMap.put(elementRef,
                                                     lane);
                            }
                        }
                    }
                }
            }
        }

        // Signal Definitions exist on the root level
        JsonNode signalDefinitionNode = BpmnJsonConverterUtil.getProperty(PROPERTY_SIGNAL_DEFINITIONS,
                                                                          modelNode);
        signalDefinitionNode = BpmnJsonConverterUtil.validateIfNodeIsTextual(signalDefinitionNode);
        signalDefinitionNode = BpmnJsonConverterUtil.validateIfNodeIsTextual(signalDefinitionNode); // no idea why this needs to be done twice ..
        if (signalDefinitionNode != null) {
            if (signalDefinitionNode instanceof ArrayNode) {
                ArrayNode signalDefinitionArrayNode = (ArrayNode) signalDefinitionNode;
                Iterator<JsonNode> signalDefinitionIterator = signalDefinitionArrayNode.iterator();
                while (signalDefinitionIterator.hasNext()) {
                    JsonNode signalDefinitionJsonNode = signalDefinitionIterator.next();
                    String signalId = signalDefinitionJsonNode.get(PROPERTY_SIGNAL_DEFINITION_ID).asText();
                    String signalName = signalDefinitionJsonNode.get(PROPERTY_SIGNAL_DEFINITION_NAME).asText();
                    String signalScope = signalDefinitionJsonNode.get(PROPERTY_SIGNAL_DEFINITION_SCOPE).asText();

                    if (StringUtils.isNotEmpty(signalId) && StringUtils.isNotEmpty(signalName)) {
                        Signal signal = new Signal();
                        signal.setId(signalId);
                        signal.setName(signalName);
                        signal.setScope((signalScope.toLowerCase().equals("processinstance")) ? Signal.SCOPE_PROCESS_INSTANCE : Signal.SCOPE_GLOBAL);
                        bpmnModel.addSignal(signal);
                    }
                }
            }
        }

        if (!nonEmptyPoolFound) {
            Process process = new Process();
            bpmnModel.getProcesses().add(process);
            process.setId(BpmnJsonConverterUtil.getPropertyValueAsString(PROPERTY_PROCESS_ID,
                                                                         modelNode));
            process.setName(BpmnJsonConverterUtil.getPropertyValueAsString(PROPERTY_NAME,
                                                                           modelNode));
            String namespace = BpmnJsonConverterUtil.getPropertyValueAsString(PROPERTY_PROCESS_NAMESPACE,
                                                                              modelNode);
            if (StringUtils.isNotEmpty(namespace)) {
                bpmnModel.setTargetNamespace(namespace);
            }
            process.setDocumentation(BpmnJsonConverterUtil.getPropertyValueAsString(PROPERTY_DOCUMENTATION,
                                                                                    modelNode));
            JsonNode processExecutableNode = JsonConverterUtil.getProperty(PROPERTY_PROCESS_EXECUTABLE,
                                                                           modelNode);
            if (processExecutableNode != null && StringUtils.isNotEmpty(processExecutableNode.asText())) {
                process.setExecutable(JsonConverterUtil.getPropertyValueAsBoolean(PROPERTY_PROCESS_EXECUTABLE,
                                                                                  modelNode));
            }

            BpmnJsonConverterUtil.convertJsonToMessages(modelNode,
                                                        bpmnModel);

            BpmnJsonConverterUtil.convertJsonToListeners(modelNode,
                                                         process);
            JsonNode eventListenersNode = BpmnJsonConverterUtil.getProperty(PROPERTY_EVENT_LISTENERS,
                                                                            modelNode);
            if (eventListenersNode != null) {
                eventListenersNode = BpmnJsonConverterUtil.validateIfNodeIsTextual(eventListenersNode);
                BpmnJsonConverterUtil.parseEventListeners(eventListenersNode.get(PROPERTY_EVENTLISTENER_VALUE),
                                                          process);
            }

            JsonNode processDataPropertiesNode = modelNode.get(EDITOR_SHAPE_PROPERTIES).get(PROPERTY_DATA_PROPERTIES);

            if (processDataPropertiesNode != null) {
                List<ValuedDataObject> dataObjects = BpmnJsonConverterUtil.convertJsonToDataProperties(processDataPropertiesNode,
                                                                                                       process);
                process.setDataObjects(dataObjects);
                process.getFlowElements().addAll(dataObjects);
            }

            processJsonElements(shapesArrayNode,
                                modelNode,
                                process,
                                shapeMap,
                                formKeyMap,
                                decisionTableKeyMap,
                                bpmnModel);
        } else {
            // sequence flows are on root level so need additional parsing for pools
            for (JsonNode shapeNode : shapesArrayNode) {
                if (STENCIL_SEQUENCE_FLOW.equalsIgnoreCase(BpmnJsonConverterUtil.getStencilId(shapeNode)) || STENCIL_ASSOCIATION.equalsIgnoreCase(BpmnJsonConverterUtil.getStencilId(shapeNode))) {

                    String sourceRef = BpmnJsonConverterUtil.lookForSourceRef(shapeNode.get(EDITOR_SHAPE_ID).asText(),
                                                                              modelNode.get(EDITOR_CHILD_SHAPES));
                    if (sourceRef != null) {
                        Lane lane = elementInLaneMap.get(sourceRef);
                        SequenceFlowJsonConverter flowConverter = new SequenceFlowJsonConverter();
                        if (lane != null) {
                            flowConverter.convertToBpmnModel(shapeNode,
                                                             modelNode,
                                                             this,
                                                             lane,
                                                             shapeMap,
                                                             bpmnModel);
                        } else {
                            flowConverter.convertToBpmnModel(shapeNode,
                                                             modelNode,
                                                             this,
                                                             bpmnModel.getProcesses().get(0),
                                                             shapeMap,
                                                             bpmnModel);
                        }
                    }
                }
            }
        }

        // sequence flows are now all on root level
        Map<String, SubProcess> subShapesMap = new HashMap<String, SubProcess>();
        for (Process process : bpmnModel.getProcesses()) {
            for (FlowElement flowElement : process.findFlowElementsOfType(SubProcess.class)) {
                SubProcess subProcess = (SubProcess) flowElement;
                fillSubShapes(subShapesMap,
                              subProcess);
            }

            if (subShapesMap.size() > 0) {
                List<String> removeSubFlowsList = new ArrayList<String>();
                for (FlowElement flowElement : process.findFlowElementsOfType(SequenceFlow.class)) {
                    SequenceFlow sequenceFlow = (SequenceFlow) flowElement;
                    if (subShapesMap.containsKey(sequenceFlow.getSourceRef())) {
                        SubProcess subProcess = subShapesMap.get(sequenceFlow.getSourceRef());
                        if (subProcess.getFlowElement(sequenceFlow.getId()) == null) {
                            subProcess.addFlowElement(sequenceFlow);
                            removeSubFlowsList.add(sequenceFlow.getId());
                        }
                    }
                }
                for (String flowId : removeSubFlowsList) {
                    process.removeFlowElement(flowId);
                }
            }
        }

        Map<String, FlowWithContainer> allFlowMap = new HashMap<String, FlowWithContainer>();
        List<Gateway> gatewayWithOrderList = new ArrayList<Gateway>();
        // post handling of process elements
        for (Process process : bpmnModel.getProcesses()) {
            postProcessElements(process,
                                process.getFlowElements(),
                                edgeMap,
                                bpmnModel,
                                allFlowMap,
                                gatewayWithOrderList);
        }

        // sort the sequence flows
        for (Gateway gateway : gatewayWithOrderList) {
            List<ExtensionElement> orderList = gateway.getExtensionElements().get("EDITOR_FLOW_ORDER");
            if (CollectionUtils.isNotEmpty(orderList)) {
                for (ExtensionElement orderElement : orderList) {
                    String flowValue = orderElement.getElementText();
                    if (StringUtils.isNotEmpty(flowValue)) {
                        if (allFlowMap.containsKey(flowValue)) {
                            FlowWithContainer flowWithContainer = allFlowMap.get(flowValue);
                            flowWithContainer.getFlowContainer().removeFlowElement(flowWithContainer.getSequenceFlow().getId());
                            flowWithContainer.getFlowContainer().addFlowElement(flowWithContainer.getSequenceFlow());
                        }
                    }
                }
            }
            gateway.getExtensionElements().remove("EDITOR_FLOW_ORDER");
        }

        return bpmnModel;
    }

    public void processJsonElements(JsonNode shapesArrayNode,
                                    JsonNode modelNode,
                                    BaseElement parentElement,
                                    Map<String, JsonNode> shapeMap,
                                    Map<String, String> formMap,
                                    Map<String, String> decisionTableMap,
                                    BpmnModel bpmnModel) {

        for (JsonNode shapeNode : shapesArrayNode) {
            String stencilId = BpmnJsonConverterUtil.getStencilId(shapeNode);
            Class<? extends BaseBpmnJsonConverter> converter = convertersToBpmnMap.get(stencilId);
            try {
                BaseBpmnJsonConverter converterInstance = converter.newInstance();
                if (converterInstance instanceof DecisionTableAwareConverter) {
                    ((DecisionTableAwareConverter) converterInstance).setDecisionTableMap(decisionTableMap);
                }

                if (converterInstance instanceof FormAwareConverter) {
                    ((FormAwareConverter) converterInstance).setFormMap(formMap);
                }

                converterInstance.convertToBpmnModel(shapeNode,
                                                     modelNode,
                                                     this,
                                                     parentElement,
                                                     shapeMap,
                                                     bpmnModel);
            } catch (Exception e) {
                LOGGER.error("Error converting {}",
                             BpmnJsonConverterUtil.getStencilId(shapeNode),
                             e);
            }
        }
    }

    private void fillSubShapes(Map<String, SubProcess> subShapesMap,
                               SubProcess subProcess) {
        for (FlowElement flowElement : subProcess.getFlowElements()) {
            if (flowElement instanceof SubProcess) {
                SubProcess childSubProcess = (SubProcess) flowElement;
                subShapesMap.put(childSubProcess.getId(),
                                 subProcess);
                fillSubShapes(subShapesMap,
                              childSubProcess);
            } else {
                subShapesMap.put(flowElement.getId(),
                                 subProcess);
            }
        }
    }

    private void postProcessElements(FlowElementsContainer parentContainer,
                                     Collection<FlowElement> flowElementList,
                                     Map<String, JsonNode> edgeMap,
                                     BpmnModel bpmnModel,
                                     Map<String, FlowWithContainer> allFlowMap,
                                     List<Gateway> gatewayWithOrderList) {

        for (FlowElement flowElement : flowElementList) {

            if (flowElement instanceof Event) {
                Event event = (Event) flowElement;
                if (CollectionUtils.isNotEmpty(event.getEventDefinitions())) {
                    EventDefinition eventDef = event.getEventDefinitions().get(0);
                    if (eventDef instanceof SignalEventDefinition) {
                        SignalEventDefinition signalEventDef = (SignalEventDefinition) eventDef;
                        if (StringUtils.isNotEmpty(signalEventDef.getSignalRef())) {
                            if (bpmnModel.getSignal(signalEventDef.getSignalRef()) == null) {
                                bpmnModel.addSignal(new Signal(signalEventDef.getSignalRef(),
                                                               signalEventDef.getSignalRef()));
                            }
                        }
                    } else if (eventDef instanceof MessageEventDefinition) {
                        MessageEventDefinition messageEventDef = (MessageEventDefinition) eventDef;
                        if (StringUtils.isNotEmpty(messageEventDef.getMessageRef())) {
                            if (bpmnModel.getMessage(messageEventDef.getMessageRef()) == null) {
                                bpmnModel.addMessage(new Message(messageEventDef.getMessageRef(),
                                                                 messageEventDef.getMessageRef(),
                                                                 null));
                            }
                        }
                    }
                }
            }

            if (flowElement instanceof BoundaryEvent) {
                BoundaryEvent boundaryEvent = (BoundaryEvent) flowElement;
                Activity activity = retrieveAttachedRefObject(boundaryEvent.getAttachedToRefId(),
                                                              parentContainer.getFlowElements());

                if (activity == null) {
                    LOGGER.warn("Boundary event " + boundaryEvent.getId() + " is not attached to any activity");
                } else {
                    boundaryEvent.setAttachedToRef(activity);
                    activity.getBoundaryEvents().add(boundaryEvent);
                }
            } else if (flowElement instanceof Gateway) {
                if (flowElement.getExtensionElements().containsKey("EDITOR_FLOW_ORDER")) {
                    gatewayWithOrderList.add((Gateway) flowElement);
                }
            } else if (flowElement instanceof SubProcess) {
                SubProcess subProcess = (SubProcess) flowElement;
                postProcessElements(subProcess,
                                    subProcess.getFlowElements(),
                                    edgeMap,
                                    bpmnModel,
                                    allFlowMap,
                                    gatewayWithOrderList);
            } else if (flowElement instanceof SequenceFlow) {
                SequenceFlow sequenceFlow = (SequenceFlow) flowElement;
                FlowElement sourceFlowElement = parentContainer.getFlowElement(sequenceFlow.getSourceRef());
                if (sourceFlowElement != null && sourceFlowElement instanceof FlowNode) {

                    FlowWithContainer flowWithContainer = new FlowWithContainer(sequenceFlow,
                                                                                parentContainer);
                    if (sequenceFlow.getExtensionElements().get("EDITOR_RESOURCEID") != null && sequenceFlow.getExtensionElements().get("EDITOR_RESOURCEID").size() > 0) {
                        allFlowMap.put(sequenceFlow.getExtensionElements().get("EDITOR_RESOURCEID").get(0).getElementText(),
                                       flowWithContainer);
                        sequenceFlow.getExtensionElements().remove("EDITOR_RESOURCEID");
                    }

                    ((FlowNode) sourceFlowElement).getOutgoingFlows().add(sequenceFlow);
                    JsonNode edgeNode = edgeMap.get(sequenceFlow.getId());
                    if (edgeNode != null) {
                        boolean isDefault = JsonConverterUtil.getPropertyValueAsBoolean(PROPERTY_SEQUENCEFLOW_DEFAULT,
                                                                                        edgeNode);
                        if (isDefault) {
                            if (sourceFlowElement instanceof Activity) {
                                ((Activity) sourceFlowElement).setDefaultFlow(sequenceFlow.getId());
                            } else if (sourceFlowElement instanceof Gateway) {
                                ((Gateway) sourceFlowElement).setDefaultFlow(sequenceFlow.getId());
                            }
                        }
                    }
                }
                FlowElement targetFlowElement = parentContainer.getFlowElement(sequenceFlow.getTargetRef());
                if (targetFlowElement != null && targetFlowElement instanceof FlowNode) {
                    ((FlowNode) targetFlowElement).getIncomingFlows().add(sequenceFlow);
                }
            }
        }
    }

    private Activity retrieveAttachedRefObject(String attachedToRefId,
                                               Collection<FlowElement> flowElementList) {
        Activity activity = null;
        if (StringUtils.isNotEmpty(attachedToRefId)) {
            for (FlowElement flowElement : flowElementList) {
                if (attachedToRefId.equals(flowElement.getId())) {
                    activity = (Activity) flowElement;
                    break;
                } else if (flowElement instanceof SubProcess) {
                    SubProcess subProcess = (SubProcess) flowElement;
                    Activity retrievedActivity = retrieveAttachedRefObject(attachedToRefId,
                                                                           subProcess.getFlowElements());
                    if (retrievedActivity != null) {
                        activity = retrievedActivity;
                        break;
                    }
                }
            }
        }
        return activity;
    }

    private void readShapeDI(JsonNode objectNode,
                             double parentX,
                             double parentY,
                             Map<String, JsonNode> shapeMap,
                             Map<String, JsonNode> sourceRefMap,
                             BpmnModel bpmnModel) {

        if (objectNode.get(EDITOR_CHILD_SHAPES) != null) {
            for (JsonNode jsonChildNode : objectNode.get(EDITOR_CHILD_SHAPES)) {

                String stencilId = BpmnJsonConverterUtil.getStencilId(jsonChildNode);
                if (!STENCIL_SEQUENCE_FLOW.equals(stencilId)) {

                    GraphicInfo graphicInfo = new GraphicInfo();

                    JsonNode boundsNode = jsonChildNode.get(EDITOR_BOUNDS);
                    ObjectNode upperLeftNode = (ObjectNode) boundsNode.get(EDITOR_BOUNDS_UPPER_LEFT);
                    graphicInfo.setX(upperLeftNode.get(EDITOR_BOUNDS_X).asDouble() + parentX);
                    graphicInfo.setY(upperLeftNode.get(EDITOR_BOUNDS_Y).asDouble() + parentY);

                    ObjectNode lowerRightNode = (ObjectNode) boundsNode.get(EDITOR_BOUNDS_LOWER_RIGHT);
                    graphicInfo.setWidth(lowerRightNode.get(EDITOR_BOUNDS_X).asDouble() - graphicInfo.getX() + parentX);
                    graphicInfo.setHeight(lowerRightNode.get(EDITOR_BOUNDS_Y).asDouble() - graphicInfo.getY() + parentY);

                    String childShapeId = jsonChildNode.get(EDITOR_SHAPE_ID).asText();
                    bpmnModel.addGraphicInfo(BpmnJsonConverterUtil.getElementId(jsonChildNode),
                                             graphicInfo);

                    shapeMap.put(childShapeId,
                                 jsonChildNode);

                    ArrayNode outgoingNode = (ArrayNode) jsonChildNode.get("outgoing");
                    if (outgoingNode != null && outgoingNode.size() > 0) {
                        for (JsonNode outgoingChildNode : outgoingNode) {
                            JsonNode resourceNode = outgoingChildNode.get(EDITOR_SHAPE_ID);
                            if (resourceNode != null) {
                                sourceRefMap.put(resourceNode.asText(),
                                                 jsonChildNode);
                            }
                        }
                    }

                    readShapeDI(jsonChildNode,
                                graphicInfo.getX(),
                                graphicInfo.getY(),
                                shapeMap,
                                sourceRefMap,
                                bpmnModel);
                }
            }
        }
    }

    private void filterAllEdges(JsonNode objectNode,
                                Map<String, JsonNode> edgeMap,
                                Map<String, List<JsonNode>> sourceAndTargetMap,
                                Map<String, JsonNode> shapeMap,
                                Map<String, JsonNode> sourceRefMap) {

        if (objectNode.get(EDITOR_CHILD_SHAPES) != null) {
            for (JsonNode jsonChildNode : objectNode.get(EDITOR_CHILD_SHAPES)) {

                ObjectNode childNode = (ObjectNode) jsonChildNode;
                String stencilId = BpmnJsonConverterUtil.getStencilId(childNode);
                if (STENCIL_SUB_PROCESS.equals(stencilId) || STENCIL_POOL.equals(stencilId) || STENCIL_LANE.equals(stencilId)) {
                    filterAllEdges(childNode,
                                   edgeMap,
                                   sourceAndTargetMap,
                                   shapeMap,
                                   sourceRefMap);
                } else if (STENCIL_SEQUENCE_FLOW.equals(stencilId) || STENCIL_ASSOCIATION.equals(stencilId)) {

                    String childEdgeId = BpmnJsonConverterUtil.getElementId(childNode);
                    JsonNode targetNode = childNode.get("target");
                    if (targetNode != null && !targetNode.isNull()) {
                        String targetRefId = targetNode.get(EDITOR_SHAPE_ID).asText();
                        List<JsonNode> sourceAndTargetList = new ArrayList<JsonNode>();
                        sourceAndTargetList.add(sourceRefMap.get(childNode.get(EDITOR_SHAPE_ID).asText()));
                        sourceAndTargetList.add(shapeMap.get(targetRefId));
                        sourceAndTargetMap.put(childEdgeId,
                                               sourceAndTargetList);
                    }
                    edgeMap.put(childEdgeId,
                                childNode);
                }
            }
        }
    }

    private void readEdgeDI(Map<String, JsonNode> edgeMap,
                            Map<String, List<JsonNode>> sourceAndTargetMap,
                            BpmnModel bpmnModel) {

        for (String edgeId : edgeMap.keySet()) {

            JsonNode edgeNode = edgeMap.get(edgeId);
            List<JsonNode> sourceAndTargetList = sourceAndTargetMap.get(edgeId);

            JsonNode sourceRefNode = null;
            JsonNode targetRefNode = null;

            if (sourceAndTargetList != null && sourceAndTargetList.size() > 1) {
                sourceRefNode = sourceAndTargetList.get(0);
                targetRefNode = sourceAndTargetList.get(1);
            }

            if (sourceRefNode == null) {
                LOGGER.info("Skipping edge {} because source ref is null",
                            edgeId);
                continue;
            }

            if (targetRefNode == null) {
                LOGGER.info("Skipping edge {} because target ref is null",
                            edgeId);
                continue;
            }

            JsonNode dockersNode = edgeNode.get(EDITOR_DOCKERS);
            double sourceDockersX = dockersNode.get(0).get(EDITOR_BOUNDS_X).asDouble();
            double sourceDockersY = dockersNode.get(0).get(EDITOR_BOUNDS_Y).asDouble();

            GraphicInfo sourceInfo = bpmnModel.getGraphicInfo(BpmnJsonConverterUtil.getElementId(sourceRefNode));
            GraphicInfo targetInfo = bpmnModel.getGraphicInfo(BpmnJsonConverterUtil.getElementId(targetRefNode));

            double sourceRefLineX = sourceInfo.getX() + sourceDockersX;
            double sourceRefLineY = sourceInfo.getY() + sourceDockersY;

            double nextPointInLineX;
            double nextPointInLineY;

            nextPointInLineX = dockersNode.get(1).get(EDITOR_BOUNDS_X).asDouble();
            nextPointInLineY = dockersNode.get(1).get(EDITOR_BOUNDS_Y).asDouble();
            if (dockersNode.size() == 2) {
                nextPointInLineX += targetInfo.getX();
                nextPointInLineY += targetInfo.getY();
            }

            Line2D firstLine = new Line2D(sourceRefLineX,
                                          sourceRefLineY,
                                          nextPointInLineX,
                                          nextPointInLineY);

            String sourceRefStencilId = BpmnJsonConverterUtil.getStencilId(sourceRefNode);
            String targetRefStencilId = BpmnJsonConverterUtil.getStencilId(targetRefNode);

            List<GraphicInfo> graphicInfoList = new ArrayList<GraphicInfo>();

            AbstractContinuousCurve2D source2D = null;
            if (DI_CIRCLES.contains(sourceRefStencilId)) {
                source2D = new Circle2D(sourceInfo.getX() + sourceDockersX,
                                        sourceInfo.getY() + sourceDockersY,
                                        sourceDockersX);
            } else if (DI_RECTANGLES.contains(sourceRefStencilId)) {
                source2D = createRectangle(sourceInfo);
            } else if (DI_GATEWAY.contains(sourceRefStencilId)) {
                source2D = createGateway(sourceInfo);
            }

            if (source2D != null) {
                Collection<Point2D> intersections = source2D.intersections(firstLine);
                if (intersections != null && intersections.size() > 0) {
                    Point2D intersection = intersections.iterator().next();
                    graphicInfoList.add(createGraphicInfo(intersection.x(),
                                                          intersection.y()));
                } else {
                    graphicInfoList.add(createGraphicInfo(sourceRefLineX,
                                                          sourceRefLineY));
                }
            }

            Line2D lastLine = null;

            if (dockersNode.size() > 2) {
                for (int i = 1; i < dockersNode.size() - 1; i++) {
                    double x = dockersNode.get(i).get(EDITOR_BOUNDS_X).asDouble();
                    double y = dockersNode.get(i).get(EDITOR_BOUNDS_Y).asDouble();
                    graphicInfoList.add(createGraphicInfo(x,
                                                          y));
                }

                double startLastLineX = dockersNode.get(dockersNode.size() - 2).get(EDITOR_BOUNDS_X).asDouble();
                double startLastLineY = dockersNode.get(dockersNode.size() - 2).get(EDITOR_BOUNDS_Y).asDouble();

                double endLastLineX = dockersNode.get(dockersNode.size() - 1).get(EDITOR_BOUNDS_X).asDouble();
                double endLastLineY = dockersNode.get(dockersNode.size() - 1).get(EDITOR_BOUNDS_Y).asDouble();

                endLastLineX += targetInfo.getX();
                endLastLineY += targetInfo.getY();

                lastLine = new Line2D(startLastLineX,
                                      startLastLineY,
                                      endLastLineX,
                                      endLastLineY);
            } else {
                lastLine = firstLine;
            }

            AbstractContinuousCurve2D target2D = null;
            if (DI_RECTANGLES.contains(targetRefStencilId)) {
                target2D = createRectangle(targetInfo);
            } else if (DI_CIRCLES.contains(targetRefStencilId)) {

                double targetDockersX = dockersNode.get(dockersNode.size() - 1).get(EDITOR_BOUNDS_X).asDouble();
                double targetDockersY = dockersNode.get(dockersNode.size() - 1).get(EDITOR_BOUNDS_Y).asDouble();

                target2D = new Circle2D(targetInfo.getX() + targetDockersX,
                                        targetInfo.getY() + targetDockersY,
                                        targetDockersX);
            } else if (DI_GATEWAY.contains(targetRefStencilId)) {
                target2D = createGateway(targetInfo);
            }

            if (target2D != null) {
                Collection<Point2D> intersections = target2D.intersections(lastLine);
                if (intersections != null && intersections.size() > 0) {
                    Point2D intersection = intersections.iterator().next();
                    graphicInfoList.add(createGraphicInfo(intersection.x(),
                                                          intersection.y()));
                } else {
                    graphicInfoList.add(createGraphicInfo(lastLine.getPoint2().x(),
                                                          lastLine.getPoint2().y()));
                }
            }

            bpmnModel.addFlowGraphicInfoList(edgeId,
                                             graphicInfoList);
        }
    }

    private Polyline2D createRectangle(GraphicInfo graphicInfo) {
        Polyline2D rectangle = new Polyline2D(new Point2D(graphicInfo.getX(),
                                                          graphicInfo.getY()),
                                              new Point2D(graphicInfo.getX() + graphicInfo.getWidth(),
                                                          graphicInfo.getY()),
                                              new Point2D(graphicInfo.getX() + graphicInfo.getWidth(),
                                                          graphicInfo.getY() + graphicInfo.getHeight()),
                                              new Point2D(graphicInfo.getX(),
                                                          graphicInfo.getY() + graphicInfo.getHeight()),
                                              new Point2D(graphicInfo.getX(),
                                                          graphicInfo.getY()));
        return rectangle;
    }

    private Polyline2D createGateway(GraphicInfo graphicInfo) {

        double middleX = graphicInfo.getX() + (graphicInfo.getWidth() / 2);
        double middleY = graphicInfo.getY() + (graphicInfo.getHeight() / 2);

        Polyline2D gatewayRectangle = new Polyline2D(new Point2D(graphicInfo.getX(),
                                                                 middleY),
                                                     new Point2D(middleX,
                                                                 graphicInfo.getY()),
                                                     new Point2D(graphicInfo.getX() + graphicInfo.getWidth(),
                                                                 middleY),
                                                     new Point2D(middleX,
                                                                 graphicInfo.getY() + graphicInfo.getHeight()),
                                                     new Point2D(graphicInfo.getX(),
                                                                 middleY));

        return gatewayRectangle;
    }

    private GraphicInfo createGraphicInfo(double x,
                                          double y) {
        GraphicInfo graphicInfo = new GraphicInfo();
        graphicInfo.setX(x);
        graphicInfo.setY(y);
        return graphicInfo;
    }

    class FlowWithContainer {

        protected SequenceFlow sequenceFlow;
        protected FlowElementsContainer flowContainer;

        public FlowWithContainer(SequenceFlow sequenceFlow,
                                 FlowElementsContainer flowContainer) {
            this.sequenceFlow = sequenceFlow;
            this.flowContainer = flowContainer;
        }

        public SequenceFlow getSequenceFlow() {
            return sequenceFlow;
        }

        public void setSequenceFlow(SequenceFlow sequenceFlow) {
            this.sequenceFlow = sequenceFlow;
        }

        public FlowElementsContainer getFlowContainer() {
            return flowContainer;
        }

        public void setFlowContainer(FlowElementsContainer flowContainer) {
            this.flowContainer = flowContainer;
        }
    }
}

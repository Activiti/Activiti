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
package com.activiti.web.rest.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.ErrorEventDefinition;
import org.activiti.bpmn.model.Event;
import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.GraphicInfo;
import org.activiti.bpmn.model.Lane;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.bpmn.model.Pool;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.TimerEventDefinition;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.domain.ServerConfig;
import com.activiti.service.activiti.ProcessDefinitionService;
import com.activiti.service.activiti.ProcessInstanceService;
import com.activiti.web.rest.client.modelinfo.InfoMapper;
import com.activiti.web.rest.client.modelinfo.bpmn.ServiceTaskInfoMapper;
import com.activiti.web.rest.client.modelinfo.bpmn.UserTaskInfoMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@RestController
public class DisplayJsonClientResource extends AbstractClientResource {

	private final Logger log = LoggerFactory.getLogger(DisplayJsonClientResource.class);

	@Autowired
	protected ProcessDefinitionService clientService;
	
	@Autowired
	protected ProcessInstanceService processInstanceService;

	protected ObjectMapper objectMapper = new ObjectMapper();
	protected List<String> eventElementTypes = new ArrayList<String>();
	protected Map<String, InfoMapper> propertyMappers = new HashMap<String, InfoMapper>();

	public DisplayJsonClientResource() {
		eventElementTypes.add("StartEvent");
		eventElementTypes.add("EndEvent");
		eventElementTypes.add("BoundaryEvent");
		eventElementTypes.add("IntermediateCatchEvent");
		eventElementTypes.add("ThrowEvent");

		propertyMappers.put("ServiceTask", new ServiceTaskInfoMapper());
		propertyMappers.put("UserTask", new UserTaskInfoMapper());
	}

	@RequestMapping(value = "/rest/activiti/process-definitions/{processDefinitionId}/model-json", method = RequestMethod.GET, produces = "application/json")
	public JsonNode getProcessDefinitionModelJSON(@PathVariable String processDefinitionId) {

		ServerConfig config = retrieveServerConfig();
		ObjectNode displayNode = objectMapper.createObjectNode();
		
		BpmnModel pojoModel = clientService.getProcessDefinitionModel(config, processDefinitionId);
		
		if (!pojoModel.getLocationMap().isEmpty()) {
			try {
				GraphicInfo diagramInfo = new GraphicInfo();
				processProcessElements(config, pojoModel, displayNode, diagramInfo, null, null);
	
				displayNode.put("diagramBeginX", diagramInfo.getX());
				displayNode.put("diagramBeginY", diagramInfo.getY());
				displayNode.put("diagramWidth", diagramInfo.getWidth());
				displayNode.put("diagramHeight", diagramInfo.getHeight());
	
			} catch (Exception e) {
				log.error("Error creating model JSON", e);
			}
		}

		return displayNode;
	}
	
	@RequestMapping(value = "/rest/activiti/process-instances/{processInstanceId}/model-json", method = RequestMethod.GET, produces = "application/json")
	public JsonNode getProcessInstanceModelJSON(@PathVariable String processInstanceId, @RequestParam(required=true) String processDefinitionId) {
		ObjectNode displayNode = objectMapper.createObjectNode();
		
		ServerConfig config = retrieveServerConfig();
		BpmnModel pojoModel = clientService.getProcessDefinitionModel(config, processDefinitionId);
		
		if (!pojoModel.getLocationMap().isEmpty()) {
			
			// Fetch process-instance activities
			List<String> completedActivityInstances = processInstanceService.getCompletedActivityInstancesAndProcessDefinitionId(config, processInstanceId);
			List<String> currentActivityinstances = processInstanceService.getCurrentActivityInstances(config, processInstanceId);
			
			// Gather completed flows
			List<String> completedFlows = gatherCompletedFlows(completedActivityInstances, currentActivityinstances, pojoModel);
			
			try {
				GraphicInfo diagramInfo = new GraphicInfo();
				Set<String> completedElements = new HashSet<String>(completedActivityInstances);
				completedElements.addAll(completedFlows);
				
				Set<String> currentElements = new HashSet<String>(currentActivityinstances);
				
				processProcessElements(config, pojoModel, displayNode, diagramInfo, completedElements, currentElements);
				
				displayNode.put("diagramBeginX", diagramInfo.getX());
				displayNode.put("diagramBeginY", diagramInfo.getY());
				displayNode.put("diagramWidth", diagramInfo.getWidth());
				displayNode.put("diagramHeight", diagramInfo.getHeight());
				
				if(completedActivityInstances != null) {
					ArrayNode completedActivities = displayNode.putArray("completedActivities");
					for(String completed : completedActivityInstances) {
						completedActivities.add(completed);
					}
				}
				
				if(currentActivityinstances != null) {
					ArrayNode currentActivities = displayNode.putArray("currentActivities");
					for(String current : currentActivityinstances) {
						currentActivities.add(current);
					}
				}
				
				if(completedFlows != null) {
					ArrayNode completedSequenceFlows = displayNode.putArray("completedSequenceFlows");
					for(String current : completedFlows) {
						completedSequenceFlows.add(current);
					}
				}
				
			} catch (Exception e) {
				log.error("Error creating model JSON", e);
			}
		}

		return displayNode;
	}

	protected List<String> gatherCompletedFlows(List<String> completedActivityInstances,
      List<String> currentActivityinstances, BpmnModel pojoModel) {
		
		List<String> completedFlows = new ArrayList<String>();
		List<String> activities = new ArrayList<String>(completedActivityInstances);
		activities.addAll(currentActivityinstances);
		
		// TODO: not a robust way of checking when parallel paths are active, should be revisited
		// Go over all activities and check if it's possible to match any outgoing paths against the activities
    for (FlowElement activity : pojoModel.getMainProcess().getFlowElements()) {
    	if(activity instanceof FlowNode) {
    		int index = activities.indexOf(activity.getId());
    		if (index >= 0 && index + 1 < activities.size()) {
    			List<SequenceFlow> outgoingFlows = ((FlowNode) activity).getOutgoingFlows();
    			for (SequenceFlow flow : outgoingFlows) {
    				String destinationFlowId = flow.getTargetRef();
    				if (destinationFlowId.equals(activities.get(index + 1))) {
    					completedFlows.add(flow.getId());
    				}
    			}
    		}
    	}
    }
	  return completedFlows;
  }

	protected void processProcessElements(ServerConfig config, BpmnModel pojoModel, ObjectNode displayNode, GraphicInfo diagramInfo, Set<String> completedElements, Set<String> currentElements) throws Exception {

		
		if (pojoModel.getLocationMap().isEmpty()) return;

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
			processElements(process.getFlowElements(), pojoModel, elementArray, flowArray, diagramInfo, completedElements, currentElements);
		}

		displayNode.put("elements", elementArray);
		displayNode.put("flows", flowArray);
	}

	protected void processElements(Collection<FlowElement> elementList,
			BpmnModel model, ArrayNode elementArray, ArrayNode flowArray,
			GraphicInfo diagramInfo, Set<String> completedElements, Set<String> currentElements) {

	
		for (FlowElement element : elementList) {

			ObjectNode elementNode = objectMapper.createObjectNode();
			if(completedElements != null) {
				elementNode.put("completed", completedElements.contains(element.getId()));
			}
			
			if(currentElements != null) {
				elementNode.put("current", currentElements.contains(element.getId()));
			}
			
			if (element instanceof SequenceFlow) {
				SequenceFlow flow = (SequenceFlow) element;
				elementNode.put("id", flow.getId());
				elementNode.put("type", "sequenceFlow");
				elementNode.put("sourceRef", flow.getSourceRef());
				elementNode.put("targetRef", flow.getTargetRef());
				
				List<GraphicInfo> flowInfo = model.getFlowLocationGraphicInfo(flow.getId());
				ArrayNode waypointArray = objectMapper.createArrayNode();
				for (GraphicInfo graphicInfo : flowInfo) {
					ObjectNode pointNode = objectMapper.createObjectNode();
					fillGraphicInfo(pointNode, graphicInfo, false);
					waypointArray.add(pointNode);
					fillDiagramInfo(graphicInfo, diagramInfo);
				}
				elementNode.put("waypoints", waypointArray);
				flowArray.add(elementNode);

			} else {

				elementNode.put("id", element.getId());
				elementNode.put("name", element.getName());

				if (element instanceof FlowNode) {
					FlowNode flowNode = (FlowNode) element;
					ArrayNode incomingFlows = objectMapper.createArrayNode();
					for (SequenceFlow flow : flowNode.getIncomingFlows()) {
						incomingFlows.add(flow.getId());
					}
					elementNode.put("incomingFlows", incomingFlows);
				}

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
					}
				}

				if (propertyMappers.containsKey(className)) {
					elementNode.put("properties", propertyMappers.get(className).map(element));
				}

				elementArray.add(elementNode);

				if (element instanceof SubProcess) {
					SubProcess subProcess = (SubProcess) element;
					processElements(subProcess.getFlowElements(), model, elementArray, flowArray, diagramInfo, currentElements, currentElements);
				}
			}
		}
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

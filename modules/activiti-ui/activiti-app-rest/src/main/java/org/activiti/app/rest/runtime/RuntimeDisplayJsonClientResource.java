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
package org.activiti.app.rest.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.activiti.app.security.SecurityUtils;
import org.activiti.app.service.editor.mapper.EventInfoMapper;
import org.activiti.app.service.editor.mapper.InfoMapper;
import org.activiti.app.service.editor.mapper.ReceiveTaskInfoMapper;
import org.activiti.app.service.editor.mapper.SequenceFlowInfoMapper;
import org.activiti.app.service.editor.mapper.ServiceTaskInfoMapper;
import org.activiti.app.service.editor.mapper.UserTaskInfoMapper;
import org.activiti.app.service.exception.BadRequestException;
import org.activiti.app.service.exception.InternalServerErrorException;
import org.activiti.app.service.exception.NotPermittedException;
import org.activiti.app.service.runtime.PermissionService;
import org.activiti.bpmn.model.Artifact;
import org.activiti.bpmn.model.Association;
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
import org.activiti.bpmn.model.TextAnnotation;
import org.activiti.bpmn.model.TimerEventDefinition;
import org.activiti.editor.language.json.converter.util.CollectionUtils;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.identity.User;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@RestController
public class RuntimeDisplayJsonClientResource {

  @Inject
  protected RepositoryService repositoryService;

  @Inject
  protected RuntimeService runtimeService;

  @Inject
  protected HistoryService historyService;

  @Inject
  protected ManagementService managementService;

  @Inject
  protected PermissionService permissionService;

  protected ObjectMapper objectMapper = new ObjectMapper();
  protected List<String> eventElementTypes = new ArrayList<String>();
  protected Map<String, InfoMapper> propertyMappers = new HashMap<String, InfoMapper>();

  public RuntimeDisplayJsonClientResource() {
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

  @RequestMapping(value = "/rest/process-instances/{processInstanceId}/model-json", method = RequestMethod.GET, produces = "application/json")
  public JsonNode getModelJSON(@PathVariable String processInstanceId) {

    User currentUser = SecurityUtils.getCurrentUserObject();
    if (!permissionService.hasReadPermissionOnProcessInstance(currentUser, processInstanceId)) {
      throw new NotPermittedException();
    }

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
    if (processInstance == null) {
      throw new BadRequestException("No process instance found with id " + processInstanceId);
    }

    BpmnModel pojoModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());

    if (pojoModel == null || pojoModel.getLocationMap().isEmpty()) {
      throw new InternalServerErrorException("Process definition could not be found with id " + processInstance.getProcessDefinitionId());
    }

    // Fetch process-instance activities
    List<HistoricActivityInstance> activityInstances = historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstanceId).list();

    Set<String> completedActivityInstances = new HashSet<String>();
    Set<String> currentElements = new HashSet<String>();
    if (CollectionUtils.isNotEmpty(activityInstances)) {
      for (HistoricActivityInstance activityInstance : activityInstances) {
        if (activityInstance.getEndTime() != null) {
          completedActivityInstances.add(activityInstance.getActivityId());
        } else {
          currentElements.add(activityInstance.getActivityId());
        }
      }
    }

    List<Job> jobs = managementService.createJobQuery().processInstanceId(processInstanceId).list();
    if (CollectionUtils.isNotEmpty(jobs)) {
      List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(processInstanceId).list();
      Map<String, Execution> executionMap = new HashMap<String, Execution>();
      for (Execution execution : executions) {
        executionMap.put(execution.getId(), execution);
      }

      for (Job job : jobs) {
        if (executionMap.containsKey(job.getExecutionId())) {
          currentElements.add(executionMap.get(job.getExecutionId()).getActivityId());
        }
      }
    }

    // Gather completed flows
    List<String> completedFlows = gatherCompletedFlows(completedActivityInstances, currentElements, pojoModel);

    Set<String> completedElements = new HashSet<String>(completedActivityInstances);
    completedElements.addAll(completedFlows);

    ObjectNode displayNode = processProcessElements(pojoModel, completedElements, currentElements);

    if (completedActivityInstances != null) {
      ArrayNode completedActivities = displayNode.putArray("completedActivities");
      for (String completed : completedActivityInstances) {
        completedActivities.add(completed);
      }
    }

    if (currentElements != null) {
      ArrayNode currentActivities = displayNode.putArray("currentActivities");
      for (String current : currentElements) {
        currentActivities.add(current);
      }
    }

    if (completedFlows != null) {
      ArrayNode completedSequenceFlows = displayNode.putArray("completedSequenceFlows");
      for (String current : completedFlows) {
        completedSequenceFlows.add(current);
      }
    }

    return displayNode;
  }

  @RequestMapping(value = "/rest/process-definitions/{processDefinitionId}/model-json", method = RequestMethod.GET, produces = "application/json")
  public JsonNode getModelJSONForProcessDefinition(@PathVariable String processDefinitionId) {

    BpmnModel pojoModel = repositoryService.getBpmnModel(processDefinitionId);

    if (pojoModel == null || pojoModel.getLocationMap().isEmpty()) {
      throw new InternalServerErrorException("Process definition could not be found with id " + processDefinitionId);
    }

    return processProcessElements(pojoModel, null, null);
  }

  @RequestMapping(value = "/rest/process-instances/history/{processInstanceId}/model-json", method = RequestMethod.GET, produces = "application/json")
  public JsonNode getModelHistoryJSON(@PathVariable String processInstanceId) {

    User currentUser = SecurityUtils.getCurrentUserObject();
    if (!permissionService.hasReadPermissionOnProcessInstance(currentUser, processInstanceId)) {
      throw new NotPermittedException();
    }

    HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
    if (processInstance == null) {
      throw new BadRequestException("No process instance found with id " + processInstanceId);
    }

    BpmnModel pojoModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());

    if (pojoModel == null || pojoModel.getLocationMap().isEmpty()) {
      throw new InternalServerErrorException("Process definition could not be found with id " + processInstance.getProcessDefinitionId());
    }

    // Fetch process-instance activities
    List<HistoricActivityInstance> activityInstances = historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstanceId).list();

    Set<String> completedActivityInstances = new HashSet<String>();
    Set<String> currentActivityinstances = new HashSet<String>();
    if (CollectionUtils.isNotEmpty(activityInstances)) {
      for (HistoricActivityInstance activityInstance : activityInstances) {
        if (activityInstance.getEndTime() != null) {
          completedActivityInstances.add(activityInstance.getActivityId());
        } else {
          currentActivityinstances.add(activityInstance.getActivityId());
        }
      }
    }

    ObjectNode displayNode = processProcessElements(pojoModel, completedActivityInstances, currentActivityinstances);
    return displayNode;
  }

  protected ObjectNode processProcessElements(BpmnModel pojoModel, Set<String> completedElements, Set<String> currentElements) {
    ObjectNode displayNode = objectMapper.createObjectNode();
    GraphicInfo diagramInfo = new GraphicInfo();

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
      processArtifacts(process.getArtifacts(), pojoModel, elementArray, flowArray, diagramInfo);
    }

    displayNode.put("elements", elementArray);
    displayNode.put("flows", flowArray);

    displayNode.put("diagramBeginX", diagramInfo.getX());
    displayNode.put("diagramBeginY", diagramInfo.getY());
    displayNode.put("diagramWidth", diagramInfo.getWidth());
    displayNode.put("diagramHeight", diagramInfo.getHeight());
    return displayNode;
  }

  protected void processElements(Collection<FlowElement> elementList, BpmnModel model, ArrayNode elementArray, ArrayNode flowArray, GraphicInfo diagramInfo, Set<String> completedElements,
      Set<String> currentElements) {

    for (FlowElement element : elementList) {

      ObjectNode elementNode = objectMapper.createObjectNode();
      if (completedElements != null) {
        elementNode.put("completed", completedElements.contains(element.getId()));
      }

      if (currentElements != null) {
        elementNode.put("current", currentElements.contains(element.getId()));
      }

      if (element instanceof SequenceFlow) {
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
          processElements(subProcess.getFlowElements(), model, elementArray, flowArray, diagramInfo, completedElements, currentElements);
          processArtifacts(subProcess.getArtifacts(), model, elementArray, flowArray, diagramInfo);
        }
      }
    }
  }

  protected void processArtifacts(Collection<Artifact> artifactList, BpmnModel model, ArrayNode elementArray, ArrayNode flowArray, GraphicInfo diagramInfo) {

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
    commonFillGraphicInfo(elementNode, graphicInfo.getX(), graphicInfo.getY(), graphicInfo.getWidth(), graphicInfo.getHeight(), includeWidthAndHeight);
  }

  protected void commonFillGraphicInfo(ObjectNode elementNode, double x, double y, double width, double height, boolean includeWidthAndHeight) {

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

  protected List<String> gatherCompletedFlows(Set<String> completedActivityInstances, Set<String> currentActivityinstances, BpmnModel pojoModel) {

    List<String> completedFlows = new ArrayList<String>();
    List<String> activities = new ArrayList<String>(completedActivityInstances);
    activities.addAll(currentActivityinstances);

    // TODO: not a robust way of checking when parallel paths are active, should be revisited
    // Go over all activities and check if it's possible to match any outgoing paths against the activities
    for (FlowElement activity : pojoModel.getMainProcess().getFlowElements()) {
      if (activity instanceof FlowNode) {
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
}

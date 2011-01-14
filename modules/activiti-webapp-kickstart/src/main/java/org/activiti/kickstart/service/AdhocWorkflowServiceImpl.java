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
package org.activiti.kickstart.service;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.kickstart.bpmn20.model.BaseElement;
import org.activiti.kickstart.bpmn20.model.Definitions;
import org.activiti.kickstart.bpmn20.model.FlowElement;
import org.activiti.kickstart.bpmn20.model.activity.resource.ActivityResource;
import org.activiti.kickstart.bpmn20.model.activity.resource.HumanPerformer;
import org.activiti.kickstart.bpmn20.model.activity.resource.PotentialOwner;
import org.activiti.kickstart.bpmn20.model.activity.type.UserTask;
import org.activiti.kickstart.bpmn20.model.connector.SequenceFlow;
import org.activiti.kickstart.bpmn20.model.gateway.ParallelGateway;
import org.activiti.kickstart.diagram.ProcessDiagramGenerator;
import org.activiti.kickstart.dto.AdhocWorkflowDto;
import org.activiti.kickstart.dto.AdhocWorkflowInfo;
import org.activiti.kickstart.dto.FormDto;
import org.activiti.kickstart.dto.TaskDto;

/**
 * @author Joram Barrez
 */
public class AdhocWorkflowServiceImpl implements AdhocWorkflowService {

  protected RepositoryService repositoryService;
  protected HistoryService historyService;

  public AdhocWorkflowServiceImpl(ProcessEngine processEngine) {
    this.repositoryService = processEngine.getRepositoryService();
    this.historyService = processEngine.getHistoryService();
  }

  public void deployAdhocWorkflow(AdhocWorkflowDto adhocWorkflow) throws JAXBException {
    String deploymentName = "Process " + adhocWorkflow.getName();
    String bpmn20XmlResourceName = generateBpmnResourceName(adhocWorkflow.getName());
    DeploymentBuilder deploymentBuilder = repositoryService.createDeployment().name(deploymentName);

    // png image (must go first, since it will add DI to the process xml)
    ProcessDiagramGenerator converter = new ProcessDiagramGenerator(adhocWorkflow);
    deploymentBuilder.addInputStream(bpmn20XmlResourceName.replace(".bpmn20.xml", ".png"), converter.execute());

    // bpmn 2.0 xml
    deploymentBuilder.addString(bpmn20XmlResourceName, createBpmn20Xml(adhocWorkflow));

    // forms
    for (TaskDto task : adhocWorkflow.getTasks()) {
      FormDto form = task.getForm();
      if (form != null) {
        deploymentBuilder.addString(task.generateDefaultFormName(), form.toHtml());
        deploymentBuilder.addString(task.generateDefaultFormName() + ".internal", form.toString());
      }
    }

    // deploy the whole package
    deploymentBuilder.deploy();
  }

  public List<AdhocWorkflowInfo> findAdhocWorkflowInformation() {
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
      .processDefinitionKeyLike("adhoc_%")
      .orderByProcessDefinitionName()
      .asc()
      .orderByProcessDefinitionVersion()
      .desc()
      .list();
    return convertToDto(processDefinitions);
  }

  protected List<AdhocWorkflowInfo> convertToDto(List<ProcessDefinition> processDefinitions) {
    List<AdhocWorkflowInfo> dtos = new ArrayList<AdhocWorkflowInfo>();
    for (ProcessDefinition processDefinition : processDefinitions) {
      AdhocWorkflowInfo dto = new AdhocWorkflowInfo();
      dto.setId(processDefinition.getId());
      dto.setKey(processDefinition.getKey());
      dto.setName(processDefinition.getName());
      dto.setVersion(processDefinition.getVersion());
      dto.setDeploymentId(processDefinition.getDeploymentId());

      Date deploymentTime = repositoryService.createDeploymentQuery()
        .deploymentId(processDefinition.getDeploymentId())
        .singleResult()
        .getDeploymentTime();
      dto.setCreateTime(deploymentTime);

      dto.setNrOfRuntimeInstances(historyService.createHistoricProcessInstanceQuery()
              .processDefinitionId(processDefinition.getId())
              .unfinished()
              .count());
      dto.setNrOfHistoricInstances(historyService.createHistoricProcessInstanceQuery()
              .processDefinitionId(processDefinition.getId())
              .finished()
              .count());

      dtos.add(dto);
    }
    return dtos;
  }

  public AdhocWorkflowDto findAdhocWorkflowById(String id) throws JAXBException {
    // Get process definition for key
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
      .processDefinitionId(id)
      .singleResult();

    // Get BPMN 2.0 XML file from database and parse it with JAXB
    InputStream is = null;
    Definitions definitions = null;
    try {
      is = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(), 
              processDefinition.getResourceName());

      JAXBContext jc = JAXBContext.newInstance(Definitions.class);
      Unmarshaller um = jc.createUnmarshaller();
      definitions = (Definitions) um.unmarshal(is);
    } finally {
      IoUtil.closeSilently(is);
    }

    // Convert JAXB to internal model
    return convertToAdhocWorkflow(processDefinition.getDeploymentId(), definitions);
  }

  public InputStream getProcessImage(String processDefinitionId) {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
      .processDefinitionId(processDefinitionId)
      .singleResult();
    return repositoryService.getResourceAsStream(processDefinition.getDeploymentId(), 
            processDefinition.getDiagramResourceName());
  };

  public InputStream getProcessBpmnXml(String processDefinitionId) {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
      .processDefinitionId(processDefinitionId)
      .singleResult();
    return repositoryService.getResourceAsStream(processDefinition.getDeploymentId(), 
            processDefinition.getResourceName());
  }

  // Helpers ////////////////////////////////////////////////////////

  protected String generateBpmnResourceName(String processName) {
    return processName.replace(" ", "_") + ".bpmn20.xml";
  }

  protected String createBpmn20Xml(AdhocWorkflowDto adhocWorkflow) throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(Definitions.class);
    Marshaller marshaller = jaxbContext.createMarshaller();
    StringWriter writer = new StringWriter();
    marshaller.marshal(adhocWorkflow.toBpmn20Xml(), writer);
    return writer.toString();
  }

  public AdhocWorkflowDto convertToAdhocWorkflow(String deploymentId, Definitions definitions) {
    AdhocWorkflowDto adhocWorkflow = new AdhocWorkflowDto();

    for (BaseElement baseElement : definitions.getRootElement()) {
      if (baseElement instanceof org.activiti.kickstart.bpmn20.model.Process) {

        org.activiti.kickstart.bpmn20.model.Process process = (org.activiti.kickstart.bpmn20.model.Process) baseElement;

        // Process name and description
        adhocWorkflow.setName(process.getName());
        if (!process.getDocumentation().isEmpty()) {
          adhocWorkflow.setDescription(process.getDocumentation().get(0).getText());
        }

        // user tasks
        Map<String, UserTask> userTasks = new HashMap<String, UserTask>();
        Map<String, ParallelGateway> gateways = new HashMap<String, ParallelGateway>();
        Map<String, List<SequenceFlow>> sequenceFlows = new HashMap<String, List<SequenceFlow>>();
        for (FlowElement flowElement : process.getFlowElement()) {
          if (flowElement instanceof UserTask) {
            userTasks.put(flowElement.getId(), (UserTask) flowElement);
          }
          if (flowElement instanceof ParallelGateway) {
            gateways.put(flowElement.getId(), (ParallelGateway) flowElement);
          }
          if (flowElement instanceof SequenceFlow) {
            SequenceFlow sequenceFlow = (SequenceFlow) flowElement;
            String sourceRef = sequenceFlow.getSourceRef().getId();
            if (!sequenceFlows.containsKey(sourceRef)) {
              sequenceFlows.put(sourceRef, new ArrayList<SequenceFlow>());
            }
            sequenceFlows.get(sourceRef).add(sequenceFlow);
          }
        }

        // Follow sequence flow to discover sequence of tasks
        SequenceFlow currentSequenceFlow = sequenceFlows.get(AdhocWorkflowDto.START_NAME).get(0); // Can be only one
        while (!currentSequenceFlow.getTargetRef().getId().equals(AdhocWorkflowDto.END_NAME)) {

          String targetRef = currentSequenceFlow.getTargetRef().getId();
          TaskDto taskDto = null;
          if (userTasks.containsKey(targetRef)) {

            taskDto = convertUserTaskToTaskDto(deploymentId, (UserTask) currentSequenceFlow.getTargetRef());
            currentSequenceFlow = sequenceFlows.get(currentSequenceFlow.getTargetRef().getId()).get(0); // Can be only one
            adhocWorkflow.addTask(taskDto);

          } else if (gateways.containsKey(targetRef)) {

            UserTask userTask = null;
            for (int i = 0; i < sequenceFlows.get(targetRef).size(); i++) {
              SequenceFlow seqFlowOutOfGateway = sequenceFlows.get(targetRef).get(i);
              userTask = (UserTask) seqFlowOutOfGateway.getTargetRef();
              taskDto = convertUserTaskToTaskDto(deploymentId, userTask);
              if (i > 0) {
                taskDto.setStartWithPrevious(true);
              }
              adhocWorkflow.addTask(taskDto);
            }

            String parallelJoinId = sequenceFlows.get(userTask.getId()).get(0).getTargetRef().getId(); // any seqflow is ok
            currentSequenceFlow = sequenceFlows.get(parallelJoinId).get(0); // can be only one

          }

        } // end while

      }
    }

    return adhocWorkflow;
  }

  protected TaskDto convertUserTaskToTaskDto(String deploymentId, UserTask userTask) {
    TaskDto task = new TaskDto();

    // task name
    task.setName(userTask.getName());

    // task description
    if (!userTask.getDocumentation().isEmpty()) {
      task.setDescription(userTask.getDocumentation().get(0).getText());
    }

    // Assignment
    for (ActivityResource activityResource : userTask.getActivityResource()) {
     if (activityResource instanceof PotentialOwner) {
        PotentialOwner potentialOwner = (PotentialOwner) activityResource;
        List<String> content = potentialOwner.getResourceAssignmentExpression().getExpression().getContent();
        if (!content.isEmpty()) {
          task.setGroups(content.get(0));
        }
      } else if (activityResource instanceof HumanPerformer) {
        HumanPerformer humanPerformer = (HumanPerformer) activityResource;
        List<String> content = humanPerformer.getResourceAssignmentExpression().getExpression().getContent();
        if (!content.isEmpty()) {
          task.setAssignee(content.get(0));
        }
      }
    }
    
    // Task form
    if (userTask.getFormKey() != null) {
      InputStream is = repositoryService.getResourceAsStream(deploymentId, userTask.getFormKey() + ".internal");
      String serializedForm = new String(IoUtil.readInputStream(is, ""));
      IoUtil.closeSilently(is);
      task.setForm(FormDto.createFromSerialized(serializedForm));
    }

    return task;
  }

}
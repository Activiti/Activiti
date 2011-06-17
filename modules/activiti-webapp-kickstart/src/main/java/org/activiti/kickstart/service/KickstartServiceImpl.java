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
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.kickstart.bpmn20.model.BaseElement;
import org.activiti.kickstart.bpmn20.model.Definitions;
import org.activiti.kickstart.bpmn20.model.FlowElement;
import org.activiti.kickstart.bpmn20.model.activity.Task;
import org.activiti.kickstart.bpmn20.model.activity.resource.ActivityResource;
import org.activiti.kickstart.bpmn20.model.activity.resource.HumanPerformer;
import org.activiti.kickstart.bpmn20.model.activity.resource.PotentialOwner;
import org.activiti.kickstart.bpmn20.model.activity.type.ScriptTask;
import org.activiti.kickstart.bpmn20.model.activity.type.ServiceTask;
import org.activiti.kickstart.bpmn20.model.activity.type.UserTask;
import org.activiti.kickstart.bpmn20.model.connector.SequenceFlow;
import org.activiti.kickstart.bpmn20.model.extension.AbstractExtensionElement;
import org.activiti.kickstart.bpmn20.model.extension.activiti.ActivitFieldExtensionElement;
import org.activiti.kickstart.bpmn20.model.extension.activiti.ActivitiFormProperty;
import org.activiti.kickstart.bpmn20.model.gateway.ParallelGateway;
import org.activiti.kickstart.diagram.ProcessDiagramGenerator;
import org.activiti.kickstart.dto.BaseTaskDto;
import org.activiti.kickstart.dto.FormDto;
import org.activiti.kickstart.dto.FormPropertyDto;
import org.activiti.kickstart.dto.KickstartWorkflowDto;
import org.activiti.kickstart.dto.KickstartWorkflowInfo;
import org.activiti.kickstart.dto.MailTaskDto;
import org.activiti.kickstart.dto.ScriptTaskDto;
import org.activiti.kickstart.dto.ServiceTaskDto;
import org.activiti.kickstart.dto.UserTaskDto;

/**
 * @author Joram Barrez
 */
public class KickstartServiceImpl implements KickstartService {

  protected RepositoryService repositoryService;
  protected HistoryService historyService;

  public KickstartServiceImpl(ProcessEngine processEngine) {
    this.repositoryService = processEngine.getRepositoryService();
    this.historyService = processEngine.getHistoryService();
  }

  public String deployKickstartWorkflow(KickstartWorkflowDto adhocWorkflow) throws JAXBException {
    String deploymentName = "Process " + adhocWorkflow.getName();
    String bpmn20XmlResourceName = generateBpmnResourceName(adhocWorkflow.getName());
    DeploymentBuilder deploymentBuilder = repositoryService.createDeployment().name(deploymentName);

    // png image (must go first, since it will add DI to the process xml)
    ProcessDiagramGenerator converter = new ProcessDiagramGenerator(adhocWorkflow);
    deploymentBuilder.addInputStream(bpmn20XmlResourceName.replace(".bpmn20.xml", ".png"), converter.execute());

    // bpmn 2.0 xml
    deploymentBuilder.addString(bpmn20XmlResourceName, createBpmn20Xml(adhocWorkflow));

    // deploy the whole package
    Deployment deployment = deploymentBuilder.deploy();
    return deployment.getId();
  }

  public List<KickstartWorkflowInfo> findKickstartWorkflowInformation() {
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
    .processDefinitionKeyLike("adhoc_%")
    .orderByProcessDefinitionName()
    .asc()
    .orderByProcessDefinitionVersion()
    .desc()
    .list();
    return convertToDto(processDefinitions);
  }

  protected List<KickstartWorkflowInfo> convertToDto(List<ProcessDefinition> processDefinitions) {
    List<KickstartWorkflowInfo> dtos = new ArrayList<KickstartWorkflowInfo>();
    for (ProcessDefinition processDefinition : processDefinitions) {
      KickstartWorkflowInfo dto = new KickstartWorkflowInfo();
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

  public KickstartWorkflowDto findKickstartWorkflowById(String id) throws JAXBException {
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

  protected String createBpmn20Xml(KickstartWorkflowDto adhocWorkflow) throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(Definitions.class);
    Marshaller marshaller = jaxbContext.createMarshaller();
    StringWriter writer = new StringWriter();
    marshaller.marshal(adhocWorkflow.toBpmn20Xml(), writer);
    return writer.toString();
  }

  public KickstartWorkflowDto convertToAdhocWorkflow(String deploymentId, Definitions definitions) {
    KickstartWorkflowDto adhocWorkflow = new KickstartWorkflowDto();

    for (BaseElement baseElement : definitions.getRootElement()) {
      if (baseElement instanceof org.activiti.kickstart.bpmn20.model.Process) {

        org.activiti.kickstart.bpmn20.model.Process process = (org.activiti.kickstart.bpmn20.model.Process) baseElement;

        // Process name and description
        adhocWorkflow.setName(process.getName());
        if (!process.getDocumentation().isEmpty()) {
          adhocWorkflow.setDescription(process.getDocumentation().get(0).getText());
        }

        // user tasks
        Map<String, Task> tasks = new HashMap<String, Task>();
        Map<String, ParallelGateway> gateways = new HashMap<String, ParallelGateway>();
        Map<String, List<SequenceFlow>> sequenceFlows = new HashMap<String, List<SequenceFlow>>();
        for (FlowElement flowElement : process.getFlowElement()) {
          if (flowElement instanceof Task) {
            tasks.put(flowElement.getId(), (Task) flowElement);
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
        SequenceFlow currentSequenceFlow = sequenceFlows.get(KickstartWorkflowDto.START_NAME).get(0); // Can be only one
        while (!currentSequenceFlow.getTargetRef().getId().equals(KickstartWorkflowDto.END_NAME)) {

          String targetRef = currentSequenceFlow.getTargetRef().getId();
          BaseTaskDto taskDto = null;
          if (tasks.containsKey(targetRef)) {

            taskDto = convertTaskToTaskDto(deploymentId, (Task) currentSequenceFlow.getTargetRef());
            currentSequenceFlow = sequenceFlows.get(currentSequenceFlow.getTargetRef().getId()).get(0); // Can be only one
            adhocWorkflow.addTask(taskDto);

          } else if (gateways.containsKey(targetRef)) {

            Task task = null;
            for (int i = 0; i < sequenceFlows.get(targetRef).size(); i++) {
              SequenceFlow seqFlowOutOfGateway = sequenceFlows.get(targetRef).get(i);
              task = (Task) seqFlowOutOfGateway.getTargetRef();
              taskDto = convertTaskToTaskDto(deploymentId, task);
              if (i > 0) {
                taskDto.setStartWithPrevious(true);
              }
              adhocWorkflow.addTask(taskDto);
            }

            String parallelJoinId = sequenceFlows.get(task.getId()).get(0).getTargetRef().getId(); // any seqflow is ok
            currentSequenceFlow = sequenceFlows.get(parallelJoinId).get(0); // can be only one

          }

        } // end while

      }
    }

    return adhocWorkflow;
  }

  protected BaseTaskDto convertTaskToTaskDto(final String deploymentId, final Task task) {
    BaseTaskDto baseTaskDto = null;

    if (task instanceof UserTask) {
      baseTaskDto = new UserTaskDto();
      convertUserTaskToTaskDto(deploymentId, (UserTaskDto) baseTaskDto, (UserTask) task);
    } else if (task instanceof ServiceTask) {
      if (((ServiceTask)task).getType() != null
              && ((ServiceTask)task).getType().equals("mail")) {
        baseTaskDto = new MailTaskDto();
        convertMailTaskToTaskDto(deploymentId, (MailTaskDto) baseTaskDto, (ServiceTask) task);
      } else {
        baseTaskDto = new ServiceTaskDto();
        convertServiceTaskToTaskDto(deploymentId, (ServiceTaskDto) baseTaskDto, (ServiceTask) task);
      }
    } else if (task instanceof ScriptTask) {
      baseTaskDto = new ScriptTaskDto();
      convertScriptTaskToTaskDto(deploymentId, (ScriptTaskDto) baseTaskDto, (ScriptTask) task);
    }

    handleBaseTaskDtoProperties(baseTaskDto, task);

    return baseTaskDto;
  }

  private void handleBaseTaskDtoProperties(BaseTaskDto baseTaskDto, Task task) {
    // task id
    baseTaskDto.setId(task.getId());

    // task name
    baseTaskDto.setName(task.getName());

    // task description
    if (!task.getDocumentation().isEmpty()) {
      baseTaskDto.setDescription(task.getDocumentation().get(0).getText());
    }
  }

  protected void convertUserTaskToTaskDto(final String deploymentId, UserTaskDto task, final UserTask userTask) {
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
    List<FormPropertyDto> formPropertyDtos = new ArrayList<FormPropertyDto>();
    if (userTask.getExtensionElements() != null) {
      for (AbstractExtensionElement extensionElement : userTask.getExtensionElements().getAllElementOfType(ActivitiFormProperty.class)) {
        ActivitiFormProperty formProperty = (ActivitiFormProperty) extensionElement;
        FormPropertyDto formPropertyDto = new FormPropertyDto();
        formPropertyDto.setProperty(formProperty.getName());
        
        String formType = formProperty.getType();
        String type = "text";
        if ("date".equals(formType)) {
          type = "date";
        } else if ("long".equals(formType)) {
          type = "number";
        }
        formPropertyDto.setType(type);
        formPropertyDto.setRequired("true".equals(formProperty.getRequired()));
        formPropertyDtos.add(formPropertyDto);
      }
    }
    
    if (formPropertyDtos.size() > 0) {
      FormDto formDto = new FormDto();
      formDto.setFormProperties(formPropertyDtos);
      task.setForm(formDto);
    }
  }

  protected void convertServiceTaskToTaskDto(final String deploymentId, ServiceTaskDto task, final ServiceTask serviceTask) {

    task.setClassName(serviceTask.getClassName());
    task.setExpression(serviceTask.getExpression());
    task.setDelegateExpression(serviceTask.getDelegateExpression());
  }

  protected void convertMailTaskToTaskDto(final String deploymentId, MailTaskDto task, final ServiceTask serviceTask) {

    List<AbstractExtensionElement> extensionElements = serviceTask.getExtensionElements().getAny();
    for (AbstractExtensionElement abstractExtensionElement : extensionElements) {
      ActivitFieldExtensionElement field = (ActivitFieldExtensionElement) abstractExtensionElement;
      String fieldName = field.getName();
      if (fieldName.equals("to")) {
        task.getTo().setStringValue(field.getStringValue());
        task.getTo().setExpression(field.getExpression());
      } else if (fieldName.equals("from")) {
        task.getFrom().setStringValue(field.getStringValue());
        task.getFrom().setExpression(field.getExpression());
      } else if (fieldName.equals("subject")) {
        task.getSubject().setStringValue(field.getStringValue());
        task.getSubject().setExpression(field.getExpression());
      } else if (fieldName.equals("cc")) {
        task.getCc().setStringValue(field.getStringValue());
        task.getCc().setExpression(field.getExpression());
      } else if (fieldName.equals("bcc")) {
        task.getBcc().setStringValue(field.getStringValue());
        task.getBcc().setExpression(field.getExpression());
      } else if (fieldName.equals("html")) {
        task.getHtml().setStringValue(field.getStringValue());
        task.getHtml().setExpression(field.getExpression());
      } else if (fieldName.equals("text")) {
        task.getText().setStringValue(field.getStringValue());
        task.getText().setExpression(field.getExpression());
      }
    }
  }

  protected void convertScriptTaskToTaskDto(final String deploymentId, ScriptTaskDto task, final ScriptTask serviceTask) {
    task.setScriptFormat(serviceTask.getScriptFormat());
    task.setResultVariableName(serviceTask.getResultVariableName());
    task.setScript(serviceTask.getScript());
  }

}
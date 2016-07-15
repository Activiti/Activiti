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
package com.activiti.service.runtime;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.ExtensionElement;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.UserTask;
import org.activiti.editor.form.converter.FormJsonConverter;
import org.activiti.editor.language.json.converter.util.CollectionUtils;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.runtime.Clock;
import org.activiti.form.engine.FormRepositoryService;
import org.activiti.form.engine.repository.FormDeploymentBuilder;
import org.activiti.form.model.FormDefinition;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.activiti.domain.editor.AbstractModel;
import com.activiti.domain.runtime.RuntimeAppDefinition;
import com.activiti.domain.runtime.RuntimeAppDeployment;
import com.activiti.service.api.AppDefinitionService;
import com.activiti.service.api.AppDefinitionServiceRepresentation;
import com.activiti.service.api.DeploymentResult;
import com.activiti.service.api.DeploymentService;
import com.activiti.service.api.ModelService;
import com.activiti.service.exception.BadRequestException;
import com.activiti.service.exception.NotPermittedException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Tijs Rademakers
 * @author Joram Barrez
 */
@Service
public class DeploymentServiceImpl implements DeploymentService {

  private static final Logger logger = LoggerFactory.getLogger(DeploymentServiceImpl.class);

  @Autowired
  protected RuntimeAppDefinitionInternalService runtimeAppDefinitionService;

  @Autowired
  protected AppDefinitionService appDefinitionService;

  @Autowired
  protected ModelService modelService;

  @Autowired
  protected RepositoryService repositoryService;

  @Autowired
  protected IdentityService identityService;

  @Autowired
  protected FormRepositoryService formRepositoryService;

  @Autowired
  protected Clock clock;

  @Autowired
  protected ObjectMapper objectMapper;
  
  protected FormJsonConverter formJsonConverter = new FormJsonConverter();

  @Override
  @Transactional
  public void deployAppDefinitions(List<Long> appDefinitions, User user) {

    // Check if user is allowed to use app definition
    List<AppDefinitionServiceRepresentation> availableAppDefinitions = appDefinitionService.getDeployableAppDefinitions(user);
    Map<Long, AppDefinitionServiceRepresentation> availableIdMap = new HashMap<Long, AppDefinitionServiceRepresentation>();
    for (AppDefinitionServiceRepresentation appDef : availableAppDefinitions) {
      availableIdMap.put(appDef.getId(), appDef);
    }

    for (Long appDefId : appDefinitions) {
      if (availableIdMap.containsKey(appDefId) == false) {
        logger.error("App definition " + appDefId + " is not allowed for user " + user.getId());
        throw new NotPermittedException("One of the app definitions is not allowed");
      }
    }

    // Deploy each app definition
    for (Long appDefId : appDefinitions) {
      AppDefinitionServiceRepresentation appDefinition = availableIdMap.get(appDefId);
      RuntimeAppDefinition runtimeAppDefinition = runtimeAppDefinitionService.getRuntimeAppDefinitionForModel(appDefinition.getId());
      if (runtimeAppDefinition == null) {

        runtimeAppDefinition = runtimeAppDefinitionService.createRuntimeAppDefinition(user, appDefinition.getName(), appDefinition.getDescription(), appDefinition.getId(),
            appDefinition.getDefinition());

        deployAppDefinitionToActiviti(appDefinition, runtimeAppDefinition, user);
      }

      // Regardless if we created the definition or not, create a RuntimeApp to connect the user
      runtimeAppDefinitionService.addAppDefinitionForUser(user, runtimeAppDefinition);
    }
  }

  @Override
  @Transactional
  public DeploymentResult updateAppDefinition(Long appDefinitionId, String appDefinitionName, String appDefinitionDescription, String appDefinitionJson, User user) {

    // Get all app models that the user can deploy
    List<AppDefinitionServiceRepresentation> availableAppDefinitions = appDefinitionService.getDeployableAppDefinitions(user);

    // Create a map {id, appModel} of the above list
    Map<Long, AppDefinitionServiceRepresentation> availableIdMap = new HashMap<Long, AppDefinitionServiceRepresentation>();
    for (AppDefinitionServiceRepresentation appDef : availableAppDefinitions) {
      availableIdMap.put(appDef.getId(), appDef);
    }

    // Check if the id provided in the method is part of this map, if not the app is not deployable by the user
    if (availableIdMap.containsKey(appDefinitionId) == false) {
      logger.error("App definition " + appDefinitionId + " is not allowed for user " + user.getId());
      throw new NotPermittedException("App definition update is not allowed for this user");
    }

    RuntimeAppDefinition runtimeAppDefinition = runtimeAppDefinitionService.getRuntimeAppDefinitionForModel(appDefinitionId);
    if (runtimeAppDefinition != null) {

      // App definition is already deployed. Update the values and deploy the related processes
      runtimeAppDefinition.setName(appDefinitionName);
      runtimeAppDefinition.setDescription(appDefinitionDescription);
      runtimeAppDefinition.setDefinition(appDefinitionJson);

    } else {

      // No app definition deployed yet. Create the first one
      runtimeAppDefinition = runtimeAppDefinitionService.createRuntimeAppDefinition(user, appDefinitionName, appDefinitionDescription, appDefinitionId, appDefinitionJson);

    }

    // Now we can deploy the processes related to the app
    return deployAppDefinitionToActiviti(availableIdMap.get(appDefinitionId), runtimeAppDefinition, user);
  }

  @Override
  @Transactional
  public void deleteAppDefinition(Long appDefinitionId) {
    RuntimeAppDefinition appDefinition = runtimeAppDefinitionService.getRuntimeAppDefinition(appDefinitionId);
    if (appDefinition != null) {
      List<RuntimeAppDeployment> appDeployments = runtimeAppDefinitionService.getRuntimeAppDeploymentsForApp(appDefinition);
      if (CollectionUtils.isNotEmpty(appDeployments)) {
        for (RuntimeAppDeployment runtimeAppDeployment : appDeployments) {
          String deploymentId = runtimeAppDeployment.getDeploymentId();
          if (deploymentId != null) {
            // First test if deployment is still there, otherwhise the transaction will be rolled back
            Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(deploymentId).singleResult();
            if (deployment != null) {
              repositoryService.deleteDeployment(deploymentId, true);
            }
          }
        }
      }
      runtimeAppDefinitionService.deleteAppDefinition(appDefinition);
    }
  }

  protected DeploymentResult deployAppDefinitionToActiviti(AppDefinitionServiceRepresentation appDefinition, RuntimeAppDefinition runtimeAppDefinition, User user) {

    DeploymentResult deploymentResult = new DeploymentResult();

    RuntimeAppDeployment appDeployment = runtimeAppDefinitionService.createRuntimeAppDeployment(user, runtimeAppDefinition, appDefinition.getId(), appDefinition.getDefinition());

    if (CollectionUtils.isNotEmpty(appDefinition.getModels())) {
      DeploymentBuilder deploymentBuilder = repositoryService.createDeployment().name(appDefinition.getName());

      for (Long modelId : appDefinition.getModels()) {

        AbstractModel processModel = modelService.getModel(modelId);
        if (processModel == null) {
          logger.error("Model " + modelId + " for app definition " + appDefinition.getId() + " could not be found");
          throw new BadRequestException("Model for app definition could not be found");
        }

        BpmnModel bpmnModel = modelService.getBpmnModel(processModel, user, false);
        Map<String, StartEvent> startEventMap = processNoneStartEvents(bpmnModel);
        Map<Long, FormDefinition> formMap = overrideBpmnForms(bpmnModel, startEventMap, processModel);

        if (formMap != null) {
          for (Long formModelId : formMap.keySet()) {
            deploymentResult.addFormModelMapping(formModelId, formMap.get(formModelId));
          }
        }

        byte[] modelXML = modelService.getBpmnXML(bpmnModel);
        deploymentBuilder.addInputStream(processModel.getName().toLowerCase().replaceAll(" ", "") + ".bpmn", new ByteArrayInputStream(modelXML));

        deploymentResult.addModelMapping(processModel, bpmnModel);
      }

      Deployment deployment = deploymentBuilder.deploy();
      deploymentResult.setDeployment(deployment);
      if (deployment instanceof DeploymentEntity) {
        List<ProcessDefinitionEntity> processDefinitions = ((DeploymentEntity) deployment).getDeployedArtifacts(ProcessDefinitionEntity.class);
        for (ProcessDefinitionEntity processDefinitionEntity : processDefinitions) {
          deploymentResult.addProcessDefinition(processDefinitionEntity);
        }
      }

      runtimeAppDefinition.setDeploymentId(deployment.getId());
      appDeployment.setDeploymentId(deployment.getId());

      runtimeAppDefinitionService.updateRuntimeAppDeployment(appDeployment);
    }

    runtimeAppDefinitionService.updateRuntimeAppDefinition(runtimeAppDefinition);

    deploymentResult.setRuntimeAppDeployment(appDeployment);
    return deploymentResult;
  }

  protected Map<String, StartEvent> processNoneStartEvents(BpmnModel bpmnModel) {
    Map<String, StartEvent> startEventMap = new HashMap<String, StartEvent>();
    for (Process process : bpmnModel.getProcesses()) {
      for (FlowElement flowElement : process.getFlowElements()) {
        if (flowElement instanceof StartEvent) {
          StartEvent startEvent = (StartEvent) flowElement;
          if (CollectionUtils.isEmpty(startEvent.getEventDefinitions())) {
            if (StringUtils.isEmpty(startEvent.getInitiator())) {
              startEvent.setInitiator("initiator");
            }
            startEventMap.put(process.getId(), startEvent);
            break;
          }
        }
      }
    }
    return startEventMap;
  }

  protected Map<Long, FormDefinition> overrideBpmnForms(BpmnModel bpmnModel, Map<String, StartEvent> startEventMap, AbstractModel model) {
    Map<Long, FormDefinition> formMap = new HashMap<Long, FormDefinition>();
    for (Process process : bpmnModel.getProcesses()) {
      processBpmnForms(process.getFlowElements(), process, startEventMap, formMap, model);
    }
    
    if (formMap.size() > 0) {
      FormDeploymentBuilder formDeploymentBuilder = formRepositoryService.createDeployment();
      for (Long formId : formMap.keySet()) {
        FormDefinition formDefinition = formMap.get(formId);
        formDeploymentBuilder.addFormModel("form-" + formId + ".form", formDefinition);
      }
      formDeploymentBuilder.deploy();
    }
    
    return formMap;
  }

  protected void processBpmnForms(Collection<FlowElement> flowElements, Process process, Map<String, StartEvent> startEventMap, 
      Map<Long, FormDefinition> formIdMap, AbstractModel model) {

    for (FlowElement flowElement : flowElements) {
      if (flowElement instanceof UserTask) {
        UserTask userTask = (UserTask) flowElement;
        String finalFormKey = retrieveFinalFormKey(userTask.getFormKey(), userTask, formIdMap, model);
        if ("$INITIATOR".equals(userTask.getAssignee())) {
          if (startEventMap.get(process.getId()) != null) {
            userTask.setAssignee("${" + startEventMap.get(process.getId()).getInitiator() + "}");
          }
        }
        userTask.setFormKey(finalFormKey);

      } else if (flowElement instanceof StartEvent) {
        StartEvent startEvent = (StartEvent) flowElement;
        String finalFormKey = retrieveFinalFormKey(startEvent.getFormKey(), startEvent, formIdMap, model);
        startEvent.setFormKey(finalFormKey);

      } else if (flowElement instanceof SubProcess) {
        processBpmnForms(((SubProcess) flowElement).getFlowElements(), process, startEventMap, formIdMap, model);
      }
    }
  }

  protected String retrieveFinalFormKey(String formKey, FlowElement flowElement, Map<Long, FormDefinition> formIdMap, AbstractModel model) {
    String finalFormKey = null;
    List<ExtensionElement> formIdExtensions = flowElement.getExtensionElements().get("form-reference-id");
    List<ExtensionElement> formNameExtensions = flowElement.getExtensionElements().get("form-reference-name");
    if (CollectionUtils.isNotEmpty(formIdExtensions) && CollectionUtils.isNotEmpty(formNameExtensions)) {
      Long formId = Long.valueOf(formIdExtensions.get(0).getElementText());
      finalFormKey = getFormKeyWithFormId(formId, formIdMap, model);

    } else if (StringUtils.isNotEmpty(formKey) && formKey.startsWith("FORM_REFERENCE")) {
      String formIdValue = formKey.replace("FORM_REFERENCE", "");
      if (NumberUtils.isNumber(formIdValue)) {
        Long formId = Long.valueOf(formIdValue);
        finalFormKey = getFormKeyWithFormId(formId, formIdMap, model);
      }
    }

    if (StringUtils.isEmpty(finalFormKey)) {
      finalFormKey = formKey;
    }

    return finalFormKey;
  }

  protected String getFormKeyWithFormId(Long formId, Map<Long, FormDefinition> formIdMap, AbstractModel model) {
    String formKey = null;
    if (formIdMap.containsKey(formId) == false) {
      AbstractModel formModel = modelService.getModel(formId);
      Map<Long, AbstractModel> formModelMap = new HashMap<Long, AbstractModel>();
      if (formModel != null) {
        formModelMap.put(formModel.getId(), formModel);
      }
      FormDefinition formDefinition = createFormDefinition(formId, formModelMap, model);
      formDefinition.setName(formModel.getName());
      formDefinition.setDescription(formModel.getDescription());
      formIdMap.put(formId, formDefinition);
      formKey = formDefinition.getKey();

    } else {
      formKey = formIdMap.get(formId).getKey();
    }
    return formKey;
  }

  protected FormDefinition createFormDefinition(Long formId, Map<Long, AbstractModel> formMap, AbstractModel model) {
    if (formMap.containsKey(formId)) {
      AbstractModel form = formMap.get(formId);
      FormDefinition formDefinition = formJsonConverter.convertToForm(form.getModelEditorJson(), null, form.getVersion());
      return formDefinition;

    } else {
      logger.error("Form " + formId + " for model " + model.getId() + " could not be found");
      throw new BadRequestException("Form for model " + model.getName() + " could not be found");
    }
  }

  protected String getProcessName(Process process) {
    String name = null;
    if (StringUtils.isNotEmpty(process.getName())) {
      name = process.getName();
    } else {
      name = process.getId();
    }
    return name;
  }
}

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
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.runtime.Clock;
import org.activiti.form.api.FormRepositoryService;
import org.activiti.form.model.FormDefinition;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.activiti.domain.editor.AbstractModel;
import com.activiti.domain.editor.AppDefinition;
import com.activiti.domain.editor.AppModelDefinition;
import com.activiti.domain.editor.Model;
import com.activiti.service.api.AppDefinitionService;
import com.activiti.service.api.DeploymentService;
import com.activiti.service.api.ModelService;
import com.activiti.service.exception.BadRequestException;
import com.activiti.service.exception.InternalServerErrorException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Tijs Rademakers
 * @author Joram Barrez
 */
@Service
public class DeploymentServiceImpl implements DeploymentService {

  private static final Logger logger = LoggerFactory.getLogger(DeploymentServiceImpl.class);

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
  public Deployment updateAppDefinition(Model appDefinitionModel, User user) {
    Deployment deployment = null;
    AppDefinition appDefinition = resolveAppDefinition(appDefinitionModel);

    if (CollectionUtils.isNotEmpty(appDefinition.getModels())) {
      DeploymentBuilder deploymentBuilder = repositoryService.createDeployment()
          .name(appDefinitionModel.getName())
          .key(appDefinitionModel.getKey());

      for (AppModelDefinition appModelDef : appDefinition.getModels()) {

        AbstractModel processModel = modelService.getModel(appModelDef.getId());
        if (processModel == null) {
          logger.error("Model " + appModelDef.getId() + " for app definition " + appDefinitionModel.getId() + " could not be found");
          throw new BadRequestException("Model for app definition could not be found");
        }

        BpmnModel bpmnModel = modelService.getBpmnModel(processModel, false);
        Map<String, StartEvent> startEventMap = processNoneStartEvents(bpmnModel);
        Map<Long, FormInfoContainer> formMap = overrideBpmnForms(bpmnModel, startEventMap, processModel);

        if (formMap.size() > 0) {
          for (Long formId : formMap.keySet()) {
            FormInfoContainer formInfo = formMap.get(formId);
            deploymentBuilder.addString("form-" + formId + ".form", formInfo.getJson());
          }
        }
        
        byte[] modelXML = modelService.getBpmnXML(bpmnModel);
        deploymentBuilder.addInputStream(processModel.getName().toLowerCase().replaceAll(" ", "") + ".bpmn", new ByteArrayInputStream(modelXML));
      }

      deployment = deploymentBuilder.deploy();
    }

    return deployment;
  }

  @Override
  @Transactional
  public void deleteAppDefinition(Long appDefinitionId) {
    // First test if deployment is still there, otherwhise the transaction will be rolled back
    List<Deployment> deployments = repositoryService.createDeploymentQuery().deploymentKey(String.valueOf(appDefinitionId)).list();
    if (deployments != null) {
      for (Deployment deployment : deployments) {
        repositoryService.deleteDeployment(deployment.getId(), true);
      }
    }
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

  protected Map<Long, FormInfoContainer> overrideBpmnForms(BpmnModel bpmnModel, Map<String, StartEvent> startEventMap, AbstractModel model) {
    Map<Long, FormInfoContainer> formMap = new HashMap<Long, FormInfoContainer>();
    for (Process process : bpmnModel.getProcesses()) {
      processBpmnForms(process.getFlowElements(), process, startEventMap, formMap, model);
    }
    
    return formMap;
  }

  protected void processBpmnForms(Collection<FlowElement> flowElements, Process process, Map<String, StartEvent> startEventMap, 
      Map<Long, FormInfoContainer> formIdMap, AbstractModel model) {

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

  protected String retrieveFinalFormKey(String formKey, FlowElement flowElement, Map<Long, FormInfoContainer> formIdMap, AbstractModel model) {
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

  protected String getFormKeyWithFormId(Long formId, Map<Long, FormInfoContainer> formIdMap, AbstractModel model) {
    String formKey = null;
    if (formIdMap.containsKey(formId) == false) {
      AbstractModel formModel = modelService.getModel(formId);
      Map<Long, AbstractModel> formModelMap = new HashMap<Long, AbstractModel>();
      if (formModel != null) {
        formModelMap.put(formModel.getId(), formModel);
      }
      FormInfoContainer formInfoContainer = createFormInfoContainer(formId, formModelMap, model);
      formIdMap.put(formId, formInfoContainer);
      formKey = formInfoContainer.getKey();

    } else {
      formKey = formIdMap.get(formId).getKey();
    }
    return formKey;
  }
  
  protected AppDefinition resolveAppDefinition(Model appDefinitionModel) {
    try {
      AppDefinition appDefinition = objectMapper.readValue(appDefinitionModel.getModelEditorJson(), AppDefinition.class);
      return appDefinition;
      
    } catch (Exception e) {
      logger.error("Error deserializing app " + appDefinitionModel.getId(), e);
      throw new InternalServerErrorException("Could not deserialize app definition");
    }
  }
  
  protected FormInfoContainer createFormInfoContainer(Long formId, Map<Long, AbstractModel> formMap, AbstractModel model) {
    if (formMap.containsKey(formId)) {
      AbstractModel form = formMap.get(formId);
      FormDefinition formDefinition = formJsonConverter.convertToForm(form.getModelEditorJson(), null, form.getVersion());
      FormInfoContainer infoContainer = new FormInfoContainer(form.getName(), form.getDescription(), formDefinition.getKey(), form.getModelEditorJson());
      return infoContainer;

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
  
  protected class FormInfoContainer {
    
    protected String name;
    protected String description;
    protected String key;
    protected String json;
    
    public FormInfoContainer(String name, String description, String key, String json) {
      this.name = name;
      this.description = description;
      this.key = key;
      this.json = json;
    }
    
    public String getName() {
      return name;
    }
    public void setName(String name) {
      this.name = name;
    }
    public String getDescription() {
      return description;
    }
    public void setDescription(String description) {
      this.description = description;
    }
    public String getKey() {
      return key;
    }
    public void setKey(String key) {
      this.key = key;
    }
    public String getJson() {
      return json;
    }
    public void setJson(String json) {
      this.json = json;
    }
  }
}

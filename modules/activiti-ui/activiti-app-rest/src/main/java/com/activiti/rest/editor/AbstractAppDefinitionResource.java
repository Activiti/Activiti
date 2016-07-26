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
package com.activiti.rest.editor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.ExtensionElement;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.Task;
import org.activiti.bpmn.model.UserTask;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.editor.language.json.converter.util.CollectionUtils;
import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.User;
import org.activiti.form.model.FormDefinition;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.multipart.MultipartFile;

import com.activiti.domain.editor.AbstractModel;
import com.activiti.domain.editor.AppDefinition;
import com.activiti.domain.editor.AppModelDefinition;
import com.activiti.domain.editor.Model;
import com.activiti.model.editor.AppDefinitionPublishRepresentation;
import com.activiti.model.editor.AppDefinitionRepresentation;
import com.activiti.model.editor.AppDefinitionUpdateResultRepresentation;
import com.activiti.model.editor.ModelRepresentation;
import com.activiti.repository.editor.ModelRepository;
import com.activiti.security.SecurityUtils;
import com.activiti.service.api.DeploymentService;
import com.activiti.service.editor.AppDefinitionPublishService;
import com.activiti.service.editor.ModelInternalService;
import com.activiti.service.exception.BadRequestException;
import com.activiti.service.exception.InternalServerErrorException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class AbstractAppDefinitionResource extends BaseModelResource {

  private static final Logger logger = LoggerFactory.getLogger(AbstractAppDefinitionResource.class);

  @Autowired
  protected Environment env;

  @Autowired
  protected ModelInternalService modelService;

  @Autowired
  protected IdentityService identityService;

  @Autowired
  protected DeploymentService deploymentService;

  @Autowired
  protected ModelRepository modelRepository;

  @Autowired
  protected AppDefinitionPublishService appDefinitionPublishService;

  @Autowired
  protected ObjectMapper objectMapper;

  protected BpmnJsonConverter bpmnJsonConverter = new BpmnJsonConverter();

  public void exportAppDefinition(HttpServletResponse response, Long modelId) throws IOException {

    if (modelId == null) {
      throw new BadRequestException("No application definition id provided");
    }

    Model appModel = getModel(modelId, true, false);
    String appDefJson = appModel.getModelEditorJson();

    AppDefinitionRepresentation appRepresentation = createAppDefinitionRepresentation(appModel);

    createAppDefinitionZip(response, appRepresentation, appDefJson, SecurityUtils.getCurrentUserObject());
  }

  public AppDefinitionRepresentation importAppDefinition(HttpServletRequest request, MultipartFile file) {
    try {
      InputStream is = file.getInputStream();
      String fileName = file.getOriginalFilename();
      return importAppDefinition(request, is, fileName, null, null, null, null);

    } catch (IOException e) {
      throw new InternalServerErrorException("Error loading file", e);
    }
  }

  public AppDefinitionRepresentation importAppDefinitionNewVersion(HttpServletRequest request, MultipartFile file, Long appDefId) {
    try {
      InputStream is = file.getInputStream();
      String fileName = file.getOriginalFilename();
      Model appModel = getModel(appDefId, true, false);
      if (appModel.getModelType().equals(Model.MODEL_TYPE_APP) == false) {
        throw new BadRequestException("No app definition found for id " + appDefId);
      }

      AppDefinitionRepresentation appDefinition = createAppDefinitionRepresentation(appModel);

      Map<String, Model> existingProcessModelMap = new HashMap<String, Model>();
      Map<String, Map<String, Model>> existingSubProcessModelMap = new HashMap<String, Map<String, Model>>();
      Map<String, Map<String, Model>> existingFormModelMap = new HashMap<String, Map<String, Model>>();
      if (appDefinition.getDefinition() != null && CollectionUtils.isNotEmpty(appDefinition.getDefinition().getModels())) {
        for (AppModelDefinition modelDef : appDefinition.getDefinition().getModels()) {
          Model processModel = getModel(modelDef.getId(), false, false);
          List<Model> formModels = modelRepository.findModelsByModelTypeAndReferenceId(Model.MODEL_TYPE_FORM, processModel.getId());

          existingProcessModelMap.put(processModel.getName(), processModel);

          Map<String, Model> formMap = new HashMap<String, Model>();
          if (CollectionUtils.isNotEmpty(formModels)) {
            for (Model formModel : formModels) {
              formMap.put(formModel.getName(), formModel);
            }
          }
          existingFormModelMap.put(processModel.getName(), formMap);
        }
      }

      return importAppDefinition(request, is, fileName, appModel, existingProcessModelMap, existingSubProcessModelMap, existingFormModelMap);

    } catch (IOException e) {
      throw new InternalServerErrorException("Error loading file", e);
    }
  }

  protected AppDefinitionRepresentation importAppDefinition(HttpServletRequest request, InputStream is, String fileName, Model existingAppModel, Map<String, Model> existingProcessModelMap,
      Map<String, Map<String, Model>> existingSubProcessModelMap, Map<String, Map<String, Model>> existingFormModelMap) {

    if (fileName != null && (fileName.endsWith(".zip"))) {
      Map<String, String> formMap = new HashMap<String, String>();
      Map<String, String> bpmnModelMap = new HashMap<String, String>();
      Map<String, byte[]> thumbnailMap = new HashMap<String, byte[]>();

      Model tempAppDefinitionModel = readZipFile(is, formMap, bpmnModelMap, thumbnailMap);
      if (StringUtils.isNotEmpty(tempAppDefinitionModel.getName()) && StringUtils.isNotEmpty(tempAppDefinitionModel.getModelEditorJson())) {

        Map<Long, String> matchFormAndProcessMap = new HashMap<Long, String>();
        if (existingAppModel != null) {
          matchFormsAndSubProcesses(formMap, bpmnModelMap, matchFormAndProcessMap);
        }

        Map<Long, Model> oldFormKeyAndModelMap = importForms(formMap, thumbnailMap, matchFormAndProcessMap, existingFormModelMap);
        Map<Long, Model> oldBpmnModelIdAndModelMap = importBpmnModels(bpmnModelMap, oldFormKeyAndModelMap, thumbnailMap, existingProcessModelMap);

        AppDefinition appDefinition = null;
        try {
          appDefinition = objectMapper.readValue(tempAppDefinitionModel.getModelEditorJson(), AppDefinition.class);
        } catch (Exception e) {
          logger.error("Error reading app definition " + tempAppDefinitionModel.getModelEditorJson(), e);
          throw new BadRequestException("Error reading app definition", e);
        }

        Model appDefinitionModel = importAppDefinition(appDefinition, existingAppModel, tempAppDefinitionModel.getName(), oldBpmnModelIdAndModelMap);

        AppDefinitionRepresentation result = new AppDefinitionRepresentation(appDefinitionModel);
        result.setDefinition(appDefinition);
        return result;

      } else {
        throw new BadRequestException("Could not find app definition json");
      }

    } else {
      throw new BadRequestException("Invalid file name, only .zip files are supported not " + fileName);
    }
  }

  public AppDefinitionUpdateResultRepresentation publishAppDefinition(Long modelId, AppDefinitionPublishRepresentation publishModel) {

    User user = SecurityUtils.getCurrentUserObject();
    Model appModel = getModel(modelId, true, true);

    // Create pojo representation of the model and the json
    AppDefinitionRepresentation appDefinitionRepresentation = createAppDefinitionRepresentation(appModel);
    AppDefinitionUpdateResultRepresentation result = new AppDefinitionUpdateResultRepresentation();

    // Actual publication
    appDefinitionPublishService.publishAppDefinition(publishModel.getComment(), appModel, user);

    result.setAppDefinition(appDefinitionRepresentation);
    return result;

  }

  //
  // HELPER METHODS
  //

  protected AppDefinitionRepresentation createAppDefinitionRepresentation(AbstractModel model) {
    AppDefinition appDefinition = null;
    try {
      appDefinition = objectMapper.readValue(model.getModelEditorJson(), AppDefinition.class);
    } catch (Exception e) {
      logger.error("Error deserializing app " + model.getId(), e);
      throw new InternalServerErrorException("Could not deserialize app definition");
    }
    AppDefinitionRepresentation result = new AppDefinitionRepresentation(model);
    result.setDefinition(appDefinition);
    return result;
  }

  protected void createAppDefinitionZip(HttpServletResponse response, AppDefinitionRepresentation appDefinition, String appDefJson, User user) {
    response.setHeader("Content-Disposition", "attachment; filename=" + appDefinition.getName() + ".zip");
    try {
      ServletOutputStream servletOutputStream = response.getOutputStream();
      response.setContentType("application/zip");

      ZipOutputStream zipOutputStream = new ZipOutputStream(servletOutputStream);

      createZipEntry(zipOutputStream, appDefinition.getName() + ".json", appDefJson);

      List<AppModelDefinition> modelDefinitions = appDefinition.getDefinition().getModels();
      if (CollectionUtils.isNotEmpty(modelDefinitions)) {
        Map<Long, Model> formMap = new HashMap<Long, Model>();

        for (AppModelDefinition modelDef : modelDefinitions) {
          Model model = getModel(modelDef.getId(), true, false);
          String modelName = modelDef.getName();

          BpmnModel bpmnModel = modelService.getBpmnModel(model, true);
          String bpmnModelJson = bpmnJsonConverter.convertToJson(bpmnModel).toString();

          createZipEntry(zipOutputStream, "bpmn-models/" + createZipEntryFileName(modelName, model.getId()) + ".json", bpmnModelJson);
          if (model.getThumbnail() != null) {
            createZipEntry(zipOutputStream, "bpmn-models/" + createZipEntryFileName(modelName, model.getId()) + ".png", model.getThumbnail());
          }
          if (bpmnModel != null) {
            for (Process process : bpmnModel.getProcesses()) {
              processBpmnEditorModel(process.getFlowElements(), formMap);
            }
          }
        }

        for (Model formModel : formMap.values()) {
          String formName = formModel.getName();
          createZipEntry(zipOutputStream, "form-models/" + createZipEntryFileName(formName, formModel.getId()) + ".json", formModel.getModelEditorJson());

          if (formModel.getThumbnail() != null) {
            createZipEntry(zipOutputStream, "form-models/" + createZipEntryFileName(formName, formModel.getId()) + ".png", formModel.getThumbnail());
          }
        }
      }

      zipOutputStream.close();

      // Flush and close stream
      servletOutputStream.flush();
      servletOutputStream.close();

    } catch (Exception e) {
      logger.error("Could not generate app definition zip archive", e);
      throw new InternalServerErrorException("Could not generate app definition zip archive");
    }
  }

  protected Model readZipFile(InputStream inputStream, Map<String, String> formMap, Map<String, String> bpmnModelMap, Map<String, byte[]> thumbnailMap) {

    Model appDefinitionModel = null;
    ZipInputStream zipInputStream = null;
    try {
      zipInputStream = new ZipInputStream(inputStream);
      ZipEntry zipEntry = zipInputStream.getNextEntry();
      while (zipEntry != null) {
        String zipEntryName = zipEntry.getName();
        if (zipEntryName.endsWith("json") || zipEntryName.endsWith("png")) {

          String modelFileName = null;
          if (zipEntryName.contains("/")) {
            modelFileName = zipEntryName.substring(zipEntryName.indexOf("/") + 1);
          } else {
            modelFileName = zipEntryName;
          }

          if (modelFileName.endsWith(".png")) {
            thumbnailMap.put(modelFileName.replace(".png", ""), IOUtils.toByteArray(zipInputStream));

          } else {
            modelFileName = modelFileName.replace(".json", "");
            String json = IOUtils.toString(zipInputStream);

            if (zipEntryName.startsWith("bpmn-models/")) {
              bpmnModelMap.put(modelFileName, json);

            } else if (zipEntryName.startsWith("form-models/")) {
              formMap.put(modelFileName, json);

            } else if (zipEntryName.contains("/") == false) {
              appDefinitionModel = new Model();
              appDefinitionModel.setModelType(Model.MODEL_TYPE_APP);
              appDefinitionModel.setName(modelFileName);
              appDefinitionModel.setModelEditorJson(json);
            }
          }
        }

        zipEntry = zipInputStream.getNextEntry();
      }
    } catch (Exception e) {
      logger.error("Error reading app definition zip file", e);
      throw new InternalServerErrorException("Error reading app definition zip file");

    } finally {
      if (zipInputStream != null) {
        try {
          zipInputStream.closeEntry();
        } catch (Exception e) {
        }
        try {
          zipInputStream.close();
        } catch (Exception e) {
        }
      }
    }

    return appDefinitionModel;
  }

  protected void matchFormsAndSubProcesses(Map<String, String> formMap, Map<String, String> bpmnModelMap, Map<Long, String> matchFormAndProcessMap) {

    for (String bpmnModelKey : bpmnModelMap.keySet()) {
      int seperatorIndex = bpmnModelKey.lastIndexOf("-");
      String processName = bpmnModelKey.substring(0, seperatorIndex);

      String bpmnModelJson = bpmnModelMap.get(bpmnModelKey);

      ObjectNode editorJsonNode = readModelJson(bpmnModelJson);

      BpmnModel bpmnModel = bpmnJsonConverter.convertToBpmnModel(editorJsonNode);

      for (Process process : bpmnModel.getProcesses()) {
        matchBpmnModelWithFormIds(processName, process.getFlowElements(), matchFormAndProcessMap);
      }

    }
  }

  protected Map<Long, Model> importForms(Map<String, String> formMap, Map<String, byte[]> thumbnailMap, Map<Long, String> matchFormAndProcessMap,
      Map<String, Map<String, Model>> existingFormModelMap) {

    Map<Long, Model> oldFormKeyAndModelMap = new HashMap<Long, Model>();

    for (String formKey : formMap.keySet()) {
      int seperatorIndex = formKey.lastIndexOf("-");
      String formName = formKey.substring(0, seperatorIndex);
      Long formId = Long.valueOf(formKey.substring(seperatorIndex + 1));

      FormDefinition formDefinition = null;
      try {
        formDefinition = objectMapper.readValue(formMap.get(formKey), FormDefinition.class);
      } catch (Exception e) {
        logger.error("Error deserializing form", e);
        throw new InternalServerErrorException("Could not deserialize form definition");
      }

      String editorJson = null;
      try {
        editorJson = objectMapper.writeValueAsString(formDefinition);
      } catch (Exception e) {
        logger.error("Error while processing form json", e);
        throw new InternalServerErrorException("Form could not be saved " + formId);
      }

      Model existingModel = null;
      if (matchFormAndProcessMap != null && existingFormModelMap != null && matchFormAndProcessMap.containsKey(formId)) {
        String processModelName = matchFormAndProcessMap.get(formId);
        if (existingFormModelMap.containsKey(processModelName) && existingFormModelMap.get(processModelName).containsKey(formName)) {
          existingModel = existingFormModelMap.get(processModelName).get(formName);
        }
      }

      Model updatedFormModel = null;
      if (existingModel != null) {
        byte[] imageBytes = null;
        if (thumbnailMap.containsKey(formKey)) {
          imageBytes = thumbnailMap.get(formKey);
        }
        updatedFormModel = modelService.saveModel(existingModel, editorJson, imageBytes, true, "App definition import", SecurityUtils.getCurrentUserObject());

      } else {
        updatedFormModel = modelService.createModel(createModelRepresentation(formName, Model.MODEL_TYPE_FORM), editorJson, SecurityUtils.getCurrentUserObject());

        if (thumbnailMap.containsKey(formKey)) {
          updatedFormModel.setThumbnail(thumbnailMap.get(formKey));
          modelRepository.save(updatedFormModel);
        }
      }

      oldFormKeyAndModelMap.put(formId, updatedFormModel);
    }
    return oldFormKeyAndModelMap;
  }

  protected Map<Long, Model> importBpmnModels(Map<String, String> bpmnModelMap, Map<Long, Model> oldFormKeyAndModelMap,
      Map<String, byte[]> thumbnailMap, Map<String, Model> existingProcessModelMap) {

    Map<Long, Model> oldBpmnModelIdAndModelMap = new HashMap<Long, Model>();
    for (String bpmnModelKey : bpmnModelMap.keySet()) {
      int seperatorIndex = bpmnModelKey.lastIndexOf("-");
      String processName = bpmnModelKey.substring(0, seperatorIndex);
      String processId = bpmnModelKey.substring(seperatorIndex + 1);

      Model existingModel = null;
      if (existingProcessModelMap != null && existingProcessModelMap.containsKey(processName)) {
        existingModel = existingProcessModelMap.get(processName);
      }

      String bpmnModelJson = bpmnModelMap.get(bpmnModelKey);

      ObjectNode editorJsonNode = readModelJson(bpmnModelJson);

      Model updatedProcessModel = null;
      if (existingModel != null) {
        byte[] imageBytes = null;
        if (thumbnailMap.containsKey(bpmnModelKey)) {
          imageBytes = thumbnailMap.get(bpmnModelKey);
        }

        BpmnModel bpmnModel = bpmnJsonConverter.convertToBpmnModel(editorJsonNode);

        for (Process process : bpmnModel.getProcesses()) {
          updateBpmnForms(existingModel.getId(), process.getFlowElements(), oldFormKeyAndModelMap);
        }

        existingModel.setModelEditorJson(bpmnJsonConverter.convertToJson(bpmnModel).toString());

        updatedProcessModel = modelService.saveModel(existingModel, existingModel.getModelEditorJson(), imageBytes, true, "App definition import", SecurityUtils.getCurrentUserObject());

      } else {
        updatedProcessModel = modelService.createModel(createModelRepresentation(processName, Model.MODEL_TYPE_BPMN), bpmnModelJson, SecurityUtils.getCurrentUserObject());

        BpmnModel bpmnModel = bpmnJsonConverter.convertToBpmnModel(editorJsonNode);

        for (Process process : bpmnModel.getProcesses()) {
          updateBpmnForms(updatedProcessModel.getId(), process.getFlowElements(), oldFormKeyAndModelMap);
        }

        updatedProcessModel.setModelEditorJson(bpmnJsonConverter.convertToJson(bpmnModel).toString());

        if (thumbnailMap.containsKey(bpmnModelKey)) {
          updatedProcessModel.setThumbnail(thumbnailMap.get(bpmnModelKey));
        }

        modelService.saveModel(updatedProcessModel);
      }

      oldBpmnModelIdAndModelMap.put(Long.valueOf(processId), updatedProcessModel);
    }

    return oldBpmnModelIdAndModelMap;
  }

  protected Model importAppDefinition(AppDefinition appDefinition, Model existingAppModel, String name, Map<Long, Model> oldBpmnModelIdAndModelMap) {

    for (AppModelDefinition modelDef : appDefinition.getModels()) {
      Model newModel = oldBpmnModelIdAndModelMap.get(modelDef.getId());
      modelDef.setId(newModel.getId());
      modelDef.setCreatedBy(newModel.getCreatedBy());
      modelDef.setLastUpdatedBy(newModel.getLastUpdatedBy());
      modelDef.setLastUpdated(newModel.getLastUpdated());
      modelDef.setVersion(newModel.getVersion());
    }

    try {
      String updatedAppDefinitionJson = objectMapper.writeValueAsString(appDefinition);

      Model appDefinitionModel = null;
      if (existingAppModel != null) {
        appDefinitionModel = modelService.saveModel(existingAppModel, updatedAppDefinitionJson, null, true, "App definition import", SecurityUtils.getCurrentUserObject());
      } else {
        appDefinitionModel = modelService.createModel(createModelRepresentation(name, Model.MODEL_TYPE_APP), updatedAppDefinitionJson, SecurityUtils.getCurrentUserObject());
      }
      return appDefinitionModel;

    } catch (Exception e) {
      logger.error("Error storing app definition", e);
      throw new InternalServerErrorException("Error storing app definition");
    }
  }

  protected void processBpmnEditorModel(Collection<FlowElement> flowElements, Map<Long, Model> formMap) {

    for (FlowElement flowElement : flowElements) {
      if (flowElement instanceof UserTask) {
        UserTask userTask = (UserTask) flowElement;
        fillForm(userTask.getFormKey(), userTask, formMap);

      } else if (flowElement instanceof StartEvent) {
        StartEvent startEvent = (StartEvent) flowElement;
        fillForm(startEvent.getFormKey(), startEvent, formMap);

      } else if (flowElement instanceof SubProcess) {
        processBpmnEditorModel(((SubProcess) flowElement).getFlowElements(), formMap);
      }
    }
  }

  protected void matchBpmnModelWithFormIds(String modelName, Collection<FlowElement> flowElements, Map<Long, String> matchFormAndProcessMap) {
    for (FlowElement flowElement : flowElements) {
      if (flowElement instanceof UserTask) {
        UserTask userTask = (UserTask) flowElement;
        matchFormKeyValue(modelName, userTask.getFormKey(), userTask, matchFormAndProcessMap);

      } else if (flowElement instanceof StartEvent) {
        StartEvent startEvent = (StartEvent) flowElement;
        matchFormKeyValue(modelName, startEvent.getFormKey(), startEvent, matchFormAndProcessMap);

      } else if (flowElement instanceof SubProcess) {
        matchBpmnModelWithFormIds(modelName, ((SubProcess) flowElement).getFlowElements(), matchFormAndProcessMap);
      }
    }
  }

  protected void matchFormKeyValue(String modelName, String formKey, FlowElement flowElement, Map<Long, String> matchFormAndProcessMap) {
    List<ExtensionElement> formIdExtensions = flowElement.getExtensionElements().get("form-reference-id");
    if (CollectionUtils.isNotEmpty(formIdExtensions)) {
      Long formId = Long.valueOf(formIdExtensions.get(0).getElementText());
      matchFormAndProcessMap.put(formId, modelName);

    } else if (StringUtils.isNotEmpty(formKey) && formKey.startsWith("FORM_REFERENCE")) {
      String formIdValue = formKey.replace("FORM_REFERENCE", "");
      if (NumberUtils.isNumber(formIdValue)) {
        Long formId = Long.valueOf(formIdValue);
        matchFormAndProcessMap.put(formId, modelName);
      }
    }
  }

  protected void updateBpmnForms(Long modelId, Collection<FlowElement> flowElements, Map<Long, Model> formMap) {

    for (FlowElement flowElement : flowElements) {
      if (flowElement instanceof UserTask) {
        UserTask userTask = (UserTask) flowElement;
        updateFormKeyValue(userTask.getFormKey(), userTask, formMap, modelId);

      } else if (flowElement instanceof StartEvent) {
        StartEvent startEvent = (StartEvent) flowElement;
        updateFormKeyValue(startEvent.getFormKey(), startEvent, formMap, modelId);

      } else if (flowElement instanceof SequenceFlow) {
        SequenceFlow sequenceFlow = (SequenceFlow) flowElement;
        List<ExtensionElement> formIdElements = sequenceFlow.getExtensionElements().get("conditionFormId");
        if (CollectionUtils.isNotEmpty(formIdElements)) {
          ExtensionElement formIdElem = formIdElements.get(0);
          String formId = formIdElem.getElementText();
          if (StringUtils.isNotEmpty(formId) && NumberUtils.isNumber(formId) && formMap.containsKey(Long.valueOf(formId))) {
            Model formModel = formMap.get(Long.valueOf(formId));
            formIdElem.setElementText("" + formModel.getId());
            ExtensionElement formNameElement = null;
            if (CollectionUtils.isEmpty(sequenceFlow.getExtensionElements().get("conditionFormName"))) {
              formNameElement = new ExtensionElement();
              formNameElement.setNamespace("http://activiti.com/modeler");
              formNameElement.setNamespacePrefix("modeler");
              formNameElement.setName("conditionFormName");
              sequenceFlow.addExtensionElement(formNameElement);
            } else {
              formNameElement = sequenceFlow.getExtensionElements().get("conditionFormName").get(0);
            }
            formNameElement.setElementText(formModel.getName());
          }
        }

      } else if (flowElement instanceof SubProcess) {
        updateBpmnForms(modelId, ((SubProcess) flowElement).getFlowElements(), formMap);
      }
    }
  }

  protected void fillForm(String formKey, FlowElement flowElement, Map<Long, Model> formMap) {
    List<ExtensionElement> formIdExtensions = flowElement.getExtensionElements().get("form-reference-id");
    if (CollectionUtils.isNotEmpty(formIdExtensions)) {
      Long formId = Long.valueOf(formIdExtensions.get(0).getElementText());
      if (formMap.containsKey(formId) == false) {
        Model formModel = getModel(formId, true, false);
        formMap.put(formId, formModel);
      }

    } else if (StringUtils.isNotEmpty(formKey) && formKey.startsWith("FORM_REFERENCE")) {
      String formIdValue = formKey.replace("FORM_REFERENCE", "");
      if (NumberUtils.isNumber(formIdValue)) {
        Long formId = Long.valueOf(formIdValue);
        if (formMap.containsKey(formId) == false) {
          Model formModel = getModel(formId, true, false);
          formMap.put(formId, formModel);
        }
      }
    }
  }

  protected void updateFormKeyValue(String formKey, FlowElement flowElement, Map<Long, Model> formMap, Long modelId) {
    List<ExtensionElement> formIdExtensions = flowElement.getExtensionElements().get("form-reference-id");
    if (CollectionUtils.isNotEmpty(formIdExtensions)) {
      Long formId = Long.valueOf(formIdExtensions.get(0).getElementText());
      if (formMap.containsKey(formId)) {
        Model formModel = formMap.get(formId);
        if (flowElement instanceof UserTask) {
          ((UserTask) flowElement).setFormKey(String.valueOf(formModel.getId()));
          flowElement.getExtensionElements().get("form-reference-id").get(0).setElementText(String.valueOf(formModel.getId()));
          flowElement.getExtensionElements().get("form-reference-name").get(0).setElementText(formModel.getName());
          if (formModel.getReferenceId() == null || formModel.getReferenceId() == 0) {
            formModel.setReferenceId(modelId);
            modelRepository.save(formModel);
          }

        } else if (flowElement instanceof StartEvent) {
          ((StartEvent) flowElement).setFormKey(String.valueOf(formModel.getId()));
          flowElement.getExtensionElements().get("form-reference-id").get(0).setElementText(String.valueOf(formModel.getId()));
          flowElement.getExtensionElements().get("form-reference-name").get(0).setElementText(formModel.getName());
          if (formModel.getReferenceId() == null || formModel.getReferenceId() == 0) {
            formModel.setReferenceId(modelId);
            modelRepository.save(formModel);
          }
        }
      }

    } else if (StringUtils.isNotEmpty(formKey) && formKey.startsWith("FORM_REFERENCE")) {
      String formIdValue = formKey.replace("FORM_REFERENCE", "");
      if (NumberUtils.isNumber(formIdValue)) {
        Long formId = Long.valueOf(formIdValue);
        if (formMap.containsKey(formId)) {
          Model formModel = formMap.get(formId);
          if (flowElement instanceof UserTask) {
            ((UserTask) flowElement).setFormKey("FORM_REFERENCE" + formModel.getId() + "_" + formModel.getName());
            if (formModel.getReferenceId() == null || formModel.getReferenceId() == 0) {
              formModel.setReferenceId(modelId);
              modelRepository.save(formModel);
            }

          } else if (flowElement instanceof StartEvent) {
            ((StartEvent) flowElement).setFormKey("FORM_REFERENCE" + formModel.getId() + "_" + formModel.getName());
            if (formModel.getReferenceId() == null || formModel.getReferenceId() == 0) {
              formModel.setReferenceId(modelId);
              modelRepository.save(formModel);
            }
          }
        }
      }
    }
  }

  protected void updateExtensionElementName(String name, String updatedName, Task task) {
    List<ExtensionElement> extensionElements = task.getExtensionElements().get(name);
    if (CollectionUtils.isNotEmpty(extensionElements)) {
      extensionElements.get(0).setName(updatedName);
      task.getExtensionElements().remove(name);
      task.addExtensionElement(extensionElements.get(0));
    }
  }

  protected ModelRepresentation createModelRepresentation(String name, int type) {
    ModelRepresentation modelRepresentation = new ModelRepresentation();
    modelRepresentation.setName(name);
    modelRepresentation.setKey(name.replaceAll(" ", ""));
    modelRepresentation.setModelType(type);
    return modelRepresentation;
  }

  protected ObjectNode readModelJson(String modelJson) {
    ObjectNode editorJsonNode = null;
    try {
      editorJsonNode = (ObjectNode) objectMapper.readTree(modelJson);
    } catch (Exception e) {
      logger.error("Error reading BPMN json", e);
      throw new BadRequestException("Error reading BPMN json");
    }
    return editorJsonNode;
  }

  protected void createZipEntry(ZipOutputStream zipOutputStream, String filename, String content) throws Exception {
    createZipEntry(zipOutputStream, filename, content.getBytes(Charset.forName("UTF-8")));
  }

  protected void createZipEntry(ZipOutputStream zipOutputStream, String filename, byte[] content) throws Exception {
    ZipEntry entry = new ZipEntry(filename);
    zipOutputStream.putNextEntry(entry);
    zipOutputStream.write(content);
    zipOutputStream.closeEntry();
  }

  protected String createZipEntryFileName(String name, Long id) {
    return name + "-" + id;
  }
}

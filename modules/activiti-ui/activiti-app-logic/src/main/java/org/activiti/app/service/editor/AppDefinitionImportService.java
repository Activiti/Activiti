package org.activiti.app.service.editor;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServletRequest;

import org.activiti.app.domain.editor.AbstractModel;
import org.activiti.app.domain.editor.AppDefinition;
import org.activiti.app.domain.editor.AppModelDefinition;
import org.activiti.app.domain.editor.Model;
import org.activiti.app.model.editor.AppDefinitionPublishRepresentation;
import org.activiti.app.model.editor.AppDefinitionRepresentation;
import org.activiti.app.model.editor.AppDefinitionUpdateResultRepresentation;
import org.activiti.app.repository.editor.ModelRepository;
import org.activiti.app.security.SecurityUtils;
import org.activiti.app.service.api.ModelService;
import org.activiti.app.service.exception.BadRequestException;
import org.activiti.app.service.exception.InternalServerErrorException;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.editor.language.json.converter.util.CollectionUtils;
import org.activiti.editor.language.json.model.ModelInfo;
import org.activiti.engine.identity.User;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AppDefinitionImportService {

  private static final Logger logger = LoggerFactory.getLogger(AppDefinitionImportService.class);

  @Autowired
  protected AppDefinitionPublishService appDefinitionPublishService;
  
  @Autowired
  protected ModelService modelService;
  
  @Autowired
  protected ModelRepository modelRepository;

  @Autowired
  protected ObjectMapper objectMapper;
  
  protected BpmnJsonConverter bpmnJsonConverter = new BpmnJsonConverter();

  public AppDefinitionRepresentation importAppDefinition(HttpServletRequest request, MultipartFile file) {
    try {
      InputStream is = file.getInputStream();
      String fileName = file.getOriginalFilename();
      return importAppDefinition(request, is, fileName, null, null, null, null);

    } catch (IOException e) {
      throw new InternalServerErrorException("Error loading file", e);
    }
  }

  public AppDefinitionRepresentation importAppDefinitionNewVersion(HttpServletRequest request, MultipartFile file, String appDefId) {
    try {
      InputStream is = file.getInputStream();
      String fileName = file.getOriginalFilename();
      Model appModel = modelService.getModel(appDefId);
      if (appModel.getModelType().equals(Model.MODEL_TYPE_APP) == false) {
        throw new BadRequestException("No app definition found for id " + appDefId);
      }

      AppDefinitionRepresentation appDefinition = createAppDefinitionRepresentation(appModel);

      Map<String, Model> existingProcessModelMap = new HashMap<String, Model>();
      Map<String, Model> existingFormModelMap = new HashMap<String, Model>();
      Map<String, Model> existingDecisionTableMap = new HashMap<String, Model>();
      if (appDefinition.getDefinition() != null && CollectionUtils.isNotEmpty(appDefinition.getDefinition().getModels())) {
        for (AppModelDefinition modelDef : appDefinition.getDefinition().getModels()) {
          Model processModel = modelService.getModel(modelDef.getId());
          
          List<Model> referencedModels = modelRepository.findModelsByParentModelId(processModel.getId());
          for (Model childModel : referencedModels) {
            if (Model.MODEL_TYPE_FORM == childModel.getModelType()) {
              existingFormModelMap.put(childModel.getKey(), childModel);
              
            } else if (Model.MODEL_TYPE_DECISION_TABLE == childModel.getModelType()) {
              existingDecisionTableMap.put(childModel.getKey(), childModel);
            }
          }

          existingProcessModelMap.put(processModel.getKey(), processModel);
        }
      }

      return importAppDefinition(request, is, fileName, appModel, existingProcessModelMap, existingFormModelMap, existingDecisionTableMap);

    } catch (IOException e) {
      throw new InternalServerErrorException("Error loading file", e);
    }
  }

  protected AppDefinitionRepresentation importAppDefinition(HttpServletRequest request, InputStream is, String fileName, Model existingAppModel, Map<String, Model> existingProcessModelMap,
      Map<String, Model> existingFormModelMap, Map<String, Model> existingDecisionTableModelMap) {

    if (fileName != null && (fileName.endsWith(".zip"))) {
      Map<String, String> formMap = new HashMap<String, String>();
      Map<String, String> decisionTableMap = new HashMap<String, String>();
      Map<String, String> bpmnModelMap = new HashMap<String, String>();
      Map<String, byte[]> thumbnailMap = new HashMap<String, byte[]>();

      Model appDefinitionModel = readZipFile(is, formMap, decisionTableMap, bpmnModelMap, thumbnailMap);
      if (StringUtils.isNotEmpty(appDefinitionModel.getKey()) && StringUtils.isNotEmpty(appDefinitionModel.getModelEditorJson())) {

        Map<String, Model> formKeyAndModelMap = importForms(formMap, thumbnailMap, existingFormModelMap);
        Map<String, Model> decisionTableKeyAndModelMap = importDecisionTables(decisionTableMap, thumbnailMap, existingDecisionTableModelMap);
        Map<String, Model> bpmnModelIdAndModelMap = importBpmnModels(bpmnModelMap, formKeyAndModelMap, decisionTableKeyAndModelMap, 
            thumbnailMap, existingProcessModelMap);

        AppDefinitionRepresentation result = importAppDefinitionModel(appDefinitionModel, existingAppModel, bpmnModelIdAndModelMap);
        return result;

      } else {
        throw new BadRequestException("Could not find app definition json");
      }

    } else {
      throw new BadRequestException("Invalid file name, only .zip files are supported not " + fileName);
    }
  }

  public AppDefinitionUpdateResultRepresentation publishAppDefinition(String modelId, AppDefinitionPublishRepresentation publishModel) {

    User user = SecurityUtils.getCurrentUserObject();
    Model appModel = modelService.getModel(modelId);

    // Create pojo representation of the model and the json
    AppDefinitionRepresentation appDefinitionRepresentation = createAppDefinitionRepresentation(appModel);
    AppDefinitionUpdateResultRepresentation result = new AppDefinitionUpdateResultRepresentation();

    // Actual publication
    appDefinitionPublishService.publishAppDefinition(publishModel.getComment(), appModel, user);

    result.setAppDefinition(appDefinitionRepresentation);
    return result;

  }

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

  protected Model readZipFile(InputStream inputStream, Map<String, String> formMap, Map<String, String> decisionTableMap,
      Map<String, String> bpmnModelMap, Map<String, byte[]> thumbnailMap) {

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
              
            } else if (zipEntryName.startsWith("decision-table-models/")) {
              decisionTableMap.put(modelFileName, json);

            } else if (zipEntryName.contains("/") == false) {
              appDefinitionModel = createModelObject(json, Model.MODEL_TYPE_APP);
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

  protected Map<String, Model> importForms(Map<String, String> formMap, Map<String, byte[]> thumbnailMap, Map<String, Model> existingFormModelMap) {

    Map<String, Model> oldFormIdAndModelMap = new HashMap<String, Model>();

    for (String formKey : formMap.keySet()) {

      Model formModel = createModelObject(formMap.get(formKey), Model.MODEL_TYPE_FORM);
      String oldFormId = formModel.getId();

      Model existingModel = null;
      if (existingFormModelMap != null && existingFormModelMap.containsKey(formModel.getKey())) {
        existingModel = existingFormModelMap.get(formModel.getKey());
      }

      Model updatedFormModel = null;
      if (existingModel != null) {
        byte[] imageBytes = null;
        if (thumbnailMap.containsKey(formKey)) {
          imageBytes = thumbnailMap.get(formKey);
        }
        updatedFormModel = modelService.saveModel(existingModel, formModel.getModelEditorJson(), imageBytes, 
            true, "App definition import", SecurityUtils.getCurrentUserObject());

      } else {
        formModel.setId(null);
        updatedFormModel = modelService.createModel(formModel, SecurityUtils.getCurrentUserObject());

        if (thumbnailMap.containsKey(formKey)) {
          updatedFormModel.setThumbnail(thumbnailMap.get(formKey));
          modelRepository.save(updatedFormModel);
        }
      }

      oldFormIdAndModelMap.put(oldFormId, updatedFormModel);
    }
    
    return oldFormIdAndModelMap;
  }
  
  protected Map<String, Model> importDecisionTables(Map<String, String> decisionTableMap, Map<String, byte[]> thumbnailMap, 
      Map<String, Model> existingDecisionTableMap) {

    Map<String, Model> oldDecisionTableIdAndModelMap = new HashMap<String, Model>();

    for (String decisionTableKey : decisionTableMap.keySet()) {

      Model decisionTableModel = createModelObject(decisionTableMap.get(decisionTableKey), Model.MODEL_TYPE_DECISION_TABLE);
      String oldDecisionTableId = decisionTableModel.getId();

      Model existingModel = null;
      if (existingDecisionTableMap != null && existingDecisionTableMap.containsKey(decisionTableModel.getKey())) {
        existingModel = existingDecisionTableMap.get(decisionTableModel.getKey());
      }

      Model updatedDecisionTableModel = null;
      if (existingModel != null) {
        byte[] imageBytes = null;
        if (thumbnailMap.containsKey(decisionTableKey)) {
          imageBytes = thumbnailMap.get(decisionTableKey);
        }
        updatedDecisionTableModel = modelService.saveModel(existingModel, decisionTableModel.getModelEditorJson(), imageBytes, 
            true, "App definition import", SecurityUtils.getCurrentUserObject());

      } else {
        decisionTableModel.setId(null);
        updatedDecisionTableModel = modelService.createModel(decisionTableModel, SecurityUtils.getCurrentUserObject());
        
        if (thumbnailMap.containsKey(decisionTableKey)) {
          updatedDecisionTableModel.setThumbnail(thumbnailMap.get(decisionTableKey));
          modelRepository.save(updatedDecisionTableModel);
        }
      }

      oldDecisionTableIdAndModelMap.put(oldDecisionTableId, updatedDecisionTableModel);
    }
    
    return oldDecisionTableIdAndModelMap;
  }

  protected Map<String, Model> importBpmnModels(Map<String, String> bpmnModelMap, Map<String, Model> formKeyAndModelMap,
      Map<String, Model> decisionTableKeyAndModelMap, Map<String, byte[]> thumbnailMap, Map<String, Model> existingProcessModelMap) {

    Map<String, Model> bpmnModelIdAndModelMap = new HashMap<String, Model>();
    for (String bpmnModelKey : bpmnModelMap.keySet()) {
      
      Model existingModel = null;
      if (existingProcessModelMap != null && existingProcessModelMap.containsKey(bpmnModelKey)) {
        existingModel = existingProcessModelMap.get(bpmnModelKey);
      }

      String bpmnModelJson = bpmnModelMap.get(bpmnModelKey);
      Model bpmnModelObject = createModelObject(bpmnModelJson, Model.MODEL_TYPE_BPMN);
      String oldBpmnModelId = bpmnModelObject.getId();
      
      JsonNode bpmnModelNode = null;
      try {
        bpmnModelNode = objectMapper.readTree(bpmnModelObject.getModelEditorJson());
      } catch (Exception e) {
        logger.error("Error reading BPMN json for " + bpmnModelKey, e);
        throw new InternalServerErrorException("Error reading BPMN json for " + bpmnModelKey);
      }
      
      Map<String, String> oldFormIdFormKeyMap = new HashMap<String, String>();
      Map<String, ModelInfo> formKeyModelIdMap = new HashMap<String, ModelInfo>();
      for (String oldFormId : formKeyAndModelMap.keySet()) {
        Model formModel = formKeyAndModelMap.get(oldFormId);
        oldFormIdFormKeyMap.put(oldFormId, formModel.getKey());
        formKeyModelIdMap.put(formModel.getKey(), new ModelInfo(formModel.getId(), formModel.getName(), formModel.getKey()));
      }
      
      Map<String, String> oldDecisionTableIdDecisionTableKeyMap = new HashMap<String, String>();
      Map<String, ModelInfo> decisionTableKeyModelIdMap = new HashMap<String, ModelInfo>();
      for (String oldDecisionTableId : decisionTableKeyAndModelMap.keySet()) {
        Model decisionTableModel = decisionTableKeyAndModelMap.get(oldDecisionTableId);
        oldDecisionTableIdDecisionTableKeyMap.put(oldDecisionTableId, decisionTableModel.getKey());
        decisionTableKeyModelIdMap.put(decisionTableModel.getKey(), new ModelInfo(decisionTableModel.getId(), 
            decisionTableModel.getName(), decisionTableModel.getKey()));
      }

      BpmnModel bpmnModel = bpmnJsonConverter.convertToBpmnModel(bpmnModelNode, oldFormIdFormKeyMap, oldDecisionTableIdDecisionTableKeyMap);
      String updatedBpmnJson = bpmnJsonConverter.convertToJson(bpmnModel, formKeyModelIdMap, decisionTableKeyModelIdMap).toString();

      Model updatedProcessModel = null;
      if (existingModel != null) {
        byte[] imageBytes = null;
        if (thumbnailMap.containsKey(bpmnModelKey)) {
          imageBytes = thumbnailMap.get(bpmnModelKey);
        }

        existingModel.setModelEditorJson(updatedBpmnJson);

        updatedProcessModel = modelService.saveModel(existingModel, existingModel.getModelEditorJson(), imageBytes, true, "App definition import", SecurityUtils.getCurrentUserObject());

      } else {
        bpmnModelObject.setId(null);
        bpmnModelObject.setModelEditorJson(updatedBpmnJson);
        updatedProcessModel = modelService.createModel(bpmnModelObject, SecurityUtils.getCurrentUserObject());
        
        if (thumbnailMap.containsKey(bpmnModelKey)) {
          updatedProcessModel.setThumbnail(thumbnailMap.get(bpmnModelKey));
          modelService.saveModel(updatedProcessModel);
        }
      }

      bpmnModelIdAndModelMap.put(oldBpmnModelId, updatedProcessModel);
    }

    return bpmnModelIdAndModelMap;
  }

  protected AppDefinitionRepresentation importAppDefinitionModel(Model appDefinitionModel, Model existingAppModel, Map<String, Model> bpmnModelIdAndModelMap) {
    
    AppDefinition appDefinition = null;
    try {
      appDefinition = objectMapper.readValue(appDefinitionModel.getModelEditorJson(), AppDefinition.class);
    } catch (Exception e) {
      logger.error("Error reading app definition " + appDefinitionModel.getName(), e);
      throw new BadRequestException("Error reading app definition", e);
    }
    
    for (AppModelDefinition modelDef : appDefinition.getModels()) {
      if (bpmnModelIdAndModelMap.containsKey(modelDef.getId())) {
        Model newModel = bpmnModelIdAndModelMap.get(modelDef.getId());
        modelDef.setId(newModel.getId());
        modelDef.setName(newModel.getName());
        modelDef.setCreatedBy(newModel.getCreatedBy());
        modelDef.setLastUpdatedBy(newModel.getLastUpdatedBy());
        modelDef.setLastUpdated(newModel.getLastUpdated());
        modelDef.setVersion(newModel.getVersion());
      }
    }

    try {
      String updatedAppDefinitionJson = objectMapper.writeValueAsString(appDefinition);

      if (existingAppModel != null) {
        appDefinitionModel = modelService.saveModel(existingAppModel, updatedAppDefinitionJson, null, true, "App definition import", SecurityUtils.getCurrentUserObject());
      } else {
        appDefinitionModel.setId(null);
        appDefinitionModel.setModelEditorJson(updatedAppDefinitionJson);
        appDefinitionModel = modelService.createModel(appDefinitionModel, SecurityUtils.getCurrentUserObject());
      }
      
      AppDefinitionRepresentation result = new AppDefinitionRepresentation(appDefinitionModel);
      result.setDefinition(appDefinition);
      return result;

    } catch (Exception e) {
      logger.error("Error storing app definition", e);
      throw new InternalServerErrorException("Error storing app definition");
    }
  }

  protected Model createModelObject(String modelJson, int modelType) {
    try {
      JsonNode modelNode = objectMapper.readTree(modelJson);
      Model model = new Model();
      model.setId(modelNode.get("id").asText());
      model.setName(modelNode.get("name").asText());
      model.setKey(modelNode.get("key").asText());
      
      JsonNode descriptionNode = modelNode.get("description");
      if (descriptionNode != null && descriptionNode.isNull() == false) {
        model.setDescription(descriptionNode.asText());
      }
      
      model.setModelEditorJson(modelNode.get("editorJson").toString());
      model.setModelType(modelType);
      
      return model;
      
    } catch (Exception e) {
      logger.error("Error reading model json", e);
      throw new InternalServerErrorException("Error reading model json");
    }
  }
}

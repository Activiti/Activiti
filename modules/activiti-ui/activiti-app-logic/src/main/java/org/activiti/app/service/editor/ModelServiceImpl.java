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
package org.activiti.app.service.editor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.app.domain.editor.AbstractModel;
import org.activiti.app.domain.editor.AppDefinition;
import org.activiti.app.domain.editor.AppModelDefinition;
import org.activiti.app.domain.editor.Model;
import org.activiti.app.domain.editor.ModelHistory;
import org.activiti.app.domain.editor.ModelRelation;
import org.activiti.app.domain.editor.ModelRelationTypes;
import org.activiti.app.model.editor.ModelKeyRepresentation;
import org.activiti.app.model.editor.ModelRepresentation;
import org.activiti.app.model.editor.ReviveModelResultRepresentation;
import org.activiti.app.model.editor.ReviveModelResultRepresentation.UnresolveModelRepresentation;
import org.activiti.app.repository.editor.ModelHistoryRepository;
import org.activiti.app.repository.editor.ModelRelationRepository;
import org.activiti.app.repository.editor.ModelRepository;
import org.activiti.app.service.api.DeploymentService;
import org.activiti.app.service.api.ModelService;
import org.activiti.app.service.api.UserCache;
import org.activiti.app.service.exception.InternalServerErrorException;
import org.activiti.app.service.exception.NotFoundException;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.ExtensionElement;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.UserTask;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.editor.language.json.converter.util.CollectionUtils;
import org.activiti.editor.language.json.converter.util.JsonConverterUtil;
import org.activiti.engine.identity.User;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class ModelServiceImpl implements ModelService {

  private final Logger log = LoggerFactory.getLogger(ModelServiceImpl.class);

  public static final String NAMESPACE = "http://activiti.com/modeler";
  
  protected static final String PROCESS_NOT_FOUND_MESSAGE_KEY = "PROCESS.ERROR.NOT-FOUND";

  @Autowired
  protected DeploymentService deploymentService;
  
  @Autowired
  protected ModelImageService modelImageService;

  @Autowired
  protected ModelRepository modelRepository;

  @Autowired
  protected ModelHistoryRepository modelHistoryRepository;

  @Autowired
  protected ModelRelationRepository modelRelationRepository;

  @Autowired
  protected ObjectMapper objectMapper;

  @Autowired
  protected UserCache userCache;

  protected BpmnJsonConverter bpmnJsonConverter = new BpmnJsonConverter();

  protected BpmnXMLConverter bpmnXMLConverter = new BpmnXMLConverter();
  
  @Override
  public Model getModel(Long modelId) {
    Model model = modelRepository.findOne(modelId);

    if (model == null) {
      NotFoundException modelNotFound = new NotFoundException("No model found with the given id: " + modelId);
      modelNotFound.setMessageKey(PROCESS_NOT_FOUND_MESSAGE_KEY);
      throw modelNotFound;
    }

    return model;
  }

  @Override
  public List<AbstractModel> getModelsByModelType(Integer modelType) {
    return new ArrayList<AbstractModel>(modelRepository.findModelsByModelType(modelType));
  }
  
  @Override
  public ModelHistory getModelHistory(Long modelId, Long modelHistoryId) {
    // Check if the user has read-rights on the process-model in order to fetch history
    Model model = getModel(modelId);
    ModelHistory modelHistory = modelHistoryRepository.findOne(modelHistoryId);

    // Check if history corresponds to the current model and is not deleted
    if (modelHistory == null || modelHistory.getRemovalDate() != null || !modelHistory.getModelId().equals(model.getId())) {
      throw new NotFoundException("Process model history not found: " + modelHistoryId);
    }
    return modelHistory;
  }

  @Override
  public byte[] getBpmnXML(AbstractModel model) {
    BpmnModel bpmnModel = getBpmnModel(model);
    return getBpmnXML(bpmnModel);
  }

  @Override
  public byte[] getBpmnXML(BpmnModel bpmnModel) {
    for (Process process : bpmnModel.getProcesses()) {
      if (StringUtils.isNotEmpty(process.getId())) {
        char firstCharacter = process.getId().charAt(0);
        // no digit is allowed as first character
        if (Character.isDigit(firstCharacter)) {
          process.setId("a" + process.getId());
        }
      }
    }
    byte[] xmlBytes = bpmnXMLConverter.convertToXML(bpmnModel);
    return xmlBytes;
  }
  
  public ModelKeyRepresentation validateModelKey(Model model, Integer modelType, String key) {
    ModelKeyRepresentation modelKeyResponse = new ModelKeyRepresentation();
    modelKeyResponse.setKey(key);
    
    List<Model> models = modelRepository.findModelsByKeyAndType(key, modelType);
    for (Model modelInfo : models) {
      if (model == null || modelInfo.getId().equals(model.getId()) == false) {
        modelKeyResponse.setKeyAlreadyExists(true);
        modelKeyResponse.setId(modelInfo.getId());
        modelKeyResponse.setName(modelInfo.getName());
        break;
      }
    }
    
    return modelKeyResponse;
  }
  
  @Override
  @Transactional
  public Model createModel(Model newModel, User createdBy) {
    newModel.setVersion(1);
    newModel.setCreated(Calendar.getInstance().getTime());
    newModel.setCreatedBy(createdBy.getId());
    newModel.setLastUpdated(Calendar.getInstance().getTime());
    newModel.setLastUpdatedBy(createdBy.getId());
    
    persistModel(newModel);
    return newModel;
  }

  @Override
  @Transactional
  public Model createModel(ModelRepresentation model, String editorJson, User createdBy) {
    Model newModel = new Model();
    newModel.setVersion(1);
    newModel.setName(model.getName());
    newModel.setKey(model.getKey());
    newModel.setModelType(model.getModelType());
    newModel.setCreated(Calendar.getInstance().getTime());
    newModel.setCreatedBy(createdBy.getId());
    newModel.setDescription(model.getDescription());
    newModel.setModelEditorJson(editorJson);
    newModel.setLastUpdated(Calendar.getInstance().getTime());
    newModel.setLastUpdatedBy(createdBy.getId());

    persistModel(newModel);
    return newModel;
  }

  @Override
  @Transactional
  public Model createNewModelVersion(Model modelObject, String comment, User updatedBy) {
    return (Model) internalCreateNewModelVersion(modelObject, comment, updatedBy, false);
  }

  @Override
  @Transactional
  public ModelHistory createNewModelVersionAndReturnModelHistory(Model modelObject, String comment, User updatedBy) {
    return (ModelHistory) internalCreateNewModelVersion(modelObject, comment, updatedBy, true);
  }

  protected AbstractModel internalCreateNewModelVersion(Model modelObject, String comment, User updatedBy, boolean returnModelHistory) {
    modelObject.setLastUpdated(new Date());
    modelObject.setLastUpdatedBy(updatedBy.getId());
    modelObject.setComment(comment);

    ModelHistory historyModel = createNewModelhistory(modelObject);
    persistModelHistory(historyModel);

    modelObject.setVersion(modelObject.getVersion() + 1);
    persistModel(modelObject);

    return returnModelHistory ? historyModel : modelObject;
  }

  @Override
  public Model saveModel(Model modelObject) {
    return persistModel(modelObject);
  }

  @Override
  @Transactional
  public Model saveModel(Model modelObject, String editorJson, byte[] imageBytes, boolean newVersion, String newVersionComment, User updatedBy) {

    return internalSave(modelObject.getName(), modelObject.getKey(), modelObject.getDescription(), editorJson, newVersion, 
        newVersionComment, imageBytes, updatedBy, modelObject);
  }

  @Override
  @Transactional
  public Model saveModel(long modelId, String name, String key, String description, String editorJson, 
      boolean newVersion, String newVersionComment, User updatedBy) {

    Model modelObject = modelRepository.findOne(modelId);
    return internalSave(name, key, description, editorJson, newVersion, newVersionComment, null, updatedBy, modelObject);
  }

  protected Model internalSave(String name, String key, String description, String editorJson, boolean newVersion, 
      String newVersionComment, byte[] imageBytes, User updatedBy, Model modelObject) {

    if (newVersion == false) {

      modelObject.setLastUpdated(new Date());
      modelObject.setLastUpdatedBy(updatedBy.getId());
      modelObject.setName(name);
      modelObject.setKey(key);
      modelObject.setDescription(description);
      modelObject.setModelEditorJson(editorJson);

      if (imageBytes != null) {
        modelObject.setThumbnail(imageBytes);
      }

    } else {

      ModelHistory historyModel = createNewModelhistory(modelObject);
      persistModelHistory(historyModel);

      modelObject.setVersion(modelObject.getVersion() + 1);
      modelObject.setLastUpdated(new Date());
      modelObject.setLastUpdatedBy(updatedBy.getId());
      modelObject.setName(name);
      modelObject.setKey(key);
      modelObject.setDescription(description);
      modelObject.setModelEditorJson(editorJson);
      modelObject.setComment(newVersionComment);

      if (imageBytes != null) {
        modelObject.setThumbnail(imageBytes);
      }
    }

    return persistModel(modelObject);
  }

  @Override
  @Transactional
  public void deleteModel(long modelId, boolean cascadeHistory, boolean deleteRuntimeApp) {

    Model model = modelRepository.findOne(modelId);
    if (model == null) {
      throw new IllegalArgumentException("No model found with id: " + modelId);
    }

    // Fetch current model history list
    List<ModelHistory> history = modelHistoryRepository.findByModelIdAndRemovalDateIsNullOrderByVersionDesc(model.getId());

    // if the model is an app definition and the runtime app needs to be deleted, remove it now
    if (deleteRuntimeApp && model.getModelType() == Model.MODEL_TYPE_APP) {
      /*Long appDefinitionId = runtimeAppDefinitionService.getDefinitionIdForModelAndUser(model.getId(), SecurityUtils.getCurrentUserObject());
      if (appDefinitionId != null) {
        deploymentService.deleteAppDefinition(appDefinitionId);
      }*/

    } else {
      // Move model to history and mark removed
      ModelHistory historyModel = createNewModelhistory(model);
      historyModel.setRemovalDate(Calendar.getInstance().getTime());
      persistModelHistory(historyModel);
    }

    if (cascadeHistory || history.size() == 0) {
      deleteModelAndChildren(model);
    } else {
      // History available and no cascade was requested. Revive latest history entry
      ModelHistory toRevive = history.remove(0);
      populateModelBasedOnHistory(model, toRevive);
      persistModel(model);
      modelHistoryRepository.delete(toRevive);
    }
  }

  protected void deleteModelAndChildren(Model model) {

    // Models have relations with each other, in all kind of wicked and funny ways.
    // Hence, we remove first all relations, comments, etc. while collecting all models.
    // Then, once all foreign key problemmakers are removed, we remove the models

    List<Model> allModels = new ArrayList<Model>();
    internalDeleteModelAndChildren(model, allModels);

    for (Model modelToDelete : allModels) {
      modelRepository.delete(modelToDelete);
    }
  }

  protected void internalDeleteModelAndChildren(Model model, List<Model> allModels) {
    // Delete all related data
    modelRelationRepository.deleteModelRelationsForParentModel(model.getId());

    allModels.add(model);
  }

  @Override
  @Transactional
  public ReviveModelResultRepresentation reviveProcessModelHistory(ModelHistory modelHistory, User user, String newVersionComment) {
    Model latestModel = modelRepository.findOne(modelHistory.getModelId());
    if (latestModel == null) {
      throw new IllegalArgumentException("No process model found with id: " + modelHistory.getModelId());
    }

    // Store the current model in history
    ModelHistory latestModelHistory = createNewModelhistory(latestModel);
    persistModelHistory(latestModelHistory);

    // Populate the actual latest version with the properties in the historic model
    latestModel.setVersion(latestModel.getVersion() + 1);
    latestModel.setLastUpdated(new Date());
    latestModel.setLastUpdatedBy(user.getId());
    latestModel.setName(modelHistory.getName());
    latestModel.setKey(modelHistory.getKey());
    latestModel.setDescription(modelHistory.getDescription());
    latestModel.setModelEditorJson(modelHistory.getModelEditorJson());
    latestModel.setModelType(modelHistory.getModelType());
    latestModel.setComment(newVersionComment);
    persistModel(latestModel);

    ReviveModelResultRepresentation result = new ReviveModelResultRepresentation();

    // For apps, we need to make sure the referenced processes exist as models.
    // It could be the user has deleted the process model in the meantime. We give back that info to the user.
    if (latestModel.getModelType() == AbstractModel.MODEL_TYPE_APP) {
      if (StringUtils.isNotEmpty(latestModel.getModelEditorJson())) {
        try {
          AppDefinition appDefinition = objectMapper.readValue(latestModel.getModelEditorJson(), AppDefinition.class);
          for (AppModelDefinition appModelDefinition : appDefinition.getModels()) {
            if (!modelRepository.exists(appModelDefinition.getId())) {
              result.getUnresolvedModels().add(new UnresolveModelRepresentation(appModelDefinition.getId(), 
                  appModelDefinition.getName(), appModelDefinition.getLastUpdatedBy()));
            }
          }
        } catch (Exception e) {
          log.error("Could not deserialize app model json (id = " + latestModel.getId() + ")", e);
        }
      }
    }

    return result;
  }

  @Override
  public BpmnModel getBpmnModel(AbstractModel model) {
    BpmnModel bpmnModel = null;
    try {
      Map<Long, Model> formMap = new HashMap<Long, Model>();
      Map<Long, Model> decisionTableMap = new HashMap<Long, Model>();
      
      List<Model> referencedModels = modelRepository.findModelsByParentModelId(model.getId());
      for (Model childModel : referencedModels) {
        if (Model.MODEL_TYPE_FORM == childModel.getModelType()) {
          formMap.put(childModel.getId(), childModel);
          
        } else if (Model.MODEL_TYPE_DECISION_TABLE == childModel.getModelType()) {
          decisionTableMap.put(childModel.getId(), childModel);
        }
      }
      
      bpmnModel = getBpmnModel(model, formMap, decisionTableMap);

    } catch (Exception e) {
      log.error("Could not generate BPMN 2.0 model for " + model.getId(), e);
      throw new InternalServerErrorException("Could not generate BPMN 2.0 model");
    }

    return bpmnModel;
  }
  
  @Override
  public BpmnModel getBpmnModel(AbstractModel model, Map<Long, Model> formMap, Map<Long, Model> decisionTableMap) {
    try {
      ObjectNode editorJsonNode = (ObjectNode) objectMapper.readTree(model.getModelEditorJson());
      Map<Long, String> formKeyMap = new HashMap<Long, String>();
      for (Model formModel : formMap.values()) {
        formKeyMap.put(formModel.getId(), formModel.getKey());
      }
      
      Map<Long, String> decisionTableKeyMap = new HashMap<Long, String>();
      for (Model decisionTableModel : decisionTableMap.values()) {
        decisionTableKeyMap.put(decisionTableModel.getId(), decisionTableModel.getKey());
      }
      
      return bpmnJsonConverter.convertToBpmnModel(editorJsonNode, formKeyMap, decisionTableKeyMap);
      
    } catch (Exception e) {
      log.error("Could not generate BPMN 2.0 model for " + model.getId(), e);
      throw new InternalServerErrorException("Could not generate BPMN 2.0 model");
    }
  }

  protected void addOrUpdateExtensionElement(String name, String value, UserTask userTask) {
    List<ExtensionElement> extensionElements = userTask.getExtensionElements().get(name);

    ExtensionElement extensionElement;

    if (CollectionUtils.isNotEmpty(extensionElements)) {
      extensionElement = extensionElements.get(0);
    } else {
      extensionElement = new ExtensionElement();
    }
    extensionElement.setNamespace(NAMESPACE);
    extensionElement.setNamespacePrefix("modeler");
    extensionElement.setName(name);
    extensionElement.setElementText(value);

    if (CollectionUtils.isEmpty(extensionElements)) {
      userTask.addExtensionElement(extensionElement);
    }
  }

  public Long getModelCountForUser(User user, int modelType) {
    return modelRepository.countByModelTypeAndUser(modelType, user.getId());
  }

  protected Model persistModel(Model model) {

    model = modelRepository.save((Model) model);

    if (StringUtils.isNotEmpty(model.getModelEditorJson())) {

      // Parse json to java
      ObjectNode jsonNode = null;
      try {
        jsonNode = (ObjectNode) objectMapper.readTree(model.getModelEditorJson());
      } catch (Exception e) {
        log.error("Could not deserialize json model", e);
        throw new InternalServerErrorException("Could not deserialize json model");
      }

      if ((model.getModelType() == null || model.getModelType().intValue() == Model.MODEL_TYPE_BPMN)) {

        // Thumbnail
        modelImageService.generateThumbnailImage(model, jsonNode);

        // Relations
        handleBpmnProcessFormModelRelations(model, jsonNode);
        handleBpmnProcessDecisionTaskModelRelations(model, jsonNode);
        
      } else if (model.getModelType().intValue() == Model.MODEL_TYPE_FORM || 
          model.getModelType().intValue() == Model.MODEL_TYPE_DECISION_TABLE) {
        
        jsonNode.put("name", model.getName());
        jsonNode.put("key", model.getKey());

      } else if (model.getModelType().intValue() == Model.MODEL_TYPE_APP) {

        handleAppModelProcessRelations(model, jsonNode);
      }
    }

    return model;
  }

  protected ModelHistory persistModelHistory(ModelHistory modelHistory) {
    return modelHistoryRepository.save(modelHistory);
  }

  protected void handleBpmnProcessFormModelRelations(AbstractModel bpmnProcessModel, ObjectNode editorJsonNode) {
    List<JsonNode> formReferenceNodes = JsonConverterUtil.filterOutJsonNodes(JsonConverterUtil.getBpmnProcessModelFormReferences(editorJsonNode));
    Set<Long> formIds = JsonConverterUtil.gatherLongPropertyFromJsonNodes(formReferenceNodes, "id");
    
    handleModelRelations(bpmnProcessModel, formIds, ModelRelationTypes.TYPE_FORM_MODEL_CHILD);
  }
  
  protected void handleBpmnProcessDecisionTaskModelRelations(AbstractModel bpmnProcessModel, ObjectNode editorJsonNode) {
    List<JsonNode> decisionTableNodes = JsonConverterUtil.filterOutJsonNodes(JsonConverterUtil.getBpmnProcessModelDecisionTableReferences(editorJsonNode));
    Set<Long> decisionTableIds = JsonConverterUtil.gatherLongPropertyFromJsonNodes(decisionTableNodes, "id");

    handleModelRelations(bpmnProcessModel, decisionTableIds, ModelRelationTypes.TYPE_DECISION_TABLE_MODEL_CHILD);
  }

  protected void handleAppModelProcessRelations(AbstractModel appModel, ObjectNode appModelJsonNode) {
    Set<Long> processModelIds = JsonConverterUtil.getAppModelReferencedModelIds(appModelJsonNode);
    handleModelRelations(appModel, processModelIds, ModelRelationTypes.TYPE_PROCESS_MODEL);
  }

  /**
   * Generic handling of model relations: deleting/adding where needed.
   */
  protected void handleModelRelations(AbstractModel bpmnProcessModel, Set<Long> idsReferencedInJson, String relationshipType) {

    // Find existing persisted relations
    List<ModelRelation> persistedModelRelations = modelRelationRepository.findByParentModelIdAndType(bpmnProcessModel.getId(), relationshipType);
    
    // if no ids referenced now, just delete them all
    if (idsReferencedInJson == null || idsReferencedInJson.size() == 0) {
      modelRelationRepository.delete(persistedModelRelations);
      return;
    }

    Set<Long> alreadyPersistedModelIds = new HashSet<Long>(persistedModelRelations.size());
    for (ModelRelation persistedModelRelation : persistedModelRelations) {
      if (!idsReferencedInJson.contains(persistedModelRelation.getModelId())) {
        // model used to be referenced, but not anymore. Delete it.
        modelRelationRepository.delete((ModelRelation) persistedModelRelation);
      } else {
        alreadyPersistedModelIds.add(persistedModelRelation.getModelId());
      }
    }

    // Loop over all referenced ids and see which one are new
    for (Long idReferencedInJson : idsReferencedInJson) {
      
      // if model is referenced, but it is not yet persisted = create it
      if (!alreadyPersistedModelIds.contains(idReferencedInJson)) {

        // Check if model actually still exists. Don't create the relationship if it doesn't exist. The client UI will have cope with this too.
        if (modelRepository.exists(idReferencedInJson)) {
          modelRelationRepository.save(new ModelRelation(bpmnProcessModel.getId(), idReferencedInJson, relationshipType));
        }
      }
    }
  }

  protected ModelHistory createNewModelhistory(Model model) {
    ModelHistory historyModel = new ModelHistory();
    historyModel.setName(model.getName());
    historyModel.setKey(model.getKey());
    historyModel.setDescription(model.getDescription());
    historyModel.setCreated(model.getCreated());
    historyModel.setLastUpdated(model.getLastUpdated());
    historyModel.setCreatedBy(model.getCreatedBy());
    historyModel.setLastUpdatedBy(model.getLastUpdatedBy());
    historyModel.setModelEditorJson(model.getModelEditorJson());
    historyModel.setModelType(model.getModelType());
    historyModel.setVersion(model.getVersion());
    historyModel.setModelId(model.getId());
    historyModel.setComment(model.getComment());

    return historyModel;
  }

  protected void populateModelBasedOnHistory(Model model, ModelHistory basedOn) {
    model.setName(basedOn.getName());
    model.setKey(basedOn.getKey());
    model.setDescription(basedOn.getDescription());
    model.setCreated(basedOn.getCreated());
    model.setLastUpdated(basedOn.getLastUpdated());
    model.setCreatedBy(basedOn.getCreatedBy());
    model.setLastUpdatedBy(basedOn.getLastUpdatedBy());
    model.setModelEditorJson(basedOn.getModelEditorJson());
    model.setModelType(basedOn.getModelType());
    model.setVersion(basedOn.getVersion());
    model.setComment(basedOn.getComment());
  }
}

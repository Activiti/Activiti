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
package org.activiti.app.rest.editor;

import java.text.ParseException;
import java.util.Date;

import org.activiti.app.domain.editor.Model;
import org.activiti.app.model.editor.ModelKeyRepresentation;
import org.activiti.app.model.editor.ModelRepresentation;
import org.activiti.app.repository.editor.ModelRepository;
import org.activiti.app.security.SecurityUtils;
import org.activiti.app.service.api.ModelService;
import org.activiti.app.service.exception.BadRequestException;
import org.activiti.app.service.exception.ConflictingRequestException;
import org.activiti.app.service.exception.InternalServerErrorException;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.identity.User;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@RestController
public class ModelResource extends AbstractModelResource {

  private static final Logger log = LoggerFactory.getLogger(ModelResource.class);

  private static final String RESOLVE_ACTION_OVERWRITE = "overwrite";
  private static final String RESOLVE_ACTION_SAVE_AS = "saveAs";
  private static final String RESOLVE_ACTION_NEW_VERSION = "newVersion";

  @Autowired
  protected ModelService modelService;
  
  @Autowired
  protected ModelRepository modelRepository;
  
  @Autowired
  protected ObjectMapper objectMapper;

  protected BpmnJsonConverter bpmnJsonConverter = new BpmnJsonConverter();

  protected BpmnXMLConverter bpmnXMLConverter = new BpmnXMLConverter();

  /**
   * GET /rest/models/{modelId} -> Get process model
   */
  @RequestMapping(value = "/rest/models/{modelId}", method = RequestMethod.GET, produces = "application/json")
  public ModelRepresentation getModel(@PathVariable String modelId) {
    return super.getModel(modelId);
  }

  /**
   * GET /rest/models/{modelId}/thumbnail -> Get process model thumbnail
   */
  @RequestMapping(value = "/rest/models/{modelId}/thumbnail", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
  public byte[] getModelThumbnail(@PathVariable String modelId) {
    return super.getModelThumbnail(modelId);
  }

  /**
   * PUT /rest/models/{modelId} -> update process model properties
   */
  @RequestMapping(value = "/rest/models/{modelId}", method = RequestMethod.PUT)
  public ModelRepresentation updateModel(@PathVariable String modelId, @RequestBody ModelRepresentation updatedModel) {
    // Get model, write-permission required if not a favorite-update
    Model model = modelService.getModel(modelId);
    
    ModelKeyRepresentation modelKeyInfo = modelService.validateModelKey(model, model.getModelType(), updatedModel.getKey());
    if (modelKeyInfo.isKeyAlreadyExists()) {
      throw new BadRequestException("Model with provided key already exists " + updatedModel.getKey());
    }

    try {
      updatedModel.updateModel(model);
      modelRepository.save(model);
      
      ModelRepresentation result = new ModelRepresentation(model);
      return result;

    } catch (Exception e) {
      throw new BadRequestException("Model cannot be updated: " + modelId);
    }
  }

  /**
   * DELETE /rest/models/{modelId} -> delete process model or, as a non-owner, remove the share info link for that user specifically
   */
  @ResponseStatus(value = HttpStatus.OK)
  @RequestMapping(value = "/rest/models/{modelId}", method = RequestMethod.DELETE)
  public void deleteModel(@PathVariable String modelId, @RequestParam(required = false) Boolean cascade, @RequestParam(required = false) Boolean deleteRuntimeApp) {

    // Get model to check if it exists, read-permission required for delete (in case user is not owner, only share info
    // will be deleted
    Model model = modelService.getModel(modelId);

    try {
      String currentUserId = SecurityUtils.getCurrentUserId();
      boolean currentUserIsOwner = currentUserId.equals(model.getCreatedBy());
      if (currentUserIsOwner) {
        modelService.deleteModel(model.getId(), Boolean.TRUE.equals(cascade), Boolean.TRUE.equals(deleteRuntimeApp));
      }

    } catch (Exception e) {
      log.error("Error while deleting: ", e);
      throw new BadRequestException("Model cannot be deleted: " + modelId);
    }
  }

  /**
   * GET /rest/models/{modelId}/editor/json -> get the JSON model
   */
  @RequestMapping(value = "/rest/models/{modelId}/editor/json", method = RequestMethod.GET, produces = "application/json")
  public ObjectNode getModelJSON(@PathVariable String modelId) {
    Model model = modelService.getModel(modelId);
    ObjectNode modelNode = objectMapper.createObjectNode();
    modelNode.put("modelId", model.getId());
    modelNode.put("name", model.getName());
    modelNode.put("key", model.getKey());
    modelNode.put("description", model.getDescription());
    modelNode.putPOJO("lastUpdated", model.getLastUpdated());
    modelNode.put("lastUpdatedBy", model.getLastUpdatedBy());
    if (StringUtils.isNotEmpty(model.getModelEditorJson())) {
      try {
        ObjectNode editorJsonNode = (ObjectNode) objectMapper.readTree(model.getModelEditorJson());
        editorJsonNode.put("modelType", "model");
        modelNode.put("model", editorJsonNode);
      } catch (Exception e) {
        log.error("Error reading editor json " + modelId, e);
        throw new InternalServerErrorException("Error reading editor json " + modelId);
      }

    } else {
      ObjectNode editorJsonNode = objectMapper.createObjectNode();
      editorJsonNode.put("id", "canvas");
      editorJsonNode.put("resourceId", "canvas");
      ObjectNode stencilSetNode = objectMapper.createObjectNode();
      stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
      editorJsonNode.put("modelType", "model");
      modelNode.put("model", editorJsonNode);
    }
    return modelNode;
  }

  /**
   * POST /rest/models/{modelId}/editor/json -> save the JSON model
   */
  @RequestMapping(value = "/rest/models/{modelId}/editor/json", method = RequestMethod.POST)
  public ModelRepresentation saveModel(@PathVariable String modelId, @RequestBody MultiValueMap<String, String> values) {

    // Validation: see if there was another update in the meantime
    long lastUpdated = -1L;
    String lastUpdatedString = values.getFirst("lastUpdated");
    if (lastUpdatedString == null) {
      throw new BadRequestException("Missing lastUpdated date");
    }
    try {
      Date readValue = objectMapper.getDeserializationConfig().getDateFormat().parse(lastUpdatedString);
      lastUpdated = readValue.getTime();
    } catch (ParseException e) {
      throw new BadRequestException("Invalid lastUpdated date: '" + lastUpdatedString + "'");
    }

    Model model = modelService.getModel(modelId);
    User currentUser = SecurityUtils.getCurrentUserObject();
    boolean currentUserIsOwner = model.getLastUpdatedBy().equals(currentUser.getId());
    String resolveAction = values.getFirst("conflictResolveAction");

    // If timestamps differ, there is a conflict or a conflict has been resolved by the user
    if (model.getLastUpdated().getTime() != lastUpdated) {

      if (RESOLVE_ACTION_SAVE_AS.equals(resolveAction)) {

        String saveAs = values.getFirst("saveAs");
        String json = values.getFirst("json_xml");
        return createNewModel(saveAs, model.getDescription(), model.getModelType(), json);

      } else if (RESOLVE_ACTION_OVERWRITE.equals(resolveAction)) {
        return updateModel(model, values, false);
      } else if (RESOLVE_ACTION_NEW_VERSION.equals(resolveAction)) {
        return updateModel(model, values, true);
      } else {

        // Exception case: the user is the owner and selected to create a new version
        String isNewVersionString = values.getFirst("newversion");
        if (currentUserIsOwner && "true".equals(isNewVersionString)) {
          return updateModel(model, values, true);
        } else {
          // Tried everything, this is really a conflict, return 409
          ConflictingRequestException exception = new ConflictingRequestException("Process model was updated in the meantime");
          exception.addCustomData("userFullName", model.getLastUpdatedBy());
          exception.addCustomData("newVersionAllowed", currentUserIsOwner);
          throw exception;
        }
      }

    } else {

      // Actual, regular, update
      return updateModel(model, values, false);

    }
  }

  /**
   * POST /rest/models/{modelId}/editor/newversion -> create a new model version
   */
  @RequestMapping(value = "/rest/models/{modelId}/newversion", method = RequestMethod.POST)
  public ModelRepresentation importNewVersion(@PathVariable String modelId, @RequestParam("file") MultipartFile file) {
    return super.importNewVersion(modelId, file);
  }
  
  protected ModelRepresentation updateModel(Model model, MultiValueMap<String, String> values, boolean forceNewVersion) {

    String name = values.getFirst("name");
    String key = values.getFirst("key");
    String description = values.getFirst("description");
    String isNewVersionString = values.getFirst("newversion");
    String newVersionComment = null;
    
    ModelKeyRepresentation modelKeyInfo = modelService.validateModelKey(model, model.getModelType(), key);
    if (modelKeyInfo.isKeyAlreadyExists()) {
      throw new BadRequestException("Model with provided key already exists " + key);
    }

    boolean newVersion = false;
    if (forceNewVersion) {
      newVersion = true;
      newVersionComment = values.getFirst("comment");
    } else {
      if (isNewVersionString != null) {
        newVersion = "true".equals(isNewVersionString);
        newVersionComment = values.getFirst("comment");
      }
    }

    String json = values.getFirst("json_xml");

    try {
      model = modelService.saveModel(model.getId(), name, key, description, json, newVersion, 
          newVersionComment, SecurityUtils.getCurrentUserObject());
      return new ModelRepresentation(model);
      
    } catch (Exception e) {
      log.error("Error saving model " + model.getId(), e);
      throw new BadRequestException("Process model could not be saved " + model.getId());
    }
  }

  protected ModelRepresentation createNewModel(String name, String description, Integer modelType, String editorJson) {
    ModelRepresentation model = new ModelRepresentation();
    model.setName(name);
    model.setDescription(description);
    model.setModelType(modelType);
    Model newModel = modelService.createModel(model, editorJson, SecurityUtils.getCurrentUserObject());
    return new ModelRepresentation(newModel);
  }
}

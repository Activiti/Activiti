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

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.activiti.app.domain.editor.AbstractModel;
import org.activiti.app.domain.editor.AppDefinition;
import org.activiti.app.domain.editor.Model;
import org.activiti.app.model.common.ResultListDataRepresentation;
import org.activiti.app.model.editor.ModelKeyRepresentation;
import org.activiti.app.model.editor.ModelRepresentation;
import org.activiti.app.model.editor.decisiontable.DecisionTableDefinitionRepresentation;
import org.activiti.app.security.SecurityUtils;
import org.activiti.app.service.exception.BadRequestException;
import org.activiti.app.service.exception.InternalServerErrorException;
import org.activiti.form.model.FormDefinition;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@RestController
public class ModelsResource extends AbstractModelsResource {

  private final Logger logger = LoggerFactory.getLogger(ModelsResource.class);

  @RequestMapping(value = "/rest/models", method = RequestMethod.GET, produces = "application/json")
  public ResultListDataRepresentation getModels(@RequestParam(required = false) String filter, @RequestParam(required = false) String sort, @RequestParam(required = false) Integer modelType,
      HttpServletRequest request) {

    return super.getModels(filter, sort, modelType, request);
  }

  @RequestMapping(value = "/rest/models-for-app-definition", method = RequestMethod.GET, produces = "application/json")
  public ResultListDataRepresentation getModelsToIncludeInAppDefinition() {
    return super.getModelsToIncludeInAppDefinition();
  }

  @RequestMapping(value = "/rest/import-process-model", method = RequestMethod.POST, produces = "application/json")
  public ModelRepresentation importProcessModel(HttpServletRequest request, @RequestParam("file") MultipartFile file) {
    return super.importProcessModel(request, file);
  }

  /*
   * specific endpoint for IE9 flash upload component
   */
  @RequestMapping(value = "/rest/import-process-model/text", method = RequestMethod.POST)
  public String importProcessModelText(HttpServletRequest request, @RequestParam("file") MultipartFile file) {

    ModelRepresentation modelRepresentation = super.importProcessModel(request, file);
    String modelRepresentationJson = null;
    try {
      modelRepresentationJson = objectMapper.writeValueAsString(modelRepresentation);
    } catch (Exception e) {
      logger.error("Error while processing Model representation json", e);
      throw new InternalServerErrorException("Model Representation could not be saved");
    }

    return modelRepresentationJson;
  }

  @RequestMapping(value = "/rest/models", method = RequestMethod.POST, produces = "application/json")
  public ModelRepresentation createModel(@RequestBody ModelRepresentation modelRepresentation) {
    modelRepresentation.setKey(modelRepresentation.getKey().replaceAll(" ", ""));
    
    ModelKeyRepresentation modelKeyInfo = modelService.validateModelKey(null, modelRepresentation.getModelType(), modelRepresentation.getKey());
    if (modelKeyInfo.isKeyAlreadyExists()) {
      throw new BadRequestException("Provided model key already exists: " + modelRepresentation.getKey());
    }
    
    String json = null;
    if (modelRepresentation.getModelType() != null && modelRepresentation.getModelType().equals(AbstractModel.MODEL_TYPE_FORM)) {
      try {
        json = objectMapper.writeValueAsString(new FormDefinition());
      } catch (Exception e) {
        logger.error("Error creating form model", e);
        throw new InternalServerErrorException("Error creating form");
      }

    } else if (modelRepresentation.getModelType() != null && modelRepresentation.getModelType().equals(AbstractModel.MODEL_TYPE_DECISION_TABLE)) {
      try {
        DecisionTableDefinitionRepresentation decisionTableDefinition = new DecisionTableDefinitionRepresentation();

        String decisionTableDefinitionKey = modelRepresentation.getName().replaceAll(" ", "");
        decisionTableDefinition.setKey(decisionTableDefinitionKey);

        json = objectMapper.writeValueAsString(decisionTableDefinition);
      } catch (Exception e) {
        logger.error("Error creating decision table model", e);
        throw new InternalServerErrorException("Error creating decision table");
      }

    } else if (modelRepresentation.getModelType() != null && modelRepresentation.getModelType().equals(AbstractModel.MODEL_TYPE_APP)) {
      try {
        json = objectMapper.writeValueAsString(new AppDefinition());
      } catch (Exception e) {
        logger.error("Error creating app definition", e);
        throw new InternalServerErrorException("Error creating app definition");
      }

    } else {
      ObjectNode editorNode = objectMapper.createObjectNode();
      editorNode.put("id", "canvas");
      editorNode.put("resourceId", "canvas");
      ObjectNode stencilSetNode = objectMapper.createObjectNode();
      stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
      editorNode.put("stencilset", stencilSetNode);
      ObjectNode propertiesNode = objectMapper.createObjectNode();
      propertiesNode.put("process_id", modelRepresentation.getKey());
      propertiesNode.put("name", modelRepresentation.getName());
      if (StringUtils.isNotEmpty(modelRepresentation.getDescription())) {
        propertiesNode.put("documentation", modelRepresentation.getDescription());
      }
      editorNode.put("properties", propertiesNode);

      ArrayNode childShapeArray = objectMapper.createArrayNode();
      editorNode.put("childShapes", childShapeArray);
      ObjectNode childNode = objectMapper.createObjectNode();
      childShapeArray.add(childNode);
      ObjectNode boundsNode = objectMapper.createObjectNode();
      childNode.put("bounds", boundsNode);
      ObjectNode lowerRightNode = objectMapper.createObjectNode();
      boundsNode.put("lowerRight", lowerRightNode);
      lowerRightNode.put("x", 130);
      lowerRightNode.put("y", 193);
      ObjectNode upperLeftNode = objectMapper.createObjectNode();
      boundsNode.put("upperLeft", upperLeftNode);
      upperLeftNode.put("x", 100);
      upperLeftNode.put("y", 163);
      childNode.put("childShapes", objectMapper.createArrayNode());
      childNode.put("dockers", objectMapper.createArrayNode());
      childNode.put("outgoing", objectMapper.createArrayNode());
      childNode.put("resourceId", "startEvent1");
      ObjectNode stencilNode = objectMapper.createObjectNode();
      childNode.put("stencil", stencilNode);
      stencilNode.put("id", "StartNoneEvent");
      json = editorNode.toString();
    }

    Model newModel = modelService.createModel(modelRepresentation, json, SecurityUtils.getCurrentUserObject());
    return new ModelRepresentation(newModel);
  }

  @RequestMapping(value = "/rest/models/{modelId}/clone", method = RequestMethod.POST, produces = "application/json")
  public ModelRepresentation duplicateModel(@PathVariable String modelId, @RequestBody ModelRepresentation modelRepresentation) {

    String json = null;
    Model model = null;
    if (modelId != null) {
      model = modelService.getModel(modelId);
      json = model.getModelEditorJson();
    }

    if (model == null) {
      throw new InternalServerErrorException("Error duplicating model : Unknown original model");
    }

    if (modelRepresentation.getModelType() != null && modelRepresentation.getModelType().equals(AbstractModel.MODEL_TYPE_FORM)) {
    	// nothing to do special for forms (just clone the json)
    } else if (modelRepresentation.getModelType() != null && modelRepresentation.getModelType().equals(AbstractModel.MODEL_TYPE_APP)) {
    	// nothing to do special for applications (just clone the json)
    }  else if (modelRepresentation.getModelType() != null && modelRepresentation.getModelType().equals(AbstractModel.MODEL_TYPE_DECISION_TABLE)) {
    	// Decision Table model
    	ObjectNode editorNode = null;

    	try {

    		editorNode = (ObjectNode) objectMapper.readTree(json);

    		json = objectMapper.writeValueAsString(editorNode);


    	} catch (Exception e) {
    		logger.error("Error creating decision table model", e);
    		throw new InternalServerErrorException("Error creating decision table");
    	}


    }else {
    	// BPMN model
    	ObjectNode editorNode = null;
    	try {
        ObjectNode editorJsonNode = (ObjectNode) objectMapper.readTree(json);

        editorNode = deleteEmbededReferencesFromBPMNModel(editorJsonNode);

        ObjectNode propertiesNode = (ObjectNode) editorNode.get("properties");
        String processId = modelRepresentation.getName().replaceAll(" ", "");
        propertiesNode.put("process_id", processId);
        propertiesNode.put("name", modelRepresentation.getName());
        if (StringUtils.isNotEmpty(modelRepresentation.getDescription())) {
          propertiesNode.put("documentation", modelRepresentation.getDescription());
        }
        editorNode.put("properties", propertiesNode);

      } catch (IOException e) {
        e.printStackTrace();
      }

      if (editorNode != null) {
        json = editorNode.toString();
      }
    }

    // create the new model
    Model newModel = modelService.createModel(modelRepresentation, json, SecurityUtils.getCurrentUserObject());

    // copy also the thumbnail
    byte[] imageBytes = model.getThumbnail();
    newModel = modelService.saveModel(newModel, newModel.getModelEditorJson(), imageBytes, false, newModel.getComment(), SecurityUtils.getCurrentUserObject());

    return new ModelRepresentation(newModel);
  }

  protected ObjectNode deleteEmbededReferencesFromBPMNModel(ObjectNode editorJsonNode) {
    try {
      internalDeleteNodeByNameFromBPMNModel(editorJsonNode, "formreference");
      internalDeleteNodeByNameFromBPMNModel(editorJsonNode, "subprocessreference");
      return editorJsonNode;
    } catch (Exception e) {
      throw new InternalServerErrorException("Cannot delete the external references");
    }
  }

  protected ObjectNode deleteEmbededReferencesFromStepModel(ObjectNode editorJsonNode) {
    try {
      JsonNode startFormNode = editorJsonNode.get("startForm");
      if (startFormNode != null) {
        editorJsonNode.remove("startForm");
      }
      internalDeleteNodeByNameFromStepModel(editorJsonNode.get("steps"), "formDefinition");
      internalDeleteNodeByNameFromStepModel(editorJsonNode.get("steps"), "subProcessDefinition");
      return editorJsonNode;
    } catch (Exception e) {
      throw new InternalServerErrorException("Cannot delete the external references");
    }
  }

  protected void internalDeleteNodeByNameFromBPMNModel(JsonNode editorJsonNode, String propertyName) {
    JsonNode childShapesNode = editorJsonNode.get("childShapes");
    if (childShapesNode != null && childShapesNode.isArray()) {
      ArrayNode childShapesArrayNode = (ArrayNode) childShapesNode;
      for (JsonNode childShapeNode : childShapesArrayNode) {
        // Properties
        ObjectNode properties = (ObjectNode) childShapeNode.get("properties");
        if (properties != null && properties.has(propertyName)) {
          JsonNode propertyNode = properties.get(propertyName);
          if (propertyNode != null) {
            properties.remove(propertyName);
          }
        }

        // Potential nested child shapes
        if (childShapeNode.has("childShapes")) {
          internalDeleteNodeByNameFromBPMNModel(childShapeNode, propertyName);
        }

      }
    }
  }

  private void internalDeleteNodeByNameFromStepModel(JsonNode stepsNode, String propertyName) {

    if (stepsNode == null || !stepsNode.isArray()) {
      return;
    }

    for (JsonNode jsonNode : stepsNode) {

      ObjectNode stepNode = (ObjectNode) jsonNode;
      if (stepNode.has(propertyName)) {
        JsonNode propertyNode = stepNode.get(propertyName);
        if (propertyNode != null) {
          stepNode.remove(propertyName);
        }
      }

      // Nested steps
      if (stepNode.has("steps")) {
        internalDeleteNodeByNameFromStepModel(stepNode.get("steps"), propertyName);
      }

      // Overdue steps
      if (stepNode.has("overdueSteps")) {
        internalDeleteNodeByNameFromStepModel(stepNode.get("overdueSteps"), propertyName);
      }

      // Choices is special, can have nested steps inside
      if (stepNode.has("choices")) {
        ArrayNode choicesArrayNode = (ArrayNode) stepNode.get("choices");
        for (JsonNode choiceNode : choicesArrayNode) {
          if (choiceNode.has("steps")) {
            internalDeleteNodeByNameFromStepModel(choiceNode.get("steps"), propertyName);
          }
        }
      }
    }
  }

}

/**
 * Activiti app component part of the Activiti project
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.activiti.rest.editor;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

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

import com.activiti.domain.editor.AbstractModel;
import com.activiti.domain.editor.AppDefinition;
import com.activiti.domain.editor.Model;
import com.activiti.model.common.ResultListDataRepresentation;
import com.activiti.model.editor.ModelRepresentation;
import com.activiti.model.editor.form.FormDefinitionRepresentation;
import com.activiti.security.SecurityUtils;
import com.activiti.service.exception.InternalServerErrorException;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@RestController
public class ModelsResource extends AbstractModelsResource {

    private final Logger logger = LoggerFactory.getLogger(ModelsResource.class);

    @RequestMapping(value = "/rest/models", method = RequestMethod.GET, produces = "application/json")
    @Timed
    public ResultListDataRepresentation getModels(
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer modelType,
            HttpServletRequest request) {

        return super.getModels(filter, sort, modelType, request);
    }

    @RequestMapping(value = "/rest/models-for-app-definition", method = RequestMethod.GET, produces = "application/json")
    @Timed
    public ResultListDataRepresentation getModelsToIncludeInAppDefinition() {
        return super.getModelsToIncludeInAppDefinition();
    }

    @RequestMapping(value = "/rest/import-process-model",
            method = RequestMethod.POST,
            produces = "application/json")
    @Timed
    public ModelRepresentation importProcessModel(HttpServletRequest request, @RequestParam("file") MultipartFile file) {
        return super.importProcessModel(request, file);
    }

    /*
     * specific endpoint for IE9 flash upload component
     */
    @RequestMapping(value = "/rest/import-process-model/text",
            method = RequestMethod.POST)
    @Timed
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

    @RequestMapping(value = "/rest/models",
            method = RequestMethod.POST,
            produces = "application/json")
    @Timed
    public ModelRepresentation createModel(@RequestBody ModelRepresentation modelRepresentation) {
        String json = null;
        if (modelRepresentation.getModelType() != null && modelRepresentation.getModelType().equals(AbstractModel.MODEL_TYPE_FORM)) {
            try {
                json = objectMapper.writeValueAsString(new FormDefinitionRepresentation());
            } catch (Exception e) {
                logger.error("Error creating form model", e);
                throw new InternalServerErrorException("Error creating form");
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
            String processId = modelRepresentation.getName().replaceAll(" ", "");
            propertiesNode.put("process_id", processId);
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

    @RequestMapping(value = "/rest/models/{modelId}/clone",
            method = RequestMethod.POST,
            produces = "application/json")
    @Timed
    public ModelRepresentation duplicateModel(@PathVariable Long modelId, @RequestBody ModelRepresentation modelRepresentation) {

        String json = null;
        Model model = null;
        if (modelId != null) {
            model = getModel(modelId, true, false);
            json = model.getModelEditorJson();
        }

        if (model == null) {
            throw new InternalServerErrorException("Error duplicating model : Unknown original model");
        }

        if (modelRepresentation.getModelType() != null && modelRepresentation.getModelType().equals(AbstractModel.MODEL_TYPE_FORM)) {
            //noting to do special for forms (just clone the json)
        } else if (modelRepresentation.getModelType() != null && modelRepresentation.getModelType().equals(AbstractModel.MODEL_TYPE_APP)) {
            //noting to do special for applications (just clone the json)
        } else {
            //BPMN model
            ObjectNode editorNode = null;
            try {
                ObjectNode editorJsonNode = (ObjectNode) objectMapper.readTree(json);

                editorNode = deleteEmbededReferencesFromBPMNModel(editorJsonNode);

                ObjectNode propertiesNode = (ObjectNode) editorNode.get("properties");
                String processId = model.getName().replaceAll(" ", "");
                propertiesNode.put("process_id", processId);
                propertiesNode.put("name", model.getName());
                if (StringUtils.isNotEmpty(model.getDescription())) {
                    propertiesNode.put("documentation", model.getDescription());
                }
                editorNode.put("properties", propertiesNode);

            } catch (IOException e) {
                e.printStackTrace();
            }

            if (editorNode != null) {
                json = editorNode.toString();
            }
        }

        //create the new model
        Model newModel = modelService.createModel(modelRepresentation, json, SecurityUtils.getCurrentUserObject());

        //copy also the thumbnail
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

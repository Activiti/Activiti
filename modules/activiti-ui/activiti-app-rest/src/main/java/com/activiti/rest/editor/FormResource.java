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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.activiti.editor.language.json.converter.util.CollectionUtils;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.activiti.domain.editor.AbstractModel;
import com.activiti.domain.editor.Model;
import com.activiti.domain.editor.ModelHistory;
import com.activiti.domain.editor.ModelShareInfo;
import com.activiti.domain.editor.SharePermission;
import com.activiti.domain.idm.User;
import com.activiti.model.editor.FormSaveRepresentation;
import com.activiti.model.editor.form.FormDefinitionRepresentation;
import com.activiti.model.editor.form.FormRepresentation;
import com.activiti.security.SecurityUtils;
import com.activiti.service.editor.ModelInternalService;
import com.activiti.service.exception.BadRequestException;
import com.activiti.service.exception.InternalServerErrorException;
import com.activiti.service.exception.NotFoundException;
import com.activiti.service.exception.NotPermittedException;
import com.activiti.util.UserUtil;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Tijs Rademakers
 */
@RestController
@RequestMapping("/rest/form-models")
public class FormResource extends BaseModelResource {

    private static final Logger logger = LoggerFactory.getLogger(FormResource.class);

    @Inject
    protected ModelInternalService modelService;

    protected ObjectMapper objectMapper = new ObjectMapper();

    @Timed
    @RequestMapping(value = "/{formId}", method = RequestMethod.GET, produces = "application/json")
    public FormRepresentation getForm(@PathVariable Long formId) {
        Model model = getFormModel(formId, true, false);
        FormRepresentation form = createFormRepresentation(model);
        return form;
    }

    @Timed
    @RequestMapping(value = "/values", method = RequestMethod.GET, produces = "application/json")
    public List<FormRepresentation> getForms(HttpServletRequest request) {
        List<FormRepresentation> formRepresentations = new ArrayList<FormRepresentation>();
        
        String[] formIds = request.getParameterValues("formId");
        
        if (formIds == null || formIds.length == 0) {
            throw new BadRequestException("No formIds provided in the request");
        }
        

        for (String formId : formIds) {
            Model model = getFormModel(Long.valueOf(formId), true, false);

            FormRepresentation form = createFormRepresentation(model);
            formRepresentations.add(form);
        }

        return formRepresentations;
    }

    @RequestMapping(value = "/{formId}/history/{formHistoryId}", method = RequestMethod.GET, produces = "application/json")
    @Timed
    public FormRepresentation getFormHistory(@PathVariable Long formId, @PathVariable Long formHistoryId) {
        ModelHistory model = getFormModelHistory(formId, formHistoryId, true, false);
        FormRepresentation form = createFormRepresentation(model);
        return form;
    }

    @Timed
    @RequestMapping(value = "/{formId}", method = RequestMethod.PUT, produces = "application/json")
    public FormRepresentation saveForm(@PathVariable Long formId, @RequestBody FormSaveRepresentation saveRepresentation) {

        User user = SecurityUtils.getCurrentUserObject();
        Model model = getFormModel(formId, true, true);

        model.setName(saveRepresentation.getFormRepresentation().getName());
        model.setDescription(saveRepresentation.getFormRepresentation().getDescription());

        String editorJson = null;
        try {
            editorJson = objectMapper.writeValueAsString(saveRepresentation.getFormRepresentation().getFormDefinition());
        } catch (Exception e) {
            logger.error("Error while processing form json", e);
            throw new InternalServerErrorException("Form could not be saved " + formId);
        }

        String filteredImageString = saveRepresentation.getFormImageBase64().replace("data:image/png;base64,", "");
        byte[] imageBytes = Base64.decodeBase64(filteredImageString);
        model = modelService.saveModel(model, editorJson, imageBytes, saveRepresentation.isNewVersion(), saveRepresentation.getComment(), user);
        FormRepresentation result = new FormRepresentation(model);
        result.setFormDefinition(saveRepresentation.getFormRepresentation().getFormDefinition());
        return result;
    }

    protected FormRepresentation createFormRepresentation(AbstractModel model) {
        FormDefinitionRepresentation formDefinitionRepresentation = null;
        try {
            formDefinitionRepresentation = objectMapper.readValue(model.getModelEditorJson(), FormDefinitionRepresentation.class);
        } catch (Exception e) {
            logger.error("Error deserializing form", e);
            throw new InternalServerErrorException("Could not deserialize form definition");
        }
        FormRepresentation result = new FormRepresentation(model);
        result.setFormDefinition(formDefinitionRepresentation);
        return result;
    }

    protected Model getFormModel(Long modelId, boolean checkRead, boolean checkEdit) {
        Model model = modelRepository.findOne(modelId);

        if (model == null) {
            NotFoundException processNotFound = new NotFoundException("No model found with the given id: " + modelId);
            processNotFound.setMessageKey(PROCESS_NOT_FOUND_MESSAGE_KEY);
            throw processNotFound;
        }

        User currentUser = SecurityUtils.getCurrentUserObject();

        // Owner can always read/write a model
        boolean isOwner = model.getCreatedBy().equals(currentUser);

        if (!isOwner) {

            Model validateModel = null;
            // check parent model if there is a model reference
            if (model.getReferenceId() != null) {
                validateModel = getParentModel(model.getReferenceId());
            } else {
                validateModel = model;
            }

            // Check if model is shared with the current user
            List<ModelShareInfo> shareInfoList = null;
            List<Long> groupIds = UserUtil.getGroupIds(currentUser);
            if (CollectionUtils.isNotEmpty(groupIds)) {
                shareInfoList = shareInfoRepository.findByModelIdWithUserIdOrGroups(validateModel.getId(), currentUser.getId(), groupIds);
            } else {
                shareInfoList = shareInfoRepository.findByModelIdWithUserId(validateModel.getId(), currentUser.getId());
            }

            if (checkRead) {
                if (CollectionUtils.isEmpty(shareInfoList)) {
                    throw new NotPermittedException("You are not permitted to access this process model");
                }
            }

            if (checkEdit) {
                if (CollectionUtils.isEmpty(shareInfoList)) {
                    throw new NotPermittedException("You are not permitted to access this process model");
                } else {
                    boolean hasWritePermission = false;
                    for (ModelShareInfo modelShareInfo : shareInfoList) {
                        if (modelShareInfo.getPermission() == SharePermission.WRITE) {
                            hasWritePermission = true;
                            break;
                        }
                    }

                    if (hasWritePermission == false) {
                        throw new NotPermittedException("You are not permitted to modify this process model");
                    }
                }
            }
        }

        return model;
    }

    protected ModelHistory getFormModelHistory(Long modelId, Long modelHistoryId, boolean checkRead, boolean checkEdit) {
        // Check if the user has read-rights on the process-model in order to
        // fetch history
        Model model = getFormModel(modelId, checkRead, checkEdit);
        ModelHistory modelHistory = historyRepository.findOne(modelHistoryId);

        // Check if history corresponds to the current model and is not deleted
        if (modelHistory == null || modelHistory.getRemovalDate() != null || !modelHistory.getModelId().equals(model.getId())) {
            throw new NotFoundException("Process model history not found: " + modelHistoryId);
        }
        return modelHistory;
    }

    protected Model getParentModel(Long parentModelId) {
        Model model = modelRepository.findOne(parentModelId);
        if (model.getReferenceId() != null) {
            return getParentModel(model.getReferenceId());
        }
        return model;
    }
}

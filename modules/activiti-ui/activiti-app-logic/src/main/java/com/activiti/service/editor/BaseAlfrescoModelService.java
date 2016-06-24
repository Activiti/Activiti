/**
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.service.editor;

import java.util.List;

import org.activiti.editor.language.json.converter.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.activiti.domain.editor.Model;
import com.activiti.domain.editor.ModelHistory;
import com.activiti.domain.editor.ModelShareInfo;
import com.activiti.domain.editor.SharePermission;
import com.activiti.domain.idm.User;
import com.activiti.model.editor.ModelRepresentation;
import com.activiti.repository.editor.ModelHistoryRepository;
import com.activiti.repository.editor.ModelRepository;
import com.activiti.repository.editor.ModelShareInfoRepository;
import com.activiti.security.SecurityUtils;
import com.activiti.service.exception.NotFoundException;
import com.activiti.service.exception.NotPermittedException;
import com.activiti.util.UserUtil;

@Service
public class BaseAlfrescoModelService {
    
    private static final Logger logger = LoggerFactory.getLogger(BaseAlfrescoModelService.class);
    
    protected static final String PROCESS_NOT_FOUND_MESSAGE_KEY = "PROCESS.ERROR.NOT-FOUND";
    
    @Autowired
    protected ModelRepository modelRepository;
    
    @Autowired
    protected ModelHistoryRepository modelHistoryRepository;
    
    @Autowired
    protected ModelShareInfoRepository shareInfoRepository;
    
    protected Model getModel(Long modelId, boolean checkRead, boolean checkEdit) {
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
                    throw new NotPermittedException("You are not permitted to access this model");
                }
            }

            if (checkEdit) {
                if (CollectionUtils.isEmpty(shareInfoList)) {
                    throw new NotPermittedException("You are not permitted to access this model");
                } else {
                    boolean hasWritePermission = false;
                    for (ModelShareInfo modelShareInfo : shareInfoList) {
                        if (modelShareInfo.getPermission() == SharePermission.WRITE) {
                            hasWritePermission = true;
                            break;
                        }
                    }

                    if (hasWritePermission == false) {
                        throw new NotPermittedException("You are not permitted to modify this model");
                    }
                }
            }
        }

        return model;
    }
    
    protected ModelHistory getModelHistory(Long modelId, Long modelHistoryId, boolean checkRead, boolean checkEdit) {
        // Check if the user has read-rights on the process-model in order to fetch history
        Model model = getModel(modelId, checkRead, checkEdit);
        ModelHistory modelHistory = modelHistoryRepository.findOne(modelHistoryId);
        
        // Check if history corresponds to the current model and is not deleted
        if (modelHistory== null || modelHistory.getRemovalDate() != null || !modelHistory.getModelId().equals(model.getId())) {
            throw new NotFoundException("Model history not found: " + modelHistoryId);
        }
        return modelHistory;
    }
    
    protected void populatePermissions(Model model, ModelRepresentation response) {
        User currentUser = SecurityUtils.getCurrentUserObject();
        if (model.getCreatedBy().equals(currentUser)) {
            response.setSharePermission(SharePermission.WRITE);
        } else {
            
            List<ModelShareInfo> shareInfoList = null;
            List<Long> groupIds = UserUtil.getGroupIds(currentUser);
            if (CollectionUtils.isNotEmpty(groupIds)) {
                shareInfoList = shareInfoRepository.findByModelIdWithUserIdOrGroups(model.getId(), currentUser.getId(), groupIds);
            } else {
                shareInfoList = shareInfoRepository.findByModelIdWithUserId(model.getId(), currentUser.getId());
            }
            
            if (CollectionUtils.isNotEmpty(shareInfoList)) {
                for (ModelShareInfo modelShareInfo : shareInfoList) {
                    response.setSharePermission(modelShareInfo.getPermission());
                    if (modelShareInfo.getPermission() == SharePermission.WRITE) {
                        break;
                    }
                }
            }
            
        }
    }

    protected void populatePermissions(Long modelId, ModelRepresentation response) {
        Model model = getModel(modelId, true, false);
        populatePermissions(model, response);
    }
    
    protected Model getParentModel(Long parentModelId) {
        Model model = modelRepository.findOne(parentModelId);
        if (model.getReferenceId() != null) {
            return getParentModel(model.getReferenceId());
        }
        return model;
    }

}

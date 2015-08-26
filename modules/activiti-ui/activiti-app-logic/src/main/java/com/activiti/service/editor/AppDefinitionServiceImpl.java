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
package com.activiti.service.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.activiti.domain.editor.AbstractModel;
import com.activiti.domain.editor.AppDefinition;
import com.activiti.domain.editor.AppModelDefinition;
import com.activiti.domain.editor.Model;
import com.activiti.domain.editor.ModelHistory;
import com.activiti.domain.editor.ModelShareInfo;
import com.activiti.domain.idm.User;
import com.activiti.repository.editor.ModelHistoryRepository;
import com.activiti.repository.editor.ModelRepository;
import com.activiti.security.SecurityUtils;
import com.activiti.service.api.AppDefinitionService;
import com.activiti.service.api.AppDefinitionServiceRepresentation;
import com.activiti.service.exception.InternalServerErrorException;
import com.activiti.util.UserUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Transactional
public class AppDefinitionServiceImpl implements AppDefinitionService {
	
	private final Logger logger = LoggerFactory.getLogger(AppDefinitionServiceImpl.class);
	
	@Autowired
	protected ModelRepository modelRepository;
	
	@Autowired
	protected ModelHistoryRepository modelHistoryRepository;
	
	@Autowired
	protected ObjectMapper objectMapper;
	
	@Override
	public List<AppDefinitionServiceRepresentation> getAppDefinitions() {
	    Map<Long, AbstractModel> modelMap = new HashMap<Long, AbstractModel>();
		List<AppDefinitionServiceRepresentation> resultList = new ArrayList<AppDefinitionServiceRepresentation>();
		
		User user = SecurityUtils.getCurrentUserObject();
		List<Model> createdByModels = modelRepository.findModelsCreatedBy(user.getId(), AbstractModel.MODEL_TYPE_APP, new Sort(Direction.ASC, "name"));
		for (Model model : createdByModels) {
		    modelMap.put(model.getId(), model);
        }
		
		List<ModelShareInfo> sharedWith = modelRepository.findModelsSharedWithUser(user.getId(), Model.MODEL_TYPE_APP, new Sort(Direction.ASC, "name"));
		
        for (ModelShareInfo info : sharedWith) {
            modelMap.put(info.getModel().getId(), info.getModel());
        }
        
		for (AbstractModel model : modelMap.values()) {
	        resultList.add(createAppDefinition(model));
		}
		
		return resultList;
	}
	
	/**
	 * Gathers all 'deployable' app definitions for the current user.
	 * 
	 * To find these:
	 * - All historical app models are fetched. Only the highest version of each app model is retained.
	 * - All historical app models shared with the groups the current user is part of are fetched. 
	 *   Only the highest version of each app model is retained.
	 */
	@Override
    public List<AppDefinitionServiceRepresentation> getDeployableAppDefinitions(User user) {
        Map<Long, ModelHistory> modelMap = new HashMap<Long, ModelHistory>();
        List<AppDefinitionServiceRepresentation> resultList = new ArrayList<AppDefinitionServiceRepresentation>();
        
        List<ModelHistory> createdByModels = modelHistoryRepository.findByCreatedByAndModelTypeAndRemovalDateIsNull(user, AbstractModel.MODEL_TYPE_APP);
        for (ModelHistory modelHistory : createdByModels) {
            if (modelMap.containsKey(modelHistory.getModelId())) {
                if (modelHistory.getVersion() > modelMap.get(modelHistory.getModelId()).getVersion()) {
                    modelMap.put(modelHistory.getModelId(), modelHistory);
                }
            } else {
                modelMap.put(modelHistory.getModelId(), modelHistory);
            }
        }
        
        List<Long> groupIds = UserUtil.getGroupIds(user);
        List<ModelHistory> historyList = null;
        if (groupIds != null && groupIds.size() > 0) {
        	historyList = modelHistoryRepository.findModelsSharedWithUserOrGroups(user.getId(), groupIds, Model.MODEL_TYPE_APP, new Sort(Direction.ASC, "name"));
        } else {
        	historyList = modelHistoryRepository.findModelsSharedWithUser(user.getId(), Model.MODEL_TYPE_APP, new Sort(Direction.ASC, "name"));
        }
        
        for (ModelHistory modelHistory : historyList) {
            if (modelMap.containsKey(modelHistory.getModelId())) {
                if (modelHistory.getVersion() > modelMap.get(modelHistory.getModelId()).getVersion()) {
                    modelMap.put(modelHistory.getModelId(), modelHistory);
                }
            } else {
                modelMap.put(modelHistory.getModelId(), modelHistory);
            }
        }
        
        for (ModelHistory model : modelMap.values()) {
            Model latestModel = modelRepository.findOne(model.getModelId());
            if (latestModel != null) {
                resultList.add(createAppDefinition(model));
            }
        }
        
        return resultList;
    }
	
	protected AppDefinitionServiceRepresentation createAppDefinition(AbstractModel model) {
	    AppDefinitionServiceRepresentation resultInfo = new AppDefinitionServiceRepresentation();
	    if (model instanceof ModelHistory) {
	        resultInfo.setId(((ModelHistory) model).getModelId());
	    } else {
	        resultInfo.setId(model.getId());
	    }
        resultInfo.setName(model.getName());
        resultInfo.setDescription(model.getDescription());
        resultInfo.setVersion(model.getVersion());
        resultInfo.setDefinition(model.getModelEditorJson());
        
        AppDefinition appDefinition = null;
        try {
            appDefinition = objectMapper.readValue(model.getModelEditorJson(), AppDefinition.class);
        } catch (Exception e) {
            logger.error("Error deserializing app " + model.getId(), e);
            throw new InternalServerErrorException("Could not deserialize app definition");
        }
        
        if (appDefinition != null) {
            resultInfo.setTheme(appDefinition.getTheme());
            resultInfo.setIcon(appDefinition.getIcon());
            List<AppModelDefinition> models = appDefinition.getModels();
            if (CollectionUtils.isNotEmpty(models)) {
                List<Long> modelIds = new ArrayList<Long>();
                for (AppModelDefinition appModelDef : models) {
                    modelIds.add(appModelDef.getId());
                }
                resultInfo.setModels(modelIds);
            }
        }
        return resultInfo;
	}
	
}

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.language.json.converter.util.JsonConverterUtil;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.activiti.domain.editor.AbstractModel;
import com.activiti.domain.editor.AppDefinition;
import com.activiti.domain.editor.AppModelDefinition;
import com.activiti.domain.editor.Model;
import com.activiti.domain.editor.ModelHistory;
import com.activiti.domain.editor.ModelRelation;
import com.activiti.domain.editor.ModelRelationTypes;
import com.activiti.domain.idm.User;
import com.activiti.domain.runtime.AppRelation;
import com.activiti.domain.runtime.RuntimeAppDefinition;
import com.activiti.domain.runtime.RuntimeAppDeployment;
import com.activiti.repository.editor.ModelHistoryRepository;
import com.activiti.repository.editor.ModelRelationRepository;
import com.activiti.repository.runtime.AppRelationRepository;
import com.activiti.repository.runtime.RuntimeAppDefinitionRepository;
import com.activiti.repository.runtime.RuntimeAppDeploymentRepository;
import com.activiti.security.SecurityUtils;
import com.activiti.service.api.AppDefinitionService;
import com.activiti.service.api.DeploymentResult;
import com.activiti.service.api.DeploymentService;
import com.activiti.service.api.ModelService;
import com.activiti.service.exception.BadRequestException;
import com.activiti.service.exception.ConflictingRequestException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Can't merge this with {@link AppDefinitionService}, as it doesn't have visibility of 
 * domain models needed to do the publication.
 * 
 * @author jbarrez
 */
@Service
@Transactional
public class AppDefinitionPublishService {
	
	private static final Logger logger = LoggerFactory.getLogger(AppDefinitionPublishService.class);
	
	@Autowired
	protected ModelInternalService modelInternalService;
	
	@Autowired
	protected ModelService modelService;
	
	@Autowired
	protected ModelHistoryRepository modelHistoryRepository;
	
	@Autowired
	protected ModelRelationRepository modelRelationRepository;
	
	@Autowired
	protected DeploymentService deploymentService;
	
	@Autowired
	protected AppRelationRepository appRelationRepository;
	
	@Autowired
	protected RepositoryService repositoryService;
	
	@Autowired
	protected RuntimeAppDeploymentRepository runtimeAppDeploymentRepository;
	
	@Autowired
	protected RuntimeAppDefinitionRepository runtimeAppDefinitionRepository;
	
	@Autowired
	protected ObjectMapper objectMapper;
	
	public void publishAppDefinition(Long modelId, String comment, Model appDefinitionModel,
			Long appDefinitionId, String appDefinitionName,  String appDefinitionDescription, String appDefinitionJson, 
			User user, Boolean force) {
		
		// First we have to check if the model we have can be deployed
		validateAppModel(modelId, appDefinitionModel, appDefinitionId, appDefinitionJson, user, force);
		
		// Create new version of the app model
        modelInternalService.createNewModelVersion(appDefinitionModel, comment, user);
        
        // Deploy the app model to be executable
        DeploymentResult deploymentResult = deploymentService.updateAppDefinition(
        		appDefinitionId, appDefinitionName, appDefinitionDescription, appDefinitionJson, user);
        
        // Create model history version of all related models
        handleAppRelations(deploymentResult, appDefinitionId, appDefinitionDescription, appDefinitionJson);
    }
	
	protected void validateAppModel(Long appModelId, Model appDefinitionModel, Long appDefinitionId, String appDefinitionJson, User user,Boolean force) {
		List<ModelRelation> modelRelations = modelRelationRepository.findByParentModelId(appModelId);
		validateProcessDefinitionKeys(user, modelRelations);
		
		// Force == true -> user has confirmed it's ok
		if (force == null || force.equals(Boolean.FALSE)) {
			validateModelUsage(appModelId, modelRelations);
		}
	}

	private void validateProcessDefinitionKeys(User user, List<ModelRelation> modelRelations) {
		
		Map<String, AbstractModel> allProcessDefinitionKeys = new HashMap<String, AbstractModel>();
		List<Map<String, String>> duplicateProcessDefinitionKeys = new ArrayList<Map<String, String>>();
		
	    // Issue #500 : Users should not be able to deploy processes with same process definition key in a tenant
		// We need to check if the process model ids being used here are unique for the tenant they'll be used in
		
		for (ModelRelation modelRelation : modelRelations) {
			if (modelRelation.getType().equals(ModelRelationTypes.TYPE_PROCESS_MODEL)) {
				
				AbstractModel relatedProcessModel = modelService.getModel(modelRelation.getModelId());
				List<String> processDefinitionKeys = getProcessDefinitionKeys(user, relatedProcessModel);
				
				// Get process definitions with same key
				for (String processDefinitionKey : processDefinitionKeys) {
					
					if (!allProcessDefinitionKeys.containsKey(processDefinitionKey)) {
						allProcessDefinitionKeys.put(processDefinitionKey, relatedProcessModel);
					} else {
						duplicateProcessDefinitionKeys.add(Collections.singletonMap(processDefinitionKey, allProcessDefinitionKeys.get(processDefinitionKey).getName()));
						duplicateProcessDefinitionKeys.add(Collections.singletonMap(processDefinitionKey, relatedProcessModel.getName()));
					}
					
					// Query should only return 1 result (otherwise the validation that fails has failed)
					List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
							.processDefinitionKey(processDefinitionKey)
							.latestVersion() // only need latest version, assuming previous ones went ok and passed this validation
							.list();
					
					// So now we've got the process definitions that share the same key as the one we're trying to publish.
					// We need to check through the deployment, which app model it is related to, and through that
					// which process models it depends on. If it's the same model, all good. If not ... there is a conflict!
					for (ProcessDefinition processDefinition : processDefinitions) {
						
						RuntimeAppDeployment runtimeAppDeployment = runtimeAppDeploymentRepository
								.findByDeploymentId(processDefinition.getDeploymentId());
						List<AppRelation> processRelations = appRelationRepository
								.findByRuntimeAppIdAndType(runtimeAppDeployment.getId(), ModelRelationTypes.TYPE_PROCESS_MODEL);
						
						for (AppRelation appRelation : processRelations) {
							
							if (getProcessDefinitionKeys(appRelation).contains(processDefinitionKey)) {
								
								ModelHistory conflictingModel = modelHistoryRepository.findOne(appRelation.getModelId());
								if (!conflictingModel.getModelId().equals(relatedProcessModel.getId())) {
								
									Map<String, Object> customData = new HashMap<String, Object>();
									customData.put("modelInAppName", relatedProcessModel.getName());
									customData.put("conflictingModelName", conflictingModel.getName());
									customData.put("conflictingAppName", runtimeAppDeployment.getAppDefinition().getName());
									customData.put("processDefinitionKey", processDefinitionKey);
									throw new ConflictingRequestException("Process definition with key " 
											+ processDefinitionKey + " already deployed", "app.publish.procdef.key.conflict", customData);
								}
								
							}
							
						}
						
					}
					
				}
				
			}
		}
		
		
		// Check for duplicate ids in the same deployment (will give Activiti exception otherwise)
		if (duplicateProcessDefinitionKeys.size() > 0) {
			Map<String, Object> customData = new HashMap<String, Object>();
			customData.put("duplicateProcessDefinitionIds", duplicateProcessDefinitionKeys);
			throw new ConflictingRequestException("Duplicate process definition keys found", "app.publish.procdef.duplicate.keys", customData);
		}
    }
	

	private void validateModelUsage(Long appModelId, List<ModelRelation> modelRelations) {
		
	    // Other validation check: is the provided model used 
		// Needs to be done AFTER previous check for duplicate process definition keys has been done
		
		// Will be non-null if the is > first version of the app being deployed
		RuntimeAppDefinition currentRuntimeAppDefinition = runtimeAppDefinitionRepository.findByModelId(appModelId);
		
		List<Pair<AbstractModel, RuntimeAppDefinition>> conflicts = new ArrayList<Pair<AbstractModel,RuntimeAppDefinition>>();
		for (ModelRelation modelRelation : modelRelations) {
			if (modelRelation.getType().equals(ModelRelationTypes.TYPE_PROCESS_MODEL)) {
				
				Long processModelId = modelRelation.getModelId();
				List<AppRelation> appRelations = appRelationRepository.findByRuntimeModelId(processModelId);
				for (AppRelation appRelation : appRelations) {
					RuntimeAppDefinition runtimeAppDefinition = appRelation.getRuntimeAppDeployment().getAppDefinition();
					if (currentRuntimeAppDefinition == null || !currentRuntimeAppDefinition.getId().equals(runtimeAppDefinition.getId())) {
						AbstractModel model = modelService.getModel(processModelId);
						conflicts.add(Pair.of(model, runtimeAppDefinition));
					}
				}
				
			}
		}
		
		if (conflicts.size() > 0) {
			Map<String, Object> customData = new HashMap<String, Object>();
			
			List<String> processModelNames = new ArrayList<String>(conflicts.size());
			List<String> appNames = new ArrayList<String>(conflicts.size());
			
			for (Pair<AbstractModel, RuntimeAppDefinition> conflict : conflicts) {
				
				String processModelName = conflict.getLeft().getName();
				String appName = conflict.getRight().getName();
				
				boolean found = false;
				for (int i=0; i<processModelNames.size(); i++) {
					
					String existingProcessModelName = processModelNames.get(i);
					String existingAppName = appNames.get(i);
					
					if (StringUtils.equals(processModelName, existingProcessModelName)
							&& StringUtils.equals(appName, existingAppName)) {
						found = true;
					}
					
				}
				
				if (!found) {
					processModelNames.add(processModelName);
					appNames.add(appName);
				}
			}
			
			customData.put("conflictingProcessModelNames", processModelNames);
			customData.put("conflictingAppNames", appNames);
			
			throw new ConflictingRequestException("Process models are already used in other apps. Use 'force' parameter with same request to acknowledge.", "app.publish.process.model.already.used", customData);
		}
		
		
    }

	protected void handleAppRelations(DeploymentResult deploymentResult, 
			Long appDefinitionId, String appDefinitionName, String appDefinitionJson) {
		
		// A new version of a runtim app is created when the user publishes it.
    	// As such, we do not need to update/delete existing relations like for the other models,
    	// only add them as relations for runtime apps are 'cast in stone' and cannot change.
    	// 
    	// For this to work, this does mean that for each of the dependent models a new versions
    	// needs to be created, to which a relation can be made.
		
		AppDefinition appDefinition = getAppDefinition(appDefinitionId, appDefinitionJson);
		for (AppModelDefinition modelDefinition : appDefinition.getModels()) {

			// Get model
			AbstractModel model = modelService.getModel(modelDefinition.getId());
			if (model == null) {
				logger.error("Model " + modelDefinition.getId() + " for app definition " + appDefinitionId + " could not be found");
				throw new BadRequestException("Model for app definition could not be found");
			}

			// Parse json to Java
			ObjectNode modelJsonNode = getModelJson(model);

			Set<Long> formIds = new HashSet<Long>();
			Set<Long> subProcessIds = new HashSet<Long>();
			
			// Forms
			List<JsonNode> formReferenceNodes = JsonConverterUtil.filterOutJsonNodes(JsonConverterUtil.getBpmnProcessModelFormReferences(modelJsonNode));
			formIds.addAll(JsonConverterUtil.gatherLongPropertyFromJsonNodes(formReferenceNodes, "id"));
			
			// For all these gathered dependent models, we now need to create a new version
    		// And create relations to these newly created models
    		User currentUser = SecurityUtils.getCurrentUserObject();
    		Long appDeploymentId = deploymentResult.getRuntimeAppDeployment().getId();
    		
    		// Process relation
    		ModelHistory processModelHistory = modelInternalService.createNewModelVersionAndReturnModelHistory((Model) model, null, currentUser);
    		appRelationRepository.save(new AppRelation(appDeploymentId, processModelHistory.getId(), 
    				ModelRelationTypes.TYPE_PROCESS_MODEL, createProcessModelMetaData(deploymentResult, model)));
    		
    		// Forms
    		for (Long formId : formIds) {
    			AbstractModel formModel = modelService.getModel(formId);
    			ModelHistory formModelHistory = modelInternalService.createNewModelVersionAndReturnModelHistory((Model) formModel, null, currentUser);
        		appRelationRepository.save(new AppRelation(appDeploymentId, formModelHistory.getId(), 
        				ModelRelationTypes.TYPE_FORM_MODEL_CHILD, createFormModelMetaData(deploymentResult, formModel)));
    		}
    		
    		// Subprocesses
    		for (Long subProcessId : subProcessIds) {
    			AbstractModel subProcessModel = modelService.getModel(subProcessId);
    			ModelHistory subProcessModelHistory = modelInternalService.createNewModelVersionAndReturnModelHistory((Model) subProcessModel, null, currentUser);
        		appRelationRepository.save(new AppRelation(appDeploymentId, subProcessModelHistory.getId(), ModelRelationTypes.TYPE_SUBPROCESS_MODEL_CHILD));
    		}
    		
    	}
    }

	protected AppDefinition getAppDefinition(Long appDefinitionId, String appDefinitionJson) {
	    AppDefinition appDefinition = null;
		try {
			appDefinition = objectMapper.readValue(appDefinitionJson, AppDefinition.class);
		} catch (JsonProcessingException e) {
			logger.error("Could not deserialize json for model " + appDefinitionId, e);
			throw new BadRequestException("Invalid app definition json"); // Rethrow, we want the transaction to fail
		} catch (IOException e) {
			logger.error("Could not deserialize json for model " + appDefinitionId, e);
			throw new BadRequestException("Invalid app definition json"); // Rethrow, we want the transaction to fail
		}
		return appDefinition;
    }
	
	protected ObjectNode getModelJson(AbstractModel model) {
	    ObjectNode modelJsonNode = null;
	    try {
	    	modelJsonNode = (ObjectNode) objectMapper.readTree(model.getModelEditorJson());
	    } catch (JsonProcessingException e) {
	    	logger.error("Could not deserialize json for model " + model.getId(), e);
	    	throw new BadRequestException("Invalid json model"); // Rethrow, we want the transaction to faik
	    } catch (IOException e) {
	    	logger.error("Could not deserialize json for model " + model.getId(), e);
	    	throw new BadRequestException("Invalid json model"); // Rethrow, we want the transaction to faik
	    }
	    return modelJsonNode;
    }
	
	protected String createProcessModelMetaData(DeploymentResult deploymentResult, AbstractModel model) {
	    ObjectNode metaDataJson = objectMapper.createObjectNode();
	    metaDataJson.put("deploymentId", deploymentResult.getDeployment().getId());
	    ArrayNode processDefinitionsNode = metaDataJson.putArray("processDefinitions");
	    for (ProcessDefinition processDefinition : deploymentResult.getProcessDefinitionsForProcessModel(model.getId())) {
	    	ObjectNode processDefinitionNode = processDefinitionsNode.addObject();
	    	processDefinitionNode.put("id", processDefinition.getId());
	    	processDefinitionNode.put("key", processDefinition.getKey());
	    	processDefinitionNode.put("name", processDefinition.getName());
	    }
	    try {
	        return objectMapper.writeValueAsString(metaDataJson);
        } catch (JsonProcessingException e) {
	        logger.error("Programmatic error: could not serialize process model metadata", e);
        }
	    return null;
    }
	
	protected String createFormModelMetaData(DeploymentResult deploymentResult, AbstractModel formModel) {
	    ObjectNode metaDataJson = objectMapper.createObjectNode();
	    metaDataJson.put("runtimeFormId", deploymentResult.getRuntimeFormForFormModel(formModel.getId()).getId());
	    metaDataJson.put("runtimeFormName", deploymentResult.getRuntimeFormForFormModel(formModel.getId()).getName());
	    try {
	        return objectMapper.writeValueAsString(metaDataJson);
        } catch (JsonProcessingException e) {
	        logger.error("Programmatic error: could not serialize process model metadata", e);
        }
	    return null;
    }
	
	protected List<String> getProcessDefinitionKeys(AppRelation appRelation) {

		List<String> processDefinitionKeys = new ArrayList<String>();
		
		JsonNode processModelJson = null;
		if (StringUtils.isNotEmpty(appRelation.getMetaData())) {
			try {
				processModelJson = objectMapper.readTree(appRelation.getMetaData());
            } catch (Exception e) {
            	logger.error("Could not deserialize process model metadata", e);
            }
		}
		
		if (processModelJson == null) {
			return processDefinitionKeys;
		}
		
		JsonNode processDefinitionsJson = processModelJson.get("processDefinitions");
		if (processDefinitionsJson.isArray()) {
			Iterator<JsonNode> it = ((ArrayNode) processDefinitionsJson).iterator();
			while (it.hasNext()) {
				JsonNode processDefinitionNode = it.next();
				processDefinitionKeys.add(processDefinitionNode.get("key").asText());
			}
		}
		return processDefinitionKeys;
	}
	
	protected List<String> getProcessDefinitionKeys(User user, AbstractModel relatedProcessModel) {
	    // Get the BpmnModel
		BpmnModel bpmnModel = modelService.getBpmnModel(relatedProcessModel, user, false);

	    // Get process definition keys
	    List<String> processDefinitionKeys = new ArrayList<String>();
	    for (org.activiti.bpmn.model.Process process : bpmnModel.getProcesses()) {
	    	processDefinitionKeys.add(process.getId());
	    }
	    return processDefinitionKeys;
    }
	
	
}

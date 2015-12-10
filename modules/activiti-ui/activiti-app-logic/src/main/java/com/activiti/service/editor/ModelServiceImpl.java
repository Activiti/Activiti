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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.Artifact;
import org.activiti.bpmn.model.Association;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.ExtensionElement;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.GraphicInfo;
import org.activiti.bpmn.model.Lane;
import org.activiti.bpmn.model.Pool;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.UserTask;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.editor.language.json.converter.util.CollectionUtils;
import org.activiti.editor.language.json.converter.util.JsonConverterUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
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
import com.activiti.domain.idm.Group;
import com.activiti.domain.idm.User;
import com.activiti.image.ImageGenerator;
import com.activiti.model.editor.ModelRepresentation;
import com.activiti.model.editor.ReviveModelResultRepresentation;
import com.activiti.model.editor.ReviveModelResultRepresentation.UnresolveModelRepresentation;
import com.activiti.repository.editor.ModelHistoryRepository;
import com.activiti.repository.editor.ModelRelationRepository;
import com.activiti.repository.editor.ModelRepository;
import com.activiti.repository.editor.ModelShareInfoRepository;
import com.activiti.security.SecurityUtils;
import com.activiti.service.api.DeploymentService;
import com.activiti.service.api.GroupHierarchyCache;
import com.activiti.service.api.ModelService;
import com.activiti.service.api.RuntimeAppDefinitionService;
import com.activiti.service.api.UserCache;
import com.activiti.service.api.UserCache.CachedUser;
import com.activiti.service.exception.BaseModelerRestException;
import com.activiti.service.exception.InternalServerErrorException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class ModelServiceImpl implements ModelService, ModelInternalService {
	
    private final Logger log = LoggerFactory.getLogger(ModelServiceImpl.class);

	public static final String NAMESPACE = "http://activiti.com/modeler";

	private static float THUMBNAIL_WIDTH = 300f;
    
    @Autowired
    protected DeploymentService deploymentService;
    
    @Autowired
    protected RuntimeAppDefinitionService runtimeAppDefinitionService;
	
	@Autowired
	protected ModelRepository modelRepository;
	
	@Autowired
	protected ModelHistoryRepository modelHistoryRepository;
	
	@Autowired
	protected ModelShareInfoRepository shareInfoRepository;
    
    @Autowired
    protected ModelRelationRepository modelRelationRepository;

	@Autowired
	protected GroupHierarchyCache groupHierarchyCache;

    @Autowired
    protected ObjectMapper objectMapper;

	@Autowired
	protected UserCache userCache;

	protected BpmnJsonConverter bpmnJsonConverter = new BpmnJsonConverter();
    
    protected BpmnXMLConverter bpmnXMLConverter = new BpmnXMLConverter();

    @Override
    public AbstractModel getModel(Long modelId) {
        return modelRepository.findOne(modelId);
    }
    
    @Override
    public List<AbstractModel> getModelsByModelTypeAndReferenceId(Integer modelType, Long referenceId) {
        return new ArrayList<AbstractModel>(modelRepository.findModelsByModelTypeAndReferenceIdOrNullReferenceId(modelType, referenceId));
    }
    
    @Override
    public byte[] getBpmnXML(AbstractModel model, User user) {
        BpmnModel bpmnModel = getBpmnModel(model, user, false);
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

    @Override
    @Transactional
	public Model createModel(ModelRepresentation model, String editorJson, User createdBy) {
		Model newModel = new Model();
		newModel.setVersion(1);
		newModel.setName(model.getName());
		newModel.setModelType(model.getModelType());
		newModel.setCreated(Calendar.getInstance().getTime());
		newModel.setCreatedBy(createdBy);
		newModel.setDescription(model.getDescription());
		newModel.setModelEditorJson(editorJson);
		newModel.setLastUpdated(Calendar.getInstance().getTime());
		newModel.setLastUpdatedBy(createdBy);
		newModel.setReferenceId(model.getReferenceId());
		
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
        modelObject.setLastUpdatedBy(updatedBy);
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
	public Model saveModel(Model modelObject, String editorJson, byte[] imageBytes, boolean newVersion, 
			String newVersionComment, User updatedBy) {
        
    	return internalSave(modelObject.getName(), modelObject.getDescription(), editorJson, 
    			newVersion, newVersionComment, imageBytes, updatedBy, modelObject);
    }
	
	@Override
    @Transactional
	public Model saveModel(long modelId, String name, String description, 
	        String editorJson, boolean newVersion, String newVersionComment, User updatedBy) {
	    
		Model modelObject = modelRepository.findOne(modelId);
		return internalSave(name, description, editorJson, newVersion, newVersionComment, null, updatedBy, modelObject);
	}

	protected Model internalSave(String name, String description, String editorJson, 
			boolean newVersion, String newVersionComment, byte[] imageBytes, User updatedBy, Model modelObject) {
		
	    if (newVersion == false) {
	    	
			modelObject.setLastUpdated(new Date());
			modelObject.setLastUpdatedBy(updatedBy);
			modelObject.setName(name);
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
			modelObject.setLastUpdatedBy(updatedBy);
			modelObject.setName(name);
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
	        Long appDefinitionId = runtimeAppDefinitionService.getDefinitionIdForModelAndUser(model.getId(), SecurityUtils.getCurrentUserObject());
	        if (appDefinitionId != null) {
	            deploymentService.deleteAppDefinition(appDefinitionId);
	        }
	    
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
		
		// Embedded models should be stored in the history such that they can be revived if needed one day
		List<Model> embeddedModels = modelRepository.findModelsByReferenceId(model.getId());
		for (Model embeddedModel : embeddedModels) {
			  ModelHistory embeddedHistoryModel = createNewModelhistory(embeddedModel);
			  embeddedHistoryModel.setRemovalDate(Calendar.getInstance().getTime());
			  persistModelHistory(embeddedHistoryModel);
			  
			  // Delete all embedded models, and their children
			  internalDeleteModelAndChildren(embeddedModel, allModels);
		}
		
		// Delete all related data
	    shareInfoRepository.deleteInBatchByModelId(model.getId());
	    modelRelationRepository.deleteModelRelationsForParentModel(model.getId());
	    
	    allModels.add(model);
	}
	
	@Override
    @Transactional
    public ReviveModelResultRepresentation reviveProcessModelHistory(ModelHistory modelHistory, User user, String newVersionComment) {
	    Model latestModel = modelRepository.findOne(modelHistory.getModelId());
	    if(latestModel == null) {
	        throw new IllegalArgumentException("No process model found with id: " + modelHistory.getModelId());
	    }
	    
	    // Store the current model in history
	    ModelHistory latestModelHistory = createNewModelhistory(latestModel);
	    persistModelHistory(latestModelHistory);
	    
	    // Populate the actual latest version with the properties in the historic model
	    latestModel.setVersion(latestModel.getVersion() + 1);
	    latestModel.setLastUpdated(new Date());
	    latestModel.setLastUpdatedBy(user);
	    latestModel.setName(modelHistory.getName());
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
	                		result.getUnresolvedModels().add(new UnresolveModelRepresentation(
	                				appModelDefinition.getId(), appModelDefinition.getName(), appModelDefinition.getLastUpdatedByFullName()));
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
    public BpmnModel getBpmnModel(AbstractModel model, User user, boolean refreshReferences) {
        BpmnModel bpmnModel = null;
        try {
            ObjectNode editorJsonNode = (ObjectNode) objectMapper.readTree(model.getModelEditorJson());
            bpmnModel = bpmnJsonConverter.convertToBpmnModel(editorJsonNode);
            
        } catch (BaseModelerRestException e) {
            throw e;
            
        } catch (Exception e) {
            log.error("Could not generate BPMN 2.0 XML for model " + model.getId(), e);
            throw new InternalServerErrorException("Could not generate BPMN 2.0 xml");
        }

		if (refreshReferences) {
		    for (Process process : bpmnModel.getProcesses()) {
		        refreshAssignmentsForUserTasks(process.getFlowElements());
		    }
		}

        return bpmnModel;
    }

	public void refreshAssignmentsForUserTasks(Collection<FlowElement> flowElements) {
		for (FlowElement nextFlowElement : flowElements) {
			if (nextFlowElement instanceof UserTask) {
			    addOrReplaceAssignmentsExtraElements((UserTask) nextFlowElement);
			
			} else if (nextFlowElement instanceof SubProcess) {
			    SubProcess subProcess = (SubProcess) nextFlowElement;
			    refreshAssignmentsForUserTasks(subProcess.getFlowElements());
			}
		}
	}

	protected void addOrReplaceAssignmentsExtraElements(UserTask userTask) {
	    if (StringUtils.isNotEmpty(userTask.getAssignee())) {
	        if (NumberUtils.isNumber(userTask.getAssignee())) {
                CachedUser user = userCache.getUser(Long.valueOf(userTask.getAssignee()));
                if (user != null && user.getUser() != null) {
                    String email = user.getUser().getEmail();
                    if (StringUtils.isNotEmpty(email)) {
                        addOrUpdateExtensionElement("assignee-info-email", email, userTask);
                    }
                }
            }
	    }
	    
	    if (CollectionUtils.isNotEmpty(userTask.getCandidateUsers())) {
	        for (String candidateUserId : userTask.getCandidateUsers()) {
                if (NumberUtils.isNumber(candidateUserId)) {
                    CachedUser user = userCache.getUser(Long.valueOf(candidateUserId));
                    if (user != null && user.getUser() != null) {
                        String email = user.getUser().getEmail();
                        if (StringUtils.isNotEmpty(email)) {
                            addOrUpdateExtensionElement("user-info-email-" + candidateUserId, email, userTask);
                        }
                    }
                }
	        }
	    }
	    
	    if (CollectionUtils.isNotEmpty(userTask.getCandidateGroups())) {
    		for (String candidateGroupId : userTask.getCandidateGroups()) {
    		    if (NumberUtils.isNumber(candidateGroupId)) {
        			Group candidateGroup = groupHierarchyCache.getGroup(Long.parseLong(candidateGroupId));

        			if (candidateGroup != null) {
            			addOrUpdateExtensionElement("group-info-name-" + candidateGroupId, candidateGroup.getName(), userTask);
        			}
    		    }
    		}
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
        return modelRepository.countByModelTypeAndUser(modelType, user);
    }
    
	protected Model persistModel(Model model) {
		
		model = modelRepository.save((Model) model); 
		
		if (StringUtils.isNotEmpty(model.getModelEditorJson())) {
			
			// Parse json to java
			ObjectNode jsonNode = null;
			try {
				jsonNode = (ObjectNode) objectMapper.readTree(model.getModelEditorJson());	
			} catch (JsonProcessingException e) {
	        	log.error("Could not deserialize json model", e);
	        } catch (IOException e) {
	        	log.error("Could not deserialize json model", e);
	        }
			
			if ((model.getModelType() == null || model.getModelType().intValue() == Model.MODEL_TYPE_BPMN) ) {

				// Thumbnail
				generateThumbnailImage((Model) model, jsonNode);
	            	
	            // Relations
	            handleBpmnProcessFormModelRelations(model, jsonNode);
				
			} else if (model.getModelType().intValue() == Model.MODEL_TYPE_APP) {
				
				handleAppModelProcessRelations(model, jsonNode);
				
			}
			
		}
		
		return model;
	}
	
	protected ModelHistory persistModelHistory(ModelHistory modelHistory) {
		return modelHistoryRepository.save(modelHistory);
	}
	
	protected void generateThumbnailImage(Model model, ObjectNode editorJsonNode) {
		try {
			
		    BpmnModel bpmnModel = bpmnJsonConverter.convertToBpmnModel(editorJsonNode);
		    
		    double scaleFactor = 1.0;
		    GraphicInfo diagramInfo = calculateDiagramSize(bpmnModel);
		    if (diagramInfo.getWidth() > THUMBNAIL_WIDTH) {
		    	scaleFactor = diagramInfo.getWidth() / THUMBNAIL_WIDTH;
		    	scaleDiagram(bpmnModel, scaleFactor);
		    }
		    
		    BufferedImage modelImage = ImageGenerator.createImage(bpmnModel, scaleFactor);
		    if (modelImage != null) {
		    	byte[] thumbnailBytes = ImageGenerator.createByteArrayForImage(modelImage, "png");
		    	model.setThumbnail(thumbnailBytes);
		    }
		 } catch (Exception e) {
         	log.error("Error creating thumbnail image " + model.getId(), e);
         }
    }
	
	protected void handleBpmnProcessFormModelRelations(AbstractModel bpmnProcessModel, ObjectNode editorJsonNode) {
		List<JsonNode> formReferenceNodes = JsonConverterUtil.filterOutJsonNodes(JsonConverterUtil.getBpmnProcessModelFormReferences(editorJsonNode));
		Set<Long> formIds = JsonConverterUtil.gatherLongPropertyFromJsonNodes(formReferenceNodes, "id");
		
		handleModelRelations(bpmnProcessModel, formIds, ModelRelationTypes.TYPE_FORM_MODEL_CHILD);
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
        historyModel.setDescription(model.getDescription());
        historyModel.setCreated(model.getCreated());
        historyModel.setLastUpdated(model.getLastUpdated());
        historyModel.setCreatedBy(model.getCreatedBy());
        historyModel.setLastUpdatedBy(model.getLastUpdatedBy());
        historyModel.setModelEditorJson(model.getModelEditorJson());
        historyModel.setModelType(model.getModelType());
        historyModel.setVersion(model.getVersion());
        historyModel.setModelId(model.getId());
        historyModel.setStencilSetId(model.getStencilSetId());
        historyModel.setReferenceId(model.getReferenceId());
        historyModel.setComment(model.getComment());
        
        return historyModel;
	}
	
	protected void populateModelBasedOnHistory(Model model, ModelHistory basedOn) {
	    model.setName(basedOn.getName());
	    model.setDescription(basedOn.getDescription());
	    model.setCreated(basedOn.getCreated());
	    model.setLastUpdated(basedOn.getLastUpdated());
	    model.setCreatedBy(basedOn.getCreatedBy());
	    model.setLastUpdatedBy(basedOn.getLastUpdatedBy());
	    model.setModelEditorJson(basedOn.getModelEditorJson());
	    model.setModelType(basedOn.getModelType());
	    model.setVersion(basedOn.getVersion());
	    model.setStencilSetId(basedOn.getStencilSetId());
	    model.setReferenceId(basedOn.getReferenceId());
	    model.setComment(basedOn.getComment());
	}
	
	protected GraphicInfo calculateDiagramSize(BpmnModel bpmnModel) {
		GraphicInfo diagramInfo = new GraphicInfo();

	    for (Pool pool : bpmnModel.getPools()) {
	      GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(pool.getId());
	      double elementMaxX = graphicInfo.getX() + graphicInfo.getWidth();
	      double elementMaxY = graphicInfo.getY() + graphicInfo.getHeight();
	      
	      if (elementMaxX > diagramInfo.getWidth()) {
	    	  diagramInfo.setWidth(elementMaxX);
	      }
	      if (elementMaxY > diagramInfo.getHeight()) {
	    	  diagramInfo.setHeight(elementMaxY);
	      }
	    }
	    
	    for (Process process : bpmnModel.getProcesses()) {
	    	calculateWidthForFlowElements(process.getFlowElements(), bpmnModel, diagramInfo);
	    	calculateWidthForArtifacts(process.getArtifacts(), bpmnModel, diagramInfo);
	    }
	    return diagramInfo;
	}
	
	protected void scaleDiagram(BpmnModel bpmnModel, double scaleFactor) {
	    for (Pool pool : bpmnModel.getPools()) {
	      GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(pool.getId());
	      scaleGraphicInfo(graphicInfo, scaleFactor);
	    }
	    
	    for (Process process : bpmnModel.getProcesses()) {
	    	scaleFlowElements(process.getFlowElements(), bpmnModel, scaleFactor);
	    	scaleArtifacts(process.getArtifacts(), bpmnModel, scaleFactor);
	    	for (Lane lane : process.getLanes()) {
	    		scaleGraphicInfo(bpmnModel.getGraphicInfo(lane.getId()), scaleFactor);
			}
	    }
	}
	
	protected void calculateWidthForFlowElements(Collection<FlowElement> elementList, BpmnModel bpmnModel, GraphicInfo diagramInfo) {
	    for (FlowElement flowElement : elementList) {
	    	List<GraphicInfo> graphicInfoList = new ArrayList<GraphicInfo>();
	    	if (flowElement instanceof SequenceFlow) {
	    	    List<GraphicInfo> flowGraphics = bpmnModel.getFlowLocationGraphicInfo(flowElement.getId());
	    	    if (flowGraphics != null && flowGraphics.size() > 0) {
	    	        graphicInfoList.addAll(flowGraphics);
	    	    }
	    	} else {
	    	    GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(flowElement.getId());
	    	    if (graphicInfo != null) {
	    	        graphicInfoList.add(graphicInfo);
	    	    }
	    	}
	    	
	    	processGraphicInfoList(graphicInfoList, diagramInfo);
	    }
	}
	
	protected void calculateWidthForArtifacts(Collection<Artifact> artifactList, BpmnModel bpmnModel, GraphicInfo diagramInfo) {
	    for (Artifact artifact : artifactList) {
	    	List<GraphicInfo> graphicInfoList = new ArrayList<GraphicInfo>();
	    	if (artifact instanceof Association) {
	    		graphicInfoList.addAll(bpmnModel.getFlowLocationGraphicInfo(artifact.getId()));
	    	} else {
	    		graphicInfoList.add(bpmnModel.getGraphicInfo(artifact.getId()));
	    	}
	    	
	    	processGraphicInfoList(graphicInfoList, diagramInfo);
	    }
	}
	
	protected void processGraphicInfoList(List<GraphicInfo> graphicInfoList, GraphicInfo diagramInfo) {
		for (GraphicInfo graphicInfo : graphicInfoList) {
    		double elementMaxX = graphicInfo.getX() + graphicInfo.getWidth();
	    	double elementMaxY = graphicInfo.getY() + graphicInfo.getHeight();
		      
	    	if (elementMaxX > diagramInfo.getWidth()) {
	    		diagramInfo.setWidth(elementMaxX);
	    	}
	    	if (elementMaxY > diagramInfo.getHeight()) {
	    		diagramInfo.setHeight(elementMaxY);
	    	}
    	}
	}
	
	protected void scaleFlowElements(Collection<FlowElement> elementList, BpmnModel bpmnModel, double scaleFactor) {
	    for (FlowElement flowElement : elementList) {
	    	List<GraphicInfo> graphicInfoList = new ArrayList<GraphicInfo>();
	    	if (flowElement instanceof SequenceFlow) {
	    		List<GraphicInfo> flowList = bpmnModel.getFlowLocationGraphicInfo(flowElement.getId());
	    		if (flowList != null) {
	    			graphicInfoList.addAll(flowList);
	    		}
	    	} else {
	    		graphicInfoList.add(bpmnModel.getGraphicInfo(flowElement.getId()));
	    	}
	    	
	    	scaleGraphicInfoList(graphicInfoList, scaleFactor);
	    	
	    	if (flowElement instanceof SubProcess) {
	    		SubProcess subProcess = (SubProcess) flowElement;
	    		scaleFlowElements(subProcess.getFlowElements(), bpmnModel, scaleFactor);
	    	}
	    }
	}
	
	protected void scaleArtifacts(Collection<Artifact> artifactList, BpmnModel bpmnModel, double scaleFactor) {
	    for (Artifact artifact : artifactList) {
	    	List<GraphicInfo> graphicInfoList = new ArrayList<GraphicInfo>();
	    	if (artifact instanceof Association) {
	    		List<GraphicInfo> flowList = bpmnModel.getFlowLocationGraphicInfo(artifact.getId());
	    		if (flowList != null) {
	    			graphicInfoList.addAll(flowList);
	    		}
	    	} else {
	    		graphicInfoList.add(bpmnModel.getGraphicInfo(artifact.getId()));
	    	}
	    	
	    	scaleGraphicInfoList(graphicInfoList, scaleFactor);
	    }
	}
	
	protected void scaleGraphicInfoList(List<GraphicInfo> graphicInfoList, double scaleFactor) {
		for (GraphicInfo graphicInfo : graphicInfoList) {
			scaleGraphicInfo(graphicInfo, scaleFactor);
		}
	}
	
	protected void scaleGraphicInfo(GraphicInfo graphicInfo, double scaleFactor) {
		graphicInfo.setX(graphicInfo.getX() / scaleFactor);
		graphicInfo.setY(graphicInfo.getY() / scaleFactor);
		graphicInfo.setWidth(graphicInfo.getWidth() / scaleFactor);
		graphicInfo.setHeight(graphicInfo.getHeight() / scaleFactor);
	}
}

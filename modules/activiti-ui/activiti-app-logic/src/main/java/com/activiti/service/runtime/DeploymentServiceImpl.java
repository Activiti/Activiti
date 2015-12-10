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
package com.activiti.service.runtime;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.ExtensionElement;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.UserTask;
import org.activiti.editor.language.json.converter.util.CollectionUtils;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.runtime.Clock;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.activiti.domain.editor.AbstractModel;
import com.activiti.domain.idm.User;
import com.activiti.domain.runtime.Form;
import com.activiti.domain.runtime.RuntimeAppDefinition;
import com.activiti.domain.runtime.RuntimeAppDeployment;
import com.activiti.repository.runtime.FormRepository;
import com.activiti.service.api.AppDefinitionService;
import com.activiti.service.api.AppDefinitionServiceRepresentation;
import com.activiti.service.api.DeploymentResult;
import com.activiti.service.api.DeploymentService;
import com.activiti.service.api.ModelService;
import com.activiti.service.api.UserService;
import com.activiti.service.exception.BadRequestException;
import com.activiti.service.exception.NotPermittedException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Tijs Rademakers
 * @author Joram Barrez
 */
@Service
public class DeploymentServiceImpl implements DeploymentService {
    
    private static final Logger logger = LoggerFactory.getLogger(DeploymentServiceImpl.class);

    @Autowired
    private RuntimeAppDefinitionInternalService runtimeAppDefinitionService;
    
    @Autowired
    private AppDefinitionService appDefinitionService;
    
    @Autowired
    private ModelService modelService;

    @Autowired
    private RepositoryService repositoryService;
    
    @Autowired
    private FormRepository formRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private Clock clock;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    @Transactional
    public void deployAppDefinitions(List<Long> appDefinitions, User user) {
    	
    	// Check if user is allowed to use app definition
        List<AppDefinitionServiceRepresentation> availableAppDefinitions = appDefinitionService.getDeployableAppDefinitions(user);
        Map<Long, AppDefinitionServiceRepresentation> availableIdMap = new HashMap<Long, AppDefinitionServiceRepresentation>();
        for (AppDefinitionServiceRepresentation appDef : availableAppDefinitions) {
            availableIdMap.put(appDef.getId(), appDef);
        }
        
        for (Long appDefId : appDefinitions) {
            if (availableIdMap.containsKey(appDefId) == false) {
                logger.error("App definition " + appDefId + " is not allowed for user " + user.getId());
                throw new NotPermittedException("One of the app definitions is not allowed");
            }
        }
        
        // Deploy each app definition
        for (Long appDefId : appDefinitions) {
            AppDefinitionServiceRepresentation appDefinition = availableIdMap.get(appDefId);
            RuntimeAppDefinition runtimeAppDefinition = runtimeAppDefinitionService.getRuntimeAppDefinitionForModel(appDefinition.getId());
            if (runtimeAppDefinition == null) {

                runtimeAppDefinition = runtimeAppDefinitionService.createRuntimeAppDefinition(user, appDefinition.getName(),
                        appDefinition.getDescription(), appDefinition.getId(), appDefinition.getDefinition());
                
                deployAppDefinitionToActiviti(appDefinition, runtimeAppDefinition, user);
            }
                
            // Regardless if we created the definition or not, create a RuntimeApp to connect the user
            runtimeAppDefinitionService.addAppDefinitionForUser(user, runtimeAppDefinition);
        }
    }
    
    @Override
    @Transactional
    public DeploymentResult updateAppDefinition(Long appDefinitionId, String appDefinitionName, 
            String appDefinitionDescription, String appDefinitionJson, User user) {

    	// Get all app models that the user can deploy
        List<AppDefinitionServiceRepresentation> availableAppDefinitions = appDefinitionService.getDeployableAppDefinitions(user);
        
        // Create a map {id, appModel} of the above list
        Map<Long, AppDefinitionServiceRepresentation> availableIdMap = new HashMap<Long, AppDefinitionServiceRepresentation>();
        for (AppDefinitionServiceRepresentation appDef : availableAppDefinitions) {
            availableIdMap.put(appDef.getId(), appDef);
        }
        
        // Check if the id provided in the method is part of this map, if not the app is not deployable by the user
        if (availableIdMap.containsKey(appDefinitionId) == false) {
            logger.error("App definition " + appDefinitionId + " is not allowed for user " + user.getId());
            throw new NotPermittedException("App definition update is not allowed for this user");
        }
        
        RuntimeAppDefinition runtimeAppDefinition = runtimeAppDefinitionService.getRuntimeAppDefinitionForModel(appDefinitionId);
        if (runtimeAppDefinition != null) {
        	
        	// App definition is already deployed. Update the values and deploy the related processes 
            runtimeAppDefinition.setName(appDefinitionName);
            runtimeAppDefinition.setDescription(appDefinitionDescription);
            runtimeAppDefinition.setDefinition(appDefinitionJson);
            
        } else {
        	
        	// No app definition deployed yet. Create the first one
        	runtimeAppDefinition = runtimeAppDefinitionService.createRuntimeAppDefinition(user, appDefinitionName, 
                      appDefinitionDescription, appDefinitionId, appDefinitionJson);
        	
        }
        
        // Now we can deploy the processes related to the app
        return deployAppDefinitionToActiviti(availableIdMap.get(appDefinitionId), runtimeAppDefinition, user);
    }
    

    @Override
    @Transactional
    public void deleteAppDefinition(Long appDefinitionId) {
        RuntimeAppDefinition appDefinition = runtimeAppDefinitionService.getRuntimeAppDefinition(appDefinitionId);
        if (appDefinition != null) {
            formRepository.deleteInBatchByAppId(appDefinitionId);
            List<RuntimeAppDeployment> appDeployments = runtimeAppDefinitionService.getRuntimeAppDeploymentsForApp(appDefinition);
            if (CollectionUtils.isNotEmpty(appDeployments)) {
                for (RuntimeAppDeployment runtimeAppDeployment : appDeployments) {
                    String deploymentId = runtimeAppDeployment.getDeploymentId();
                    if (deploymentId != null) {
                        // First test if deployment is still there, otherwhise the transaction will be rolled back
                        Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(deploymentId).singleResult();
                        if (deployment != null) {
                            repositoryService.deleteDeployment(deploymentId, true);
                        }
                    }
                }
            }
            runtimeAppDefinitionService.deleteAppDefinition(appDefinition);
        }
    }
    
    protected DeploymentResult deployAppDefinitionToActiviti(AppDefinitionServiceRepresentation appDefinition, RuntimeAppDefinition runtimeAppDefinition, User user) {
    	
    	DeploymentResult deploymentResult = new DeploymentResult();
    	
        RuntimeAppDeployment appDeployment = runtimeAppDefinitionService.createRuntimeAppDeployment(user, runtimeAppDefinition, 
                appDefinition.getId(), appDefinition.getDefinition());
        
        if (CollectionUtils.isNotEmpty(appDefinition.getModels())) {
            DeploymentBuilder deploymentBuilder = repositoryService.createDeployment()
                    .name(appDefinition.getName());
            
            for (Long modelId : appDefinition.getModels()) {
            	
                AbstractModel processModel = modelService.getModel(modelId);
                if (processModel == null) {
                    logger.error("Model " + modelId + " for app definition " + appDefinition.getId() + " could not be found");
                    throw new BadRequestException("Model for app definition could not be found");
                }
                
                BpmnModel bpmnModel = modelService.getBpmnModel(processModel, user, false);
                Map<String, StartEvent> startEventMap = processNoneStartEvents(bpmnModel);
                Map<Long, Form> formMap = overrideBpmnForms(bpmnModel, startEventMap, processModel, runtimeAppDefinition.getId(), appDeployment.getId(), user);
                
                if (formMap != null) {
                	for (Long formModelId : formMap.keySet()) {
                		deploymentResult.addFormModelMapping(formModelId, formMap.get(formModelId));
                		System.out.println("form mapping " + formModelId + " " + formMap.get(formModelId));
                	}
                }
                
                // When including users by email in the processes, we swap out that email with a real user id
                processEmailUsers(bpmnModel);
                
                byte[] modelXML = modelService.getBpmnXML(bpmnModel);
                deploymentBuilder.addInputStream(processModel.getName().toLowerCase().replaceAll(" ", "") + ".bpmn", new ByteArrayInputStream(modelXML));
                
                deploymentResult.addModelMapping(processModel, bpmnModel);
            }
            
            Deployment deployment = deploymentBuilder.deploy();
            deploymentResult.setDeployment(deployment);
            if (deployment instanceof DeploymentEntity) {
            	List<ProcessDefinitionEntity> processDefinitions = ((DeploymentEntity) deployment).getDeployedArtifacts(ProcessDefinitionEntity.class);
            	for (ProcessDefinitionEntity processDefinitionEntity : processDefinitions) {
            		deploymentResult.addProcessDefinition(processDefinitionEntity);
            	}
            }
            
            runtimeAppDefinition.setDeploymentId(deployment.getId());
            appDeployment.setDeploymentId(deployment.getId());
            
            runtimeAppDefinitionService.updateRuntimeAppDeployment(appDeployment);
        }
        
        runtimeAppDefinitionService.updateRuntimeAppDefinition(runtimeAppDefinition);
        
        deploymentResult.setRuntimeAppDeployment(appDeployment);
        return deploymentResult;
    }
    
    protected Map<String, StartEvent> processNoneStartEvents(BpmnModel bpmnModel) {
        Map<String, StartEvent> startEventMap = new HashMap<String, StartEvent>();
        for (Process process : bpmnModel.getProcesses()) {
            for (FlowElement flowElement : process.getFlowElements()) {
                if (flowElement instanceof StartEvent) {
                    StartEvent startEvent = (StartEvent) flowElement;
                    if (CollectionUtils.isEmpty(startEvent.getEventDefinitions())) {
                        if (StringUtils.isEmpty(startEvent.getInitiator())) {
                            startEvent.setInitiator("initiator");
                        }
                        startEventMap.put(process.getId(), startEvent);
                        break;
                    }
                }
            }
        }
        return startEventMap;
    }
    
    protected  Map<Long, Form>  overrideBpmnForms(BpmnModel bpmnModel, Map<String, StartEvent> startEventMap, AbstractModel model, 
            Long appDefinitionId, Long appDeploymentId, User user) {
        
        Map<Long, Form> formMap = new HashMap<Long, Form>();
        for (Process process : bpmnModel.getProcesses()) {
            processBpmnForms(process.getFlowElements(), process, startEventMap, formMap, model, 
                    appDefinitionId, appDeploymentId, user);
        }
        return formMap;
    }
    
    protected void processBpmnForms(Collection<FlowElement> flowElements, Process process, Map<String, StartEvent> startEventMap, 
            Map<Long, Form> formIdMap, AbstractModel model, Long appDefinitionId, Long appDeploymentId, User user) {
        
        for (FlowElement flowElement : flowElements) {
            if (flowElement instanceof UserTask) {
                UserTask userTask = (UserTask) flowElement;
                String finalFormKey = retrieveFinalFormKey(userTask.getFormKey(), userTask, formIdMap, model, appDefinitionId, appDeploymentId, user);
                if ("$INITIATOR".equals(userTask.getAssignee())) {
                    if (startEventMap.get(process.getId()) != null) {
                        userTask.setAssignee("${" + startEventMap.get(process.getId()).getInitiator() + "}");
                    }
                }
                userTask.setFormKey(finalFormKey);
            
            } else if (flowElement instanceof StartEvent) {
                StartEvent startEvent = (StartEvent) flowElement;
                String finalFormKey = retrieveFinalFormKey(startEvent.getFormKey(), startEvent, formIdMap, model, appDefinitionId, appDeploymentId, user);
                startEvent.setFormKey(finalFormKey);
                
            } else if (flowElement instanceof SequenceFlow) {
                SequenceFlow flow = (SequenceFlow) flowElement;
                fillSequenceFlowCondition(flow, formIdMap, model, appDefinitionId, appDeploymentId, user);
            
            } else if (flowElement instanceof SubProcess) {
                processBpmnForms(((SubProcess) flowElement).getFlowElements(), process, startEventMap, formIdMap, 
                        model, appDefinitionId, appDeploymentId, user);
            }
        }
    }
    
    protected String retrieveFinalFormKey(String formKey, FlowElement flowElement, Map<Long, Form> formIdMap, 
            AbstractModel model, Long appDefinitionId, Long appDeploymentId, User user) {
        
        String finalFormKey = null;
        List<ExtensionElement> formIdExtensions = flowElement.getExtensionElements().get("form-reference-id");
        List<ExtensionElement> formNameExtensions = flowElement.getExtensionElements().get("form-reference-name");
        if (CollectionUtils.isNotEmpty(formIdExtensions) && CollectionUtils.isNotEmpty(formNameExtensions)) {
            Long formId = Long.valueOf(formIdExtensions.get(0).getElementText());
            finalFormKey = getFormKeyWithFormId(formId, formIdMap, model, appDefinitionId, appDeploymentId, user);
            
        } else if (StringUtils.isNotEmpty(formKey) && formKey.startsWith("FORM_REFERENCE")) {
            String formIdValue = formKey.replace("FORM_REFERENCE", "");
            if (NumberUtils.isNumber(formIdValue)) {
                Long formId = Long.valueOf(formIdValue);
                finalFormKey = getFormKeyWithFormId(formId, formIdMap, model, appDefinitionId, appDeploymentId, user);
            }
        }
        
        if (StringUtils.isEmpty(finalFormKey)) {
            finalFormKey = formKey;
        }
        
        return finalFormKey;
    }
    
    protected String getFormKeyWithFormId(Long formId, Map<Long, Form> formIdMap, AbstractModel model, Long appDefinitionId, Long appDeploymentId, User user) {
        String formKey = null;
        if (formIdMap.containsKey(formId) == false) {
            AbstractModel formModel = modelService.getModel(formId);
            Map<Long, AbstractModel> formModelMap = new HashMap<Long, AbstractModel>();
            if (formModel != null) {
                formModelMap.put(formModel.getId(), formModel);
            }
            Form runtimeForm = createRuntimeForm(formId, formModelMap, model, appDefinitionId, appDeploymentId, user);
            formRepository.save(runtimeForm);
            formIdMap.put(formId, runtimeForm);
            formKey = String.valueOf(runtimeForm.getId());
            
        } else {
            formKey = String.valueOf(formIdMap.get(formId).getId());
        }
        return formKey;
    }
    
    protected void fillSequenceFlowCondition(SequenceFlow sequenceFlow, Map<Long, Form> formIdMap, AbstractModel model, 
            Long appDefinitionId, Long appDeploymentId, User user) {
        
        if (sequenceFlow.getExtensionElements().get("conditionFormId") != null) {
            
            String conditionFormId = sequenceFlow.getExtensionElements().get("conditionFormId").get(0).getElementText();
            
            String conditionOperator = null;
            if (sequenceFlow.getExtensionElements().get("conditionOperator") != null) {
                conditionOperator = sequenceFlow.getExtensionElements().get("conditionOperator").get(0).getElementText();
            }
            
            String conditionOutcomeName = null;
            if (sequenceFlow.getExtensionElements().get("conditionOutcomeName") != null) {
                conditionOutcomeName = sequenceFlow.getExtensionElements().get("conditionOutcomeName").get(0).getElementText();
            }
            
            if (StringUtils.isNotEmpty(conditionFormId) && StringUtils.isNotEmpty(conditionOperator) && 
                    StringUtils.isNotEmpty(conditionOutcomeName)) {
                
                Long formId = Long.valueOf(conditionFormId);
                if (formIdMap.containsKey(formId) == false) {
                    AbstractModel formModel = modelService.getModel(formId);
                    Map<Long, AbstractModel> formModelMap = new HashMap<Long, AbstractModel>();
                    if (formModel != null) {
                        formModelMap.put(formModel.getId(), formModel);
                    }
                    Form runtimeForm = createRuntimeForm(formId, formModelMap, model, appDefinitionId, appDeploymentId, user);
                    formRepository.save(runtimeForm);
                    formIdMap.put(formId, runtimeForm);
                }
                
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append("${form")
                    .append(formIdMap.get(formId).getId())
                    .append("outcome")
                    .append(conditionOperator)
                    .append("'")
                    .append(conditionOutcomeName)
                    .append("'")
                    .append("}");
                
                sequenceFlow.setConditionExpression(conditionBuilder.toString());
            }
        }
    }
    
    protected Form createRuntimeForm(Long formId, Map<Long, AbstractModel> formMap, AbstractModel model, 
            Long appDefinitionId, Long appDeploymentId, User user) {
        
        if (formMap.containsKey(formId)) {
            AbstractModel form = formMap.get(formId);
            Form runtimeForm = new Form();
            runtimeForm.setName(form.getName());
            runtimeForm.setDescription(form.getDescription());
            runtimeForm.setCreated(clock.getCurrentTime());
            runtimeForm.setCreatedBy(user);
            runtimeForm.setAppDefinitionId(appDefinitionId);
            runtimeForm.setAppDeploymentId(appDeploymentId);
            runtimeForm.setModelId(formId);
            runtimeForm.setDefinition(form.getModelEditorJson());
            return runtimeForm;
            
        } else {
            logger.error("Form " + formId + " for app deployment " + appDeploymentId + " and model " + model.getId() + " could not be found");
            throw new BadRequestException("Form for model " + model.getName() + " could not be found");
        }
    }
    
    protected void processEmailUsers(BpmnModel bpmnModel) {
    	if (bpmnModel == null) {
    		return;
    	}
    	
    	for (Process process : bpmnModel.getProcesses()) {
    		List<UserTask> userTasks = process.findFlowElementsOfType(UserTask.class, true);
    		for (UserTask userTask : userTasks) {
    			Map<String, List<ExtensionElement>> extensionElements = userTask.getExtensionElements();
    			if (extensionElements != null) {

    				if (extensionElements.containsKey("activiti-assignee-email")) {
    					ExtensionElement extensionElement = extensionElements.get("activiti-assignee-email").get(0); // Should be only one
    					String email = extensionElement.getElementText();
    					if (email != null && email.length() > 0) {
    					    User user = userService.findOrCreateUserByEmail(email);
    						userTask.setAssignee(String.valueOf(user.getId()));
    					}
    				}
    				
    				if (extensionElements.containsKey("activiti-candidate-users-emails")) {
    					ExtensionElement extensionElement = extensionElements.get("activiti-candidate-users-emails").get(0); // Should be only one
    					String text = extensionElement.getElementText();
    					if (text != null && text.length() > 0) {
    						String[] emailsString = StringUtils.split(text, ",");
    						List<String> emails = Arrays.asList(emailsString);
    						List<String> userIds = userTask.getCandidateUsers();
    						for (String userId : userIds) {
    						    int index = emails.indexOf(userId);
    						    if (index >= 0) {
    						        User user = userService.findOrCreateUserByEmail(userId);
    						        emails.set(index, String.valueOf(user.getId()));
    						    }
    						}
    					}
    				}
    				
    			}
    		}
    	}
    }
    
    protected String getProcessName(Process process) {
        String name = null;
        if (StringUtils.isNotEmpty(process.getName())) {
            name = process.getName();
        } else {
            name = process.getId();
        }
        return name;
    }
}

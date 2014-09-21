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
package org.activiti.workflow.simple.alfresco.conversion;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.FieldExtension;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.IntermediateCatchEvent;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.TimerEventDefinition;
import org.activiti.bpmn.model.UserTask;
import org.activiti.workflow.simple.alfresco.conversion.form.AlfrescoFormCreator;
import org.activiti.workflow.simple.alfresco.conversion.script.PropertyReference;
import org.activiti.workflow.simple.alfresco.model.M2Aspect;
import org.activiti.workflow.simple.alfresco.model.M2Model;
import org.activiti.workflow.simple.alfresco.model.M2Namespace;
import org.activiti.workflow.simple.alfresco.model.M2Type;
import org.activiti.workflow.simple.alfresco.model.config.Configuration;
import org.activiti.workflow.simple.alfresco.model.config.Extension;
import org.activiti.workflow.simple.alfresco.model.config.Form;
import org.activiti.workflow.simple.alfresco.model.config.FormField;
import org.activiti.workflow.simple.alfresco.model.config.FormFieldControl;
import org.activiti.workflow.simple.alfresco.model.config.FormFieldControlParameter;
import org.activiti.workflow.simple.alfresco.model.config.Module;
import org.activiti.workflow.simple.converter.WorkflowDefinitionConversion;
import org.activiti.workflow.simple.converter.listener.WorkflowDefinitionConversionListener;
import org.activiti.workflow.simple.definition.AbstractConditionStepListContainer;
import org.activiti.workflow.simple.definition.AbstractStepDefinitionContainer;
import org.activiti.workflow.simple.definition.AbstractStepListContainer;
import org.activiti.workflow.simple.definition.FormStepDefinition;
import org.activiti.workflow.simple.definition.ListConditionStepDefinition;
import org.activiti.workflow.simple.definition.ListStepDefinition;
import org.activiti.workflow.simple.definition.StepDefinition;
import org.activiti.workflow.simple.definition.WorkflowDefinition;
import org.activiti.workflow.simple.definition.form.FormDefinition;
import org.activiti.workflow.simple.definition.form.FormPropertyDefinition;
import org.activiti.workflow.simple.definition.form.FormPropertyGroup;
import org.activiti.workflow.simple.definition.form.ReferencePropertyDefinition;

/**
 * A {@link WorkflowDefinitionConversionListener} that creates a {@link M2Model} and a {@link Configuration}
 * before conversion, that can be used to add any models and configuration needed throughout the conversion.
 * 
 * @author Frederik Heremans
 * @author Joram Barrez
 */
public class InitializeAlfrescoModelsConversionListener implements WorkflowDefinitionConversionListener, AlfrescoConversionConstants {

  private static final long serialVersionUID = 1L;
  
	protected AlfrescoFormCreator formCreator;
	
	// Types of ReferencePropertyDefinition that should be ignore for reuse
	protected static final Set<String> IGNORED_REFERENCE_TYPES_REUSE = new HashSet<String>(Arrays.asList(
			AlfrescoConversionConstants.FORM_REFERENCE_DUEDATE,
			AlfrescoConversionConstants.FORM_REFERENCE_PRIORITY,
			AlfrescoConversionConstants.FORM_REFERENCE_PACKAGE_ITEMS,
			AlfrescoConversionConstants.FORM_REFERENCE_WORKFLOW_DESCRIPTION
  ));
  
  public InitializeAlfrescoModelsConversionListener() {
  	formCreator = new AlfrescoFormCreator();
  }
  
	@Override
	public void beforeStepsConversion(WorkflowDefinitionConversion conversion) {
		String processId = null;
		if(conversion.getWorkflowDefinition().getId() != null) {
			processId = AlfrescoConversionUtil.getValidIdString(conversion.getWorkflowDefinition().getId());
		} else {
			processId = generateUniqueProcessId(conversion);
		}
		
		M2Model model = addContentModel(conversion, processId);
		addExtension(conversion, processId);
		
		// In case the same property definitions are used across multiple forms, we need to identify this
		// up-front and create an aspect for this that can be shared due to the fact that you cannot define the same
		// property twice in a the same content-model namespace
		addAspectsForReusedProperties(conversion.getWorkflowDefinition(), model, processId);
		
		// Add list of property references
		conversion.setArtifact(ARTIFACT_PROPERTY_REFERENCES, new ArrayList<PropertyReference>());
	}

	@Override
	public void afterStepsConversion(WorkflowDefinitionConversion conversion) {
		M2Model model = AlfrescoConversionUtil.getContentModel(conversion);
		M2Namespace modelNamespace = model.getNamespaces().get(0);
		
		for(FlowElement flowElement : conversion.getProcess().getFlowElements()) {
			if(flowElement instanceof StartEvent) {
				StartEvent startEvent = (StartEvent) flowElement;
				if(startEvent.getFormKey() == null) {
					
					Module module = AlfrescoConversionUtil.getExtension(conversion).getModules().get(0);
					Configuration detailsForm = module.addConfiguration(EVALUATOR_STRING_COMPARE, 
							MessageFormat.format(EVALUATOR_CONDITION_ACTIVITI, conversion.getProcess().getId()));
					
					// No form-key is set, either use the default or generate of start-form if this
					// is available
					if(conversion.getWorkflowDefinition().getStartFormDefinition() != null
							&& !conversion.getWorkflowDefinition().getStartFormDefinition().getFormGroups().isEmpty()) {
						
						// Create the content model for the start-task
						M2Type type = new M2Type();
					
						model.getTypes().add(type);
						type.setName(AlfrescoConversionUtil.getQualifiedName(modelNamespace.getPrefix(),
								AlfrescoConversionConstants.START_TASK_SIMPLE_NAME));
						type.setParentName(AlfrescoConversionConstants.DEFAULT_START_FORM_TYPE);
						
						// Create a form-config for the start-task
						Module shareModule = AlfrescoConversionUtil.getExtension(conversion).getModules().get(0);
						Configuration configuration = shareModule.addConfiguration(AlfrescoConversionConstants.EVALUATOR_TASK_TYPE
								, type.getName());
						Form formConfig = configuration.createForm();
						formConfig.setStartForm(true);
						
						// Populate model and form based on FormDefinition
						formCreator.createForm(type, formConfig, conversion.getWorkflowDefinition().getStartFormDefinition(), conversion);
						
						// Use the same form-config for the workflow details
						detailsForm.addForm(formConfig);
						
						// Set formKey on start-event, referencing type
						startEvent.setFormKey(type.getName());
					} else {
						// Revert to the default start-form
						startEvent.setFormKey(DEFAULT_START_FORM_TYPE);
						
						// Also add form-config to the share-module for workflow detail screen, based on the default form
						populateDefaultDetailFormConfig(detailsForm);
					}
					
				}
			}
		}
		
		

		// Check all elements that can contain PropertyReferences or need additional builders invoked
		List<PropertyReference> references = AlfrescoConversionUtil.getPropertyReferences(conversion);
		for(FlowElement element : conversion.getProcess().getFlowElements()) {
			if(element instanceof SequenceFlow) {
				resolvePropertyRefrencesInSequenceFlow((SequenceFlow) element, modelNamespace, references);
			} else if(element instanceof IntermediateCatchEvent) {
				resolvePropertyRefrencesInCatchEvent((IntermediateCatchEvent) element, modelNamespace, references);
			} else if(element instanceof ServiceTask)  {
				resolvePropertyRefrencesInServiceTask((ServiceTask) element, modelNamespace, references);
			} else if(element instanceof UserTask) {
				addScriptListenersToUserTask((UserTask) element, conversion);
			}
		}
		
		// Check if all property-references reference a valid property
		if(references != null && !references.isEmpty()) {
			for(PropertyReference reference : references) {
				reference.validate(model);
			}
		}
		
	}
	
	protected void addScriptListenersToUserTask(UserTask userTask, WorkflowDefinitionConversion conversion) {
		// Add create-script-listener if it has been used in this conversion
		if(AlfrescoConversionUtil.hasTaskScriptTaskListenerBuilder(conversion, userTask.getId(), 
				AlfrescoConversionConstants.TASK_LISTENER_EVENT_CREATE)) {
			userTask.getTaskListeners().add(AlfrescoConversionUtil.getScriptTaskListenerBuilder(conversion, userTask.getId(), 
					AlfrescoConversionConstants.TASK_LISTENER_EVENT_CREATE).build());
		}
		
		// Add complete-script-listener if it has been used in this conversion
		if(AlfrescoConversionUtil.hasTaskScriptTaskListenerBuilder(conversion, userTask.getId(), 
				AlfrescoConversionConstants.TASK_LISTENER_EVENT_COMPLETE)) {
			userTask.getTaskListeners().add(AlfrescoConversionUtil.getScriptTaskListenerBuilder(conversion, userTask.getId(), 
					AlfrescoConversionConstants.TASK_LISTENER_EVENT_COMPLETE).build());
		}
  }
	
	protected void resolvePropertyRefrencesInSequenceFlow(SequenceFlow sequenceFlow, M2Namespace modelNamespace, List<PropertyReference> references) {
		if(sequenceFlow.getConditionExpression() != null && PropertyReference.containsPropertyReference(sequenceFlow.getConditionExpression())) {
			sequenceFlow.setConditionExpression(PropertyReference.replaceAllPropertyReferencesInString(sequenceFlow.getConditionExpression(), modelNamespace.getPrefix(), references, false));
		}
	}
	
	protected void resolvePropertyRefrencesInCatchEvent(IntermediateCatchEvent event, M2Namespace modelNamespace, List<PropertyReference> references) {
		if(event.getEventDefinitions() != null && !event.getEventDefinitions().isEmpty()) {
			for(EventDefinition def : event.getEventDefinitions()) {
				if(def instanceof TimerEventDefinition) {
					TimerEventDefinition timer = (TimerEventDefinition) def;
					if(timer.getTimeDate() != null && PropertyReference.isPropertyReference(timer.getTimeDate())) {
						timer.setTimeDate(PropertyReference.createReference(timer.getTimeDate()).getPropertyReferenceExpression(modelNamespace.getPrefix()));
					}
				}
			}
		}
	}
	
	protected void resolvePropertyRefrencesInServiceTask(ServiceTask serviceTask, M2Namespace modelNamespace, List<PropertyReference> references) {
		if(serviceTask.getFieldExtensions() != null && !serviceTask.getFieldExtensions().isEmpty()) {
			for(FieldExtension extension : serviceTask.getFieldExtensions()) {
				String value = extension.getExpression();
				if(value != null && !value.isEmpty() && PropertyReference.containsPropertyReference(value)) {
					value = PropertyReference.replaceAllPropertyReferencesInString(value, modelNamespace.getPrefix(), references, true);
					extension.setExpression(value);
				}
			}
		}
	}
	
	
	
	protected String generateUniqueProcessId(WorkflowDefinitionConversion conversion) {
		String processId = AlfrescoConversionUtil.getValidIdString(
				PROCESS_ID_PREFIX + UUID.randomUUID().toString());
		conversion.getProcess().setId(processId);
		return processId;
  }
	
	protected void addAspectsForReusedProperties(WorkflowDefinition workflowDefinition, M2Model model, String processId) {
		Map<String, FormPropertyDefinition> definitionMap = new HashMap<String, FormPropertyDefinition>();
		
		// Add start-form properties
		addDefinitionsToMap(workflowDefinition.getStartFormDefinition(), definitionMap);
		
		// Run through steps recursivelye, looking for properties
		addAspectsForReusedProperties(workflowDefinition.getSteps(), model, processId, definitionMap);
		
		// Check if the map contains values other than null, this indicates duplicate properties are found
		for(Entry<String, FormPropertyDefinition> entry : definitionMap.entrySet()) {
			if(entry.getValue() != null) {
				// Create an aspect for this property. The aspect itself will be populated when the first
				// property is converted with that name
				M2Aspect aspect = new M2Aspect();
				aspect.setName(AlfrescoConversionUtil.getQualifiedName(processId, entry.getKey()));
				model.getAspects().add(aspect);
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
  protected void addAspectsForReusedProperties(List<StepDefinition> steps, M2Model model, String processId, Map<String, FormPropertyDefinition> definitionMap) {
		for (StepDefinition step : steps) {
			if (step instanceof FormStepDefinition) {
				addDefinitionsToMap(((FormStepDefinition) step).getForm(), definitionMap);
			} else if(step instanceof AbstractStepListContainer<?>) {
        List<ListStepDefinition<?>> stepList = ((AbstractStepListContainer) step).getStepList();
        for(ListStepDefinition<?> list : stepList) {
        	addAspectsForReusedProperties(list.getSteps(), model, processId, definitionMap);
        }
      } else if(step instanceof AbstractConditionStepListContainer<?>) {
        List<ListConditionStepDefinition<?>> stepList = ((AbstractConditionStepListContainer) step).getStepList();
        for(ListConditionStepDefinition<?> list : stepList) {
        	addAspectsForReusedProperties(list.getSteps(), model, processId, definitionMap);
        }
      } else if(step instanceof AbstractStepDefinitionContainer<?>) {
      	addAspectsForReusedProperties(((AbstractStepDefinitionContainer<WorkflowDefinition>) step).getSteps(), model, processId, definitionMap);
      }
		}
	}
	
	protected void addDefinitionsToMap(FormDefinition formDefinition, Map<String, FormPropertyDefinition> definitionMap) {
		if(formDefinition != null && formDefinition.getFormGroups() != null) {
			String finalPropertyName = null;
			for(FormPropertyGroup group : formDefinition.getFormGroups()) {
				if(group.getFormPropertyDefinitions() != null) {
					for(FormPropertyDefinition def : group.getFormPropertyDefinitions()) {
						if(isPropertyReuseCandidate(def)) {
							finalPropertyName = AlfrescoConversionUtil.getValidIdString(def.getName());
							if(definitionMap.containsKey(finalPropertyName)) {
								definitionMap.put(finalPropertyName, def);
							} else {
								definitionMap.put(finalPropertyName, null);
							}
						}
					}
				}
			}
		}
  }
	
	protected boolean isPropertyReuseCandidate(FormPropertyDefinition def) {
		boolean valid = !(def instanceof ReferencePropertyDefinition);
		if(!valid) {
			ReferencePropertyDefinition reference = (ReferencePropertyDefinition) def;
			valid = ! IGNORED_REFERENCE_TYPES_REUSE.contains(reference.getType());
		}
		return valid;
	}

	protected M2Model addContentModel(WorkflowDefinitionConversion conversion, String processId) {
		// The process ID is used as namespace prefix, to guarantee uniqueness
		
		// Set general model properties
		M2Model model = new M2Model();
		model.setName(AlfrescoConversionUtil.getQualifiedName(processId, 
				CONTENT_MODEL_UNQUALIFIED_NAME));
		
		M2Namespace namespace = AlfrescoConversionUtil.createNamespace(processId);
		model.getNamespaces().add(namespace);
		
		
		// Import required alfresco models
		model.getImports().add(DICTIONARY_NAMESPACE);
		model.getImports().add(CONTENT_NAMESPACE);
		model.getImports().add(BPM_NAMESPACE);
		
		// Store model in the conversion artifacts to be accessed later
		AlfrescoConversionUtil.storeContentModel(model, conversion);
		AlfrescoConversionUtil.storeModelNamespacePrefix(namespace.getPrefix(), conversion);
		
		return model;
  }
	
	protected void addExtension(WorkflowDefinitionConversion conversion, String processId) {
		// Create form-configuration
		Extension extension = new Extension();
		Module module = new Module();
		extension.addModule(module);
		module.setId(MessageFormat.format(MODULE_ID, processId));
		AlfrescoConversionUtil.storeExtension(extension, conversion);
  }
	
	protected void populateDefaultDetailFormConfig(Configuration configuration) {
	  Form form = configuration.createForm();
	  
	  // Add visibility of fields
	  form.getFormFieldVisibility().addShowFieldElement(PROPERTY_WORKFLOW_DESCRIPTION);
	  form.getFormFieldVisibility().addShowFieldElement(PROPERTY_WORKFLOW_DUE_DATE);
	  form.getFormFieldVisibility().addShowFieldElement(PROPERTY_WORKFLOW_PRIORITY);
	  form.getFormFieldVisibility().addShowFieldElement(PROPERTY_PACKAGEITEMS);
	  form.getFormFieldVisibility().addShowFieldElement(PROPERTY_SEND_EMAIL_NOTIFICATIONS);
	  
	  // Add all sets to the appearance
	  form.getFormAppearance().addFormSet(FORM_SET_GENERAL, FORM_SET_APPEARANCE_TITLE, FORM_SET_GENERAL_LABEL, null);
	  form.getFormAppearance().addFormSet(FORM_SET_INFO, null, null, FORM_SET_TEMPLATE_2_COLUMN);
	  form.getFormAppearance().addFormSet(FORM_SET_ASSIGNEE, FORM_SET_APPEARANCE_TITLE, FORM_SET_ASSIGNEE_LABEL, null);
	  form.getFormAppearance().addFormSet(FORM_SET_ITEMS, FORM_SET_APPEARANCE_TITLE, FORM_SET_ITEMS_LABEL, null);
	  form.getFormAppearance().addFormSet(FORM_SET_OTHER, FORM_SET_APPEARANCE_TITLE, FORM_SET_OTHER_LABEL, null);
	  
	  // Finally, add the individual fields
	  FormField descriptionField = new FormField();
	  descriptionField.setId(PROPERTY_WORKFLOW_DESCRIPTION);
	  descriptionField.setControl(new FormFieldControl(FORM_MULTILINE_TEXT_TEMPLATE));
	  descriptionField.setLabelId(FORM_WORKFLOW_DESCRIPTION_LABEL);
	  form.getFormAppearance().addFormAppearanceElement(descriptionField);
	  
	  FormField dueDateField = new FormField();
	  dueDateField.setId(PROPERTY_WORKFLOW_DUE_DATE);
	  dueDateField.setSet(FORM_SET_INFO);
	  dueDateField.setLabelId(FORM_WORKFLOW_DUE_DATE_LABEL);
	  dueDateField.setControl(new FormFieldControl(FORM_DATE_TEMPLATE));
	  dueDateField.getControl().getControlParameters().add(new FormFieldControlParameter(FORM_DATE_PARAM_SHOW_TIME, Boolean.FALSE.toString()));
	  dueDateField.getControl().getControlParameters().add(new FormFieldControlParameter(FORM_DATE_PARAM_SUBMIT_TIME, Boolean.FALSE.toString()));
	  form.getFormAppearance().addFormAppearanceElement(dueDateField);
	  
	  FormField priorityField = new FormField();
	  priorityField.setSet(FORM_SET_INFO);
	  priorityField.setLabelId(FORM_WORKFLOW_PRIORITY_LABEL);
	  priorityField.setId(PROPERTY_WORKFLOW_PRIORITY);
	  priorityField.setControl(new FormFieldControl(FORM_PRIORITY_TEMPLATE));
	  form.getFormAppearance().addFormAppearanceElement(priorityField);
	  
	  form.getFormAppearance().addFormField(PROPERTY_PACKAGEITEMS, null, FORM_SET_ITEMS);
	  
	  FormField emailNotificationsField = new FormField();
	  emailNotificationsField.setSet(FORM_SET_OTHER);
	  emailNotificationsField.setId(PROPERTY_SEND_EMAIL_NOTIFICATIONS);
	  emailNotificationsField.setControl(new FormFieldControl(FORM_EMAIL_NOTIFICATION_TEMPLATE));
	  form.getFormAppearance().addFormAppearanceElement(emailNotificationsField);
	}
}

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.UserTask;
import org.activiti.workflow.simple.alfresco.conversion.exception.AlfrescoSimpleWorkflowException;
import org.activiti.workflow.simple.alfresco.conversion.form.AlfrescoFormCreator;
import org.activiti.workflow.simple.alfresco.conversion.script.PropertyReference;
import org.activiti.workflow.simple.alfresco.conversion.script.ScriptTaskListenerBuilder;
import org.activiti.workflow.simple.alfresco.model.M2Model;
import org.activiti.workflow.simple.alfresco.model.M2Namespace;
import org.activiti.workflow.simple.alfresco.model.M2Type;
import org.activiti.workflow.simple.alfresco.model.config.Configuration;
import org.activiti.workflow.simple.alfresco.model.config.Form;
import org.activiti.workflow.simple.alfresco.model.config.Module;
import org.activiti.workflow.simple.converter.WorkflowDefinitionConversion;
import org.activiti.workflow.simple.converter.step.HumanStepDefinitionConverter;
import org.activiti.workflow.simple.converter.step.StepDefinitionConverter;
import org.activiti.workflow.simple.definition.HumanStepAssignment.HumanStepAssignmentType;
import org.activiti.workflow.simple.definition.HumanStepDefinition;
import org.activiti.workflow.simple.definition.StepDefinition;
import org.activiti.workflow.simple.exception.SimpleWorkflowException;

/**
 * A {@link StepDefinitionConverter} which adds a content-model and a form-config to the conversion
 * based on the {@link HumanStepDefinition} that is converted.
 *  
 * @author Frederik Heremans
 */
public class AlfrescoHumanStepDefinitionConverter extends HumanStepDefinitionConverter {

  private static final long serialVersionUID = 1L;
  
  private AlfrescoFormCreator formCreator;
  
  public AlfrescoHumanStepDefinitionConverter() {
  	formCreator = new AlfrescoFormCreator();
  }

	@Override
	public Class<? extends StepDefinition> getHandledClass() {
		return HumanStepDefinition.class;
	}

	@Override
	public UserTask convertStepDefinition(StepDefinition stepDefinition, WorkflowDefinitionConversion conversion) {
		HumanStepDefinition humanStep = (HumanStepDefinition) stepDefinition;
		validate(humanStep);
		
		M2Model model = AlfrescoConversionUtil.getContentModel(conversion);
		M2Namespace modelNamespace = model.getNamespaces().get(0);
		
		// Let superclass handle BPMN-specific conversion
		UserTask userTask = super.convertStepDefinition(stepDefinition, conversion);
		
		// Clear form-properties in the BPMN file, as we use custom form-mapping in Alfresco
		userTask.getFormProperties().clear();
		
		userTask.setName(humanStep.getName() != null ? humanStep.getName() : humanStep.getId());
		
		// Create the content model for the task
		M2Type type = new M2Type();
		model.getTypes().add(type);
		type.setName(AlfrescoConversionUtil.getQualifiedName(modelNamespace.getPrefix(),
				humanStep.getId()));
		type.setParentName(AlfrescoConversionConstants.DEFAULT_BASE_FORM_TYPE);
		
		// Update task-key on the task itself
		userTask.setFormKey(type.getName());
		
		// Create a form-config for the task
		Module shareModule = AlfrescoConversionUtil.getExtension(conversion).getModules().get(0);
		Configuration configuration = shareModule.addConfiguration(AlfrescoConversionConstants.EVALUATOR_TASK_TYPE
				, type.getName());
		Form formConfig = configuration.createForm();
		
		// Populate model and form based on FormDefinition
		formCreator.createForm(type, formConfig, humanStep.getForm(), conversion);
		
		// Set up property sharing using task-listeners
		addPropertySharing(humanStep, conversion, userTask);
		
		// Special handling for assignee that reference form-properties, before BPMN
		// is created
		if (humanStep.getAssignmentType() == HumanStepAssignmentType.USER) {
			String assignee = humanStep.getAssignment().getAssignee();

			if (assignee != null && PropertyReference.isPropertyReference(assignee)) {
				PropertyReference reference = PropertyReference.createReference(assignee);
				AlfrescoConversionUtil.getPropertyReferences(conversion).add(reference);
				userTask.setAssignee(reference.getUsernameReferenceExpression(modelNamespace.getPrefix()));
			}
		} else if (humanStep.getAssignmentType() == HumanStepAssignmentType.USERS) {
			if(humanStep.getAssignment().getCandidateUsers() != null) {
				userTask.setCandidateUsers(resolveUserPropertyReferences(humanStep.getAssignment().getCandidateUsers(), modelNamespace.getPrefix(), conversion));
			}
		} else if (humanStep.getAssignmentType() == HumanStepAssignmentType.GROUPS) {
			if(humanStep.getAssignment().getCandidateGroups() != null) {
				userTask.setCandidateGroups(resolveGroupPropertyReferences(humanStep.getAssignment().getCandidateGroups(), modelNamespace.getPrefix(), conversion));
			}
		}
		
		return userTask;
	}
	
	protected UserTask locateUserTask(WorkflowDefinitionConversion conversion) {
		List<FlowElement> elements = (List<FlowElement>) conversion.getProcess().getFlowElements();
		
		for(int i=elements.size() -1; i >= 0; i--) {
			if(elements.get(i) instanceof UserTask) {
				return (UserTask) elements.get(i);
			}
		}
		throw new SimpleWorkflowException("No usertask found in conversion");
  }

	protected void addPropertySharing(HumanStepDefinition humanStep, WorkflowDefinitionConversion conversion, UserTask userTask) {
		PropertySharing sharing = AlfrescoConversionUtil.getPropertySharing(conversion, userTask.getId());
		
		// Add default incoming properties (due-date and priority)
		// TODO: make optional?
		ScriptTaskListenerBuilder createEventBuilder = AlfrescoConversionUtil.getScriptTaskListenerBuilder(conversion, userTask.getId(), 
				AlfrescoConversionConstants.TASK_LISTENER_EVENT_CREATE);
		
		createEventBuilder.addDueDateInheritance();
		createEventBuilder.addPriorityInheritance();
		
		// Add create-listener in case incoming properties are present
		if(sharing.hasIncomingProperties()) {
			createEventBuilder = AlfrescoConversionUtil.getScriptTaskListenerBuilder(conversion, userTask.getId(), 
					AlfrescoConversionConstants.TASK_LISTENER_EVENT_CREATE);
			for(Entry<String, String> entry : sharing.getIncomingProperties().entrySet()) {
				createEventBuilder.addIncomingProperty(entry.getKey(), entry.getValue());
			}
		}
		
		// Add complete-listener in case incoming properties are present
		if(sharing.hasOutgoingProperties()) {
			ScriptTaskListenerBuilder completeEventBuilder = AlfrescoConversionUtil.getScriptTaskListenerBuilder(conversion, userTask.getId(), 
					AlfrescoConversionConstants.TASK_LISTENER_EVENT_COMPLETE);
			for(Entry<String, String> entry : sharing.getOutgoingProperties().entrySet()) {
				completeEventBuilder.addOutgoingProperty(entry.getKey(), entry.getValue());
			}
		}
  }

	protected List<String> resolveUserPropertyReferences(List<String> list, String namespacePrefix, WorkflowDefinitionConversion conversion) {
		if(list != null) {
			List<String> result = new ArrayList<String>();
			PropertyReference propertyReference = null;
			for(String string : list) {
				if(PropertyReference.isPropertyReference(string)) {
					propertyReference = PropertyReference.createReference(string);
					result.add(propertyReference.getUsernameReferenceExpression(namespacePrefix));
					
					// Add reference to be validated 
					AlfrescoConversionUtil.getPropertyReferences(conversion).add(propertyReference);
				} else {
					result.add(string);
				}
			}
			return result;
		}
		return null;
	}
	
	protected List<String> resolveGroupPropertyReferences(List<String> list, String namespacePrefix, WorkflowDefinitionConversion conversion) {
		if(list != null) {
			List<String> result = new ArrayList<String>();
			PropertyReference propertyReference = null;
			for(String string : list) {
				if(PropertyReference.isPropertyReference(string)) {
					propertyReference = PropertyReference.createReference(string);
					result.add(propertyReference.getGroupReferenceExpression(namespacePrefix));
					
					// Add reference to be validated 
					AlfrescoConversionUtil.getPropertyReferences(conversion).add(propertyReference);
				} else {
					result.add(string);
				}
			}
			return result;
		}
		return null;
	}
	
	@Override
	protected String getInitiatorExpression() {
		// Use the correct assignee expression if the initiator is used for assignment
	  return AlfrescoConversionConstants.INITIATOR_ASSIGNEE_EXPRESSION;
	}
	
	@Override
	protected String getInitiatorVariable() {
		// Variable used to store the assignee
		return AlfrescoConversionConstants.INITIATOR_VARIABLE;
	}
	

	protected void validate(HumanStepDefinition stepDefinition) {
		if(stepDefinition.getId() == null) {
			if(stepDefinition.getName() == null && !stepDefinition.getName().isEmpty()) {
				throw new AlfrescoSimpleWorkflowException("Name or id of a human step is required.");
			}
			stepDefinition.setId(AlfrescoConversionUtil.getValidIdString(stepDefinition.getName()));
		}
  }

}

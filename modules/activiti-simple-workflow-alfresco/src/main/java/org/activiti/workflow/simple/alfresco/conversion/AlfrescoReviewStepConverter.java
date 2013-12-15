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

import java.util.Arrays;

import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.MultiInstanceLoopCharacteristics;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.bpmn.model.UserTask;
import org.activiti.workflow.simple.alfresco.conversion.script.PropertyReference;
import org.activiti.workflow.simple.alfresco.conversion.script.ScriptServiceTaskBuilder;
import org.activiti.workflow.simple.alfresco.conversion.script.ScriptTaskListenerBuilder;
import org.activiti.workflow.simple.alfresco.form.AlfrescoTransitionsPropertyDefinition;
import org.activiti.workflow.simple.alfresco.model.M2AssociationSource;
import org.activiti.workflow.simple.alfresco.model.M2AssociationTarget;
import org.activiti.workflow.simple.alfresco.model.M2ClassAssociation;
import org.activiti.workflow.simple.alfresco.model.M2Model;
import org.activiti.workflow.simple.alfresco.model.M2Type;
import org.activiti.workflow.simple.alfresco.step.AlfrescoEndProcessStepDefinition;
import org.activiti.workflow.simple.alfresco.step.AlfrescoReviewStepDefinition;
import org.activiti.workflow.simple.converter.ConversionConstants;
import org.activiti.workflow.simple.converter.WorkflowDefinitionConversion;
import org.activiti.workflow.simple.converter.step.BaseStepDefinitionConverter;
import org.activiti.workflow.simple.definition.ChoiceStepsDefinition;
import org.activiti.workflow.simple.definition.ConditionDefinition;
import org.activiti.workflow.simple.definition.HumanStepAssignment.HumanStepAssignmentType;
import org.activiti.workflow.simple.definition.HumanStepDefinition;
import org.activiti.workflow.simple.definition.ListConditionStepDefinition;
import org.activiti.workflow.simple.definition.StepDefinition;
import org.activiti.workflow.simple.definition.form.FormDefinition;
import org.activiti.workflow.simple.definition.form.ListPropertyEntry;

public class AlfrescoReviewStepConverter extends BaseStepDefinitionConverter<AlfrescoReviewStepDefinition, FlowElement> {

	private static final long serialVersionUID = 1L;

	@Override
	public Class<? extends StepDefinition> getHandledClass() {
		return AlfrescoReviewStepDefinition.class;
	}

	@Override
	protected FlowElement createProcessArtifact(AlfrescoReviewStepDefinition stepDefinition,
	    WorkflowDefinitionConversion conversion) {
		FlowElement lastElement;

		M2Model model = AlfrescoConversionUtil.getContentModel(conversion);
		String namespacePrefix = model.getNamespaces().get(0).getPrefix();
		
		String id = stepDefinition.getId();
		if(id == null) {
			id = AlfrescoConversionUtil.getValidIdString(stepDefinition.getName());
		}
		
		// Break down the review into separate steps and convert those instead
		HumanStepDefinition reviewTask = new HumanStepDefinition();
		reviewTask.setName(stepDefinition.getName());
		reviewTask.setDescription("Review task");

		// Clone the review-form and add custom transitions property
		FormDefinition finalForm = null;
		if (stepDefinition.getForm() != null) {
			finalForm = stepDefinition.getForm().clone();
		} else {
			finalForm = new FormDefinition();
		}
		finalForm.addFormProperty(createTransitionsProperty());
		reviewTask.setForm(finalForm);
		
		
		// Assignment
		if(stepDefinition.getAssignmentType() == HumanStepAssignmentType.USER) {
			reviewTask.setAssignee(new PropertyReference(stepDefinition.getAssignmentPropertyName()).getPlaceholder());
		}
		
		// Add a script-task that initializes the correct variables for the review
		ScriptServiceTaskBuilder builder = new ScriptServiceTaskBuilder();
		builder.setExecutionVariable(getCountVariableName(id, namespacePrefix), "0");
		
		String requiredCount = null;
		if(stepDefinition.getRequiredApprovalCount() != null) {
			if(PropertyReference.isPropertyReference(stepDefinition.getRequiredApprovalCount())) {
				PropertyReference reference = PropertyReference.createReference(stepDefinition.getRequiredApprovalCount());
				requiredCount = reference.getVariableReference(namespacePrefix);
				AlfrescoConversionUtil.getPropertyReferences(conversion).add(reference);
			} else {
				// No reference, use explicit value
				requiredCount = stepDefinition.getRequiredApprovalCount();
			}
		} else {
			requiredCount = "1";
		}
		builder.setExecutionVariable(getRequiredCountVariableName(id, namespacePrefix), requiredCount);
		
		ServiceTask serviceTask = builder.build();
		serviceTask.setName("Review initialization");
		serviceTask.setId(conversion.getUniqueNumberedId(ConversionConstants.SERVICE_TASK_ID_PREFIX));
		
		addFlowElement(conversion, serviceTask, true);

		// Create the actual element
		UserTask userTask = (UserTask) conversion.getConversionFactory().getStepConverterFor(reviewTask)
		    .convertStepDefinition(reviewTask, conversion);
		lastElement = userTask;
		

		M2Type userTaskType = model.getType(userTask.getFormKey());
		
		// Update parent, since we use an "outcome" for this task
		userTaskType.setParentName(AlfrescoConversionConstants.OUTCOME_BASE_FORM_TYPE);
		
		// Add script to the complete-task listener to update approval count (if needed)
		ScriptTaskListenerBuilder listenerBuilder = AlfrescoConversionUtil.getScriptTaskListenerBuilder(conversion, userTask.getId(), AlfrescoConversionConstants.TASK_LISTENER_EVENT_COMPLETE);
		String approverCount = getCountVariableName(id, namespacePrefix);
		listenerBuilder.addLine("if(task.getVariableLocal('" + getTransitionProperty(userTaskType, namespacePrefix) + "') == '" + AlfrescoConversionConstants.TRANSITION_APPROVE +"') {");
		listenerBuilder.addLine("execution.setVariable('" +approverCount + "', " + approverCount + " + 1);");
		listenerBuilder.addLine("}");
		
		if(stepDefinition.getAssignmentType() == HumanStepAssignmentType.USERS) {
			String assignmentVariableName = id + "Assignee";
			
			// Add the assignee-property to the content-model
			M2ClassAssociation reviewAssignee = new M2ClassAssociation();
			M2AssociationTarget target = new M2AssociationTarget();
			target.setClassName(AlfrescoConversionConstants.CONTENT_TYPE_PEOPLE);
			target.setMandatory(true);
			target.setMany(false);
			
			M2AssociationSource source = new M2AssociationSource();
			source.setMany(false);
			source.setMandatory(true);
			
			reviewAssignee.setName(AlfrescoConversionUtil.getQualifiedName(namespacePrefix, assignmentVariableName));
			reviewAssignee.setTarget(target);
			reviewAssignee.setSource(source);
			
			userTaskType.getAssociations().add(reviewAssignee);
			userTask.setAssignee(new PropertyReference(assignmentVariableName).getUsernameReferenceExpression(namespacePrefix));
			
			// Finally, add the multi-instance characteristics to the userTask
			MultiInstanceLoopCharacteristics mi = new MultiInstanceLoopCharacteristics();
			mi.setCompletionCondition(getCompletionCondition(id, namespacePrefix));
			mi.setElementVariable(new PropertyReference(assignmentVariableName).getVariableReference(namespacePrefix));
			
			PropertyReference reference = null;
			if(PropertyReference.isPropertyReference(stepDefinition.getAssignmentPropertyName())) {
				reference = PropertyReference.createReference(stepDefinition.getAssignmentPropertyName());
			} else {
				reference = new PropertyReference(stepDefinition.getAssignmentPropertyName());
			}
			mi.setInputDataItem(reference.getVariableReference(namespacePrefix));
			AlfrescoConversionUtil.getPropertyReferences(conversion).add(reference);
			mi.setSequential(false);
			userTask.setLoopCharacteristics(mi);
		}

		if (stepDefinition.getRejectionSteps() != null) {
			// Create choice-step
			ChoiceStepsDefinition choice = new ChoiceStepsDefinition();
			choice.setId(id + "choice");

			// Add rejection steps to the choice
			ListConditionStepDefinition<ChoiceStepsDefinition> rejectStepList = new ListConditionStepDefinition<ChoiceStepsDefinition>();
			rejectStepList.setName("Rejected");
			for (StepDefinition child : stepDefinition.getRejectionSteps()) {
				rejectStepList.addStep(child);
			}
			
			// Add end-process step to reject path, if needed
			if(stepDefinition.isEndProcessOnReject()) {
				rejectStepList.addStep(new AlfrescoEndProcessStepDefinition());
			}

			// Make choice condition based on review outcome
			ConditionDefinition condition = new ConditionDefinition();
			condition.setLeftOperand(getCountVariableName(id, namespacePrefix));
			condition.setOperator("<");
			condition.setRightOperand(getRequiredCountVariableName(id, namespacePrefix));
			rejectStepList.setConditions(Arrays.asList(condition));
			choice.addStepList(rejectStepList);

			// Add default (empty) choice for approval AFTER the review-one
			ListConditionStepDefinition<ChoiceStepsDefinition> defaultStepList = new ListConditionStepDefinition<ChoiceStepsDefinition>();
			defaultStepList.setName("Approved");
			choice.addStepList(defaultStepList);

			// Convert the choice-step
			lastElement = (FlowElement) conversion.getConversionFactory().getStepConverterFor(choice)
			    .convertStepDefinition(choice, conversion);
		}
		return lastElement;
	}
	
	protected String getTransitionProperty(M2Type type, String namespacePrefix) {
		return new PropertyReference(AlfrescoConversionUtil.getValidIdString(type.getName() + AlfrescoConversionConstants.PROPERTY_TRANSITIONS_SUFFIX)).getVariableReference(namespacePrefix);
  }

	protected String getCompletionCondition(String id, String namespacePrefix) {
		return "${" + getCountVariableName(id, namespacePrefix) + " >= " + getRequiredCountVariableName(id, namespacePrefix) + "}";
  }

	protected String getCountVariableName(String id, String namespacePrefix) {
		return new PropertyReference(id + "ApprovalCount").getVariableReference(namespacePrefix);
	}
	
	protected String getRequiredCountVariableName(String id, String namespacePrefix) {
		return new PropertyReference(id + "RequiredApprovalCount").getVariableReference(namespacePrefix);
	}

	protected AlfrescoTransitionsPropertyDefinition createTransitionsProperty() {
		AlfrescoTransitionsPropertyDefinition prop = new AlfrescoTransitionsPropertyDefinition();
		prop.addEntry(new ListPropertyEntry(AlfrescoConversionConstants.TRANSITION_APPROVE,
		    AlfrescoConversionConstants.TRANSITION_APPROVE));
		prop.addEntry(new ListPropertyEntry(AlfrescoConversionConstants.TRANSITION_REJECT,
		    AlfrescoConversionConstants.TRANSITION_REJECT));
		return prop;
	}

}

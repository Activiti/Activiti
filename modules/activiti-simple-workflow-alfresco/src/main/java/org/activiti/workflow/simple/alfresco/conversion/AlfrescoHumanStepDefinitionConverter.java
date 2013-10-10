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

import org.activiti.bpmn.model.UserTask;
import org.activiti.workflow.simple.alfresco.conversion.exception.AlfrescoSimpleWorkflowException;
import org.activiti.workflow.simple.alfresco.conversion.form.AlfrescoFormCreator;
import org.activiti.workflow.simple.alfresco.model.M2Model;
import org.activiti.workflow.simple.alfresco.model.M2Namespace;
import org.activiti.workflow.simple.alfresco.model.M2Type;
import org.activiti.workflow.simple.alfresco.model.config.Configuration;
import org.activiti.workflow.simple.alfresco.model.config.Form;
import org.activiti.workflow.simple.alfresco.model.config.Module;
import org.activiti.workflow.simple.converter.WorkflowDefinitionConversion;
import org.activiti.workflow.simple.converter.step.HumanStepDefinitionConverter;
import org.activiti.workflow.simple.converter.step.StepDefinitionConverter;
import org.activiti.workflow.simple.definition.HumanStepDefinition;
import org.activiti.workflow.simple.definition.StepDefinition;

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
	public void convertStepDefinition(StepDefinition stepDefinition, WorkflowDefinitionConversion conversion) {
		// Let superclass handle BPMN-specific conversion
		super.convertStepDefinition(stepDefinition, conversion);
		
		// Clear form-properties in the BPMN file, as we use custom form-mapping in Alfresco
		String userTaskId = conversion.getLastActivityId();
		UserTask userTask = (UserTask) conversion.getProcess().getFlowElement(userTaskId);
		userTask.getFormProperties().clear();
		
		HumanStepDefinition humanStep = (HumanStepDefinition) stepDefinition;
		validate(humanStep);
		
		userTask.setName(humanStep.getName() != null ? humanStep.getName() : humanStep.getId());
		
		// Create the content model for the task
		M2Type type = new M2Type();
		M2Model model = AlfrescoConversionUtil.getContentModel(conversion);
		model.getTypes().add(type);
		M2Namespace modelNamespace = model.getNamespaces().get(0);
		type.setName(AlfrescoConversionUtil.getQualifiedName(modelNamespace.getPrefix(),
				humanStep.getId()));
		type.setParentName(AlfrescoConversionConstants.DEFAULT_BASE_FORM_TYPE);
		
		// Update task-key on the task itself
		userTask.setFormKey(type.getName());
		
		// Create a form-config for the task
		Module shareModule = AlfrescoConversionUtil.getModule(conversion);
		Configuration configuration = shareModule.addConfiguration(AlfrescoConversionConstants.EVALUATOR_TASK_TYPE
				, type.getName());
		Form formConfig = configuration.createForm();
		
		// Populate model and form based on FormDefinition
		formCreator.createForm(type, formConfig, humanStep.getForm(), conversion);
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
			throw new AlfrescoSimpleWorkflowException("Id of a human step is required.");
		}
  }

}

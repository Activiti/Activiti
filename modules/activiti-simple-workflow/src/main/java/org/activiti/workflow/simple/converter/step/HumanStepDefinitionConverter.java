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
package org.activiti.workflow.simple.converter.step;

import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.UserTask;
import org.activiti.workflow.simple.converter.ConversionConstants;
import org.activiti.workflow.simple.converter.WorkflowDefinitionConversion;
import org.activiti.workflow.simple.definition.HumanStepAssignment.HumanStepAssignmentType;
import org.activiti.workflow.simple.definition.HumanStepDefinition;
import org.activiti.workflow.simple.definition.StepDefinition;
import org.activiti.workflow.simple.definition.form.FormDefinition;

/**
 * {@link StepDefinitionConverter} for converting a {@link HumanStepDefinition} to a {@link UserTask}.
 * 
 * @author Frederik Heremans
 * @author Joram Barrez
 */
public class HumanStepDefinitionConverter extends BaseStepDefinitionConverter<HumanStepDefinition, UserTask> {
  
	private static final long serialVersionUID = 1L;
  
	private static final String DEFAULT_INITIATOR_VARIABLE = "initiator";
  private static final String DEFAULT_INITIATOR_ASSIGNEE_EXPRESSION = "${initiator}";

  public Class< ? extends StepDefinition> getHandledClass() {
    return HumanStepDefinition.class;
  }
  
  protected UserTask createProcessArtifact(HumanStepDefinition stepDefinition, WorkflowDefinitionConversion conversion) {
    UserTask userTask = createUserTask(stepDefinition, conversion);
    addFlowElement(conversion, userTask, true);
    
    return userTask;
  }

  protected UserTask createUserTask(HumanStepDefinition humanStepDefinition, WorkflowDefinitionConversion conversion) {
    
    // TODO: validate and throw exception on missing properties
    
    UserTask userTask = new UserTask();
    userTask.setId(conversion.getUniqueNumberedId(ConversionConstants.USER_TASK_ID_PREFIX));
    userTask.setName(humanStepDefinition.getName());
    userTask.setDocumentation(humanStepDefinition.getDescription());

    // Initiator
    if (humanStepDefinition.getAssignmentType() == HumanStepAssignmentType.INITIATOR) {
      userTask.setAssignee(getInitiatorExpression());

      // Add the initiator variable declaration to the start event
      for (StartEvent startEvent : conversion.getProcess().findFlowElementsOfType(StartEvent.class)) {
        startEvent.setInitiator(getInitiatorVariable());
      }
      
    // Assignee  
    } else if (humanStepDefinition.getAssignmentType() == HumanStepAssignmentType.USER) {
      userTask.setAssignee(humanStepDefinition.getAssignee());
    }

    // Candidate Users
    if (humanStepDefinition.getAssignmentType() == HumanStepAssignmentType.USERS || humanStepDefinition.getAssignmentType() == HumanStepAssignmentType.MIXED) {
      userTask.setCandidateUsers(humanStepDefinition.getCandidateUsers());
    }

    // Candidate groups
    if (humanStepDefinition.getAssignmentType() == HumanStepAssignmentType.GROUPS || humanStepDefinition.getAssignmentType() == HumanStepAssignmentType.MIXED) {
      userTask.setCandidateGroups(humanStepDefinition.getCandidateGroups());
    }
    
    // Form
    if (humanStepDefinition.getForm() != null) {
      
      FormDefinition formDefinition = humanStepDefinition.getForm();
      
      // Form properties
      userTask.setFormProperties(convertProperties(formDefinition));
      
      if(formDefinition.getFormKey() != null) {
      	userTask.setFormKey(formDefinition.getFormKey());
      }
    }

    return userTask;
  }
  
  //Extracted in a method such that subclasses can override if needed
  protected String getInitiatorVariable() {
    return DEFAULT_INITIATOR_VARIABLE;
  }
  
  // Extracted in a method such that subclasses can override if needed
  protected String getInitiatorExpression() {
    return DEFAULT_INITIATOR_ASSIGNEE_EXPRESSION;
  }
  
}

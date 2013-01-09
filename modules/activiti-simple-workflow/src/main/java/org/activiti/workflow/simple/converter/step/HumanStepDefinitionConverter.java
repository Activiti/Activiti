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

import org.activiti.bpmn.model.FormProperty;
import org.activiti.bpmn.model.UserTask;
import org.activiti.workflow.simple.converter.WorkflowDefinitionConversion;
import org.activiti.workflow.simple.definition.FormDefinition;
import org.activiti.workflow.simple.definition.FormPropertyDefinition;
import org.activiti.workflow.simple.definition.HumanStepDefinition;
import org.activiti.workflow.simple.definition.StepDefinition;

/**
 * @author Frederik Heremans
 * @author Joram Barrez
 */
public class HumanStepDefinitionConverter extends BaseStepDefinitionConverter<HumanStepDefinition, UserTask> {

  private static final String USER_TASK_PREFIX = "userTask";

  private static final String INITIATOR_ASSIGNEE_EXPRESSION = "${initiator.properties.userName}";

  public Class< ? extends StepDefinition> getHandledClass() {
    return HumanStepDefinition.class;
  }
  
  protected UserTask createProcessArtifact(HumanStepDefinition stepDefinition, WorkflowDefinitionConversion conversion) {
    UserTask userTask = createUserTask(stepDefinition, conversion);
    addFlowElement(conversion, userTask);
    
    return userTask;
  }

  protected UserTask createUserTask(HumanStepDefinition humanStepDefinition, WorkflowDefinitionConversion conversion) {
    
    // TODO: validate and throw exception on missing properties
    
    UserTask userTask = new UserTask();
    userTask.setId(conversion.getUniqueNumberedId(USER_TASK_PREFIX));
    userTask.setName(humanStepDefinition.getName());
    userTask.setDocumentation(humanStepDefinition.getDescription());

    // Initiator
    if (humanStepDefinition.isAssigneeInitiator()) {
      userTask.setAssignee(INITIATOR_ASSIGNEE_EXPRESSION);
      
    // Assignee  
    } else if (humanStepDefinition.getAssignee() != null) {
      userTask.setAssignee(humanStepDefinition.getAssignee());
    }

    // Candidate Users
    if (humanStepDefinition.getCandidateUsers() != null && humanStepDefinition.getCandidateUsers().size() > 0) {
      userTask.setCandidateUsers(humanStepDefinition.getCandidateUsers());
    }

    // Candidate groups
    if (humanStepDefinition.getCandidateGroups() != null && humanStepDefinition.getCandidateGroups().size() > 0) {
      userTask.setCandidateGroups(humanStepDefinition.getCandidateGroups());
    }
    
    // Form
    if (humanStepDefinition.getForm() != null) {
      
      FormDefinition formDefinition = humanStepDefinition.getForm();
      
      // Form key
      userTask.setFormKey(formDefinition.getFormKey());
      
      // Form properties
      for (FormPropertyDefinition propertyDefinition : formDefinition.getFormProperties()) {
        FormProperty formProperty = new FormProperty();
        formProperty.setId(propertyDefinition.getPropertyName());
        formProperty.setName(propertyDefinition.getPropertyName());
        formProperty.setRequired(propertyDefinition.isRequired());
        
        String type = "string";
        if ("number".equals(propertyDefinition.getType())) {
          type = "long";
        } else if ("date".equals(propertyDefinition.getType())) {
          type = "date";
        }
        formProperty.setType(type);
        
        userTask.getFormProperties().add(formProperty);
      }
      
    }

    return userTask;
  }
  
}

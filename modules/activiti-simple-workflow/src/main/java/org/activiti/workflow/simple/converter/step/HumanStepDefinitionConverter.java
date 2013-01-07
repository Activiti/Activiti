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

import org.activiti.bpmn.model.UserTask;
import org.activiti.workflow.simple.converter.WorkflowDefinitionConversion;
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

    if (humanStepDefinition.isAssigneeInitiator()) {
      userTask.setAssignee(INITIATOR_ASSIGNEE_EXPRESSION);
    } else if (humanStepDefinition.getAssignee() != null) {
      userTask.setAssignee(humanStepDefinition.getAssignee());
    }

    if (humanStepDefinition.getCandidateUsers() != null && humanStepDefinition.getCandidateUsers().size() > 0) {
      userTask.setCandidateUsers(humanStepDefinition.getCandidateUsers());
    }

    if (humanStepDefinition.getCandidateGroups() != null && humanStepDefinition.getCandidateGroups().size() > 0) {
      userTask.setCandidateGroups(humanStepDefinition.getCandidateGroups());
    }

    return userTask;
  }

}

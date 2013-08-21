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
package org.activiti.workflow.simple.definition;

import java.util.List;

import org.activiti.workflow.simple.definition.HumanStepAssignment.HumanStepAssignmentType;
import org.activiti.workflow.simple.definition.form.FormDefinition;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * Defines a step that must be executed by a human actor.
 * 
 * @author Joram Barrez
 */
@JsonTypeName("human-step")
public class HumanStepDefinition extends AbstractNamedStepDefinition {

  private static final long serialVersionUID = 1L;
  
  protected FormDefinition form;
  protected HumanStepAssignment assignment;
  
  @JsonIgnore
  public HumanStepAssignmentType getAssignmentType() {
  	return ensureAssignment().getType();
  }

  public String getAssignee() {
    return ensureAssignment().getAssignee();
  }

  public HumanStepDefinition setAssignee(String assignee) {
  	ensureAssignment().setAssignee(assignee);
    return this;
  }

  public List<String> getCandidateUsers() {
    return ensureAssignment().getCandidateUsers();
  }

  public HumanStepDefinition setCandidateUsers(List<String> candidateUsers) {
  	ensureAssignment().setCandidateUsers(candidateUsers);
    return this;
  }

  public List<String> getCandidateGroups() {
    return ensureAssignment().getCandidateGroups();
  }

  public HumanStepDefinition setCandidateGroups(List<String> candidateGroups) {
  	ensureAssignment().setCandidateGroups(candidateGroups);
    return this;
  }

  public FormDefinition getForm() {
    return form;
  }

  public HumanStepDefinition setForm(FormDefinition form) {
    this.form = form;
    return this;
  }
  
  public HumanStepAssignment getAssignment() {
	  return ensureAssignment();
  }
  
  public void setAssignment(HumanStepAssignment assignment) {
	  this.assignment = assignment;
  }
  
  protected HumanStepAssignment ensureAssignment() {
  	if(assignment == null) {
  		assignment = new HumanStepAssignment();
  	}
  	return assignment;
  }
}

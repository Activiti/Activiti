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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.activiti.workflow.simple.definition.HumanStepAssignment.HumanStepAssignmentType;
import org.activiti.workflow.simple.definition.form.FormDefinition;
import org.activiti.workflow.simple.exception.SimpleWorkflowException;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Defines a step that must be executed by a human actor.
 * 
 * @author Joram Barrez
 */
@JsonTypeName("human-step")
public class HumanStepDefinition extends AbstractNamedStepDefinition implements FormStepDefinition {

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

  public HumanStepDefinition addForm(FormDefinition form) {
    this.form = form;
    return this;
  }
  
  public void setForm(FormDefinition form) {
    this.form = form;
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

  @Override
  public StepDefinition clone() {
    HumanStepDefinition clone = new HumanStepDefinition();
    clone.setValues(this);
    return clone;
  }
  
  @Override
  public void setValues(StepDefinition otherDefinition) {
    if(!(otherDefinition instanceof HumanStepDefinition)) {
      throw new SimpleWorkflowException("An instance of HumanStepDefinition is required to set values");
    }
    
    HumanStepDefinition stepDefinition = (HumanStepDefinition) otherDefinition;
    setAssignee(stepDefinition.getAssignee());
    if (stepDefinition.getCandidateGroups() != null && !stepDefinition.getCandidateGroups().isEmpty()) {
      setCandidateGroups(new ArrayList<String>(stepDefinition.getCandidateGroups()));
    }
    if (stepDefinition.getCandidateUsers() != null && !stepDefinition.getCandidateUsers().isEmpty()) {
      setCandidateUsers(new ArrayList<String>(stepDefinition.getCandidateUsers()));
    }
    setDescription(stepDefinition.getDescription());
    if (stepDefinition.getForm() != null) {
      setForm(stepDefinition.getForm().clone());
    } else {
      setForm(null);
    }
    setId(stepDefinition.getId());
    setName(stepDefinition.getName());
    setStartsWithPrevious(stepDefinition.isStartsWithPrevious());
    getAssignment().setType(stepDefinition.getAssignmentType());
    
    setParameters(new HashMap<String, Object>(otherDefinition.getParameters()));
  }
}

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
import java.util.Map;

import org.activiti.workflow.simple.exception.SimpleWorkflowException;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion;

/**
 * Defines a block of steps of which one collection of steps is executed based on conditions.
 * 
 * @author Tijs Rademakers
 */
@JsonTypeName("choice-step")
public class ChoiceStepsDefinition extends AbstractConditionStepListContainer<ChoiceStepsDefinition> implements StepDefinition {

  private static final long serialVersionUID = 1L;
  
	protected WorkflowDefinition workflowDefinition;
	protected Map<String, Object> parameters = new HashMap<String, Object>();
  
  public ChoiceStepsDefinition() {
    
  }
  
  public ChoiceStepsDefinition(WorkflowDefinition workflowDefinition) {
    this.workflowDefinition = workflowDefinition;
  }
  
  public WorkflowDefinition endChoice() {
    if (workflowDefinition == null) {
      throw new SimpleWorkflowException("Can only call endChoice when inChoice was called on a workflow definition first");
    }
    return workflowDefinition;
  }
  
  @Override
  public StepDefinition clone() {
    ChoiceStepsDefinition clone = new ChoiceStepsDefinition();
    clone.setValues(this);
    return clone;
  }
  
  @Override
  public void setValues(StepDefinition otherDefinition) {
    if(!(otherDefinition instanceof ChoiceStepsDefinition)) {
      throw new SimpleWorkflowException("An instance of ParallelStepsDefinition is required to set values");
    }
    
    ChoiceStepsDefinition definition = (ChoiceStepsDefinition) otherDefinition;
    setId(definition.getId());

    setParameters(new HashMap<String, Object>(otherDefinition.getParameters()));
    
    steps = new ArrayList<ListConditionStepDefinition<ChoiceStepsDefinition>>();
    if (definition.getStepList() != null && !definition.getStepList().isEmpty()) {
      for (ListConditionStepDefinition<ChoiceStepsDefinition> stepDefinition : definition.getStepList()) {
        steps.add(stepDefinition.clone());
      }
    }
  }
  
  @Override
  @JsonSerialize(include=Inclusion.NON_EMPTY)
  public Map<String, Object> getParameters() {
  	return parameters;
  }
  
  public void setParameters(Map<String,Object> parameters) {
  	this.parameters = parameters;
  }
}

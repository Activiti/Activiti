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
import java.util.Map;

import org.activiti.workflow.simple.exception.SimpleWorkflowException;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion;


/**
 * @author Tijs Rademakers
 */
@SuppressWarnings("unchecked")
@JsonTypeName("list")
public class ListConditionStepDefinition<T> extends AbstractStepDefinitionContainer<ListConditionStepDefinition<T>> implements StepDefinition, NamedStepDefinition {

  private static final long serialVersionUID = 1L;
  
  protected ConditionStepListContainer<T> stepListContainer;
  protected List<ConditionDefinition> conditions = new ArrayList<ConditionDefinition>();
  protected String name;
  protected Map<String, Object> parameters = new HashMap<String, Object>();

	protected String description;
  
  public ListConditionStepDefinition() {
    super();
  }
  
  public ListConditionStepDefinition(ConditionStepListContainer<T> stepListContainer) {
    this.stepListContainer = stepListContainer;
  }
  
  public ListConditionStepDefinition<T> addCondition(String leftOperand, String operator, String rightOperand) {
    ConditionDefinition condition = new ConditionDefinition();
    condition.setLeftOperand(leftOperand);
    condition.setOperator(operator);
    condition.setRightOperand(rightOperand);
    this.conditions.add(condition);
    return this;
  }
  
  public T endList() {
    if (stepListContainer == null) {
      throw new SimpleWorkflowException("Can only call endList when inList was called on a workflow definition first");
    }
    
    return (T) stepListContainer;
  }
  
  @Override
  public String getDescription() {
	  return description;
  }

	@Override
  public void setDescription(String description) {
	  this.description = description;
  }
  
  @Override
  public ListConditionStepDefinition<T> clone() {
    ListConditionStepDefinition<T> clone = new ListConditionStepDefinition<T>();
    clone.setValues(this);
    return clone;
  }
  
  @Override
  public void setValues(StepDefinition otherDefinition) {
    if(!(otherDefinition instanceof ListConditionStepDefinition)) {
      throw new SimpleWorkflowException("An instance of SerialStepsDefinition is required to set values");
    }
    
    ListConditionStepDefinition<T> definition = (ListConditionStepDefinition<T>) otherDefinition;
    setId(definition.getId());
    setName(definition.getName());

    setParameters(new HashMap<String, Object>(otherDefinition.getParameters()));
    
    steps = new ArrayList<StepDefinition>();
    if (definition.getSteps() != null && !definition.getSteps().isEmpty()) {
      for (StepDefinition stepDefinition : definition.getSteps()) {
        steps.add(stepDefinition.clone());
      }
    }
    
    conditions = new ArrayList<ConditionDefinition>();
    if (definition.getConditions() != null && !definition.getConditions().isEmpty()) {
      for (ConditionDefinition condition : definition.getConditions()) {
        conditions.add(condition.clone());
      }
    }
  }

  public List<ConditionDefinition> getConditions() {
    return conditions;
  }

  public void setConditions(List<ConditionDefinition> conditions) {
    this.conditions = conditions;
  }
  
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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

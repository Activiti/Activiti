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
 * @author Tijs Rademakers
 */
@SuppressWarnings("unchecked")
@JsonTypeName("list")
public class ListStepDefinition<T> extends AbstractStepDefinitionContainer<ListStepDefinition<T>> implements StepDefinition {

  private static final long serialVersionUID = 1L;
  
  protected StepListContainer<T> stepListContainer;
  protected String name;
  protected Map<String, Object> parameters = new HashMap<String, Object>();
  
  public ListStepDefinition() {}
  
  public ListStepDefinition(StepListContainer<T> stepListContainer) {
    this.stepListContainer = stepListContainer;
  }
  
  public T endList() {
    if (stepListContainer == null) {
      throw new SimpleWorkflowException("Can only call endList when inList was called on a workflow definition first");
    }
    
    return (T) stepListContainer;
  }

  @Override
  public ListStepDefinition<T> clone() {
    ListStepDefinition<T> clone = new ListStepDefinition<T>();
    clone.setValues(this);
    return clone;
  }
  
  @Override
  public void setValues(StepDefinition otherDefinition) {
    if(!(otherDefinition instanceof ListStepDefinition)) {
      throw new SimpleWorkflowException("An instance of SerialStepsDefinition is required to set values");
    }
    
    ListStepDefinition<T> definition = (ListStepDefinition<T>) otherDefinition;
    setId(definition.getId());
    setName(definition.getName());

    setParameters(new HashMap<String, Object>(otherDefinition.getParameters()));
    
    steps = new ArrayList<StepDefinition>();
    if (definition.getSteps() != null && !definition.getSteps().isEmpty()) {
      for (StepDefinition stepDefinition : definition.getSteps()) {
        steps.add(stepDefinition.clone());
      }
    }
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

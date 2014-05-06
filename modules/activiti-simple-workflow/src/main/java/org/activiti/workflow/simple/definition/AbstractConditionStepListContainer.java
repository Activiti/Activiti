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
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @author Tijs Rademakers
 */
public abstract class AbstractConditionStepListContainer<T> implements ConditionStepListContainer<T> {
  
  protected String id;
  protected List<ListConditionStepDefinition<T>> steps;
  protected ListConditionStepDefinition<T> currentListStepDefinition;
  
  public AbstractConditionStepListContainer() {
    this.steps = new ArrayList<ListConditionStepDefinition<T>>();
  }
    
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void addStepList(ListConditionStepDefinition<T> stepDefinition) {
    steps.add(stepDefinition);
  }

  @JsonSerialize(contentAs=ListConditionStepDefinition.class)
  public List<ListConditionStepDefinition<T>> getStepList() {
    return steps;
  }
  
  public ListConditionStepDefinition<T> inList() {
    currentListStepDefinition = new ListConditionStepDefinition<T>(this);
    addStepList(currentListStepDefinition);
    return currentListStepDefinition;
  }
}

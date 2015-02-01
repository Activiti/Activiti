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
public abstract class AbstractStepListContainer<T> implements StepListContainer<T> {
  
  protected String id;
  protected List<ListStepDefinition<T>> steps;
  protected ListStepDefinition<T> currentListStepDefinition;
  
  public AbstractStepListContainer() {
    this.steps = new ArrayList<ListStepDefinition<T>>();
  }
    
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void addStepList(ListStepDefinition<T> stepDefinition) {
    steps.add(stepDefinition);
  }

  @JsonSerialize(contentAs=ListStepDefinition.class)
  public List<ListStepDefinition<T>> getStepList() {
    return steps;
  }
  
  public ListStepDefinition<T> inList() {
    currentListStepDefinition = new ListStepDefinition<T>(this);
    addStepList(currentListStepDefinition);
    return currentListStepDefinition;
  }
}

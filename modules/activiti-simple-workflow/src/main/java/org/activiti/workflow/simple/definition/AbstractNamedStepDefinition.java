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

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion;

/**
 * Superclass for all {@link StepDefinition} classes that have a name or description.
 * 
 * @author Joram Barrez
 * @author Tijs Rademakers
 */
public abstract class AbstractNamedStepDefinition implements StepDefinition, NamedStepDefinition {

  private static final long serialVersionUID = 1L;
  
  protected String id;
	protected String name;
  protected String description;
  protected boolean startsWithPrevious;
  protected Map<String, Object> parameters = new HashMap<String, Object>();

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean isStartsWithPrevious() {
    return startsWithPrevious;
  }

  public void setStartsWithPrevious(boolean startsWithPrevious) {
    this.startsWithPrevious = startsWithPrevious;
  }
  
  @Override
  @JsonSerialize(include=Inclusion.NON_EMPTY)
  public Map<String, Object> getParameters() {
  	return parameters;
  }
  
  @Override
  public void setParameters(Map<String, Object> parameters) {
  	this.parameters = parameters;
  }
  
  public abstract StepDefinition clone();
  
  public abstract void setValues(StepDefinition otherDefinition);
}

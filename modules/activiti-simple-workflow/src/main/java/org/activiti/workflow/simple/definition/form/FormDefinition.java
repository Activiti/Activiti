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
package org.activiti.workflow.simple.definition.form;

import java.util.ArrayList;
import java.util.List;

import org.activiti.workflow.simple.definition.HumanStepDefinition;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * A {@link HumanStepDefinition} can have a form associated with it 
 * that a user must complete to continue the workflow.
 * Such a form contains {@link FormPropertyDefinition}s and {@link FormPropertyGroup}s  
 * or potentially a form key, when the properties are not used.
 * 
 * @author Joram Barrez
 * @author Frederik Heremans
 */
public class FormDefinition implements FormPropertyDefinitionContainer {

	protected String formKey;
  protected List<FormPropertyDefinition> formProperties = new ArrayList<FormPropertyDefinition>();
  protected List<FormPropertyGroup> formGroups = new ArrayList<FormPropertyGroup>();

  /**
   * @return All {@link FormPropertyDefinition}s that are not part of any {@link FormPropertyGroup}.
   */
  @JsonSerialize(contentAs=FormPropertyDefinition.class)
  @JsonProperty(value="formProperties")
  public List<FormPropertyDefinition> getFormPropertyDefinitions() {
    return formProperties;
  }

  public void getFormPropertyDefinitions(List<FormPropertyDefinition> formProperties) {
    this.formProperties = formProperties;
  }

  /**
   * Adds a form property to the form, not part of any group.
   * @param formProperty the property to add.
   */
  public void addFormProperty(FormPropertyDefinition definition) {
    formProperties.add(definition);
  }
  
  @Override
  public boolean removeFormProperty(FormPropertyDefinition definition) {
    return formProperties.remove(definition);
  }
  
  @JsonSerialize(contentAs=FormPropertyGroup.class)
  public List<FormPropertyGroup> getFormGroups() {
	  return formGroups;
  }
  
  public void setFormGroups(List<FormPropertyGroup> formGroups) {
	  this.formGroups = formGroups;
  }
  
  /**
   * Adds a group of properties to this form.
   * @param group the group to add.
   */
  public void addFormPropertyGroup(FormPropertyGroup group) {
  	this.formGroups.add(group);
  }
  
  public void setFormKey(String formKey) {
	  this.formKey = formKey;
  }
  
  public String getFormKey() {
	  return formKey;
  }
}

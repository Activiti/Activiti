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
import org.activiti.workflow.simple.exception.SimpleWorkflowException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * A {@link HumanStepDefinition} (or other) can have a form associated with it 
 * that a user must complete to continue the workflow.
 * Such a form contains {@link FormPropertyDefinition}s and {@link FormPropertyGroup}s  
 * or potentially a form key, when the properties are not used.
 * 
 * @author Joram Barrez
 * @author Frederik Heremans
 */
public class FormDefinition implements FormPropertyDefinitionContainer {

  protected String description;
  protected String formKey;
  protected List<FormPropertyDefinition> formProperties = new ArrayList<FormPropertyDefinition>();
  protected List<FormPropertyGroup> formGroups = new ArrayList<FormPropertyGroup>();

  public String getDescription() {
	return description;
  }

  public void setDescription(String description) {
	this.description = description;
  }

/**
   * @return All {@link FormPropertyDefinition}s that are not part of any {@link FormPropertyGroup}.
   */
  @JsonDeserialize(contentAs=FormPropertyDefinition.class)
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
  
  public FormDefinition clone() {
    FormDefinition clone = new FormDefinition();
    clone.setValues(this);
    return clone;
  }
  
  public void setValues(FormDefinition otherDefinition) {
    if(!(otherDefinition instanceof FormDefinition)) {
      throw new SimpleWorkflowException("An instance of FormDefinition is required to set values");
    }
    
    FormDefinition formDefinition = (FormDefinition) otherDefinition;
    setFormKey(formDefinition.getFormKey());
    setDescription(formDefinition.getDescription());
    
    List<FormPropertyGroup> groupList = new ArrayList<FormPropertyGroup>();
    if (formDefinition.getFormGroups() != null && !formDefinition.getFormGroups().isEmpty()) {
      for (FormPropertyGroup propertyGroup : formDefinition.getFormGroups()) {
        groupList.add(propertyGroup.clone());
      }
    }
    setFormGroups(groupList);
    
    formProperties = new ArrayList<FormPropertyDefinition>();
    if (formDefinition.getFormGroups() != null && !formDefinition.getFormGroups().isEmpty()) {
      for (FormPropertyDefinition propertyDefinition : formDefinition.getFormPropertyDefinitions()) {
        formProperties.add(propertyDefinition.clone());
      }
    }
  }
}

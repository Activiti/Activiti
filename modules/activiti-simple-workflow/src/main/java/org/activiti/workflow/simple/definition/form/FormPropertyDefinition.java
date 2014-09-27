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

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 * Defines one property in a {@link FormDefinition}.
 * 
 * @author Joram Barrez
 * @author Frederik Heremans
 */
@JsonTypeInfo(use=Id.NAME, include=As.PROPERTY, property="type")
public abstract class FormPropertyDefinition {

  protected String name;
  protected String displayName;
  protected boolean mandatory;
  protected boolean writable;
  protected String type;
  protected String value;
  
  protected Map<String, Object> parameters = new HashMap<String, Object>(); 
  
  public String getName() {
    return name;
  }

  public void setName(String propertyName) {
    this.name = propertyName;
  }

  public String getDisplayName() {
	return displayName;
  }

  public void setDisplayName(String displayName) {
	this.displayName = displayName;
  }

public boolean isMandatory() {
    return mandatory;
  }

  public void setMandatory(boolean required) {
    this.mandatory = required;
  }
  
  public boolean isWritable() {
	  return writable;
  }
  
  public void setWritable(boolean writable) {
	  this.writable = writable;
  }
  
  public String getType() {
	return type;
  }

  public void setType(String type) {
	this.type = type;
  }
  
  
  public String getValue() {
	return value;
  }

  public void setValue(String value) {
	this.value = value;
  }

public void setParameters(Map<String, Object> parameters) {
	  this.parameters = parameters;
  }
  
  @JsonInclude(Include.NON_EMPTY)
  public Map<String, Object> getParameters() {
	  return parameters;
  }
  
  /**
   * Create a close of this {@link FormPropertyDefinition} instance.
   */
  public abstract FormPropertyDefinition clone();
  
  /**
   * Sets the properties of this {@link FormPropertyDefinition} instance based in the
   * properties present in the given definition. 
   */
  public abstract void setValues(FormPropertyDefinition otherDefinition);
  
  protected Map<String, Object> cloneParameters() {
  	Map<String, Object> result = new HashMap<String, Object>();
  	if(parameters != null && !parameters.isEmpty()) {
  		result.putAll(parameters);
  	}
  	return result;
  }
}

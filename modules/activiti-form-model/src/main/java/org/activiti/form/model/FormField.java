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
package org.activiti.form.model;

import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 * @author Joram Barrez
 */
@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "fieldType", defaultImpl = FormField.class)
@JsonSubTypes({ @Type(FormContainer.class), @Type(OptionFormField.class), @Type(ExpressionFormField.class) })
public class FormField implements Serializable {

  private static final long serialVersionUID = 1L;
  
  protected String id;
  protected String name;
  protected String type;
  protected Object value;
  protected boolean required;
  protected boolean readOnly;
  protected boolean overrideId;
  protected String placeholder;
  protected Map<String, Object> params;

  protected LayoutDefinition layout;

  // size of the dropdown
  protected int sizeX;
  protected int sizeY;

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

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Object getValue() {
    return value;
  }

  public void setValue(Object value) {
    this.value = value;
  }

  public boolean isRequired() {
    return required;
  }

  public void setRequired(boolean required) {
    this.required = required;
  }

  public boolean isReadOnly() {
    return readOnly;
  }

  public void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
  }

  public boolean isOverrideId() {
    return overrideId;
  }

  public void setOverrideId(boolean overrideId) {
    this.overrideId = overrideId;
  }

  public String getPlaceholder() {
    return placeholder;
  }

  public void setPlaceholder(String placeholder) {
    this.placeholder = placeholder;
  }

  public LayoutDefinition getLayout() {
    return layout;
  }

  public void setLayout(LayoutDefinition layout) {
    this.layout = layout;
  }

  public int getSizeX() {
	  return sizeX;
  }

  public void setSizeX(int sizeX) {
	  this.sizeX = sizeX;
  }
  
  public int getSizeY() {
    return sizeY;
  }

  public void setSizeY(int sizeY) {
    this.sizeY = sizeY;
  }

  @JsonInclude(Include.NON_EMPTY)
  public Map<String, Object> getParams() {
    return params;
  }

  public void setParams(Map<String, Object> params) {
    this.params = params;
  }

  @JsonIgnore
  public Object getParam(String name) {
    if (params != null) {
      return params.get(name);
    }
    return null;
  }
}

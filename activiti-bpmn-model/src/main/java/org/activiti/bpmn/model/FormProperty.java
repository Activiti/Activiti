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
package org.activiti.bpmn.model;

import java.util.ArrayList;
import java.util.List;

public class FormProperty extends BaseElement {

  protected String name;
  protected String expression;
  protected String variable;
  protected String type;
  protected String defaultExpression;
  protected String datePattern;
  protected boolean readable = true;
  protected boolean writeable = true;
  protected boolean required;
  protected List<FormValue> formValues = new ArrayList<FormValue>();

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getExpression() {
    return expression;
  }

  public void setExpression(String expression) {
    this.expression = expression;
  }

  public String getVariable() {
    return variable;
  }

  public void setVariable(String variable) {
    this.variable = variable;
  }

  public String getType() {
    return type;
  }

  public String getDefaultExpression() {
    return defaultExpression;
  }

  public void setDefaultExpression(String defaultExpression) {
    this.defaultExpression = defaultExpression;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getDatePattern() {
    return datePattern;
  }

  public void setDatePattern(String datePattern) {
    this.datePattern = datePattern;
  }

  public boolean isReadable() {
    return readable;
  }

  public void setReadable(boolean readable) {
    this.readable = readable;
  }

  public boolean isWriteable() {
    return writeable;
  }

  public void setWriteable(boolean writeable) {
    this.writeable = writeable;
  }

  public boolean isRequired() {
    return required;
  }

  public void setRequired(boolean required) {
    this.required = required;
  }

  public List<FormValue> getFormValues() {
    return formValues;
  }

  public void setFormValues(List<FormValue> formValues) {
    this.formValues = formValues;
  }

  public FormProperty clone() {
    FormProperty clone = new FormProperty();
    clone.setValues(this);
    return clone;
  }

  public void setValues(FormProperty otherProperty) {
    super.setValues(otherProperty);
    setName(otherProperty.getName());
    setExpression(otherProperty.getExpression());
    setVariable(otherProperty.getVariable());
    setType(otherProperty.getType());
    setDefaultExpression(otherProperty.getDefaultExpression());
    setDatePattern(otherProperty.getDatePattern());
    setReadable(otherProperty.isReadable());
    setWriteable(otherProperty.isWriteable());
    setRequired(otherProperty.isRequired());

    formValues = new ArrayList<FormValue>();
    if (otherProperty.getFormValues() != null && !otherProperty.getFormValues().isEmpty()) {
      for (FormValue formValue : otherProperty.getFormValues()) {
        formValues.add(formValue.clone());
      }
    }
  }
}

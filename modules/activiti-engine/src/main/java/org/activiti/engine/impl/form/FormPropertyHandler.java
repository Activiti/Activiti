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

package org.activiti.engine.impl.form;

import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.impl.runtime.ExecutionEntity;


/**
 * @author Tom Baeyens
 */
public class FormPropertyHandler {

  protected String id;
  protected String name;
  protected AbstractFormType type;
  protected boolean isReadable;
  protected boolean isWritable;
  protected boolean isRequired;
  protected String variableName;
  protected Expression variableExpression;
  
  public FormProperty createFormProperty(ExecutionEntity execution) {
    FormPropertyImpl formProperty = new FormPropertyImpl(this);

    if (execution!=null) {
      Object modelValue = null;
      if (variableName != null) {
        modelValue = execution.getVariable(variableName);
      } else if (variableExpression != null) {
        modelValue = variableExpression.getValue(execution);
      } else {
        modelValue = execution.getVariable(id);
      }
      if (type != null) {
        String formValue = type.convertModelValueToFormValue(modelValue);
        formProperty.setValue(formValue);
      } else if (modelValue != null) {
        formProperty.setValue(modelValue.toString());
      }
    }
    
    return formProperty;
  }

  public void submitFormProperty(ExecutionEntity execution, Map<String, String> properties) {
    if (!isWritable && properties.containsKey(id)) {
      throw new ActivitiException("form property '"+id+"' is not writable");
    }
    
    if (isRequired && !properties.containsKey(id)) {
      throw new ActivitiException("form property '"+id+"' is required");
    }
    
    if (properties.containsKey(id)) {
      String propertyValue = properties.remove(id);
      
      Object modelValue;
      if (type!=null) {
        modelValue = type.convertFormValueToModelValue(propertyValue);
      } else {
        modelValue = propertyValue;
      }

      if (variableName!=null) {
        execution.setVariable(variableName, modelValue);
      } else if (variableExpression!=null) {
        variableExpression.setValue(modelValue, execution);
      } else {
        execution.setVariable(id, modelValue);
      }
    }
  }

  // getters and setters //////////////////////////////////////////////////////
  
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
  
  public AbstractFormType getType() {
    return type;
  }
  
  public void setType(AbstractFormType type) {
    this.type = type;
  }
  
  public boolean isReadable() {
    return isReadable;
  }
  
  public void setReadable(boolean isReadable) {
    this.isReadable = isReadable;
  }
  
  public boolean isRequired() {
    return isRequired;
  }
  
  public void setRequired(boolean isRequired) {
    this.isRequired = isRequired;
  }
  
  public String getVariableName() {
    return variableName;
  }
  
  public void setVariableName(String variableName) {
    this.variableName = variableName;
  }
  
  public Expression getVariableExpression() {
    return variableExpression;
  }
  
  public void setVariableExpression(Expression variableExpression) {
    this.variableExpression = variableExpression;
  }
  
  public boolean isWritable() {
    return isWritable;
  }

  public void setWritable(boolean isWritable) {
    this.isWritable = isWritable;
  }
}

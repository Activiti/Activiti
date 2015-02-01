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

import java.io.Serializable;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.form.AbstractFormType;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.impl.el.NoExecutionVariableScope;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;


/**
 * @author Tom Baeyens
 */
public class FormPropertyHandler implements Serializable {

  private static final long serialVersionUID = 1L;
  
  protected String id;
  protected String name;
  protected AbstractFormType type;
  protected boolean isReadable;
  protected boolean isWritable;
  protected boolean isRequired;
  protected String variableName;
  protected Expression variableExpression;
  protected Expression defaultExpression;
  
  public FormProperty createFormProperty(ExecutionEntity execution) {
    FormPropertyImpl formProperty = new FormPropertyImpl(this);
    Object modelValue = null;
    
    if (execution!=null) {
      if (variableName != null || variableExpression == null) {
        final String varName = variableName != null ? variableName : id;
        if (execution.hasVariable(varName)) {
          modelValue = execution.getVariable(varName);
        } else if (defaultExpression != null) {
          modelValue = defaultExpression.getValue(execution);
        }
      } else {
        modelValue = variableExpression.getValue(execution);
      }
    } else {
      // Execution is null, the form-property is used in a start-form. Default value
      // should be available (ACT-1028) even though no execution is available.
      if (defaultExpression != null) {
        modelValue = defaultExpression.getValue(NoExecutionVariableScope.getSharedInstance());
      }
    }

    if (modelValue instanceof String) {
      formProperty.setValue((String) modelValue);
    } else if (type != null) {
      String formValue = type.convertModelValueToFormValue(modelValue);
      formProperty.setValue(formValue);
    } else if (modelValue != null) {
      formProperty.setValue(modelValue.toString());
    }
    
    return formProperty;
  }

  public void submitFormProperty(ExecutionEntity execution, Map<String, String> properties) {
    if (!isWritable && properties.containsKey(id)) {
      throw new ActivitiException("form property '"+id+"' is not writable");
    }
    
    if (isRequired && !properties.containsKey(id) && defaultExpression == null) {
      throw new ActivitiException("form property '"+id+"' is required");
    }
    boolean propertyExits = false;
    Object modelValue = null;
    if (properties.containsKey(id)) {
    	propertyExits = true;
      final String propertyValue = properties.remove(id);
      if (type != null) {
        modelValue = type.convertFormValueToModelValue(propertyValue);
      } else {
        modelValue = propertyValue;
      }
    } else if (defaultExpression != null) {
      final Object expressionValue = defaultExpression.getValue(execution);
      if (type != null && expressionValue != null) {
        modelValue = type.convertFormValueToModelValue(expressionValue.toString());
      } else if (expressionValue != null) {
        modelValue = expressionValue.toString();
      } else if (isRequired) {
        throw new ActivitiException("form property '"+id+"' is required");
      }
    }
    if (propertyExits || (modelValue != null)) {
      if (variableName != null) {
        execution.setVariable(variableName, modelValue);
      } else if (variableExpression != null) {
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
  
  public Expression getDefaultExpression() {
    return defaultExpression;
  }
  
  public void setDefaultExpression(Expression defaultExpression) {
    this.defaultExpression = defaultExpression;
  }
  
  public boolean isWritable() {
    return isWritable;
  }

  public void setWritable(boolean isWritable) {
    this.isWritable = isWritable;
  }
}

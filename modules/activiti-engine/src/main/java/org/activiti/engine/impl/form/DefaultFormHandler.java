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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.delegate.Expression;
import org.activiti.engine.form.AbstractFormType;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.apache.commons.lang3.StringUtils;


/**
 * @author Tom Baeyens
 */
public class DefaultFormHandler implements FormHandler {
  
  protected Expression formKey;
  protected String deploymentId;
  protected List<FormPropertyHandler> formPropertyHandlers = new ArrayList<FormPropertyHandler>();
  
  public void parseConfiguration(List<org.activiti.bpmn.model.FormProperty> formProperties, String formKey, DeploymentEntity deployment, ProcessDefinitionEntity processDefinition) {
    this.deploymentId = deployment.getId();
    
    ExpressionManager expressionManager = Context
        .getProcessEngineConfiguration()
        .getExpressionManager();
    
    if (StringUtils.isNotEmpty(formKey)) {
      this.formKey = expressionManager.createExpression(formKey);
    }
    
    FormTypes formTypes = Context
      .getProcessEngineConfiguration()
      .getFormTypes();
    
    for (org.activiti.bpmn.model.FormProperty formProperty : formProperties) {
      FormPropertyHandler formPropertyHandler = new FormPropertyHandler();
      formPropertyHandler.setId(formProperty.getId());
      formPropertyHandler.setName(formProperty.getName());
      
      AbstractFormType type = formTypes.parseFormPropertyType(formProperty);
      formPropertyHandler.setType(type);
      formPropertyHandler.setRequired(formProperty.isRequired());
      formPropertyHandler.setReadable(formProperty.isReadable());
      formPropertyHandler.setWritable(formProperty.isWriteable());
      formPropertyHandler.setVariableName(formProperty.getVariable());

      if (StringUtils.isNotEmpty(formProperty.getExpression())) {
        Expression expression = expressionManager.createExpression(formProperty.getExpression());
        formPropertyHandler.setVariableExpression(expression);
      }

      if (StringUtils.isNotEmpty(formProperty.getDefaultExpression())) {
        Expression defaultExpression = expressionManager.createExpression(formProperty.getDefaultExpression());
        formPropertyHandler.setDefaultExpression(defaultExpression);
      }

      formPropertyHandlers.add(formPropertyHandler);
    }
  }

  protected void initializeFormProperties(FormDataImpl formData, ExecutionEntity execution) {
    List<FormProperty> formProperties = new ArrayList<FormProperty>();
    for (FormPropertyHandler formPropertyHandler: formPropertyHandlers) {
      if (formPropertyHandler.isReadable()) {
        FormProperty formProperty = formPropertyHandler.createFormProperty(execution);
        formProperties.add(formProperty);
      }
    }
    formData.setFormProperties(formProperties);
  }

  public void submitFormProperties(Map<String, String> properties, ExecutionEntity execution) {
    Map<String, String> propertiesCopy = new HashMap<String, String>(properties);
    for (FormPropertyHandler formPropertyHandler: formPropertyHandlers) {
      // submitFormProperty will remove all the keys which it takes care of
      formPropertyHandler.submitFormProperty(execution, propertiesCopy);
    }
    for (String propertyId: propertiesCopy.keySet()) {
      execution.setVariable(propertyId, propertiesCopy.get(propertyId));
    }
  }


  // getters and setters //////////////////////////////////////////////////////
  
  public Expression getFormKey() {
    return formKey;
  }
  
  public void setFormKey(Expression formKey) {
    this.formKey = formKey;
  }
  
  public String getDeploymentId() {
    return deploymentId;
  }
  
  public void setDeploymentId(String deploymentId) {
    this.deploymentId = deploymentId;
  }
  
  public List<FormPropertyHandler> getFormPropertyHandlers() {
    return formPropertyHandlers;
  }
  
  public void setFormPropertyHandlers(List<FormPropertyHandler> formPropertyHandlers) {
    this.formPropertyHandlers = formPropertyHandlers;
  }
}

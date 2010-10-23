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

import org.activiti.engine.form.FormProperty;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.BpmnParser;
import org.activiti.engine.impl.el.ActivitiValueExpression;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.repository.DeploymentEntity;
import org.activiti.engine.impl.runtime.ExecutionEntity;
import org.activiti.engine.impl.util.xml.Element;


/**
 * @author Tom Baeyens
 */
public class DefaultFormHandler {

  protected String formKey;
  protected String deploymentId;
  protected List<FormPropertyHandler> formPropertyHandlers = new ArrayList<FormPropertyHandler>();
  
  public void parseConfiguration(Element activityElement, DeploymentEntity deployment, BpmnParse bpmnParse) {
    this.deploymentId = deployment.getId();
    this.formKey = activityElement.attributeNS(BpmnParser.BPMN_EXTENSIONS_NS, "formKey");
    Element extensionElement = activityElement.element("extensionElements");
    if (extensionElement != null) {

      CommandContext commandContext = CommandContext.getCurrent();
      ExpressionManager expressionManager = commandContext
        .getProcessEngineConfiguration()
        .getExpressionManager();
      
      FormTypes formTypes = commandContext
        .getProcessEngineConfiguration()
        .getFormTypes();
    
      List<Element> formPropertyElements = extensionElement.elementsNS(BpmnParser.BPMN_EXTENSIONS_NS, "formProperty");
      for (Element formPropertyElement : formPropertyElements) {
        FormPropertyHandler formProperty = new FormPropertyHandler();
        
        String id = formPropertyElement.attribute("http://activiti.org/bpmn-extensions:id");
        if (id==null) {
          bpmnParse.addError("attribute 'id' is required", formPropertyElement);
        }
        formProperty.setId(id);
        
        String name = formPropertyElement.attribute("http://activiti.org/bpmn-extensions:name");
        formProperty.setName(name);
        
        AbstractFormType type = formTypes.parseFormPropertyType(formPropertyElement, bpmnParse);
        formProperty.setType(type);

        String requiredText = formPropertyElement.attribute("http://activiti.org/bpmn-extensions:required", "false");
        Boolean required = bpmnParse.parseBooleanAttribute(requiredText);
        if (required!=null) {
          formProperty.setRequired(required);
        } else {
          bpmnParse.addError("attribute 'required' must be one of {on|yes|true|enabled|active|off|no|false|disabled|inactive}", formPropertyElement);
        }

        String readableText = formPropertyElement.attribute("readable", "true");
        Boolean readable = bpmnParse.parseBooleanAttribute(readableText);
        if (readable!=null) {
          formProperty.setReadable(readable);
        } else {
          bpmnParse.addError("attribute 'readable' must be one of {on|yes|true|enabled|active|off|no|false|disabled|inactive}", formPropertyElement);
        }
        
        String writableText = formPropertyElement.attribute("http://activiti.org/bpmn-extensions:writable", "true");
        Boolean writable = bpmnParse.parseBooleanAttribute(writableText);
        if (writable!=null) {
          formProperty.setWritable(writable);
        } else {
          bpmnParse.addError("attribute 'writable' must be one of {on|yes|true|enabled|active|off|no|false|disabled|inactive}", formPropertyElement);
        }

        String variableName = formPropertyElement.attribute("http://activiti.org/bpmn-extensions:variable");
        formProperty.setVariableName(variableName);

        String valueExpressionText = formPropertyElement.attribute("http://activiti.org/bpmn-extensions:expression");
        if (valueExpressionText!=null) {
          ActivitiValueExpression valueExpression = expressionManager.createValueExpression(valueExpressionText);
          formProperty.setVariableExpression(valueExpression);
        }

        formPropertyHandlers.add(formProperty);
      }
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

  protected void submitFormProperties(Map<String, String> properties, ExecutionEntity execution) {
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
  
  public String getFormKey() {
    return formKey;
  }
  
  public void setFormKey(String formKey) {
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

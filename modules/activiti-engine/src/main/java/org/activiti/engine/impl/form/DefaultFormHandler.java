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

import java.util.List;

import org.activiti.engine.impl.bpmn.parser.BpmnParser;
import org.activiti.engine.impl.el.ActivitiMethodExpression;
import org.activiti.engine.impl.el.ActivitiValueExpression;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.repository.DeploymentEntity;
import org.activiti.engine.impl.util.xml.Element;


/**
 * @author Tom Baeyens
 */
public class DefaultFormHandler {

  protected String formKey;
  protected String deploymentId;
  protected List<FormDisplayProperty> formDisplayProperties;
  protected List<FormSubmitProperty> formSubmitProperties;
  
  public void parseConfiguration(DeploymentEntity deployment, Element activityElement) {
    this.deploymentId = deployment.getId();
    this.formKey = activityElement.attributeNS(BpmnParser.BPMN_EXTENSIONS_NS, "formKey");
    Element extensionElement = activityElement.element("extensionElements");
    if (extensionElement != null) {
      List<Element> formDisplayPropertyElements = extensionElement.elementsNS(BpmnParser.BPMN_EXTENSIONS_NS, "formDisplay");
      for (Element formDisplayPropertyElement : formDisplayPropertyElements) {
        FormDisplayProperty formDisplayProperty = new FormDisplayProperty();
        
        String destProperty = formDisplayPropertyElement.attribute("destProperty");
        formDisplayProperty.setDestProperty(destProperty);
        
        String srcVariable = formDisplayPropertyElement.attribute("srcVar");
        formDisplayProperty.setSrcVariable(srcVariable);
        
        ExpressionManager expressionManager = CommandContext
          .getCurrent()
          .getProcessEngineConfiguration()
          .getExpressionManager();
        
        String srcValueExpressionText = formDisplayPropertyElement.attribute("srcValueExpr");
        if (srcValueExpressionText!=null) {
          ActivitiValueExpression srcValueExpression = expressionManager.createValueExpression(srcValueExpressionText);
          formDisplayProperty.setSrcValueExpression(srcValueExpression);
        }

        String srcMethodExpressionText = formDisplayPropertyElement.attribute("srcMethodExpr");
        if (srcMethodExpressionText!=null) {
          ActivitiMethodExpression srcMethodExpression = expressionManager.createMethodExpression(srcMethodExpressionText);
          formDisplayProperty.setSrcMethodExpression(srcMethodExpression);
        }
      }
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
  
  public List<FormDisplayProperty> getFormDisplayProperties() {
    return formDisplayProperties;
  }
  
  public void setFormDisplayProperties(List<FormDisplayProperty> formDisplayProperties) {
    this.formDisplayProperties = formDisplayProperties;
  }
  
  public List<FormSubmitProperty> getFormSubmitProperties() {
    return formSubmitProperties;
  }
  
  public void setFormSubmitProperties(List<FormSubmitProperty> formSubmitProperties) {
    this.formSubmitProperties = formSubmitProperties;
  }
}

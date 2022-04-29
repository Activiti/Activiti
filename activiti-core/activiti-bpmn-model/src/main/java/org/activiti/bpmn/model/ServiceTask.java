/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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

public class ServiceTask extends TaskWithFieldExtensions {

  public static final String DMN_TASK = "dmn";
  public static final String MAIL_TASK = "mail";

  protected String implementation;
  protected String implementationType;
  protected String resultVariableName;
  protected String type;
  protected String operationRef;
  protected String extensionId;
  protected List<CustomProperty> customProperties = new ArrayList<CustomProperty>();
  protected String skipExpression;

  public String getImplementation() {
    return implementation;
  }

  public void setImplementation(String implementation) {
    this.implementation = implementation;
  }

  public String getImplementationType() {
    return implementationType;
  }

  public void setImplementationType(String implementationType) {
    this.implementationType = implementationType;
  }

  public String getResultVariableName() {
    return resultVariableName;
  }

  public void setResultVariableName(String resultVariableName) {
    this.resultVariableName = resultVariableName;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public List<CustomProperty> getCustomProperties() {
    return customProperties;
  }

  public void setCustomProperties(List<CustomProperty> customProperties) {
    this.customProperties = customProperties;
  }

  public String getOperationRef() {
    return operationRef;
  }

  public void setOperationRef(String operationRef) {
    this.operationRef = operationRef;
  }

  public String getExtensionId() {
    return extensionId;
  }

  public void setExtensionId(String extensionId) {
    this.extensionId = extensionId;
  }

  public boolean isExtended() {
    return extensionId != null && !extensionId.isEmpty();
  }

  public String getSkipExpression() {
    return skipExpression;
  }

  public void setSkipExpression(String skipExpression) {
    this.skipExpression = skipExpression;
  }

  public ServiceTask clone() {
    ServiceTask clone = new ServiceTask();
    clone.setValues(this);
    return clone;
  }

  public void setValues(ServiceTask otherElement) {
    super.setValues(otherElement);
    setImplementation(otherElement.getImplementation());
    setImplementationType(otherElement.getImplementationType());
    setResultVariableName(otherElement.getResultVariableName());
    setType(otherElement.getType());
    setOperationRef(otherElement.getOperationRef());
    setExtensionId(otherElement.getExtensionId());
    setSkipExpression(otherElement.getSkipExpression());

    fieldExtensions = new ArrayList<FieldExtension>();
    if (otherElement.getFieldExtensions() != null && !otherElement.getFieldExtensions().isEmpty()) {
      for (FieldExtension extension : otherElement.getFieldExtensions()) {
        fieldExtensions.add(extension.clone());
      }
    }

    customProperties = new ArrayList<CustomProperty>();
    if (otherElement.getCustomProperties() != null && !otherElement.getCustomProperties().isEmpty()) {
      for (CustomProperty property : otherElement.getCustomProperties()) {
        customProperties.add(property.clone());
      }
    }
  }
}

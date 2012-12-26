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

/**
 * @author Tijs Rademakers
 */
public class ServiceTask extends Task {

  public static final String MAIL_TASK = "mail";
  
  protected String implementation;
  protected String implementationType;
  protected String resultVariableName;
  protected String type;
  protected String operationRef;
  protected String extensionId;
  protected List<FieldExtension> fieldExtensions = new ArrayList<FieldExtension>();
  protected List<CustomProperty> customProperties = new ArrayList<CustomProperty>();

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
  public List<FieldExtension> getFieldExtensions() {
    return fieldExtensions;
  }
  public void setFieldExtensions(List<FieldExtension> fieldExtensions) {
    this.fieldExtensions = fieldExtensions;
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
}

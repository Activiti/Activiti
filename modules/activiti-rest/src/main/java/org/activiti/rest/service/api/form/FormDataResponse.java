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

package org.activiti.rest.service.api.form;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tijs Rademakers
 */
public class FormDataResponse {
  
  protected String formKey;
  protected String deploymentId;
  protected String processDefinitionId;
  protected String processDefinitionUrl;
  protected String taskId;
  protected String taskUrl;
  protected List<RestFormProperty> formProperties = new ArrayList<RestFormProperty>();
  
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
  public String getProcessDefinitionId() {
    return processDefinitionId;
  }
  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }
  public String getProcessDefinitionUrl() {
    return processDefinitionUrl;
  }
  public void setProcessDefinitionUrl(String processDefinitionUrl) {
    this.processDefinitionUrl = processDefinitionUrl;
  }
  public String getTaskId() {
    return taskId;
  }
  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }
  public String getTaskUrl() {
    return taskUrl;
  }
  public void setTaskUrl(String taskUrl) {
    this.taskUrl = taskUrl;
  }
  public List<RestFormProperty> getFormProperties() {
    return formProperties;
  }
  public void setFormProperties(List<RestFormProperty> formProperties) {
    this.formProperties = formProperties;
  }
  public void addFormProperty(RestFormProperty formProperty) {
    formProperties.add(formProperty);
  }
}

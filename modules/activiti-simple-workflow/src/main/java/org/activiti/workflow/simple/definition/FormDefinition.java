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
package org.activiti.workflow.simple.definition;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Joram Barrez
 */
public class FormDefinition {

  protected String formKey;

  protected List<FormPropertyDefinition> formProperties = new ArrayList<FormPropertyDefinition>();

  public List<FormPropertyDefinition> getFormProperties() {
    return formProperties;
  }

  public void setFormProperties(List<FormPropertyDefinition> formProperties) {
    this.formProperties = formProperties;
  }

  public void addFormProperty(FormPropertyDefinition formProperty) {
    formProperties.add(formProperty);
  }

  public String getFormKey() {
    return formKey;
  }

  public void setFormKey(String formKey) {
    this.formKey = formKey;
  }

}

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
public class ActivitiListener extends BaseElement {

  protected String event;
  protected String implementationType;
  protected String implementation;
  protected List<FieldExtension> fieldExtensions = new ArrayList<FieldExtension>();

  public String getEvent() {
    return event;
  }
  public void setEvent(String event) {
    this.event = event;
  }
  public String getImplementationType() {
    return implementationType;
  }
  public void setImplementationType(String implementationType) {
    this.implementationType = implementationType;
  }
  public String getImplementation() {
    return implementation;
  }
  public void setImplementation(String implementation) {
    this.implementation = implementation;
  }
  public List<FieldExtension> getFieldExtensions() {
    return fieldExtensions;
  }
  public void setFieldExtensions(List<FieldExtension> fieldExtensions) {
    this.fieldExtensions = fieldExtensions;
  }
  
  public ActivitiListener clone() {
    ActivitiListener clone = new ActivitiListener();
    clone.setValues(this);
    return clone;
  }
  
  public void setValues(ActivitiListener otherListener) {
    setEvent(otherListener.getEvent());
    setImplementation(otherListener.getImplementation());
    setImplementationType(otherListener.getImplementationType());
    
    fieldExtensions = new ArrayList<FieldExtension>();
    if (otherListener.getFieldExtensions() != null && !otherListener.getFieldExtensions().isEmpty()) {
      for (FieldExtension extension : otherListener.getFieldExtensions()) {
        fieldExtensions.add(extension.clone());
      }
    }
  }
}

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


/**
 * @author Tijs Rademakers
 */
public class CustomProperty extends BaseElement {

  protected String name;
  protected String simpleValue;
  protected ComplexDataType complexValue;

  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public String getSimpleValue() {
    return simpleValue;
  }
  public void setSimpleValue(String simpleValue) {
    this.simpleValue = simpleValue;
  }
  public ComplexDataType getComplexValue() {
    return complexValue;
  }
  public void setComplexValue(ComplexDataType complexValue) {
    this.complexValue = complexValue;
  }
  
  public CustomProperty clone() {
    CustomProperty clone = new CustomProperty();
    clone.setValues(this);
    return clone;
  }
  
  public void setValues(CustomProperty otherProperty) {
    setName(otherProperty.getName());
    setSimpleValue(otherProperty.getSimpleValue());
    
    if (otherProperty.getComplexValue() != null && otherProperty.getComplexValue() instanceof DataGrid) {
      setComplexValue(((DataGrid) otherProperty.getComplexValue()).clone());
    }
  }
}

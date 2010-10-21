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


/**
 * @author Tom Baeyens
 */
public class FormSubmitProperty {

  protected String srcProperty;
  protected String srcExpression;
  protected String destVariable;
  protected String destExpression;
  protected List<String> ignoreProperties;
  
  public String getSrcProperty() {
    return srcProperty;
  }
  
  public void setSrcProperty(String srcProperty) {
    this.srcProperty = srcProperty;
  }
  
  public String getSrcExpression() {
    return srcExpression;
  }
  
  public void setSrcExpression(String srcExpression) {
    this.srcExpression = srcExpression;
  }
  
  public String getDestVariable() {
    return destVariable;
  }
  
  public void setDestVariable(String destVariable) {
    this.destVariable = destVariable;
  }
  
  public String getDestExpression() {
    return destExpression;
  }
  
  public void setDestExpression(String destExpression) {
    this.destExpression = destExpression;
  }
  
  public List<String> getIgnoreProperties() {
    return ignoreProperties;
  }
  
  public void setIgnoreProperties(List<String> ignoreProperties) {
    this.ignoreProperties = ignoreProperties;
  }
  
  
}

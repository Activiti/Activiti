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

import org.activiti.engine.impl.el.ActivitiMethodExpression;
import org.activiti.engine.impl.el.ActivitiValueExpression;


/**
 * @author Tom Baeyens
 */
public class FormDisplayProperty {

  protected String srcVariable;
  protected ActivitiValueExpression srcValueExpression;
  protected ActivitiMethodExpression srcMethodExpression;
  protected String destProperty;
  
  public String getSrcVariable() {
    return srcVariable;
  }
  
  public void setSrcVariable(String srcVariable) {
    this.srcVariable = srcVariable;
  }
  
  public String getDestProperty() {
    return destProperty;
  }
  
  public void setDestProperty(String destProperty) {
    this.destProperty = destProperty;
  }

  
  public ActivitiValueExpression getSrcValueExpression() {
    return srcValueExpression;
  }

  
  public void setSrcValueExpression(ActivitiValueExpression srcValueExpression) {
    this.srcValueExpression = srcValueExpression;
  }

  
  public ActivitiMethodExpression getSrcMethodExpression() {
    return srcMethodExpression;
  }

  
  public void setSrcMethodExpression(ActivitiMethodExpression srcMethodExpression) {
    this.srcMethodExpression = srcMethodExpression;
  }
}

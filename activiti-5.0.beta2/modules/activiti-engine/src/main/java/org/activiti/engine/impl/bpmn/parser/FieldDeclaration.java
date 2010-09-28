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

package org.activiti.engine.impl.bpmn.parser;

import org.activiti.engine.impl.el.ActivitiValueExpression;


/**
 * Represents a field declaration in object form:
 * 
 * &lt;field name='someField&gt; &lt;string ...
 * 
 * @author Joram Barrez
 */
public class FieldDeclaration {
  
  protected String name;
  protected String type;
  protected ActivitiValueExpression valueExpression;
  
  public FieldDeclaration(String name, String type, ActivitiValueExpression valueExpression) {
    this.name = name;
    this.type = type;
    this.valueExpression = valueExpression;
  }
  
  public FieldDeclaration() {
    
  }

  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }
  public ActivitiValueExpression getValueExpression() {
    return valueExpression;
  }
  public void setValue(ActivitiValueExpression valueExpression) {
    this.valueExpression = valueExpression;
  }
  
}
 
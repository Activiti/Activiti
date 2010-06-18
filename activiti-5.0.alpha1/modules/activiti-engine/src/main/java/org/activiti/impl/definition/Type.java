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
package org.activiti.impl.definition;

import java.io.Serializable;

import org.activiti.ActivitiException;
import org.activiti.impl.variable.VariableInstance;


/**
 * @author Tom Baeyens
 */
public class Type implements Serializable {

  private static final long serialVersionUID = 1L;
  
  protected VariableInstance variableInstance;
  protected Class<? extends VariableInstance> variableInstanceClass;
  
  public Type(Class< ? extends VariableInstance> variableInstanceClass) {
    this.variableInstanceClass = variableInstanceClass;
    this.variableInstance = createVariableInstance();
  }
  
  public String getName() {
    return variableInstance.getTypeName();
  }

  public boolean isAbleToStore(Object value) {
    return variableInstance.isAbleToStore(value);
  }

  public VariableInstance createVariableInstance() {
    try {
      return variableInstanceClass.newInstance();
    } catch (Exception e) {
      throw new ActivitiException("couldn't instantiate varaible instance "+variableInstanceClass, e);
    }
  }
}

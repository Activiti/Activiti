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

package org.activiti.explorer.form;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.impl.form.AbstractFormType;


/**
 * Form type that holds an ID of a user.
 * 
 * @author 'Frederik Heremans'
 */
public class UserFormType extends AbstractFormType {

  public static final String TYPE_NAME = "user";
  
  public String getName() {
    return TYPE_NAME;
  }

  @Override
  public Object convertFormValueToModelValue(String propertyValue) {
    // Check if user exists
    if(propertyValue != null) {
      // TODO: perhaps better wiring mechanism for service
      long count = ProcessEngines.getDefaultProcessEngine()
      .getIdentityService()
      .createUserQuery()
      .userId(propertyValue).count();
      
      if(count == 0) {
        throw new ActivitiException("User " + propertyValue + " does not exist");
      }
      return propertyValue;
    }
    return null;
  }

  @Override
  public String convertModelValueToFormValue(Object modelValue) {
    return (String) modelValue;
  }
}

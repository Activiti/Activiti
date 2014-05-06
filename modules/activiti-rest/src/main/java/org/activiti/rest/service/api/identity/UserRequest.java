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

package org.activiti.rest.service.api.identity;

import com.fasterxml.jackson.annotation.JsonIgnore;


/**
 * @author Frederik Heremans
 */
public class UserRequest extends UserResponse {

  protected boolean firstNameChanged = false;
  protected boolean lastNameChanged = false;
  protected boolean passwordChanged = false;
  protected boolean emailChanged = false;
  
  @Override
  public void setEmail(String email) {
    super.setEmail(email);
    emailChanged = true;
  }
  
  @Override
  public void setFirstName(String firstName) {
    super.setFirstName(firstName);
    firstNameChanged = true;
  }
  
  @Override
  public void setLastName(String lastName) {
    super.setLastName(lastName);
    lastNameChanged = true;
  }
  
  @Override
  public void setPassword(String passWord) {
    super.setPassword(passWord);
    passwordChanged = true;
  }
  
  @JsonIgnore
  public boolean isEmailChanged() {
    return emailChanged;
  }
  @JsonIgnore
  public boolean isFirstNameChanged() {
    return firstNameChanged;
  }
  @JsonIgnore
  public boolean isLastNameChanged() {
    return lastNameChanged;
  }
  @JsonIgnore
  public boolean isPasswordChanged() {
    return passwordChanged;
  }
}

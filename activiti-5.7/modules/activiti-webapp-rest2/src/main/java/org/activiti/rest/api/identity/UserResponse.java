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

package org.activiti.rest.api.identity;

import org.activiti.engine.identity.User;

/**
 * @author Tijs Rademakers
 */
public class UserResponse {
  
  String id;
  String firstName;
  String lastName;
  String email;
  
  public UserResponse(User user) {
    setId(user.getId());
    setEmail(user.getEmail());
    setFirstName(user.getFirstName());
    setLastName(user.getLastName());
  }
  
  public String getId() {
    return id;
  }
  public UserResponse setId(String id) {
    this.id = id;
    return this;
  }
  public String getFirstName() {
    return firstName;
  }
  public UserResponse setFirstName(String firstName) {
    this.firstName = firstName;
    return this;
  }
  public String getLastName() {
    return lastName;
  }
  public UserResponse setLastName(String lastName) {
    this.lastName = lastName;
    return this;
  }
  public String getEmail() {
    return email;
  }
  public UserResponse setEmail(String email) {
    this.email = email;
    return this;
  }
}

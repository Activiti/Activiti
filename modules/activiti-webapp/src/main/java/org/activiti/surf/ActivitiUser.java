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
package org.activiti.surf;

import org.springframework.extensions.webscripts.connector.User;

import java.util.HashMap;
import java.util.Map;

/**
 * The Activiti user object that, besides the base user info, contains the users
 * security role groups and assignment groups.
 *
 * @author Erik Winlof
 */
public class ActivitiUser extends User {

  /**
   * Property key for manager.
   */
  public static final String CAPABILITY_MANAGER = "isManager";

  /**
   * Property key for security role groups.
   */
  public static final String PROP_SECURITY_ROLE_GROUPS = "securityRoleGroups";

  /**
   * Property key for assignment groups
   */
  public static final String PROP_ASSIGNMENT_GROUPS = "assignmentGroups";

  /**
   * Constructor
   *
   * @param id The user username
   * @param capabilities The users capabilities
   */
  public ActivitiUser(String id, Map<String, Boolean> capabilities) {
    super(id, capabilities);
  }

  /**
   * Setter for security role groups.
   *
   * @param groups The security role groups
   */
  public void setSecurityRoleGroups(HashMap<String, String> groups) {
    setProperty(PROP_SECURITY_ROLE_GROUPS, groups);
  }

  /**
   * Getter for the security role groups.
   *
   * @return the security role group id and names.
   */
  public HashMap<String, String> getSecurityRoleGroups() {
    return (HashMap<String, String>) getProperty(PROP_SECURITY_ROLE_GROUPS);
  }

  /**
   * Setter for the assignment groups.
   *
   * @param groups The assignment groups
   */
  public void setAssignmentGroups(HashMap<String, String> groups) {
    setProperty(PROP_ASSIGNMENT_GROUPS, groups);
  }

  /**
   * Getter for the assignment groups.
   *
   * @return The assignment group id and names
   */
  public HashMap<String, String> getAssignmentGroups() {
    return (HashMap<String, String>) getProperty(PROP_ASSIGNMENT_GROUPS);
  }

  /**
   * Checks if the user is a manager.
   *
   * @return the isManager
   */
  public boolean isManager()
  {
    Boolean value = this.capabilities.get(CAPABILITY_MANAGER);
    return value == null ? false : value;
  }
    
}

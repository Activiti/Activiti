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
package org.activiti.rest;

/**
 * @author Erik Winlof
 */
public class Config {

  private String engine = "default";
  private String userGroupId = "user";
  private String managerGroupId = "manager";
  private String adminGroupId = "admin";
  private String securityRoleGroupTypeId = "security-role";
  private String assignmentGroupTypeId = "assignment";

  public String getEngine() {
    return engine;
  }

  public void setEngine(String engine) {
    this.engine = engine;
  }

  public String getUserGroupId()
  {
    return userGroupId;
  }

  public void setUserGroupId(String userGroupId)
  {
    this.userGroupId = userGroupId;
  }

  public String getManagerGroupId()
  {
    return managerGroupId;
  }

  public void setManagerGroupId(String managerGroupId)
  {
    this.managerGroupId = managerGroupId;
  }

  public String getAdminGroupId()
  {
    return adminGroupId;
  }

  public void setAdminGroupId(String adminGroupId)
  {
    this.adminGroupId = adminGroupId;
  }

  public String getSecurityRoleGroupTypeId()
  {
    return securityRoleGroupTypeId;
  }

  public void setSecurityRoleGroupTypeId(String securityRoleGroupTypeId)
  {
    this.securityRoleGroupTypeId = securityRoleGroupTypeId;
  }

  public String getAssignmentGroupTypeId()
  {
    return assignmentGroupTypeId;
  }

  public void setAssignmentGroupTypeId(String assignmentGroupTypeId)
  {
    this.assignmentGroupTypeId = assignmentGroupTypeId;
  }
}

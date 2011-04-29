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
package org.activiti.administrator.ui.i18n;

import java.util.ListResourceBundle;

public class Messages extends ListResourceBundle {

  public static final String OkKey = generateId();
  public static final String CancelKey = generateId();
  public static final String Save = generateId();
  public static final String Reset = generateId();
  public static final String Close = generateId();
  public static final String Edit = generateId();
  public static final String Delete = generateId();
  public static final String Create = generateId();
  public static final String Refresh = generateId();
  public static final String RefreshTable = generateId();
  public static final String ConfirmDelete = generateId();

  // Application
  public static final String AppTitle = generateId();

  // LoginScreen
  public static final String Username = generateId();
  public static final String Password = generateId();
  public static final String Login = generateId();
  public static final String LoginButton = generateId();

  // Error messages
  public static final String FailedLogin = generateId();
  public static final String ServiceUnavilable = generateId();
  public static final String UsernameIsMissing = generateId();
  public static final String PasswordIsMissing = generateId();
  public static final String EmailIsMissing = generateId();
  public static final String FirstNameIsMissing = generateId();
  public static final String LastNameIsMissing = generateId();
  public static final String IdIsMissing = generateId();
  public static final String EmailFormatError = generateId();
  public static final String InvalidUsername = generateId();

  // Application Header
  public static final String Title = generateId();
  public static final String Subtitle = generateId();
  public static final String Logout = generateId();
  public static final String SignedInMessage = generateId();

  // Application Footer
  public static final String Footer = generateId();

  // Users
  public static final String Users = generateId();
  public static final String User = generateId();
  public static final String EditUser = generateId();
  public static final String DeleteUserQuestion = generateId();
  public static final String DeletedUser = generateId();
  public static final String DeleteUserAffectsNoTasks = generateId();
  public static final String DeleteUserAffectsTasks = generateId();
  public static final String CreateUser = generateId();
  public static final String UserCreatedMessage = generateId();
  public static final String DeleteUser = generateId();
  public static final String UserUpdatedMessage = generateId();
  public static final String AvailableUsers = generateId();

  // Groups
  public static final String Groups = generateId();
  public static final String Group = generateId();
  public static final String EditGroup = generateId();
  public static final String DeleteGroupQuestion = generateId();
  public static final String DeletedGroup = generateId();
  public static final String DeleteGroupAffectsNoTasks = generateId();
  public static final String DeleteGroupAffectsTasks = generateId();
  public static final String CreateGroup = generateId();
  public static final String GroupCreatedMessage = generateId();
  public static final String GroupUpdatedMessage = generateId();
  public static final String DeleteGroup = generateId();
  public static final String AvailableGroups = generateId();
  public static final String MemberOfGroups = generateId();
  public static final String Members = generateId();
  public static final String GroupMembers = generateId();
  public static final String GroupNameIsMissing = generateId();

  // Tasks
  public static final String Task = generateId();
  public static final String Tasks = generateId();
  public static final String AssignedTasks = generateId();
  public static final String UnassignedTasks = generateId();

  // Group Types
  public static final String Role = generateId();
  public static final String Team = generateId();
  public static final String Unit = generateId();
  public static final String Assignment = generateId();
  public static final String Project = generateId();
  public static final String Program = generateId();
  public static final String Types = generateId();

  public static String generateId() {
    return new Integer(ids++).toString();
  }

  static int ids = 0;

  @Override
  public Object[][] getContents() {
    return null;
  }

}

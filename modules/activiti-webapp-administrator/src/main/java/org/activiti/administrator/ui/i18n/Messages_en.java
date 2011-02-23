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

public class Messages_en extends Messages {

  @Override
  public Object[][] getContents() {
    return contents_en;
  }

  static final Object[][] contents_en = {
      { OkKey, "OK" },
      { CancelKey, "Cancel" },
      { Save, "Save" },
      { Reset, "Reset" },
      { Close, "Close" },
      { Edit, "Edit" },
      { Delete, "Delete" },
      { Create, "Create" },
      { Refresh, "Refresh" },
      { RefreshTable, "Refresh Table" },
      { ConfirmDelete, "Confirm Delete: " },

      // Application
      { AppTitle, "activiti-administrator" },

      // LoginScreen
      { Username, "Username" },
      { Password, "Password" },
      { Login, "Login" },
      { LoginButton, "Login" },

      // Error messages
      { FailedLogin, "Invalid username or password" },
      { ServiceUnavilable, "Activiti service is not available" },
      { UsernameIsMissing, "Username is missing" },
      { PasswordIsMissing, "Password is missing" },
      { EmailIsMissing, "Email is missing" },
      { FirstNameIsMissing, "First name is missing" },
      { LastNameIsMissing, "Last name is missing" },
      { EmailFormatError, "Email must contain '@' and have full domain." },
      { InvalidUsername, "User id must contain 4 to 20 characters long of the following set [a-zA-Z0-9_-]." },

      // Application Header
      { Title, "Activiti Administrator" },
      { Subtitle, "User & Group Management" },
      { Logout, "Logout" },
      { SignedInMessage, "Signed in as:" },

      // Application Footer
      { Footer, "Copyright 2010 Activiti.org. All rights reserved." },

      // Users
      { Users, "Users" }, { EditUser, "Edit User" }, { User, "User" }, { DeleteUserQuestion, "Delete User? " }, { DeletedUser, "Deleted User: " },
      { DeleteUserAffectsNoTasks, "Deleting this user will not effect any existing tasks." },
      { DeleteUserAffectsTasks, "Deleting this user may effect the following task(s): " },
      { CreateUser, "Create User" },
      { DeleteUser, "Delete User" },
      { UserCreatedMessage, "Created User: " },
      { UserUpdatedMessage, "Updated User: " },
      { AvailableUsers, "Available Users" },

      // Groups
      { Groups, "Groups" }, { EditGroup, "Edit Group" }, { Group, "Groups" }, { DeleteGroupQuestion, "Delete Group? " }, { DeletedGroup, "Deleted Group: " },
      { DeleteGroupAffectsNoTasks, "Deleting this group will not effect any existing tasks." },
      { DeleteGroupAffectsTasks, "Deleting this group may effect the following task(s): " }, { CreateGroup, "Create Group" },
      { GroupCreatedMessage, "Created Group: " }, { GroupUpdatedMessage, "Updated Group: " }, { DeleteGroup, "Delete Group" },
      { AvailableGroups, "Available Groups" }, { MemberOfGroups, "Member of Groups" }, { Members, "Members" }, { GroupMembers, "Group Members" },
      { GroupNameIsMissing, "Group name is missing" },

      // Tasks
      { Task, "Task" }, { Tasks, "Tasks" }, { AssignedTasks, "Assigned Tasks " }, { UnassignedTasks, "Unassigned Tasks " },

      // Types
      { Role, "Role" }, { Team, "Team" }, { Unit, "Unit" }, { Assignment, "Assignment" }, { Project, "Project" }, { Program, "Program" }, { Types, "Types" },

  };
}

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
package org.activiti.impl;

import java.util.List;

import org.activiti.IdentityService;
import org.activiti.identity.Group;
import org.activiti.identity.User;
import org.activiti.impl.cmd.CreateMembershipCmd;
import org.activiti.impl.cmd.DeleteGroupCmd;
import org.activiti.impl.cmd.DeleteMembershipCmd;
import org.activiti.impl.cmd.DeleteUserCmd;
import org.activiti.impl.cmd.FindGroupCmd;
import org.activiti.impl.cmd.FindGroupsByUserCmd;
import org.activiti.impl.cmd.FindUserCmd;
import org.activiti.impl.cmd.FindUsersByGroupCmd;
import org.activiti.impl.cmd.CheckPassword;
import org.activiti.impl.cmd.SaveGroupCmd;
import org.activiti.impl.cmd.SaveUserCmd;
import org.activiti.impl.identity.GroupImpl;
import org.activiti.impl.identity.UserImpl;
import org.activiti.impl.interceptor.CommandExecutor;


/**
 * @author Tom Baeyens
 */
public class IdentityServiceImpl implements IdentityService {
  
  protected CommandExecutor commandExecutor;
  
  public void setCmdExecutor(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
  }
  
  public Group newGroup(String groupId) {
    return new GroupImpl(groupId);
  }

  public User newUser(String userId) {
    return new UserImpl(userId);
  }

  public void saveGroup(Group group) {
    commandExecutor.execute(new SaveGroupCmd((GroupImpl) group));
  }

  public void saveUser(User user) {
    commandExecutor.execute(new SaveUserCmd(user));
  }

  public User findUser(String userId) {
    return commandExecutor.execute(new FindUserCmd(userId));
  }

  public Group findGroup(String groupId) {
    return commandExecutor.execute(new FindGroupCmd(groupId));
  }

  public void createMembership(String userId, String groupId) {
    commandExecutor.execute(new CreateMembershipCmd(userId, groupId));
  }

  public List<Group> findGroupsByUser(String userId) {
    return commandExecutor.execute(new FindGroupsByUserCmd(userId, null));
  }

  public List<Group> findGroupsByUserAndType(String userId, String groupType) {
    return commandExecutor.execute(new FindGroupsByUserCmd(userId, groupType));
  }

  public List<User> findUsersByGroup(String groupId) {
    return commandExecutor.execute(new FindUsersByGroupCmd(groupId));
  }

  public void deleteGroup(String groupId) {
    commandExecutor.execute(new DeleteGroupCmd(groupId));
  }

  public void deleteMembership(String userId, String groupId) {
    commandExecutor.execute(new DeleteMembershipCmd(userId, groupId));
  }

  public boolean checkPassword(String userId, String password) {
    return commandExecutor.execute(new CheckPassword(userId, password));
  }

  public void deleteUser(String userId) {
    commandExecutor.execute(new DeleteUserCmd(userId));
  }
}

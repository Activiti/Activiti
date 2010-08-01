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
package org.activiti.engine.impl;

import java.util.List;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.cmd.CheckPassword;
import org.activiti.engine.impl.cmd.CreateMembershipCmd;
import org.activiti.engine.impl.cmd.DeleteGroupCmd;
import org.activiti.engine.impl.cmd.DeleteMembershipCmd;
import org.activiti.engine.impl.cmd.DeleteUserCmd;
import org.activiti.engine.impl.cmd.FindGroupCmd;
import org.activiti.engine.impl.cmd.FindGroupsByUserIdCmd;
import org.activiti.engine.impl.cmd.FindUserCmd;
import org.activiti.engine.impl.cmd.FindUsersByGroupCmd;
import org.activiti.engine.impl.cmd.SaveGroupCmd;
import org.activiti.engine.impl.cmd.SaveUserCmd;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.persistence.identity.GroupEntity;
import org.activiti.engine.impl.persistence.identity.UserEntity;


/**
 * @author Tom Baeyens
 */
public class IdentityServiceImpl extends ServiceImpl implements IdentityService {
  
  public Group newGroup(String groupId) {
    return new GroupEntity(groupId);
  }

  public User newUser(String userId) {
    return new UserEntity(userId);
  }

  public void saveGroup(Group group) {
    commandExecutor.execute(new SaveGroupCmd((GroupEntity) group));
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

  @SuppressWarnings("unchecked")
  public List<Group> findGroupsByUser(String userId) {
    return commandExecutor.execute(new FindGroupsByUserIdCmd(userId, null));
  }

  @SuppressWarnings("unchecked")
  public List<Group> findGroupsByUserAndType(String userId, String groupType) {
    return commandExecutor.execute(new FindGroupsByUserIdCmd(userId, groupType));
  }

  @SuppressWarnings("unchecked")
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

  // getters and setters //////////////////////////////////////////////////////
  
  public CommandExecutor getCommandExecutor() {
    return commandExecutor;
  }
}

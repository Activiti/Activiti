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


/**
 * @author Tom Baeyens
 */
public class IdentityServiceImpl extends ServiceImpl implements IdentityService {
  
  public IdentityServiceImpl(ProcessEngineImpl processEngine) {
    super(processEngine);
  }

  public Group newGroup(String groupId) {
    return new GroupImpl(groupId);
  }

  public User newUser(String userId) {
    return new UserImpl(userId);
  }

  public void saveGroup(Group group) {
    cmdExecutor.execute(new SaveGroupCmd((GroupImpl) group), processEngine);
  }

  public void saveUser(User user) {
    cmdExecutor.execute(new SaveUserCmd(user), processEngine);
  }

  public User findUser(String userId) {
    return cmdExecutor.execute(new FindUserCmd(userId), processEngine);
  }

  public Group findGroup(String groupId) {
    return cmdExecutor.execute(new FindGroupCmd(groupId), processEngine);
  }

  public void createMembership(String userId, String groupId) {
    cmdExecutor.execute(new CreateMembershipCmd(userId, groupId), processEngine);
  }

  public List<Group> findGroupsByUser(String userId) {
    return cmdExecutor.execute(new FindGroupsByUserCmd(userId, null), processEngine);
  }

  public List<Group> findGroupsByUserAndType(String userId, String groupType) {
    return cmdExecutor.execute(new FindGroupsByUserCmd(userId, groupType), processEngine);
  }

  public List<User> findUsersByGroup(String groupId) {
    return cmdExecutor.execute(new FindUsersByGroupCmd(groupId), processEngine);
  }

  public void deleteGroup(String groupId) {
    cmdExecutor.execute(new DeleteGroupCmd(groupId), processEngine);
  }

  public void deleteMembership(String userId, String groupId) {
    cmdExecutor.execute(new DeleteMembershipCmd(userId, groupId), processEngine);
  }

  public boolean checkPassword(String userId, String password) {
    return cmdExecutor.execute(new CheckPassword(userId, password), processEngine);
  }

  public void deleteUser(String userId) {
    cmdExecutor.execute(new DeleteUserCmd(userId), processEngine);
  }
}

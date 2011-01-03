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

package org.activiti.standalone.cfg.identity;

import java.util.List;
import java.util.logging.Logger;

import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.GroupQuery;
import org.activiti.engine.identity.User;
import org.activiti.engine.identity.UserQuery;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.cfg.IdentitySession;
import org.activiti.engine.impl.identity.UserEntity;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.interceptor.Session;

/**
 * Example of a custom implementation of an identitySession, just logs the
 * method calls and returns a user with the given id in the createNewUser
 * method.
 * 
 * @author Nils Preusker (nils.preusker@camunda.com)
 */
public class CustomIdentitySession implements IdentitySession, Session {

  private static Logger log = Logger.getLogger(CustomIdentitySession.class.getName());

  // IdentitySession

  public void createMembership(String userId, String groupId) {
    trace(Thread.currentThread().getStackTrace());
  }

  public Group createNewGroup(String groupId) {
    trace(Thread.currentThread().getStackTrace());
    return null;
  }

  public GroupQuery createNewGroupQuery(CommandExecutor commandExecutor) {
    trace(Thread.currentThread().getStackTrace());
    return null;
  }

  public User createNewUser(String userId) {
    trace(Thread.currentThread().getStackTrace());
    return new UserEntity(userId);
  }

  public UserQuery createNewUserQuery(CommandExecutor commandExecutor) {
    trace(Thread.currentThread().getStackTrace());
    return null;
  }

  public void deleteGroup(String groupId) {
    trace(Thread.currentThread().getStackTrace());
  }

  public void deleteMembership(String userId, String groupId) {
    trace(Thread.currentThread().getStackTrace());
  }

  public void deleteUser(String userId) {
    trace(Thread.currentThread().getStackTrace());
  }

  public Group findGroupById(String groupId) {
    trace(Thread.currentThread().getStackTrace());
    return null;
  }

  public List<Group> findGroupByQueryCriteria(Object query, Page page) {
    trace(Thread.currentThread().getStackTrace());
    return null;
  }

  public long findGroupCountByQueryCriteria(Object query) {
    trace(Thread.currentThread().getStackTrace());
    return 0;
  }

  public List<Group> findGroupsByUser(String userId) {
    trace(Thread.currentThread().getStackTrace());
    return null;
  }

  // this method always returns a user with the password "xxx" 
  public User findUserById(String userId) {
    trace(Thread.currentThread().getStackTrace());
    User user = new UserEntity(userId);
    user.setPassword("xxx");
    return user;
  }

  public List<User> findUserByQueryCriteria(Object query, Page page) {
    trace(Thread.currentThread().getStackTrace());
    return null;
  }

  public long findUserCountByQueryCriteria(Object query) {
    trace(Thread.currentThread().getStackTrace());
    return 0;
  }

  public List<User> findUsersByGroupId(String groupId) {
    trace(Thread.currentThread().getStackTrace());
    return null;
  }

  public void insertGroup(Group group) {
    trace(Thread.currentThread().getStackTrace());
  }

  public void insertUser(User user) {
    trace(Thread.currentThread().getStackTrace());
  }

  public boolean isValidUser(String userId) {
    trace(Thread.currentThread().getStackTrace());
    return false;
  }

  public void updateGroup(Group updatedGroup) {
    trace(Thread.currentThread().getStackTrace());
  }

  public void updateUser(User updatedUser) {
    trace(Thread.currentThread().getStackTrace());
  }

  // Session

  public void close() {
    trace(Thread.currentThread().getStackTrace());
  }

  public void flush() {
    trace(Thread.currentThread().getStackTrace());
  }

  // Utility Methods

  /**
   * Utility method to log the method calls
   */
  public static void trace(StackTraceElement e[]) {
    boolean doNext = false;
    for (StackTraceElement s : e) {
      if (doNext) {
        log.info(s.getClassName() + ": " + s.getMethodName());
        return;
      }
      doNext = s.getMethodName().equals("getStackTrace");
    }
  }

}

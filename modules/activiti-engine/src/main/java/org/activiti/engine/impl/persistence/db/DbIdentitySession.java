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

package org.activiti.engine.impl.persistence.db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.cfg.IdentitySession;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.persistence.identity.GroupImpl;
import org.activiti.engine.impl.persistence.identity.UserImpl;


/**
 * @author Tom Baeyens
 */
public class DbIdentitySession implements IdentitySession, Session {

  protected DbSqlSession dbSqlSession;

  public DbIdentitySession() {
    this.dbSqlSession = CommandContext.getCurrentSession(DbSqlSession.class);
  }

  public void saveUser(UserImpl user) {
    if (user.isNew()) {
      dbSqlSession.insert("insertUser", user);
    } else {
      dbSqlSession.update("updateUser", user);
    }
  }

  public UserImpl findUser(String userId) {
    return (UserImpl) dbSqlSession.selectOne("selectUser", userId);
  }

  public List<UserImpl> findUsersByGroup(String groupId) {
    return dbSqlSession.selectList("selectUsersByGroup", groupId);
  }

  public List<UserImpl> findUsers() {
    return dbSqlSession.selectList("selectUsers");
  }

  public boolean isValidUser(String userId) {
    return findUser(userId) != null;
  }

  public void deleteUser(String userId) {
    dbSqlSession.delete("deleteMembershipsForUser", userId);
    dbSqlSession.delete("deleteUser", userId);
  }

  public void saveGroup(GroupImpl group) {
    if (group.isNew()) {
      dbSqlSession.insert("insertGroup", group);
    } else {
      dbSqlSession.update("updateGroup", group);
    }
  }

  public GroupImpl findGroup(String groupId) {
    return (GroupImpl) dbSqlSession.selectOne("selectGroup", groupId);
  }

  public List<GroupImpl> findGroupsByUser(String userId) {
    return dbSqlSession.selectList("selectGroupsByUser", userId);
  }

  public List<GroupImpl> findGroupsByUserAndType(String userId, String groupType) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("userId", userId);
    parameters.put("groupType", groupType);
    return dbSqlSession.selectList("selectGroupsByUserAndType", parameters);
  }

  public List<GroupImpl> findGroups() {
    return dbSqlSession.selectList("selectGroups");
  }

  public void deleteGroup(String groupId) {
    dbSqlSession.delete("deleteMembershipsForGroup", groupId);
    dbSqlSession.delete("deleteGroup", groupId);
  }

  public void createMembership(String userId, String groupId) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("userId", userId);
    parameters.put("groupId", groupId);
    dbSqlSession.insert("insertMembership", parameters);
  }

  public void deleteMembership(String userId, String groupId) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("userId", userId);
    parameters.put("groupId", groupId);
    dbSqlSession.delete("deleteMembership", parameters);
  }


  public void close() {
  }

  public void flush() {
  }
}

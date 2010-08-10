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
import org.activiti.engine.impl.persistence.identity.GroupEntity;
import org.activiti.engine.impl.persistence.identity.UserEntity;


/**
 * @author Tom Baeyens
 */
public class DbIdentitySession implements IdentitySession, Session {

  protected DbSqlSession dbSqlSession;

  public DbIdentitySession() {
    this.dbSqlSession = CommandContext.getCurrentSession(DbSqlSession.class);
  }

  public void insertUser(UserEntity user) {
    dbSqlSession.insert(user);
  }

  public UserEntity findUserById(String userId) {
    return (UserEntity) dbSqlSession.selectOne("selectUserById", userId);
  }

  @SuppressWarnings("unchecked")
  public List<UserEntity> findUsersByGroupId(String groupId) {
    return dbSqlSession.selectList("selectUsersByGroupId", groupId);
  }

  @SuppressWarnings("unchecked")
  public List<UserEntity> findUsers() {
    return dbSqlSession.selectList("selectUsers");
  }

  public boolean isValidUser(String userId) {
    return findUserById(userId) != null;
  }

  public void deleteUser(String userId) {
    dbSqlSession.delete("deleteMembershipsByUserId", userId);
    dbSqlSession.delete("deleteUser", userId);
  }

  public void insertGroup(GroupEntity group) {
    dbSqlSession.insert(group);
  }

  public GroupEntity findGroupById(String groupId) {
    return (GroupEntity) dbSqlSession.selectOne("selectGroupById", groupId);
  }

  @SuppressWarnings("unchecked")
  public List<GroupEntity> findGroupsByUser(String userId) {
    return dbSqlSession.selectList("selectGroupsByUserId", userId);
  }

  @SuppressWarnings("unchecked")
  public List<GroupEntity> findGroupsByUserAndType(String userId, String groupType) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("userId", userId);
    parameters.put("groupType", groupType);
    return dbSqlSession.selectList("selectGroupsByUserIdAndGroupType", parameters);
  }

  @SuppressWarnings("unchecked")
  public List<GroupEntity> findGroups() {
    return dbSqlSession.selectList("selectGroups");
  }

  public void deleteGroup(String groupId) {
    dbSqlSession.delete("deleteMembershipsByGroupId", groupId);
    dbSqlSession.delete("deleteGroup", groupId);
  }

  public void createMembership(String userId, String groupId) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("userId", userId);
    parameters.put("groupId", groupId);
    dbSqlSession.getSqlSession().insert("insertMembership", parameters);
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

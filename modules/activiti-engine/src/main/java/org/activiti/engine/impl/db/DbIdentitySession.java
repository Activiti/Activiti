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

package org.activiti.engine.impl.db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.GroupQuery;
import org.activiti.engine.identity.User;
import org.activiti.engine.identity.UserQuery;
import org.activiti.engine.impl.GroupQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.UserQueryImpl;
import org.activiti.engine.impl.cfg.IdentitySession;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.identity.GroupEntity;
import org.activiti.engine.impl.identity.IdentityInfoEntity;
import org.activiti.engine.impl.identity.UserEntity;
import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.runtime.ByteArrayEntity;


/**
 * @author Tom Baeyens
 */
public class DbIdentitySession implements IdentitySession, Session {

  protected DbSqlSession dbSqlSession;

  public DbIdentitySession() {
    this.dbSqlSession = Context.getCommandContext().getSession(DbSqlSession.class);
  }

  public void insertUser(User user) {
    dbSqlSession.insert((PersistentObject) user);
  }

  public UserEntity findUserById(String userId) {
    return (UserEntity) dbSqlSession.selectOne("selectUserById", userId);
  }

  @SuppressWarnings("unchecked")
  public List<User> findUsersByGroupId(String groupId) {
    return dbSqlSession.selectList("selectUsersByGroupId", groupId);
  }

  public boolean isValidUser(String userId) {
    return findUserById(userId) != null;
  }

  public void deleteUser(String userId) {
    UserEntity user = findUserById(userId);
    if (user!=null) {
      if (user.getPictureByteArrayId()!=null) {
        dbSqlSession.delete(ByteArrayEntity.class, user.getPictureByteArrayId());
      }
      dbSqlSession.delete("deleteIdentityInfosByUserId", userId);
      dbSqlSession.delete("deleteMembershipsByUserId", userId);
      dbSqlSession.delete("deleteUser", userId);
    }
  }

  public void insertGroup(Group group) {
    dbSqlSession.insert((PersistentObject) group);
  }

  public GroupEntity findGroupById(String groupId) {
    return (GroupEntity) dbSqlSession.selectOne("selectGroupById", groupId);
  }

  @SuppressWarnings("unchecked")
  public List<Group> findGroupsByUser(String userId) {
    return dbSqlSession.selectList("selectGroupsByUserId", userId);
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
  
  @SuppressWarnings("unchecked")
  public List<User> findUserByQueryCriteria(Object query, Page page) {
    return dbSqlSession.selectList("selectUserByQueryCriteria", query, page);
  }
  
  public long findUserCountByQueryCriteria(Object query) {
    return (Long) dbSqlSession.selectOne("selectUserCountByQueryCriteria", query);
  }
  
  @SuppressWarnings("unchecked")
  public List<Group> findGroupByQueryCriteria(Object query, Page page) {
    return dbSqlSession.selectList("selectGroupByQueryCriteria", query, page);
  }
  
  public long findGroupCountByQueryCriteria(Object query) {
    return (Long) dbSqlSession.selectOne("selectGroupCountByQueryCriteria", query);
  }

  public Group createNewGroup(String groupId) {
    return new GroupEntity(groupId);
  }

  public GroupQuery createNewGroupQuery() {
    return new GroupQueryImpl(Context.getProcessEngineConfiguration().getCommandExecutorTxRequired());
  }

  public User createNewUser(String userId) {
    return new UserEntity(userId);
  }

  public UserQuery createNewUserQuery() {
    return new UserQueryImpl(Context.getProcessEngineConfiguration().getCommandExecutorTxRequired());
  }

  public void updateGroup(Group updatedGroup) {
    GroupEntity persistentGroup = findGroupById(updatedGroup.getId());
    persistentGroup.update((GroupEntity) updatedGroup);
  }

  public void updateUser(User updatedUser) {
    UserEntity persistentUser = findUserById(updatedUser.getId());
    persistentUser.update((UserEntity) updatedUser);
  }

  public void deleteUserInfoByUserIdAndKey(String userId, String key) {
    IdentityInfoEntity identityInfoEntity = findIdentityInfoEntityByUserIdAndKey(userId, key);
    if (identityInfoEntity!=null) {
      dbSqlSession.delete(IdentityInfoEntity.class, identityInfoEntity.getId());
    }
  }

  public IdentityInfoEntity findUserInfoByUserIdAndKey(String userId, String key) {
    IdentityInfoEntity identityInfoEntity = findIdentityInfoEntityByUserIdAndKey(userId, key);
    if (identityInfoEntity!=null) {
      return identityInfoEntity;
    }
    return null;
  }

  public void setUserInfo(String userId, String type, String key, String value, String password) {
    IdentityInfoEntity identityInfoEntity = findIdentityInfoEntityByUserIdAndKey(userId, key);
    if (identityInfoEntity!=null) {
      identityInfoEntity.setValue(value);
      identityInfoEntity.setPassword(password);
    } else {
      identityInfoEntity = new IdentityInfoEntity();
      identityInfoEntity.setUserId(userId);
      identityInfoEntity.setType(type);
      identityInfoEntity.setKey(key);
      identityInfoEntity.setValue(value);
      identityInfoEntity.setPassword(password);
      dbSqlSession.insert(identityInfoEntity);
    }
  }

  protected IdentityInfoEntity findIdentityInfoEntityByUserIdAndKey(String userId, String key) {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userId);
    parameters.put("key", key);
    return (IdentityInfoEntity) dbSqlSession.selectOne("selectIdentityInfoByUserIdAndKey", parameters);
  }

  @SuppressWarnings("unchecked")
  public List<String> findIdentityInfoKeysByUserIdAndType(String userId, String type) {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userId);
    parameters.put("type", type);
    return (List) dbSqlSession.getSqlSession().selectList("selectIdentityInfoKeysByUserIdAndType", parameters);
  }

  public void close() {
  }

  public void flush() {
  }
}

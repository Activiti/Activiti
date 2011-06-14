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

package org.activiti.engine.impl.persistence.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.identity.UserQuery;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.UserQueryImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.impl.persistence.AbstractManager;


/**
 * @author Tom Baeyens
 */
public class UserManager extends AbstractManager {

  public User createNewUser(String userId) {
    return new UserEntity(userId);
  }

  public void insertUser(User user) {
    getDbSqlSession().insert((PersistentObject) user);
  }
  
  public void updateUser(User updatedUser) {
    UserEntity persistentUser = findUserById(updatedUser.getId());
    persistentUser.update((UserEntity) updatedUser);
  }

  public UserEntity findUserById(String userId) {
    return (UserEntity) getDbSqlSession().selectOne("selectUserById", userId);
  }

  @SuppressWarnings("unchecked")
  public void deleteUser(String userId) {
    UserEntity user = findUserById(userId);
    if (user!=null) {
      if (user.getPictureByteArrayId()!=null) {
        getDbSqlSession().delete(ByteArrayEntity.class, user.getPictureByteArrayId());
      }
      List<IdentityInfoEntity> identityInfos = getDbSqlSession().selectList("selectIdentityInfoByUserId", userId);
      for (IdentityInfoEntity identityInfo: identityInfos) {
        getIdentityInfoManager().deleteIdentityInfo(identityInfo);
      }
      getDbSqlSession().delete("deleteMembershipsByUserId", userId);
      getDbSqlSession().delete("deleteUser", userId);
    }
  }
  
  @SuppressWarnings("unchecked")
  public List<User> findUserByQueryCriteria(Object query, Page page) {
    return getDbSqlSession().selectList("selectUserByQueryCriteria", query, page);
  }
  
  public long findUserCountByQueryCriteria(Object query) {
    return (Long) getDbSqlSession().selectOne("selectUserCountByQueryCriteria", query);
  }
  
  @SuppressWarnings("unchecked")
  public List<Group> findGroupsByUser(String userId) {
    return getDbSqlSession().selectList("selectGroupsByUserId", userId);
  }

  public UserQuery createNewUserQuery() {
    return new UserQueryImpl(Context.getProcessEngineConfiguration().getCommandExecutorTxRequired());
  }

  public IdentityInfoEntity findUserInfoByUserIdAndKey(String userId, String key) {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userId);
    parameters.put("key", key);
    return (IdentityInfoEntity) getDbSqlSession().selectOne("selectIdentityInfoByUserIdAndKey", parameters);
  }

  @SuppressWarnings("unchecked")
  public List<String> findUserInfoKeysByUserIdAndType(String userId, String type) {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userId);
    parameters.put("type", type);
    return (List) getDbSqlSession().getSqlSession().selectList("selectIdentityInfoKeysByUserIdAndType", parameters);
  }
  
  public Boolean checkPassword(String userId, String password) {
    User user = findUserById(userId);
    if ((user != null) && (password != null) && (password.equals(user.getPassword()))) {
      return true;
    }
    return false;
  }
}

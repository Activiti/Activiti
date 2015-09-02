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

import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.Picture;
import org.activiti.engine.identity.User;
import org.activiti.engine.identity.UserQuery;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.UserQueryImpl;
import org.activiti.engine.impl.db.Entity;

/**
 * @author Tom Baeyens
 * @author Saeid Mirzaei
 * @author Joram Barrez
 */
public class UserEntityManagerImpl extends AbstractEntityManager<UserEntity> implements UserEntityManager {
  
  @Override
  public Class<UserEntity> getManagedEntity() {
    return UserEntity.class;
  }

  public User createNewUser(String userId) {
    return new UserEntity(userId);
  }

  public void updateUser(User updatedUser) {
    getDbSqlSession().update((Entity) updatedUser);

    if (getEventDispatcher().isEnabled()) {
      getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_UPDATED, updatedUser));
    }
  }

  public void delete(UserEntity userEntity) {
    super.delete(userEntity);
    deletePicture(userEntity);
  }
  
  @Override
  public void deletePicture(User user) {
    UserEntity userEntity = (UserEntity) user;
    if (userEntity.getPictureByteArrayRef() != null) {
      userEntity.getPictureByteArrayRef().delete();
    }
  }

  @SuppressWarnings("unchecked")
  public void delete(String userId) {
    UserEntity user = findById(userId);
    if (user != null) {
      List<IdentityInfoEntity> identityInfos = getDbSqlSession().selectList("selectIdentityInfoByUserId", userId);
      for (IdentityInfoEntity identityInfo : identityInfos) {
        getIdentityInfoEntityManager().delete(identityInfo);
      }
      getDbSqlSession().delete("deleteMembershipsByUserId", userId);

      delete(user);
    }
  }

  @SuppressWarnings("unchecked")
  public List<User> findUserByQueryCriteria(UserQueryImpl query, Page page) {
    return getDbSqlSession().selectList("selectUserByQueryCriteria", query, page);
  }

  public long findUserCountByQueryCriteria(UserQueryImpl query) {
    return (Long) getDbSqlSession().selectOne("selectUserCountByQueryCriteria", query);
  }

  @SuppressWarnings("unchecked")
  public List<Group> findGroupsByUser(String userId) {
    return getDbSqlSession().selectList("selectGroupsByUserId", userId);
  }

  public UserQuery createNewUserQuery() {
    return new UserQueryImpl(getCommandExecutor());
  }

  public IdentityInfoEntity findUserInfoByUserIdAndKey(String userId, String key) {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userId);
    parameters.put("key", key);
    return (IdentityInfoEntity) getDbSqlSession().selectOne("selectIdentityInfoByUserIdAndKey", parameters);
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public List<String> findUserInfoKeysByUserIdAndType(String userId, String type) {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("userId", userId);
    parameters.put("type", type);
    return (List) getDbSqlSession().getSqlSession().selectList("selectIdentityInfoKeysByUserIdAndType", parameters);
  }

  public Boolean checkPassword(String userId, String password) {
    User user = null;
    
    if (userId != null) {
      user = findById(userId);
    }
    
    if ((user != null) && (password != null) && (password.equals(user.getPassword()))) {
      return true;
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  public List<User> findPotentialStarterUsers(String proceDefId) {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("procDefId", proceDefId);
    return (List<User>) getDbSqlSession().selectOne("selectUserByQueryCriteria", parameters);

  }

  @SuppressWarnings("unchecked")
  public List<User> findUsersByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return getDbSqlSession().selectListWithRawParameter("selectUserByNativeQuery", parameterMap, firstResult, maxResults);
  }

  public long findUserCountByNativeQuery(Map<String, Object> parameterMap) {
    return (Long) getDbSqlSession().selectOne("selectUserCountByNativeQuery", parameterMap);
  }

  @Override
  public boolean isNewUser(User user) {
    return ((UserEntity) user).getRevision() == 0;
  }

  @Override
  public Picture getUserPicture(String userId) {
    UserEntity user = findById(userId);
    return user.getPicture();
  }

  @Override
  public void setUserPicture(String userId, Picture picture) {
    UserEntity user = findById(userId);
    if (user == null) {
      throw new ActivitiObjectNotFoundException("user " + userId + " doesn't exist", User.class);
    }

    user.setPicture(picture);
  }

}

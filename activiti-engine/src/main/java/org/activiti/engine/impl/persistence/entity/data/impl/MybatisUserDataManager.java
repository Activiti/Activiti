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
package org.activiti.engine.impl.persistence.entity.data.impl;

import java.util.List;
import java.util.Map;

import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.UserQueryImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.entity.UserEntity;
import org.activiti.engine.impl.persistence.entity.UserEntityImpl;
import org.activiti.engine.impl.persistence.entity.data.AbstractDataManager;
import org.activiti.engine.impl.persistence.entity.data.UserDataManager;

/**
 * @author Joram Barrez
 */
public class MybatisUserDataManager extends AbstractDataManager<UserEntity> implements UserDataManager {
  
  public MybatisUserDataManager(ProcessEngineConfigurationImpl processEngineConfiguration) {
    super(processEngineConfiguration);
  }

  @Override
  public Class<? extends UserEntity> getManagedEntityClass() {
    return UserEntityImpl.class;
  }
  
  @Override
  public UserEntity create() {
    return new UserEntityImpl();
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

  @SuppressWarnings("unchecked")
  public List<User> findUsersByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return getDbSqlSession().selectListWithRawParameter("selectUserByNativeQuery", parameterMap, firstResult, maxResults);
  }

  public long findUserCountByNativeQuery(Map<String, Object> parameterMap) {
    return (Long) getDbSqlSession().selectOne("selectUserCountByNativeQuery", parameterMap);
  }

  
}

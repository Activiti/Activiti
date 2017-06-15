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
import org.activiti.engine.impl.GroupQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.entity.GroupEntity;
import org.activiti.engine.impl.persistence.entity.GroupEntityImpl;
import org.activiti.engine.impl.persistence.entity.data.AbstractDataManager;
import org.activiti.engine.impl.persistence.entity.data.GroupDataManager;

/**
 * @author Joram Barrez
 */
public class MybatisGroupDataManager extends AbstractDataManager<GroupEntity> implements GroupDataManager {

  public MybatisGroupDataManager(ProcessEngineConfigurationImpl processEngineConfiguration) {
    super(processEngineConfiguration);
  }

  @Override
  public Class<? extends GroupEntity> getManagedEntityClass() {
    return GroupEntityImpl.class;
  }
  
  @Override
  public GroupEntity create() {
    return new GroupEntityImpl();
  }
  
  @SuppressWarnings("unchecked")
  public List<Group> findGroupByQueryCriteria(GroupQueryImpl query, Page page) {
    return getDbSqlSession().selectList("selectGroupByQueryCriteria", query, page);
  }

  public long findGroupCountByQueryCriteria(GroupQueryImpl query) {
    return (Long) getDbSqlSession().selectOne("selectGroupCountByQueryCriteria", query);
  }

  @SuppressWarnings("unchecked")
  public List<Group> findGroupsByUser(String userId) {
    return getDbSqlSession().selectList("selectGroupsByUserId", userId);
  }

  @SuppressWarnings("unchecked")
  public List<Group> findGroupsByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return getDbSqlSession().selectListWithRawParameter("selectGroupByNativeQuery", parameterMap, firstResult, maxResults);
  }

  public long findGroupCountByNativeQuery(Map<String, Object> parameterMap) {
    return (Long) getDbSqlSession().selectOne("selectGroupCountByNativeQuery", parameterMap);
  }
  
}

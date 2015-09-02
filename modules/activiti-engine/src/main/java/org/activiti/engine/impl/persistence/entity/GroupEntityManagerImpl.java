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

import java.util.List;
import java.util.Map;

import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.GroupQuery;
import org.activiti.engine.impl.GroupQueryImpl;
import org.activiti.engine.impl.Page;

/**
 * @author Tom Baeyens
 * @author Saeid Mirzaei
 * @author Joram Barrez
 */
public class GroupEntityManagerImpl extends AbstractEntityManager<GroupEntity> implements GroupEntityManager {

  @Override
  public Class<GroupEntity> getManagedEntity() {
    return GroupEntity.class;
  }
  
  public Group createNewGroup(String groupId) {
    return new GroupEntity(groupId);
  }

  public void updateGroup(Group updatedGroup) {
    getDbSqlSession().update((GroupEntity) updatedGroup);

    if (getEventDispatcher().isEnabled()) {
      getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_UPDATED, updatedGroup));
    }
  }
  
  @Override
  public void delete(String groupId) {
    GroupEntity group = getDbSqlSession().selectById(GroupEntity.class, groupId);

    if (group != null) {
      if (getEventDispatcher().isEnabled()) {
        getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createMembershipEvent(ActivitiEventType.MEMBERSHIPS_DELETED, groupId, null));
      }

      getDbSqlSession().delete("deleteMembershipsByGroupId", groupId);
      getDbSqlSession().delete(group);

      if (getEventDispatcher().isEnabled()) {
        getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_DELETED, group));
      }
    }
  }

  public GroupQuery createNewGroupQuery() {
    return new GroupQueryImpl(getCommandExecutor());
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

  @Override
  public boolean isNewGroup(Group group) {
    return ((GroupEntity) group).getRevision() == 0;
  }

}

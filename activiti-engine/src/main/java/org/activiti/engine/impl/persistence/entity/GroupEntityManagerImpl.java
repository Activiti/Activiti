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
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.entity.data.DataManager;
import org.activiti.engine.impl.persistence.entity.data.GroupDataManager;

/**
 * @author Tom Baeyens
 * @author Saeid Mirzaei
 * @author Joram Barrez
 */
public class GroupEntityManagerImpl extends AbstractEntityManager<GroupEntity> implements GroupEntityManager {
  
  protected GroupDataManager groupDataManager;
  
  public GroupEntityManagerImpl(ProcessEngineConfigurationImpl processEngineConfiguration, GroupDataManager groupDataManager) {
    super(processEngineConfiguration);
    this.groupDataManager = groupDataManager;
  }

  @Override
  protected DataManager<GroupEntity> getDataManager() {
    return groupDataManager;
  }
  
  public Group createNewGroup(String groupId) {
    GroupEntity groupEntity = groupDataManager.create();
    groupEntity.setId(groupId);
    groupEntity.setRevision(0); // Needed as groups can be transient and not save when they are returned 
    return groupEntity;
  }

  @Override
  public void delete(String groupId) {
    GroupEntity group = groupDataManager.findById(groupId); 

    if (group != null) {
      
      getMembershipEntityManager().deleteMembershipByGroupId(groupId);
      if (getEventDispatcher().isEnabled()) {
        getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createMembershipEvent(ActivitiEventType.MEMBERSHIPS_DELETED, groupId, null));
      }
      
      delete(group);
    }
  }

  public GroupQuery createNewGroupQuery() {
    return new GroupQueryImpl(getCommandExecutor());
  }

  public List<Group> findGroupByQueryCriteria(GroupQueryImpl query, Page page) {
    return groupDataManager.findGroupByQueryCriteria(query, page);
  }

  public long findGroupCountByQueryCriteria(GroupQueryImpl query) {
    return groupDataManager.findGroupCountByQueryCriteria(query);
  }

  public List<Group> findGroupsByUser(String userId) {
    return groupDataManager.findGroupsByUser(userId);
  }

  public List<Group> findGroupsByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return groupDataManager.findGroupsByNativeQuery(parameterMap, firstResult, maxResults);
  }

  public long findGroupCountByNativeQuery(Map<String, Object> parameterMap) {
    return groupDataManager.findGroupCountByNativeQuery(parameterMap);
  }

  @Override
  public boolean isNewGroup(Group group) {
    return ((GroupEntity) group).getRevision() == 0;
  }

  public GroupDataManager getGroupDataManager() {
    return groupDataManager;
  }

  public void setGroupDataManager(GroupDataManager groupDataManager) {
    this.groupDataManager = groupDataManager;
  }
  
}

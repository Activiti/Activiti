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

import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.GroupQuery;
import org.activiti.engine.impl.GroupQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.impl.persistence.AbstractManager;


/**
 * @author Tom Baeyens
 */
public class GroupManager extends AbstractManager {

  public Group createNewGroup(String groupId) {
    return new GroupEntity(groupId);
  }

  public void insertGroup(Group group) {
    getDbSqlSession().insert((PersistentObject) group);
  }

  public void updateGroup(Group updatedGroup) {
    GroupEntity persistentGroup = findGroupById(updatedGroup.getId());
    persistentGroup.update((GroupEntity) updatedGroup);
  }


  public void deleteGroup(String groupId) {
    getDbSqlSession().delete("deleteMembershipsByGroupId", groupId);
    getDbSqlSession().delete("deleteGroup", groupId);
  }

  public GroupQuery createNewGroupQuery() {
    return new GroupQueryImpl(Context.getProcessEngineConfiguration().getCommandExecutorTxRequired());
  }

  @SuppressWarnings("unchecked")
  public List<Group> findGroupByQueryCriteria(Object query, Page page) {
    return getDbSqlSession().selectList("selectGroupByQueryCriteria", query, page);
  }
  
  public long findGroupCountByQueryCriteria(Object query) {
    return (Long) getDbSqlSession().selectOne("selectGroupCountByQueryCriteria", query);
  }

  public GroupEntity findGroupById(String groupId) {
    return (GroupEntity) getDbSqlSession().selectOne("selectGroupById", groupId);
  }

  @SuppressWarnings("unchecked")
  public List<Group> findGroupsByUser(String userId) {
    return getDbSqlSession().selectList("selectGroupsByUserId", userId);
  }
}

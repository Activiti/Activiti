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

import org.activiti.engine.impl.context.Context;

/**
 * @author Frederik Heremans
 * @author Joram Barrez
 */
public class HistoricIdentityLinkEntityManagerImpl extends AbstractEntityManager<HistoricIdentityLinkEntity> implements HistoricIdentityLinkEntityManager {

  @Override
  public Class<HistoricIdentityLinkEntity> getManagedPersistentObject() {
    return HistoricIdentityLinkEntity.class;
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public List<HistoricIdentityLinkEntity> findHistoricIdentityLinksByTaskId(String taskId) {
    return getDbSqlSession().selectList("selectHistoricIdentityLinksByTask", taskId);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<HistoricIdentityLinkEntity> findHistoricIdentityLinksByProcessInstanceId(String processInstanceId) {
    return getDbSqlSession().selectList("selectHistoricIdentityLinksByProcessInstance", processInstanceId);
  }

  @Override
  public void deleteHistoricIdentityLinksByTaskId(String taskId) {
    List<HistoricIdentityLinkEntity> identityLinks = findHistoricIdentityLinksByTaskId(taskId);
    for (HistoricIdentityLinkEntity identityLink : identityLinks) {
      delete(identityLink);
    }
  }

  @Override
  public void deleteHistoricIdentityLinksByProcInstance(String processInstanceId) {

    // Identity links from db
    List<HistoricIdentityLinkEntity> identityLinks = findHistoricIdentityLinksByProcessInstanceId(processInstanceId);
    // Delete
    for (HistoricIdentityLinkEntity identityLink : identityLinks) {
      delete(identityLink);
    }

    // Identity links from cache
    List<HistoricIdentityLinkEntity> identityLinksFromCache = Context.getCommandContext().getDbSqlSession().findInCache(HistoricIdentityLinkEntity.class);
    for (HistoricIdentityLinkEntity identityLinkEntity : identityLinksFromCache) {
      if (processInstanceId.equals(identityLinkEntity.getProcessInstanceId())) {
        delete(identityLinkEntity);
      }
    }
  }

}

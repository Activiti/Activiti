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

import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.AbstractManager;


/**
 * @author Tijs Rademakers
 */
public class ProcessDefinitionInfoEntityManager extends AbstractManager {

  public void insertProcessDefinitionInfo(ProcessDefinitionInfoEntity processDefinitionInfo) {
    getDbSqlSession().insert((PersistentObject) processDefinitionInfo);
    
    if (Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
    	Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
    			ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_CREATED, processDefinitionInfo));
    	Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
    			ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_INITIALIZED, processDefinitionInfo));
    }
  }

  public void updateProcessDefinitionInfo(ProcessDefinitionInfoEntity updatedProcessDefinitionInfo) {
    CommandContext commandContext = Context.getCommandContext();
    DbSqlSession dbSqlSession = commandContext.getDbSqlSession();
    dbSqlSession.update(updatedProcessDefinitionInfo);
    
    if (Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
    	Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
    			ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_UPDATED, updatedProcessDefinitionInfo));
    }
  }

  public void deleteProcessDefinitionInfo(String processDefinitionId) {
    ProcessDefinitionInfoEntity processDefinitionInfo = findProcessDefinitionInfoByProcessDefinitionId(processDefinitionId);
    if (processDefinitionInfo != null) {
      getDbSqlSession().delete(processDefinitionInfo);
      deleteInfoJson(processDefinitionInfo);
      
      if (Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
      	Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
      			ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_DELETED, processDefinitionInfo));
      }
    }
  }
  
  public void updateInfoJson(String id, byte[] json) {
    ProcessDefinitionInfoEntity processDefinitionInfo = getDbSqlSession().selectById(ProcessDefinitionInfoEntity.class, id);
    if (processDefinitionInfo != null) {
      ByteArrayRef ref = new ByteArrayRef(processDefinitionInfo.getInfoJsonId());
      ref.setValue("json", json);
      
      if (processDefinitionInfo.getInfoJsonId() == null) {
        processDefinitionInfo.setInfoJsonId(ref.getId());
        updateProcessDefinitionInfo(processDefinitionInfo);
      }
    }
  }
  
  public void deleteInfoJson(ProcessDefinitionInfoEntity processDefinitionInfo) {
    if (processDefinitionInfo.getInfoJsonId() != null) {
      ByteArrayRef ref = new ByteArrayRef(processDefinitionInfo.getInfoJsonId());
      ref.delete();
    }
  }

  public ProcessDefinitionInfoEntity findProcessDefinitionInfoById(String id) {
    return (ProcessDefinitionInfoEntity) getDbSqlSession().selectOne("selectProcessDefinitionInfo", id);
  }
  
  public ProcessDefinitionInfoEntity findProcessDefinitionInfoByProcessDefinitionId(String processDefinitionId) {
    return (ProcessDefinitionInfoEntity) getDbSqlSession().selectOne("selectProcessDefinitionInfoByProcessDefinitionId", processDefinitionId);
  }
  
  public byte[] findInfoJsonById(String infoJsonId) {
    ByteArrayRef ref = new ByteArrayRef(infoJsonId);
    return ref.getBytes();
  }
}

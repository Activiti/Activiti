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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.persistence.CachedEntityMatcher;
import org.activiti.engine.impl.variable.VariableType;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 * @author Saeid Mirzaei
 */
public class VariableInstanceEntityManagerImpl extends AbstractEntityManager<VariableInstanceEntity> implements VariableInstanceEntityManager {

  @Override
  public Class<VariableInstanceEntity> getManagedEntity() {
    return VariableInstanceEntity.class;
  }
  
  @Override
  public VariableInstanceEntity create(String name, VariableType type, Object value) {
    VariableInstanceEntity variableInstance = new VariableInstanceEntity();
    variableInstance.name = name;
    variableInstance.type = type;
    variableInstance.typeName = type.getTypeName();
    variableInstance.setValue(value);
    return variableInstance;
  }
  
  @Override
  public VariableInstanceEntity createAndInsert(String name, VariableType type, Object value) {
    VariableInstanceEntity variableInstance = create(name, type, value);
    insert(variableInstance);
    return variableInstance;
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<VariableInstanceEntity> findVariableInstancesByTaskId(String taskId) {
    return getDbSqlSession().selectList("selectVariablesByTaskId", taskId);
  }
  
  @Override
  public Collection<VariableInstanceEntity> findVariableInstancesByExecutionId(final String executionId) {
    return getList("selectVariablesByExecutionId", executionId, new CachedEntityMatcher<VariableInstanceEntity>() {
      public boolean isRetained(VariableInstanceEntity variableInstanceEntity) {
        return variableInstanceEntity.getExecutionId() != null && variableInstanceEntity.getExecutionId().equals(executionId);
      }
    }, true);
  }

  @Override
  public VariableInstanceEntity findVariableInstanceByExecutionAndName(String executionId, String variableName) {
    Map<String, String> params = new HashMap<String, String>(2);
    params.put("executionId", executionId);
    params.put("name", variableName);
    return (VariableInstanceEntity) getDbSqlSession().selectOne("selectVariableInstanceByExecutionAndName", params);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<VariableInstanceEntity> findVariableInstancesByExecutionAndNames(String executionId, Collection<String> names) {
    Map<String, Object> params = new HashMap<String, Object>(2);
    params.put("executionId", executionId);
    params.put("names", names);
    return getDbSqlSession().selectList("selectVariableInstancesByExecutionAndNames", params);
  }

  @Override
  public VariableInstanceEntity findVariableInstanceByTaskAndName(String taskId, String variableName) {
    Map<String, String> params = new HashMap<String, String>(2);
    params.put("taskId", taskId);
    params.put("name", variableName);
    return (VariableInstanceEntity) getDbSqlSession().selectOne("selectVariableInstanceByTaskAndName", params);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<VariableInstanceEntity> findVariableInstancesByTaskAndNames(String taskId, Collection<String> names) {
    Map<String, Object> params = new HashMap<String, Object>(2);
    params.put("taskId", taskId);
    params.put("names", names);
    return getDbSqlSession().selectList("selectVariableInstancesByTaskAndNames", params);
  }

  @Override
  public void delete(VariableInstanceEntity entity) {
    delete(entity, true);
  }

  @Override
  public void delete(VariableInstanceEntity entity, boolean fireDeleteEvent) {
    getDbSqlSession().delete(entity);
    ByteArrayRef byteArrayRef = entity.getByteArrayRef();
    if (byteArrayRef != null) {
      byteArrayRef.delete();
    }
    entity.setDeleted(true);

    if (fireDeleteEvent && getEventDispatcher().isEnabled()) {
      getEventDispatcher().dispatchEvent(ActivitiEventBuilder.createEntityEvent(ActivitiEventType.ENTITY_DELETED, entity));
    }

  }

  @Override
  public void deleteVariableInstanceByTask(TaskEntity task) {
    Map<String, VariableInstanceEntity> variableInstances = task.getVariableInstances();
    if (variableInstances != null) {
      for (VariableInstanceEntity variableInstance : variableInstances.values()) {
        delete(variableInstance);
      }
    }
  }
}

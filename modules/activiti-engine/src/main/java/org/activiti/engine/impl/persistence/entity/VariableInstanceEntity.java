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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.BulkDeleteable;
import org.activiti.engine.impl.db.HasRevision;
import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.impl.util.ReflectUtil;
import org.activiti.engine.impl.variable.EntityMetaData;
import org.activiti.engine.impl.variable.JPAEntityMappings;
import org.activiti.engine.impl.variable.JPAEntityVariableType;
import org.activiti.engine.impl.variable.ValueFields;
import org.activiti.engine.impl.variable.VariableType;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Tom Baeyens
 * @author Marcus Klimstra (CGI)
 */
public class VariableInstanceEntity implements ValueFields, PersistentObject, HasRevision, BulkDeleteable, Serializable {

  private static final long serialVersionUID = 1L;

  protected String id;
  protected int revision;

  protected String name;
  protected VariableType type;

  protected String processInstanceId;
  protected String executionId;
  protected String taskId;

  protected Long longValue;
  protected Double doubleValue; 
  protected String textValue;
  protected String textValue2;
  protected final ByteArrayRef byteArrayRef = new ByteArrayRef();

  protected Object cachedValue;
  protected boolean forcedUpdate;
  protected boolean deleted = false;

  protected static final ThreadLocal<List<VariableInstanceEntity>> threadLocalInstances = new ThreadLocal<List<VariableInstanceEntity>>();
  protected List<VariableInstanceEntity> queryInstances;

  // Default constructor for SQL mapping
  protected VariableInstanceEntity() {
    initializeQueryInstances();
  }
  
  public static void touch(VariableInstanceEntity variableInstance) {
	  Context.getCommandContext()
      .getDbSqlSession()
      .touch(variableInstance);
	  
  }
  
  public static VariableInstanceEntity createAndInsert(String name, VariableType type, Object value) {
    VariableInstanceEntity variableInstance = create(name, type, value);

    Context.getCommandContext()
      .getDbSqlSession()
      .insert(variableInstance);
  
    return variableInstance;
  }
  
  public static VariableInstanceEntity create(String name, VariableType type, Object value) {
    VariableInstanceEntity variableInstance = new VariableInstanceEntity();
    variableInstance.name = name;
    variableInstance.type = type;
    variableInstance.setValue(value);
    return variableInstance;
  }

  public void setExecution(ExecutionEntity execution) {
    this.executionId = execution.getId();
    this.processInstanceId = execution.getProcessInstanceId();
    forceUpdate();
  }
  
  public void forceUpdate() {
	  forcedUpdate = true;
  }

  public void delete() {
    Context
      .getCommandContext()
      .getDbSqlSession()
      .delete(this);
    
    byteArrayRef.delete();
    deleted = true; 
  }

  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    if (longValue != null) {
      persistentState.put("longValue", longValue);
    }
    if (doubleValue != null) {
      persistentState.put("doubleValue", doubleValue);
    }
    if (textValue != null) {
      persistentState.put("textValue", textValue);
    }
    if (textValue2 != null) {
      persistentState.put("textValue2", textValue2);
    }
    if (byteArrayRef.getId() != null) {
      persistentState.put("byteArrayValueId", byteArrayRef.getId());
    }
    if (forcedUpdate) {
      persistentState.put("forcedUpdate", Boolean.TRUE);
    }
    return persistentState;
  }
  
  public int getRevisionNext() {
    return revision+1;
  }
  
  
  public boolean isDeleted() {
    return deleted;
  }

  // lazy initialized relations ///////////////////////////////////////////////

  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }
  
  // byte array value /////////////////////////////////////////////////////////
  
  @Override
  public byte[] getBytes() {
    return byteArrayRef.getBytes();
  }

  @Override
  public void setBytes(byte[] bytes) {
    byteArrayRef.setValue("var-" + name, bytes);
  }
  
  @Override @Deprecated
  public ByteArrayEntity getByteArrayValue() {
    return byteArrayRef.getEntity();
  }
  
  @Override @Deprecated
  public String getByteArrayValueId() {
    return byteArrayRef.getId();
  }

  @Override @Deprecated
  public void setByteArrayValue(byte[] bytes) {
    setBytes(bytes);
  }

  // value ////////////////////////////////////////////////////////////////////

  public Object getValue() {
    if (!type.isCachable() || cachedValue==null) {
      if(type instanceof JPAEntityVariableType) {
        initializeQueryInstances();
        ensureJpaVariablesInitialized();
      } else {
        cachedValue = type.getValue(this);
      }
    }
    return cachedValue;
  }

  private void ensureJpaVariablesInitialized() {
    Map<String, List<VariableInstanceEntity>> entityMap = new HashMap<String, List<VariableInstanceEntity>>();
    Map<String, List<String>> entityIdMap = new HashMap<String, List<String>>();
    ListIterator<VariableInstanceEntity> it = queryInstances.listIterator();
    // Find all uninitialized jpa entities
    while (it.hasNext()) {
      VariableInstanceEntity entity = it.next();
      it.remove();
      if(entity.getType() != null && JPAEntityVariableType.TYPE_NAME.equals(entity.getType().getTypeName())) {
        String className = entity.getTextValue();
        List<VariableInstanceEntity> entities = entityMap.get(className);
        if (entities == null) {
          entities = new ArrayList<VariableInstanceEntity>();
          entityMap.put(className, entities);
        }
        entities.add(entity);
        String id = entity.getTextValue2();
        List<String> entityIds = entityIdMap.get(className);
        if (entityIds == null) {
          entityIds = new ArrayList<String>();
          entityIdMap.put(className, entityIds);
        }
        entityIds.add(id);
      }
    }
    // load entities from the database and initialize variable instances
    JPAEntityMappings mappings = new JPAEntityMappings();
    for (Map.Entry<String, List<String>> entry : entityIdMap.entrySet()) {
      String className = entry.getKey();
      final List<Object> entities = mappings.getJPAEntities(className, entry.getValue());
      outer: for(VariableInstanceEntity instance : entityMap.get(className)) {
        EntityMetaData metaData = mappings.getEntityMetaData(ReflectUtil.loadClass(className));
        String stringId = instance.getTextValue2();
        Object instanceId = mappings.createId(metaData, stringId);
        for (Object entity : entities) {
          Object entityId = mappings.getIdValue(entity, metaData);
          if(Objects.equals(entityId, instanceId)) {
            instance.setCachedValue(entity);
            continue outer;
          }
        }
        // if entity has not been found within batch query then try to find it by id.
        // some types eg. java.sql.Date, java.sql.Time can be truncated by database
        Object entity = mappings.getJPAEntity(className, stringId);
        if(entity != null) {
          instance.setCachedValue(entity);
        } else {
          throw new ActivitiException("Entity does not exist: " + className + " - " + instanceId);
        }
      }
    }
  }

  public void setValue(Object value) {
    type.setValue(value, this);
    cachedValue = value;
    initializeQueryInstances().remove(this);
  }

  /**
   * @return list of jpa-entities to load
   */
  private List<VariableInstanceEntity> initializeQueryInstances() {
    // initialize jpa-entity lists and put 'this' in that list
    if(queryInstances == null) {
      queryInstances = threadLocalInstances.get();
      if(queryInstances == null) {
        queryInstances = new ArrayList<VariableInstanceEntity>();
        threadLocalInstances.set(queryInstances);
      }
    }
    if(!queryInstances.contains(this)) {
      queryInstances.add(this);
    }
    return queryInstances;
  }

  // getters and setters //////////////////////////////////////////////////////

  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  
  public int getRevision() {
    return revision;
  }
  public void setRevision(int revision) {
    this.revision = revision;
  }

  public String getName() {
    return name;
  }

  public VariableType getType() {
    return type;
  }
  public void setType(VariableType type) {
    this.type = type;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }
  public String getTaskId() {
    return taskId;
  }
  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }
  public String getExecutionId() {
    return executionId;
  }
  
  public Long getLongValue() {
    return longValue;
  }
  public void setLongValue(Long longValue) {
    this.longValue = longValue;
  }
  
  public Double getDoubleValue() {
    return doubleValue;
  }
  public void setDoubleValue(Double doubleValue) {
    this.doubleValue = doubleValue;
  }
  
  public String getTextValue() {
    return textValue;
  }
  public void setTextValue(String textValue) {
    this.textValue = textValue;
  }

  public String getTextValue2() {
    return textValue2;
  }
  public void setTextValue2(String textValue2) {
    this.textValue2 = textValue2;
  }

  public Object getCachedValue() {
    return cachedValue;
  }
  public void setCachedValue(Object cachedValue) {
    this.cachedValue = cachedValue;
  }

  // misc methods /////////////////////////////////////////////////////////////

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("VariableInstanceEntity[");
    sb.append("id=").append(id);
    sb.append(", name=").append(name);
    sb.append(", type=").append(type != null ? type.getTypeName() : "null");
    if (longValue != null) {
      sb.append(", longValue=").append(longValue);
    }
    if (doubleValue != null) {
      sb.append(", doubleValue=").append(doubleValue);
    }
    if (textValue != null) {
      sb.append(", textValue=").append(StringUtils.abbreviate(textValue, 40));
    }
    if (textValue2 != null) {
      sb.append(", textValue2=").append(StringUtils.abbreviate(textValue2, 40));
    }
    if (byteArrayRef.getId() != null) {
      sb.append(", byteArrayValueId=").append(byteArrayRef.getId());
    }
    sb.append("]");
    return sb.toString();
  }
  
}

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
package org.activiti.engine.impl.runtime;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.task.TaskEntity;
import org.activiti.engine.impl.variable.Type;

/**
 * @author Tom Baeyens
 */
public class VariableInstanceEntity implements Serializable, PersistentObject {

  private static final long serialVersionUID = 1L;

  protected String id;
  protected int revision;

  protected String name;

  protected String processInstanceId;
  protected String executionId;
  protected String taskId;

  protected Long longValue;
  protected Double doubleValue; 
  protected String textValue;
  protected String textValue2;

  protected ByteArrayEntity byteArrayValue;
  protected String byteArrayValueId;

  protected int historyNextIndex;

  protected Object cachedValue;

  protected Type type;
  
  // Default constructor for SQL mapping
  protected VariableInstanceEntity() {
  }

  public static VariableInstanceEntity createAndInsert(String name, Type type, Object value) {
    VariableInstanceEntity variableInstance = create(name, type, value);

    CommandContext
      .getCurrent()
      .getDbSqlSession()
      .insert(variableInstance);
  
    return variableInstance;
  }
  
  public static VariableInstanceEntity create(String name, Type type, Object value) {
    VariableInstanceEntity variableInstance = new VariableInstanceEntity();
    variableInstance.name = name;
    variableInstance.type = type;
    variableInstance.historyNextIndex = 0;
    variableInstance.setValue(value);
    
    return variableInstance;
  }

  public void setExecution(ExecutionEntity execution) {
    this.executionId = execution.getId();
    this.processInstanceId = execution.getProcessInstanceId();
  }

  public void setTask(TaskEntity task) {
    if (task != null) {
      this.taskId = task.getId();
    } else {
      this.taskId = null;
    }
  }

  public void delete() {
    // delete variable
    DbSqlSession dbSqlSession = CommandContext
      .getCurrent()
      .getDbSqlSession();
    
    dbSqlSession.delete(VariableInstanceEntity.class, id);

    if (byteArrayValueId != null) {
      dbSqlSession.delete(ByteArrayEntity.class, byteArrayValueId);
    }
  }

  public void setByteArrayValue(ByteArrayEntity byteArrayValue) {
    this.byteArrayValue = byteArrayValue;
    if (byteArrayValue != null) {
      this.byteArrayValueId = byteArrayValue.getId();
    } else {
      this.byteArrayValueId = null;
    }
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
    if (byteArrayValueId != null) {
      persistentState.put("byteArrayValueId", byteArrayValueId);
    }
    return persistentState;
  }
  
  public int getRevisionNext() {
    return revision+1;
  }

  // lazy initialized relations ///////////////////////////////////////////////

  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }
  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }
  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  public void setByteArrayValueId(String byteArrayValueId) {
    this.byteArrayValueId = byteArrayValueId;
    this.byteArrayValue = null;
  }

  public ByteArrayEntity getByteArrayValue() {
    if ((byteArrayValue == null) && (byteArrayValueId != null)) {
      byteArrayValue = CommandContext.getCurrent().getRuntimeSession().findByteArrayById(byteArrayValueId);
    }
    return byteArrayValue;
  }
  
  public int generateNextHistoryIndex() {
    return historyNextIndex++;
  }

  // type /////////////////////////////////////////////////////////////////////

  public Object getValue() {
    if (!type.isCachable() || cachedValue==null) {
      cachedValue = type.getValue(this);
    }
    return cachedValue;
  }

  public void setValue(Object value) {
    type.setValue(value, this);
    cachedValue = value;
  }

  // getters and setters //////////////////////////////////////////////////////

  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public String getByteArrayValueId() {
    return byteArrayValueId;
  }
  public String getTextValue() {
    return textValue;
  }
  public String getProcessInstanceId() {
    return processInstanceId;
  }
  public String getExecutionId() {
    return executionId;
  }
  public String getTaskId() {
    return taskId;
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
  public void setName(String name) {
    this.name = name;
  }
  public void setTextValue(String textValue) {
    this.textValue = textValue;
  }
  public String getName() {
    return name;
  }
  public int getRevision() {
    return revision;
  }
  public void setRevision(int revision) {
    this.revision = revision;
  }
  public void setType(Type type) {
    this.type = type;
  }
  public Type getType() {
    return type;
  }
  public Object getCachedValue() {
    return cachedValue;
  }
  public void setCachedValue(Object cachedValue) {
    this.cachedValue = cachedValue;
  }
  public String getTextValue2() {
    return textValue2;
  }
  public void setTextValue2(String textValue2) {
    this.textValue2 = textValue2;
  }
  public int getHistoryNextIndex() {
    return historyNextIndex;
  }
  public void setHistoryNextIndex(int historyNextIndex) {
    this.historyNextIndex = historyNextIndex;
  }
}

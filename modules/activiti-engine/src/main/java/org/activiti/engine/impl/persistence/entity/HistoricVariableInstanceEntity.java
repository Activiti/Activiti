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
import java.util.List;

import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.HasRevision;
import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.impl.variable.ValueFields;
import org.activiti.engine.impl.variable.VariableType;

/**
 * @author Christian Lipphardt (camunda)
 */
public class HistoricVariableInstanceEntity implements ValueFields, HistoricVariableInstance, PersistentObject, HasRevision, Serializable {

  private static final long serialVersionUID = 1L;

  protected String id;
  protected String processInstanceId;
  
  protected String taskId;
  protected String executionId;
  
  protected String name;
  protected int revision;
  protected VariableType variableType;

  protected Long longValue;
  protected Double doubleValue;
  protected String textValue;
  protected String textValue2;

  protected ByteArrayEntity byteArrayValue;
  protected String byteArrayValueId;

  protected Object cachedValue;

  public HistoricVariableInstanceEntity() {
  }

  public HistoricVariableInstanceEntity(VariableInstanceEntity variableInstance) {
    this.id = variableInstance.getId();
    this.processInstanceId = variableInstance.getProcessInstanceId();
    this.executionId = variableInstance.getExecutionId();
    this.taskId = variableInstance.getTaskId();
    this.revision = variableInstance.getRevision();
    this.name = variableInstance.getName();
    this.variableType = variableInstance.getType();

    copyValue(variableInstance);
  }
  
  public void copyValue(VariableInstanceEntity variableInstance) {
    this.textValue = variableInstance.getTextValue();
    this.textValue2 = variableInstance.getTextValue2();
    this.doubleValue = variableInstance.getDoubleValue();
    this.longValue = variableInstance.getLongValue();
    if (variableInstance.getByteArrayValueId()!=null) {
      setByteArrayValue(variableInstance.getByteArrayValue().getBytes());
    }
  }

  public void delete() {
    deleteByteArrayValue();
    Context
      .getCommandContext()
      .getDbSqlSession()
      .delete(this);
  }

  public Object getPersistentState() {
    List<Object> state = new ArrayList<Object>(5);
    state.add(textValue);
    state.add(textValue2);
    state.add(doubleValue);
    state.add(longValue);
    state.add(byteArrayValueId);
    return state;
  }
  
  public int getRevisionNext() {
    return revision+1;
  }

  public Object getValue() {
    if (!variableType.isCachable() || cachedValue == null) {
      cachedValue = variableType.getValue(this);
    }
    return cachedValue;
  }
  
  // byte array value /////////////////////////////////////////////////////////
  
  // i couldn't find a easy readable way to extract the common byte array value logic
  // into a common class.  therefor it's duplicated in VariableInstanceEntity, 
  // HistoricVariableInstance and HistoricDetailVariableInstanceUpdateEntity 
  
  public String getByteArrayValueId() {
    return byteArrayValueId;
  }

  public void setByteArrayValueId(String byteArrayValueId) {
    this.byteArrayValueId = byteArrayValueId;
    this.byteArrayValue = null;
  }

  public ByteArrayEntity getByteArrayValue() {
    if ((byteArrayValue == null) && (byteArrayValueId != null)) {
      byteArrayValue = Context
        .getCommandContext()
        .getDbSqlSession()
        .selectById(ByteArrayEntity.class, byteArrayValueId);
    }
    return byteArrayValue;
  }
  
  public void setByteArrayValue(byte[] bytes) {
    ByteArrayEntity byteArrayValue = null;
    if (this.byteArrayValueId!=null) {
      getByteArrayValue();
      Context
        .getCommandContext()
        .getByteArrayManager()
        .deleteByteArrayById(this.byteArrayValueId);
    }
    if (bytes!=null) {
      byteArrayValue = new ByteArrayEntity(bytes);
      Context
        .getCommandContext()
        .getDbSqlSession()
        .insert(byteArrayValue);
    }
    this.byteArrayValue = byteArrayValue;
    if (byteArrayValue != null) {
      this.byteArrayValueId = byteArrayValue.getId();
    } else {
      this.byteArrayValueId = null;
    }
  }

  protected void deleteByteArrayValue() {
    if (byteArrayValueId != null) {
      // the next apparently useless line is probably to ensure consistency in the DbSqlSession 
      // cache, but should be checked and docced here (or removed if it turns out to be unnecessary)
      getByteArrayValue();
      Context
        .getCommandContext()
        .getByteArrayManager()
        .deleteByteArrayById(this.byteArrayValueId);
    }
  }

  // getters and setters //////////////////////////////////////////////////////

  public String getVariableTypeName() {
    return (variableType != null ? variableType.getTypeName() : null);
  }

  public String getVariableName() {
    return name;
  }

  public VariableType getVariableType() {
    return variableType;
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

  public void setName(String name) {
    this.name = name;
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

  public void setByteArrayValue(ByteArrayEntity byteArrayValue) {
    this.byteArrayValue = byteArrayValue;
  }

  public Object getCachedValue() {
    return cachedValue;
  }

  public void setCachedValue(Object cachedValue) {
    this.cachedValue = cachedValue;
  }

  public void setVariableType(VariableType variableType) {
    this.variableType = variableType;
  }

  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  public String getId() {
    return id;
  }
  
  public void setId(String id) {
    this.id = id;
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
  
  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }
}

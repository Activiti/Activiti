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
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.ActivitiVariableEvent;
import org.activiti.engine.delegate.event.impl.ActivitiEventBuilder;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.BulkDeleteable;
import org.activiti.engine.impl.variable.VariableType;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Tom Baeyens
 * @author Marcus Klimstra (CGI)
 */
public class VariableInstanceEntity implements VariableInstance, BulkDeleteable, Serializable {

  private static final long serialVersionUID = 1L;

  protected String id;
  protected int revision;

  protected String name;
  protected String localizedName;
  protected String localizedDescription;
  protected VariableType type;
  protected String typeName;

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
  
  // Default constructor for SQL mapping
  protected VariableInstanceEntity() {
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
    variableInstance.typeName = type.getTypeName();
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
    
    if (!deleted && Context.getProcessEngineConfiguration() != null && Context.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
      Context.getProcessEngineConfiguration().getEventDispatcher().dispatchEvent(
          createVariableDeleteEvent(this)
        );
    }
    
    byteArrayRef.delete();
    deleted = true; 
    
  }
  
  protected static ActivitiVariableEvent createVariableDeleteEvent(VariableInstanceEntity variableInstance) {
    return ActivitiEventBuilder.createVariableEvent(ActivitiEventType.VARIABLE_DELETED, variableInstance.getName(), null, variableInstance.getType(),
        variableInstance.getTaskId(), variableInstance.getExecutionId(), variableInstance.getProcessInstanceId(), null);
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
      cachedValue = type.getValue(this);
    }
    return cachedValue;
  }

  public void setValue(Object value) {
    type.setValue(value, this);
    typeName = type.getTypeName();
    cachedValue = value;
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
  
  public void setName(String name) {
    this.name = name;
  }

  public String getLocalizedName() {
    return localizedName;
  }

  public void setLocalizedName(String localizedName) {
    this.localizedName = localizedName;
  }

  public String getLocalizedDescription() {
    return localizedDescription;
  }

  public void setLocalizedDescription(String localizedDescription) {
    this.localizedDescription = localizedDescription;
  }
  
  public String getTypeName() {
    return typeName;
  }
  public void setTypeName(String typeName) {
    this.typeName = typeName;
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

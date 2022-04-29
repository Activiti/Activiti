/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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

import org.activiti.engine.impl.db.BulkDeleteable;
import org.activiti.engine.impl.variable.ValueFields;
import org.activiti.engine.impl.variable.VariableType;
import org.apache.commons.lang3.StringUtils;

/**



 */
public class VariableInstanceEntityImpl extends AbstractEntity implements VariableInstanceEntity, ValueFields, BulkDeleteable, Serializable {

  private static final long serialVersionUID = 1L;

  protected String name;
  protected VariableType type;
  protected String typeName;

  protected String processInstanceId;
  protected String executionId;
  protected String taskId;

  protected Long longValue;
  protected Double doubleValue;
  protected String textValue;
  protected String textValue2;
  protected ByteArrayRef byteArrayRef;

  protected Object cachedValue;
  protected boolean forcedUpdate;
  protected boolean deleted;

  public VariableInstanceEntityImpl() {

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
    if (byteArrayRef != null && byteArrayRef.getId() != null) {
      persistentState.put("byteArrayValueId", byteArrayRef.getId());
    }
    if (forcedUpdate) {
      persistentState.put("forcedUpdate", Boolean.TRUE);
    }
    return persistentState;
  }

  public void setExecution(ExecutionEntity execution) {
    this.executionId = execution.getId();
    this.processInstanceId = execution.getProcessInstanceId();
    forceUpdate();
  }

  public void forceUpdate() {
    forcedUpdate = true;
  }

  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }

  // byte array value ///////////////////////////////////////////////////////////

  @Override
  public byte[] getBytes() {
    ensureByteArrayRefInitialized();
    return byteArrayRef.getBytes();
  }

  @Override
  public void setBytes(byte[] bytes) {
    ensureByteArrayRefInitialized();
    byteArrayRef.setValue("var-" + name, bytes);
  }

  public ByteArrayRef getByteArrayRef() {
    return byteArrayRef;
  }

  protected void ensureByteArrayRefInitialized() {
    if (byteArrayRef == null) {
      byteArrayRef = new ByteArrayRef();
    }
  }

  // value //////////////////////////////////////////////////////////////////////

  public Object getValue() {
    if (!type.isCachable() || cachedValue == null) {
      cachedValue = type.getValue(this);
    }
    return cachedValue;
  }

  public void setValue(Object value) {
    type.setValue(value, this);
    typeName = type.getTypeName();
    cachedValue = value;
  }

  // getters and setters ////////////////////////////////////////////////////////

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public String getTypeName() {
    if (typeName != null) {
      return typeName;
    } else if (type != null) {
      return type.getTypeName();
    } else {
      return typeName;
    }
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

  // misc methods ///////////////////////////////////////////////////////////////

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
    if (byteArrayRef != null && byteArrayRef.getId() != null) {
      sb.append(", byteArrayValueId=").append(byteArrayRef.getId());
    }
    sb.append("]");
    return sb.toString();
  }

}

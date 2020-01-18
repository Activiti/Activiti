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
import java.util.Date;
import java.util.HashMap;

import org.activiti.engine.impl.db.BulkDeleteable;
import org.activiti.engine.impl.variable.VariableType;
import org.apache.commons.lang3.StringUtils;

/**


 */
public class HistoricVariableInstanceEntityImpl extends AbstractEntity implements HistoricVariableInstanceEntity, BulkDeleteable, Serializable {

  private static final long serialVersionUID = 1L;

  protected String name;
  protected VariableType variableType;

  protected String processInstanceId;
  protected String executionId;
  protected String taskId;

  protected Date createTime;
  protected Date lastUpdatedTime;

  protected Long longValue;
  protected Double doubleValue;
  protected String textValue;
  protected String textValue2;
  protected ByteArrayRef byteArrayRef;

  protected Object cachedValue;

  public HistoricVariableInstanceEntityImpl() {
    
  }

  public Object getPersistentState() {
    HashMap<String, Object> persistentState = new HashMap<String, Object>();

    persistentState.put("textValue", textValue);
    persistentState.put("textValue2", textValue2);
    persistentState.put("doubleValue", doubleValue);
    persistentState.put("longValue", longValue);
    
    if (byteArrayRef != null) {
      persistentState.put("byteArrayRef", byteArrayRef.getId());
    }

    persistentState.put("createTime", createTime);
    persistentState.put("lastUpdatedTime", lastUpdatedTime);

    return persistentState;
  }

  public Object getValue() {
    if (!variableType.isCachable() || cachedValue == null) {
      cachedValue = variableType.getValue(this);
    }
    return cachedValue;
  }

  // byte array value /////////////////////////////////////////////////////////

  @Override
  public byte[] getBytes() {
    if (byteArrayRef != null) {
      return byteArrayRef.getBytes();
    }
    return null;
  }

  @Override
  public void setBytes(byte[] bytes) {
    if (byteArrayRef == null) {
      byteArrayRef = new ByteArrayRef();
    }
    byteArrayRef.setValue("hist.var-" + name, bytes);
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

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public String getTaskId() {
    return taskId;
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  public Date getLastUpdatedTime() {
    return lastUpdatedTime;
  }

  public void setLastUpdatedTime(Date lastUpdatedTime) {
    this.lastUpdatedTime = lastUpdatedTime;
  }

  public String getExecutionId() {
    return executionId;
  }

  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }

  public Date getTime() {
    return getCreateTime();
  }
  
  public ByteArrayRef getByteArrayRef() {
    return byteArrayRef;
  }
  
  // common methods //////////////////////////////////////////////////////////

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("HistoricVariableInstanceEntity[");
    sb.append("id=").append(id);
    sb.append(", name=").append(name);
    sb.append(", revision=").append(revision);
    sb.append(", type=").append(variableType != null ? variableType.getTypeName() : "null");
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

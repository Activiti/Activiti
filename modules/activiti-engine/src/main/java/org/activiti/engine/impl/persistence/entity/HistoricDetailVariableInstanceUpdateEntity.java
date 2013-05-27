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

import org.activiti.engine.history.HistoricVariableUpdate;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.HasRevision;
import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.impl.util.ClockUtil;
import org.activiti.engine.impl.variable.ValueFields;
import org.activiti.engine.impl.variable.VariableType;
import org.apache.commons.lang.StringUtils;

/**
 * @author Tom Baeyens
 */
public class HistoricDetailVariableInstanceUpdateEntity extends HistoricDetailEntity implements ValueFields, HistoricVariableUpdate, PersistentObject, HasRevision {
  
  private static final long serialVersionUID = 1L;
  
  protected int revision;

  protected String name;
  protected VariableType variableType;

  protected Long longValue;
  protected Double doubleValue; 
  protected String textValue;
  protected String textValue2;

  protected ByteArrayEntity byteArrayValue;
  protected String byteArrayValueId;

  protected Object cachedValue;

  public HistoricDetailVariableInstanceUpdateEntity() {
  }

  public HistoricDetailVariableInstanceUpdateEntity(VariableInstanceEntity variableInstance) {
    this.processInstanceId = variableInstance.getProcessInstanceId();
    this.executionId = variableInstance.getExecutionId();
    this.taskId = variableInstance.getTaskId();
    this.revision = variableInstance.getRevision();
    this.name = variableInstance.getName();
    this.variableType = variableInstance.getType();
    this.time = ClockUtil.getCurrentTime();
    this.textValue = variableInstance.getTextValue();
    this.textValue2 = variableInstance.getTextValue2();
    this.doubleValue = variableInstance.getDoubleValue();
    this.longValue = variableInstance.getLongValue();
    
    if (variableInstance.getBytes() != null) {

      this.byteArrayValue = ByteArrayEntity.createAndInsert("hist.var-" + name, variableInstance.getBytes());
      this.byteArrayValueId = byteArrayValue.getId();
    }
  }
  
  public Object getValue() {
    if (!variableType.isCachable() || cachedValue==null) {
      cachedValue = variableType.getValue(this);
    }
    return cachedValue;
  }

  public void delete() {
    super.delete();

    if (byteArrayValueId != null) {
      Context
        .getCommandContext()
        .getByteArrayEntityManager()
        .deleteByteArrayById(byteArrayValueId);
    }
  }

  public Object getPersistentState() {
    // HistoricDetailVariableInstanceUpdateEntity is immutable, so always the same object is returned
    return HistoricDetailVariableInstanceUpdateEntity.class;
  }
  
  public String getVariableTypeName() {
    return (variableType != null ? variableType.getTypeName() : null);
  }

  public int getRevisionNext() {
    return revision + 1;
  }

  // byte array value /////////////////////////////////////////////////////////
  
  // i couldn't find a easy readable way to extract the common byte array value logic
  // into a common class.  therefor it's duplicated in VariableInstanceEntity, 
  // HistoricVariableInstance and HistoricDetailVariableInstanceUpdateEntity 
  
  @Override
  public byte[] getBytes() {
    ByteArrayEntity byteArrayValue = getByteArrayEntity();
    return (byteArrayValue != null ? byteArrayValue.getBytes() : null);
  }

  @Override
  public void setBytes(byte[] bytes) {
    if (bytes == null) {
      if (byteArrayValueId != null) {
        Context.getCommandContext()
          .getByteArrayEntityManager()
          .deleteByteArrayById(byteArrayValueId);
        byteArrayValueId = null;
      }
    }
    else {
      if (byteArrayValueId == null) {
        byteArrayValue = ByteArrayEntity.createAndInsert("var-", bytes);
        byteArrayValueId = byteArrayValue.getId();
      }
      else {
        ByteArrayEntity byteArrayValue = getByteArrayEntity();
        byteArrayValue.setBytes(bytes);
      }
    }
  }

  @Override @Deprecated
  public String getByteArrayValueId() {
    return byteArrayValueId;
  }

  @Override @Deprecated
  public ByteArrayEntity getByteArrayValue() {
    return getByteArrayEntity();
  }
  
  @Override @Deprecated
  public void setByteArrayValue(byte[] bytes) {
    setBytes(bytes);
  }

  private ByteArrayEntity getByteArrayEntity() {
    if ((byteArrayValue == null) && (byteArrayValueId != null)) {
      byteArrayValue = Context
        .getCommandContext()
        .getDbSqlSession()
        .selectById(ByteArrayEntity.class, byteArrayValueId);
    }
    return byteArrayValue;
  }
  
  // getters and setters //////////////////////////////////////////////////////
  
  public int getRevision() {
    return revision;
  }
  public void setRevision(int revision) {
    this.revision = revision;
  }
  
  public String getVariableName() {
    return name;
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  public VariableType getVariableType() {
    return variableType;
  }
  public void setVariableType(VariableType variableType) {
    this.variableType = variableType;
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
    if (byteArrayValueId != null) {
      sb.append(", byteArrayValueId=").append(byteArrayValueId);
    }
    sb.append("]");
    return sb.toString();
  }
  
}

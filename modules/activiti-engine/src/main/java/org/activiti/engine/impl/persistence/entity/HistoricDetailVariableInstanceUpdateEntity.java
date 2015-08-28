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
import org.activiti.engine.impl.variable.ValueFields;
import org.activiti.engine.impl.variable.VariableType;
import org.apache.commons.lang3.StringUtils;

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
  protected final ByteArrayRef byteArrayRef = new ByteArrayRef();

  protected Object cachedValue;

  protected HistoricDetailVariableInstanceUpdateEntity() {
    this.detailType = "VariableUpdate";
  }

  public static HistoricDetailVariableInstanceUpdateEntity copyAndInsert(VariableInstanceEntity variableInstance) {
    HistoricDetailVariableInstanceUpdateEntity historicVariableUpdate = new HistoricDetailVariableInstanceUpdateEntity();
    historicVariableUpdate.processInstanceId = variableInstance.getProcessInstanceId();
    historicVariableUpdate.executionId = variableInstance.getExecutionId();
    historicVariableUpdate.taskId = variableInstance.getTaskId();
    historicVariableUpdate.time = Context.getProcessEngineConfiguration().getClock().getCurrentTime();
    historicVariableUpdate.revision = variableInstance.getRevision();
    historicVariableUpdate.name = variableInstance.getName();
    historicVariableUpdate.variableType = variableInstance.getType();
    historicVariableUpdate.textValue = variableInstance.getTextValue();
    historicVariableUpdate.textValue2 = variableInstance.getTextValue2();
    historicVariableUpdate.doubleValue = variableInstance.getDoubleValue();
    historicVariableUpdate.longValue = variableInstance.getLongValue();

    if (variableInstance.getBytes() != null) {
      String byteArrayName = "hist.detail.var-" + variableInstance.getName();
      historicVariableUpdate.byteArrayRef.setValue(byteArrayName, variableInstance.getBytes());
    }
    
    Context.getCommandContext()
      .getDbSqlSession()
      .insert(historicVariableUpdate);

    return historicVariableUpdate;
  }
  
  public Object getValue() {
    if (!variableType.isCachable() || cachedValue==null) {
      cachedValue = variableType.getValue(this);
    }
    return cachedValue;
  }

  public void delete() {
    super.delete();
    
    byteArrayRef.delete();
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
  
  @Override
  public byte[] getBytes() {
    return byteArrayRef.getBytes();
  }

  @Override
  public void setBytes(byte[] bytes) {
    throw new UnsupportedOperationException("HistoricDetailVariableInstanceUpdateEntity is immutable");
  }

  @Override @Deprecated
  public String getByteArrayValueId() {
    return byteArrayRef.getId();
  }

  @Override @Deprecated
  public ByteArrayEntity getByteArrayValue() {
    return byteArrayRef.getEntity();
  }
  
  @Override @Deprecated
  public void setByteArrayValue(byte[] bytes) {
    setBytes(bytes);
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
    sb.append("HistoricDetailVariableInstanceUpdateEntity[");
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
    if (byteArrayRef.getId() != null) {
      sb.append(", byteArrayValueId=").append(byteArrayRef.getId());
    }
    sb.append("]");
    return sb.toString();
  }
  
}

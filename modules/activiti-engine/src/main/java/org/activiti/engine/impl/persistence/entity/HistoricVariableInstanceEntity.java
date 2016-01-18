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

import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.db.BulkDeleteable;
import org.activiti.engine.impl.db.HasRevision;
import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.impl.variable.ValueFields;
import org.activiti.engine.impl.variable.VariableType;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Christian Lipphardt (camunda)
 * @author Joram Barrez
 */
public class HistoricVariableInstanceEntity implements ValueFields, HistoricVariableInstance, PersistentObject, HasRevision, BulkDeleteable, Serializable {

  private static final long serialVersionUID = 1L;

  protected String id;
  protected int revision;

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
  protected final ByteArrayRef byteArrayRef = new ByteArrayRef();

  protected Object cachedValue;

  // Default constructor for SQL mapping
  protected HistoricVariableInstanceEntity() {
  }

  public static HistoricVariableInstanceEntity copyAndInsert(VariableInstanceEntity variableInstance) {
    HistoricVariableInstanceEntity historicVariableInstance = new HistoricVariableInstanceEntity();
    historicVariableInstance.id = variableInstance.getId();
    historicVariableInstance.processInstanceId = variableInstance.getProcessInstanceId();
    historicVariableInstance.executionId = variableInstance.getExecutionId();
    historicVariableInstance.taskId = variableInstance.getTaskId();
    historicVariableInstance.revision = variableInstance.getRevision();
    historicVariableInstance.name = variableInstance.getName();
    historicVariableInstance.variableType = variableInstance.getType();
    
    historicVariableInstance.copyValue(variableInstance);
    
    Date time = Context.getProcessEngineConfiguration().getClock().getCurrentTime();
    historicVariableInstance.setCreateTime(time);
    historicVariableInstance.setLastUpdatedTime(time);
    
    Context.getCommandContext()
      .getDbSqlSession()
      .insert(historicVariableInstance);
    
    return historicVariableInstance;
  }
  
  public void copyValue(VariableInstanceEntity variableInstance) {
    this.textValue = variableInstance.getTextValue();
    this.textValue2 = variableInstance.getTextValue2();
    this.doubleValue = variableInstance.getDoubleValue();
    this.longValue = variableInstance.getLongValue();
    
    this.variableType = variableInstance.getType();
    if (variableInstance.getByteArrayValueId()!=null) {
      setByteArrayValue(variableInstance.getByteArrayValue().getBytes());
    }
    
    this.lastUpdatedTime = Context.getProcessEngineConfiguration().getClock().getCurrentTime();
  }

  public void delete() {
    Context
      .getCommandContext()
      .getDbSqlSession()
      .delete(this);
    
    byteArrayRef.delete();
  }

  public Object getPersistentState() {
  	HashMap<String, Object> persistentState = new HashMap<String, Object>();
  	
  	persistentState.put("textValue", textValue);
  	persistentState.put("textValue2", textValue2);
  	persistentState.put("doubleValue", doubleValue);
  	persistentState.put("longValue", longValue);
  	persistentState.put("byteArrayRef", byteArrayRef.getId());
  	persistentState.put("createTime", createTime);
  	persistentState.put("lastUpdatedTime", lastUpdatedTime);
  	
  	return persistentState;
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
  
  @Override
  public byte[] getBytes() {
    return byteArrayRef.getBytes();
  }

  @Override
  public void setBytes(byte[] bytes) {
    byteArrayRef.setValue("hist.var-" + name, bytes);
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

  // getters and setters //////////////////////////////////////////////////////

  public String getId() {
    return id;
  }
  
  public void setId(String id) {
    this.id = id;
  }

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

  // common methods  //////////////////////////////////////////////////////////

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
    if (byteArrayRef.getId() != null) {
      sb.append(", byteArrayValueId=").append(byteArrayRef.getId());
    }
    sb.append("]");
    return sb.toString();
  }
  
}

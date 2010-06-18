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
package org.activiti.impl.variable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.activiti.impl.bytes.ByteArrayImpl;
import org.activiti.impl.db.execution.DbExecutionImpl;
import org.activiti.impl.interceptor.CommandContext;
import org.activiti.impl.persistence.PersistenceSession;
import org.activiti.impl.persistence.PersistentObject;
import org.activiti.impl.task.TaskImpl;

/**
 * @author Tom Baeyens
 */
public class VariableInstance implements Serializable, PersistentObject {

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

  protected ByteArrayImpl byteArrayValue;
  protected String byteArrayValueId;
  
  protected Object cachedValue;
  
  protected String typeName;
  protected Type type;

  public VariableInstance() {
  }
  
  public VariableInstance(String name, String typeName) {
    this.name = name;
    this.typeName = typeName;
    this.type = CommandContext
      .getCurrent()
      .getVariableTypes()
      .getVariableType(typeName);
  }

  public VariableInstance(String name, Object value) {
    this.name = name;
    this.type = CommandContext
      .getCurrent()
      .getVariableTypes()
      .findVariableType(value);
    this.typeName = type.getTypeName();
  }
  
  public void setExecution(DbExecutionImpl execution) {
    if (execution==null) {
      this.executionId = null;
      this.processInstanceId = null;
    } else {
      this.executionId = execution.getId();
      this.processInstanceId = execution.getProcessInstanceId();
    }
  }

  public void setTask(TaskImpl task) {
    if (task!=null) {
      this.taskId = task.getId();
    } else {
      this.taskId = null;
    }
  }

  public void delete() {
    PersistenceSession persistenceSession = CommandContext
      .getCurrent()
      .getPersistenceSession();
    
    persistenceSession.delete(this);
    
    if (byteArrayValueId!=null) {
      persistenceSession.delete(getByteArrayValue());
    }
  }

  public void setByteArrayValue(ByteArrayImpl byteArrayValue) {
    this.byteArrayValue = byteArrayValue;
    if (byteArrayValue!=null) {
      this.byteArrayValueId = byteArrayValue.getId();
    } else {
      this.byteArrayValueId = null;
    }
  }

  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    if (longValue!=null) {
      persistentState.put("longValue", longValue);
    }
    if (doubleValue!=null) {
      persistentState.put("doubleValue", doubleValue);
    }
    if (textValue!=null) {
      persistentState.put("textValue", textValue);
    }
    if (byteArrayValueId!=null) {
      persistentState.put("byteArrayValueId", byteArrayValueId);
    }
    return persistentState;
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

  public ByteArrayImpl getByteArrayValue() {
    if ( (byteArrayValue==null) && (byteArrayValueId!=null) ) {
      byteArrayValue = CommandContext 
        .getCurrent()
        .getPersistenceSession()
        .findByteArrayById(byteArrayValueId);
    }
    return byteArrayValue;
  }

  // type /////////////////////////////////////////////////////////////////////
  
  public void setTypeName(String typeName) {
    this.typeName = typeName;
    VariableTypes variableTypes = CommandContext
      .getCurrent()
      .getVariableTypes();
    this.type = variableTypes.getVariableType(typeName);
  }
  
  public Object getValue() {
    return type.getValue(this);
  }

  public void setValue(Object value) {
    type.setValue(value, this);
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
  public String getTypeName() {
    return typeName;
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
}

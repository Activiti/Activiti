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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.cfg.ProcessEngineConfiguration;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.history.HistoricVariableUpdateEntity;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.variable.VariableType;
import org.activiti.engine.impl.variable.VariableTypes;



/**
 * @author Tom Baeyens
 */
public class VariableMap implements Map<String, Object> , Serializable {
  
  private static final long serialVersionUID = 1L;
  
  protected String executionId;
  protected String processInstanceId;
  protected Map<String, VariableInstanceEntity> variableInstances = null;

  public VariableMap(String executionId, String processInstanceId) {
    this.executionId = executionId;
    this.processInstanceId = processInstanceId;
  }
  
  /** returns an initialized empty variable map */
  public static VariableMap createNewInitialized(String executionId, String processInstanceId) {
    VariableMap variableMap = new VariableMap(executionId, processInstanceId);
    variableMap.variableInstances = new HashMap<String, VariableInstanceEntity>();
    return variableMap;
  }

  protected void ensureInitialized() {
    if (variableInstances==null) {
      variableInstances = new HashMap<String, VariableInstanceEntity>();
      CommandContext commandContext = CommandContext.getCurrent();
      if (commandContext == null) {
        throw new ActivitiException("lazy loading outside command context");
      }
      List<VariableInstanceEntity> variableInstancesList = commandContext.getRuntimeSession().findVariableInstancesByExecutionId(executionId);
      for (VariableInstanceEntity variableInstance : variableInstancesList) {
        variableInstances.put(variableInstance.getName(), variableInstance);
      }
    }
  }

  public Object get(Object key) {
    ensureInitialized();
    VariableInstanceEntity variableInstance = variableInstances.get(key);
    if (variableInstance==null) {
      return null;
    }
    return variableInstance.getValue();
  }
  

  public boolean isEmpty() {
    ensureInitialized();
    return variableInstances.isEmpty();
  }

  public boolean containsKey(Object key) {
    ensureInitialized();
    return variableInstances.containsKey(key);
  }

  public Set<String> keySet() {
    ensureInitialized();
    return variableInstances.keySet();
  }

  public Object put(String key, Object value) {
    ensureInitialized();
    VariableInstanceEntity variableInstance = variableInstances.get(key);
    if ((variableInstance != null) && (!variableInstance.getType().isAbleToStore(value))) {
      // delete variable
      remove(key);
      variableInstance = null;
    }
    CommandContext commandContext = CommandContext.getCurrent();
    if (variableInstance == null) {
      VariableTypes variableTypes = commandContext
        .getProcessEngineConfiguration()
        .getVariableTypes();
      
      VariableType type = variableTypes.findVariableType(value);
  
      variableInstance = VariableInstanceEntity.createAndInsert(key, type, value);
      variableInstance.setExecutionId(executionId);
      variableInstance.setProcessInstanceId(processInstanceId);
    }
    
    variableInstance.setValue(value);
    
    int historyLevel = commandContext.getProcessEngineConfiguration().getHistoryLevel();
    if (historyLevel==ProcessEngineConfiguration.HISTORYLEVEL_FULL) {
      DbSqlSession dbSqlSession = commandContext.getDbSqlSession();
      HistoricVariableUpdateEntity historicVariableUpdate = new HistoricVariableUpdateEntity(variableInstance, dbSqlSession);
      dbSqlSession.insert(historicVariableUpdate);
    }
    
    variableInstances.put(key, variableInstance);
    return null;
  }

  public Object remove(Object key) {
    ensureInitialized();
    VariableInstanceEntity variableInstance = variableInstances.remove(key);
    if (variableInstance != null) {
      variableInstance.delete();
    }
    return null;
  }

  public void putAll(Map< ? extends String, ? extends Object> m) {
    for (String key: m.keySet()) {
      put(key, m.get(key));
    }
  }

  public int size() {
    ensureInitialized();
    return variableInstances.size();
  }

  public void clear() {
    ensureInitialized();
    Set<String> keys = new HashSet<String>(variableInstances.keySet());
    for (String key: keys) {
      remove(key);
    }
  }

  // unsupported map operations ///////////////////////////////////////////////
  
  public boolean containsValue(Object value) {
    throw new UnsupportedOperationException("please implement me");
  }

  public Set<java.util.Map.Entry<String, Object>> entrySet() {
    throw new UnsupportedOperationException("please implement me");
  }

  public Collection<Object> values() {
    throw new UnsupportedOperationException("please implement me");
  }
}

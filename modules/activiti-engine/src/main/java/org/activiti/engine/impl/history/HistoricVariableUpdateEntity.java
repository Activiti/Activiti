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

package org.activiti.engine.impl.history;

import java.util.Date;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.history.HistoricVariableUpdate;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.runtime.ByteArrayEntity;
import org.activiti.engine.impl.runtime.VariableInstanceEntity;
import org.activiti.engine.impl.util.ClockUtil;


/**
 * @author Tom Baeyens
 */
public class HistoricVariableUpdateEntity extends VariableInstanceEntity implements HistoricVariableUpdate, PersistentObject {
  
  private static final long serialVersionUID = 1L;
  
  protected String historicFormInstanceId;
  protected Date time;
  
  public HistoricVariableUpdateEntity() {
  }

  public HistoricVariableUpdateEntity(VariableInstanceEntity variableInstance, DbSqlSession dbSqlSession) {
    this.processInstanceId = variableInstance.getProcessInstanceId();
    if (processInstanceId==null) {
      throw new ActivitiException("bug");
    }
    this.executionId = variableInstance.getExecutionId();
    if (executionId==null) {
      throw new ActivitiException("bug");
    }
    this.revision = variableInstance.getRevision();
    this.name = variableInstance.getName();
    this.type = variableInstance.getType();
    this.time = ClockUtil.getCurrentTime();
    if (variableInstance.getByteArrayValueId()!=null) {
      // TODO test and review.  name ok here?
      this.byteArrayValue = new ByteArrayEntity(name, variableInstance.getByteArrayValue().getBytes());
      dbSqlSession.insert(byteArrayValue);
      this.byteArrayValueId = byteArrayValue.getId();
    }
    this.textValue = variableInstance.getTextValue();
    this.textValue2 = variableInstance.getTextValue2();
    this.doubleValue = variableInstance.getDoubleValue();
    this.longValue = variableInstance.getLongValue();
  }
  
  public void delete() {
    DbSqlSession dbSqlSession = CommandContext
      .getCurrent()
      .getDbSqlSession();

    dbSqlSession.delete(HistoricVariableUpdateEntity.class, id);

    if (byteArrayValueId != null) {
      // the next apparently useless line is probably to ensure consistency in the DbSqlSession 
      // cache, but should be checked and docced here (or removed if it turns out to be unnecessary)
      // @see also HistoricVariableInstanceEntity
      getByteArrayValue();
      dbSqlSession.delete(ByteArrayEntity.class, byteArrayValueId);
    }
  }

  public Object getPersistentState() {
    // HistoricVariableUpdateEntity is immutable, so always the same object is returned
    return HistoricVariableUpdateEntity.class;
  }


  public Date getTime() {
    return time;
  }

  public void setTime(Date time) {
    this.time = time;
  }

  public String getVariableName() {
    return name;
  }

  public String getVariableType() {
    return type.getTypeName();
  }

  public String getHistoricFormInstanceId() {
    return historicFormInstanceId;
  }

  public void setHistoricFormInstanceId(String historicFormInstanceId) {
    this.historicFormInstanceId = historicFormInstanceId;
  }
}

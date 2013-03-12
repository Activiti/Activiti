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
package org.activiti.explorer.reporting;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.activiti.engine.ProcessEngines;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.repository.ProcessDefinition;


/**
 * @author Joram Barrez
 */
public class ReportingUtil {
  
  
  public static Connection getCurrentDatabaseConnection() {
    return Context.getCommandContext().getDbSqlSession().getSqlSession().getConnection();
  }
  
  public static ResultSet executeSelectSqlQuery(String sql) throws Exception {
    
    Connection connection = getCurrentDatabaseConnection();
    Statement select = connection.createStatement();
    return select.executeQuery(sql);
  }
  
  public static ProcessDefinition getProcessDefinition(DelegateExecution delegateExecution) {
    ExecutionEntity executionEntity = (ExecutionEntity) delegateExecution;
    if (executionEntity.getProcessDefinition() != null) {
      return (ProcessDefinition) executionEntity.getProcessDefinition();
    } else {
      return ProcessEngines.getDefaultProcessEngine()
              .getRepositoryService()
              .createProcessDefinitionQuery()
              .processDefinitionId(delegateExecution.getProcessDefinitionId())
              .singleResult();
    }
  }

}

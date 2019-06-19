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
package org.activiti.engine.impl.cfg.multitenant;

import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;


/**
 * {@link Command} that is used by the {@link MultiSchemaMultiTenantProcessEngineConfiguration} to 
 * make sure the 'databaseSchemaUpdate' setting is applied for each tenant datasource.
 * @deprecated multi-tenant code will be removed in future version of Activiti and Activiti Cloud
 */
@Deprecated
public class ExecuteSchemaOperationCommand implements Command<Void> {
  
  protected String schemaOperation;
  
  protected TenantInfoHolder tenantInfoHolder;
  
  public ExecuteSchemaOperationCommand(String schemaOperation) {
    this.schemaOperation = schemaOperation;
  }
  
  public Void execute(CommandContext commandContext) {
    if (ProcessEngineConfigurationImpl.DB_SCHEMA_UPDATE_DROP_CREATE.equals(schemaOperation)) {
      try {
        commandContext.getDbSqlSession().dbSchemaDrop();
      } catch (RuntimeException e) {
        // ignore
      }
    }
    if (org.activiti.engine.ProcessEngineConfiguration.DB_SCHEMA_UPDATE_CREATE_DROP.equals(schemaOperation)
        || ProcessEngineConfigurationImpl.DB_SCHEMA_UPDATE_DROP_CREATE.equals(schemaOperation) 
        || ProcessEngineConfigurationImpl.DB_SCHEMA_UPDATE_CREATE.equals(schemaOperation)) {
      commandContext.getDbSqlSession().dbSchemaCreate();

    } else if (org.activiti.engine.ProcessEngineConfiguration.DB_SCHEMA_UPDATE_FALSE.equals(schemaOperation)) {
      commandContext.getDbSqlSession().dbSchemaCheckVersion();

    } else if (ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE.equals(schemaOperation)) {
      commandContext.getDbSqlSession().dbSchemaUpdate();
    }
    
    return null;
  }

  
}

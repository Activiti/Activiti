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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricDetail;
import org.activiti.engine.history.HistoricFormProperty;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.history.HistoricVariableUpdate;
import org.activiti.engine.impl.TablePageQueryImpl;
import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.impl.persistence.AbstractManager;
import org.activiti.engine.management.TableMetaData;
import org.activiti.engine.management.TablePage;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.ibatis.session.RowBounds;


/**
 * @author Tom Baeyens
 */
public class TableDataManager extends AbstractManager {
  
  private static Logger log = Logger.getLogger(TableDataManager.class.getName());
  
  public static Map<Class<?>, String> apiTypeToTableNameMap = new HashMap<Class<?>, String>();
  public static Map<Class<? extends PersistentObject>, String> persistentObjectToTableNameMap = new HashMap<Class<? extends PersistentObject>, String>();
  
  static {
    // runtime
    persistentObjectToTableNameMap.put(TaskEntity.class, "ACT_RU_TASK");
    persistentObjectToTableNameMap.put(ExecutionEntity.class, "ACT_RU_EXECUTION");
    persistentObjectToTableNameMap.put(IdentityLinkEntity.class, "ACT_RU_IDENTITYLINK");
    persistentObjectToTableNameMap.put(VariableInstanceEntity.class, "ACT_RU_VARIABLE");
    
    persistentObjectToTableNameMap.put(JobEntity.class, "ACT_RU_JOB");
    persistentObjectToTableNameMap.put(MessageEntity.class, "ACT_RU_JOB");
    persistentObjectToTableNameMap.put(TimerEntity.class, "ACT_RU_JOB");
    
    persistentObjectToTableNameMap.put(EventSubscriptionEntity.class, "ACT_RU_EVENT_SUBSCRIPTION");
    persistentObjectToTableNameMap.put(CompensateEventSubscriptionEntity.class, "ACT_RU_EVENT_SUBSCRIPTION");    
    persistentObjectToTableNameMap.put(MessageEventSubscriptionEntity.class, "ACT_RU_EVENT_SUBSCRIPTION");    
    persistentObjectToTableNameMap.put(SignalEventSubscriptionEntity.class, "ACT_RU_EVENT_SUBSCRIPTION");
        
    // repository
    persistentObjectToTableNameMap.put(DeploymentEntity.class, "ACT_RE_DEPLOYMENT");
    persistentObjectToTableNameMap.put(ProcessDefinitionEntity.class, "ACT_RE_PROCDEF");
    
    // history
    persistentObjectToTableNameMap.put(CommentEntity.class, "ACT_HI_COMMENT");
    
    persistentObjectToTableNameMap.put(HistoricActivityInstanceEntity.class, "ACT_HI_ACTINST");
    persistentObjectToTableNameMap.put(AttachmentEntity.class, "ACT_HI_ATTACHMEN");
    persistentObjectToTableNameMap.put(HistoricProcessInstanceEntity.class, "ACT_HI_PROCINST");
    persistentObjectToTableNameMap.put(HistoricVariableInstanceEntity.class, "ACT_HI_VARINST");
    persistentObjectToTableNameMap.put(HistoricTaskInstanceEntity.class, "ACT_HI_TASKINST");
    
    // a couple of stuff goes to the same table
    persistentObjectToTableNameMap.put(HistoricDetailAssignmentEntity.class, "ACT_HI_DETAIL");
    persistentObjectToTableNameMap.put(HistoricDetailTransitionInstanceEntity.class, "ACT_HI_DETAIL");
    persistentObjectToTableNameMap.put(HistoricFormPropertyEntity.class, "ACT_HI_DETAIL");
    persistentObjectToTableNameMap.put(HistoricDetailVariableInstanceUpdateEntity.class, "ACT_HI_DETAIL");
    persistentObjectToTableNameMap.put(HistoricDetailEntity.class, "ACT_HI_DETAIL");
    
    
    // Identity module
    persistentObjectToTableNameMap.put(GroupEntity.class, "ACT_ID_GROUP");
    persistentObjectToTableNameMap.put(MembershipEntity.class, "ACT_ID_MEMBERSHIP");
    persistentObjectToTableNameMap.put(UserEntity.class, "ACT_ID_USER");
    persistentObjectToTableNameMap.put(IdentityInfoEntity.class, "ACT_ID_INFO");
    
    // general
    persistentObjectToTableNameMap.put(PropertyEntity.class, "ACT_GE_PROPERTY");
    persistentObjectToTableNameMap.put(ByteArrayEntity.class, "ACT_GE_BYTEARRAY");
    persistentObjectToTableNameMap.put(ResourceEntity.class, "ACT_GE_BYTEARRAY");
    
    // and now the map for the API types (does not cover all cases)
    apiTypeToTableNameMap.put(Task.class, "ACT_RU_TASK");
    apiTypeToTableNameMap.put(Execution.class, "ACT_RU_EXECUTION");
    apiTypeToTableNameMap.put(ProcessInstance.class, "ACT_RU_EXECUTION");
    apiTypeToTableNameMap.put(ProcessDefinition.class, "ACT_RE_PROCDEF");
    apiTypeToTableNameMap.put(Deployment.class, "ACT_RE_DEPLOYMENT");    
    apiTypeToTableNameMap.put(Job.class, "ACT_RU_JOB");
    
    // history
    apiTypeToTableNameMap.put(HistoricProcessInstance.class, "ACT_HI_PROCINST");
    apiTypeToTableNameMap.put(HistoricActivityInstance.class, "ACT_HI_ACTINST");
    apiTypeToTableNameMap.put(HistoricDetail.class, "ACT_HI_DETAIL");
    apiTypeToTableNameMap.put(HistoricVariableUpdate.class, "ACT_HI_DETAIL");
    apiTypeToTableNameMap.put(HistoricFormProperty.class, "ACT_HI_DETAIL");
    apiTypeToTableNameMap.put(HistoricTaskInstance.class, "ACT_HI_TASKINST");        
    apiTypeToTableNameMap.put(HistoricVariableInstance.class, "ACT_HI_VARINST");
    
    // TODO: Identity skipped for the moment as no SQL injection is provided here
  }

  public Map<String, Long> getTableCount() {
    Map<String, Long> tableCount = new HashMap<String, Long>();
    try {
      for (String tableName: getTablesPresentInDatabase()) {
        tableCount.put(tableName, getTableCount(tableName));
      }
      log.fine("Number of rows per activiti table: "+tableCount);
    } catch (Exception e) {
      throw new ActivitiException("couldn't get table counts", e);
    }
    return tableCount;
  }

  public List<String> getTablesPresentInDatabase() {
    List<String> tableNames = new ArrayList<String>();
    Connection connection = null;
    try {
      connection = getDbSqlSession().getSqlSession().getConnection();
      DatabaseMetaData databaseMetaData = connection.getMetaData();
      ResultSet tables = null;
      try {
        log.fine("retrieving activiti tables from jdbc metadata");
        String databaseTablePrefix = getDbSqlSession().getDbSqlSessionFactory().getDatabaseTablePrefix();
        String tableNameFilter = databaseTablePrefix+"ACT_%";
        if ("postgres".equals(getDbSqlSession().getDbSqlSessionFactory().getDatabaseType())) {
          tableNameFilter = databaseTablePrefix+"act_%";
        }
        tables = databaseMetaData.getTables(null, null, tableNameFilter, getDbSqlSession().JDBC_METADATA_TABLE_TYPES);
        while (tables.next()) {
          String tableName = tables.getString("TABLE_NAME");
          tableName = tableName.toUpperCase();
          tableNames.add(tableName);
          log.fine("  retrieved activiti table name "+tableName);
        }
      } finally {
        tables.close();
      }
    } catch (Exception e) {
      throw new ActivitiException("couldn't get activiti table names using metadata: "+e.getMessage(), e); 
    }
    return tableNames;
  }

  protected long getTableCount(String tableName) {
    log.fine("selecting table count for "+tableName);
    Long count = (Long) getDbSqlSession().selectOne("selectTableCount",
            Collections.singletonMap("tableName", tableName));
    return count;
  }

  @SuppressWarnings("unchecked")
  public TablePage getTablePage(TablePageQueryImpl tablePageQuery, int firstResult, int maxResults) {

    TablePage tablePage = new TablePage();

    @SuppressWarnings("rawtypes")
    List tableData = getDbSqlSession().getSqlSession()
      .selectList("selectTableData", tablePageQuery, new RowBounds(firstResult, maxResults));

    tablePage.setTableName(tablePageQuery.getTableName());
    tablePage.setTotal(getTableCount(tablePageQuery.getTableName()));
    tablePage.setRows((List<Map<String,Object>>)tableData);
    tablePage.setFirstResult(firstResult);
    
    return tablePage;
  }
  
  public String getTableName(Class<?> entityClass, boolean withPrefix) {
    String databaseTablePrefix = getDbSqlSession().getDbSqlSessionFactory().getDatabaseTablePrefix();
    String tableName = null;
    
    if (PersistentObject.class.isAssignableFrom(entityClass)) {
      tableName = persistentObjectToTableNameMap.get(entityClass);
    }
    else {
      tableName = apiTypeToTableNameMap.get(entityClass);
    }
    if (withPrefix) {
      return databaseTablePrefix + tableName;
    }
    else {
      return tableName;
    }
  }

  public TableMetaData getTableMetaData(String tableName) {
    TableMetaData result = new TableMetaData();
    try {
      result.setTableName(tableName);
      DatabaseMetaData metaData = getDbSqlSession()
        .getSqlSession()
        .getConnection()
        .getMetaData();

      if ("postgres".equals(getDbSqlSession().getDbSqlSessionFactory().getDatabaseType())) {
        tableName = tableName.toLowerCase();
      }

      ResultSet resultSet = metaData.getColumns(null, null, tableName, null);
      while(resultSet.next()) {
        String name = resultSet.getString("COLUMN_NAME").toUpperCase();
        String type = resultSet.getString("TYPE_NAME").toUpperCase();
        result.addColumnMetaData(name, type);
      }
      
    } catch (SQLException e) {
      throw new ActivitiException("Could not retrieve database metadata: " + e.getMessage());
    }

    if(result.getColumnNames().size() == 0) {
      // According to API, when a table doesn't exist, null should be returned
      result = null;
    }
    return result;
  }

}

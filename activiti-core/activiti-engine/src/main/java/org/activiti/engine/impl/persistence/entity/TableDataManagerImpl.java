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

import static java.util.Collections.singletonMap;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricDetail;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.history.HistoricVariableUpdate;
import org.activiti.engine.impl.TablePageQueryImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.persistence.AbstractManager;
import org.activiti.engine.management.TableMetaData;
import org.activiti.engine.management.TablePage;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TableDataManagerImpl extends AbstractManager implements TableDataManager {

  public TableDataManagerImpl(ProcessEngineConfigurationImpl processEngineConfiguration) {
    super(processEngineConfiguration);
  }

  private static Logger log = LoggerFactory.getLogger(TableDataManagerImpl.class);

  public static Map<Class<?>, String> apiTypeToTableNameMap = new HashMap<Class<?>, String>();
  public static Map<Class<? extends Entity>, String> entityToTableNameMap = new HashMap<Class<? extends Entity>, String>();

  static {
    // runtime
    entityToTableNameMap.put(TaskEntity.class, "ACT_RU_TASK");
    entityToTableNameMap.put(ExecutionEntity.class, "ACT_RU_EXECUTION");
    entityToTableNameMap.put(IdentityLinkEntity.class, "ACT_RU_IDENTITYLINK");
    entityToTableNameMap.put(VariableInstanceEntity.class, "ACT_RU_VARIABLE");

    entityToTableNameMap.put(JobEntity.class, "ACT_RU_JOB");
    entityToTableNameMap.put(TimerJobEntity.class, "ACT_RU_TIMER_JOB");
    entityToTableNameMap.put(SuspendedJobEntity.class, "ACT_RU_SUSPENDED_JOB");
    entityToTableNameMap.put(DeadLetterJobEntity.class, "ACT_RU_DEADLETTER_JOB");


    entityToTableNameMap.put(EventSubscriptionEntity.class, "ACT_RU_EVENT_SUBSCR");
    entityToTableNameMap.put(CompensateEventSubscriptionEntity.class, "ACT_RU_EVENT_SUBSCR");
    entityToTableNameMap.put(MessageEventSubscriptionEntity.class, "ACT_RU_EVENT_SUBSCR");
    entityToTableNameMap.put(SignalEventSubscriptionEntity.class, "ACT_RU_EVENT_SUBSCR");

    // repository
    entityToTableNameMap.put(DeploymentEntity.class, "ACT_RE_DEPLOYMENT");
    entityToTableNameMap.put(ProcessDefinitionEntity.class, "ACT_RE_PROCDEF");
    entityToTableNameMap.put(ModelEntity.class, "ACT_RE_MODEL");
    entityToTableNameMap.put(ProcessDefinitionInfoEntity.class, "ACT_PROCDEF_INFO");

    // history
    entityToTableNameMap.put(CommentEntity.class, "ACT_HI_COMMENT");

    entityToTableNameMap.put(HistoricActivityInstanceEntity.class, "ACT_HI_ACTINST");
    entityToTableNameMap.put(AttachmentEntity.class, "ACT_HI_ATTACHMENT");
    entityToTableNameMap.put(HistoricProcessInstanceEntity.class, "ACT_HI_PROCINST");
    entityToTableNameMap.put(HistoricVariableInstanceEntity.class, "ACT_HI_VARINST");
    entityToTableNameMap.put(HistoricTaskInstanceEntity.class, "ACT_HI_TASKINST");
    entityToTableNameMap.put(HistoricIdentityLinkEntity.class, "ACT_HI_IDENTITYLINK");

    // a couple of stuff goes to the same table
    entityToTableNameMap.put(HistoricDetailAssignmentEntity.class, "ACT_HI_DETAIL");
    entityToTableNameMap.put(HistoricDetailTransitionInstanceEntity.class, "ACT_HI_DETAIL");
    entityToTableNameMap.put(HistoricDetailVariableInstanceUpdateEntity.class, "ACT_HI_DETAIL");
    entityToTableNameMap.put(HistoricDetailEntity.class, "ACT_HI_DETAIL");

    // general
    entityToTableNameMap.put(PropertyEntity.class, "ACT_GE_PROPERTY");
    entityToTableNameMap.put(ByteArrayEntity.class, "ACT_GE_BYTEARRAY");
    entityToTableNameMap.put(ResourceEntity.class, "ACT_GE_BYTEARRAY");

    entityToTableNameMap.put(EventLogEntryEntity.class, "ACT_EVT_LOG");

    // and now the map for the API types (does not cover all cases)
    apiTypeToTableNameMap.put(Task.class, "ACT_RU_TASK");
    apiTypeToTableNameMap.put(Execution.class, "ACT_RU_EXECUTION");
    apiTypeToTableNameMap.put(ProcessInstance.class, "ACT_RU_EXECUTION");
    apiTypeToTableNameMap.put(ProcessDefinition.class, "ACT_RE_PROCDEF");
    apiTypeToTableNameMap.put(Deployment.class, "ACT_RE_DEPLOYMENT");
    apiTypeToTableNameMap.put(Job.class, "ACT_RU_JOB");
    apiTypeToTableNameMap.put(Model.class, "ACT_RE_MODEL");

    // history
    apiTypeToTableNameMap.put(HistoricProcessInstance.class, "ACT_HI_PROCINST");
    apiTypeToTableNameMap.put(HistoricActivityInstance.class, "ACT_HI_ACTINST");
    apiTypeToTableNameMap.put(HistoricDetail.class, "ACT_HI_DETAIL");
    apiTypeToTableNameMap.put(HistoricVariableUpdate.class, "ACT_HI_DETAIL");
    apiTypeToTableNameMap.put(HistoricTaskInstance.class, "ACT_HI_TASKINST");
    apiTypeToTableNameMap.put(HistoricVariableInstance.class, "ACT_HI_VARINST");


    // TODO: Identity skipped for the moment as no SQL injection is provided
    // here
  }

  protected DbSqlSession getDbSqlSession() {
    return getSession(DbSqlSession.class);
  }

  @Override
  public Map<String, Long> getTableCount() {
    Map<String, Long> tableCount = new HashMap<String, Long>();
    try {
      for (String tableName : getTablesPresentInDatabase()) {
        tableCount.put(tableName, getTableCount(tableName));
      }
      log.debug("Number of rows per activiti table: {}", tableCount);
    } catch (Exception e) {
      throw new ActivitiException("couldn't get table counts", e);
    }
    return tableCount;
  }

  @Override
  public List<String> getTablesPresentInDatabase() {
    List<String> tableNames = new ArrayList<String>();
    Connection connection = null;
    try {
      connection = getDbSqlSession().getSqlSession().getConnection();
      DatabaseMetaData databaseMetaData = connection.getMetaData();
      ResultSet tables = null;
      try {
        log.debug("retrieving activiti tables from jdbc metadata");
        String databaseTablePrefix = getDbSqlSession().getDbSqlSessionFactory().getDatabaseTablePrefix();
        String tableNameFilter = databaseTablePrefix + "ACT_%";
        if ("postgres".equals(getDbSqlSession().getDbSqlSessionFactory().getDatabaseType())) {
          tableNameFilter = databaseTablePrefix + "act_%";
        }
        if ("oracle".equals(getDbSqlSession().getDbSqlSessionFactory().getDatabaseType())) {
          tableNameFilter = databaseTablePrefix + "ACT" + databaseMetaData.getSearchStringEscape() + "_%";
        }

        String catalog = null;
        if (getProcessEngineConfiguration().getDatabaseCatalog() != null && getProcessEngineConfiguration().getDatabaseCatalog().length() > 0) {
          catalog = getProcessEngineConfiguration().getDatabaseCatalog();
        }

        String schema = null;
        if (getProcessEngineConfiguration().getDatabaseSchema() != null && getProcessEngineConfiguration().getDatabaseSchema().length() > 0) {
          if ("oracle".equals(getDbSqlSession().getDbSqlSessionFactory().getDatabaseType())) {
            schema = getProcessEngineConfiguration().getDatabaseSchema().toUpperCase();
          } else {
            schema = getProcessEngineConfiguration().getDatabaseSchema();
          }
        }

        tables = databaseMetaData.getTables(catalog, schema, tableNameFilter, DbSqlSession.JDBC_METADATA_TABLE_TYPES);
        while (tables.next()) {
          String tableName = tables.getString("TABLE_NAME");
          tableName = tableName.toUpperCase();
          tableNames.add(tableName);
          log.debug("  retrieved activiti table name {}", tableName);
        }
      } finally {
        tables.close();
      }
    } catch (Exception e) {
      throw new ActivitiException("couldn't get activiti table names using metadata: " + e.getMessage(), e);
    }
    return tableNames;
  }

  protected long getTableCount(String tableName) {
    log.debug("selecting table count for {}", tableName);
    Long count = (Long) getDbSqlSession().selectOne("selectTableCount", singletonMap("tableName", tableName));
    return count;
  }

  @Override
  @SuppressWarnings("unchecked")
  public TablePage getTablePage(TablePageQueryImpl tablePageQuery, int firstResult, int maxResults) {

    TablePage tablePage = new TablePage();

    @SuppressWarnings("rawtypes")
    List tableData = getDbSqlSession().getSqlSession().selectList("selectTableData", tablePageQuery, new RowBounds(firstResult, maxResults));

    tablePage.setTableName(tablePageQuery.getTableName());
    tablePage.setTotal(getTableCount(tablePageQuery.getTableName()));
    tablePage.setRows((List<Map<String, Object>>) tableData);
    tablePage.setFirstResult(firstResult);

    return tablePage;
  }

  @Override
  public String getTableName(Class<?> entityClass, boolean withPrefix) {
    String databaseTablePrefix = getDbSqlSession().getDbSqlSessionFactory().getDatabaseTablePrefix();
    String tableName = null;

    if (Entity.class.isAssignableFrom(entityClass)) {
      tableName = entityToTableNameMap.get(entityClass);
    } else {
      tableName = apiTypeToTableNameMap.get(entityClass);
    }
    if (withPrefix) {
      return databaseTablePrefix + tableName;
    } else {
      return tableName;
    }
  }

  @Override
  public TableMetaData getTableMetaData(String tableName) {
    TableMetaData result = new TableMetaData();
    try {
      result.setTableName(tableName);
      DatabaseMetaData metaData = getDbSqlSession().getSqlSession().getConnection().getMetaData();

      if ("postgres".equals(getDbSqlSession().getDbSqlSessionFactory().getDatabaseType())) {
        tableName = tableName.toLowerCase();
      }

      String catalog = null;
      if (getProcessEngineConfiguration().getDatabaseCatalog() != null && getProcessEngineConfiguration().getDatabaseCatalog().length() > 0) {
        catalog = getProcessEngineConfiguration().getDatabaseCatalog();
      }

      String schema = null;
      if (getProcessEngineConfiguration().getDatabaseSchema() != null && getProcessEngineConfiguration().getDatabaseSchema().length() > 0) {
        if ("oracle".equals(getDbSqlSession().getDbSqlSessionFactory().getDatabaseType())) {
          schema = getProcessEngineConfiguration().getDatabaseSchema().toUpperCase();
        } else {
          schema = getProcessEngineConfiguration().getDatabaseSchema();
        }
      }

      ResultSet resultSet = metaData.getColumns(catalog, schema, tableName, null);
      while(resultSet.next()) {
        boolean wrongSchema = false;
        if (schema != null && schema.length() > 0) {
          for (int i = 0; i < resultSet.getMetaData().getColumnCount(); i++) {
            String columnName = resultSet.getMetaData().getColumnName(i+1);
            if ("TABLE_SCHEM".equalsIgnoreCase(columnName) || "TABLE_SCHEMA".equalsIgnoreCase(columnName)) {
              if (!schema.equalsIgnoreCase(resultSet.getString(resultSet.getMetaData().getColumnName(i+1)))) {
                wrongSchema = true;
              }
              break;
            }
          }
        }

        if (!wrongSchema) {
          String name = resultSet.getString("COLUMN_NAME").toUpperCase();
          String type = resultSet.getString("TYPE_NAME").toUpperCase();
          result.addColumnMetaData(name, type);
        }
      }

    } catch (SQLException e) {
      throw new ActivitiException("Could not retrieve database metadata: " + e.getMessage());
    }

    if (result.getColumnNames().isEmpty()) {
      // According to API, when a table doesn't exist, null should be returned
      result = null;
    }
    return result;
  }

}

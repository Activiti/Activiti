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

package org.activiti.engine.impl.db;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiOptimisticLockingException;
import org.activiti.engine.SortOrder;
import org.activiti.engine.TableMetaData;
import org.activiti.engine.TablePage;
import org.activiti.engine.impl.AbstractQuery;
import org.activiti.engine.impl.TablePageQueryImpl;
import org.activiti.engine.impl.cfg.ManagementSession;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.repository.PropertyEntity;
import org.apache.ibatis.session.RowBounds;


/**
 * @author Tom Baeyens
 */
public class DbManagementSession implements ManagementSession, Session {
  
  private static Logger log = Logger.getLogger(DbManagementSession.class.getName());

  protected static String[] tableNames = new String[]{
    "ACT_GE_PROPERTY",
    "ACT_GE_BYTEARRAY",
    "ACT_RE_DEPLOYMENT",
    "ACT_RU_EXECUTION",
    "ACT_ID_GROUP",
    "ACT_ID_MEMBERSHIP",
    "ACT_ID_USER",
    "ACT_RU_JOB",
    "ACT_RE_PROC_DEF",
    "ACT_RU_TASK",
    "ACT_RU_TASKINVOLVEMENT",
    "ACT_RU_VARIABLE",
    "ACT_HI_PROC_INST",
    "ACT_HI_ACT_INST"
  };


  protected DbSqlSession dbSqlSession;

  public DbManagementSession() {
    this.dbSqlSession = CommandContext.getCurrentSession(DbSqlSession.class);
  }
  
  public Map<String, Long> getTableCount() {
    Map<String, Long> tableCount = new HashMap<String, Long>();
    try {
      for (String tableName: tableNames) {
        tableCount.put(tableName, getTableCount(tableName));
      }
    } catch (Exception e) {
      throw new ActivitiException("couldn't get table counts", e);
    }
    return tableCount;
  }

  protected long getTableCount(String tableName) {
    log.fine("selecting table count for "+tableName);
    Long count = (Long) dbSqlSession.selectOne("selectTableCount",
            Collections.singletonMap("tableName", tableName));
    return count;
  }

  @SuppressWarnings("unchecked")
 public TablePage getTablePage(TablePageQueryImpl tablePageQuery, int firstResult, int maxResults) {

    TablePage tablePage = new TablePage();

    List<Map<String, Object>> tableData = (List<Map<String, Object>>) dbSqlSession
      .getSqlSession()
      .selectList("selectTableData", tablePageQuery, new RowBounds(firstResult, maxResults));

    tablePage.setTableName(tablePageQuery.getTableName());
    tablePage.setTotal(getTableCount(tablePageQuery.getTableName()));
    tablePage.setRows(tableData);
    tablePage.setFirstResult(firstResult);
    tablePage.setSort(tablePageQuery.getSortColumn());
    String sortOrderString = tablePageQuery.getSortOrder();
    if (AbstractQuery.SORTORDER_ASC.equals(sortOrderString)) {
      tablePage.setOrder(SortOrder.ASC);
    } else if (AbstractQuery.SORTORDER_DESC.equals(sortOrderString)) {
      tablePage.setOrder(SortOrder.DESC);
    }
    
    return tablePage;
  }

  public TableMetaData getTableMetaData(String tableName) {
    TableMetaData result = new TableMetaData();
    try {
      result.setTableName(tableName);
      DatabaseMetaData metaData = dbSqlSession
        .getSqlSession()
        .getConnection()
        .getMetaData();
      ResultSet resultSet = metaData.getColumns(null, null, tableName, null);
      while(resultSet.next()) {
        String name = resultSet.getString("COLUMN_NAME");
        String type = resultSet.getString("TYPE_NAME");
        result.addColumnMetaData(name, type);
      }
    } catch (SQLException e) {
      throw new ActivitiException("Could not retrieve database metadata: " + e.getMessage());
    }

    return result;
  }

  public IdBlock getNextIdBlock() {
    String statement = dbSqlSession.getDbSqlSessionFactory().mapStatement("selectProperty");
    PropertyEntity property = (PropertyEntity) dbSqlSession.selectOne(statement, "next.dbid");
    long oldValue = Long.parseLong(property.getValue());
    
    long idBlockSize = CommandContext
      .getCurrent()
      .getProcessEngineConfiguration()
      .getIdBlockSize();
    
    long newValue = oldValue+idBlockSize;
    Map<String, Object> updateValues = new HashMap<String, Object>();
    updateValues.put("name", property.getName());
    updateValues.put("revision", property.getDbversion());
    updateValues.put("newRevision", property.getDbversion()+1);
    updateValues.put("value", Long.toString(newValue));
    int rowsUpdated = dbSqlSession.getSqlSession().update("updateProperty", updateValues);
    if (rowsUpdated!=1) {
      throw new ActivitiOptimisticLockingException("couldn't get next block of dbids");
    }
    return new IdBlock(oldValue, newValue-1);
  }

  public void close() {
  }

  public void flush() {
  }
}

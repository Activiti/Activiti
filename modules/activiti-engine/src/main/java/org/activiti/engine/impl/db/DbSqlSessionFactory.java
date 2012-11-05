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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.activiti.engine.impl.cfg.IdGenerator;
import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.interceptor.SessionFactory;
import org.activiti.engine.impl.util.ClassNameUtil;
import org.apache.ibatis.session.SqlSessionFactory;


/**
 * @author Tom Baeyens
 */
public class DbSqlSessionFactory implements SessionFactory {

  protected static final Map<String, Map<String, String>> databaseSpecificStatements = new HashMap<String, Map<String,String>>();
  
  public static final Map<String, String> databaseSpecificLimitBeforeStatements = new HashMap<String, String>();
  public static final Map<String, String> databaseSpecificLimitAfterStatements = new HashMap<String, String>();
  public static final Map<String, String> databaseSpecificLimitBetweenStatements = new HashMap<String, String>();
  public static final Map<String, String> databaseSpecificOrderByStatements = new HashMap<String, String>();

  static {
    
    String defaultOrderBy = " order by ${orderBy} ";
    
    // h2
    databaseSpecificLimitBeforeStatements.put("h2", "");
    databaseSpecificLimitAfterStatements.put("h2", "LIMIT #{maxResults} OFFSET #{firstResult}");
    databaseSpecificLimitBetweenStatements.put("h2", "");
    databaseSpecificOrderByStatements.put("h2", defaultOrderBy);
    
	  //mysql specific
    databaseSpecificLimitBeforeStatements.put("mysql", "");
    databaseSpecificLimitAfterStatements.put("mysql", "LIMIT #{maxResults} OFFSET #{firstResult}");
    databaseSpecificLimitBetweenStatements.put("mysql", "");
    databaseSpecificOrderByStatements.put("mysql", defaultOrderBy);
    addDatabaseSpecificStatement("mysql", "selectNextJobsToExecute", "selectNextJobsToExecute_mysql");
    addDatabaseSpecificStatement("mysql", "selectExclusiveJobsToExecute", "selectExclusiveJobsToExecute_mysql");
    addDatabaseSpecificStatement("mysql", "selectProcessDefinitionsByQueryCriteria", "selectProcessDefinitionsByQueryCriteria_mysql");
    addDatabaseSpecificStatement("mysql", "selectProcessDefinitionCountByQueryCriteria", "selectProcessDefinitionCountByQueryCriteria_mysql");
    addDatabaseSpecificStatement("mysql", "selectDeploymentsByQueryCriteria", "selectDeploymentsByQueryCriteria_mysql");
    addDatabaseSpecificStatement("mysql", "selectDeploymentCountByQueryCriteria", "selectDeploymentCountByQueryCriteria_mysql");
    
    //postgres specific
    databaseSpecificLimitBeforeStatements.put("postgres", "");
    databaseSpecificLimitAfterStatements.put("postgres", "LIMIT #{maxResults} OFFSET #{firstResult}");
    databaseSpecificLimitBetweenStatements.put("postgres", "");
    databaseSpecificOrderByStatements.put("postgres", defaultOrderBy);
    addDatabaseSpecificStatement("postgres", "insertByteArray", "insertByteArray_postgres");
    addDatabaseSpecificStatement("postgres", "updateByteArray", "updateByteArray_postgres");
    addDatabaseSpecificStatement("postgres", "selectByteArray", "selectByteArray_postgres");
    addDatabaseSpecificStatement("postgres", "selectResourceByDeploymentIdAndResourceName", "selectResourceByDeploymentIdAndResourceName_postgres");
    addDatabaseSpecificStatement("postgres", "selectResourcesByDeploymentId", "selectResourcesByDeploymentId_postgres");
    addDatabaseSpecificStatement("postgres", "selectHistoricDetailsByQueryCriteria", "selectHistoricDetailsByQueryCriteria_postgres");
    addDatabaseSpecificStatement("postgres", "insertIdentityInfo", "insertIdentityInfo_postgres");
    addDatabaseSpecificStatement("postgres", "updateIdentityInfo", "updateIdentityInfo_postgres");
    addDatabaseSpecificStatement("postgres", "selectIdentityInfoById", "selectIdentityInfoById_postgres");
    addDatabaseSpecificStatement("postgres", "selectIdentityInfoByUserIdAndKey", "selectIdentityInfoByUserIdAndKey_postgres");
    addDatabaseSpecificStatement("postgres", "selectIdentityInfoByUserId", "selectIdentityInfoByUserId_postgres");
    addDatabaseSpecificStatement("postgres", "selectIdentityInfoDetails", "selectIdentityInfoDetails_postgres");
    addDatabaseSpecificStatement("postgres", "insertComment", "insertComment_postgres");
    addDatabaseSpecificStatement("postgres", "selectCommentsByTaskId", "selectCommentsByTaskId_postgres");
    addDatabaseSpecificStatement("postgres", "selectCommentsByProcessInstanceId", "selectCommentsByProcessInstanceId_postgres");
    addDatabaseSpecificStatement("postgres", "selectEventsByTaskId", "selectEventsByTaskId_postgres");
        
    // oracle
    databaseSpecificLimitBeforeStatements.put("oracle", "select * from ( select a.*, ROWNUM rnum from (");
    databaseSpecificLimitAfterStatements.put("oracle", "  ) a where ROWNUM < #{lastRow}) where rnum  >= #{firstRow}");
    databaseSpecificLimitBetweenStatements.put("oracle", "");
    databaseSpecificOrderByStatements.put("oracle", defaultOrderBy);
    addDatabaseSpecificStatement("oracle", "selectExclusiveJobsToExecute", "selectExclusiveJobsToExecute_integerBoolean");
    
    // db2
    databaseSpecificLimitBeforeStatements.put("db2", "SELECT SUB.* FROM (");
    databaseSpecificLimitAfterStatements.put("db2", ")RES ) SUB WHERE SUB.rnk >= #{firstRow} AND SUB.rnk < #{lastRow}");
    databaseSpecificLimitBetweenStatements.put("db2", ", row_number() over (ORDER BY ${orderBy}) rnk FROM ( select distinct RES.* ");
    databaseSpecificOrderByStatements.put("db2", "");
    addDatabaseSpecificStatement("db2", "selectExclusiveJobsToExecute", "selectExclusiveJobsToExecute_integerBoolean");
    
    // mssql
    databaseSpecificLimitBeforeStatements.put("mssql", "SELECT SUB.* FROM (");
    databaseSpecificLimitAfterStatements.put("mssql", ")RES ) SUB WHERE SUB.rnk >= #{firstRow} AND SUB.rnk < #{lastRow}");
    databaseSpecificLimitBetweenStatements.put("mssql", ", row_number() over (ORDER BY ${orderBy}) rnk FROM ( select distinct RES.* ");
    databaseSpecificOrderByStatements.put("mssql", "");
    addDatabaseSpecificStatement("mssql", "selectExclusiveJobsToExecute", "selectExclusiveJobsToExecute_integerBoolean");
  }
  
  protected String databaseType;
  protected String databaseTablePrefix = "";
  /**
   * In some situations you want to set the schema to use for table checks /
   * generation if the database metadata doesn't return that correctly, see
   * https://jira.codehaus.org/browse/ACT-1220,
   * https://jira.codehaus.org/browse/ACT-1062
   */
  protected String databaseSchema;
  protected SqlSessionFactory sqlSessionFactory;
  protected IdGenerator idGenerator;
  protected Map<String, String> statementMappings;
  protected Map<Class<?>,String>  insertStatements = new ConcurrentHashMap<Class<?>, String>();
  protected Map<Class<?>,String>  updateStatements = new ConcurrentHashMap<Class<?>, String>();
  protected Map<Class<?>,String>  deleteStatements = new ConcurrentHashMap<Class<?>, String>();
  protected Map<Class<?>,String>  selectStatements = new ConcurrentHashMap<Class<?>, String>();
  protected boolean isDbIdentityUsed = true;
  protected boolean isDbHistoryUsed = true;

  public Class< ? > getSessionType() {
    return DbSqlSession.class;
  }

  public Session openSession() {
    return new DbSqlSession(this);
  }
  
  // insert, update and delete statements /////////////////////////////////////
  
  public String getInsertStatement(PersistentObject object) {
    return getStatement(object.getClass(), insertStatements, "insert");
  }

  public String getUpdateStatement(PersistentObject object) {
    return getStatement(object.getClass(), updateStatements, "update");
  }

  public String getDeleteStatement(Class<?> persistentObjectClass) {
    return getStatement(persistentObjectClass, deleteStatements, "delete");
  }

  public String getSelectStatement(Class<?> persistentObjectClass) {
    return getStatement(persistentObjectClass, selectStatements, "select");
  }

  private String getStatement(Class<?> persistentObjectClass, Map<Class<?>,String> cachedStatements, String prefix) {
    String statement = cachedStatements.get(persistentObjectClass);
    if (statement!=null) {
      return statement;
    }
    statement = prefix+ClassNameUtil.getClassNameWithoutPackage(persistentObjectClass);
    statement = statement.substring(0, statement.length()-6);
    cachedStatements.put(persistentObjectClass, statement);
    return statement;
  }

  // db specific mappings /////////////////////////////////////////////////////
  
  protected static void addDatabaseSpecificStatement(String databaseType, String activitiStatement, String ibatisStatement) {
    Map<String, String> specificStatements = databaseSpecificStatements.get(databaseType);
    if (specificStatements == null) {
      specificStatements = new HashMap<String, String>();
      databaseSpecificStatements.put(databaseType, specificStatements);
    }
    specificStatements.put(activitiStatement, ibatisStatement);
  }
  
  public String mapStatement(String statement) {
    if (statementMappings==null) {
      return statement;
    }
    String mappedStatement = statementMappings.get(statement);
    return (mappedStatement!=null ? mappedStatement : statement);
  }
  
  // customized getters and setters ///////////////////////////////////////////
  
  public void setDatabaseType(String databaseType) {
    this.databaseType = databaseType;
    this.statementMappings = databaseSpecificStatements.get(databaseType);
  }

  // getters and setters //////////////////////////////////////////////////////
  
  public SqlSessionFactory getSqlSessionFactory() {
    return sqlSessionFactory;
  }
  
  public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
    this.sqlSessionFactory = sqlSessionFactory;
  }
  
  public IdGenerator getIdGenerator() {
    return idGenerator;
  }
  
  public void setIdGenerator(IdGenerator idGenerator) {
    this.idGenerator = idGenerator;
  }

  
  public String getDatabaseType() {
    return databaseType;
  }

  
  public Map<String, String> getStatementMappings() {
    return statementMappings;
  }

  
  public void setStatementMappings(Map<String, String> statementMappings) {
    this.statementMappings = statementMappings;
  }

  
  public Map<Class< ? >, String> getInsertStatements() {
    return insertStatements;
  }

  
  public void setInsertStatements(Map<Class< ? >, String> insertStatements) {
    this.insertStatements = insertStatements;
  }

  
  public Map<Class< ? >, String> getUpdateStatements() {
    return updateStatements;
  }

  
  public void setUpdateStatements(Map<Class< ? >, String> updateStatements) {
    this.updateStatements = updateStatements;
  }

  
  public Map<Class< ? >, String> getDeleteStatements() {
    return deleteStatements;
  }

  
  public void setDeleteStatements(Map<Class< ? >, String> deleteStatements) {
    this.deleteStatements = deleteStatements;
  }

  
  public Map<Class< ? >, String> getSelectStatements() {
    return selectStatements;
  }

  
  public void setSelectStatements(Map<Class< ? >, String> selectStatements) {
    this.selectStatements = selectStatements;
  }

  public boolean isDbIdentityUsed() {
    return isDbIdentityUsed;
  }
  
  public void setDbIdentityUsed(boolean isDbIdentityUsed) {
    this.isDbIdentityUsed = isDbIdentityUsed;
  }
  
  public boolean isDbHistoryUsed() {
    return isDbHistoryUsed;
  }
  
  public void setDbHistoryUsed(boolean isDbHistoryUsed) {
    this.isDbHistoryUsed = isDbHistoryUsed;
  }

  public void setDatabaseTablePrefix(String databaseTablePrefix) {
    this.databaseTablePrefix = databaseTablePrefix;
  }
    
  public String getDatabaseTablePrefix() {
    return databaseTablePrefix;
  }
  
  public String getDatabaseSchema() {
    return databaseSchema;
  }
  
  public void setDatabaseSchema(String databaseSchema) {
    this.databaseSchema = databaseSchema;
  }

}

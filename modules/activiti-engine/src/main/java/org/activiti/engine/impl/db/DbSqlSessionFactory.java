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
import org.apache.ibatis.session.SqlSessionFactory;


/**
 * @author Tom Baeyens
 */
public class DbSqlSessionFactory implements SessionFactory {

  protected static final Map<String, Map<String, String>> databaseSpecificStatements = new HashMap<String, Map<String,String>>();
  
  protected String databaseType;
  protected String databaseTablePrefix = "";
  private boolean tablePrefixIsSchema;

  protected String databaseCatalog;
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
  protected Map<Class<?>,String>  bulkDeleteStatements = new ConcurrentHashMap<Class<?>, String>();
  protected Map<Class<?>,String>  selectStatements = new ConcurrentHashMap<Class<?>, String>();
  protected boolean isDbIdentityUsed = true;
  protected boolean isDbHistoryUsed = true;
  protected boolean isOptimizeDeleteOperationsEnabled;


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
  
  public String getBulkDeleteStatement(Class<?> persistentObjectClass) {
    return getStatement(persistentObjectClass, bulkDeleteStatements, "bulkDelete");
  }

  public String getSelectStatement(Class<?> persistentObjectClass) {
    return getStatement(persistentObjectClass, selectStatements, "select");
  }

  private String getStatement(Class<?> persistentObjectClass, Map<Class<?>,String> cachedStatements, String prefix) {
    String statement = cachedStatements.get(persistentObjectClass);
    if (statement!=null) {
      return statement;
    }
    statement = prefix + persistentObjectClass.getSimpleName();
    statement = statement.substring(0, statement.length()-6); // removing 'entity'
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
  
  
  public Map<Class<?>, String> getBulkDeleteStatements() {
		return bulkDeleteStatements;
	}

	public void setBulkDeleteStatements(Map<Class<?>, String> bulkDeleteStatements) {
		this.bulkDeleteStatements = bulkDeleteStatements;
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

  public String getDatabaseCatalog() {
    return databaseCatalog;
  }

  public void setDatabaseCatalog(String databaseCatalog) {
    this.databaseCatalog = databaseCatalog;
  }

  public String getDatabaseSchema() {
    return databaseSchema;
  }
  
  public void setDatabaseSchema(String databaseSchema) {
    this.databaseSchema = databaseSchema;
  }

	public void setTablePrefixIsSchema(boolean tablePrefixIsSchema) {
		this.tablePrefixIsSchema = tablePrefixIsSchema;
  }
	
	public boolean isTablePrefixIsSchema() {
	  return tablePrefixIsSchema;
  }

	public boolean isOptimizeDeleteOperationsEnabled() {
		return isOptimizeDeleteOperationsEnabled;
	}

	public void setOptimizeDeleteOperationsEnabled(boolean isOptimizeDeleteOperationsEnabled) {
		this.isOptimizeDeleteOperationsEnabled = isOptimizeDeleteOperationsEnabled;
	}
	
}

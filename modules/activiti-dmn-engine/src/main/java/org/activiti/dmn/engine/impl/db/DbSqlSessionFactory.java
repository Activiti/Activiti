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

package org.activiti.dmn.engine.impl.db;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.activiti.dmn.engine.ActivitiDmnException;
import org.activiti.dmn.engine.impl.cfg.IdGenerator;
import org.activiti.dmn.engine.impl.interceptor.CommandContext;
import org.activiti.dmn.engine.impl.interceptor.Session;
import org.activiti.dmn.engine.impl.interceptor.SessionFactory;
import org.apache.ibatis.session.SqlSessionFactory;

/**
 * @author Tijs Rademakers
 * @author Joram Barrez
 */
public class DbSqlSessionFactory implements SessionFactory {

  protected static final Map<String, Map<String, String>> databaseSpecificStatements = new HashMap<String, Map<String, String>>();
  
  protected String databaseType;
  protected String databaseTablePrefix = "";
  private boolean tablePrefixIsSchema;

  protected String databaseCatalog;
  /**
   * In some situations you want to set the schema to use for table checks /
   * generation if the database metadata doesn't return that correctly, see
   * https://activiti.atlassian.net/browse/ACT-1220,
   * https://activiti.atlassian.net/browse/ACT-1062
   */
  protected String databaseSchema;
  protected SqlSessionFactory sqlSessionFactory;
  protected IdGenerator idGenerator;
  protected Map<String, String> statementMappings;
  protected Map<Class<?>,String> insertStatements = new ConcurrentHashMap<Class<?>, String>();
  protected Map<Class<?>,String> updateStatements = new ConcurrentHashMap<Class<?>, String>();
  protected Map<Class<?>,String> deleteStatements = new ConcurrentHashMap<Class<?>, String>();
  protected Map<Class<?>,String> selectStatements = new ConcurrentHashMap<Class<?>, String>();
  
  public Class<?> getSessionType() {
    return DbSqlSession.class;
  }

  public Session openSession(CommandContext commandContext) {
    DbSqlSession dbSqlSession = new DbSqlSession(this);
    if (getDatabaseSchema() != null && getDatabaseSchema().length() > 0) {
      try {
        dbSqlSession.getSqlSession().getConnection().setSchema(getDatabaseSchema());
      } catch (SQLException e) {
        throw new ActivitiDmnException("Could not set database schema on connection", e);
      }
    }
    if (getDatabaseCatalog() != null && getDatabaseCatalog().length() > 0) {
      try {
        dbSqlSession.getSqlSession().getConnection().setCatalog(getDatabaseCatalog());
      } catch (SQLException e) {
        throw new ActivitiDmnException("Could not set database catalog on connection", e);
      }
    }
    return dbSqlSession;
  }

  // insert, update and delete statements
  // /////////////////////////////////////

  public String getInsertStatement(Entity object) {
    return getStatement(object.getClass(), insertStatements, "insert");
  }
  
  
  public String getInsertStatement(Class<? extends Entity> clazz) {
    return getStatement(clazz, insertStatements, "insert");
  }

  public String getUpdateStatement(Entity object) {
    return getStatement(object.getClass(), updateStatements, "update");
  }

  public String getDeleteStatement(Class<?> entityClass) {
    return getStatement(entityClass, deleteStatements, "delete");
  }

  public String getSelectStatement(Class<?> entityClass) {
    return getStatement(entityClass, selectStatements, "select");
  }

  private String getStatement(Class<?> entityClass, Map<Class<?>, String> cachedStatements, String prefix) {
    String statement = cachedStatements.get(entityClass);
    if (statement != null) {
      return statement;
    }
    statement = prefix + entityClass.getSimpleName();
    if (statement.endsWith("Impl")) {
      statement = statement.substring(0, statement.length() - 10); // removing 'entityImpl'
    } else {
      statement = statement.substring(0, statement.length() - 6); // removing 'entity'
    }
    cachedStatements.put(entityClass, statement);
    return statement;
  }

  // db specific mappings
  // /////////////////////////////////////////////////////

  protected static void addDatabaseSpecificStatement(String databaseType, String activitiStatement, String ibatisStatement) {
    Map<String, String> specificStatements = databaseSpecificStatements.get(databaseType);
    if (specificStatements == null) {
      specificStatements = new HashMap<String, String>();
      databaseSpecificStatements.put(databaseType, specificStatements);
    }
    specificStatements.put(activitiStatement, ibatisStatement);
  }

  public String mapStatement(String statement) {
    if (statementMappings == null) {
      return statement;
    }
    String mappedStatement = statementMappings.get(statement);
    return (mappedStatement != null ? mappedStatement : statement);
  }

  // customized getters and setters
  // ///////////////////////////////////////////

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

  public Map<Class<?>, String> getInsertStatements() {
    return insertStatements;
  }

  public void setInsertStatements(Map<Class<?>, String> insertStatements) {
    this.insertStatements = insertStatements;
  }

  public Map<Class< ? >, String> getUpdateStatements() {
    return updateStatements;
  }

  public void setUpdateStatements(Map<Class<?>, String> updateStatements) {
    this.updateStatements = updateStatements;
  }

  public Map<Class<?>, String> getDeleteStatements() {
    return deleteStatements;
  }

  public void setDeleteStatements(Map<Class<?>, String> deleteStatements) {
    this.deleteStatements = deleteStatements;
  }

  public Map<Class<?>, String> getSelectStatements() {
    return selectStatements;
  }

  public void setSelectStatements(Map<Class<?>, String> selectStatements) {
    this.selectStatements = selectStatements;
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
}

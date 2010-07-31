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

package org.activiti.engine.impl.persistence.db;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiWrongDbException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.cfg.IdGenerator;
import org.activiti.engine.impl.cfg.ProcessEngineConfiguration;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationAware;
import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.interceptor.SessionFactory;
import org.activiti.engine.impl.persistence.PersistentObject;
import org.activiti.engine.impl.util.ClassNameUtil;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.impl.persistence.LoadedObject;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;


/**
 * @author Tom Baeyens
 */
public class DbSqlSessionFactory implements SessionFactory, ProcessEngineConfigurationAware {

  private static Logger log = Logger.getLogger(DbSqlSessionFactory.class.getName());
  protected static final Map<String, Map<String, String>> databaseSpecificStatements = new HashMap<String, Map<String,String>>();

  protected Map<Object, LoadedObject> loadedObjects = new HashMap<Object, LoadedObject>();

  static {
    addDatabaseSpecificStatement("mysql", "selectTaskByDynamicCriteria", "selectTaskByDynamicCriteria_mysql");
    addDatabaseSpecificStatement("mysql", "selectNextJobsToExecute", "selectNextJobsToExecute_mysql");
  }
  
  protected String databaseName;
  protected SqlSessionFactory sqlSessionFactory;
  protected IdGenerator idGenerator;
  protected Map<String, String> statementMappings;
  protected Map<Class<?>,String>  insertStatements = Collections.synchronizedMap(new HashMap<Class<?>, String>());
  protected Map<Class<?>,String>  updateStatements = Collections.synchronizedMap(new HashMap<Class<?>, String>());
  protected Map<Class<?>,String>  deleteStatements = Collections.synchronizedMap(new HashMap<Class<?>, String>());
  
  public void configurationCompleted(ProcessEngineConfiguration processEngineConfiguration) {
    this.databaseName = processEngineConfiguration.getDatabaseName();
    this.sqlSessionFactory = ((DbRuntimeSessionFactory)processEngineConfiguration.getPersistenceSessionFactory()).getSqlSessionFactory();
    this.idGenerator = processEngineConfiguration.getIdGenerator();
    this.statementMappings = databaseSpecificStatements.get(processEngineConfiguration.getDatabaseName());
  }

  public Session openSession() {
    return new DbSqlSession(this);
  }
  
  // insert, update and delete statements /////////////////////////////////////
  
  public String getInsertStatement(PersistentObject object) {
    return getStatement(object, insertStatements, "insert");
  }

  public String getUpdateStatement(PersistentObject object) {
    return getStatement(object, updateStatements, "update");
  }

  public String getDeleteStatement(PersistentObject object) {
    return getStatement(object, deleteStatements, "delete");
  }

  private String getStatement(PersistentObject object, Map<Class<?>,String> cachedStatements, String prefix) {
    Class< ? extends PersistentObject> persistentObjectClass = object.getClass();
    String statement = cachedStatements.get(persistentObjectClass);
    if (statement!=null) {
      return statement;
    }
    statement = prefix+ClassNameUtil.getClassNameWithoutPackage(object);
    cachedStatements.put(persistentObjectClass, statement);
    return statement;
  }
  
  // db specific mappings /////////////////////////////////////////////////////
  
  protected static void addDatabaseSpecificStatement(String databaseName, String activitiStatement, String ibatisStatement) {
    Map<String, String> specificStatements = databaseSpecificStatements.get(databaseName);
    if (specificStatements == null) {
      specificStatements = new HashMap<String, String>();
      databaseSpecificStatements.put(databaseName, specificStatements);
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
  
  // db operations ////////////////////////////////////////////////////////////
  
  public void dbSchemaCheckVersion() {
    /*
     * Not quite sure if this is the right setting? We do want multiple updates
     * to be batched for performance ...
     */
    SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
    boolean success = false;
    try {
      String selectSchemaVersionStatement = mapStatement("selectDbSchemaVersion");
      String dbVersion = (String) sqlSession.selectOne(selectSchemaVersionStatement);
      if (!ProcessEngine.VERSION.equals(dbVersion)) {
        throw new ActivitiWrongDbException(ProcessEngine.VERSION, dbVersion);
      }

      success = true;

    } catch (Exception e) {
      String exceptionMessage = e.getMessage();
      if ((exceptionMessage.indexOf("Table") != -1) && (exceptionMessage.indexOf("not found") != -1)) {
        throw new ActivitiException(
                "no activiti tables in db.  set property db.schema.strategy=create-drop in activiti.properties for automatic schema creation", e);
      } else {
        if (e instanceof RuntimeException) {
          throw (RuntimeException) e;
        } else {
          throw new ActivitiException("couldn't get db schema version", e);
        }
      }
    } finally {
      if (success) {
        sqlSession.commit(true);
      } else {
        sqlSession.rollback(true);
      }
      sqlSession.close();
    }

    log.fine("activiti db schema check successful");
  }

  public void dbSchemaCreate() {
    executeSchemaResource("create", databaseName, sqlSessionFactory);
  }

  public void dbSchemaDrop() {
    executeSchemaResource("drop", databaseName, sqlSessionFactory);
  }

  public static void executeSchemaResource(String operation, String databaseName, SqlSessionFactory sqlSessionFactory) {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    boolean success = false;
    try {
      Connection connection = sqlSession.getConnection();
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      String resource = "org/activiti/db/" + operation + "/activiti." + databaseName + "." + operation + ".sql";
      InputStream inputStream = classLoader.getResourceAsStream(resource);
      if (inputStream == null) {
        throw new ActivitiException("resource '" + resource + "' is not available for creating the schema");
      }

      Exception exception = null;
      byte[] bytes = IoUtil.readInputStream(inputStream, resource);
      String ddlStatements = new String(bytes);
      StringTokenizer tokenizer = new StringTokenizer(ddlStatements, ";");
      while (tokenizer.hasMoreTokens()) {
        String ddlStatement = tokenizer.nextToken().trim();
        if (!ddlStatement.startsWith("#")) {
          Statement jdbcStatement = connection.createStatement();
          try {
            log.fine("\n" + ddlStatement);
            jdbcStatement.execute(ddlStatement);
            jdbcStatement.close();
          } catch (Exception e) {
            if (exception == null) {
              exception = e;
            }
            log.log(Level.SEVERE, "problem during schema " + operation + ", statement '" + ddlStatement, e);
          }
        }
      }

      if (exception != null) {
        throw exception;
      }

      success = true;

    } catch (Exception e) {
      throw new ActivitiException("couldn't create db schema", e);

    } finally {
      if (success) {
        sqlSession.commit(true);
      } else {
        sqlSession.rollback(true);
      }
      sqlSession.close();
    }

    log.fine("activiti db schema " + operation + " successful");
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
  
  public Map<Object, LoadedObject> getLoadedObjects() {
    return loadedObjects;
  }
  
  public void setLoadedObjects(Map<Object, LoadedObject> loadedObjects) {
    this.loadedObjects = loadedObjects;
  }
}

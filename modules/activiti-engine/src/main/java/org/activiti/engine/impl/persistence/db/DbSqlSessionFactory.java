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

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.persistence.repository.DeploymentEntity;
import org.activiti.engine.impl.persistence.repository.ResourceEntity;
import org.activiti.impl.bytes.ByteArrayImpl;
import org.activiti.impl.cfg.ProcessEngineConfiguration;
import org.activiti.impl.cfg.ProcessEngineConfigurationAware;
import org.activiti.impl.db.IdGenerator;
import org.activiti.impl.db.execution.DbExecutionImpl;
import org.activiti.impl.interceptor.SessionFactory;
import org.activiti.impl.job.JobImpl;
import org.activiti.impl.job.MessageImpl;
import org.activiti.impl.job.TimerImpl;
import org.activiti.impl.persistence.IbatisPersistenceSessionFactory;
import org.activiti.impl.persistence.LoadedObject;
import org.activiti.impl.task.TaskImpl;
import org.activiti.impl.task.TaskInvolvement;
import org.activiti.impl.tx.Session;
import org.activiti.impl.variable.VariableInstance;
import org.apache.ibatis.session.SqlSessionFactory;


/**
 * @author Tom Baeyens
 */
public class DbSqlSessionFactory implements SessionFactory, ProcessEngineConfigurationAware {

  public static final Map<Class<?>,String> DEFAULT_INSERT_STATEMENTS = new HashMap<Class<?>, String>();
  public static final Map<Class<?>,String> DEFAULT_UPDATE_STATEMENTS = new HashMap<Class<?>, String>();
  public static final Map<Class<?>,String> DEFAULT_DELETE_STATEMENTS = new HashMap<Class<?>, String>();
  protected static final Map<String, Map<String, String>> databaseSpecificStatements = new HashMap<String, Map<String,String>>();

  protected Map<Object, LoadedObject> loadedObjects = new HashMap<Object, LoadedObject>();

  static {
    DEFAULT_INSERT_STATEMENTS.put(DeploymentEntity.class, "insertDeployment");
    DEFAULT_INSERT_STATEMENTS.put(ResourceEntity.class, "insertResource");
    DEFAULT_INSERT_STATEMENTS.put(DbExecutionImpl.class, "insertExecution");
    DEFAULT_INSERT_STATEMENTS.put(JobImpl.class, "insertJob");
    DEFAULT_INSERT_STATEMENTS.put(TaskImpl.class, "insertTask");
    DEFAULT_INSERT_STATEMENTS.put(TaskInvolvement.class, "insertTaskInvolvement");
    DEFAULT_INSERT_STATEMENTS.put(VariableInstance.class, "insertVariableInstance");
    DEFAULT_INSERT_STATEMENTS.put(ByteArrayImpl.class, "insertByteArray");
    DEFAULT_INSERT_STATEMENTS.put(MessageImpl.class, "insertMessage");
    DEFAULT_INSERT_STATEMENTS.put(TimerImpl.class, "insertTimer");

    DEFAULT_UPDATE_STATEMENTS.put(DbExecutionImpl.class, "updateExecution");
    DEFAULT_UPDATE_STATEMENTS.put(TaskImpl.class, "updateTask");
    DEFAULT_UPDATE_STATEMENTS.put(TaskInvolvement.class, "updateTaskInvolvement");
    DEFAULT_UPDATE_STATEMENTS.put(VariableInstance.class, "updateVariableInstance");
    DEFAULT_UPDATE_STATEMENTS.put(ByteArrayImpl.class, "updateByteArray");
    DEFAULT_UPDATE_STATEMENTS.put(MessageImpl.class, "updateMessage");
    DEFAULT_UPDATE_STATEMENTS.put(TimerImpl.class, "updateTimer");

    DEFAULT_DELETE_STATEMENTS.put(DeploymentEntity.class, "deleteDeployment");
    DEFAULT_DELETE_STATEMENTS.put(DbExecutionImpl.class, "deleteExecution");
    DEFAULT_DELETE_STATEMENTS.put(TaskImpl.class, "deleteTask");
    DEFAULT_DELETE_STATEMENTS.put(TaskInvolvement.class, "deleteTaskInvolvement");
    DEFAULT_DELETE_STATEMENTS.put(VariableInstance.class, "deleteVariableInstance");
    DEFAULT_DELETE_STATEMENTS.put(ByteArrayImpl.class, "deleteByteArray");
    DEFAULT_DELETE_STATEMENTS.put(MessageImpl.class, "deleteJob");
    DEFAULT_DELETE_STATEMENTS.put(TimerImpl.class, "deleteJob");
    
    addDatabaseSpecificStatement("mysql", "selectTaskByDynamicCriteria", "selectTaskByDynamicCriteria_mysql");
    addDatabaseSpecificStatement("mysql", "selectNextJobsToExecute", "selectNextJobsToExecute_mysql");
  }
  
  protected SqlSessionFactory sqlSessionFactory;
  protected IdGenerator idGenerator;
  protected Map<String, String> statementMappings;
  protected Map<Class<?>,String>  insertStatements = DEFAULT_INSERT_STATEMENTS;
  protected Map<Class<?>,String>  updateStatements = DEFAULT_UPDATE_STATEMENTS;
  protected Map<Class<?>,String>  deleteStatements = DEFAULT_DELETE_STATEMENTS;
  
  public void configurationCompleted(ProcessEngineConfiguration processEngineConfiguration) {
    this.sqlSessionFactory = ((IbatisPersistenceSessionFactory)processEngineConfiguration.getPersistenceSessionFactory()).getSqlSessionFactory();
    this.idGenerator = processEngineConfiguration.getIdGenerator();
    this.statementMappings = databaseSpecificStatements.get(processEngineConfiguration.getDatabaseName());
  }

  public Session openSession() {
    return new DbSqlSession(this);
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

  // getters and setters //////////////////////////////////////////////////////
  
  public SqlSessionFactory getSqlSessionFactory() {
    return sqlSessionFactory;
  }
  
  public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
    this.sqlSessionFactory = sqlSessionFactory;
  }
  
  public Map<Class< ? >, String> getInsertStatements() {
    return insertStatements;
  }
  
  public void setInsertStatements(Map<Class< ? >, String> insertStatements) {
    this.insertStatements = insertStatements;
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
}

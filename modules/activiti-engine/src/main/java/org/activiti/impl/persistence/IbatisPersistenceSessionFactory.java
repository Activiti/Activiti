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
package org.activiti.impl.persistence;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.activiti.ActivitiException;
import org.activiti.ActivitiWrongDbException;
import org.activiti.ProcessEngine;
import org.activiti.impl.db.IdGenerator;
import org.activiti.impl.interceptor.CommandContext;
import org.activiti.impl.util.IoUtil;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.ibatis.transaction.managed.ManagedTransactionFactory;

/**
 * @author Tom Baeyens
 */
public class IbatisPersistenceSessionFactory implements PersistenceSessionFactory {

  private static Logger log = Logger.getLogger(IbatisPersistenceSessionFactory.class.getName());

  protected static List<String> statements = new ArrayList<String>();
  protected static Map<String, Map<String, String>> databaseSpecificStatements = null;
  static {
    // Default statement ids
    statements.add("selectExecution");
    statements.add("selectDbSchemaVersion");
    statements.add("selectRootExecutionsForProcessDefinition");
    statements.add("selectChildExecutions");
    statements.add("selectProcessInstanceCountByDynamicCriteria");
    statements.add("selectProcessInstanceByDynamicCriteria");
    statements.add("selectVariablesByExecutionId");
    statements.add("selectVariablesByTaskId");
    statements.add("selectTask");
    statements.add("selectTaskByExecution");
    statements.add("selectDeployments");
    statements.add("selectDeployment");
    statements.add("selectDeploymentByProcessDefinitionId");
    statements.add("selectByteArraysForDeployment");
    statements.add("selectResourceNamesForDeployment");
    statements.add("selectDeploymentResource");
    statements.add("selectBytesOfByteArray");
    statements.add("selectByteArrayById");
    statements.add("selectProcessDefinitionById");
    statements.add("selectLatestProcessDefinitionByKey");
    statements.add("selectProcessDefinitions");
    statements.add("selectProcessDefinitionIdsByDeployment");
    statements.add("selectProcessDefinitionByDeploymentAndKey");
    statements.add("selectJob");
    statements.add("selectJobs");
    statements.add("selectNextJobsToExecute");
    statements.add("selectFirstTimer");
    statements.add("selectTimersByExecutionId");
    statements.add("selectUser");
    statements.add("selectUsersByGroup");
    statements.add("selectUsers");
    statements.add("selectGroup");
    statements.add("selectGroupsByUser");
    statements.add("selectGroupsByUserAndType");
    statements.add("selectGroups");
    statements.add("selectCandidateTasks");
    statements.add("selectTasksByAssignee");
    statements.add("selectTaskByDynamicCriteria");
    statements.add("selectTaskCountByDynamicCriteria");
    statements.add("selectTaskInvolvementsByTask");
    statements.add("selectTableCount");
    statements.add("selectTableData");
    statements.add("selectProperty");

    statements.add("insertExecution");
    statements.add("insertJob");
    statements.add("insertTask");
    statements.add("insertTaskInvolvement");
    statements.add("insertVariableInstance");
    statements.add("insertByteArray");
    statements.add("insertMessage");
    statements.add("insertTimer");
    statements.add("insertDeployment");
    statements.add("insertByteArray");
    statements.add("insertProcessDefinition");
    statements.add("insertGroup");
    statements.add("insertUser");
    statements.add("insertMembership");

    statements.add("updateUser");
    statements.add("updateGroup");
    statements.add("updateProperty");
    statements.add("updateExecution");
    statements.add("updateTask");
    statements.add("updateTaskInvolvement");
    statements.add("updateVariableInstance");
    statements.add("updateByteArray");
    statements.add("updateMessage");
    statements.add("updateTimer");

    statements.add("deleteMembership");
    statements.add("deleteDeployment");
    statements.add("deleteExecution");
    statements.add("deleteTask");
    statements.add("deleteTaskInvolvement");
    statements.add("deleteVariableInstance");
    statements.add("deleteByteArray");
    statements.add("deleteJob");
    statements.add("deleteProcessDefinitionsForDeployment");
    statements.add("deleteByteArraysForDeployment");
    statements.add("deleteMembershipsForGroup");
    statements.add("deleteGroup");
    statements.add("deleteMembershipsForUser");
    statements.add("deleteUser");

    // DB specific statement ids
    // e.g. addDatabaseSpecificStatement("oracle", "selectExecution",
    // "selectExecution_oracle");
    addDatabaseSpecificStatement("mysql", "selectTableData", "selectTableData_mysql");
    addDatabaseSpecificStatement("mysql", "selectTaskByDynamicCriteria", "selectTaskByDynamicCriteria_mysql");
  }

  protected static void addDatabaseSpecificStatement(String databaseName, String activitiStatement, String ibatisStatement) {
	Map<String, String> specificStatements = null;
	if (databaseSpecificStatements==null) {
	  databaseSpecificStatements = new HashMap<String, Map<String,String>>();	
      specificStatements = new HashMap<String, String>();
      databaseSpecificStatements.put(databaseName, specificStatements);
    }
	else
	{
		specificStatements = databaseSpecificStatements.get(databaseName);
	}

    specificStatements.put(activitiStatement, ibatisStatement);
  }

  protected final String databaseName;
  protected final SqlSessionFactory sqlSessionFactory;
  protected final IdGenerator idGenerator;
  protected Map<String, String> databaseStatements;

  public IbatisPersistenceSessionFactory(IdGenerator idGenerator, String databaseName, String jdbcDriver, String jdbcUrl, String jdbcUsername, String jdbcPassword) {
    this(idGenerator, databaseName, new PooledDataSource(Thread.currentThread().getContextClassLoader(), jdbcDriver, jdbcUrl, jdbcUsername, jdbcPassword), true);
  }

  public IbatisPersistenceSessionFactory(IdGenerator idGenerator, String databaseName, DataSource dataSource, boolean localTransactions) {
    this.idGenerator = idGenerator;
    this.databaseName = databaseName;
    sqlSessionFactory = createSessionFactory(dataSource, localTransactions ? new JdbcTransactionFactory() : new ManagedTransactionFactory());
  }

  private SqlSessionFactory createSessionFactory(DataSource dataSource, TransactionFactory transactionFactory) {
    try {
      initializeDatabaseStatements(databaseName);

      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      InputStream inputStream = classLoader.getResourceAsStream("org/activiti/db/ibatis/activiti.ibatis.mem.conf.xml");

      // update the jdbc parameters to the configured ones...
      Reader reader = new InputStreamReader(inputStream);
      SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);      
      
      Environment environment = new Environment("default", transactionFactory, dataSource);
      sqlSessionFactory.getConfiguration().setEnvironment(environment);
      return sqlSessionFactory;

    } catch (Exception e) {
      throw new ActivitiException("Error while building ibatis SqlSessionFactory: " + e.getMessage(), e);
    }
  }

  // database statements //////////////////////////////////////////////////////

  protected void initializeDatabaseStatements(String databaseName) {
    databaseStatements = new HashMap<String, String>();
    for (String defaultStatement : statements) {
      databaseStatements.put(defaultStatement, defaultStatement);
    }

    Map<String, String> specificStatements = databaseSpecificStatements.get(databaseName);
    if (specificStatements != null) {
      databaseStatements.putAll(specificStatements);
    }
  }

  public String statement(String statement) {
    return databaseStatements.get(statement);
  }

  // database version check ///////////////////////////////////////////////////

  public void dbSchemaCheckVersion() {
    /*
     * Not quite sure if this is the right setting? We do want multiple updates
     * to be batched for performance ...
     */
    SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
    boolean success = false;
    try {
      String dbVersion = (String) sqlSession.selectOne(statement("selectDbSchemaVersion"));
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
    executeSchemaResource("create", sqlSessionFactory);
  }

  public void dbSchemaDrop() {
    executeSchemaResource("drop", sqlSessionFactory);
  }

  public void executeSchemaResource(String operation, SqlSessionFactory sqlSessionFactory) {
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

  public PersistenceSession openPersistenceSession(CommandContext commandContext) {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    return new IbatisPersistenceSession(sqlSession, idGenerator, databaseStatements);
  }

  // getters and setters //////////////////////////////////////////////////////

  public SqlSessionFactory getSqlSessionFactory() {
    return sqlSessionFactory;
  }
  public IdGenerator getIdGenerator() {
    return idGenerator;
  }
}

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
package org.activiti.impl.cfg;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.Statement;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.activiti.ActivitiException;
import org.activiti.ActivitiWrongDbException;
import org.activiti.ProcessEngine;
import org.activiti.impl.db.IdGenerator;
import org.activiti.impl.persistence.IbatisPersistenceSession;
import org.activiti.impl.persistence.PersistenceSession;
import org.activiti.impl.util.IoUtil;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;


/**
 * @author Tom Baeyens
 */
public class IbatisPersistenceSessionFactory implements PersistenceSessionFactory {
  
  private static Logger log = Logger.getLogger(IbatisPersistenceSessionFactory.class.getName());

  SqlSessionFactory sqlSessionFactory;
  IdGenerator idGenerator;
  
  public IbatisPersistenceSessionFactory(
              String databaseName,
              String jdbcDriver,
              String jdbcUrl,
              String jdbcUsername,
              String jdbcPassword) {
    
    try {
// TODO ping iBatis team for programmatic creation of a SqlSessionFactory
// Following results into the H2 in-memory database to disappear after successful creation of all tables.
//
//      Configuration configuration = new Configuration();
//      JdbcTransactionFactory jdbcTransactionFactory = new JdbcTransactionFactory();
//      DataSource dataSource = new PooledDataSource(jdbcDriver, 
//                                                   jdbcUrl, 
//                                                   jdbcUsername,
//                                                   jdbcPassword);

//      Environment environment = new Environment("DEFAULT", jdbcTransactionFactory, dataSource);
//      configuration.setEnvironment(environment);
//      
//      Map<String, XNode> sqlFragments = new HashMap<String, XNode>();
//      String[] resources = new String[]{
//        "org/activiti/db/ibatis/engine.mapping.xml",
//        "org/activiti/db/ibatis/deployment.mapping.xml",
//        "org/activiti/db/ibatis/processdefinition.mapping.xml",
//        "org/activiti/db/ibatis/execution.mapping.xml",
//        "org/activiti/db/ibatis/job.mapping.xml",
//        "org/activiti/db/ibatis/task.mapping.xml",
//        "org/activiti/db/ibatis/identity.mapping.xml",
//        "org/activiti/db/ibatis/variable.mapping.xml"
//      };
//      for (String resource: resources) {
//        ErrorContext.instance().resource(resource);
//        Reader reader = Resources.getResourceAsReader(resource);
//        XMLMapperBuilder mapperParser = new XMLMapperBuilder(reader, configuration, resource, sqlFragments);
//        mapperParser.parse();
//      }
//
//      this.sqlSessionFactory = new DefaultSqlSessionFactory(configuration);
      
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      InputStream inputStream = classLoader.getResourceAsStream("org/activiti/db/ibatis/activiti.ibatis.mem.conf.xml"); 
      Reader reader = new InputStreamReader(inputStream);
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
      
      // update the jdbc parameters to the configured ones...
      // TODO ask iBatis people for a more elegant way of building the SqlSessionFactory programmatically
      PooledDataSource originalPooledDatasource = (PooledDataSource) this.sqlSessionFactory.getConfiguration().getEnvironment().getDataSource();
      DataSource newUnpooledDataSource = new UnpooledDataSource(jdbcDriver,jdbcUrl,jdbcUsername,jdbcPassword);
      Field dataSourceField = PooledDataSource.class.getDeclaredField("dataSource");
      dataSourceField.setAccessible(true);
      dataSourceField.set(originalPooledDatasource, newUnpooledDataSource);
      
    } catch (Exception e) {
      throw new ActivitiException("Error while building ibatis SqlSessionFactory: "+e.getMessage(), e);
    }
  }
  
  public void setDbidGenerator(IdGenerator idGenerator) {
    this.idGenerator = idGenerator;
  }

  public void dbSchemaCheckVersion() {
    SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH); // Not quite sure if this is the right setting? We do want multiple updates to be batched for performance ...
    boolean success = false;
    try {
      String dbVersion = (String) sqlSession.selectOne("selectDbSchemaVersion");
      if (!ProcessEngine.VERSION.equals(dbVersion)) {
        throw new ActivitiWrongDbException(ProcessEngine.VERSION, dbVersion);
      }
      
      success = true;

    } catch(Exception e) {
      String exceptionMessage = e.getMessage();
      if ( (exceptionMessage.indexOf("Table")!=-1)
           && (exceptionMessage.indexOf("not found")!=-1)
         ) {
        throw new ActivitiException("no activiti tables in db.  set property db.schema.strategy=create-drop in activiti.properties for automatic schema creation", e);
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
    log.fine("activiti db schema creation successful");
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
      String resource = "org/activiti/db/"+operation+"/activiti.h2."+operation+".sql";
      InputStream inputStream = classLoader.getResourceAsStream(resource);
      if (inputStream==null) {
        throw new ActivitiException("resource '"+resource+"' is not available for creating the schema");
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
            log.fine("\n"+ddlStatement);
            jdbcStatement.execute(ddlStatement);
            jdbcStatement.close();
          } catch (Exception e) {
            if (exception==null) {
              exception = e;
            }
            log.log(Level.SEVERE, "problem during schema "+operation+", statement '"+ddlStatement, e);
          }
        }
      }
      
      if (exception!=null) {
        throw exception;          
      }
      
      success = true;

    } catch(Exception e) {
      throw new ActivitiException("couldn't create db schema", e);
      
    } finally {
      if (success) {
        sqlSession.commit(true);
      } else {
        sqlSession.rollback(true);        
      }
      sqlSession.close();        
    }
    
    log.fine("activiti db schema creation successful");
  }

  public PersistenceSession openPersistenceSession() {
    SqlSession openSession = sqlSessionFactory.openSession();
    return new IbatisPersistenceSession(openSession, idGenerator);
  }

  
  public SqlSessionFactory getSqlSessionFactory() {
    return sqlSessionFactory;
  }

  
  public IdGenerator getIdGenerator() {
    return idGenerator;
  }
  
  
}

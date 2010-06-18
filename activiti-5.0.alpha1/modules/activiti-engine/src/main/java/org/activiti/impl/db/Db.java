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
package org.activiti.impl.db;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.ActivitiException;
import org.activiti.ActivitiWrongDbException;
import org.activiti.Configuration;
import org.activiti.ProcessEngine;
import org.activiti.impl.ProcessEngineImpl;
import org.activiti.impl.util.IoUtil;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;


/**
 * Wrapper around various core SQL Database functions, 
 *  such as creating and upgrading schemas.
 * Note that this class is likely to undergo a lot of
 *  work as we move to supporting multiple databases,
 *  and again as we support cloud storage.
 * @author Tom Baeyens
 */
public class Db {

  private static Logger log = Logger.getLogger(Db.class.getName());

  public static void dbSchemaCreate(ProcessEngineImpl processEngine) {
    SqlSessionFactory sqlSessionFactory = processEngine.getConfigurationObject(Configuration.NAME_IBATISSQLSESSIONFACTORY, SqlSessionFactory.class);
    Map<String, Object> configurations = processEngine.getConfigurationObjects();
    String dbSchema = (String) configurations.get(Configuration.NAME_DBSCHEMA);
    if (dbSchema!=null) {
      dbSchema = dbSchema.toLowerCase();
      if (dbSchema.indexOf("create")!=-1) {
        executeSchemaResource("create", sqlSessionFactory);
      }
    }
  }

  public static void dbSchemaCheckVersion(ProcessEngineImpl processEngine) {
    SqlSessionFactory sqlSessionFactory = processEngine.getConfigurationObject(Configuration.NAME_IBATISSQLSESSIONFACTORY, SqlSessionFactory.class);
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
        throw new ActivitiException("no activiti tables in db.  add 'create' to value of <property name=\"DbSchema\" value=\"...\" /> in activiti.cfg.xml for automatic schema creation", e);
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
    
    log.fine("activiti db schema creation successful");
  }

  public static void dbSchemaDrop(ProcessEngineImpl processEngine) {
    SqlSessionFactory sqlSessionFactory = processEngine.getConfigurationObject(Configuration.NAME_IBATISSQLSESSIONFACTORY, SqlSessionFactory.class);
    Map<String, Object> configurations = processEngine.getConfigurationObjects();
    String dbSchema = (String) configurations.get(Configuration.NAME_DBSCHEMA);
    if (dbSchema!=null) {
      dbSchema = dbSchema.toLowerCase();
      if (dbSchema.indexOf("drop")!=-1) {
        executeSchemaResource("drop", sqlSessionFactory);
      }
    }
  }

  public static void executeSchemaResource(String operation, SqlSessionFactory sqlSessionFactory) {
    SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH); // Not quite sure if this is the right setting? We do want multiple updates to be batched for performance ...
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
}

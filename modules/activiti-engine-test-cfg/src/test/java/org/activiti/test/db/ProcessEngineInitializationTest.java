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
package org.activiti.test.db;

import java.util.HashMap;
import java.util.Map;

import org.activiti.ActivitiException;
import org.activiti.ActivitiWrongDbException;
import org.activiti.DbProcessEngineBuilder;
import org.activiti.DbSchemaStrategy;
import org.activiti.ProcessEngine;
import org.activiti.impl.ProcessEngineImpl;
import org.activiti.impl.persistence.IbatisPersistenceSessionFactory;
import org.activiti.impl.persistence.PersistenceSessionFactory;
import org.activiti.test.LogTestCase;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;


/**
 * @author Tom Baeyens
 */
public class ProcessEngineInitializationTest extends LogTestCase {

  public void testNoTables() {
    try {
      new DbProcessEngineBuilder()
        .configureFromPropertiesResource("org/activiti/test/db/activiti.properties")
        .buildProcessEngine();
      fail("expected exception");
    } catch (ActivitiException e) {
      assertExceptionMessage("no activiti tables in db.  set property db.schema.strategy=create-drop in activiti.properties for automatic schema creation", e);
    }
  }

  public void testVersionMismatch() {
    // first create the schema
    ProcessEngineImpl processEngine = (ProcessEngineImpl) new DbProcessEngineBuilder()
      .configureFromPropertiesResource("org/activiti/test/db/activiti.properties")
      .setDbSchemaStrategy(DbSchemaStrategy.CREATE_DROP)
      .buildProcessEngine();
    
    // then update the version to something that is different to the library version
    PersistenceSessionFactory persistenceSessionFactory = processEngine.getPersistenceSessionFactory();
    SqlSessionFactory sqlSessionFactory = ((IbatisPersistenceSessionFactory)persistenceSessionFactory).getSqlSessionFactory();
    SqlSession sqlSession = sqlSessionFactory.openSession();
    boolean success = false;
    try {
      Map<String, Object> parameters = new HashMap<String, Object>();
      parameters.put("name", "schema.version");
      parameters.put("value", "25.7");
      parameters.put("revision", new Integer(1));
      parameters.put("newRevision", new Integer(2));
      sqlSession.update("updateProperty", parameters);
      success = true;
    } catch(Exception e) {
      throw new ActivitiException("couldn't update db schema version", e);
    } finally {
      if (success) {
        sqlSession.commit(true);
      } else {
        sqlSession.rollback(true);        
      }
      sqlSession.close();        
    }
    
    // now we can see what happens if when a process engine is being 
    // build with a version mismatch between library and db tables
    try {
      new DbProcessEngineBuilder()
        .configureFromPropertiesResource("org/activiti/test/db/activiti.properties")
        .setDbSchemaStrategy(DbSchemaStrategy.CHECK_VERSION)
        .buildProcessEngine();
      fail("expected exception");
    } catch (ActivitiWrongDbException e) {
      assertExceptionMessage("version mismatch", e);
      assertEquals("25.7", e.getDbVersion());
      assertEquals(ProcessEngine.VERSION, e.getLibraryVersion());
    }

    // closing the original process engine to drop the db tables
    processEngine.close();
  }
}

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
import org.activiti.Configuration;
import org.activiti.ProcessEngine;
import org.activiti.impl.ProcessEngineImpl;
import org.activiti.test.LogTestCase;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;


/**
 * @author Tom Baeyens
 */
public class ProcessEngineInitializationTest extends LogTestCase {
  
  public void testNoTables() {
    try {
      new Configuration()
        .configurationResource("org/activiti/test/db/activiti.cfg.xml")
        .buildProcessEngine();
      fail("expected exception");
    } catch (ActivitiException e) {
      assertExceptionMessage("no activiti tables in db.  add 'create' to value of <property name=\"DbSchema\" value=\"...\" /> in activiti.cfg.xml for automatic schema creation", e);
    }
  }

  public void testVersionMismatch() {
    // first create the schema
    ProcessEngineImpl processEngine = (ProcessEngineImpl) new Configuration()
      .configurationResource("org/activiti/test/db/activiti.cfg.xml")
      .configurationObject(Configuration.NAME_DBSCHEMA, "create-drop")
      .buildProcessEngine();
    
    // then update the version
    SqlSessionFactory sqlSessionFactory = processEngine.getConfigurationObject(Configuration.NAME_IBATISSQLSESSIONFACTORY, SqlSessionFactory.class);
    SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH); // Not quite sure if this is the right setting? We do want multiple updates to be batched for performance ...
    boolean success = false;
    try {
      Map<String, Object> parameters = new HashMap<String, Object>();
      parameters.put("name", "schema.version");
      parameters.put("value", "25.7");
      parameters.put("dbversion", new Integer(1));
      parameters.put("newDbversion", new Integer(2));
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
    
    // now we can see what happens if there is a version mismatch
    try {
      new Configuration()
        .configurationResource("org/activiti/test/db/activiti.cfg.xml")
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

  public void testGetProcessEngineVersion() {
    assertNotNull(ProcessEngine.VERSION);
  }
}

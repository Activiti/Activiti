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

package org.activiti.standalone.jpa;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.test.AbstractActivitiTestCase;
import org.activiti.engine.impl.variable.EntityManagerSession;
import org.activiti.engine.impl.variable.EntityManagerSessionFactory;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:eugene.khrustalev@gmail.com">Eugene Khrustalev</a>
 */
public class JPABatchFetchingTest extends AbstractActivitiTestCase {

    protected static ProcessEngine cachedProcessEngine;
    private static EntityManagerFactory entityManagerFactory;

    @Override
    protected void initializeProcessEngine() {
        if (cachedProcessEngine==null) {
            ProcessEngineConfigurationImpl processEngineConfiguration = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration
                    .createProcessEngineConfigurationFromResource("org/activiti/standalone/jpa/activiti.cfg.xml");


            cachedProcessEngine = processEngineConfiguration.buildProcessEngine();

            EntityManagerSessionFactory entityManagerSessionFactory = (EntityManagerSessionFactory) processEngineConfiguration
                    .getSessionFactories()
                    .get(EntityManagerSession.class);

            entityManagerFactory = entityManagerSessionFactory.getEntityManagerFactory();
        }
        processEngine = cachedProcessEngine;
    }

    @Deployment(resources = {
            "org/activiti/standalone/jpa/JPAVariableTest.testStoreJPAEntityAsVariable.bpmn20.xml"
    })
    public void testJpaEntityListFetching() {

        final int LIST_SIZE = 10;

        EntityManager manager = entityManagerFactory.createEntityManager();
        manager.getTransaction().begin();

        // create some jpa entities and put them to the persistence context and collections
        List<StringIdJPAEntity> stringIdEntities = new ArrayList<StringIdJPAEntity>();
        List<IntegerIdJPAEntity> integerIdEntities = new ArrayList<IntegerIdJPAEntity>();
        for (int i = 0; i < LIST_SIZE; i++) {
            StringIdJPAEntity stringIdEntity = new StringIdJPAEntity();
            stringIdEntity.setStringId("string" + i);
            manager.persist(stringIdEntity);
            stringIdEntities.add(stringIdEntity);

            IntegerIdJPAEntity integerIdEntity = new IntegerIdJPAEntity();
            integerIdEntity.setIntId(i);
            manager.persist(integerIdEntity);
            integerIdEntities.add(integerIdEntity);
        }

        manager.flush();
        manager.getTransaction().commit();
        manager.close();

        // start a process
        Map variables = new HashMap();
        variables.put("stringIdEntities", stringIdEntities);
        variables.put("integerIdEntities", integerIdEntities);
        ProcessInstance process = processEngine.getRuntimeService()
                .startProcessInstanceByKey("JPAVariableProcess", variables);

        // query tasks
        Task task = processEngine.getTaskService()
                .createTaskQuery()
                .includeProcessVariables()
                .singleResult();

        stringIdEntities = (List<StringIdJPAEntity>) task.getProcessVariables().get("stringIdEntities");
        assertNotNull(stringIdEntities);
        assertEquals(stringIdEntities.size(), LIST_SIZE);

        integerIdEntities = (List<IntegerIdJPAEntity>) task.getProcessVariables().get("integerIdEntities");
        assertNotNull(integerIdEntities);
        assertEquals(integerIdEntities.size(), LIST_SIZE);

        for (int id = 0; id < LIST_SIZE; id++) {
            boolean found = false;

            String stringId = "string" + id;
            for (StringIdJPAEntity entity : stringIdEntities) {
                if(stringId.equals(entity.getStringId())) {
                    found = true;
                    break;
                }
            }
            assertTrue("StringIdJPAEntity with id='" + stringId + "' is not found", found);

            found = false;
            for (IntegerIdJPAEntity entity : integerIdEntities) {
                if(id == entity.getIntId()) {
                    found = true;
                    break;
                }
            }
            assertTrue("IntegerIdJPAEntity with id=" + id + " is not found", found);
        }
    }
}

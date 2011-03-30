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
package org.activiti.engine.test.bpmn.sendtask;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.bpmn.data.FieldBaseStructureInstance;
import org.activiti.engine.impl.bpmn.data.ItemDefinition;
import org.activiti.engine.impl.bpmn.data.ItemInstance;
import org.activiti.engine.impl.cfg.RepositorySession;
import org.activiti.engine.impl.db.DbRepositorySessionFactory;
import org.activiti.engine.impl.repository.ProcessDefinitionEntity;
import org.activiti.engine.test.bpmn.servicetask.AbstractWebServiceTaskTest;

/**
 * @author Esteban Robles Luna
 * @author Falko Menge
 */
public class WebServiceUELTest extends AbstractWebServiceTaskTest {

  public void testAsyncInvocationWithDataFlowUEL() throws Exception {
    assertEquals(-1, counter.getCount());

    DbRepositorySessionFactory dbRepositorySessionFactory = (DbRepositorySessionFactory) 
      this.processEngineConfiguration.getSessionFactories().get(RepositorySession.class);
    
    String processDefinitionId = repositoryService.createProcessDefinitionQuery().processDefinitionKey("asyncWebServiceInvocationWithDataFlowUEL").singleResult().getId();
    ProcessDefinitionEntity processDefinition = dbRepositorySessionFactory.getProcessDefinitionCache().get(processDefinitionId);
    ItemDefinition itemDefinition = processDefinition.getIoSpecification().getDataInputs().get(0).getDefinition();

    ItemInstance itemInstance = itemDefinition.createInstance();
    FieldBaseStructureInstance structureInstance = (FieldBaseStructureInstance) itemInstance.getStructureInstance();
    structureInstance.setFieldValue("newCounterValue", 23);

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("dataInputOfProcess", itemInstance);

    processEngine.getRuntimeService().startProcessInstanceByKey("asyncWebServiceInvocationWithDataFlowUEL", variables);
    waitForJobExecutorToProcessAllJobs(10000L, 250L);

    assertEquals(23, counter.getCount());
  }
}

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
package org.activiti.engine.test.bpmn.servicetask;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.bpmn.FieldBaseStructureInstance;
import org.activiti.engine.impl.bpmn.ItemDefinition;
import org.activiti.engine.impl.bpmn.ItemInstance;
import org.activiti.engine.impl.cfg.RepositorySession;
import org.activiti.engine.impl.db.DbRepositorySessionFactory;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;

/**
 * @author Esteban Robles Luna
 */
public class WebServiceUELTest extends AbstractWebServiceTaskTest {

  public void testWebServiceInvocationWithDataFlowUEL() throws Exception {
    DbRepositorySessionFactory dbRepositorySessionFactory = (DbRepositorySessionFactory) this.processEngineConfiguration.getSessionFactories().get(
            RepositorySession.class);
    ProcessDefinition processDefinition = dbRepositorySessionFactory.getProcessDefinitionCache().get("webServiceInvocationWithDataFlowUEL:1");
    ItemDefinition itemDefinition = processDefinition.getIoSpecification().getDataInputs().get(0).getDefinition();

    ItemInstance itemInstance = itemDefinition.createInstance();
    FieldBaseStructureInstance structureInstance = (FieldBaseStructureInstance) itemInstance.getStructureInstance();
    structureInstance.setFieldValue("prefix", "The counter has the value ");
    structureInstance.setFieldValue("suffix", ". Good news");

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("dataInputOfProcess", itemInstance);

    ProcessInstance instance = processEngine.getRuntimeService().startProcessInstanceByKey("webServiceInvocationWithDataFlowUEL", variables);
    waitForJobExecutorToProcessAllJobs(10000L, 250L);

    String response = (String) processEngine.getRuntimeService().getVariable(instance.getId(), "dataOutputOfProcess");
    assertEquals("The counter has the value -1. Good news", response);
  }
}

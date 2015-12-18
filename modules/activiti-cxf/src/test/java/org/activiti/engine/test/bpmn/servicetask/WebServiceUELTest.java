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

import org.activiti.engine.impl.bpmn.data.FieldBaseStructureInstance;
import org.activiti.engine.impl.bpmn.data.ItemDefinition;
import org.activiti.engine.impl.bpmn.data.ItemInstance;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;

/**
 * @author Esteban Robles Luna
 */
public class WebServiceUELTest extends AbstractWebServiceTaskTest {

  @Deployment
  public void testWebServiceInvocationWithDataFlowUEL() throws Exception {
    ProcessDefinitionEntity processDefinition = processEngineConfiguration
      .getCommandExecutor()
      .execute(new Command<ProcessDefinitionEntity>() {
        public ProcessDefinitionEntity execute(CommandContext commandContext) {
          return Context
            .getProcessEngineConfiguration()
            .getDeploymentManager()
            .findDeployedLatestProcessDefinitionByKey("webServiceInvocationWithDataFlowUEL");
        }
      });
  
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

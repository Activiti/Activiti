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
package org.activiti5.engine.test.api.event;

import java.util.Map;

import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;
import org.activiti5.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti5.engine.impl.test.PluggableActivitiTestCase;

/**
 * Test for event-listeners that are registered on a process-definition scope,
 * rather than on the global engine-wide scope.
 * 
 * @author Frederik Heremans
 */
public class ProcessDefinitionScopedEventListenerTest extends PluggableActivitiTestCase {

	protected TestActivitiEventListener testListenerAsBean;
	protected Map<Object, Object> oldBeans;

	/**
	 * Test to verify listeners on a process-definition are only called for events
	 * related to that definition.
	 */
	@Deployment(resources = { "org/activiti5/engine/test/api/runtime/oneTaskProcess.bpmn20.xml",
	    "org/activiti5/engine/test/api/event/simpleProcess.bpmn20.xml" })
	public void testProcessDefinitionScopedListener() throws Exception {
		ProcessDefinition firstDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(deploymentIdFromDeploymentAnnotation)
		    .processDefinitionKey("oneTaskProcess").singleResult();
		assertNotNull(firstDefinition);

		ProcessDefinition secondDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(deploymentIdFromDeploymentAnnotation)
		    .processDefinitionKey("simpleProcess").singleResult();
		assertNotNull(firstDefinition);

		// Fetch a reference to the process definition entity to add the listener
		org.activiti5.engine.impl.cfg.ProcessEngineConfigurationImpl activiti5ProcessEngineConfig = (org.activiti5.engine.impl.cfg.ProcessEngineConfigurationImpl) 
        processEngineConfiguration.getActiviti5CompatibilityHandler().getRawProcessConfiguration();
		
		TestActivitiEventListener listener = new TestActivitiEventListener();
		ProcessDefinitionEntity definitionEntity = (ProcessDefinitionEntity) activiti5ProcessEngineConfig.getRepositoryService()
		    .getProcessDefinition(firstDefinition.getId());
		assertNotNull(definitionEntity);

		definitionEntity.getEventSupport().addEventListener(listener);

		// Start a process for the first definition, events should be received
		ProcessInstance processInstance = runtimeService.startProcessInstanceById(firstDefinition.getId());
		assertNotNull(processInstance);

		assertFalse(listener.getEventsReceived().isEmpty());
		listener.clearEventsReceived();

		// Start an instance of the other definition
		ProcessInstance otherInstance = runtimeService.startProcessInstanceById(secondDefinition.getId());
		assertNotNull(otherInstance);
		assertTrue(listener.getEventsReceived().isEmpty());
	}
}

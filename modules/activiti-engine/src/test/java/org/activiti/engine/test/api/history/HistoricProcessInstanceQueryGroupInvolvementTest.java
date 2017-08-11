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
package org.activiti.engine.test.api.history;

import java.util.ArrayList;
import java.util.List;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;

public class HistoricProcessInstanceQueryGroupInvolvementTest extends PluggableActivitiTestCase {

	protected void setUp() throws Exception {
	    super.setUp();
	    repositoryService.createDeployment()
	      .addClasspathResource("org/activiti/engine/test/api/history/HistoricProcessInstanceQueryGroupInvolvementTest.bpmn20.xml")
	      .deploy();
	    
	    ProcessInstance processInstance0 = runtimeService.startProcessInstanceByKey("groupInvolvementProcess");
		runtimeService.addParticipantGroup(processInstance0.getId(), "group1");

		ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("groupInvolvementProcess");
		runtimeService.addParticipantGroup(processInstance1.getId(), "group1");
		runtimeService.addParticipantGroup(processInstance1.getId(), "group2");

		ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("groupInvolvementProcess");
		runtimeService.addParticipantUser(processInstance2.getId(), "kermit");
		List<Task> taskList = taskService.createTaskQuery().list();
		for (Task task: taskList) {
			taskService.complete(task.getId());
		}
	  }

	protected void tearDown() throws Exception {
		for (org.activiti.engine.repository.Deployment deployment : repositoryService.createDeploymentQuery().list()) {
			repositoryService.deleteDeployment(deployment.getId(), true);
		}
		super.tearDown();
	}
	
	public void testGroupInvolvementWithProcessInstance() {
		List<String> groupList = new ArrayList<String>();
	    groupList.add("group1");
	    groupList.add("group2");
	    //Assert group and user involvement query is working 
		assertEquals(3L, historyService.createHistoricProcessInstanceQuery().or().involvedUser("kermit")
				.involvedGroupsIn(groupList).endOr().count());
		//Assert group only involvement query working
		assertEquals(2L, historyService.createHistoricProcessInstanceQuery().involvedGroupsIn(groupList).count());
		//Assert user only involvement query working
		assertEquals(1L, historyService.createHistoricProcessInstanceQuery().involvedUser("kermit").count());
	}

}

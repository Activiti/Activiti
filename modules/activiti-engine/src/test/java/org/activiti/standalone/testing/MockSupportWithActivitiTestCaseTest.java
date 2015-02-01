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
package org.activiti.standalone.testing;

import org.activiti.engine.test.ActivitiTestCase;
import org.activiti.engine.test.Deployment;
import org.activiti.engine.test.mock.MockServiceTask;
import org.activiti.engine.test.mock.MockServiceTasks;
import org.activiti.engine.test.mock.NoOpServiceTasks;
import org.activiti.standalone.testing.helpers.ServiceTaskTestMock;

/**
 * @author Joram Barrez
 */
public class MockSupportWithActivitiTestCaseTest extends ActivitiTestCase { 
	
	@Override
	protected void setUp() throws Exception {
	  super.setUp();
	  
	  ServiceTaskTestMock.CALL_COUNT.set(0);
	  
	  mockSupport().mockServiceTaskWithClassDelegate("com.yourcompany.delegate", ServiceTaskTestMock.class);
	  mockSupport().mockServiceTaskWithClassDelegate("com.yourcompany.anotherDelegate", "org.activiti.standalone.testing.helpers.ServiceTaskTestMock");
	}
	
	@Deployment
	public void testClassDelegateMockSupport() {
		assertEquals(0, ServiceTaskTestMock.CALL_COUNT.get());
		runtimeService.startProcessInstanceByKey("mockSupportTest");
		assertEquals(1, ServiceTaskTestMock.CALL_COUNT.get());
	}
	
	@Deployment
	public void testClassDelegateStringMockSupport() {
		assertEquals(0, ServiceTaskTestMock.CALL_COUNT.get());
		runtimeService.startProcessInstanceByKey("mockSupportTest");
		assertEquals(1, ServiceTaskTestMock.CALL_COUNT.get());
	}
	
	@Deployment
	@MockServiceTask(originalClassName="com.yourcompany.delegate", 
	                 mockedClassName="org.activiti.standalone.testing.helpers.ServiceTaskTestMock")
	public void testMockedServiceTaskAnnotation() {
		assertEquals(0, ServiceTaskTestMock.CALL_COUNT.get());
		runtimeService.startProcessInstanceByKey("mockSupportTest");
		assertEquals(1, ServiceTaskTestMock.CALL_COUNT.get());	
	}
	
	@Deployment(resources = {"org/activiti/standalone/testing/MockSupportWithActivitiTestCaseTest.testMockedServiceTaskAnnotation.bpmn20.xml"})
	@MockServiceTask(id = "serviceTask", mockedClassName="org.activiti.standalone.testing.helpers.ServiceTaskTestMock")
	public void testMockedServiceTaskByIdAnnotation() {
		assertEquals(0, ServiceTaskTestMock.CALL_COUNT.get());
		runtimeService.startProcessInstanceByKey("mockSupportTest");
		assertEquals(1, ServiceTaskTestMock.CALL_COUNT.get());	
	}
	
	@Deployment
	@MockServiceTasks({
			@MockServiceTask(originalClassName="com.yourcompany.delegate1", mockedClassName="org.activiti.standalone.testing.helpers.ServiceTaskTestMock"),
			@MockServiceTask(originalClassName="com.yourcompany.delegate2", mockedClassName="org.activiti.standalone.testing.helpers.ServiceTaskTestMock")
	})
	public void testMockedServiceTasksAnnotation() {
		assertEquals(0, ServiceTaskTestMock.CALL_COUNT.get());
		runtimeService.startProcessInstanceByKey("mockSupportTest");
		assertEquals(2, ServiceTaskTestMock.CALL_COUNT.get());	
	}
	
	@Deployment
	@NoOpServiceTasks
	public void testNoOpServiceTasksAnnotation() {
		assertEquals(0, mockSupport().getNrOfNoOpServiceTaskExecutions());
		runtimeService.startProcessInstanceByKey("mockSupportTest");
		assertEquals(5, mockSupport().getNrOfNoOpServiceTaskExecutions());
		
		for (int i=1; i<=5; i++) {
			assertEquals("com.yourcompany.delegate" + i, mockSupport().getExecutedNoOpServiceTaskDelegateClassNames().get(i-1));
		}
	}
	
	@Deployment(resources = {"org/activiti/standalone/testing/MockSupportWithActivitiTestCaseTest.testNoOpServiceTasksAnnotation.bpmn20.xml"})
	@NoOpServiceTasks(
			ids = {"serviceTask1", "serviceTask3", "serviceTask5"}, 
	    classNames= {"com.yourcompany.delegate2", "com.yourcompany.delegate4"}
  )
	public void testNoOpServiceTasksWithIdsAnnotation() {
		assertEquals(0, mockSupport().getNrOfNoOpServiceTaskExecutions());
		runtimeService.startProcessInstanceByKey("mockSupportTest");
		assertEquals(5, mockSupport().getNrOfNoOpServiceTaskExecutions());
		
		for (int i=1; i<=5; i++) {
			assertEquals("com.yourcompany.delegate" + i, mockSupport().getExecutedNoOpServiceTaskDelegateClassNames().get(i-1));
		}
	}

}

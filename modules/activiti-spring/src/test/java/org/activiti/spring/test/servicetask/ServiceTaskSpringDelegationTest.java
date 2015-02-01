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
package org.activiti.spring.test.servicetask;

import org.activiti.engine.impl.test.JobTestHelper;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;
import org.activiti.spring.impl.test.SpringActivitiTestCase;
import org.springframework.test.context.ContextConfiguration;


/**
 * @author Joram Barrez
 */
@ContextConfiguration("classpath:org/activiti/spring/test/servicetask/servicetaskSpringTest-context.xml")
public class ServiceTaskSpringDelegationTest extends SpringActivitiTestCase {

    @Deployment
    public void testDelegateExpression() {
        ProcessInstance procInst = runtimeService.startProcessInstanceByKey("delegateExpressionToSpringBean");
        assertEquals("Activiti BPMN 2.0 process engine", runtimeService.getVariable(procInst.getId(), "myVar"));
        assertEquals("fieldInjectionWorking", runtimeService.getVariable(procInst.getId(), "fieldInjection"));
    }

    @Deployment
    public void testAsyncDelegateExpression() throws Exception {
        ProcessInstance procInst = runtimeService.startProcessInstanceByKey("delegateExpressionToSpringBean");
        assertTrue(JobTestHelper.areJobsAvailable(managementService));
        waitForJobExecutorToProcessAllJobs(5000, 500);
        Thread.sleep(1000);
        assertEquals("Activiti BPMN 2.0 process engine", runtimeService.getVariable(procInst.getId(), "myVar"));
        assertEquals("fieldInjectionWorking", runtimeService.getVariable(procInst.getId(), "fieldInjection"));
    }

    @Deployment
    public void testMethodExpressionOnSpringBean() {
        ProcessInstance procInst = runtimeService.startProcessInstanceByKey("methodExpressionOnSpringBean");
        assertEquals("ACTIVITI BPMN 2.0 PROCESS ENGINE", runtimeService.getVariable(procInst.getId(), "myVar"));
    }

    @Deployment
    public void testAsyncMethodExpressionOnSpringBean() {
        ProcessInstance procInst = runtimeService.startProcessInstanceByKey("methodExpressionOnSpringBean");
        assertTrue(JobTestHelper.areJobsAvailable(managementService));
        waitForJobExecutorToProcessAllJobs(5000, 500);
        assertEquals("ACTIVITI BPMN 2.0 PROCESS ENGINE", runtimeService.getVariable(procInst.getId(), "myVar"));
    }

    @Deployment
    public void testExecutionAndTaskListenerDelegationExpression() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("executionAndTaskListenerDelegation");
        assertEquals("working", runtimeService.getVariable(processInstance.getId(), "executionListenerVar"));
        assertEquals("working", runtimeService.getVariable(processInstance.getId(), "taskListenerVar"));

        assertEquals("executionListenerInjection", runtimeService.getVariable(processInstance.getId(), "executionListenerField"));
        assertEquals("taskListenerInjection", runtimeService.getVariable(processInstance.getId(), "taskListenerField"));
    }

}

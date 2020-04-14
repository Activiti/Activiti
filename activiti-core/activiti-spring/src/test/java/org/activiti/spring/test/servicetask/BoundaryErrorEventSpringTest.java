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

import java.util.List;

import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.spring.impl.test.SpringActivitiTestCase;
import org.springframework.test.context.ContextConfiguration;

/**
 * @see https://activiti.atlassian.net/browse/ACT-1166
 */
@ContextConfiguration("classpath:org/activiti/spring/test/servicetask/serviceraskSpringTestCatchError-context.xml")
public class BoundaryErrorEventSpringTest extends SpringActivitiTestCase {

    private void cleanUp() {
        List<org.activiti.engine.repository.Deployment> deployments = repositoryService.createDeploymentQuery().list();
        for (org.activiti.engine.repository.Deployment deployment : deployments) {
            repositoryService.deleteDeployment(deployment.getId(),
                                               true);
        }
    }

    @Override
    public void tearDown() {
        cleanUp();
    }

    @Deployment
    public void testCatchErrorThrownByJavaDelegateOnServiceTask() {
        String procId = runtimeService.startProcessInstanceByKey("catchErrorThrownByExpressionDelegateOnServiceTask").getId();
        assertThatErrorHasBeenCaught(procId);
    }

    private void assertThatErrorHasBeenCaught(String procId) {
        // The service task will throw an error event,
        // which is caught on the service task boundary
        assertEquals("No tasks found in task list.", 1, taskService.createTaskQuery().count());
        Task task = taskService.createTaskQuery().singleResult();
        assertEquals("Escalated Task", task.getName());

        // Completing the task will end the process instance
        taskService.complete(task.getId());
        assertProcessEnded(procId);
    }
}

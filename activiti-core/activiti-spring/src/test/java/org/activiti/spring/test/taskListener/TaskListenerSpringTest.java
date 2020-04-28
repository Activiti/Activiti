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

package org.activiti.spring.test.taskListener;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.spring.impl.test.SpringActivitiTestCase;
import org.springframework.test.context.ContextConfiguration;

/**

 */
@ContextConfiguration("classpath:org/activiti/spring/test/taskListener/TaskListenerDelegateExpressionTest-context.xml")
public class TaskListenerSpringTest extends SpringActivitiTestCase {

    private void cleanUp() {
        List<org.activiti.engine.repository.Deployment> deployments = repositoryService.createDeploymentQuery().list();
        for (org.activiti.engine.repository.Deployment deployment : deployments) {
            repositoryService.deleteDeployment(deployment.getId(), true);
        }
    }

    @Override
    public void tearDown() {
        cleanUp();
    }

    @Deployment
    public void testTaskListenerDelegateExpression() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("taskListenerDelegateExpression");

        // Completing first task will set variable on process instance
        Task task = taskService.createTaskQuery().singleResult();
        taskService.complete(task.getId());
        assertThat(runtimeService.getVariable(processInstance.getId(), "calledInExpression")).isEqualTo("task1-complete");

        // Completing second task will set variable on process instance
        task = taskService.createTaskQuery().singleResult();
        taskService.complete(task.getId());
        assertThat(runtimeService.getVariable(processInstance.getId(), "calledThroughNotify")).isEqualTo("task2-notify");
    }

}

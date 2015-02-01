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

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.spring.impl.test.SpringActivitiTestCase;
import org.springframework.test.context.ContextConfiguration;


/**
 * @author Joram Barrez
 */
@ContextConfiguration("classpath:org/activiti/spring/test/taskListener/TaskListenerDelegateExpressionTest-context.xml")
public class TaskListenerSpringTest extends SpringActivitiTestCase {

    @Deployment
    public void testTaskListenerDelegateExpression() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("taskListenerDelegateExpression");

        // Completing first task will set variable on process instance
        Task task = taskService.createTaskQuery().singleResult();
        taskService.complete(task.getId());
        assertEquals("task1-complete", runtimeService.getVariable(processInstance.getId(), "calledInExpression"));

        // Completing second task will set variable on process instance
        task = taskService.createTaskQuery().singleResult();
        taskService.complete(task.getId());
        assertEquals("task2-notify", runtimeService.getVariable(processInstance.getId(), "calledThroughNotify"));
    }

}

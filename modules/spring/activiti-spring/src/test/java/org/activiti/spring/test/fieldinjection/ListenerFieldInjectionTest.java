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
package org.activiti.spring.test.fieldinjection;

import java.util.Map;

import org.activiti.engine.impl.util.CollectionUtil;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.spring.impl.test.SpringActivitiTestCase;
import org.springframework.test.context.ContextConfiguration;


/**
 * @author Joram Barrez
 */
@ContextConfiguration("classpath:org/activiti/spring/test/fieldinjection/fieldInjectionSpringTest-context.xml")
public class ListenerFieldInjectionTest extends SpringActivitiTestCase {

    @Deployment
    public void testDelegateExpressionListenerFieldInjection() {
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("listenerFieldInjection", CollectionUtil.singletonMap("startValue", 42));

      // Process start execution listener
      Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
      Map<String, Object> variables = runtimeService.getVariables(processInstance.getId());
      assertEquals(2, variables.size());
      assertEquals(4200, ((Number) variables.get("processStartValue")).intValue());
      
      // Sequence flow execution listener
      taskService.complete(task.getId());
      task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
      variables = runtimeService.getVariables(processInstance.getId());
      assertEquals(3, variables.size());
      assertEquals(420000, ((Number) variables.get("sequenceFlowValue")).intValue());
      
      // task listeners
      taskService.complete(task.getId());
      task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
      variables = runtimeService.getVariables(processInstance.getId());
      assertEquals(4, variables.size());
      assertEquals(210000, ((Number) variables.get("taskCreateValue")).intValue());
      
      taskService.complete(task.getId());
      variables = runtimeService.getVariables(processInstance.getId());
      assertEquals(5, variables.size());
      assertEquals(105000, ((Number) variables.get("taskCompleteValue")).intValue());
      
      assertEquals(1, TestExecutionListener.INSTANCE_COUNT.get());
      assertEquals(1, TestTaskListener.INSTANCE_COUNT.get());
    }
    
}

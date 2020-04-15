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

package org.activiti.examples.bpmn.callactivity;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.activiti.engine.test.Deployment;

/**
 */
public class CallActivityTest extends PluggableActivitiTestCase {

  @Deployment(resources = { "org/activiti/examples/bpmn/callactivity/orderProcess.bpmn20.xml", "org/activiti/examples/bpmn/callactivity/checkCreditProcess.bpmn20.xml" })
  public void testOrderProcessWithCallActivity() {
    // After the process has started, the 'verify credit history' task
    // should be active
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("orderProcess");
    TaskQuery taskQuery = taskService.createTaskQuery();
    Task verifyCreditTask = taskQuery.singleResult();
    assertThat(verifyCreditTask.getName()).isEqualTo("Verify credit history");

    // Verify with Query API
    ProcessInstance subProcessInstance = runtimeService.createProcessInstanceQuery().superProcessInstanceId(pi.getId()).singleResult();
    assertThat(subProcessInstance).isNotNull();
    assertThat(runtimeService.createProcessInstanceQuery().subProcessInstanceId(subProcessInstance.getId()).singleResult().getId()).isEqualTo(pi.getId());

    // Completing the task with approval, will end the subprocess and
    // continue the original process
    taskService.complete(verifyCreditTask.getId(), singletonMap("creditApproved", true));
    Task prepareAndShipTask = taskQuery.singleResult();
    assertThat(prepareAndShipTask.getName()).isEqualTo("Prepare and Ship");
  }

  @Deployment(resources = { "org/activiti/examples/bpmn/callactivity/mainProcess.bpmn20.xml", "org/activiti/examples/bpmn/callactivity/childProcess.bpmn20.xml" })
  public void testCallActivityWithModeledDataObjectsInSubProcess() {
    // After the process has started, the 'verify credit history' task should be active
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("mainProcess");
    TaskQuery taskQuery = taskService.createTaskQuery();
    Task verifyCreditTask = taskQuery.singleResult();
    assertThat(verifyCreditTask.getName()).isEqualTo("User Task 1");

    // Verify with Query API
    ProcessInstance subProcessInstance = runtimeService.createProcessInstanceQuery().superProcessInstanceId(pi.getId()).singleResult();
    assertThat(subProcessInstance).isNotNull();
    assertThat(runtimeService.createProcessInstanceQuery().subProcessInstanceId(subProcessInstance.getId()).singleResult().getId()).isEqualTo(pi.getId());

    assertThat(runtimeService.getVariable(subProcessInstance.getId(), "Name")).isEqualTo("Batman");
  }

  @Deployment(resources = { "org/activiti/examples/bpmn/callactivity/mainProcess.bpmn20.xml",
                            "org/activiti/examples/bpmn/callactivity/childProcess.bpmn20.xml",
                            "org/activiti/examples/bpmn/callactivity/mainProcessBusinessKey.bpmn20.xml",
                            "org/activiti/examples/bpmn/callactivity/mainProcessInheritBusinessKey.bpmn20.xml"})
  public void testCallActivityWithBusinessKey() {
    // No use of business key attributes
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("mainProcess");
    ProcessInstance subProcessInstance = runtimeService.createProcessInstanceQuery().superProcessInstanceId(pi.getId()).singleResult();
    assertThat(subProcessInstance.getBusinessKey()).isNull();

    // Modeled using expression: businessKey="${busKey}"
    Map<String,Object> variables = new HashMap<>();
    variables.put("busKey", "123");
    pi = runtimeService.startProcessInstanceByKey("mainProcessBusinessKey", variables);
    subProcessInstance = runtimeService.createProcessInstanceQuery().superProcessInstanceId(pi.getId()).singleResult();
    assertThat(subProcessInstance.getBusinessKey()).isEqualTo("123");

    // Inherit business key
    pi = runtimeService.startProcessInstanceByKey("mainProcessInheritBusinessKey", "123");
    subProcessInstance = runtimeService.createProcessInstanceQuery().superProcessInstanceId(pi.getId()).singleResult();
    assertThat(subProcessInstance.getBusinessKey()).isEqualTo("123");
  }

}

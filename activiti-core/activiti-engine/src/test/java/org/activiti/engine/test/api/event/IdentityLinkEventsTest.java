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
package org.activiti.engine.test.api.event;

import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

/**
 * Test case for all {@link ActivitiEvent}s related to process definitions.
 *

 */
public class IdentityLinkEventsTest extends PluggableActivitiTestCase {

  private TestActivitiEntityEventListener listener;

  /**
   * Check identity links on process definitions.
   */
  @Deployment(resources = { "org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testProcessDefinitionIdentityLinkEvents() throws Exception {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey("oneTaskProcess").singleResult();

    assertThat(processDefinition).isNotNull();

    // Add candidate user and group
    repositoryService.addCandidateStarterUser(processDefinition.getId(), "kermit");
    repositoryService.addCandidateStarterGroup(processDefinition.getId(), "sales");
    assertThat(listener.getEventsReceived()).hasSize(4);

    ActivitiEntityEvent event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_CREATED);
    assertThat(event.getEntity()).isInstanceOf(IdentityLink.class);
    assertThat(event.getProcessDefinitionId()).isEqualTo(processDefinition.getId());
    assertThat(event.getProcessInstanceId()).isNull();
    assertThat(event.getExecutionId()).isNull();

    event = (ActivitiEntityEvent) listener.getEventsReceived().get(1);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_INITIALIZED);

    event = (ActivitiEntityEvent) listener.getEventsReceived().get(2);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_CREATED);
    assertThat(event.getEntity()).isInstanceOf(IdentityLink.class);
    assertThat(event.getProcessDefinitionId()).isEqualTo(processDefinition.getId());
    assertThat(event.getProcessInstanceId()).isNull();
    assertThat(event.getExecutionId()).isNull();

    event = (ActivitiEntityEvent) listener.getEventsReceived().get(3);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_INITIALIZED);
    listener.clearEventsReceived();

    // Delete identity links
    repositoryService.deleteCandidateStarterUser(processDefinition.getId(), "kermit");
    repositoryService.deleteCandidateStarterGroup(processDefinition.getId(), "sales");
    assertThat(listener.getEventsReceived()).hasSize(2);
    event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_DELETED);
    assertThat(event.getEntity()).isInstanceOf(IdentityLink.class);
    assertThat(event.getProcessDefinitionId()).isEqualTo(processDefinition.getId());
    assertThat(event.getProcessInstanceId()).isNull();
    assertThat(event.getExecutionId()).isNull();
    event = (ActivitiEntityEvent) listener.getEventsReceived().get(1);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_DELETED);
    assertThat(event.getEntity()).isInstanceOf(IdentityLink.class);
    assertThat(event.getProcessDefinitionId()).isEqualTo(processDefinition.getId());
    assertThat(event.getProcessInstanceId()).isNull();
    assertThat(event.getExecutionId()).isNull();
  }

  /**
   * Check identity links on process instances.
   */
  @Deployment(resources = { "org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testProcessInstanceIdentityLinkEvents() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // Add identity link
    runtimeService.addUserIdentityLink(processInstance.getId(), "kermit", "test");
    assertThat(listener.getEventsReceived()).hasSize(2);

    ActivitiEntityEvent event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_CREATED);
    assertThat(event.getEntity()).isInstanceOf(IdentityLink.class);
    assertThat(event.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(event.getExecutionId()).isEqualTo(processInstance.getId());
    assertThat(event.getProcessDefinitionId()).isEqualTo(processInstance.getProcessDefinitionId());
    IdentityLink link = (IdentityLink) event.getEntity();
    assertThat(link.getUserId()).isEqualTo("kermit");
    assertThat(link.getType()).isEqualTo("test");

    event = (ActivitiEntityEvent) listener.getEventsReceived().get(1);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_INITIALIZED);

    listener.clearEventsReceived();

    // Deleting process should delete identity link
    runtimeService.deleteProcessInstance(processInstance.getId(), "test");
    assertThat(listener.getEventsReceived()).hasSize(1);

    event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_DELETED);
    assertThat(event.getEntity()).isInstanceOf(IdentityLink.class);
    link = (IdentityLink) event.getEntity();
    assertThat(link.getUserId()).isEqualTo("kermit");
    assertThat(link.getType()).isEqualTo("test");
  }

  /**
   * Check identity links on process instances.
   */
  @Deployment(resources = { "org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testTaskIdentityLinks() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(task).isNotNull();

    // Add identity link
    taskService.addCandidateUser(task.getId(), "kermit");
    taskService.addCandidateGroup(task.getId(), "sales");

    // Three events are received, since the user link on the task also
    // creates an involvement in the process
    assertThat(listener.getEventsReceived()).hasSize(6);

    ActivitiEntityEvent event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_CREATED);
    assertThat(event.getEntity()).isInstanceOf(IdentityLink.class);
    IdentityLink link = (IdentityLink) event.getEntity();
    assertThat(link.getUserId()).isEqualTo("kermit");
    assertThat(link.getType()).isEqualTo("candidate");
    assertThat(link.getTaskId()).isEqualTo(task.getId());
    assertThat(event.getExecutionId()).isEqualTo(task.getExecutionId());
    assertThat(event.getProcessDefinitionId()).isEqualTo(task.getProcessDefinitionId());
    assertThat(event.getProcessInstanceId()).isEqualTo(task.getProcessInstanceId());

    event = (ActivitiEntityEvent) listener.getEventsReceived().get(1);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_INITIALIZED);
    assertThat(link.getUserId()).isEqualTo("kermit");
    assertThat(link.getType()).isEqualTo("candidate");

    event = (ActivitiEntityEvent) listener.getEventsReceived().get(4);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_CREATED);
    assertThat(event.getEntity()).isInstanceOf(IdentityLink.class);
    link = (IdentityLink) event.getEntity();
    assertThat(link.getGroupId()).isEqualTo("sales");
    assertThat(link.getType()).isEqualTo("candidate");
    assertThat(link.getTaskId()).isEqualTo(task.getId());
    assertThat(event.getExecutionId()).isEqualTo(task.getExecutionId());
    assertThat(event.getProcessDefinitionId()).isEqualTo(task.getProcessDefinitionId());
    assertThat(event.getProcessInstanceId()).isEqualTo(task.getProcessInstanceId());
    event = (ActivitiEntityEvent) listener.getEventsReceived().get(5);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_INITIALIZED);
    assertThat(link.getGroupId()).isEqualTo("sales");
    assertThat(link.getType()).isEqualTo("candidate");

    listener.clearEventsReceived();

    // Deleting process should delete identity link
    runtimeService.deleteProcessInstance(processInstance.getId(), "test");
    assertThat(listener.getEventsReceived()).hasSize(3);

    event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_DELETED);
    event = (ActivitiEntityEvent) listener.getEventsReceived().get(1);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_DELETED);
    event = (ActivitiEntityEvent) listener.getEventsReceived().get(2);
    assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_DELETED);
  }

  /**
   * Check deletion of links on process instances.
   */
  @Deployment(resources = { "org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testProcessInstanceIdentityDeleteCandidateGroupEvents() throws Exception {

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(task).isNotNull();

    // Add identity link
    taskService.addCandidateUser(task.getId(), "kermit");
    taskService.addCandidateGroup(task.getId(), "sales");

    // Three events are received, since the user link on the task also creates an involvement in the process. See previous test
    assertThat(listener.getEventsReceived()).hasSize(6);

    listener.clearEventsReceived();
    taskService.deleteCandidateUser(task.getId(), "kermit");
    assertThat(listener.getEventsReceived()).hasSize(1);

    listener.clearEventsReceived();
    taskService.deleteCandidateGroup(task.getId(), "sales");
    assertThat(listener.getEventsReceived()).hasSize(1);
  }

  @Override
  protected void initializeServices() {
    super.initializeServices();

    listener = new TestActivitiEntityEventListener(IdentityLink.class);
    processEngineConfiguration.getEventDispatcher().addEventListener(listener);
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();

    if (listener != null) {
      listener.clearEventsReceived();
      processEngineConfiguration.getEventDispatcher().removeEventListener(listener);
    }
  }
}

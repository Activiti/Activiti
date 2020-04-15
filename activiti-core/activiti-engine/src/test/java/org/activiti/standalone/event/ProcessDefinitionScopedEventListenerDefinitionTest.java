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
package org.activiti.standalone.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.List;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.impl.test.ResourceActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.engine.test.api.event.StaticTestActivitiEventListener;
import org.activiti.engine.test.api.event.TestActivitiEventListener;

/**
 * Test for event-listeners that are registered on a process-definition scope, rather than on the global engine-wide scope, declared in the BPMN XML.
 *
 */
public class ProcessDefinitionScopedEventListenerDefinitionTest extends ResourceActivitiTestCase {

  public ProcessDefinitionScopedEventListenerDefinitionTest() {
    super("org/activiti/standalone/event/activiti-eventlistener.cfg.xml");
  }

  protected TestActivitiEventListener testListenerBean;

  /**
   * Test to verify listeners defined in the BPMN xml are added to the process definition and are active.
   */
  @Deployment
  public void testProcessDefinitionListenerDefinition() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testEventListeners");
    assertThat(testListenerBean).isNotNull();

    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.complete(task.getId());

    // Check if the listener (defined as bean) received events (only creation, not other events)
    assertThat(testListenerBean.getEventsReceived().isEmpty()).isFalse();
    for (ActivitiEvent event : testListenerBean.getEventsReceived()) {
      assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_CREATED);
    }

    // First event received should be creation of Process-instance
    assertThat(testListenerBean.getEventsReceived().get(0)).isInstanceOf(ActivitiEntityEvent.class);
    ActivitiEntityEvent event = (ActivitiEntityEvent) testListenerBean.getEventsReceived().get(0);
    assertThat(event.getEntity()).isInstanceOf(ProcessInstance.class);
    assertThat(((ProcessInstance) event.getEntity()).getId()).isEqualTo(processInstance.getId());

    // Check if listener, defined by classname, received all events
    List<ActivitiEvent> events = StaticTestActivitiEventListener.getEventsReceived();
    assertThat(events.isEmpty()).isFalse();

    boolean insertFound = false;
    boolean deleteFound = false;

    for (ActivitiEvent e : events) {
      if (ActivitiEventType.ENTITY_CREATED == e.getType()) {
        insertFound = true;
      } else if (ActivitiEventType.ENTITY_DELETED == e.getType()) {
        deleteFound = true;
      }
    }
    assertThat(insertFound).isTrue();
    assertThat(deleteFound).isTrue();
  }

  /**
   * Test to verify listeners defined in the BPMN xml with invalid class/delegateExpression values cause an exception when process is started.
   */
  public void testProcessDefinitionListenerDefinitionError() throws Exception {

    // Deploy process with expression which references an unexisting bean
    org.activiti.engine.repository.Deployment deployment = repositoryService.createDeployment().addClasspathResource("org/activiti/standalone/event/invalidEventListenerExpression.bpmn20.xml").deploy();
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testInvalidEventExpression");
    assertThat(processInstance).isNotNull();
    repositoryService.deleteDeployment(deployment.getId(), true);

    // Deploy process with listener which references an unexisting class
    deployment = repositoryService.createDeployment().addClasspathResource("org/activiti/standalone/event/invalidEventListenerClass.bpmn20.xml").deploy();
    processInstance = runtimeService.startProcessInstanceByKey("testInvalidEventClass");
    repositoryService.deleteDeployment(deployment.getId(), true);
  }

  /**
   * Test to verify if event listeners defined in the BPMN XML which have illegal event-types cause an exception on deploy.
   */
  public void testProcessDefinitionListenerDefinitionIllegalType() throws Exception {
    assertThatExceptionOfType(ActivitiIllegalArgumentException.class)
      .isThrownBy(() -> repositoryService.createDeployment()
        .addClasspathResource("org/activiti/standalone/event/invalidEventListenerType.bpmn20.xml")
        .deploy())
      .withMessageContaining("Invalid event-type: invalid");
  }

  /**
   * Test to verify listeners defined in the BPMN xml are added to the process definition and are active, for all entity types
   */
  @Deployment
  public void testProcessDefinitionListenerDefinitionEntities() throws Exception {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testEventListeners");
    assertThat(processInstance).isNotNull();
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertThat(task).isNotNull();

    // Attachment entity
    TestActivitiEventListener theListener = (TestActivitiEventListener) processEngineConfiguration.getBeans().get("testAttachmentEventListener");
    assertThat(theListener).isNotNull();
    assertThat(theListener.getEventsReceived()).hasSize(0);

    taskService.createAttachment("test", task.getId(), processInstance.getId(), "test", "test", "url");
    assertThat(theListener.getEventsReceived()).hasSize(2);
    assertThat(theListener.getEventsReceived().get(0).getType()).isEqualTo(ActivitiEventType.ENTITY_CREATED);
    assertThat(theListener.getEventsReceived().get(1).getType()).isEqualTo(ActivitiEventType.ENTITY_INITIALIZED);

  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    testListenerBean = (TestActivitiEventListener) processEngineConfiguration.getBeans().get("testEventListener");
  }
}

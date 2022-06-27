/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.test.api.event;

import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.repository.Deployment;

/**
 * Test case for all {@link ActivitiEvent}s related to deployments.
 *

 */
public class DeploymentEventsTest extends PluggableActivitiTestCase {

  private TestActivitiEntityEventListener listener;

  /**
   * Test create, update and delete events of deployment entities.
   */
  public void testDeploymentEvents() throws Exception {
    Deployment deployment = null;
    try {
      listener.clearEventsReceived();
      deployment = repositoryService.createDeployment().addClasspathResource("org/activiti/engine/test/api/runtime/oneTaskProcess.bpmn20.xml").deploy();
      assertThat(deployment).isNotNull();

      // Check create-event
      assertThat(listener.getEventsReceived()).hasSize(2);
      assertThat(listener.getEventsReceived().get(0)).isInstanceOf(ActivitiEntityEvent.class);

      ActivitiEntityEvent event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
      assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_CREATED);
      assertThat(((Deployment) event.getEntity()).getId()).isEqualTo(deployment.getId());

      assertThat(listener.getEventsReceived().get(1)).isInstanceOf(ActivitiEntityEvent.class);
      event = (ActivitiEntityEvent) listener.getEventsReceived().get(1);
      assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_INITIALIZED);
      assertThat(((Deployment) event.getEntity()).getId()).isEqualTo(deployment.getId());

      listener.clearEventsReceived();

      // Check update event when category is updated
      repositoryService.setDeploymentCategory(deployment.getId(), "test");
      assertThat(listener.getEventsReceived()).hasSize(1);
      assertThat(listener.getEventsReceived().get(0)).isInstanceOf(ActivitiEntityEvent.class);

      event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
      assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_UPDATED);
      assertThat(((Deployment) event.getEntity()).getId()).isEqualTo(deployment.getId());
      assertThat(((Deployment) event.getEntity()).getCategory()).isEqualTo("test");
      listener.clearEventsReceived();

      // Check delete event when category is updated
      repositoryService.deleteDeployment(deployment.getId(), true);
      assertThat(listener.getEventsReceived()).hasSize(1);
      assertThat(listener.getEventsReceived().get(0)).isInstanceOf(ActivitiEntityEvent.class);

      event = (ActivitiEntityEvent) listener.getEventsReceived().get(0);
      assertThat(event.getType()).isEqualTo(ActivitiEventType.ENTITY_DELETED);
      assertThat(((Deployment) event.getEntity()).getId()).isEqualTo(deployment.getId());
      listener.clearEventsReceived();

    } finally {
      if (deployment != null && repositoryService.createDeploymentQuery().deploymentId(deployment.getId()).count() > 0) {
        repositoryService.deleteDeployment(deployment.getId(), true);
      }
    }
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    listener = new TestActivitiEntityEventListener(Deployment.class);
    processEngineConfiguration.getEventDispatcher().addEventListener(listener);
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();

    if (listener != null) {
      processEngineConfiguration.getEventDispatcher().removeEventListener(listener);
    }
  }
}

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

import java.util.List;

import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;

/**
 * Test case for all {@link ActivitiEvent}s related to activities.
 *
 */
public class HistoricActivityEventsTest extends PluggableActivitiTestCase {

  private TestHistoricActivityEventListener listener;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    this.listener = new TestHistoricActivityEventListener();
    processEngineConfiguration.getEventDispatcher().addEventListener(listener);
  }

  @Override
  protected void tearDown() throws Exception {

    if (listener != null) {
      listener.clearEventsReceived();
      processEngineConfiguration.getEventDispatcher().removeEventListener(listener);
    }

    super.tearDown();
  }

  /**
   * Test added to assert the historic activity instance event
   */
  @Deployment
  public void testHistoricActivityEventDispatched() {
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {

      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("TestActivityEvents");
      assertThat(processInstance).isNotNull();

      for (int i = 0; i < 2; i++) {
        taskService.complete(taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult().getId());
      }

      List<ActivitiEvent> events = listener.getEventsReceived();

      // Process instance start
      assertThat(events.get(0).getType()).isEqualTo(ActivitiEventType.HISTORIC_PROCESS_INSTANCE_CREATED);

      // main start
      assertThat(events.get(1).getType()).isEqualTo(ActivitiEventType.HISTORIC_ACTIVITY_INSTANCE_CREATED);
      assertThat((((HistoricActivityInstance) ((ActivitiEntityEvent) events.get(1)).getEntity()).getActivityId())).isEqualTo("mainStart");

      assertThat(events.get(2).getType()).isEqualTo(ActivitiEventType.HISTORIC_ACTIVITY_INSTANCE_ENDED);
      assertThat((((HistoricActivityInstance) ((ActivitiEntityEvent) events.get(2)).getEntity()).getActivityId())).isEqualTo("mainStart");
      assertThat((((HistoricActivityInstance) ((ActivitiEntityEvent) events.get(2)).getEntity()).getEndTime())).as("mainStart").isNotNull();

      // Subprocess start
      assertThat(events.get(3).getType()).isEqualTo(ActivitiEventType.HISTORIC_ACTIVITY_INSTANCE_CREATED);
      assertThat((((HistoricActivityInstance) ((ActivitiEntityEvent) events.get(3)).getEntity()).getActivityId())).isEqualTo("subProcess");

      // subProcessStart
      assertThat(events.get(4).getType()).isEqualTo(ActivitiEventType.HISTORIC_ACTIVITY_INSTANCE_CREATED);
      assertThat((((HistoricActivityInstance) ((ActivitiEntityEvent) events.get(4)).getEntity()).getActivityId())).isEqualTo("subProcessStart");

      assertThat(events.get(5).getType()).isEqualTo(ActivitiEventType.HISTORIC_ACTIVITY_INSTANCE_ENDED);
      assertThat((((HistoricActivityInstance) ((ActivitiEntityEvent) events.get(5)).getEntity()).getActivityId())).isEqualTo("subProcessStart");
      assertThat((((HistoricActivityInstance) ((ActivitiEntityEvent) events.get(5)).getEntity()).getEndTime())).as("subProcessStart").isNotNull();

      // Task a
      assertThat(events.get(6).getType()).isEqualTo(ActivitiEventType.HISTORIC_ACTIVITY_INSTANCE_CREATED);
      assertThat((((HistoricActivityInstance) ((ActivitiEntityEvent) events.get(6)).getEntity()).getActivityId())).isEqualTo("a");

      assertThat(events.get(7).getType()).isEqualTo(ActivitiEventType.HISTORIC_ACTIVITY_INSTANCE_ENDED);
      assertThat((((HistoricActivityInstance) ((ActivitiEntityEvent) events.get(7)).getEntity()).getActivityId())).isEqualTo("a");
      assertThat((((HistoricActivityInstance) ((ActivitiEntityEvent) events.get(7)).getEntity()).getEndTime())).as("a").isNotNull();

      // Task b
      assertThat(events.get(8).getType()).isEqualTo(ActivitiEventType.HISTORIC_ACTIVITY_INSTANCE_CREATED);
      assertThat((((HistoricActivityInstance) ((ActivitiEntityEvent) events.get(8)).getEntity()).getActivityId())).isEqualTo("b");

      assertThat(events.get(9).getType()).isEqualTo(ActivitiEventType.HISTORIC_ACTIVITY_INSTANCE_ENDED);
      assertThat((((HistoricActivityInstance) ((ActivitiEntityEvent) events.get(9)).getEntity()).getActivityId())).isEqualTo("b");
      assertThat((((HistoricActivityInstance) ((ActivitiEntityEvent) events.get(9)).getEntity()).getEndTime())).as("b").isNotNull();

      // subProcessEnd
      assertThat(events.get(10).getType()).isEqualTo(ActivitiEventType.HISTORIC_ACTIVITY_INSTANCE_CREATED);
      assertThat((((HistoricActivityInstance) ((ActivitiEntityEvent) events.get(10)).getEntity()).getActivityId())).isEqualTo("subprocessEnd");

      assertThat(events.get(11).getType()).isEqualTo(ActivitiEventType.HISTORIC_ACTIVITY_INSTANCE_ENDED);
      assertThat((((HistoricActivityInstance) ((ActivitiEntityEvent) events.get(11)).getEntity()).getActivityId())).isEqualTo("subprocessEnd");
      assertThat((((HistoricActivityInstance) ((ActivitiEntityEvent) events.get(11)).getEntity()).getEndTime())).as("subprocessEnd").isNotNull();

      // subProcess end
      assertThat(events.get(12).getType()).isEqualTo(ActivitiEventType.HISTORIC_ACTIVITY_INSTANCE_ENDED);
      assertThat((((HistoricActivityInstance) ((ActivitiEntityEvent) events.get(12)).getEntity()).getActivityId())).isEqualTo("subProcess");
      assertThat((((HistoricActivityInstance) ((ActivitiEntityEvent) events.get(12)).getEntity()).getEndTime())).as("subProcess").isNotNull();

      // main end
      assertThat(events.get(13).getType()).isEqualTo(ActivitiEventType.HISTORIC_ACTIVITY_INSTANCE_CREATED);
      assertThat((((HistoricActivityInstance) ((ActivitiEntityEvent) events.get(13)).getEntity()).getActivityId())).isEqualTo("mainEnd");

      assertThat(events.get(14).getType()).isEqualTo(ActivitiEventType.HISTORIC_ACTIVITY_INSTANCE_ENDED);
      assertThat((((HistoricActivityInstance) ((ActivitiEntityEvent) events.get(14)).getEntity()).getActivityId())).isEqualTo("mainEnd");
      assertThat((((HistoricActivityInstance) ((ActivitiEntityEvent) events.get(14)).getEntity()).getEndTime())).as("mainEnd").isNotNull();

      // Process instance end
      assertThat(events.get(15).getType()).isEqualTo(ActivitiEventType.HISTORIC_PROCESS_INSTANCE_ENDED);

    }
  }

}

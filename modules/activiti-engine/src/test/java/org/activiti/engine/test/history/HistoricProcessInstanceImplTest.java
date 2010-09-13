/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.test.history;

import static org.junit.Assert.*;

import java.util.Date;

import org.activiti.engine.impl.history.HistoricProcessInstanceEntity;
import org.junit.Test;

/**
 * @author Christian Stettler
 */
public class HistoricProcessInstanceImplTest {

  @Test
  public void testInitializeHistoricProcessInstance() {
    Date startTime = new Date();

    HistoricProcessInstanceEntity historicProcessInstance = new HistoricProcessInstanceEntity("processInstanceId", "processInstanceId", startTime);

    assertEquals("processInstanceId", historicProcessInstance.getProcessInstanceId());
    assertEquals("processInstanceId", historicProcessInstance.getProcessDefinitionId());
    assertEquals(startTime, historicProcessInstance.getStartTime());

    assertNull(historicProcessInstance.getEndTime());
    assertNull(historicProcessInstance.getDurationInMillis());
    assertNull(historicProcessInstance.getEndActivityId());
  }

  @Test
  public void testMarkHistoricProcessInstanceEnded() {
    Date startTime = new Date();
    Date endTime = new Date(startTime.getTime() + 1234);

    HistoricProcessInstanceEntity historicProcessInstance = new HistoricProcessInstanceEntity("processInstanceId", "processInstanceId", startTime);
    historicProcessInstance.markEnded(endTime, "endState");

    assertEquals(endTime, historicProcessInstance.getEndTime());
    assertEquals(Long.valueOf(1234), historicProcessInstance.getDurationInMillis());
    assertEquals("endState", historicProcessInstance.getEndActivityId());
  }

  @Test
  public void testMandatoryStateForHistoricProcessInstanceInitialization() {
    assertIllegalArgumentException("process instance id", new Runnable() {
      public void run() {
        new HistoricProcessInstanceEntity(null, "processInstanceId", new Date());
      }
    });

    assertIllegalArgumentException("process definition id", new Runnable() {
      public void run() {
        new HistoricProcessInstanceEntity("processInstanceId", null, new Date());
      }
    });

    assertIllegalArgumentException("start time", new Runnable() {
      public void run() {
        new HistoricProcessInstanceEntity("processInstanceId", "processInstanceId", null);
      }
    });
  }

  @Test
  public void testEndTimeMustBeAfterStartTime() {
    assertIllegalArgumentException("before start time", new Runnable() {
      public void run() {
        Date startTime = new Date();
        Date endTime = new Date(startTime.getTime() - 1000);

        HistoricProcessInstanceEntity historicProcessInstance = new HistoricProcessInstanceEntity("processInstanceId", "processInstanceId", startTime);
        historicProcessInstance.markEnded(endTime, "endStateName");
      }
    });
  }

  private static void assertIllegalArgumentException(String message, Runnable runnable) {
    try {
      runnable.run();
      fail("expected IllegalArgumentException with message '" + message + "'");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().toLowerCase().contains(message));
    }
  }

}

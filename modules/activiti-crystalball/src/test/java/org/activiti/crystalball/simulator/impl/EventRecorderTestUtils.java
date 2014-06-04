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
package org.activiti.crystalball.simulator.impl;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.runtime.Clock;

import java.util.Calendar;

public final class EventRecorderTestUtils {

  protected EventRecorderTestUtils() {
    // static class
  }

  /**
   * increase clockUtils time
   * Simulation operates in milliseconds. Sometime could happen (especially during automated tests)
   * that 2 recorded events have the same time. In that case it is not possible to recognize order.
   */
  public static void increaseTime(Clock clock) {
    Calendar c = Calendar.getInstance();
    c.setTime(clock.getCurrentTime());
    c.add(Calendar.SECOND, 1);
    clock.setCurrentTime(c.getTime());
  }

  public static void closeProcessEngine(ProcessEngine processEngine, ActivitiEventListener listener) {
    if (listener != null) {
      final ProcessEngineConfigurationImpl processEngineConfiguration = ((ProcessEngineImpl) processEngine).getProcessEngineConfiguration();
      processEngineConfiguration.getEventDispatcher().removeEventListener(listener);
    }
    processEngine.close();
  }


}

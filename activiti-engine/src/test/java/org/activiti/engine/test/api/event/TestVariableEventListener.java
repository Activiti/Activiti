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

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiVariableEvent;

public class TestVariableEventListener implements ActivitiEventListener {

  private List<ActivitiEvent> eventsReceived;

  public TestVariableEventListener() {
    eventsReceived = new ArrayList<ActivitiEvent>();
  }

  public List<ActivitiEvent> getEventsReceived() {
    return eventsReceived;
  }

  public void clearEventsReceived() {
    eventsReceived.clear();
  }

  @Override
  public void onEvent(ActivitiEvent event) {
    if (event instanceof ActivitiVariableEvent) {
      eventsReceived.add(event);
    }
  }

  @Override
  public boolean isFailOnException() {
    return true;
  }

}

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

package org.activiti.impl.history;

import java.util.HashMap;
import java.util.Map;

import org.activiti.impl.event.Event;
import org.activiti.impl.event.EventListener;
import org.activiti.impl.event.type.EndActivityEvent;
import org.activiti.impl.event.type.EndProcessInstanceEvent;
import org.activiti.impl.event.type.StartActivityEvent;
import org.activiti.impl.event.type.StartProcessInstanceEvent;
import org.activiti.impl.execution.ExecutionImpl;
import org.activiti.impl.history.event.EndActivityHistoryEvent;
import org.activiti.impl.history.event.EndProcessInstanceHistoryEvent;
import org.activiti.impl.history.event.StartActivityHistoryEvent;
import org.activiti.impl.history.event.StartProcessInstanceHistoryEvent;


/**
 * @author Tom Baeyens
 */
public class HistoryEventListener implements EventListener {
  
  protected Map<Event, HistoryEvent> historyEvents;

  public HistoryEventListener() {
    historyEvents = new HashMap<Event, HistoryEvent>();
    historyEvents.put(EndActivityEvent.INSTANCE, EndActivityHistoryEvent.INSTANCE);
    historyEvents.put(EndProcessInstanceEvent.INSTANCE, EndProcessInstanceHistoryEvent.INSTANCE);
    historyEvents.put(StartActivityEvent.INSTANCE, StartActivityHistoryEvent.INSTANCE);
    historyEvents.put(StartProcessInstanceEvent.INSTANCE, StartProcessInstanceHistoryEvent.INSTANCE);
  }
  
  public void notify(ExecutionImpl execution, Event event) {
    HistoryEvent historyEvent = historyEvents.get(event);
    if (historyEvent!=null) {
      historyEvent.process(execution);
    }
  }
}

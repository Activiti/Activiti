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

package org.activiti.impl.event;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.activiti.pvm.event.ProcessEvent;
import org.activiti.pvm.event.ProcessEventBus;
import org.activiti.pvm.event.ProcessEventConsumer;

/**
 * Default implementation of the {@link org.activiti.pvm.event.ProcessEventBus}
 * used by the process virtual machine.
 *
 * @author Micha Kiener
 */
public class DefaultProcessEventBus implements ProcessEventBus {
  /** The map of statically registered consumers. */
  private final Map<Class<?>, List<ProcessEventConsumer<ProcessEvent>>> consumers = new ConcurrentHashMap<Class<?>, List<ProcessEventConsumer<ProcessEvent>>>();

  public void postEvent(ProcessEvent event) {
    dispatchEvent(event);
  }

  /**
   * Internal hook method to dispatch the given event.
   *
   * @param event the event to be dispatched
   */
  protected void dispatchEvent(ProcessEvent event) {
    // get the consumers for this event
    List<ProcessEventConsumer<ProcessEvent>> consumerList = consumers.get(event.getEventType());
    if (consumerList == null) {
      return;
    }

    for (ProcessEventConsumer<ProcessEvent> consumer : consumerList) {
      consumer.consumeEvent(event);
    }
  }

  public void subscribe(ProcessEventConsumer<? extends ProcessEvent> consumer, Class<?>... eventTypes) {
    for (Class<?> eventType : eventTypes) {
      List<ProcessEventConsumer<ProcessEvent>> consumerList = consumers.get(eventType);
      if (consumerList == null) {
        consumerList = new CopyOnWriteArrayList<ProcessEventConsumer<ProcessEvent>>();
        consumers.put(eventType, consumerList);
      }
      consumerList.add((ProcessEventConsumer<ProcessEvent>) consumer);
    }
  }

  public void unsubscribe(ProcessEventConsumer<? extends ProcessEvent> consumer) {
    for (Iterator<Map.Entry<Class<?>, List<ProcessEventConsumer<ProcessEvent>>>> it = consumers.entrySet().iterator(); it.hasNext();) {
      Map.Entry<Class<?>, List<ProcessEventConsumer<ProcessEvent>>> entry = it.next();
      List<ProcessEventConsumer<ProcessEvent>> consumerList = entry.getValue();
      if (consumerList.remove(consumer)) {
        // if the list of consumers is now empty, remove the entry from the map
        if (consumerList.isEmpty()) {
          it.remove();
        }
      }
    }
  }
}


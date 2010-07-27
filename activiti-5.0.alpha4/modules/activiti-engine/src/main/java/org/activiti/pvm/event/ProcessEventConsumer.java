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

package org.activiti.pvm.event;

/**
 * A process event consumer is responsible for handling {@link ProcessEvent}s
 * published through the {@link ProcessEventBus} and hence acts as a subscriber
 * to the event queue.
 *
 * @author Micha Kiener
 * @param <T> the type of process event handled by this consumer
 */
public interface ProcessEventConsumer<T extends ProcessEvent> {

  /**
   * Invoked by the {@link ProcessEventBus} to consume the given event. Events
   * are only being sent to consumers they subscribed for the type of event. If
   * more than one consumer is registered to receive a certain type of event,
   * they are chained and invoked in the same order they have been registered.
   * They obviously will receive the same event object so take care, if the
   * event object itself is changed.
   *
   * @param event the event to be consumed by this consumer
   */
  void consumeEvent(T event);
}

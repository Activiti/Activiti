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

import java.util.Map;

/**
 * This interface is common to all process events being triggered by the
 * workflow system and handled by the {@link org.activiti.pvm.event.ProcessEventBus}.
 * <p> Each event is identified through a type which is a class to provide type
 * safety and must extend the {@link org.activiti.pvm.event.ProcessEvent}
 * interface. It is a good practice to use the same class as the event itself,
 * however, in certain cases this might not be an option.<br/>
 *
 * @author Micha Kiener
 * @author Christian Stettler 
 *
 * @param <T> the type of the payload this event is handling
 */
public interface ProcessEvent<T> {
  /**
   * Returns the type of this event which is used within the bus to search for
   * an appropriate handler consuming this event. It is the mapping between
   * publisher (producer) and subscribers (consumers) and is handled by the
   * bus.
   *
   * @return the type of this event
   */
  Class<?> getEventType();

  /**
   * Returns the value for the header attribute with the given key, if any,
   * <code>null</code> otherwise.
   *
   * @param key the key with which the attribute has been stored
   * @param <A> the expected type of the attribute
   * @return the value or <code>null</code> if not available
   */
  <A> A getHeaderAttribute(String key);

  /**
   * Returns a map view on the header attributes of this event. Even though
   * header attributes are optional, the map being returned is never
   * <code>null</code> but might be empty, furthermore, it is immutable and must
   * not be changed.
   *
   * @return the map of header attributes for this event
   */
  Map<String, ?> getHeaderAttributesMap();

  /**
   * Returns the payload of this event. If the workflow system is used in a
   * distributed or clustered environment, it might be necessary to make the
   * payload serializable, depending on the type of event, if it needs to be
   * broadcasted or not.
   *
   * @return the optional payload for this event, might be <code>null</code>
   */
  T getPayload();
}

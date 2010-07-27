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

import java.util.Collections;
import java.util.Map;

import org.activiti.pvm.event.ProcessEvent;

/**
 * The abstract base class for a {@link org.activiti.pvm.event.ProcessEvent},
 * supporting header attributes and payload object.
 *
 * @author Micha Kiener
 * @author Christian Stettler
 */
public abstract class AbstractProcessEvent<T> implements ProcessEvent<T> {
  private final Map<String, Object> headerAttributesMap;
  private final T payload;

  /**
   * Standard constructor used to create a new process event.
   *
   * @param headerAttributesMap the optional map of header attributes
   * @param payload the optional payload
   */
  protected AbstractProcessEvent(Map<String, Object> headerAttributesMap, T payload) {
    this.headerAttributesMap = headerAttributesMap == null ? Collections.<String, Object>emptyMap() : Collections.unmodifiableMap(headerAttributesMap);
    this.payload = payload;
  }

  /**
   * This basic implementation just returns the class of this event, you have to
   * override it, if the type does not match the event implementing class.
   *
   * @see org.activiti.pvm.event.ProcessEvent#getEventType()
   */
  public Class<?> getEventType() {
    return this.getClass();
  }

  public Map<String, Object> getHeaderAttributesMap() {
    return headerAttributesMap;
  }

  public <A> A getHeaderAttribute(String key) {
    return (A) headerAttributesMap.get(key);
  }

  public T getPayload() {
    return payload;
  }
}

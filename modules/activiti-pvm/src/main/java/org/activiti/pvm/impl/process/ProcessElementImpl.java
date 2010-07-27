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

package org.activiti.pvm.impl.process;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.pvm.event.EventListener;


/** common properties for process definition, activity and transition 
 * including event listeners.
 * 
 * @author Tom Baeyens
 */
public class ProcessElementImpl {

  protected String id;
  protected String name;
  protected Map<String, List<EventListener>> eventListeners = new HashMap<String, List<EventListener>>();

  protected void setEventListeners(Map<String, List<EventListener>> eventListeners) {
    this.eventListeners = eventListeners;
  }

  public void addEventListener(String eventName, EventListener eventListener) {
    // TODO
  }
  
  public Map<String, List<EventListener>> getEventListeners() {
    return eventListeners;
  }

  
  public String getId() {
    return id;
  }

  
  public void setId(String id) {
    this.id = id;
  }

  
  public String getName() {
    return name;
  }

  
  public void setName(String name) {
    this.name = name;
  }
}

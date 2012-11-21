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

package org.activiti.explorer.ui.event;

import com.vaadin.ui.Component;
import com.vaadin.ui.Component.Event;


/**
 * Generic event to use when simething is submitted or cancelled. Optionally,
 * an object can be passed, representing data or state for this event.
 * 
 * @author Frederik Heremans
 */
public class SubmitEvent extends Event {

  private static final long serialVersionUID = 1L;
  
  public static final String SUBMITTED = "submit";
  public static final String CANCELLED = "cancel";
  
  private String type;
  private Object data;
  
  
  public SubmitEvent(Component source, String type) {
    this(source, type, null);
  }
  public SubmitEvent(Component source, String type, Object data) {
    super(source);
    this.type = type;
    this.data = data;
  }

  public String getType() {
    return type;
  }
  
  /**
   * Additional state or data for this event.
   */
  public Object getData() {
    return data;
  }
  
}

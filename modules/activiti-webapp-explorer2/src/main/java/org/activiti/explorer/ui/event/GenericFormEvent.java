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
 * Generic event to use when form is submitted or cancelled. Optionally,
 * an object can be passed, representing data or state for this event.
 * 
 * @author Frederik Heremans
 */
public class GenericFormEvent extends Event {

  private static final long serialVersionUID = 1L;
  
  public static final String FORM_SUBMITTED = "submit";
  public static final String FORM_CANCELLED = "cancel";
  
  private String type;
  private Object formData;
  
  
  public GenericFormEvent(Component source, String type) {
    this(source, type, null);
  }
  public GenericFormEvent(Component source, String type, Object formData) {
    super(source);
    this.type = type;
    this.formData = formData;
  }

  public String getType() {
    return type;
  }
  
  /**
   * Additional state or data for this event.
   */
  public Object getFormData() {
    return formData;
  }
  
}

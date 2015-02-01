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

package org.activiti.explorer.ui.form;

import org.activiti.explorer.ui.form.FormPropertiesForm.FormPropertiesEvent;

import com.vaadin.ui.Component.Event;
import com.vaadin.ui.Component.Listener;



/**
 * Listener that only handles {@link FormPropertiesEvent}. The handling
 * of the event is delegated on to handleFormSubmit and handleFormCancel methods
 * 
 * @see FormPropertiesForm
 * 
 * @author Frederik Heremans
 */
public abstract class FormPropertiesEventListener implements Listener {

  private static final long serialVersionUID = 7560512657831865244L;

  public final void componentEvent(Event event) {
    if(event instanceof FormPropertiesEvent) {
      FormPropertiesEvent propertyEvent = (FormPropertiesEvent) event;
      if(FormPropertiesEvent.TYPE_SUBMIT.equals(propertyEvent.getType())) {
        handleFormSubmit(propertyEvent);
      } else {
        handleFormCancel(propertyEvent);
      }
    }
  }
  
  protected abstract void handleFormSubmit(FormPropertiesEvent event);
  
  protected abstract void handleFormCancel(FormPropertiesEvent event);
}

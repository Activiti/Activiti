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

import com.vaadin.ui.Component.Event;
import com.vaadin.ui.Component.Listener;


/**
 * Listener that listens to {@link GenericFormEvent}s and delegates to
 * formSubmitted or formCancelled methods.
 * 
 * @author Frederik Heremans
 */
public abstract class GenericFormEventListener implements Listener {

  private static final long serialVersionUID = 1L;

  public final void componentEvent(Event event) {
    if(event instanceof GenericFormEvent) {
      GenericFormEvent gfe = (GenericFormEvent) event;
      if(GenericFormEvent.FORM_SUBMITTED.equals(gfe.getType())) {
         formSubmitted(gfe);
      } else {
        formCancelled(gfe);
      }
    }
  }
  
  /**
   * Called when form is submitted.
   */
  protected abstract void formSubmitted(GenericFormEvent event);
  
  /**
   * Called when form is cancelled.
   */
  protected abstract void formCancelled(GenericFormEvent event);
  
}

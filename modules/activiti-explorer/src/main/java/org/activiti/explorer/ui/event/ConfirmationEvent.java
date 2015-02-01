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
 * 
 * @author Frederik Heremans
 */
public class ConfirmationEvent extends Event {

  private static final long serialVersionUID = 1L;
  
  private boolean confirmed;
  
  public ConfirmationEvent(Component source, boolean confirmed) {
    super(source);
    this.confirmed = confirmed;
  }
  
  public boolean isConfirmed() {
    return confirmed;
  }
}

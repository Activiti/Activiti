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

package org.activiti.explorer.ui.custom;

import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.Window;


/**
 * Superclass for popup windows. 
 * Popup windows that inherit from this class can be closed using the 'escape' key.
 * 
 * @author Joram Barrez
 */
public class PopupWindow extends Window {

  private static final long serialVersionUID = 1L;
  
  public PopupWindow() {
    
  }
  
  public PopupWindow(String caption) {
    super(caption);
  }

  @Override
  public void attach() {
    super.attach();
    
    addActionHandler(new Handler() {
      public void handleAction(Action action, Object sender, Object target) {
        close();
      }
      
      public Action[] getActions(Object target, Object sender) {
        return new Action[] {new ShortcutAction("escape", ShortcutAction.KeyCode.ESCAPE, null)};
      }
    });
  }
  
}

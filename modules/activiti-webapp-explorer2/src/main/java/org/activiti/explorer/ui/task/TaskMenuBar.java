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

package org.activiti.explorer.ui.task;

import org.activiti.explorer.Constants;
import org.activiti.explorer.ui.MenuBar;
import org.activiti.explorer.ui.ViewManager;



/**
 * @author Joram Barrez
 */
public class TaskMenuBar extends MenuBar {
  
  private static final long serialVersionUID = 7957488256766569264L;

  public TaskMenuBar(ViewManager viewManager) {
    super(viewManager);
    addStyleName(Constants.STYLE_MENUBAR);
    
    createMenuBarButton("Inbox (12)");
    createMenuBarButton("Todo (4)");
    createMenuBarButton("Planned (9)");
    createMenuBarButton("Queued (58)");
    createMenuBarButton("Delegated (2)");
    createMenuBarButton("Archived");
    fillRemainingSpace();
  }

}

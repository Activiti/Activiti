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

import org.activiti.engine.task.Task;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.ui.mainlayout.ExplorerLayout;

import com.vaadin.ui.Label;


/**
 * @author Joram Barrez
 */
public class PriorityLabel extends Label {
  
  private static final long serialVersionUID = 1L;
  
  protected int priority;
  protected I18nManager i18nManager;

  public PriorityLabel(Task task, I18nManager i18nManager) {
    this.i18nManager = i18nManager;
    this.priority = task.getPriority();
   
    addStyleName(ExplorerLayout.STYLE_CLICKABLE);
    setSizeUndefined();
    setValue(priority);
  }
  
  public int getPriority() {
    return priority;
  }
  
  @Override
  public void setValue(Object newValue) {
    if (newValue instanceof Integer) {
      priority = (Integer) newValue;
      if (priority < Task.PRIORITY_NORMAL) {
        super.setValue(i18nManager.getMessage(Messages.TASK_PRIORITY_LOW));
        addStyleName(ExplorerLayout.STYLE_TASK_HEADER_PRIORITY_LOW);
      } else if (priority == Task.PRIORITY_NORMAL) {
        super.setValue(i18nManager.getMessage(Messages.TASK_PRIORITY_MEDIUM));
        addStyleName(ExplorerLayout.STYLE_TASK_HEADER_PRIORITY_MEDIUM);
      } else if (priority > Task.PRIORITY_NORMAL) {
        super.setValue(i18nManager.getMessage(Messages.TASK_PRIORITY_HIGH));
        addStyleName(ExplorerLayout.STYLE_TASK_HEADER_PRIORITY_HIGH);
      }
    } else {
      throw new IllegalArgumentException("Can only set integer as new value for PriorityLabel");
    }
  }

}

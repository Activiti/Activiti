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

import java.util.Arrays;

import org.activiti.engine.task.Task;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;

import com.vaadin.ui.ComboBox;


/**
 * @author Joram Barrez
 */
public class PriorityComboBox extends ComboBox {

  private static final long serialVersionUID = 1L;
  
  protected I18nManager i18nManager;
  
  public PriorityComboBox(I18nManager i18nManager) {
    super(null, Arrays.asList(
            i18nManager.getMessage(Messages.TASK_PRIORITY_LOW),
            i18nManager.getMessage(Messages.TASK_PRIORITY_MEDIUM),
            i18nManager.getMessage(Messages.TASK_PRIORITY_HIGH)));
    this.i18nManager = i18nManager;
    setValue(i18nManager.getMessage(Messages.TASK_PRIORITY_LOW));
    setNullSelectionAllowed(false);
    setInvalidAllowed(false);
    setImmediate(true);
    setWidth(125, UNITS_PIXELS);
  }
  
  public PriorityComboBox(I18nManager i18nManager, Object value) {
    this(i18nManager);
    
    setValue(value);
  }
  
  public int getPriority() {
    String value = getValue().toString();
    if (i18nManager.getMessage(Messages.TASK_PRIORITY_LOW).equals(value)) {
      return Task.PRIORITY_MINIUM;
    } else if (i18nManager.getMessage(Messages.TASK_PRIORITY_MEDIUM).equals(value)) {
      return Task.PRIORITY_NORMAL;
    } else {
      return Task.PRIORITY_MAXIMUM;
    }
  }

}

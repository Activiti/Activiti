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

import java.util.Calendar;

import org.activiti.engine.form.FormProperty;
import org.activiti.explorer.ExplorerApp;
import org.activiti.explorer.I18nManager;
import org.activiti.explorer.Messages;
import org.activiti.explorer.form.MonthFormType;

import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Field;


/**
 * @author Joram Barrez
 */
public class MonthFormPropertyRenderer extends AbstractFormPropertyRenderer {
  
  private static final long serialVersionUID = 1L;

  public MonthFormPropertyRenderer() {
    super(MonthFormType.class);
  }

  public Field getPropertyField(FormProperty formProperty) {
    ComboBox comboBox = new MonthCombobox(getPropertyLabel(formProperty));
    comboBox.setRequired(formProperty.isRequired());
    comboBox.setRequiredError(getMessage(Messages.FORM_FIELD_REQUIRED, getPropertyLabel(formProperty)));
    comboBox.setEnabled(formProperty.isWritable());

    // Fill combobox
    I18nManager i18nManager = ExplorerApp.get().getI18nManager();
    for (int i=0; i<12; i++) {
      comboBox.addItem(i);
      comboBox.setItemCaption(i, i18nManager.getMessage(Messages.MONTH_PREFIX + i));
    }
    
    // Select first
    comboBox.setNullSelectionAllowed(false);
    Calendar cal = Calendar.getInstance();
    comboBox.select(cal.get(Calendar.MONTH));
    
    return comboBox;
  }
    

  // See https://vaadin.com/forum/-/message_boards/view_message/142750
  public class MonthCombobox extends ComboBox {

    private static final long serialVersionUID = 1L;

    public MonthCombobox(String s) {
      super(s);
      pageLength = 20;
    }
  }
  
}

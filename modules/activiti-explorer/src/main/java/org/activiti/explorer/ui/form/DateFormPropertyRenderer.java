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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.activiti.engine.form.FormProperty;
import org.activiti.engine.impl.form.DateFormType;
import org.activiti.explorer.Messages;

import com.vaadin.ui.Field;
import com.vaadin.ui.PopupDateField;

/**
 * @author Frederik Heremans
 */
public class DateFormPropertyRenderer extends AbstractFormPropertyRenderer {

  public DateFormPropertyRenderer() {
    super(DateFormType.class);
  }

  @Override
  public Field getPropertyField(FormProperty formProperty) {
    // Writable string
    PopupDateField dateField = new PopupDateField(getPropertyLabel(formProperty));
    String datePattern = (String) formProperty.getType().getInformation("datePattern");
    dateField.setDateFormat(datePattern);
    dateField.setRequired(formProperty.isRequired());
    dateField.setRequiredError(getMessage(Messages.FORM_FIELD_REQUIRED, getPropertyLabel(formProperty)));
    dateField.setEnabled(formProperty.isWritable());

    if (formProperty.getValue() != null) {
      // Try parsing the current value
      SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern);

      try {
        Date date = dateFormat.parse(formProperty.getValue());
        dateField.setValue(date);
      } catch (ParseException e) {
        // TODO: what happens if current value is illegal date?
      }
    }
    return dateField;
  }
  
  @Override
  public String getFieldValue(FormProperty formProperty, Field field) {
    PopupDateField dateField = (PopupDateField) field;
    Date selectedDate = (Date) dateField.getValue();
    
    if(selectedDate != null) {
      // Use the datePattern specified in the form property type
      String datePattern = (String) formProperty.getType().getInformation("datePattern");
      SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern);
      return dateFormat.format(selectedDate);
    }
    
    return null;
  }

}

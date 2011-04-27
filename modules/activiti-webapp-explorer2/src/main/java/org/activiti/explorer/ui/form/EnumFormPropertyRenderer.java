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

import java.util.Map;
import java.util.Map.Entry;

import org.activiti.engine.form.FormProperty;
import org.activiti.engine.impl.form.EnumFormType;
import org.activiti.explorer.Messages;

import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Field;

/**
 * @author Frederik Heremans
 */
public class EnumFormPropertyRenderer extends AbstractFormPropertyRenderer {

  public EnumFormPropertyRenderer() {
    super(EnumFormType.class);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Field getPropertyField(FormProperty formProperty) {
    ComboBox comboBox = new ComboBox(getPropertyLabel(formProperty));
    comboBox.setRequired(formProperty.isRequired());
    comboBox.setRequiredError(getMessage(Messages.FORM_FIELD_REQUIRED, getPropertyLabel(formProperty)));
    comboBox.setEnabled(formProperty.isWritable());

    Map<String, String> values = (Map<String, String>) formProperty.getType().getInformation("values");
    if (values != null) {
      for (Entry<String, String> enumEntry : values.entrySet()) {
        // Add value and label (if any)
        comboBox.addItem(enumEntry.getKey());
        if (enumEntry.getValue() != null) {
          comboBox.setItemCaption(enumEntry.getKey(), enumEntry.getValue());
        }
      }
    }
    return comboBox;
  }
}

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

import org.activiti.engine.form.FormProperty;
import org.activiti.explorer.Messages;
import org.activiti.explorer.form.UserFormType;

import com.vaadin.ui.Field;

/**
 * @author Frederik Heremans
 */
public class UserFormPropertyRenderer extends AbstractFormPropertyRenderer {

  public UserFormPropertyRenderer() {
    super(UserFormType.class);
  }

  @Override
  public Field getPropertyField(FormProperty formProperty) {
    SelectUserField selectUserField = new SelectUserField(getPropertyLabel(formProperty));
    selectUserField.setRequired(formProperty.isRequired());
    selectUserField.setRequiredError(getMessage(Messages.FORM_FIELD_REQUIRED, getPropertyLabel(formProperty)));
    selectUserField.setEnabled(formProperty.isWritable());

    if (formProperty.getValue() != null) {
      selectUserField.setValue(formProperty.getValue());
    }

    return selectUserField;
  }

}

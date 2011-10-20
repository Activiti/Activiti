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
import org.activiti.engine.impl.form.BooleanFormType;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Field;

/**
 * @author Frederik Heremans
 */
public class BooleanFormPropertyRenderer extends AbstractFormPropertyRenderer {

  public BooleanFormPropertyRenderer() {
    super(BooleanFormType.class);
  }

  @Override
  public Field getPropertyField(FormProperty formProperty) {
    
    CheckBox checkBox = new CheckBox(getPropertyLabel(formProperty));
    checkBox.setRequired(formProperty.isRequired());
    checkBox.setEnabled(formProperty.isWritable());

    if (formProperty.getValue() != null) {
      checkBox.setValue(formProperty.getValue());
    }

    return checkBox;
  }

}

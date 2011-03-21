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
import org.activiti.engine.impl.form.StringFormType;

import com.vaadin.ui.Component;
import com.vaadin.ui.TextField;


/**
 * @author Frederik Heremans
 */
public class StringFormPropertyRenderer extends AbstractFormPropertyRenderer {

  public StringFormPropertyRenderer() {
    super(StringFormType.class);
  }

  @Override
  public Component getComponentProperty(FormProperty formProperty) {
    Component component = null;
    if(formProperty.isWritable()) {
      // Writable string
      TextField textField = new TextField();
      if(formProperty.getValue() != null) {
        textField.setValue(formProperty.getValue());
      }
      component = textField;
    } else {
      component = getDefaultReadonlyPropertyComponent(formProperty);
    }
    return component;
  }

}

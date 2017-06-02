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

package org.activiti.engine.impl.form;

import static org.activiti.engine.impl.form.FormTypeSupport.CONSTRUCTOR_WITH_FORM_PROPERTY;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.activiti.bpmn.model.FormProperty;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.form.AbstractFormType;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Tom Baeyens
 */
public class FormTypes {

  protected Map<String, AbstractFormType> formTypes = new HashMap<String, AbstractFormType>();

  public void addFormType(AbstractFormType formType) {
    formTypes.put(formType.getName(), formType);
  }

  public AbstractFormType parseFormPropertyType(FormProperty formProperty) {
    if (StringUtils.isEmpty(formProperty.getType())) {
      return null;
    }
    return getFormType(formProperty);
  }

  private AbstractFormType getFormType(FormProperty formProperty) {
    AbstractFormType formType = formTypes.get(formProperty.getType());
    if (formType == null) {
      throw new ActivitiIllegalArgumentException("unknown type '" + formProperty.getType() + "' " + formProperty.getId());
    }
    return getFormType(formProperty, formType);
  }

  private AbstractFormType getFormType(FormProperty formProperty, AbstractFormType formType) {
    Method method;
    try {
      method = formType.getClass().getMethod(CONSTRUCTOR_WITH_FORM_PROPERTY, FormProperty.class);
    } catch (Exception e) {
      return formType;
    }

    try {
      return (AbstractFormType) method.invoke(formType, formProperty);
    } catch (Exception e) {
      return null;
    }
  }

}

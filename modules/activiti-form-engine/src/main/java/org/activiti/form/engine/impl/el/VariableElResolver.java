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
package org.activiti.form.engine.impl.el;

import java.beans.FeatureDescriptor;
import java.util.Iterator;
import java.util.Map;

import javax.el.ELContext;
import javax.el.ELResolver;

/**
 * Implementation of an {@link ELResolver} that resolves expressions with the submitted form values and process variables.
 * 
 * @author Joram Barrez
 * @author Tijs Rademakers
 */
public class VariableElResolver extends ELResolver {

  protected Map<String, Object> variables;

  public VariableElResolver(Map<String, Object> variables) {
    this.variables = variables;
  }

  public Object getValue(ELContext context, Object base, Object property) {

    if (base == null) {
      String variable = (String) property; // according to javadoc, can only be a String

      if (variables.containsKey(variable)) {
        context.setPropertyResolved(true); // if not set, the next elResolver in the CompositeElResolver will be called
        return variables.get(variable);
      }
    }

    // property resolution (eg. bean.value) will be done by the
    // BeanElResolver (part of the CompositeElResolver)
    // It will use the bean resolved in this resolver as base.

    return null;
  }

  public boolean isReadOnly(ELContext context, Object base, Object property) {
    return true;
  }

  public void setValue(ELContext context, Object base, Object property, Object value) {
    // setting value is not implemented for form value expressions
  }

  public Class<?> getCommonPropertyType(ELContext arg0, Object arg1) {
    return Object.class;
  }

  public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext arg0, Object arg1) {
    return null;
  }

  public Class<?> getType(ELContext arg0, Object arg1, Object arg2) {
    return Object.class;
  }

}

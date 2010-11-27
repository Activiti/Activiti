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

import java.util.List;
import java.util.Map;


/**
 * @author Tom Baeyens
 */
public class EnumFormType extends AbstractFormType {

  protected Map<String, String> values;

  public EnumFormType(Map<String, String> values) {
    this.values = values;
  }

  public String getName() {
    return "enum";
  }
  
  @Override
  public Object getInformation(String key) {
    if (key.equals("values")) {
      return values;
    }
    return null;
  }

  @Override
  public Object convertFormValueToModelValue(String propertyValue) {
    // TODO
    return null;
  }

  @Override
  public String convertModelValueToFormValue(Object modelValue) {
    // TODO
    return null;
  }

}

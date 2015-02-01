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

import java.text.Format;
import java.text.ParseException;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.form.AbstractFormType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;


/**
 * @author Tom Baeyens
 */
public class DateFormType extends AbstractFormType {
  
  private static final long serialVersionUID = 1L;
  
  protected String datePattern; 
  protected Format dateFormat; 

  public DateFormType(String datePattern) {
    this.datePattern = datePattern;
    this.dateFormat = FastDateFormat.getInstance(datePattern);
  }
  
  public String getName() {
    return "date";
  }
  
  public Object getInformation(String key) {
    if ("datePattern".equals(key)) {
      return datePattern;
    }
    return null;
  }

  public Object convertFormValueToModelValue(String propertyValue) {
    if (StringUtils.isEmpty(propertyValue)) {
      return null;
    }
    try {
      return dateFormat.parseObject(propertyValue);
    } catch (ParseException e) {
      throw new ActivitiIllegalArgumentException("invalid date value "+propertyValue);
    }
  }

  public String convertModelValueToFormValue(Object modelValue) {
    if (modelValue == null) {
      return null;
    }
    return dateFormat.format(modelValue);
  }
}

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

package org.activiti.explorer;

import java.io.Serializable;
import java.util.Locale;

import org.springframework.context.MessageSource;


/**
 * @author Joram Barrez
 */
public class I18nManager implements Serializable {

  private static final long serialVersionUID = 1L;
  protected MessageSource messageSource;
  protected Locale locale;
  
  public String getMessage(String key) {
    checkLocale();
    return messageSource.getMessage(key, null, locale);
  }

  public String getMessage(String key, Object... arguments) {
    checkLocale();
    return messageSource.getMessage(key, arguments, locale);
  }
  
  public void setLocale(Locale locale) {
    this.locale = locale;
  }
  
  protected void checkLocale() {
    if (locale == null) {
      locale = ExplorerApp.get().getLocale();
    }
  }
  
  public void setMessageSource(MessageSource messageSource) {
    this.messageSource = messageSource;
  }
  
}

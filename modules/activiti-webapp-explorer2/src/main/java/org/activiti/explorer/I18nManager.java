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

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;


/**
 * @author Joram Barrez
 */
public class I18nManager {

  protected ResourceBundle messages;
  
  public I18nManager(Locale locale) {
    this.messages = ResourceBundle.getBundle(Constant.RESOURCE_BUNDLE, locale);
  }
  
  public String getMessage(String key) {
    return messages.getString(key);
  }

  public String getMessage(String key, Object... arguments) {
    return MessageFormat.format(messages.getString(key), arguments);
  }
  
}

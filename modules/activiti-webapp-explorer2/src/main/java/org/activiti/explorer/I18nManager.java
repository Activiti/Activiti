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
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;


/**
 * @author Joram Barrez
 */
public class I18nManager implements Serializable {

  private static final long serialVersionUID = 1L;
  protected ResourceBundle messages;
  
  public String getMessage(String key) {
    if (messages == null) {
      createResourceBundle();
    }
    return messages.getString(key);
  }

  public String getMessage(String key, Object... arguments) {
    if (messages == null) {
      createResourceBundle();
    }
    return MessageFormat.format(messages.getString(key), arguments);
  }
  
  public void createResourceBundle() {
    Locale locale = ExplorerApp.get().getLocale();
    this.messages = ResourceBundle.getBundle(Constants.RESOURCE_BUNDLE, locale);
  }
  
}

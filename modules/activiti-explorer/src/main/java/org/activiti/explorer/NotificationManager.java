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

import org.activiti.explorer.ui.MainWindow;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.ui.Window.Notification;


/**
 * @author Joram Barrez
 */
public class NotificationManager implements Serializable {
  
  private static final long serialVersionUID = 1L;

  @Autowired
  protected MainWindow mainWindow;
  
  @Autowired
  protected I18nManager i18nManager;
  
  public void showErrorNotification(String captionKey, String description) {
    mainWindow.showNotification(i18nManager.getMessage(captionKey), 
            "<br/>" + description, 
            Notification.TYPE_ERROR_MESSAGE);
  }
  
  public void showErrorNotification(String captionKey, Exception exception) {
    mainWindow.showNotification(i18nManager.getMessage(captionKey), 
            "<br/>" + exception.getMessage(), 
            Notification.TYPE_ERROR_MESSAGE);
  }
  
  public void showWarningNotification(String captionKey, String descriptionKey) {
    Notification notification = new Notification(i18nManager.getMessage(captionKey), 
            i18nManager.getMessage(descriptionKey), 
            Notification.TYPE_WARNING_MESSAGE);
    notification.setDelayMsec(-1); // click to hide
    mainWindow.showNotification(notification);
  }
  
  public void showWarningNotification(String captionKey, String descriptionKey, Object ... params) {
    Notification notification = new Notification(i18nManager.getMessage(captionKey) + "<br/>", 
            MessageFormat.format(i18nManager.getMessage(descriptionKey), params), 
            Notification.TYPE_WARNING_MESSAGE);
    notification.setDelayMsec(5000); // click to hide
    mainWindow.showNotification(notification);
  }
  
  public void showInformationNotification(String key) {
    mainWindow.showNotification(i18nManager.getMessage(key), Notification.TYPE_HUMANIZED_MESSAGE);
  }
  
  public void showInformationNotification(String key, Object ... params) {
    mainWindow.showNotification(MessageFormat.format(i18nManager.getMessage(key), params),
            Notification.TYPE_HUMANIZED_MESSAGE);
  }
  
  public void setMainWindow(MainWindow mainWindow) {
    this.mainWindow = mainWindow;
  }
  
  public void setI18nManager(I18nManager i18nManager) {
    this.i18nManager = i18nManager;
  }
  
}

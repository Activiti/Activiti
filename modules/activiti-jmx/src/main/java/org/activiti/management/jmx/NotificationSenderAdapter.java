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
package org.activiti.management.jmx;

import javax.management.Notification;
import javax.management.modelmbean.ModelMBeanNotificationBroadcaster;

import org.activiti.engine.ActivitiException;

/**
 * @author Saeid Mirzaei
 */
public final class NotificationSenderAdapter implements NotificationSender {

  protected ModelMBeanNotificationBroadcaster broadcaster;

  public NotificationSenderAdapter(ModelMBeanNotificationBroadcaster broadcaster) {
    this.broadcaster = broadcaster;
  }

  @Override
  public void sendNotification(Notification notification) {
    try {
      broadcaster.sendNotification(notification);
    } catch (Exception e) {
      throw new ActivitiException("Error sending notification", e);
    }
  }
}
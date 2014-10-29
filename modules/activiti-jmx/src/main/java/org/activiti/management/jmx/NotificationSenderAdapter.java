package org.activiti.management.jmx;

import javax.management.Notification;
import javax.management.modelmbean.ModelMBeanNotificationBroadcaster;


public final class NotificationSenderAdapter implements NotificationSender {
  ModelMBeanNotificationBroadcaster broadcaster;

  public NotificationSenderAdapter(ModelMBeanNotificationBroadcaster broadcaster) {
      this.broadcaster = broadcaster;
  }

  @Override
  public void sendNotification(Notification notification) {
      try {
          broadcaster.sendNotification(notification);
      } catch (Exception e) {
          throw new RuntimeException(e);
      }
  }
}
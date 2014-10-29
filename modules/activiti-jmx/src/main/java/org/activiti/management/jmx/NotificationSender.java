package org.activiti.management.jmx;

import javax.management.Notification;


public interface NotificationSender {

  /**
   * Send notification
   *
   * @param notification notification to send
   */
  void sendNotification(Notification notification);
}


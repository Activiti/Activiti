package org.activiti.management.jmx.annotations;

import org.activiti.management.jmx.NotificationSender;


public interface NotificationSenderAware {

  /**
   * {@link NotificationSender} to use for sending notifications.
   *
   * @param sender sender to use for sending notifications
   */
  void setNotificationSender(NotificationSender sender);
}

package org.activiti5.engine.delegate.event;

/**
 * An {@link org.activiti5.engine.delegate.event.ActivitiEvent} related to cancel event being sent when activiti
 * object is cancelled.
 *
 * @author martin.grofcik
 */
public interface ActivitiCancelledEvent extends ActivitiEvent{
  /**
   * @return the cause of the cancel event. Returns null, if no specific cause has been specified.
   */
  public Object getCause();
}

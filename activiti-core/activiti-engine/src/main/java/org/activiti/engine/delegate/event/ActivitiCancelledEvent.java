package org.activiti.engine.delegate.event;

/**
 * An {@link org.activiti.engine.delegate.event.ActivitiEvent} related to cancel event being sent when activiti object is cancelled.
 * 

 */
public interface ActivitiCancelledEvent extends ActivitiEvent {
  /**
   * @return the cause of the cancel event. Returns null, if no specific cause has been specified.
   */
  public Object getCause();
}

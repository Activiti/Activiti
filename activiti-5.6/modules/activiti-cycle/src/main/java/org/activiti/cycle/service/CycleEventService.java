package org.activiti.cycle.service;

/**
 * Cycle event service
 * 
 * @author daniel.meyer@camunda.com
 */
public interface CycleEventService {

  /**
   * Fire a cycle event
   */
  public <T> void fireEvent(T event);

}

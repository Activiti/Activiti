package org.activiti.cycle.event;

import org.activiti.cycle.annotations.CycleComponent;

/**
 * An interface for EventListeners. Can be implemented by a
 * {@link CycleComponent} to be notified of Cycle Events.
 * 
 * @author daniel.meyer@camunda.com
 */
public interface CycleEventListener<T> {

  void onEvent(T event);

}
